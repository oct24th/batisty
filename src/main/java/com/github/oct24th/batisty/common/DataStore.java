package com.github.oct24th.batisty.common;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class DataStore extends LinkedHashMap<String, Object> {

    public final HashMap<String, Integer> counter = new HashMap<>();

    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        DataContainer container = (DataContainer) value;
        return container.getValue();
    }

    @Override
    public Object put(String key, Object value) {
        int cnt = counter.getOrDefault(key, -1) + 1;
        counter.put(key, cnt);
        return super.put(key+cnt, value);
    }

    public DataContainer getContainer(String key){
        return (DataContainer) super.get(key);
    }
}
