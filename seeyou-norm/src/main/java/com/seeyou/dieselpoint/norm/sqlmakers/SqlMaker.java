package com.seeyou.dieselpoint.norm.sqlmakers;

import com.seeyou.dieselpoint.norm.Query;

import java.sql.ResultSet;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public interface SqlMaker {
    public String getInsertSql(Query query, Object row);

    public Object[] getInsertArgs(Query query, Object row);

    public String getUpdateSql(Query query, Object row);

    public Object[] getUpdateArgs(Query query, Object row);

    public String getDeleteSql(Query query, Object row);

    public Object[] getDeleteArgs(Query query, Object row);

    public String getUpsertSql(Query query, Object row);

    public Object[] getUpsertArgs(Query query, Object row);

    public String getSelectSql(Query query, Class rowClass);

    public String getCreateTableSql(Class<?> clazz);

    public PojoInfo getPojoInfo(Class rowClass);

    public void populateGeneratedKey(ResultSet generatedKeys, Object insertRow);
}
