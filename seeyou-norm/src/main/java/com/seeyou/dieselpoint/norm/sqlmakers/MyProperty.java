package com.seeyou.dieselpoint.norm.sqlmakers;

import com.seeyou.dieselpoint.norm.serialize.DBSerializable;
import net.data.annotation.Column;
import net.data.annotation.EnumType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class MyProperty {
    public String       name;
    public Method       readMethod;
    public Method       writeMethod;
    public Field        field;
    public Class<?>     dataType;
    public boolean      isGenerated;
    public boolean      isPrimaryKey;
    public boolean      isEnumField;
    public Class<Enum>  enumClass;
    public EnumType enumType;
    public Column columnAnnotation;
    public DBSerializable serializer;

}
