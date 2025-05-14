package utcn.isp.quizapp.service;

import utcn.isp.quizapp.model.Question;
import utcn.isp.quizapp.model.QuizGame;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import jakarta.servlet.http.HttpSession; // Added import

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 

@Service
@SessionScope // Crucial: one instance per user session
public class QuizSessionService implements Serializable { // Serializable for session replication if needed

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(QuizSessionService.class); // SLF4J Logger

    private final ActiveSessionsService activeSessionsService; // Added dependency
    private final HttpSession httpSession; // Added dependency

    private QuizGame currentGame;
    private List<Question> allQuestions;
    private final String questionsResourceName;
    private static final long QUIZ_DURATION_MS = 60 * 1000; // 1 minute

    public QuizSessionService(@Value("${quiz.questions.file-name:questions.txt}") String questionsResourceName,
                              ActiveSessionsService activeSessionsService, // Added dependency
                              HttpSession httpSession) { // Added dependency
        this.questionsResourceName = questionsResourceName;
        this.activeSessionsService = activeSessionsService; // Initialize dependency
        this.httpSession = httpSession; // Initialize dependency
        logger.info("Attempting to load questions from resource: {}", this.questionsResourceName); // Log the filename
        loadAllQuestions();
    }

    private void loadAllQuestions() {
        this.allQuestions = QuestionLoader.loadQuestionsFromClasspath(questionsResourceName);
        if (this.allQuestions == null || this.allQuestions.isEmpty()) {
            logger.error("FATAL: No questions loaded or questions file is empty/malformed. Check '{}' in classpath.", questionsResourceName); // Use logger
            this.allQuestions = Collections.emptyList();
        } else {
            logger.info("Successfully loaded {} questions from {}.", allQuestions.size(), questionsResourceName);
        }
    }

    public void startNewGame(String userName) {
        if (allQuestions.isEmpty()) {
            logger.warn("Starting game for user '{}' with no questions loaded.", userName);
            this.currentGame = new QuizGame(Collections.emptyList(), userName);
            activeSessionsService.addSession(httpSession.getId(), this.currentGame); // Add session
            return;
        }
        List<Question> currentQuizQuestions = new java.util.ArrayList<>(allQuestions);
        Collections.shuffle(currentQuizQuestions);
        this.currentGame = new QuizGame(currentQuizQuestions, userName);
        activeSessionsService.addSession(httpSession.getId(), this.currentGame); // Add session
    }

    public QuizGame getCurrentGame() {
        return currentGame;
    }

    public Question getCurrentQuestion() {
        return (currentGame != null) ? currentGame.getCurrentQuestion() : null;
    }

    public boolean isGameActive() {
        return currentGame != null;
    }

    public boolean hasNextQuestion() {
        return currentGame != null && currentGame.hasNextQuestion();
    }
    
    public boolean isTimeUp() {
        return currentGame != null && currentGame.isTimeUp(QUIZ_DURATION_MS);
    }

    public boolean submitAnswer(int selectedOptionIndex) {
        if (currentGame == null || isTimeUp()) {
            return false;
        }
        boolean correct = currentGame.answerQuestion(selectedOptionIndex);
        if (correct) {
            currentGame.moveToNextQuestion();
        }
        return correct;
    }

    public void endGame() {
        // The game naturally ends when time is up or a wrong answer is given,
        // or all questions are answered. This method can be used for explicit cleanup if needed.
        // For now, the controller will handle leaderboard updates.
        // currentGame = null; // Or keep it for displaying final score until a new game starts
        activeSessionsService.removeSession(httpSession.getId()); // Remove session
    }

    public int getScore() {
        return (currentGame != null) ? currentGame.getCurrentScore() : 0;
    }

    public String getUserName() {
        return (currentGame != null) ? currentGame.getUserName() : "";
    }
}
