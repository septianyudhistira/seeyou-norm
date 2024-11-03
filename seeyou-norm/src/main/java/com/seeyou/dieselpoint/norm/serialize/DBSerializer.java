package com.seeyou.dieselpoint.norm.serialize;


import java.lang.annotation.*;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DBSerializer {
    Class<?extends DBSerializable> value();
}