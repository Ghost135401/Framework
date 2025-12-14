package frame.servlet;

import frame.annotation.URLMapping;
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
            System.out.println("║   INITIALISATION FRONT CONTROLLER - DEBUT             ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");

            String packageName = getServletContext().getInitParameter("controllerPackage");
            if (packageName == null) {
                packageName = "controller";
            }
cd
            System.out.println("📦 Package à scanner: " + packageName);

            scanControllers(packageName);

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║   MAPPINGS TROUVÉS                                     ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");

            if (urlMappings.isEmpty()) {
                System.err.println("❌❌❌ AUCUN MAPPING TROUVÉ ! ❌❌❌");
                System.err.println("Vérifiez que:");
                System.err.println("  1. Les classes sont dans WEB-INF/classes/controller/");
                System.err.println("  2. Les méthodes ont @URLMapping");
                System.err.println("  3. framework6.jar contient bien les annotations");
            } else {
                for (Map.Entry<String, Mapping> entry : urlMappings.entrySet()) {
                    System.out.println("✓ URL: " + entry.getKey() +
                            " → " + entry.getValue().getClassName() +
                            "." + entry.getValue().getMethodName() + "()");
                }
                System.out.println("\n✅ Total: " + urlMappings.size() + " mappings chargés");
            }

            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║   FRONT CONTROLLER PRÊT !                              ║");
            System.out.println("╚════════════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.err.println("❌ ERREUR FATALE lors du scan:");
            e.printStackTrace();
            throw new ServletException("Erreur lors du scan des contrôleurs", e);
        }
    }

    private void scanControllers(String packageName) throws Exception {
        String path = packageName.replace('.', '/');
        String realPath = getServletContext().getRealPath("/WEB-INF/classes/" + path);

        System.out.println("📂 Chemin recherché: /WEB-INF/classes/" + path);
        System.out.println("📍 Chemin réel: " + realPath);

        if (realPath == null) {
            System.err.println("❌ ERREUR: realPath est null - contexte non déployé correctement");
            return;
        }

        File directory = new File(realPath);
        if (!directory.exists()) {
            System.err.println("❌ ERREUR: Le répertoire n'existe pas: " + realPath);
            System.err.println("💡 Vérifiez que les .class sont bien compilés dans WEB-INF/classes/");
            return;
        }

        System.out.println("✓ Répertoire trouvé: " + directory.getAbsolutePath());
        System.out.println("🔍 Scan en cours...\n");

        scanDirectory(directory, packageName);
    }

    private void scanDirectory(File directory, String packageName) throws Exception {
        File[] files = directory.listFiles();
        if (files == null) {
            System.err.println("❌ Impossible de lister les fichiers dans: " + directory);
            return;
        }

        System.out.println("📁 Scan de: " + packageName);
        System.out.println("   Fichiers trouvés: " + files.length);

        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("   📁 Sous-dossier: " + file.getName());
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                System.out.println("   📄 Classe trouvée: " + className);
                processClass(className);
            }
        }
    }

    private void processClass(String className) throws Exception {
        try {
            Class<?> clazz = Class.forName(className);
            System.out.println("      ✓ Classe chargée: " + clazz.getName());

            Method[] methods = clazz.getDeclaredMethods();
            System.out.println("      → Méthodes trouvées: " + methods.length);

            int mappingsFound = 0;
            for (Method method : methods) {
                if (method.isAnnotationPresent(URLMapping.class)) {
                    URLMapping annotation = method.getAnnotation(URLMapping.class);
                    String url = annotation.value();

                    Mapping mapping = new Mapping(className, method.getName(), method);
                    urlMappings.put(url, mapping);

                    System.out.println("      ✓✓✓ MAPPING: " + url + " → " + method.getName() + "()");
                    mappingsFound++;
                }
            }

            if (mappingsFound == 0) {
                System.out.println("      ⚠️  Aucun @URLMapping trouvé dans cette classe");
            }

        } catch (ClassNotFoundException e) {
            System.err.println("      ❌ Impossible de charger la classe: " + className);
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
        System.out.println("║   REQUÊTE REÇUE                                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("📥 URI complète: " + uri);
        System.out.println("📂 Context path: " + contextPath);
        System.out.println("🎯 URL recherchée: '" + url + "'");
        System.out.println("📋 Mappings disponibles: " + urlMappings.keySet());

        // Ressources statiques
        if (url.equals("/") || url.isEmpty() ||
                url.endsWith(".html") || url.endsWith(".jsp") ||
                url.endsWith(".css") || url.endsWith(".js") ||
                url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".gif")) {

            System.out.println("➡️  Ressource statique, forwarding...");

            if (url.equals("/") || url.isEmpty()) {
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            } else {
                request.getRequestDispatcher(url).forward(request, response);
            }
            return;
        }

        Mapping mapping = urlMappings.get(url);

        if (mapping == null) {
            System.err.println("❌ AUCUN MAPPING TROUVÉ POUR: '" + url + "'");
            System.err.println("💡 URLs valides:");
            for (String key : urlMappings.keySet()) {
                System.err.println("   - " + key);
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "URL non mappée: " + url);
            return;
        }

        System.out.println("✅ Mapping trouvé!");

        try {
            Class<?> clazz = Class.forName(mapping.getClassName());
            Object controller = clazz.getDeclaredConstructor().newInstance();
            Method method = mapping.getMethod();

            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];

            System.out.println("🔧 Méthode: " + method.getName());
            System.out.println("📊 Paramètres attendus: " + parameters.length);

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                String paramName = "arg" + i;
                String paramValue = request.getParameter(paramName);

                System.out.println(
                        "   - " + paramName + " = " + paramValue + " (type: " + param.getType().getSimpleName() + ")");

                if (paramValue == null || paramValue.trim().isEmpty()) {
                    throw new Exception("Paramètre manquant: " + paramName);
                }

                Class<?> paramType = param.getType();
                args[i] = convertParameter(paramValue, paramType);
            }

            Object result = method.invoke(controller, args);

            System.out.println("✅ Résultat: " + result);
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
                    "    .result { padding: 20px; background: #e8f5e9; border-radius: 8px; border-left: 4px solid #4CAF50; }");
            response.getWriter().println("    .back { margin-top: 20px; }");
            response.getWriter().println("    a { color: #0066cc; text-decoration: none; }");
            response.getWriter().println("    a:hover { text-decoration: underline; }");
            response.getWriter().println("  </style>");
            response.getWriter().println("</head>");
            response.getWriter().println("<body>");
            response.getWriter().println("  <h2>✅ Résultat</h2>");
            response.getWriter().println("  <div class='result'>" + result + "</div>");
            response.getWriter().println("  <div class='back'>");
            response.getWriter().println("    <a href='javascript:history.back()'>← Retour</a> | ");
            response.getWriter().println("    <a href='/test-project/'>🏠 Accueil</a>");
            response.getWriter().println("  </div>");
            response.getWriter().println("</body>");
            response.getWriter().println("</html>");

        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de l'exécution:");
            e.printStackTrace();
            throw new ServletException("Erreur lors de l'exécution du contrôleur: " + e.getMessage(), e);
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