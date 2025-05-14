package utcn.isp.quizapp.service;

import org.springframework.stereotype.Service;
import utcn.isp.quizapp.model.QuizGame;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveSessionsService {

    private final Map<String, QuizGame> activeSessions = new ConcurrentHashMap<>();

    public void addSession(String sessionId, QuizGame game) {
        activeSessions.put(sessionId, game);
    }

    public void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
    }

    public Collection<QuizGame> getActiveSessions() {
        return activeSessions.values();
    }

    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}
