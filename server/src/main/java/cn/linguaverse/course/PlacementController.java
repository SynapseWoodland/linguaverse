package cn.linguaverse.course;

import cn.linguaverse.common.R;
import cn.linguaverse.course.dto.PlacementViews.PlacementPaperVO;
import cn.linguaverse.course.dto.PlacementViews.ResultVO;
import cn.linguaverse.course.dto.PlacementViews.SubmitRequest;
import cn.linguaverse.course.service.PlacementService;
import cn.linguaverse.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/placement")
public class PlacementController {

    private final PlacementService placementService;

    public PlacementController(PlacementService placementService) {
        this.placementService = placementService;
    }

    @GetMapping("/{lang}")
    public R<PlacementPaperVO> paper(@PathVariable String lang) {
        return R.ok(placementService.paper(lang));
    }

    @PostMapping("/{lang}")
    public R<ResultVO> submit(@PathVariable String lang, @Valid @RequestBody SubmitRequest req) {
        return R.ok(placementService.submit(CurrentUser.requireId(), lang, req.answers()));
    }
}
