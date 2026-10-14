package cn.linguaverse.community.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.community.dto.CommunityViews.*;
import cn.linguaverse.community.entity.Comment;
import cn.linguaverse.community.entity.Post;
import cn.linguaverse.community.entity.PostLike;
import cn.linguaverse.community.mapper.CommentMapper;
import cn.linguaverse.community.mapper.PostLikeMapper;
import cn.linguaverse.community.mapper.PostMapper;
import cn.linguaverse.learn.service.AchievementService;
import cn.linguaverse.user.User;
import cn.linguaverse.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Task 9：社区信息流。
 * 列表只出 deleted=0 / hidden=0；@TableLogic 自动过滤逻辑删除帖。
 * 点赞 toggle 同步 post_likes 与冗余 like_count；删帖仅作者（越权 403），评论物理级联。
 */
@Service
public class CommunityService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final PostLikeMapper likeMapper;
    private final UserMapper userMapper;
    private final AchievementService achievementService;

    public CommunityService(PostMapper postMapper, CommentMapper commentMapper,
                            PostLikeMapper likeMapper, UserMapper userMapper,
                            AchievementService achievementService) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.likeMapper = likeMapper;
        this.userMapper = userMapper;
        this.achievementService = achievementService;
    }

    public PageVO<PostVO> feed(long userId, String langCode, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 50);
        QueryWrapper<Post> qw = new QueryWrapper<Post>()
                .eq("hidden", false)
                .orderByDesc("created_at", "id");
        if (langCode != null && !langCode.isBlank()) {
            qw.eq("lang_code", langCode);
        }
        IPage<Post> result = postMapper.selectPage(new Page<>(p, s), qw);
        List<Post> posts = result.getRecords();

        Set<Long> likedIds = likedPostIds(userId, posts.stream().map(Post::getId).toList());
        Map<Long, User> authors = authorMap(posts.stream().map(Post::getAuthorId).toList());

        List<PostVO> items = posts.stream()
                .map(post -> toPostVO(post, authors.get(post.getAuthorId()), likedIds.contains(post.getId())))
                .toList();
        return new PageVO<>(p, s, result.getTotal(), (int) result.getPages(), items);
    }

    @Transactional
    public CreatePostVO create(long userId, CreatePostRequest req) {
        Post post = new Post();
        post.setAuthorId(userId);
        post.setLangCode(langOrNull(req.langCode()));
        post.setTitle(blankToNull(req.title()));
        post.setContent(req.content().trim());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setHidden(false);
        post.setDeleted(false);
        post.setCreatedAt(LocalDateTime.now());
        postMapper.insert(post);
        achievementService.evaluateOnFirstPost(userId);
        return new CreatePostVO(post.getId());
    }

    @Transactional
    public LikeVO toggleLike(long userId, long postId) {
        Post post = visiblePost(postId);
        PostLike existing = likeMapper.selectOne(new QueryWrapper<PostLike>()
                .eq("post_id", postId).eq("user_id", userId));
        LocalDateTime now = LocalDateTime.now();
        boolean liked;
        if (existing != null) {
            likeMapper.deleteById(existing.getId());
            post.setLikeCount(Math.max(0, nz(post.getLikeCount()) - 1));
            liked = false;
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setCreatedAt(now);
            try {
                likeMapper.insert(like);
                post.setLikeCount(nz(post.getLikeCount()) + 1);
            } catch (DuplicateKeyException e) {
                // 并发重复点赞：保持幂等，不重复计数
                post.setLikeCount(nz(post.getLikeCount()));
            }
            liked = true;
        }
        postMapper.updateById(post);
        return new LikeVO(postId, liked, post.getLikeCount());
    }

    public List<CommentVO> comments(long userId, long postId) {
        visiblePost(postId);
        List<Comment> comments = commentMapper.selectList(
                new QueryWrapper<Comment>().eq("post_id", postId).orderByAsc("created_at", "id"));
        Map<Long, User> authors = authorMap(comments.stream().map(Comment::getAuthorId).toList());
        return comments.stream()
                .map(c -> new CommentVO(c.getId(), c.getContent(),
                        toAuthor(authors.get(c.getAuthorId())), c.getCreatedAt()))
                .toList();
    }

    @Transactional
    public CommentVO addComment(long userId, long postId, String content) {
        Post post = visiblePost(postId);
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(userId);
        comment.setContent(content.trim());
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        post.setCommentCount(nz(post.getCommentCount()) + 1);
        postMapper.updateById(post);
        return new CommentVO(comment.getId(), comment.getContent(), toAuthor(userMapper.selectById(userId)),
                comment.getCreatedAt());
    }

    @Transactional
    public void deletePost(long userId, long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw BizException.notFound("帖子不存在");
        }
        if (!post.getAuthorId().equals(userId)) {
            throw BizException.forbidden("只能删除自己发布的帖子");
        }
        // 逻辑删帖 + 物理级联评论（FK 级联仅对物理删除生效，此处显式处理）
        commentMapper.delete(new QueryWrapper<Comment>().eq("post_id", postId));
        postMapper.deleteById(postId);
    }

    private Post visiblePost(long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || Boolean.TRUE.equals(post.getHidden())) {
            throw BizException.notFound("帖子不存在");
        }
        return post;
    }

    private Set<Long> likedPostIds(long userId, List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(likeMapper.selectList(new QueryWrapper<PostLike>()
                        .eq("user_id", userId).in("post_id", postIds))
                .stream().map(PostLike::getPostId).toList());
    }

    private Map<Long, User> authorMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, User> map = new HashMap<>();
        for (User u : userMapper.selectBatchIds(new HashSet<>(userIds))) {
            map.put(u.getId(), u);
        }
        return map;
    }

    private PostVO toPostVO(Post post, User author, boolean liked) {
        return new PostVO(post.getId(), post.getLangCode(), post.getTitle(), post.getContent(),
                nz(post.getLikeCount()), nz(post.getCommentCount()), liked,
                toAuthor(author), post.getCreatedAt());
    }

    private AuthorVO toAuthor(User u) {
        if (u == null) {
            return null;
        }
        return new AuthorVO(u.getId(), u.getUsername(), u.getNickname(), u.getAvatarSeed());
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    private static String langOrNull(String lang) {
        return lang == null || lang.isBlank() ? null : lang;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
