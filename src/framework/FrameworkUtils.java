package framework;

import java.util.Map;

public class FrameworkUtils {

    public static String getString(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }

    public static String getString(Map<String, Object> parameters, String key, String defaultValue) {
        String value = getString(parameters, key);
        return value != null ? value : defaultValue;
    }

    public static Integer getInt(Map<String, Object> parameters, String key) {
        String value = getString(parameters, key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public static Integer getInt(Map<String, Object> parameters, String key, Integer defaultValue) {
        Integer value = getInt(parameters, key);
        return value != null ? value : defaultValue;
    }

    public static Double getDouble(Map<String, Object> parameters, String key) {
        String value = getString(parameters, key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public static Boolean getBoolean(Map<String, Object> parameters, String key) {
        String value = getString(parameters, key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }

    public static String[] getStringArray(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value instanceof String[]) {
            return (String[]) value;
        } else if (value != null) {
            return new String[]{value.toString()};
        }
        return new String[0];
    }

    public static boolean containsKey(Map<String, Object> parameters, String key) {
        return parameters.containsKey(key);
    }
}
