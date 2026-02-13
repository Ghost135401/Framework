package com.protect.framework;

import java.io.Serializable;
import java.util.*;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private List<String> roles = new ArrayList<>();
    private Map<String, Object> attributes = new HashMap<>();
    
    public User() {}
    
    public User(String username) {
        this.username = username;
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public void addRole(String role) { roles.add(role); }
    public boolean hasRole(String role) { return roles.contains(role); }
    
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttribute(String key, Object value) { attributes.put(key, value); }
    public Object getAttribute(String key) { return attributes.get(key); }
}
