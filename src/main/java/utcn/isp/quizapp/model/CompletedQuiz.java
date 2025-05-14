package utcn.isp.quizapp.model;

import java.time.LocalDateTime;

public class CompletedQuiz {
    private final String userName;
    private final int score;
    private final LocalDateTime completionTime;

    public CompletedQuiz(String userName, int score) {
        this.userName = userName;
        this.score = score;
        this.completionTime = LocalDateTime.now();
    }

    public String getUserName() {
        return userName;
    }

    public int getScore() {
        return score;
    }

    public LocalDateTime getCompletionTime() {
        return completionTime;
    }
}
