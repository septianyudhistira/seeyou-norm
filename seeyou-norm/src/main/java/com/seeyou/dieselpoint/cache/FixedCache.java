package com.seeyou.dieselpoint.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class FixedCache {
    public static ReentrantLock fixedLock = new ReentrantLock();
    public static Ehcache data = null;

    public static void putCache(final String key, final Object value) {
        try {
            fixedLock.lock();

            final Element e = new Element(key, value);
            data.put(e);
        } catch (Exception e) {
        } finally {
            fixedLock.unlock();
        }
    }

    public static void remove(final String key) {
        try {
            fixedLock.lock();

            data.remove(key);
        } catch (Exception e) {
        } finally {
            fixedLock.unlock();
        }
    }

    public static Object get(final String key) {
        Object returnValue = null;

        try {
            fixedLock.lock();

            final Element element = data.get(key);
            returnValue = element.getObjectValue();
        } catch (Exception e){
        } finally {
            fixedLock.unlock();
        }

        return returnValue;
    }
}
