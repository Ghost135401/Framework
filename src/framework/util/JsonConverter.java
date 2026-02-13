package framework.util;
import framework.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public class JsonConverter {
    
    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + obj + "\"";
        if (obj instanceof Number) return obj.toString();
        if (obj instanceof Boolean) return obj.toString();
        if (obj instanceof Collection) return collectionToJson((Collection<?>) obj);
        if (obj instanceof Map) return mapToJson((Map<?,?>) obj);
        return objectToJson(obj);
    }
    
    private static String objectToJson(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        List<String> list = new ArrayList<>();
        
        for (Field f : fields) {
            if (f.isAnnotationPresent(ApiIgnore.class)) continue;
            f.setAccessible(true);
            try {
                Object val = f.get(obj);
                if (val != null) {
                    String name = f.isAnnotationPresent(ApiField.class) ? 
                        f.getAnnotation(ApiField.class).value() : f.getName();
                    list.add("\"" + name + "\":" + toJson(val));
                }
            } catch (Exception e) {}
        }
        sb.append(String.join(",", list));
        sb.append("}");
        return sb.toString();
    }
    
    private static String collectionToJson(Collection<?> c) {
        List<String> list = new ArrayList<>();
        for (Object o : c) list.add(toJson(o));
        return "[" + String.join(",", list) + "]";
    }
    
    private static String mapToJson(Map<?,?> m) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<?,?> e : m.entrySet()) {
            list.add("\"" + e.getKey() + "\":" + toJson(e.getValue()));
        }
        return "{" + String.join(",", list) + "}";
    }
}
