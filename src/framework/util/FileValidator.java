package framework.util;

import framework.model.UploadedFile;
import java.util.*;

public class FileValidator {
    
    private List<String> errors = new ArrayList<>();
    
    public boolean validate(UploadedFile file, long maxSize, String[] allowedTypes) {
        errors.clear();
        
        // Vérifier la taille
        if (maxSize > 0 && file.getSize() > maxSize) {
            errors.add("Fichier trop volumineux: " + file.getSize() + 
                       " (max: " + maxSize + ")");
        }
        
        // Vérifier le type
        if (allowedTypes != null && allowedTypes.length > 0) {
            boolean typeOk = false;
            for (String type : allowedTypes) {
                if (type.equals("*") || type.equals(file.getContentType())) {
                    typeOk = true;
                    break;
                }
                if (type.startsWith("image/") && file.getContentType().startsWith("image/")) {
                    typeOk = true;
                    break;
                }
            }
            if (!typeOk) {
                errors.add("Type de fichier non autorisé: " + file.getContentType());
            }
        }
        
        return errors.isEmpty();
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public static boolean isImage(UploadedFile file) {
        return file.getContentType() != null && 
               file.getContentType().startsWith("image/");
    }
    
    public static boolean isDocument(UploadedFile file) {
        String type = file.getContentType();
        return type != null && (
            type.equals("application/pdf") ||
            type.equals("application/msword") ||
            type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }
}
