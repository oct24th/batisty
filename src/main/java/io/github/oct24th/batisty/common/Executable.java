package io.github.oct24th.batisty.common;


import io.github.oct24th.batisty.annotation.Param;
import io.github.oct24th.batisty.enums.ExecutableResultKind;
import io.github.oct24th.batisty.proxy.StatementIdSupplier;
import io.github.oct24th.batisty.enums.SqlCommandKind;
import lombok.Getter;
import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.NotFoundException;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 프로시저 혹은 함수의 부모에 해당하는 추상클래스
 * @param <T> 프로시저 혹은 함수가 리턴하는 값을 받을 타입. 리턴이 없으면 VOID로 준다.
 */
@Getter
public abstract class Executable<T> implements StatementIdSupplier {

    public static Map<String, List<Parameter>> executableMetaData = new HashMap<>();

    private final Class<?> returnType;
    private final ExecutableResultKind executableResultKind;

    @SuppressWarnings("unchecked")
    protected Executable(){
        String className = this.getClass().getName();
        Type superClass = this.getClass().getGenericSuperclass();
        Type genericType = ((ParameterizedType) superClass).getActualTypeArguments()[0];

        if(genericType instanceof ParameterizedType){
            returnType = (Class<T>)((ParameterizedType) genericType).getRawType();
            executableResultKind = ExecutableResultKind.LIST;
        }else{
            returnType = (Class<T>) genericType;
            executableResultKind = returnType == Void.class ? ExecutableResultKind.NONE : ExecutableResultKind.ONE;
        }

        if(!executableMetaData.containsKey(className)) makeMetaData(className);
    }

    @Override
    public String createStatementId(SqlCommandKind sqlCommandKind) {
        return sqlCommandKind.name().charAt(0) + "_" + this.getClass().getSimpleName();
    }

    /**
     * javassist를 이용해서 프로시저 혹은 함수에 선언된 파라미터 정보를 생성한다.<br>
     * 단순히 reflection을 이용하면 JVM에 따라서 필드의 순서를 보장할 수 없다.<br>
     * DB의 프로시저, 함수는 파라미터를 순서대로 처리하기때문에 Bytecode 라이브러리인 javassist를 이용한다.<br>
     * Bytecode를 직접 읽음으로서 속도가 늦어질수 있기때문에 각 객체별로 한번씩만 동작하도록 executableMetaData에 캐시한다.
     * @param className 패키지를 포함한 클래스명
     */
    private void makeMetaData(String className) {
        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass clazz = classPool.get(className);
            executableMetaData.put(className, Arrays.stream(clazz.getDeclaredFields()).map(field -> {
                Param paramMetaInfo = null;
                try {
                    paramMetaInfo = (Param) field.getAnnotation(Param.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                ParameterMode mode = null;
                JdbcType jdbcType = null;
                if(paramMetaInfo != null) {
                    mode = paramMetaInfo.mode();
                    jdbcType = paramMetaInfo.jdbcType();
                    if(mode == ParameterMode.IN) mode = null;
                    if(jdbcType == JdbcType.NULL) jdbcType = null;
                }
                return new Parameter(field.getName(), mode, jdbcType);
            }).collect(Collectors.toList()));
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }
}
