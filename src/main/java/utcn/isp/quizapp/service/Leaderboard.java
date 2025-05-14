package utcn.isp.quizapp.service;

import utcn.isp.quizapp.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct; // For Spring Boot 3+
// import javax.annotation.PostConstruct; // For Spring Boot 2.x or Java EE
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class Leaderboard {
    // Make scores thread-safe for web environment
    private Map<String, Integer> scores = new ConcurrentHashMap<>();
    
    private final String leaderboardResourceName; // e.g., "leaderboard.txt" for classpath loading
    private final String leaderboardSavePath;   // e.g., "/path/to/external/leaderboard.txt" or "leaderboard-data.txt" for relative path

    // Inject paths from application.properties
    public Leaderboard(@Value("${quiz.leaderboard.resource-name:leaderboard.txt}") String resourceName,
                       @Value("${quiz.leaderboard.save-path:./leaderboard_scores.txt}") String savePath) {
        this.leaderboardResourceName = resourceName;
        this.leaderboardSavePath = savePath;
    }

    @PostConstruct
    private void initializeLeaderboard() {
        loadLeaderboardFromClasspath();
        // Optionally, try to load from savePath if it exists and is more recent,
        // or merge, but for simplicity, classpath is the initial seed.
        // If savePath exists, it might be loaded to override/augment classpath data.
        // For now, we just load from classpath as initial state.
        // If leaderboardSavePath file exists, we could load it here to get persisted scores.
        loadLeaderboardFromFile(leaderboardSavePath);

    }
    
    private void loadLeaderboardFromClasspath() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(leaderboardResourceName);
        if (inputStream == null) {
            System.err.println("Warning: Leaderboard resource not found in classpath: " + leaderboardResourceName);
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            parseLeaderboardData(reader);
        } catch (IOException e) {
            System.err.println("Error loading leaderboard from classpath resource: " + e.getMessage());
        }
    }

    private void loadLeaderboardFromFile(String filePath) {
        if (!Files.exists(Paths.get(filePath))) {
            System.out.println("Leaderboard save file not found, will be created: " + filePath);
            return; 
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            parseLeaderboardData(reader);
        } catch (IOException e) {
            System.err.println("Error loading leaderboard from file: " + filePath + " - " + e.getMessage());
        }
    }

    private void parseLeaderboardData(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                try {
                    // Merge scores, keeping the highest if duplicates exist from different sources
                    scores.merge(parts[0], Integer.parseInt(parts[1]), Integer::max);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping malformed line in leaderboard: " + line);
                }
            }
        }
    }


    private synchronized void saveLeaderboard() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(leaderboardSavePath, StandardCharsets.UTF_8))) {
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving leaderboard to " + leaderboardSavePath + ": " + e.getMessage());
        }
    }

    public synchronized void addScore(String userName, int score) {
        scores.merge(userName, score, Integer::max);
        saveLeaderboard();
    }

    public List<User> getTopScores(int count) {
        return scores.entrySet().stream()
                .map(entry -> new User(entry.getKey(), entry.getValue()))
                .sorted() // Uses User.compareTo for descending score
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<User> getAllScoresSorted() {
        return scores.entrySet().stream()
                .map(entry -> new User(entry.getKey(), entry.getValue()))
                .sorted()
                .collect(Collectors.toList());
    }
}
