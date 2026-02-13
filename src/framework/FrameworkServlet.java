package framework;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public abstract class FrameworkServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, Object> parameters = new HashMap<>();
        
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] values = request.getParameterValues(paramName);
            
            if (values.length == 1) {
                parameters.put(paramName, values[0]);
            } else {
                parameters.put(paramName, values);
            }
        }

        Enumeration<String> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attrName = attributeNames.nextElement();
            parameters.put(attrName, request.getAttribute(attrName));
        }

        try {
            invokeSaveMethod(parameters);
            onSuccess(request, response, parameters);
        } catch (Exception e) {
            onError(request, response, parameters, e);
        }
    }

    private void invokeSaveMethod(Map<String, Object> parameters) throws Exception {
        Method[] methods = this.getClass().getDeclaredMethods();
        
        for (Method method : methods) {
            if ("save".equals(method.getName())) {
                Parameter[] params = method.getParameters();
                
                if (params.length == 1 && params[0].isAnnotationPresent(EntityParam.class)) {
                    Class<?> entityClass = params[0].getType();
                    Object entity = ObjectMapper.mapToObject(parameters, entityClass);
                    method.invoke(this, entity);
                    return;
                }
                
                if (params.length == 1 && params[0].getType() == Map.class) {
                    method.invoke(this, parameters);
                    return;
                }
            }
        }
        
        throw new NoSuchMethodException("Méthode save(@EntityParam) ou save(Map) non trouvée");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected abstract void onSuccess(HttpServletRequest request, HttpServletResponse response,
                                    Map<String, Object> parameters) throws ServletException, IOException;

    protected abstract void onError(HttpServletRequest request, HttpServletResponse response,
                                  Map<String, Object> parameters, Exception e) throws ServletException, IOException;
}
