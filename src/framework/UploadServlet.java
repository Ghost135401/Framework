package framework;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import framework.annotation.*;
import framework.model.*;
import framework.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public abstract class UploadServlet extends HttpServlet {
    
    protected String uploadDirectory = "uploads";
    protected long maxFileSize = 10485760; // 10MB
    protected List<String> allowedTypes = Arrays.asList("*");
    
    private Map<String, Object> endpoints = new HashMap<>();
    
    protected void registerEndpoint(Object endpoint) {
        Class<?> clazz = endpoint.getClass();
        if (clazz.isAnnotationPresent(UploadEndpoint.class)) {
            UploadEndpoint ann = clazz.getAnnotation(UploadEndpoint.class);
            endpoints.put(ann.path(), endpoint);
            System.out.println("📤 Endpoint upload: " + ann.path());
        }
    }
    
    public void setUploadDirectory(String dir) {
        this.uploadDirectory = dir;
        new File(dir).mkdirs();
    }
    
    public void setMaxFileSize(long maxSize) {
        this.maxFileSize = maxSize;
    }
    
    public void setAllowedTypes(String... types) {
        this.allowedTypes = Arrays.asList(types);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        String path = req.getPathInfo();
        
        if (path == null || path.equals("/") || path.equals("/index.html")) {
            serveFile(req, resp, "/index.html");
            return;
        }
        
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
    
    private void serveFile(HttpServletRequest req, HttpServletResponse resp, String filePath) 
            throws IOException {
        String realPath = getServletContext().getRealPath(filePath);
        File file = new File(realPath);
        
        if (file.exists()) {
            resp.setContentType("text/html");
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = resp.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        
        String path = req.getPathInfo();
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            Map<String, Object> data = UploadParser.parseMultipart(req);
            List<UploadedFile> files = (List<UploadedFile>) data.get("files");
            
            if (files == null || files.isEmpty()) {
                out.print("{\"status\":400,\"message\":\"Aucun fichier uploadé\"}");
                return;
            }
            
            FileValidator validator = new FileValidator();
            for (UploadedFile file : files) {
                if (!validator.validate(file, maxFileSize, 
                        allowedTypes.toArray(new String[0]))) {
                    out.print("{\"status\":400,\"message\":\"" + 
                             validator.getErrors().get(0) + "\"}");
                    return;
                }
            }
            
            for (UploadedFile file : files) {
                file.saveTo(uploadDirectory);
            }
            
            Object result = handleUpload(path, data);
            
            // Construire la réponse JSON manuellement
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"status\":200,");
            json.append("\"message\":\"").append(files.size()).append(" fichier(s) uploadé(s)\",");
            json.append("\"timestamp\":").append(System.currentTimeMillis()).append(",");
            
            json.append("\"files\":[");
            for (int i = 0; i < files.size(); i++) {
                UploadedFile f = files.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"name\":\"").append(escapeJson(f.getFileName())).append("\",");
                json.append("\"size\":").append(f.getSize()).append(",");
                json.append("\"type\":\"").append(escapeJson(f.getContentType())).append("\"");
                json.append("}");
            }
            json.append("]");
            
            if (result instanceof Map) {
                Map<?,?> map = (Map<?,?>) result;
                for (Map.Entry<?,?> entry : map.entrySet()) {
                    json.append(",\"").append(entry.getKey()).append("\":");
                    if (entry.getValue() instanceof String) {
                        json.append("\"").append(escapeJson(entry.getValue().toString())).append("\"");
                    } else {
                        json.append(entry.getValue());
                    }
                }
            }
            
            json.append("}");
            out.print(json.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":500,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    private Object handleUpload(String path, Map<String, Object> data) throws Exception {
        for (Object obj : endpoints.values()) {
            Class<?> clazz = obj.getClass();
            if (!clazz.isAnnotationPresent(UploadEndpoint.class)) continue;
            
            String basePath = clazz.getAnnotation(UploadEndpoint.class).path();
            
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(UploadMethod.class)) {
                    String methodPath = m.getAnnotation(UploadMethod.class).path();
                    String fullPath = basePath + methodPath;
                    
                    if (fullPath.equals(path)) {
                        return m.invoke(obj, data);
                    }
                }
            }
        }
        return null;
    }
}
