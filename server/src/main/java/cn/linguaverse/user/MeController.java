package cn.linguaverse.user;

import cn.linguaverse.common.BizException;
import cn.linguaverse.common.R;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.service.EnrollmentService;
import cn.linguaverse.security.CurrentUser;
import cn.linguaverse.user.dto.OnboardingRequest;
import cn.linguaverse.user.dto.UpdateMeRequest;
import cn.linguaverse.user.dto.UserViews.EnrollmentVO;
import cn.linguaverse.user.dto.UserViews.MeResponse;
import cn.linguaverse.user.dto.UserViews.UserVO;
import cn.linguaverse.user.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me")
public class MeController {

    private final UserMapper userMapper;
    private final EnrollmentService enrollmentService;

    public MeController(UserMapper userMapper, EnrollmentService enrollmentService) {
        this.userMapper = userMapper;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public R<MeResponse> me() {
        long uid = CurrentUser.requireId();
        List<EnrollmentVO> enrollments = enrollmentService.listByUser(uid).stream()
                .map(EnrollmentVO::of).toList();
        return R.ok(new MeResponse(UserVO.of(requireUser(uid)), enrollments));
    }

    @PatchMapping
    public R<UserVO> updateMe(@Valid @RequestBody UpdateMeRequest req) {
        long uid = CurrentUser.requireId();
        User u = requireUser(uid);
        if (req.getNickname() != null) {
            u.setNickname(req.getNickname());
        }
        if (req.getBio() != null) {
            u.setBio(req.getBio());
        }
        if (req.getAvatarSeed() != null) {
            u.setAvatarSeed(req.getAvatarSeed());
        }
        if (req.getDailyGoalXp() != null) {
            if (!java.util.Set.of(10, 30, 60, 100).contains(req.getDailyGoalXp())) {
                throw BizException.badRequest("每日目标仅支持 10 / 30 / 60 / 100 XP 四档");
            }
            u.setDailyGoalXp(req.getDailyGoalXp());
        }
        userMapper.updateById(u);
        return R.ok(UserVO.of(u));
    }

    /**
     * Onboarding 三路径落库：
     * PLACEMENT 建空定级 enrollment；DIRECT 直接按 L1；SKIP 不产生学习记录。
     * 三种模式均标记引导已完成（onboarded=true，幂等），后续登录不再强制进入引导。
     */
    @PostMapping("/onboarding")
    public R<MeResponse> onboarding(@Valid @RequestBody OnboardingRequest req) {
        long uid = CurrentUser.requireId();
        if ("DIRECT".equals(req.getMode())) {
            enrollmentService.upsert(uid, req.getLangCode(), 1);
        } else if ("PLACEMENT".equals(req.getMode())) {
            enrollmentService.upsert(uid, req.getLangCode(), null);
        }
        // SKIP：不产生 enrollment，但仍确认引导完成
        User u = requireUser(uid);
        if (!Boolean.TRUE.equals(u.getOnboarded())) {
            u.setOnboarded(true);
            userMapper.updateById(u);
        }
        List<Enrollment> enrollments = enrollmentService.listByUser(uid);
        return R.ok(new MeResponse(UserVO.of(u),
                enrollments.stream().map(EnrollmentVO::of).toList()));
    }

    private User requireUser(long uid) {
        User u = userMapper.selectById(uid);
        if (u == null) {
            throw BizException.notFound("用户不存在");
        }
        return u;
    }
}
