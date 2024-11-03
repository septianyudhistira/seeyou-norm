package net.data.annotation;

import java.lang.annotation.*;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Entity {
    String name() default "";
}
