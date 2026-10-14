package cn.linguaverse.course;

import cn.linguaverse.common.R;
import cn.linguaverse.course.dto.CourseViews.LanguageVO;
import cn.linguaverse.course.dto.CourseViews.LessonDetailVO;
import cn.linguaverse.course.service.CourseService;
import cn.linguaverse.security.CurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/courses")
    public R<List<LanguageVO>> languages() {
        return R.ok(courseService.listLanguages(CurrentUser.requireId()));
    }

    @GetMapping("/courses/{lang}")
    public R<LanguageVO> courseTree(@PathVariable String lang) {
        return R.ok(courseService.courseTree(CurrentUser.requireId(), lang));
    }

    @GetMapping("/lessons/{id}")
    public R<LessonDetailVO> lesson(@PathVariable long id) {
        return R.ok(courseService.lessonDetail(CurrentUser.requireId(), id));
    }
}
