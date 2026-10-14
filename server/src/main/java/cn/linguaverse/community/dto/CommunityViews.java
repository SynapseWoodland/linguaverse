package cn.linguaverse.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** Task 9：社区帖子、评论、点赞视图。 */
public final class CommunityViews {
    private CommunityViews() {}

    public record AuthorVO(long id, String username, String nickname, String avatarSeed) {}

    public record PostVO(long id, String langCode, String title, String content,
                         int likeCount, int commentCount, boolean liked,
                         AuthorVO author, LocalDateTime createdAt) {}

    public record PageVO<T>(int page, int size, long total, int totalPages, List<T> items) {}

    public record CreatePostRequest(
            @Pattern(regexp = "^(en|ja|ko)$", message = "语种仅支持 en/ja/ko")
            String langCode,
            @Size(max = 128, message = "标题最长 128 个字符")
            String title,
            @NotBlank(message = "内容不能为空")
            @Size(max = 2000, message = "内容最长 2000 个字符")
            String content) {}

    public record CreatePostVO(long id) {}

    public record LikeVO(long postId, boolean liked, int likeCount) {}

    public record CommentVO(long id, String content, AuthorVO author, LocalDateTime createdAt) {}

    public record CreateCommentRequest(
            @NotBlank(message = "评论内容不能为空")
            @Size(max = 1000, message = "评论最长 1000 个字符")
            String content) {}
}
