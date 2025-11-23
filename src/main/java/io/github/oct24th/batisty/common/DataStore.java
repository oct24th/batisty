package io.github.oct24th.batisty.common;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class DataStore extends LinkedHashMap<String, Object> {

    public final HashMap<String, Integer> counter = new HashMap<>();

    @Override
    public Object get(Object key) {
        Object value = super.get(key);

        if(!(value instanceof DataContainer)) return value;
        return ((DataContainer) value).getValue();
    }

    @Override
    public Object put(String key, Object value) {

        if(!(value instanceof DataContainer)) return super.put(key, value);

        int cnt = counter.getOrDefault(key, -1) + 1;
        counter.put(key, cnt);
        return super.put(key+cnt, value);
    }

    public DataContainer getContainer(String key){
        Object value = super.get(key);
        if(!(value instanceof DataContainer)) return null;
        return (DataContainer) value;
    }
}
