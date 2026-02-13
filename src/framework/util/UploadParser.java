package framework.util;

import framework.model.UploadedFile;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

public class UploadParser {
    
    private static final String LINE_FEED = "\r\n";
    private static final String BOUNDARY_PREFIX = "--";
    
    public static Map<String, Object> parseMultipart(HttpServletRequest request) 
            throws IOException {
        
        Map<String, Object> result = new HashMap<>();
        String contentType = request.getContentType();
        
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return result;
        }
        
        String boundary = extractBoundary(contentType);
        if (boundary == null) return result;
        
        try (ServletInputStream inputStream = request.getInputStream()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            byte[] fullData = baos.toByteArray();
            parseMultipartData(fullData, boundary, result);
        }
        
        return result;
    }
    
    private static String extractBoundary(String contentType) {
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                return part.substring(9);
            }
        }
        return null;
    }
    
    private static void parseMultipartData(byte[] data, String boundary, 
                                          Map<String, Object> result) {
        byte[] boundaryBytes = (BOUNDARY_PREFIX + boundary).getBytes();
        byte[] endBoundaryBytes = (BOUNDARY_PREFIX + boundary + "--").getBytes();
        
        int pos = 0;
        List<byte[]> parts = new ArrayList<>();
        
        while (pos < data.length) {
            int start = findBytes(data, boundaryBytes, pos);
            if (start == -1) break;
            
            int end = findBytes(data, boundaryBytes, start + boundaryBytes.length);
            if (end == -1) {
                end = findBytes(data, endBoundaryBytes, start + boundaryBytes.length);
            }
            
            if (end != -1) {
                int partStart = start + boundaryBytes.length + 2;
                int partEnd = end - 2;
                
                if (partEnd > partStart) {
                    byte[] part = Arrays.copyOfRange(data, partStart, partEnd);
                    parts.add(part);
                }
                pos = end;
            } else {
                break;
            }
        }
        
        for (byte[] part : parts) {
            parsePart(part, result);
        }
    }
    
    private static int findBytes(byte[] data, byte[] pattern, int start) {
        outer: for (int i = start; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
    
    private static void parsePart(byte[] part, Map<String, Object> result) {
        String header = "";
        byte[] content = part;
        
        for (int i = 0; i < part.length - 3; i++) {
            if (part[i] == '\r' && part[i+1] == '\n' && part[i+2] == '\r' && part[i+3] == '\n') {
                header = new String(part, 0, i);
                content = Arrays.copyOfRange(part, i + 4, part.length);
                break;
            }
        }
        
        String name = extractFieldName(header);
        String filename = extractFileName(header);
        
        if (filename != null) {
            // C'est un fichier
            String contentType = extractContentType(header);
            UploadedFile file = new UploadedFile(
                name, filename, contentType, content.length, content
            );
            
            List<UploadedFile> files = (List<UploadedFile>) result.get("files");
            if (files == null) {
                files = new ArrayList<>();
                result.put("files", files);
            }
            files.add(file);
        } else {
            // C'est un champ texte
            result.put(name, new String(content).trim());
        }
    }
    
    private static String extractFieldName(String header) {
        String[] lines = header.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("Content-Disposition:")) {
                int idx = line.indexOf("name=\"");
                if (idx != -1) {
                    int end = line.indexOf("\"", idx + 6);
                    if (end != -1) {
                        return line.substring(idx + 6, end);
                    }
                }
            }
        }
        return null;
    }
    
    private static String extractFileName(String header) {
        String[] lines = header.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("Content-Disposition:")) {
                int idx = line.indexOf("filename=\"");
                if (idx != -1) {
                    int end = line.indexOf("\"", idx + 10);
                    if (end != -1) {
                        return line.substring(idx + 10, end);
                    }
                }
            }
        }
        return null;
    }
    
    private static String extractContentType(String header) {
        String[] lines = header.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("Content-Type:")) {
                return line.substring(13).trim();
            }
        }
        return "application/octet-stream";
    }
}
