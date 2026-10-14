package cn.linguaverse.learn;

import cn.linguaverse.common.R;
import cn.linguaverse.learn.dto.ReviewViews.DueQueueVO;
import cn.linguaverse.learn.dto.ReviewViews.RateRequest;
import cn.linguaverse.learn.dto.ReviewViews.WordCardVO;
import cn.linguaverse.learn.service.SrsService;
import cn.linguaverse.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@Validated
public class ReviewController {

    private final SrsService srsService;

    public ReviewController(SrsService srsService) {
        this.srsService = srsService;
    }

    @GetMapping("/due")
    public R<DueQueueVO> due(@RequestParam @Pattern(regexp = "^(en|ja|ko)$", message = "语种仅支持 en/ja/ko") String lang,
                             @RequestParam(required = false) Integer limit) {
        return R.ok(srsService.due(CurrentUser.requireId(), lang, limit));
    }

    @PostMapping("/rate")
    public R<WordCardVO> rate(@Valid @RequestBody RateRequest req) {
        return R.ok(srsService.rate(CurrentUser.requireId(),
                req.langCode(), req.word(), req.rating()));
    }
}
