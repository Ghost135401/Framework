package framework.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UploadField {
    String value() default "";
    boolean required() default false;
    long maxSize() default 10485760; // 10MB par défaut
    String[] allowedTypes() default {};
}
