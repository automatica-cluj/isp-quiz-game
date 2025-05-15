package utcn.isp.quizapp.controller;

import utcn.isp.quizapp.model.Question;
import utcn.isp.quizapp.service.Leaderboard;
import utcn.isp.quizapp.service.QuizSessionService;
import utcn.isp.quizapp.service.ActiveSessionsService;
import utcn.isp.quizapp.service.CompletedQuizService; // Added import
import utcn.isp.quizapp.model.CompletedQuiz; // Added import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import jakarta.servlet.http.HttpServletResponse; // Added import
import jakarta.servlet.http.HttpSession; // For Spring Boot 3+
// import javax.servlet.http.HttpSession; // For Spring Boot 2.x
import java.io.InputStream; // Added import
import java.io.OutputStream; // Added import
import java.nio.file.Files; // Added import
import java.nio.file.Path; // Added import
import java.nio.file.Paths; // Added import
import java.io.IOException; // Added import

@Controller
public class QuizWebController {
    /// xyz
    private final QuizSessionService quizSessionService;
    private final Leaderboard leaderboard;
    private final ActiveSessionsService activeSessionsService;
    private final CompletedQuizService completedQuizService; // Added dependency

    @Value("${dashboard.password}") // Inject password from application.properties
    private String expectedDashboardPassword;

    @Value("${quiz.leaderboard.save-path:leaderboard_data.txt}") // Inject leaderboard file path
    private String leaderboardFilePath;

    @Autowired
    public QuizWebController(QuizSessionService quizSessionService, 
                             Leaderboard leaderboard, 
                             ActiveSessionsService activeSessionsService,
                             CompletedQuizService completedQuizService) { // Added dependency
        this.quizSessionService = quizSessionService;
        this.leaderboard = leaderboard;
        this.activeSessionsService = activeSessionsService;
        this.completedQuizService = completedQuizService; // Initialize dependency
    }

    @GetMapping("/")
    public String index(SessionStatus sessionStatus, HttpSession session) {
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
        if (!quizSessionService.isGameActive() && quizSessionService.getUserName().isEmpty()) {
            model.addAttribute("message", "No active game found or game already ended.");
            model.addAttribute("leaderboard", leaderboard.getAllScoresSorted());
            quizSessionService.endGame(); 
            return "gameOver";
        }
        
        String userName = quizSessionService.getUserName();
        int score = quizSessionService.getScore();

        leaderboard.addScore(userName, score);
        completedQuizService.addCompletedQuiz(new CompletedQuiz(userName, score)); // Add to completed quizzes

        model.addAttribute("username", userName);
        model.addAttribute("score", score);
        model.addAttribute("leaderboard", leaderboard.getAllScoresSorted());
        
        quizSessionService.endGame();
        return "gameOver";
    }

    @GetMapping("/dashboard-login")
    public String dashboardLoginPage(HttpSession session) {
        if (Boolean.TRUE.equals(session.getAttribute("dashboardAuthorized"))) {
            return "redirect:/dashboard"; // Already logged in
        }
        return "dashboard-login";
    }

    @PostMapping("/dashboard-login")
    public String processDashboardLogin(@RequestParam("password") String password, HttpSession session, Model model) {
        if (expectedDashboardPassword.equals(password)) {
            session.setAttribute("dashboardAuthorized", true);
            return "redirect:/dashboard";
        } else {
            return "redirect:/dashboard-login?error";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("dashboardAuthorized"))) {
            return "redirect:/dashboard-login";
        }
        model.addAttribute("activeSessions", activeSessionsService.getActiveSessions());
        model.addAttribute("activeSessionCount", activeSessionsService.getActiveSessionCount());
        return "dashboard";
    }

    @GetMapping("/dashboard-logout")
    public String dashboardLogout(HttpSession session) {
        session.removeAttribute("dashboardAuthorized");
        return "redirect:/dashboard-login";
    }

    @GetMapping("/dashboard-results")
    public String dashboardResults(Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("dashboardAuthorized"))) {
            return "redirect:/dashboard-login";
        }
        model.addAttribute("completedQuizzes", completedQuizService.getCompletedQuizzes());
        model.addAttribute("completedQuizCount", completedQuizService.getCompletedQuizCount());
        return "dashboard-results"; // New template
    }

    @GetMapping("/download-leaderboard")
    public void downloadLeaderboardFile(HttpSession session, HttpServletResponse response) throws IOException {
        if (!Boolean.TRUE.equals(session.getAttribute("dashboardAuthorized"))) {
            response.sendRedirect("/dashboard-login"); // Redirect if not authorized
            return;
        }

        Path filePath = Paths.get(leaderboardFilePath);
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Leaderboard file not found or not readable.");
            return;
        }

        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filePath.getFileName().toString() + "\"");

        try (InputStream inputStream = Files.newInputStream(filePath);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // Log error, maybe send a different error response
            System.err.println("Error writing leaderboard file to output stream: " + e.getMessage());
            if (!response.isCommitted()) {
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error occurred while downloading the file.");
            }
        }
    }
}
