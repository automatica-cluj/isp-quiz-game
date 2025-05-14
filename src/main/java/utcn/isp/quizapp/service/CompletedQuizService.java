package utcn.isp.quizapp.service;

import org.springframework.stereotype.Service;
import utcn.isp.quizapp.model.CompletedQuiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class CompletedQuizService {

    // Use CopyOnWriteArrayList for thread-safe iteration and modification
    private final List<CompletedQuiz> completedQuizzes = new CopyOnWriteArrayList<>();

    public void addCompletedQuiz(CompletedQuiz quiz) {
        this.completedQuizzes.add(0, quiz); // Add to the beginning to show newest first
        // Optional: Limit the size of the list to prevent memory issues
        // if (this.completedQuizzes.size() > 100) { // Keep last 100, for example
        //     this.completedQuizzes.remove(this.completedQuizzes.size() - 1);
        // }
    }

    public List<CompletedQuiz> getCompletedQuizzes() {
        return Collections.unmodifiableList(new ArrayList<>(completedQuizzes)); // Return a copy for safety
    }

    public int getCompletedQuizCount(){
        return completedQuizzes.size();
    }
}
