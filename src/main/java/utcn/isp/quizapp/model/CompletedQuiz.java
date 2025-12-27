package utcn.isp.quizapp.model;

import java.time.LocalDateTime;

public class CompletedQuiz {
    private final String userName;
    private final int score;
    private final int totalQuestions;
    private final int answeredQuestions;
    private final long durationSeconds;
    private final LocalDateTime completionTime;

    public CompletedQuiz(String userName, int score) {
        this(userName, score, 0, 0, 0);
    }

    public CompletedQuiz(String userName, int score, int totalQuestions, int answeredQuestions, long durationSeconds) {
        this.userName = userName;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.durationSeconds = durationSeconds;
        this.completionTime = LocalDateTime.now();
    }

    public String getUserName() {
        return userName;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getAnsweredQuestions() {
        return answeredQuestions;
    }

    public int getIncorrectAnswers() {
        return Math.max(0, answeredQuestions - score);
    }

    public double getAccuracyPercentage() {
        if (totalQuestions <= 0) {
            return 0;
        }
        return (score * 100.0) / totalQuestions;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public LocalDateTime getCompletionTime() {
        return completionTime;
    }
}
