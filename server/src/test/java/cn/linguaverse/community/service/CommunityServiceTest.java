package cn.linguaverse.community.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.community.dto.CommunityViews.CommentVO;
import cn.linguaverse.community.dto.CommunityViews.CreatePostRequest;
import cn.linguaverse.community.dto.CommunityViews.CreatePostVO;
import cn.linguaverse.community.dto.CommunityViews.LikeVO;
import cn.linguaverse.community.dto.CommunityViews.PageVO;
import cn.linguaverse.community.dto.CommunityViews.PostVO;
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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Task 9 社区（TR-9.2）：发帖字段规整、点赞 toggle 三分支、评论计数、删帖权限与级联。 */
@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    private static final long USER_ID = 9L;
    private static final long POST_ID = 100L;

    @Mock
    PostMapper postMapper;
    @Mock
    CommentMapper commentMapper;
    @Mock
    PostLikeMapper likeMapper;
    @Mock
    UserMapper userMapper;
    @Mock
    AchievementService achievementService;

    CommunityService service;

    @BeforeEach
    void setUp() {
        service = new CommunityService(postMapper, commentMapper, likeMapper,
                userMapper, achievementService);
    }

    private Post post(long id, long authorId, int likeCount, int commentCount, boolean hidden) {
        Post p = new Post();
        p.setId(id);
        p.setAuthorId(authorId);
        p.setLangCode("en");
        p.setTitle("t" + id);
        p.setContent("c" + id);
        p.setLikeCount(likeCount);
        p.setCommentCount(commentCount);
        p.setHidden(hidden);
        p.setDeleted(false);
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    private User author(long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("u" + id);
        u.setNickname("n" + id);
        u.setAvatarSeed("s" + id);
        return u;
    }

    // ---------- 发帖 ----------

    @Test
    @DisplayName("发帖：内容/标题去空白、计数与标记初始化、触发首帖成就")
    void createPost() {
        when(postMapper.insert(any(Post.class))).thenAnswer(inv -> {
            inv.getArgument(0, Post.class).setId(POST_ID);
            return 1;
        });

        CreatePostVO vo = service.create(USER_ID,
                new CreatePostRequest("en", "  标题  ", "  正文  "));

        assertThat(vo.id()).isEqualTo(POST_ID);
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).insert(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getAuthorId()).isEqualTo(USER_ID);
        assertThat(saved.getTitle()).isEqualTo("标题");
        assertThat(saved.getContent()).isEqualTo("正文");
        assertThat(saved.getLikeCount()).isZero();
        assertThat(saved.getCommentCount()).isZero();
        assertThat(saved.getHidden()).isFalse();
        assertThat(saved.getDeleted()).isFalse();
        verify(achievementService).evaluateOnFirstPost(USER_ID);
    }

    @Test
    @DisplayName("发帖：空白标题/语种归一为 null")
    void createPostBlankToNull() {
        when(postMapper.insert(any(Post.class))).thenAnswer(inv -> {
            inv.getArgument(0, Post.class).setId(POST_ID);
            return 1;
        });

        service.create(USER_ID, new CreatePostRequest("  ", "   ", "正文"));

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).insert(captor.capture());
        assertThat(captor.getValue().getTitle()).isNull();
        assertThat(captor.getValue().getLangCode()).isNull();
    }

    // ---------- 点赞 toggle ----------

    @Test
    @DisplayName("点赞：无记录 → 插入并 +1")
    void likeAdds() {
        Post p = post(POST_ID, 7L, 3, 0, false);
        when(postMapper.selectById(POST_ID)).thenReturn(p);
        when(likeMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        LikeVO vo = service.toggleLike(USER_ID, POST_ID);

        assertThat(vo.liked()).isTrue();
        assertThat(vo.likeCount()).isEqualTo(4);
        verify(likeMapper).insert(any(PostLike.class));
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).updateById(captor.capture());
        assertThat(captor.getValue().getLikeCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("取消点赞：删记录并 -1，0 为下限")
    void unlikeRemoves() {
        Post p = post(POST_ID, 7L, 1, 0, false);
        when(postMapper.selectById(POST_ID)).thenReturn(p);
        PostLike existing = new PostLike();
        existing.setId(5L);
        existing.setPostId(POST_ID);
        existing.setUserId(USER_ID);
        when(likeMapper.selectOne(any(QueryWrapper.class))).thenReturn(existing);

        LikeVO vo = service.toggleLike(USER_ID, POST_ID);

        assertThat(vo.liked()).isFalse();
        assertThat(vo.likeCount()).isZero();
        verify(likeMapper).deleteById(5L);
        verify(likeMapper, never()).insert(any(PostLike.class));
    }

    @Test
    @DisplayName("并发重复点赞（唯一键冲突）→ 幂等不重复计数")
    void likeDuplicateKeyIdempotent() {
        Post p = post(POST_ID, 7L, 3, 0, false);
        when(postMapper.selectById(POST_ID)).thenReturn(p);
        when(likeMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(likeMapper.insert(any(PostLike.class)))
                .thenThrow(new DuplicateKeyException("uk_pl_post_user"));

        LikeVO vo = service.toggleLike(USER_ID, POST_ID);

        assertThat(vo.liked()).isTrue();
        assertThat(vo.likeCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("点赞：帖子不存在 → 404，隐藏帖 → 404")
    void likeHiddenOrMissing404() {
        when(postMapper.selectById(POST_ID)).thenReturn(null);
        assertThatThrownBy(() -> service.toggleLike(USER_ID, POST_ID))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));

        when(postMapper.selectById(POST_ID)).thenReturn(post(POST_ID, 7L, 0, 0, true));
        assertThatThrownBy(() -> service.toggleLike(USER_ID, POST_ID))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));
    }

    // ---------- 评论 ----------

    @Test
    @DisplayName("发评论：内容去空白、commentCount +1、回执带作者")
    void addComment() {
        Post p = post(POST_ID, 7L, 0, 2, false);
        when(postMapper.selectById(POST_ID)).thenReturn(p);
        when(commentMapper.insert(any(Comment.class))).thenAnswer(inv -> {
            inv.getArgument(0, Comment.class).setId(50L);
            return 1;
        });
        when(userMapper.selectById(USER_ID)).thenReturn(author(USER_ID));

        CommentVO vo = service.addComment(USER_ID, POST_ID, "  好帖  ");

        assertThat(vo.id()).isEqualTo(50L);
        assertThat(vo.content()).isEqualTo("好帖");
        assertThat(vo.author().id()).isEqualTo(USER_ID);
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).updateById(captor.capture());
        assertThat(captor.getValue().getCommentCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("评论列表：按时间映射、作者补齐")
    void commentsList() {
        when(postMapper.selectById(POST_ID)).thenReturn(post(POST_ID, 7L, 0, 0, false));
        Comment c = new Comment();
        c.setId(50L);
        c.setPostId(POST_ID);
        c.setAuthorId(USER_ID);
        c.setContent("前排");
        c.setCreatedAt(LocalDateTime.now());
        when(commentMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(c));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(author(USER_ID)));

        List<CommentVO> list = service.comments(USER_ID, POST_ID);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).content()).isEqualTo("前排");
        assertThat(list.get(0).author().username()).isEqualTo("u9");
    }

    // ---------- 删帖 ----------

    @Test
    @DisplayName("作者删帖：逻辑删帖 + 物理级联评论")
    void authorDeletes() {
        when(postMapper.selectById(POST_ID)).thenReturn(post(POST_ID, USER_ID, 0, 0, false));

        service.deletePost(USER_ID, POST_ID);

        verify(commentMapper).delete(any(QueryWrapper.class));
        verify(postMapper).deleteById(POST_ID);
    }

    @Test
    @DisplayName("越权删帖 → 403，不执行任何删除")
    void strangerDelete403() {
        when(postMapper.selectById(POST_ID)).thenReturn(post(POST_ID, 7L, 0, 0, false));

        assertThatThrownBy(() -> service.deletePost(USER_ID, POST_ID))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(403));

        verify(commentMapper, never()).delete(any(QueryWrapper.class));
        verify(postMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("删除不存在的帖子 → 404")
    void deleteMissing404() {
        when(postMapper.selectById(POST_ID)).thenReturn(null);
        assertThatThrownBy(() -> service.deletePost(USER_ID, POST_ID))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));
    }

    // ---------- 信息流 ----------

    @Test
    @DisplayName("信息流：liked 标记与作者映射正确")
    void feedMapsLikedAndAuthor() {
        Post mine = post(1L, USER_ID, 2, 0, false);
        Post other = post(2L, 7L, 5, 1, false);
        Page<Post> page = new Page<>(1, 10, 2);
        page.setRecords(List.of(mine, other));
        when(postMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        PostLike like = new PostLike();
        like.setPostId(2L);
        like.setUserId(USER_ID);
        when(likeMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(like));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(author(USER_ID), author(7L)));

        PageVO<PostVO> vo = service.feed(USER_ID, "en", null, null);

        assertThat(vo.total()).isEqualTo(2);
        assertThat(vo.items()).hasSize(2);
        PostVO first = vo.items().get(0);
        PostVO second = vo.items().get(1);
        assertThat(first.liked()).isFalse();
        assertThat(second.liked()).isTrue();
        assertThat(second.likeCount()).isEqualTo(5);
        assertThat(second.author().nickname()).isEqualTo("n7");
    }
}
