package mg.framework.core;

import java.lang.reflect.Method;

public class ControllerMapping {
    private Object controller;
    private Method method;
    private String httpMethod;

    public ControllerMapping(Object controller, Method method, String httpMethod) {
        this.controller = controller;
        this.method = method;
        this.httpMethod = httpMethod;
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    public String getHttpMethod() {
        return httpMethod;
    }
}
