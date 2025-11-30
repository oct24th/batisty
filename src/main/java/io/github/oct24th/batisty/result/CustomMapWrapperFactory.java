package io.github.oct24th.batisty.result;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomMapWrapperFactory implements ObjectWrapperFactory {

    private final MapValueTypeHandler[] handlers;

    @Override
    public boolean hasWrapperFor(Object object) {
        return object instanceof Map;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        return new CustomMapWrapper(metaObject, (Map<String, Object>)object, handlers);
    }

    static class CustomMapWrapper extends MapWrapper {

        private final MapValueTypeHandler[] handlers;

        public CustomMapWrapper(MetaObject metaObject, Map<String, Object> map, MapValueTypeHandler[] handlers) {
            super(metaObject, map);
            this.handlers = handlers;
        }

        @Override
        public String findProperty(String name, boolean useCamelCaseMapping) {
            if(!useCamelCaseMapping || name == null || name.isEmpty()) return name;
            return JdbcUtils.convertUnderscoreNameToPropertyName(name);
        }

        @Override
        public void set(PropertyTokenizer prop, Object value) {
            for (MapValueTypeHandler handler : handlers) {
                if(handler.isSupport(prop, value)) {
                    super.set(prop, handler.convert(value));
                    return;
                }
            }
            super.set(prop, value);
        }
    }
}
