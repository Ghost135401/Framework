package framework;

import java.lang.reflect.Field;
import java.util.Map;

public class ObjectMapper {

    public static <T> T mapToObject(Map<String, Object> parameters, Class<T> clazz) 
            throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();
        
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            
            String paramName = getParameterName(field);
            
            if (parameters.containsKey(paramName)) {
                Object value = parameters.get(paramName);
                Object convertedValue = convertValue(value, field.getType());
                field.set(instance, convertedValue);
            } else {
                if (field.isAnnotationPresent(EntityField.class)) {
                    EntityField annotation = field.getAnnotation(EntityField.class);
                    if (annotation.required()) {
                        throw new IllegalArgumentException(
                            "Champ requis manquant: " + paramName
                        );
                    }
                }
            }
        }
        
        return instance;
    }
    
    private static String getParameterName(Field field) {
        if (field.isAnnotationPresent(EntityField.class)) {
            EntityField annotation = field.getAnnotation(EntityField.class);
            if (!annotation.value().isEmpty()) {
                return annotation.value();
            }
        }
        return field.getName();
    }
    
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        
        String stringValue = value.toString();
        
        if (targetType == String.class) {
            return stringValue;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(stringValue);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(stringValue);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(stringValue);
        } else if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(stringValue);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(stringValue);
        }
        
        return value;
    }
}
