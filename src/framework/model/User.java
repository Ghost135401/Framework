package framework.model;

import java.io.Serializable;
import java.util.*;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String username;
    private String email;
    private List<String> roles = new ArrayList<>();
    private Map<String, Object> attributes = new HashMap<>();
    private long loginTime;
    private long lastAccess;
    
    public User() {
        this.id = UUID.randomUUID().toString();
        this.loginTime = System.currentTimeMillis();
        this.lastAccess = this.loginTime;
    }
    
    public User(String username) {
        this();
        this.username = username;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public void addRole(String role) { this.roles.add(role); }
    public boolean hasRole(String role) { return roles.contains(role); }
    
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    public void setAttribute(String key, Object value) { attributes.put(key, value); }
    public Object getAttribute(String key) { return attributes.get(key); }
    
    public long getLoginTime() { return loginTime; }
    public void setLoginTime(long loginTime) { this.loginTime = loginTime; }
    
    public long getLastAccess() { return lastAccess; }
    public void setLastAccess(long lastAccess) { this.lastAccess = lastAccess; }
    
    public void updateLastAccess() { this.lastAccess = System.currentTimeMillis(); }
    
    public boolean isLoggedIn() { return username != null; }
    
    public long getSessionAge() {
        return System.currentTimeMillis() - loginTime;
    }
    
    public long getIdleTime() {
        return System.currentTimeMillis() - lastAccess;
    }
}
