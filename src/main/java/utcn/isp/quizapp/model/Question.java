package utcn.isp.quizapp.model;

import java.util.List;

public class Question {
    private String questionText;
    private List<String> options;
    private int correctOptionIndex; // 0-based index

    public Question(String questionText, List<String> options, int correctOptionIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public boolean isCorrect(int selectedOptionIndex) {
        return selectedOptionIndex == correctOptionIndex;
    }
}
