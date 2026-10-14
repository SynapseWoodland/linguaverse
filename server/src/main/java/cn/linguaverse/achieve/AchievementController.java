package cn.linguaverse.achieve;

import cn.linguaverse.achieve.dto.AchievementVO;
import cn.linguaverse.achieve.service.AchievementQueryService;
import cn.linguaverse.common.R;
import cn.linguaverse.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AchievementController {

    private final AchievementQueryService queryService;

    public AchievementController(AchievementQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/achievements")
    public R<List<AchievementVO>> list() {
        return R.ok(queryService.list(CurrentUser.requireId()));
    }
}
