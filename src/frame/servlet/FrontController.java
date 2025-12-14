package frame.servlet;

import frame.annotation.URLMapping;
import frame.annotation.RequestParam;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class FrontController extends HttpServlet {
    private Map<String, Mapping> urlMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        try {
            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║   FRONT CONTROLLER - RÉSOLUTION AUTO PARAMÈTRES       ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");

            String packageName = getServletContext().getInitParameter("controllerPackage");
            if (packageName == null) {
                packageName = "controller";
            }

            System.out.println("📦 Package: " + packageName);
            scanControllers(packageName);

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║   MAPPINGS TROUVÉS                                     ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");

            if (urlMappings.isEmpty()) {
                System.err.println("❌❌❌ AUCUN MAPPING ! ❌❌❌");
            } else {
                for (Map.Entry<String, Mapping> entry : urlMappings.entrySet()) {
                    System.out.println("✓ " + entry.getKey() + " → " +
                            entry.getValue().getClassName() + "." +
                            entry.getValue().getMethodName() + "()");
                }
                System.out.println("\n✅ " + urlMappings.size() + " mappings chargés");
            }
            System.out.println("╚════════════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.err.println("❌ ERREUR:");
            e.printStackTrace();
            throw new ServletException("Erreur lors du scan", e);
        }
    }

    private void scanControllers(String packageName) throws Exception {
        String path = packageName.replace('.', '/');
        String realPath = getServletContext().getRealPath("/WEB-INF/classes/" + path);

        System.out.println("📂 /WEB-INF/classes/" + path);
        System.out.println("📍 " + realPath);

        if (realPath == null) {
            System.err.println("❌ realPath null");
            return;
        }

        File directory = new File(realPath);
        if (!directory.exists()) {
            System.err.println("❌ Inexistant: " + realPath);
            return;
        }

        System.out.println("✓ OK\n");
        scanDirectory(directory, packageName);
    }

    private void scanDirectory(File directory, String packageName) throws Exception {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        System.out.println("📁 " + packageName + " (" + files.length + " fichiers)");

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                System.out.println("   📄 " + className);
                processClass(className);
            }
        }
    }

    private void processClass(String className) throws Exception {
        try {
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getDeclaredMethods();
            System.out.println("      → " + methods.length + " méthodes");

            for (Method method : methods) {
                if (method.isAnnotationPresent(URLMapping.class)) {
                    URLMapping annotation = method.getAnnotation(URLMapping.class);
                    String url = annotation.value();

                    Mapping mapping = new Mapping(className, method.getName(), method);
                    urlMappings.put(url, mapping);

                    System.out.println("      ✓✓✓ " + url + " → " + method.getName() + "()");

                    // Debug paramètres
                    Parameter[] params = method.getParameters();
                    for (int i = 0; i < params.length; i++) {
                        Parameter p = params[i];
                        String display;

                        if (p.isAnnotationPresent(RequestParam.class)) {
                            String paramName = p.getAnnotation(RequestParam.class).value();
                            display = "@RequestParam(\"" + paramName + "\") " + p.getType().getSimpleName();
                        } else {
                            display = p.getName() + " " + p.getType().getSimpleName() + " (auto)";
                        }

                        System.out.println("          [" + i + "] " + display);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("      ❌ Classe introuvable");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = uri.substring(contextPath.length());

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║   REQUÊTE                                              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("📥 URI: " + uri);
        System.out.println("📂 Context: " + contextPath);
        System.out.println("🎯 URL: '" + url + "'");

        Mapping mapping = urlMappings.get(url);

        if (mapping == null) {
            System.err.println("❌ PAS DE MAPPING pour: '" + url + "'");
            System.err.println("💡 Mappings: " + urlMappings.keySet());
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "URL non mappée: " + url);
            return;
        }

        System.out.println("✅ Mapping OK!");

        try {
            Class<?> clazz = Class.forName(mapping.getClassName());
            Object controller = clazz.getDeclaredConstructor().newInstance();
            Method method = mapping.getMethod();

            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];

            System.out.println("🔧 Méthode: " + method.getName());
            System.out.println("📊 Paramètres: " + parameters.length);
            System.out.println("\n🔍 Résolution des paramètres:");

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                String paramName;
                String resolveType;

                // Stratégie de résolution
                if (param.isAnnotationPresent(RequestParam.class)) {
                    // 1. @RequestParam prioritaire
                    RequestParam requestParam = param.getAnnotation(RequestParam.class);
                    paramName = requestParam.value();
                    resolveType = "@RequestParam";
                } else {
                    // 2. Nom réel du paramètre (nécessite -parameters lors compilation)
                    paramName = param.getName();
                    resolveType = "auto";
                }

                String paramValue = request.getParameter(paramName);

                System.out.println("   [" + i + "] '" + paramName + "' (" + resolveType + ")");
                System.out.println("       Type: " + param.getType().getSimpleName());
                System.out.println("       Valeur URL: " + paramValue);

                if (paramValue == null || paramValue.trim().isEmpty()) {
                    System.err.println("       ❌ MANQUANT!");
                    String error = "Paramètre manquant: '" + paramName + "' pour " + method.getName() + "()";

                    if (resolveType.equals("auto") && paramName.startsWith("arg")) {
                        error += "\n\n💡 Le nom du paramètre est '" + paramName +
                                "' (arg0, arg1...). Cela signifie que la compilation n'a pas " +
                                "conservé les vrais noms.\n" +
                                "Solution: Compilez avec l'option -parameters:\n" +
                                "javac -parameters -cp ... -d ... *.java";
                    }

                    throw new Exception(error);
                }

                Class<?> paramType = param.getType();
                args[i] = convertParameter(paramValue, paramType);
                System.out.println("       ✓ Converti: " + args[i]);
            }

            Object result = method.invoke(controller, args);

            System.out.println("\n✅ RÉSULTAT: " + result);
            System.out.println("╚════════════════════════════════════════════════════════╝\n");

            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<!DOCTYPE html>");
            response.getWriter().println("<html>");
            response.getWriter().println("<head>");
            response.getWriter().println("  <meta charset='UTF-8'>");
            response.getWriter().println("  <title>Résultat</title>");
            response.getWriter().println("  <style>");
            response.getWriter().println("    body { font-family: Arial; margin: 40px; background: #f5f5f5; }");
            response.getWriter().println(
                    "    .result { padding: 20px; background: #e8f5e9; border-radius: 8px; border-left: 4px solid #4CAF50; margin: 20px 0; }");
            response.getWriter().println("    .back { margin-top: 20px; }");
            response.getWriter().println(
                    "    a { color: #0066cc; text-decoration: none; padding: 8px 15px; background: #e3f2fd; border-radius: 4px; }");
            response.getWriter().println("    a:hover { background: #2196F3; color: white; }");
            response.getWriter().println("  </style>");
            response.getWriter().println("</head>");
            response.getWriter().println("<body>");
            response.getWriter().println("  <h2>✅ Résultat</h2>");
            response.getWriter().println("  <div class='result'>" + result + "</div>");
            response.getWriter().println("  <div class='back'>");
            response.getWriter().println("    <a href='javascript:history.back()'>← Retour</a>");
            response.getWriter().println("  </div>");
            response.getWriter().println("</body>");
            response.getWriter().println("</html>");

        } catch (Exception e) {
            System.err.println("❌ ERREUR:");
            e.printStackTrace();

            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<!DOCTYPE html>");
            response.getWriter().println("<html>");
            response.getWriter().println("<head><meta charset='UTF-8'><title>Erreur</title>");
            response.getWriter().println("<style>");
            response.getWriter().println("body { font-family: Arial; margin: 40px; background: #f5f5f5; }");
            response.getWriter().println(
                    ".error { padding: 20px; background: #ffebee; border-radius: 8px; border-left: 4px solid #f44336; }");
            response.getWriter()
                    .println("pre { background: #fff; padding: 15px; border-radius: 4px; overflow-x: auto; }");
            response.getWriter().println("</style></head><body>");
            response.getWriter().println("<h2 style='color:#f44336'>❌ Erreur</h2>");
            response.getWriter().println("<div class='error'>");
            response.getWriter().println("<strong>Message:</strong><br>");
            response.getWriter().println("<pre>" + e.getMessage() + "</pre>");
            response.getWriter().println("</div>");
            response.getWriter().println(
                    "<a href='javascript:history.back()' style='display:inline-block;margin-top:20px;color:#0066cc;text-decoration:none'>← Retour</a>");
            response.getWriter().println("</body></html>");
        }
    }

    private Object convertParameter(String value, Class<?> targetType) {
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