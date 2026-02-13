package framework;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import framework.annotation.*;
import framework.model.*;
import framework.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public abstract class JsonFrameworkServlet extends HttpServlet {
    
    protected List<Object> endpoints = new ArrayList<>();
    
    protected void registerEndpoint(Object endpoint) {
        endpoints.add(endpoint);
        EndpointRegistry.getInstance().register(endpoint);
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.isEmpty()) path = "/";
        
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            if (path.equals("/health")) {
                Map<String,Object> h = new HashMap<>();
                h.put("status", "UP");
                h.put("time", System.currentTimeMillis());
                out.print(JsonConverter.toJson(ApiResponse.success(h)));
                return;
            }
            
            Object result = handleRequest(path);
            if (result instanceof ApiResponse) {
                out.print(JsonConverter.toJson(result));
            } else {
                out.print(JsonConverter.toJson(ApiResponse.success(result)));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            out.print(JsonConverter.toJson(ApiResponse.error(e.getMessage())));
        }
    }
    
    private Object handleRequest(String path) throws Exception {
        for (Object obj : endpoints) {
            Class<?> clazz = obj.getClass();
            if (clazz.isAnnotationPresent(ApiEndpoint.class)) {
                String base = clazz.getAnnotation(ApiEndpoint.class).path();
                for (Method m : clazz.getDeclaredMethods()) {
                    String fullPath = base + "/" + m.getName();
                    if (fullPath.equals(path)) {
                        return m.invoke(obj);
                    }
                }
            }
        }
        throw new Exception("Endpoint non trouvé: " + path);
    }
}
