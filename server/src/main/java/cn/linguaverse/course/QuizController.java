package cn.linguaverse.course;

import cn.linguaverse.common.R;
import cn.linguaverse.course.dto.QuizViews.*;
import cn.linguaverse.course.service.QuizService;
import cn.linguaverse.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/lessons/{id}/grammar/submit")
    public R<GrammarResultVO> grammar(@PathVariable long id, @Valid @RequestBody GrammarSubmitRequest req) {
        return R.ok(quizService.gradeGrammar(CurrentUser.requireId(), id, req.answers()));
    }

    @PostMapping("/lessons/{id}/listening/submit")
    public R<ListeningResultVO> listening(@PathVariable long id, @Valid @RequestBody ListeningSubmitRequest req) {
        return R.ok(quizService.gradeListening(CurrentUser.requireId(), id, req.choiceIndex()));
    }

    @PostMapping("/lessons/{id}/speaking/submit")
    public R<SpeakingResultVO> speaking(@PathVariable long id, @Valid @RequestBody SpeakingSubmitRequest req) {
        return R.ok(quizService.gradeSpeaking(CurrentUser.requireId(), id, req));
    }
}
