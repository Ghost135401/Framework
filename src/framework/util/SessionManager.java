package framework.util;

import framework.model.User;
import framework.model.SessionInfo;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    
    private static SessionManager instance;
    private Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private Map<String, User> users = new ConcurrentHashMap<>();
    private int defaultTimeout = 1800; // 30 minutes
    
    private SessionManager() {}
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void setDefaultTimeout(int seconds) {
        this.defaultTimeout = seconds;
    }
    
    public SessionInfo createSession(HttpSession session) {
        SessionInfo info = new SessionInfo();
        info.setSessionId(session.getId());
        info.setCreationTime(new Date());
        info.setLastAccessedTime(new Date());
        info.setMaxInactiveInterval(defaultTimeout);
        
        sessions.put(session.getId(), info);
        return info;
    }
    
    public SessionInfo getSession(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        if (info != null) {
            info.setLastAccessedTime(new Date());
        }
        return info;
    }
    
    public void removeSession(String sessionId) {
        SessionInfo info = sessions.remove(sessionId);
        if (info != null && info.getUser() != null) {
            users.remove(info.getUser().getId());
        }
    }
    
    public User login(String username, String password) {
        // À surcharger dans l'application
        return null;
    }
    
    public void setUser(String sessionId, User user) {
        SessionInfo info = sessions.get(sessionId);
        if (info != null) {
            info.setUser(user);
            users.put(user.getId(), user);
        }
    }
    
    public User getUser(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        return info != null ? info.getUser() : null;
    }
    
    public User getUserById(String userId) {
        return users.get(userId);
    }
    
    public void logout(String sessionId) {
        SessionInfo info = sessions.remove(sessionId);
        if (info != null && info.getUser() != null) {
            users.remove(info.getUser().getId());
        }
    }
    
    public List<SessionInfo> getActiveSessions() {
        List<SessionInfo> active = new ArrayList<>();
        for (SessionInfo info : sessions.values()) {
            if (info.isValid()) {
                active.add(info);
            }
        }
        return active;
    }
    
    public int getActiveCount() {
        return getActiveSessions().size();
    }
    
    public void cleanup() {
        sessions.entrySet().removeIf(entry -> !entry.getValue().isValid());
    }
    
    public boolean isLoggedIn(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        return info != null && info.getUser() != null && info.isValid();
    }
    
    public boolean hasRole(String sessionId, String role) {
        SessionInfo info = sessions.get(sessionId);
        return info != null && info.getUser() != null && 
               info.getUser().hasRole(role);
    }
}
