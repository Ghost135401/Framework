package framework.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UploadEndpoint {
    String path() default "";
    String method() default "POST";
}
