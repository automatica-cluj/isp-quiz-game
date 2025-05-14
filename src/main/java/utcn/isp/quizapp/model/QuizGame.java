package utcn.isp.quizapp.model;

import java.util.List;

public class QuizGame {
    private List<Question> questions;
    private User currentUser; // For current game session tracking, not leaderboard user
    private int currentQuestionIndex;
    private int currentScore;
    private long startTime; // Time when the quiz started

    public QuizGame(List<Question> questions, String userName) {
        this.questions = questions;
        this.currentUser = new User(userName, 0); // Score here is for current game
        this.currentQuestionIndex = 0;
        this.currentScore = 0;
        this.startTime = System.currentTimeMillis(); // Record start time
    }

    public Question getCurrentQuestion() {
        if (hasNextQuestion()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }

    public boolean hasNextQuestion() {
        return currentQuestionIndex < questions.size();
    }

    public boolean answerQuestion(int selectedOptionIndex) {
        Question question = getCurrentQuestion();
        if (question != null) {
            boolean correct = question.isCorrect(selectedOptionIndex);
            if (correct) {
                currentScore++;
            }
            return correct;
        }
        return false;
    }

    public void moveToNextQuestion() {
        if (hasNextQuestion()) {
            currentQuestionIndex++;
        }
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public String getUserName() {
        return currentUser.getName();
    }

    public boolean isTimeUp(long maxDurationMillis) {
        return (System.currentTimeMillis() - startTime) >= maxDurationMillis;
    }

    public long getStartTime() { // Add this method
        return startTime;
    }
}
