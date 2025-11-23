package io.github.oct24th.batisty.proxy;

import io.github.oct24th.batisty.enums.SqlCommandKind;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.NoOp;

import java.util.ArrayList;
import java.util.List;

/**
 * BatistyDAO를 이용해서 CRUD 수행시 대상 객체의 cglib proxy 클래스를 생성
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BatistyProxyFactory {

    public static <T> T createProxyEntity(SqlCommandKind sqlCommandKind, Class<T> type){

        List<Class<?>> interfaceList = new ArrayList<>();
        interfaceList.add(BasicEntityProxy.class);
        switch (sqlCommandKind){
            case UPDATE: interfaceList.add(UpdateEntityProxy.class);
            case SELECT:
            case DELETE: interfaceList.add(WhereCauseProxy.class);
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setInterfaces(interfaceList.toArray(new Class<?>[0]));
        enhancer.setCallbacks(new Callback[] { NoOp.INSTANCE, new ProxyMethodInterceptor(sqlCommandKind.getDataStoreCount()) });

        enhancer.setCallbackFilter(method -> {
            String methodName = method.getName();
            switch (methodName) {
                case "set":
                case "where":
                case "getDataStores":
                case "createStatementId":
                case "equal":
                case "not":
                case "like":
                case "notLike":
                case "lt":
                case "lte":
                case "gt":
                case "gte": return 1;
                default:
                    if(methodName.startsWith("set")) return 1;
                    else return 0;
            }
        });

        return (T) enhancer.create();
    }
}
