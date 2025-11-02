import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface ClassAnnotation {
    String value() default "";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MethodAnnotation {
    String description() default "";
}

public class AnnotationDetector {

    public static boolean isClassAnnotated(Class<?> clazz,
            Class<? extends Annotation> annotationClass) {
        return clazz.isAnnotationPresent(annotationClass);
    }

    public static boolean isMethodAnnotated(Method method,
            Class<? extends Annotation> annotationClass) {
        return method.isAnnotationPresent(annotationClass);
    }

    public static void analyzeClass(Class<?> clazz) {
        System.out.println("=== Analyse de la classe: " + clazz.getSimpleName() + " ===\n");

        // Vérifier les annotations de la classe
        Annotation[] classAnnotations = clazz.getAnnotations();
        if (classAnnotations.length > 0) {
            System.out.println("Annotations de la classe:");
            for (Annotation annotation : classAnnotations) {
                System.out.println("  - " + annotation.annotationType().getSimpleName());
            }
        } else {
            System.out.println("Aucune annotation sur la classe");
        }

        System.out.println();

        // Vérifier les annotations des méthodes
        Method[] methods = clazz.getDeclaredMethods();
        System.out.println("Méthodes de la classe:");
        for (Method method : methods) {
            System.out.println("  Méthode: " + method.getName());

            Annotation[] methodAnnotations = method.getAnnotations();
            if (methodAnnotations.length > 0) {
                for (Annotation annotation : methodAnnotations) {
                    System.out.println("    - Annotation: " +
                            annotation.annotationType().getSimpleName());
                }
            } else {
                System.out.println("    - Aucune annotation");
            }
        }
        System.out.println();
    }

    public static <T extends Annotation> T getClassAnnotation(Class<?> clazz,
            Class<T> annotationClass) {
        return clazz.getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T getMethodAnnotation(Method method,
            Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }
}