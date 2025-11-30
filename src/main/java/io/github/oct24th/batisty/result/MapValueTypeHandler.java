package io.github.oct24th.batisty.result;

import org.apache.ibatis.reflection.property.PropertyTokenizer;

public interface MapValueTypeHandler {

    boolean isSupport(PropertyTokenizer prop, Object value);

    Object convert(Object value);
}
