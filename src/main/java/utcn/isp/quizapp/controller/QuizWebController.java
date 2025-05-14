package utcn.isp.quizapp.controller;

import utcn.isp.quizapp.model.Question;
import utcn.isp.quizapp.service.Leaderboard;
import utcn.isp.quizapp.service.QuizSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import jakarta.servlet.http.HttpSession; // For Spring Boot 3+
// import javax.servlet.http.HttpSession; // For Spring Boot 2.x

@Controller
public class QuizWebController {

    private final QuizSessionService quizSessionService;
    private final Leaderboard leaderboard;

    @Autowired
    public QuizWebController(QuizSessionService quizSessionService, Leaderboard leaderboard) {
        this.quizSessionService = quizSessionService;
        this.leaderboard = leaderboard;
    }

    @GetMapping("/")
    public String index(SessionStatus sessionStatus, HttpSession session) {
        // If coming back to index, effectively end previous game by invalidating session service instance
        // or resetting its state. @SessionScope handles instance per session.
        // For a cleaner start, we can explicitly reset or rely on new session if old one timed out.
        // Or, if QuizSessionService is designed to be reset:
        // quizSessionService.resetGame();
        return "index";
    }

    @PostMapping("/start")
    public String startGame(@RequestParam("username") String username, Model model) {
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Username cannot be empty.");
            return "index";
        }
        quizSessionService.startNewGame(username.trim());
        if (!quizSessionService.hasNextQuestion()) {
             model.addAttribute("error", "No questions available to start the quiz.");
             return "index"; // Or a specific error page
        }
        return "redirect:/quiz";
    }

    @GetMapping("/quiz")
    public String quizPage(Model model, HttpSession session) {
        if (!quizSessionService.isGameActive()) {
            return "redirect:/"; // No active game, go to start page
        }

        if (quizSessionService.isTimeUp() || !quizSessionService.hasNextQuestion()) {
            return "redirect:/gameOver";
        }

        Question currentQuestion = quizSessionService.getCurrentQuestion();
        model.addAttribute("question", currentQuestion);
        model.addAttribute("game", quizSessionService.getCurrentGame()); 
        // For timer display, pass remaining time
        long elapsedTime = System.currentTimeMillis() - quizSessionService.getCurrentGame().getStartTime();
        long quizDurationMs = 60 * 1000; // Should be consistent with QuizSessionService
        long remainingTime = Math.max(0, (quizDurationMs - elapsedTime) / 1000); // in seconds
        model.addAttribute("remainingTime", remainingTime);

        return "quiz";
    }

    @PostMapping("/submitAnswer")
    public String submitAnswer(@RequestParam("answer") int selectedOptionIndex,
                               Model model) {
        if (!quizSessionService.isGameActive() || quizSessionService.isTimeUp()) {
            return "redirect:/gameOver";
        }

        boolean correct = quizSessionService.submitAnswer(selectedOptionIndex);

        if (!correct || !quizSessionService.hasNextQuestion() || quizSessionService.isTimeUp()) {
            return "redirect:/gameOver";
        }
        
        return "redirect:/quiz";
    }

    @GetMapping("/gameOver")
    public String gameOver(Model model, SessionStatus sessionStatus, HttpSession httpSession) {
        if (!quizSessionService.isGameActive()) {
             // If no game was active, perhaps redirect to index or show a generic message
            model.addAttribute("message", "No active game found or game already ended.");
            model.addAttribute("leaderboard", leaderboard.getAllScoresSorted());
            return "gameOver"; // Still show leaderboard
        }
        
        leaderboard.addScore(quizSessionService.getUserName(), quizSessionService.getScore());
        model.addAttribute("username", quizSessionService.getUserName());
        model.addAttribute("score", quizSessionService.getScore());
        model.addAttribute("leaderboard", leaderboard.getAllScoresSorted());
        
        // quizSessionService.endGame(); // Perform any cleanup in session service
        // SessionStatus.setComplete() can be used if the controller is @SessionAttributes managed.
        // For @SessionScope beans, their lifecycle is tied to the HTTP session.
        // To ensure the QuizSessionService bean is fresh for a new game if the user navigates back,
        // one might invalidate the session or rely on the user starting a "new game" via POST /start
        // which re-initializes the QuizSessionService's state.
        // httpSession.removeAttribute("scopedTarget.quizSessionService"); // One way to force re-creation on next access
        // Or, more simply, ensure startNewGame fully reinitializes the QuizSessionService state.

        return "gameOver";
    }
}
