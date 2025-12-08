package frame.servlet;

import java.lang.reflect.Method;

public class Mapping {
    private String className;
    private String methodName;
    private Method method;
    
    public Mapping(String className, String methodName, Method method) {
        this.className = className;
        this.methodName = methodName;
        this.method = method;
    }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    
    public Method getMethod() { return method; }
    public void setMethod(Method method) { this.method = method; }
}
