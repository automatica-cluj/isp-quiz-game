package utcn.isp.quizapp.service;

import org.springframework.stereotype.Service;
import utcn.isp.quizapp.model.CompletedQuiz;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class CompletedQuizService {

    private final List<CompletedQuiz> completedQuizzes = new CopyOnWriteArrayList<>();
    private final String leaderboardFilePath;

    // Inject the path from application.properties
    public CompletedQuizService(@Value("${quiz.leaderboard.save-path:leaderboard_data.txt}") String leaderboardFilePath) {
        this.leaderboardFilePath = leaderboardFilePath;
    }

    @PostConstruct
    public void loadInitialLeaderboardData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(leaderboardFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String userName = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    completedQuizzes.add(new CompletedQuiz(userName, score));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading initial leaderboard data into CompletedQuizService: " + e.getMessage());
        }
    }

    public void addCompletedQuiz(CompletedQuiz quiz) {
        this.completedQuizzes.add(0, quiz); // Add to the beginning to show newest first
    }

    public List<CompletedQuiz> getCompletedQuizzes() {
        return Collections.unmodifiableList(new ArrayList<>(completedQuizzes)); // Return a copy for safety
    }

    public int getCompletedQuizCount(){
        return completedQuizzes.size();
    }
}
