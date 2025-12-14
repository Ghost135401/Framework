package mg.framework.servlet;

import mg.framework.annotation.*;
import mg.framework.core.ControllerMapping;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DispatcherServlet extends HttpServlet {
    private Map<String, ControllerMapping> urlMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        String packageToScan = getInitParameter("basePackage");
        if (packageToScan == null || packageToScan.isEmpty()) {
            throw new ServletException("Le paramètre 'basePackage' est obligatoire");
        }

        try {
            scanControllers(packageToScan);
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des contrôleurs" + e.getMessage(), e);
        }
    }

    private void scanControllers(String basePackage) throws Exception {
        // Utiliser le classloader de la servlet au lieu du thread
        ClassLoader classLoader = this.getClass().getClassLoader();
        String path = basePackage.replace('.', '/');
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            System.out.println("Package non trouvé: " + basePackage);
            return;
        }

        File directory = new File(resource.getFile());
        if (directory.exists()) {
            scanDirectory(directory, basePackage, classLoader);
        }
    }

    private void scanDirectory(File directory, String packageName, ClassLoader classLoader) throws Exception {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classLoader);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = classLoader.loadClass(className);

                    if (clazz.isAnnotationPresent(Controller.class)) {
                        registerController(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Impossible de charger: " + className);
                    throw e;
                }
            }
        }
    }

    private void registerController(Class<?> controllerClass) throws Exception {
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping annotation = method.getAnnotation(GetMapping.class);
                String url = annotation.value();
                urlMappings.put("GET:" + url, new ControllerMapping(controllerInstance, method, "GET"));
                System.out.println("Enregistré: GET " + url + " -> " + controllerClass.getSimpleName() + "." + method.getName());
            }

            if (method.isAnnotationPresent(PostMapping.class)) {
                PostMapping annotation = method.getAnnotation(PostMapping.class);
                String url = annotation.value();
                urlMappings.put("POST:" + url, new ControllerMapping(controllerInstance, method, "POST"));
                System.out.println("Enregistré: POST " + url + " -> " + controllerClass.getSimpleName() + "." + method.getName());
            }

            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                String url = annotation.value();
                String httpMethod = annotation.method().name();
                urlMappings.put(httpMethod + ":" + url, new ControllerMapping(controllerInstance, method, httpMethod));
                System.out.println("Enregistré: " + httpMethod + " " + url + " -> " + controllerClass.getSimpleName() + "." + method.getName());
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI().substring(req.getContextPath().length());
        String method = req.getMethod();
        String key = method + ":" + uri;

        ControllerMapping mapping = urlMappings.get(key);

        if (mapping == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().println("URL non trouvée: " + method + " " + uri);
            return;
        }

        try {
            Object result = invokeMethod(mapping, req, resp);

            if (result != null) {
                resp.setContentType("text/html;charset=UTF-8");
                PrintWriter out = resp.getWriter();
                out.println(result.toString());
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'invocation de la méthode", e);
        }
    }

    private Object invokeMethod(ControllerMapping mapping, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method method = mapping.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            if (param.getType() == HttpServletRequest.class) {
                args[i] = req;
            } else if (param.getType() == HttpServletResponse.class) {
                args[i] = resp;
            } else if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam annotation = param.getAnnotation(RequestParam.class);
                String paramName = annotation.value();
                String paramValue = req.getParameter(paramName);

                if (paramValue == null && annotation.required()) {
                    throw new ServletException("Paramètre obligatoire manquant: " + paramName);
                }

                args[i] = convertParameter(paramValue, param.getType());
            } else {
                args[i] = null;
            }
        }

        return method.invoke(mapping.getController(), args);
    }

    private Object convertParameter(String value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        return value;
    }
}
