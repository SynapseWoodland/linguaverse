package cn.linguaverse.community;

import cn.linguaverse.common.R;
import cn.linguaverse.community.dto.CommunityViews.*;
import cn.linguaverse.community.service.CommunityService;
import cn.linguaverse.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@Validated
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping
    public R<PageVO<PostVO>> feed(
            @RequestParam(required = false)
            @Pattern(regexp = "^(en|ja|ko)$", message = "语种仅支持 en/ja/ko") String lang,
            @RequestParam(required = false) @Min(value = 1, message = "页码从 1 开始") Integer page,
            @RequestParam(required = false) @Min(value = 1, message = "每页条数至少 1")
            @Max(value = 50, message = "每页条数最多 50") Integer size) {
        return R.ok(communityService.feed(CurrentUser.requireId(), lang, page, size));
    }

    @PostMapping
    public R<CreatePostVO> create(@Valid @RequestBody CreatePostRequest req) {
        return R.ok(communityService.create(CurrentUser.requireId(), req));
    }

    @PostMapping("/{id}/like")
    public R<LikeVO> toggleLike(@PathVariable long id) {
        return R.ok(communityService.toggleLike(CurrentUser.requireId(), id));
    }

    @GetMapping("/{id}/comments")
    public R<List<CommentVO>> comments(@PathVariable long id) {
        return R.ok(communityService.comments(CurrentUser.requireId(), id));
    }

    @PostMapping("/{id}/comments")
    public R<CommentVO> addComment(@PathVariable long id, @Valid @RequestBody CreateCommentRequest req) {
        return R.ok(communityService.addComment(CurrentUser.requireId(), id, req.content()));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable long id) {
        communityService.deletePost(CurrentUser.requireId(), id);
        return R.ok(null);
    }
}
