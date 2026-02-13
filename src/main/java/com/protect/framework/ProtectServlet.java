package com.protect.framework;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.reflect.*;

public abstract class ProtectServlet extends HttpServlet {
    
    protected AuthService authService = AuthService.getInstance();
    protected SessionManager sessionManager = SessionManager.getInstance();
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String path = req.getPathInfo();
        String method = req.getMethod();
        
        System.out.println("➡️ " + method + " " + path);
        
        try {
            // Routes spéciales
            if (path == null || path.equals("/")) {
                handleHome(req, resp);
                return;
            }
            
            // Gérer les endpoints
            if (path.startsWith("/auth/")) {
                handleAuth(path.substring(6), req, resp);
            } else if (path.startsWith("/api/")) {
                handleApi(path.substring(5), req, resp);
            } else {
                resp.sendError(404);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }
    
    private void handleAuth(String action, HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        
        if ("login".equals(action)) {
            showLoginPage(req, resp);
        } else if ("dologin".equals(action)) {
            doLogin(req, resp);
        } else if ("logout".equals(action)) {
            doLogout(req, resp);
        } else {
            resp.sendError(404);
        }
    }
    
    private void showLoginPage(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        
        if (sessionManager.isLoggedIn(req.getSession())) {
            resp.sendRedirect(req.getContextPath() + "/api/home");
            return;
        }
        
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Login</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; max-width: 400px; margin: 100px auto; }");
        out.println("input { width: 100%; padding: 10px; margin: 10px 0; }");
        out.println("button { width: 100%; padding: 10px; background: #4CAF50; color: white; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Connexion</h2>");
        out.println("<form method='post' action='" + req.getContextPath() + "/auth/dologin'>");
        out.println("Username: <input type='text' name='username' required><br>");
        out.println("Password: <input type='password' name='password' required><br>");
        out.println("<button type='submit'>Se connecter</button>");
        out.println("</form>");
        out.println("<p>Comptes: admin/admin123, user/user123</p>");
        out.println("</body>");
        out.println("</html>");
    }
    
    private void doLogin(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        User user = authService.authenticate(username, password);
        
        if (user != null) {
            sessionManager.login(req.getSession(), user);
            resp.sendRedirect(req.getContextPath() + "/api/home");
        } else {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<html><body>");
            out.println("<h3 style='color:red'>Erreur de connexion</h3>");
            out.println("<a href='" + req.getContextPath() + "/auth/login'>Retour</a>");
            out.println("</body></html>");
        }
    }
    
    private void doLogout(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        sessionManager.logout(req.getSession());
        resp.sendRedirect(req.getContextPath() + "/auth/login");
    }
    
    private void handleHome(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        resp.sendRedirect(req.getContextPath() + "/auth/login");
    }
    
    protected abstract void handleApi(String path, HttpServletRequest req, HttpServletResponse resp) 
            throws IOException;
    
    protected boolean checkSecurity(HttpServletRequest req, HttpServletResponse resp, String... requiredRoles) 
            throws IOException {
        
        HttpSession session = req.getSession();
        
        if (!sessionManager.isLoggedIn(session)) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return false;
        }
        
        if (requiredRoles != null && requiredRoles.length > 0) {
            if (!sessionManager.hasAnyRole(session, requiredRoles)) {
                resp.sendError(403, "Accès interdit");
                return false;
            }
        }
        
        return true;
    }
    
    protected void sendJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print(data);
    }
}
