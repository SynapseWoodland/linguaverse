package cn.linguaverse.learn;

import cn.linguaverse.common.R;
import cn.linguaverse.learn.dto.EnrollRequest;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.service.EnrollmentService;
import cn.linguaverse.security.CurrentUser;
import cn.linguaverse.user.dto.UserViews.EnrollmentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /** 课程中心加入语种（未定级时 placementLevel 为空）。幂等：重复加入不报错。 */
    @PostMapping
    public R<EnrollmentVO> enroll(@Valid @RequestBody EnrollRequest req) {
        long uid = CurrentUser.requireId();
        Enrollment e = enrollmentService.upsert(uid, req.getLangCode(), null);
        return R.ok(EnrollmentVO.of(e));
    }
}
