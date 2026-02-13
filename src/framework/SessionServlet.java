package framework;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import framework.annotation.*;
import framework.model.*;
import framework.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public abstract class SessionServlet extends HttpServlet {
    
    private Map<String, Object> endpoints = new HashMap<>();
    
    protected void registerEndpoint(Object endpoint) {
        endpoints.put(endpoint.getClass().getSimpleName(), endpoint);
        System.out.println("🔐 Endpoint: " + endpoint.getClass().getSimpleName());
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String uri = req.getRequestURI();
        String ctxPath = req.getContextPath();
        String path = uri.substring(ctxPath.length());
        
        System.out.println("➡️  " + req.getMethod() + " " + path);
        
        // Laisser Tomcat servir les ressources statiques et JSP
        if (path.startsWith("/WEB-INF/") || path.endsWith(".jsp") || path.endsWith(".html")) {
            System.out.println("📄 Fichier statique: " + path);
            req.getRequestDispatcher(path).forward(req, resp);
            return;
        }
        
        // Initialiser le contexte
        SessionContext.set(req, resp);
        
        try {
            // Routes spéciales
            if (path.equals("/") || path.isEmpty()) {
                resp.sendRedirect(ctxPath + "/auth/login");
                return;
            }
            
            // Chercher l'endpoint
            boolean found = false;
            
            for (Object obj : endpoints.values()) {
                String className = obj.getClass().getSimpleName().toLowerCase().replace("endpoint", "");
                
                for (Method m : obj.getClass().getDeclaredMethods()) {
                    String methodPath = "/" + className + "/" + m.getName();
                    
                    if (methodPath.equals(path)) {
                        found = true;
                        
                        // Vérifier sécurité
                        if (!checkSecurity(obj.getClass(), req, resp)) {
                            return;
                        }
                        
                        // Préparer paramètres
                        Object[] args = prepareParameters(m, req, resp);
                        
                        // Exécuter
                        m.invoke(obj, args);
                        return;
                    }
                }
            }
            
            if (!found) {
                resp.sendError(404, "Endpoint non trouvé: " + path);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", e.getMessage());
            try {
                req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
            } catch (Exception ex) {
                resp.sendError(500, e.getMessage());
            }
        } finally {
            SessionContext.clear();
        }
    }
    
    private boolean checkSecurity(Class<?> clazz, HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        
        SessionContext ctx = SessionContext.get();
        
        if (clazz.isAnnotationPresent(LoginRequired.class)) {
            if (!ctx.isLoggedIn()) {
                resp.sendRedirect(req.getContextPath() + "/auth/login");
                return false;
            }
        }
        
        if (clazz.isAnnotationPresent(RoleRequired.class)) {
            RoleRequired ann = clazz.getAnnotation(RoleRequired.class);
            if (!ctx.isLoggedIn() || !checkRoles(ctx, ann.value())) {
                resp.sendRedirect(req.getContextPath() + "/auth/login");
                return false;
            }
        }
        
        return true;
    }
    
    private boolean checkRoles(SessionContext ctx, String[] roles) {
        for (String role : roles) {
            if (ctx.getUser().hasRole(role)) return true;
        }
        return false;
    }
    
    private Object[] prepareParameters(Method m, HttpServletRequest req, HttpServletResponse resp) {
        Class<?>[] paramTypes = m.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];
            
            if (type == HttpServletRequest.class) {
                args[i] = req;
            } else if (type == HttpServletResponse.class) {
                args[i] = resp;
            } else if (type == HttpSession.class) {
                args[i] = req.getSession();
            } else if (type == User.class) {
                args[i] = SessionContext.get().getUser();
            } else {
                args[i] = null;
            }
        }
        
        return args;
    }
}
