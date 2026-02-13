package framework.model;

import java.io.Serializable;
import java.util.*;

public class SessionInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sessionId;
    private User user;
    private Date creationTime;
    private Date lastAccessedTime;
    private int maxInactiveInterval;
    private Map<String, Object> attributes = new HashMap<>();
    
    public SessionInfo() {
        this.creationTime = new Date();
        this.lastAccessedTime = this.creationTime;
    }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Date getCreationTime() { return creationTime; }
    public void setCreationTime(Date creationTime) { this.creationTime = creationTime; }
    
    public Date getLastAccessedTime() { return lastAccessedTime; }
    public void setLastAccessedTime(Date lastAccessedTime) { this.lastAccessedTime = lastAccessedTime; }
    
    public int getMaxInactiveInterval() { return maxInactiveInterval; }
    public void setMaxInactiveInterval(int maxInactiveInterval) { this.maxInactiveInterval = maxInactiveInterval; }
    
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    public void setAttribute(String key, Object value) { attributes.put(key, value); }
    public Object getAttribute(String key) { return attributes.get(key); }
    
    public boolean isValid() {
        if (maxInactiveInterval <= 0) return true;
        long idle = System.currentTimeMillis() - lastAccessedTime.getTime();
        return idle < maxInactiveInterval * 1000L;
    }
}
