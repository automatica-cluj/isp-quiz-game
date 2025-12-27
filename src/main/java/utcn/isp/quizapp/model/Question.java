package utcn.isp.quizapp.model;

import java.util.ArrayList;
import java.util.Collections;
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

    public Question shuffledCopy() {
        if (options == null || options.isEmpty()) {
            List<String> safeOptions = (options == null) ? Collections.emptyList() : new ArrayList<>(options);
            return new Question(questionText, safeOptions, correctOptionIndex);
        }
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes);
        List<String> shuffledOptions = new ArrayList<>(options.size());
        int newCorrectIndex = 0;
        for (int pos = 0; pos < indexes.size(); pos++) {
            int originalIndex = indexes.get(pos);
            shuffledOptions.add(options.get(originalIndex));
            if (originalIndex == correctOptionIndex) {
                newCorrectIndex = pos;
            }
        }
        return new Question(questionText, shuffledOptions, newCorrectIndex);
    }
}
