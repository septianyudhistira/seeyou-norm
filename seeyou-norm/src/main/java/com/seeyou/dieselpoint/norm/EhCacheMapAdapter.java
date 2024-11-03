package com.seeyou.dieselpoint.norm;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import java.util.*;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class EhCacheMapAdapter<K, V> implements Map<K, V> {
    public Ehcache ehCache;

    public EhCacheMapAdapter() {
        super();
        this.ehCache = CacheUtil.getDatabaseCache();
    }

    @Override
    public void clear() {
        ehCache.removeAll();
    }

    @Override
    public boolean containsKey(Object key) {
        return ehCache.isKeyInCache(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return ehCache.isValueInCache(value);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        if (key == null) {
            return null;
        }

        Element element = ehCache.get(key);

        if (element == null) {
            return null;
        }

        return (V) element.getObjectValue();
    }

    @Override
    public boolean isEmpty() {
        return ehCache.getSize() == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        List<K> l = ehCache.getKeys();

        return new HashSet(l);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        Element e = new Element(key, value);
        ehCache.put(e);

        return null;
    }

    @Override
    public V remove(Object key) {
        V retObj = null;

        if (this.containsKey(key)) {
            retObj = this.get(key);
        }

        ehCache.remove(key);

        return retObj;
    }

    @Override
    public int size() {
        return ehCache.getSize();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<?extends K, ?extends V> m) {
        for (K key : m.keySet()) {
            this.put(key, m.get(key));
        }
    }
}
