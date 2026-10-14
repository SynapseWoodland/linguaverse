package cn.linguaverse.learn;

import cn.linguaverse.common.R;
import cn.linguaverse.learn.dto.StageProgressViews.ProgressRequest;
import cn.linguaverse.learn.dto.StageProgressViews.ProgressResponse;
import cn.linguaverse.learn.service.StageProgressService;
import cn.linguaverse.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class LessonProgressController {

    private final StageProgressService stageProgressService;

    public LessonProgressController(StageProgressService stageProgressService) {
        this.stageProgressService = stageProgressService;
    }

    @PutMapping("/lessons/{id}/progress")
    public R<ProgressResponse> saveProgress(@PathVariable long id, @Valid @RequestBody ProgressRequest req) {
        return R.ok(stageProgressService.save(
                CurrentUser.requireId(), id, req.stage(), req.score()));
    }
}
