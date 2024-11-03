package com.seeyou.dieselpoint.norm;

import com.seeyou.dieselpoint.norm.sqlmakers.PostgresMaker;
import com.seeyou.dieselpoint.norm.sqlmakers.SqlMaker;
import com.seeyou.logging.MyLogger;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class DB {
    private static DataSource ds;
    private final SqlMaker    sqlMaker;

    public DB() {
        this.sqlMaker = new PostgresMaker();
    }

    public static void open(DataSource ds) {
        DB.ds = ds;
        MyLogger.info("INIT NORM 1 DATASOURCE " + ds);
    }

    public SqlMaker getSqlMaker() {
        return sqlMaker;
    }

    /**
     * Create a query using straight SQL. Overrides any other methods like
     * .where(), .orderBy(), etc.
     *
     * @param sql The SQL string to use, may include ? parameters.
     * @param args The parameter values to use in the query.
     */
    public Query sql(String sql) {
        return new Query(this).sql(sql);
    }

    /**
     * Returns a JDBC connection. Can be useful if you need to customize how
     * transactions work, but you shouldn't normally need to call this method.
     * You must close the connection after you're done with it.
     */
    public Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (Throwable t) {
            throw new DBException(t);
        }
    }

    /**
     * Execute a "select" query and get some results. The system will create a
     * new object of type "clazz" for each row in the result set and add it to a
     * List. It will also try to extract the table name from a @Table annotation
     * in the clazz.
     public <T> List<T> results(Class<T> clazz) {
     return new Query(this).results(clazz);
     }
     */

    /**
     * Returns the first row in a query in a pojo. Will return it in a Map if a
     * class that implements Map is specified.
     public <T> T first(Class<T> clazz) {
     return new Query(this).first(clazz);
     }
     public StandardPojoInfo getPojo(final Class rowClass) {
     sqlMaker.getPojoInfo(rowClass);

     return (Data.pojoMap).get(rowClass);
     }

     public int batchUpdate(final List<String> sqls) {
     Connection conn   = null;
     int        length = 0;

     final long start = System.currentTimeMillis();

     try {
     conn = getConnection();

     final Statement stmt = conn.createStatement();

     conn.setAutoCommit(false);

     for (String sql : sqls) {
     stmt.addBatch(sql);
     }

     length = stmt.executeBatch().length;
     conn.commit();

     //            boolean isContainNeracaSaldoUpdate = false;
     for (String sql : sqls) {
     //                if (sql.contains("UPDATE thoth_accounts")) {
     //                    isContainNeracaSaldoUpdate = true;
     //                }
     CacheUtil.removeChache(sql);

     //                new Thread(new RemoveCacheResultDB(sql)).start();
     }

     //            if(isContainNeracaSaldoUpdate){
     //                new MonitoringNeracaSaldo(sqls);
     //            }
     } catch (Exception e) {
     MyLogger.error(e.getMessage(), e);

     try {
     if (conn != null) {
     MyLogger.error("DB ROLE BACK : " + sqls);
     conn.rollback();
     }
     } catch (SQLException e1) {
     }
     } finally {
     try {
     if (conn != null) {
     conn.close();
     }
     } catch (SQLException e) {
     }
     }

     final long end = System.currentTimeMillis() - start;

     if (end > 4000) {
     MyLogger.dbError("SLOW_QUERY : " + (end) + "ms) : [" + sqls + "], rows : " + length);
     }

     MyLogger.db("SQL (" + (end) + "ms) : [" + sqls + "], rows : " + length);

     return length;
     }
     */

    /**
     * Create a query and specify which table it operates on.
     public Query table(String table) {
     return new Query(this).table(table);
     }

     public Query from(String... table) {
     return new Query(this).from(table);
     }
     */

    /**
     * Create a query with the given where clause.
     * @param where Example: "name=?"
     * @param args The parameter values to use in the where, example: "Bob"
     */
    public Query where(String where, Object... args) {
        return new Query(this).where(where, args);
    }
}
