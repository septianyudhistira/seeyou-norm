package com.seeyou.dieselpoint.norm.sqlmakers;


/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public interface PojoInfo {
    public Object getValue(Object pojo, String name);

    public void putValue(Object pojo, String name, Object value);

    public MyProperty getGeneratedColumnProperty();
}
