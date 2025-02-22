package com.github.oct24th.batisty.proxy;

import com.github.oct24th.batisty.annotation.Ignore;
import com.github.oct24th.batisty.common.DataContainer;
import com.github.oct24th.batisty.common.DataStore;
import com.github.oct24th.batisty.sql.SqlCommandKind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * BatistyProxyFactory로 생성된 cglib proxy의 MethodInterceptor
 */
@Slf4j
public class ProxyMethodInterceptor implements MethodInterceptor {

    private int storeIdx = 0;
    private String operator = "";
    private final List<DataStore> dataStores = new ArrayList<>();

    public ProxyMethodInterceptor(int dataStoreCount){
        for (int i = 0; i < dataStoreCount; i++) {
            dataStores.add(new DataStore());
        }
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        String methodName = method.getName();
        switch (methodName){
            case  "set":
                storeIdx = 0;
                ((Consumer)objects[0]).accept(obj);
                return null;
            case  "where":
                storeIdx = 1;
                ((Consumer)objects[0]).accept(obj);
                storeIdx = 0;
                return null;
            case "getDataStores":
                return dataStores;
            case "createStatementId":
                StringBuilder sb = new StringBuilder();
                sb.append(((SqlCommandKind) objects[0]).name().charAt(0));
                sb.append("_");
                sb.append(obj.getClass().getSuperclass().getSimpleName());
                dataStores.forEach(ds ->
                    ds.keySet().forEach(key -> {
                        DataContainer dc = ds.getContainer(key);
                        sb.append("&").append(dc.getOperator()).append(key);
                    })
                );
                return sb.toString();
            case "equal":
                operator = "=";
                return obj;
            case "not":
                operator = "<>";
                return obj;
            case "like":
                operator = "LIKE";
                return obj;
            case "notLike":
                operator = "NOT LIKE";
                return obj;
            case "lt":
                operator = "<";
                return obj;
            case "lte":
                operator = "<=";
                return obj;
            case "gt":
                operator = ">";
                return obj;
            case "gte":
                operator = ">=";
                return obj;
            default:
                String propertyName = StringUtils.uncapitalize(methodName.replaceFirst("set", ""));
                Field field = this.findField(obj.getClass(), propertyName);
                if(!field.isAnnotationPresent(Ignore.class)){
                    DataContainer dc = new DataContainer(field, operator, objects[0]);
                    DataStore ds = dataStores.get(storeIdx);
                    ds.put(storeIdx + propertyName, dc);
                    operator = "";
                }
                return methodProxy.invokeSuper(obj, objects);
        }
    }

    public <T> Field findField(Class<T> type, String fieldName){
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> parent = type.getSuperclass();
            if(parent == Object.class) throw new RuntimeException(e);
            else return findField(parent, fieldName);
        }
    }
}
