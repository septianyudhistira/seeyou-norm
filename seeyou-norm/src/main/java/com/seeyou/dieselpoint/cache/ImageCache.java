package com.seeyou.dieselpoint.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import static com.seeyou.dieselpoint.cache.FixedCache.fixedLock;


import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class ImageCache {
    public static ReentrantLock imagelock = new ReentrantLock();
    public static Ehcache data = null;

    public static void putCache(final String key, final Object value) {
        boolean islock = false;

        try {
            fixedLock.lock();
            islock = true;

            final Element element = new Element(key, value);
            data.put(element);
        } catch (Exception e) {
        } finally {
            if (islock) {
                fixedLock.unlock();
            }
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
        }finally {
            fixedLock.unlock();
        }
        return returnValue;
    }
}
