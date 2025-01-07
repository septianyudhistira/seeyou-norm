package com.seeyou.dieselpoint.norm;

import com.seeyou.dieselpoint.cache.FixedCache;
import com.seeyou.dieselpoint.cache.ImageCache;
import com.seeyou.logging.MyLogger;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.statistics.StatisticsGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class CacheUtil {
    public static ReentrantLock cache1Lock        = new ReentrantLock();
    public static CacheManager cacheMgr          = null;
    public static Ehcache publicKeyCache    = null;
    public static boolean       isCacheProduction;

    //
    private static Ehcache databaseCache = null;
    public static String   cacheName     = "seeyou";

    public static Ehcache getDatabaseCache() {
        if (cacheMgr == null) {
            // We could use an environment or a VM variable
            cacheMgr = CacheManager.newInstance();

            final String suffixCacheName = CacheUtil.isCacheProduction ? "prod" : "";

            databaseCache       = cacheMgr.getEhcache(CacheUtil.cacheName + "cache" + suffixCacheName);
            publicKeyCache      = cacheMgr.getEhcache(CacheUtil.cacheName + "publickey" + suffixCacheName);
            FixedCache.data     = cacheMgr.getEhcache(CacheUtil.cacheName + "fixed" + suffixCacheName);
            ImageCache.data     = cacheMgr.getEhcache(CacheUtil.cacheName + "image" + suffixCacheName);

            MyLogger.info("CACHE NAME : " + CacheUtil.cacheName);
            MyLogger.info("INIT CACHE " + cacheMgr);
            MyLogger.info("INIT DATABASE CACHE " + databaseCache);
            MyLogger.info("INIT PUBLIC KEY CACHE " + publicKeyCache);
            MyLogger.info("INIT FIXED CACHE " + FixedCache.data);
            MyLogger.info("INIT IMAGE CACHE " + ImageCache.data);
        }

        return databaseCache;
    }

    public Object readWithRetry(String key) {
        int i = 0;

        while (true) {
            final Ehcache cache   = getDatabaseCache();
            final Element element = cache.get(key);
            final Object  object  = element.getObjectValue();

            if (object == null) {
                try {
                    Thread.sleep(1000);
                    i++;
                } catch (Exception e) {
                    MyLogger.error(e.getMessage(), e);
                }
            } else {
                return cache.get(key);
            }

            if (i > 3) {
                MyLogger.error("Retry Data null more than 3 times");

                return null;
            }
        }
    }

    public static void putCache(final String key, final Object value) {
        boolean isLock = false;

        try {
            cache1Lock.lock();
            isLock = true;

            final Element e     = new Element(key, value);
            final Ehcache cache = getDatabaseCache();

            cache.put(e);

            MyLogger.trace("CACHE_SQL " + cache.getSize() + " " + key.replace("\n", "").replace("\r", ""));
        } catch (Exception e) {
        } finally {
            if (isLock) {
                cache1Lock.unlock();
            }
        }
    }

    public static void remove(final String key) {
        try {
            cache1Lock.lock();

            final Ehcache cache = getDatabaseCache();
            cache.remove(key);
        } catch (Exception e) {
        } finally {
            cache1Lock.unlock();
        }
    }

    public static Object get(final String key) {
        Object     returnValue = null;
        final long start       = System.currentTimeMillis();

        try {
            cache1Lock.lock();

            final Ehcache cache   = getDatabaseCache();
            final Element element = cache.get(key);
            returnValue           = element.getObjectValue();
        } catch (Exception e) {
        } finally {
            cache1Lock.unlock();
        }

        final long end = System.currentTimeMillis() - start;

        if (end > 100) {
            MyLogger.dbError("SLOW_QUERY : " + (end) + "ms) : [" + key + "]");
        }

        return returnValue;
    }

    public static String getStatistic() {
        /* get stats for all known caches */
        final StatisticsGateway stats = databaseCache.getStatistics();

        return String.format("HEAP SIZE %s, OFF HEAP SIZE %s, KEY SIZE %s, SIZE %s", stats.getLocalHeapSize(), stats.getLocalOffHeapSize(), stats.getLocalDiskSize(), databaseCache.getKeys().size());
    }

    public static void removeChache(final String sqlParam1) {
        final String sqlParam = sqlParam1.replaceAll("  ", " ").trim();

        String       tableName   = "";
        boolean      isInsertSql = false;

        if (sqlParam.startsWith("UPDATE")) {
            tableName = sqlParam.split("SET")[0].replace("UPDATE ", "").trim();
        } else if (sqlParam.startsWith("INSERT INTO")) {
            isInsertSql     = true;
            tableName       = sqlParam.replace("INSERT INTO", "").split(" ")[1].trim();
        } else if (sqlParam.startsWith("DELETE FROM")) {
            tableName = sqlParam.replace("DELETE FROM", "").split(" ")[1].trim();
        }

        if (tableName.trim().isEmpty()) {
            MyLogger.dbError("SQL_NOTIFY : table name NULL for SQL :" + sqlParam.replaceAll("\r", "").replaceAll("\n", ""));

            return;
        }

        final String  finalTableName = tableName;
        final boolean finalInsertSql = isInsertSql;
        Runnable      runnable       =
                new Runnable() {
                    public void run() {
                        try {
                            String paramColumns = "";

                            if (finalInsertSql) {
                                // SQL QUERY INSERT
                                final Map<String, String> parseSqlInsert = SqlInsertToMap.parseSqlInsert(sqlParam);
                                paramColumns = parseSqlInsert.containsKey("application_id") ? parseSqlInsert.get("application_id") : "";
                            } else {
                                // SQL QUERY UPDATE DELETE
                                final String key = "application_id";

                                if (sqlParam1.contains(" " + key) || sqlParam1.contains("." + key)) {
                                    paramColumns = ParsingSql.getValueSql(sqlParam, key);
                                }

                            }

                            MyLogger.db("SQL_NOTIFY : table name = " + finalTableName + ", column = " + paramColumns);

                            //
                            // destroy menu path by application id
                            if ((paramColumns != null) && (finalTableName.equals("module_fe_privilege") || finalTableName.equals("roles") || finalTableName.equals("module_fe") || finalTableName.equals("module_menu_fe_privilege") || finalTableName.equals("module_menu_fe") || finalTableName.equals("dashboard_privileges") || finalTableName.equals("dashboard"))) {
                                String appId = paramColumns.replaceAll("application_id=", "").replace("'", "");

                                if ((appId == null) || appId.isEmpty()) {
                                    return;
                                }

                                if (appId.contains(",")) {
                                    appId = (appId.split(",")[0]).trim();
                                }

                                try {
                                    Integer.valueOf(appId);
                                } catch (NumberFormatException e) {
                                    MyLogger.dbError("PARSING SQL APP ID ID ERROR :" + paramColumns, e);

                                    return;
                                }

                                if (sqlParam.contains("role_id")) {
                                    final int    indexOfRoleId   = sqlParam.indexOf("role_id");
                                    final String normalSqlRoleId = sqlParam.substring(indexOfRoleId).replaceAll(" =", "=").replaceAll("= ", "=").split(",")[0];
                                    final String roleId          = normalSqlRoleId.replace("role_id=", "").replace("'", "");

                                    if ((roleId == null) || roleId.isEmpty()) {
                                        return;
                                    }

                                    try {
                                        Integer.valueOf(roleId);
                                    } catch (Exception e) {
                                        MyLogger.dbError("PARSING SQL ROLE ID ID ERROR :" + roleId, e);

                                        return;
                                    }

                                    final String productAppId = new Database().getValue("CACHE SELECT product_application_id FROM applications WHERE applications.id = '" + appId + "'");

                                    final String keyApp = productAppId + appId + roleId;

                                    if (Data.menuAndPathByAppIdRoleIdCcId.containsKey(keyApp)) {
                                        try {
                                            Data.menuPathLock.lock();
                                            Data.menuAndPathByAppIdRoleIdCcId.remove(keyApp);
                                            MyLogger.debug("destroy menu and path for : " + keyApp);
                                        } catch (Exception e) {
                                        } finally {
                                            Data.menuPathLock.unlock();
                                        }
                                    }
                                } else {
                                    final ArrayList<String> keys         = new ArrayList<String>();
                                    final String            productAppId = new Database().getValue("CACHE SELECT product_application_id FROM applications WHERE applications.id = '" + appId + "'");
                                    final String            keyApp       = productAppId + appId;

                                    for (String key : Data.menuAndPathByAppIdRoleIdCcId.keySet()) {
                                        if (key.startsWith(keyApp)) {
                                            keys.add(key);
                                        }
                                    }

                                    if (! keys.isEmpty()) {
                                        try {
                                            Data.menuPathLock.lock();

                                            for (String key : keys) {
                                                Data.menuAndPathByAppIdRoleIdCcId.remove(key);
                                                MyLogger.debug("destroy menu and path for : " + key);
                                            }
                                        } catch (Exception e) {
                                        } finally {
                                            Data.menuPathLock.unlock();
                                        }
                                    }
                                }
                            }

                            final List<String> keys       = CacheUtil.getDatabaseCache().getKeys();
                            final List<String> removeSqls = new ArrayList<String>();

                            for (String sqlCache : keys) {
                                if ((paramColumns == null) || paramColumns.isEmpty()) {
                                    continue;
                                }

                                final String sqlOrign = sqlCache;
                                sqlCache = sqlCache.replaceAll("\r", "").replaceAll("\n", "").replaceAll("  ", " ").replaceAll(" =", "=").replaceAll("= ", "=");

                                if ((paramColumns != null) && sqlCache.contains(finalTableName) && sqlCache.contains(paramColumns)) {
                                    removeSqls.add(sqlOrign);
                                }

                                if ((paramColumns == null) && sqlCache.contains(finalTableName)) {
                                    removeSqls.add(sqlOrign);
                                }
                            }

                            if (! removeSqls.isEmpty()) {
                                for (String sql1 : removeSqls) {
                                    MyLogger.trace("CLEAR_CACHE_SQL : " + sql1.replace("\r", "").replace("\n", ""));
                                    CacheUtil.remove(sql1);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                };

        new Thread(runnable).start();
    }
}
