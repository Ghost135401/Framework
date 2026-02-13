package framework.util;

import framework.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SessionContext {
    
    private static ThreadLocal<SessionContext> context = new ThreadLocal<>();
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private User user;
    
    private SessionContext(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.session = request.getSession();
        
        SessionManager manager = SessionManager.getInstance();
        if (manager.getSession(session.getId()) == null) {
            manager.createSession(session);
        }
        
        this.user = manager.getUser(session.getId());
    }
    
    public static void set(HttpServletRequest request, HttpServletResponse response) {
        context.set(new SessionContext(request, response));
    }
    
    public static SessionContext get() {
        return context.get();
    }
    
    public static void clear() {
        context.remove();
    }
    
    public HttpServletRequest getRequest() { return request; }
    public HttpServletResponse getResponse() { return response; }
    public HttpSession getSession() { return session; }
    
    public User getUser() { return user; }
    public void setUser(User user) { 
        this.user = user;
        SessionManager.getInstance().setUser(session.getId(), user);
    }
    
    public boolean isLoggedIn() { return user != null; }
    
    public void setAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }
    
    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }
    
    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }
    
    public void invalidate() {
        SessionManager.getInstance().logout(session.getId());
        session.invalidate();
        user = null;
    }
}
