package cn.linguaverse.stats;

import cn.linguaverse.common.R;
import cn.linguaverse.security.CurrentUser;
import cn.linguaverse.stats.dto.StatsViews.DashboardVO;
import cn.linguaverse.stats.dto.StatsViews.TodayPlanVO;
import cn.linguaverse.stats.service.DashboardService;
import cn.linguaverse.stats.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final DashboardService dashboardService;
    private final RecommendationService recommendationService;

    public StatsController(DashboardService dashboardService,
                           RecommendationService recommendationService) {
        this.dashboardService = dashboardService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/dashboard")
    public R<DashboardVO> dashboard() {
        return R.ok(dashboardService.dashboard(CurrentUser.requireId()));
    }

    @GetMapping("/today/recommendations")
    public R<TodayPlanVO> today() {
        return R.ok(recommendationService.today(CurrentUser.requireId()));
    }
}
