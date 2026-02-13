package framework.model;

import java.io.*;

public class UploadedFile {
    private String fieldName;
    private String fileName;
    private String contentType;
    private long size;
    private byte[] content;
    private File tempFile;
    
    public UploadedFile() {}
    
    public UploadedFile(String fieldName, String fileName, String contentType, 
                        long size, byte[] content) {
        this.fieldName = fieldName;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.content = content;
    }
    
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    
    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }
    
    public File getTempFile() { return tempFile; }
    public void setTempFile(File tempFile) { this.tempFile = tempFile; }
    
    public String getExtension() {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
    
    public boolean saveTo(String directory) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        
        File dest = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(content);
        }
        return true;
    }
    
    public boolean saveTo(String directory, String newName) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        
        File dest = new File(dir, newName);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(content);
        }
        return true;
    }
}
