package cn.linguaverse.learn;

import cn.linguaverse.common.R;
import cn.linguaverse.learn.dto.CompletionViews.CompleteRequest;
import cn.linguaverse.learn.dto.CompletionViews.CompleteVO;
import cn.linguaverse.learn.service.CompletionService;
import cn.linguaverse.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class LessonCompletionController {

    private final CompletionService completionService;

    public LessonCompletionController(CompletionService completionService) {
        this.completionService = completionService;
    }

    @PostMapping("/lessons/{id}/complete")
    public R<CompleteVO> complete(@PathVariable long id, @Valid @RequestBody CompleteRequest req) {
        return R.ok(completionService.complete(CurrentUser.requireId(), id, req.durationSec()));
    }
}
