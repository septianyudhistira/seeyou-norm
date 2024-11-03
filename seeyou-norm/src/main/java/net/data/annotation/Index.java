package net.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {
    String name() default "";

    String columnList();

    boolean unique() default false;
}