package com.protect.framework;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    
    private static SessionManager instance;
    private Map<String, User> sessions = new ConcurrentHashMap<>();
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void login(HttpSession session, User user) {
        session.setAttribute("user", user);
        sessions.put(session.getId(), user);
    }
    
    public void logout(HttpSession session) {
        session.removeAttribute("user");
        sessions.remove(session.getId());
        session.invalidate();
    }
    
    public User getUser(HttpSession session) {
        if (session == null) return null;
        User user = (User) session.getAttribute("user");
        return user;
    }
    
    public boolean isLoggedIn(HttpSession session) {
        return getUser(session) != null;
    }
    
    public boolean hasRole(HttpSession session, String role) {
        User user = getUser(session);
        return user != null && user.hasRole(role);
    }
    
    public boolean hasAnyRole(HttpSession session, String[] roles) {
        User user = getUser(session);
        if (user == null) return false;
        for (String role : roles) {
            if (user.hasRole(role)) return true;
        }
        return false;
    }
}
