package com.protect.framework;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    
    private static AuthService instance;
    private Map<String, User> users = new ConcurrentHashMap<>();
    private Map<String, String> passwords = new ConcurrentHashMap<>();
    
    private AuthService() {
        // Utilisateur admin
        User admin = new User("admin");
        admin.addRole("ADMIN");
        admin.addRole("USER");
        users.put("admin", admin);
        passwords.put("admin", "admin123");
        
        // Utilisateur normal
        User user = new User("user");
        user.addRole("USER");
        users.put("user", user);
        passwords.put("user", "user123");
    }
    
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    public User authenticate(String username, String password) {
        if (users.containsKey(username) && passwords.get(username).equals(password)) {
            return users.get(username);
        }
        return null;
    }
    
    public User getUser(String username) {
        return users.get(username);
    }
}
