package framework.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LoginRequired {
    String redirect() default "/login";
    String[] roles() default {};
}
