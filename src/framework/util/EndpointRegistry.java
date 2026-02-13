package framework.util;

import framework.annotation.ApiEndpoint;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EndpointRegistry {
    private static EndpointRegistry instance;
    private Map<String, Object> endpoints = new ConcurrentHashMap<>();
    
    private EndpointRegistry() {}
    
    public static synchronized EndpointRegistry getInstance() {
        if (instance == null) instance = new EndpointRegistry();
        return instance;
    }
    
    public void register(Object obj) {
        Class<?> clazz = obj.getClass();
        if (clazz.isAnnotationPresent(ApiEndpoint.class)) {
            ApiEndpoint ann = clazz.getAnnotation(ApiEndpoint.class);
            String base = ann.path();
            for (Method m : clazz.getDeclaredMethods()) {
                String path = base + "/" + m.getName();
                endpoints.put(path, obj);
                System.out.println("→ " + ann.method() + " " + path);
            }
        }
    }
    
    public Object getEndpoint(String path) {
        return endpoints.get(path);
    }
}
