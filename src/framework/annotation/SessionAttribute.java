package framework.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SessionAttribute {
    String value() default "";
    boolean required() default false;
}
