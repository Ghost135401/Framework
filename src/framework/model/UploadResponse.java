package framework.model;

import java.util.*;

public class UploadResponse {
    private int status;
    private String message;
    private List<UploadedFile> files;
    private Map<String, Object> metadata;
    private long timestamp;
    
    public UploadResponse() {
        this.timestamp = System.currentTimeMillis();
        this.files = new ArrayList<>();
        this.metadata = new HashMap<>();
    }
    
    public UploadResponse(int status, String message) {
        this();
        this.status = status;
        this.message = message;
    }
    
    public static UploadResponse success(String message) {
        return new UploadResponse(200, message);
    }
    
    public static UploadResponse success(String message, List<UploadedFile> files) {
        UploadResponse resp = new UploadResponse(200, message);
        resp.setFiles(files);
        return resp;
    }
    
    public static UploadResponse error(String message) {
        return new UploadResponse(500, message);
    }
    
    public static UploadResponse badRequest(String message) {
        return new UploadResponse(400, message);
    }
    
    // Getters et Setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public List<UploadedFile> getFiles() { return files; }
    public void setFiles(List<UploadedFile> files) { this.files = files; }
    public void addFile(UploadedFile file) { this.files.add(file); }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public void addMetadata(String key, Object value) { this.metadata.put(key, value); }
    
    public long getTimestamp() { return timestamp; }
}
