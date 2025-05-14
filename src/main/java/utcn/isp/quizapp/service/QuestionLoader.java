package utcn.isp.quizapp.service;

import utcn.isp.quizapp.model.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QuestionLoader {
    private static final int LINES_PER_QUESTION_BLOCK = 6; // Question, 4 options, 1 correct answer

    public static List<Question> loadQuestionsFromClasspath(String resourceName) {
        List<Question> questions = new ArrayList<>();
        // Try loading as a resource from the classpath
        InputStream inputStream = QuestionLoader.class.getClassLoader().getResourceAsStream(resourceName);

        if (inputStream == null) {
            System.err.println("Error: Cannot find question file in classpath: " + resourceName);
            return questions; // Return empty list
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            List<String> currentQuestionBlock = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) { // Separator between questions or end of block
                    if (currentQuestionBlock.size() == LINES_PER_QUESTION_BLOCK) {
                        addQuestionFromBlock(questions, currentQuestionBlock);
                    }
                    currentQuestionBlock.clear();
                } else {
                    currentQuestionBlock.add(line);
                }
            }
            // Process the last block if file doesn't end with a blank line
            if (currentQuestionBlock.size() == LINES_PER_QUESTION_BLOCK) {
                addQuestionFromBlock(questions, currentQuestionBlock);
            }
        } catch (IOException e) {
            System.err.println("Error loading questions from resource: " + resourceName + " - " + e.getMessage());
            // Return empty list or throw custom exception
        }
        return questions;
    }

    private static void addQuestionFromBlock(List<Question> questions, List<String> block) {
        String questionText = block.get(0);
        List<String> options = new ArrayList<>(block.subList(1, 5));
        String correctAnswerLetter = block.get(5).toUpperCase();
        int correctOptionIndex = -1;
        switch (correctAnswerLetter) {
            case "A": correctOptionIndex = 0; break;
            case "B": correctOptionIndex = 1; break;
            case "C": correctOptionIndex = 2; break;
            case "D": correctOptionIndex = 3; break;
            default:
                System.err.println("Warning: Invalid correct answer letter '" + correctAnswerLetter + "' for question: " + questionText);
                return; // Skip this question
        }
        questions.add(new Question(questionText, options, correctOptionIndex));
    }
}
