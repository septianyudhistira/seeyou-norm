package com.seeyou.dieselpoint.norm.serialize;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public interface DBSerializable {
    public String serialize(Object in);

    public Object deserialize(String in, Class<?> targetClass);
}
