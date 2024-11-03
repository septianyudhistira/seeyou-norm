package com.seeyou.dieselpoint.norm;

import com.seeyou.dieselpoint.cache.FixedCache;
import com.seeyou.logging.MyLogger;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class Database {
    private static DataSource dataSource;
    public static long        maxTimeDDB = 0;

    public static void open(DataSource ds) {
        dataSource = ds;
        MyLogger.debug("INIT JDBC 2 DATASOURCE" + ds);
    }

    public int batchUpdate(final List<String> sqls) throws Exception {
        Connection conn   = null;
        int        length = 0;

        final long start = System.currentTimeMillis();

        try {
            conn = dataSource.getConnection();

            final Statement stmt = conn.createStatement();

            conn.setAutoCommit(false);

            for (String sql : sqls) {
                stmt.addBatch(sql);
            }

            length = stmt.executeBatch().length;
            conn.commit();

            for (String sql : sqls) {
                if (sql.contains("UPDATE thoth_accounts")) {
                }

                CacheUtil.removeChache(sql);
            }

            try {
                stmt.close();
            } catch (Exception e) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e2) {
            }
        } catch (Exception e) {
            MyLogger.error(e.getMessage(), e);

            try {
                if (conn != null) {
                    MyLogger.error("DB ROLE BACK : " + sqls);

                    conn.rollback();
                }
            } catch (SQLException e1) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e2) {
            }

            throw e;
        }

        final long dbTimes = System.currentTimeMillis() - start;

        if (dbTimes > maxTimeDDB) {
            maxTimeDDB = dbTimes;
        }

        MyLogger.db("SQL (" + dbTimes + "ms) : [" + sqls + "], rows : " + length);

        return length;
    }

    public Map<String, String> first(final String sqlParam)
            throws Exception {
        final List<Map<String, String>> results = executeSql(sqlParam);

        return results.isEmpty() ? new HashMap<String, String>() : results.get(0);
    }

    public String getValue(final String sqlParam) throws Exception {
        final List<Map<String, String>> results     = executeSql(sqlParam);
        String                          returnValue = "";

        if (! results.isEmpty()) {
            final Map<String, String> map = results.get(0);
            final String              key = map.keySet().iterator().next();
            returnValue                   = map.get(key);
        }

        return returnValue;
    }

    public List<Map<String, String>> executeSql(final String sqlParam)
            throws Exception {
        final long start = System.currentTimeMillis();
        String     sql   = sqlParam;

        final boolean isFixCacheData = sql.startsWith("FIXCACHE");

        if (isFixCacheData) {
            sql = sql.replace("FIXCACHE ", "");

            final Object fixCacheData = FixedCache.get(sql);

            if (fixCacheData != null) {
                return (List<Map<String, String>>) fixCacheData;
            }
        }

        final boolean isCacheData = sql.startsWith("CACHE");

        if (isCacheData) {
            sql = sql.replace("CACHE ", "");

            final Object cacheData = CacheUtil.get(sql);

            if (cacheData != null) {
                return (List<Map<String, String>>) cacheData;
            }
        }

        List<Map<String, String>> returnValue = new ArrayList<Map<String, String>>();
        Connection                connection  = null;

        try {
            connection = dataSource.getConnection();

            final Statement createStatement = connection.createStatement();

            if (sql.startsWith("SELECT") || sql.startsWith("WITH")) {
                final ResultSet executeQuery = createStatement.executeQuery(sql);
                returnValue = getListMapResult(executeQuery);
                executeQuery.close();
            } else {
                final int row = createStatement.executeUpdate(sql);

                if (row != 0) {
                    final Map<String, String> map = new HashMap<String, String>();

                    map.put("ROWS", String.valueOf(row));

                    returnValue.add(map);
                    CacheUtil.removeChache(sqlParam);
                }
            }

            try {
                createStatement.close();
            } catch (Exception e) {
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e1) {
                }
            }
        } catch (Exception e) {
            MyLogger.error(e.getMessage() + " [" + sqlParam + "]", e);

            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e1) {
                }
            }

            throw e;
        }

        if (isFixCacheData) {
            FixedCache.putCache(sql, returnValue);
        }

        if (isCacheData && ! returnValue.isEmpty()) {
            CacheUtil.putCache(sql, returnValue);
        }

        final long end = System.currentTimeMillis() - start;

        if (end > 500) {
            MyLogger.dbError("SLOW_QUERY : " + (end) + "ms) : [" + sql + "]");
        }

        ParsingCreateIndex.parseSql(sql);

        final int dataLength = returnValue.size();

        if ((dataLength > 5000) && ! isCacheData && ! isFixCacheData) {
            MyLogger.warn("BIG_SQL_RESULT : " + (end) + "ms) : [" + sql + "], size : " + dataLength);
        }

        if (end > maxTimeDDB) {
            maxTimeDDB = end;
        }

        MyLogger.db("SQL (" + end + "ms) : [" + sql + "], results : " + (returnValue.toString().replace("\r", "").replace("\n", "")));

        return returnValue;
    }

    public List<Map<String, String>> update(final String sqlParam)
            throws Exception {
        final long start = System.currentTimeMillis();
        String     sql   = sqlParam;

        List<Map<String, String>> returnValue = new ArrayList<Map<String, String>>();
        Connection                connection  = null;

        try {
            connection = dataSource.getConnection();

            final Statement createStatement = connection.createStatement();
            final int       row             = createStatement.executeUpdate(sql);

            if (row != 0) {
                final Map<String, String> map = new HashMap<String, String>();

                map.put("ROWS", String.valueOf(row));

                returnValue.add(map);
                CacheUtil.removeChache(sqlParam);
            }

            try {
                createStatement.close();
            } catch (Exception e) {
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e1) {
                }
            }
        } catch (Exception e) {
            MyLogger.error(e.getMessage() + " [" + sqlParam + "]", e);

            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e1) {
                }
            }

            throw e;
        }

        final long end = System.currentTimeMillis() - start;

        if (end > 500) {
            MyLogger.dbError("SLOW_QUERY : " + (end) + "ms) : [" + sql + "]");
        }

        ParsingCreateIndex.parseSql(sql);

        if (end > maxTimeDDB) {
            maxTimeDDB = end;
        }

        MyLogger.db("SQL (" + end + "ms) : [" + sql + "], results : " + (returnValue.toString().replace("\r", "").replace("\n", "")));

        return returnValue;
    }

    private List<Map<String, String>> getListMapResult(final ResultSet rs) {
        final List<Map<String, String>> returnValue = new ArrayList<Map<String, String>>();

        try {
            final ResultSetMetaData metaData = rs.getMetaData();
            final int               size     = metaData.getColumnCount();

            while (rs.next()) {
                final Map<String, String> map = new HashMap<String, String>();

                for (int i = 0; i < size; i++) {
                    final int    index = i + 1;
                    final String key   = metaData.getColumnLabel(index);
                    final String val   = rs.getString(index);

                    map.put(key.toUpperCase(), val);
                }

                returnValue.add(map);
            }
        } catch (Exception e) {
            MyLogger.error(e.getMessage(), e);
        }

        return returnValue;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public String generateInsertQuery(final String table, final Map<String, String> map) {
        final StringBuilder updateQuery = new StringBuilder("INSERT INTO ") //
                .append(table) //
                .append("(");

        int                 i = 0;

        for (String key : map.keySet()) {
            updateQuery.append(key);

            if (i != (map.size() - 1)) {
                updateQuery.append(",");
            }

            i++;
        }

        updateQuery.append(") VALUES (");
        i = 0;

        for (String key : map.keySet()) {
            Object val = map.get(key);

            if (val == null) {
                updateQuery.append("null");
            } else {
                if (val instanceof Double) {
                    val = new BigDecimal(String.valueOf(val)).longValue();
                } else if (val instanceof String) {
                    val = String.valueOf(val).replaceAll("'", "''");
                }

                updateQuery.append("'").append(val).append("'");
            }

            if (i != (map.size() - 1)) {
                updateQuery.append(",");
            }

            i++;
        }

        updateQuery.append(")");

        return updateQuery.toString().replace("'null'", "null");
    }

    public String generateUpdateQuery(final String table, final Map<String, String> map) {
        final StringBuilder updateQuery = new StringBuilder("UPDATE ") //
                .append(table) //
                .append(" SET ");

        String              primaryColumn = "";
        long                valColumn     = 0;
        int                 i             = 0;
        int                 size          = map.size();

        for (String key : map.keySet()) {
            Object val = map.get(key);

            if (key.equals("id")) {
                primaryColumn     = "id";
                valColumn         = new BigDecimal(String.valueOf(val)).longValue();

                continue;
            }

            if (val instanceof Double) {
                val = new BigDecimal(String.valueOf(val)).longValue();
            } else if (val instanceof String) {
                val = String.valueOf(val).replaceAll("'", "''");
            }

            updateQuery.append(key);
            updateQuery.append("=");

            if ((val == null) || val.equals("null")) {
                updateQuery.append("null");
            } else {
                updateQuery.append("'").append(val).append("'");
            }

            if (i != (size - 1)) {
                updateQuery.append(",");
            }

            i++;
        }

        if ((primaryColumn == null) || primaryColumn.isEmpty()) {
            return "";
        }

        updateQuery //
                .append(" WHERE ") //
                .append(primaryColumn).append("=").append("'") //
                .append(valColumn).append("'");

        return updateQuery.toString().replace(", WHERE", " WHERE").replace("'null'", "null");
    }

    public String generateDeleteQuery(final String table, final Map<String, String> map) {
        final StringBuilder updateQuery   = new StringBuilder("DELETE FROM ") //
                .append(table); //

        String              primaryColumn = "";
        long                valColumn     = 0;

        for (String key : map.keySet()) {
            final Object val = map.get(key);

            if (key.equals("id")) {
                primaryColumn     = "id";
                valColumn         = new BigDecimal(String.valueOf(val)).longValue();

                break;
            }
        }

        if ((primaryColumn == null) || primaryColumn.isEmpty()) {

            return "";
        }

        updateQuery //
                .append(" WHERE ") //
                .append(primaryColumn).append("=").append("'") //
                .append(valColumn).append("'");

        return updateQuery.toString();
    }
}
