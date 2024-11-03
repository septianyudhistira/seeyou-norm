package com.seeyou.dieselpoint.norm.sqlmakers;

import com.seeyou.dieselpoint.norm.DBException;
import com.seeyou.dieselpoint.norm.serialize.DBSerializer;
import net.data.annotation.*;

import java.beans.IntrospectionException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class StandardPojoInfo implements PojoInfo{

    LinkedHashMap<String, MyProperty> propertyMap = new LinkedHashMap<String, MyProperty>();
    String table;
    String primaryKeyName;
    String generatedColumnName;

    String insertSql;
    int insertSqlArgCount;
    String [] insertColumnNames;

    String upsertSql;
    int upsertSqlArgCount;
    String [] upsertColumnNames;

    String updateSql;
    String[] updateColumnNames;
    int updateSqlArgCount;

    String selectColumns;

    public StandardPojoInfo(Class<?> clazz) {

        try {

            if (Map.class.isAssignableFrom(clazz)) {
                //leave properties empty
            } else {
                List<MyProperty> props = populateProperties(clazz);

                ColumnOrder colOrder = clazz.getAnnotation(ColumnOrder.class);
                if (colOrder != null) {
                    // reorder the properties
                    String [] cols = colOrder.value();
                    List<MyProperty> reordered = new ArrayList<>();
                    for (int i = 0; i < cols.length; i++) {
                        for (MyProperty prop: props) {
                            if (prop.name.equals(cols[i])) {
                                reordered.add(prop);
                                break;
                            }
                        }
                    }
                    // props not in the cols list are ignored
                    props = reordered;
                }

                for (MyProperty prop: props) {
                    propertyMap.put(prop.name, prop);
                }
            }

            Table annot = clazz.getAnnotation(Table.class);
            if (annot != null) {
                if (annot.schema() != null && !annot.schema().isEmpty()) {
                    table = annot.schema() + "." + annot.name();
                }
                else {
                    table = annot.name();
                }
            } else {
                table = clazz.getSimpleName();
            }

        } catch (Throwable t) {
            throw new DBException(t);
        }
    }



    private List<MyProperty> populateProperties(Class<?> clazz) throws IntrospectionException, InstantiationException, IllegalAccessException {

        List<MyProperty> props = new ArrayList<>();

        for (Field field : clazz.getFields()) {
            int modifiers = field.getModifiers();

            if (Modifier.isPublic(modifiers)) {

                if (Modifier.isStatic(modifiers)
                        || Modifier.isFinal(modifiers)) {
                    continue;
                }

                if (field.getAnnotation(Transient.class) != null) {
                    continue;
                }

                MyProperty prop = new MyProperty();
                prop.name = field.getName();
                prop.field = field;
                prop.dataType = field.getType();

                applyAnnotations(prop, field);

                props.add(prop);
            }
        }
        return props;
    }


    /**
     * Apply the annotations on the field or getter method to the property.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void applyAnnotations(MyProperty prop, AnnotatedElement ae) throws InstantiationException, IllegalAccessException {

        Column col = ae.getAnnotation(Column.class);
        if (col != null) {
            String name = col.name().trim();
            if (name.length() > 0) {
                prop.name = name;
            }
            prop.columnAnnotation = col;
        }

        if (ae.getAnnotation(Id.class) != null) {
            prop.isPrimaryKey = true;
            primaryKeyName = prop.name;
        }

        if (ae.getAnnotation(GeneratedValue.class) != null) {
            generatedColumnName = prop.name;
            prop.isGenerated = true;
        }

        if (prop.dataType.isEnum()) {
            prop.isEnumField = true;
            prop.enumClass = (Class<Enum>) prop.dataType;
            /* We default to STRING enum type. Can be overriden with @Enumerated annotation */
            prop.enumType = EnumType.STRING;
            if (ae.getAnnotation(Enumerated.class) != null) {
                prop.enumType = ae.getAnnotation(Enumerated.class).value();
            }
        }

        DBSerializer sc = ae.getAnnotation(DBSerializer.class);
        if (sc != null) {
            prop.serializer = sc.value().newInstance();
        }

    }

    public Object getValue(Object pojo, String name) {

        try {

            MyProperty prop = propertyMap.get(name);
            if (prop == null) {
                throw new DBException("No such field: " + name);
            }

            Object value = null;

            if (prop.readMethod != null) {
                value = prop.readMethod.invoke(pojo);

            } else if (prop.field != null) {
                value = prop.field.get(pojo);
            }

            if (value != null) {
                if (prop.serializer != null) {
                    value =  prop.serializer.serialize(value);

                } else if (prop.isEnumField) {
                    // handle enums according to selected enum type
                    if (prop.enumType == EnumType.ORDINAL) {
                        value = ((Enum) value).ordinal();
                    }
                    // EnumType.STRING and others (if present in the future)
                    else {
                        value = value.toString();
                    }
                }
            }

            return value;

        } catch (Throwable t) {
            throw new DBException(t);
        }
    }

    public void putValue(Object pojo, String name, Object value) {

        MyProperty prop = propertyMap.get(name);
        if (prop == null) {
            throw new DBException("No such field: " + name);
        }

        if (value != null) {
            if (prop.serializer != null) {
                value = prop.serializer.deserialize((String) value, prop.dataType);

            } else if (prop.isEnumField) {
                value = getEnumConst(prop.enumClass, prop.enumType, value);
            }
        }

        if (prop.writeMethod != null) {
            try {
                prop.writeMethod.invoke(pojo, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new DBException("Could not write value into pojo. Property: " + prop.name + " method: "
                        + prop.writeMethod.toString() + " value: " + value + " value class:" + value.getClass().toString(), e);
            }
            return;
        }

        if (prop.field != null) {
            try {
                prop.field.set(pojo, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new DBException("Could not set value into pojo. Field: " + prop.field.toString() + " value: " + value, e);
            }
            return;
        }

    }

    /**
     * Convert a string to an enum const of the appropriate class.
     */
    private <T extends Enum<T>> Object getEnumConst(Class<T> enumType, EnumType type, Object value) {
        String str = value.toString();
        if (type == EnumType.ORDINAL) {
            Integer ordinalValue = (Integer) value;
            if (ordinalValue < 0 || ordinalValue >= enumType.getEnumConstants().length) {
                throw new DBException("Invalid ordinal number " + ordinalValue + " for enum class " + enumType.getCanonicalName());
            }
            return enumType.getEnumConstants()[ordinalValue];
        }
        else {
            for (T e: enumType.getEnumConstants()) {
                if (str.equals(e.toString())) {
                    return e;
                }
            }
            throw new DBException("Enum value does not exist. value:" + str);
        }
    }



    @Override
    public MyProperty getGeneratedColumnProperty() {
        return propertyMap.get(generatedColumnName);
    }


}
