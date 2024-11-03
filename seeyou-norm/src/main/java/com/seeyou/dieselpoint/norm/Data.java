package com.seeyou.dieselpoint.norm;

import com.seeyou.dieselpoint.norm.entities.AppMenu;
import com.seeyou.dieselpoint.norm.sqlmakers.StandardPojoInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class Data {
    public static ConcurrentHashMap<Class, StandardPojoInfo> pojoMap = new ConcurrentHashMap<Class, StandardPojoInfo>();
    public static final ReentrantLock menuPathLock = new ReentrantLock();
    public static final Map<String, AppMenu> menuAndPathByAppIdRoleIdCcId = new EhCacheMapAdapter<String, AppMenu>();
}
