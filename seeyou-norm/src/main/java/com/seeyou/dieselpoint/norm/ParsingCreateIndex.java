package com.seeyou.dieselpoint.norm;

import com.seeyou.logging.MyLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class ParsingCreateIndex {
    public static final Map<String, String> newSqlIndex      = new HashMap<String, String>();
    public static final Set<String> registerSqlIndex = new HashSet<String>();
    public static final Set<String>         tableNames       = new HashSet<String>();
    public static boolean                   isProduction     = false;
    public static boolean                   isCreateIndex    = false;

    public static void parseSql(final String sql2) {
        if (isCreateIndex) {
            if (sql2.startsWith("CREATE INDEX") || sql2.contains("pg_indexes") || sql2.contains("app_table_indexing") || sql2.contains("information_schema")) { //

                return;
            }

            if (! sql2.contains(".")) {
                if (sql2.contains("WHERE ")) {
                    MyLogger.dbError("SQL WHERE NOT HAVE DOT FOR : " + sql2);
                }

                //
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String sql = sql2.replaceAll("=", " = ").replaceAll("\n|\r", " ").replaceAll("  ", " ");

                    try {
                        parseSqlIndex(sql);
                    } catch (Exception e) {
                    }
                }
            }).start();
        }
    }
    public static void parseSqlIndex(final String sql) {
        final String[] split = sql.replaceAll("JOIN", "\nJOIN").replaceAll("WHERE", "\nWHERE").split("\n");

        for (String string : split) {
            // join
            if (string.startsWith("JOIN")) {
                final String joinTables = getJoinTables(string);
                createSqlIndex(true, joinTables);
            }

            // where
            if (string.startsWith("WHERE")) {
                final String whereTables = getWhereTables(string);
                createSqlIndex(false, whereTables);
            }
        }
    }

    private static void createSqlIndex(final boolean isFromJoin, final String joinTables) {
        final String[] split = joinTables.replaceAll("=", " ").replaceAll(",", "").replaceAll("\\)", "").replaceAll("\\(", "").split(" ");

        for (int i = 0; i < split.length; i++) {
            String column = split[i];

            if (column.contains(".id")) {
                continue;
            }

            if (column.contains(".") && (isFromJoin || column.endsWith("_id") || column.endsWith(".code") || column.contains(".created_at") || column.contains(".updated_at") || column.contains(".name"))) {
                final String tableName = column.split("\\.")[0].trim();

                if (tableNames.contains(tableName)) {
                    final String indexName = (column.replace(".", "_") + "_sys_idx").trim();

                    if (! registerSqlIndex.contains(indexName) && ! newSqlIndex.containsKey(indexName)) {
                        final boolean isName   = column.contains(".name");
                        String        validSql = "";

                        if (isName) {
                            validSql = "CREATE INDEX " + column.replace(".", "_") + "_sys_idx ON " + column.replace(".", " USING gin  ( UPPER(") + ") gin_trgm_ops)";
                        } else {
                            validSql = "CREATE INDEX " + column.replace(".", "_") + "_sys_idx ON " + column.replace(".", " (" + (isName ? "UPPER(" : "")) + " " + ((column.contains("created_at") || column.contains("updated_at")) ? " DESC NULLS LAST " : "") + (isName ? ")" : "") + " )";
                        }

                        newSqlIndex.put(indexName, validSql);
                        MyLogger.db("REG_SQL_CREATE_INDEX KEY = " + indexName + ", VALUE = " + validSql);
                    }
                }
            }
        }
    }

    private static String getWhereTables(String sql) {
        return sql.substring(sql.toLowerCase().indexOf(" where ") + 6).trim();
    }

    private static String getJoinTables(String sql) {
        return sql.substring(sql.toLowerCase().indexOf(" join ") + 5).trim();
    }
}
