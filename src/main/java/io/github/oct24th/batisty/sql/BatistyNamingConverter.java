package io.github.oct24th.batisty.sql;

import io.github.oct24th.batisty.annotation.Param;
import io.github.oct24th.batisty.common.Parameter;
import io.github.oct24th.batisty.util.Utils;

import java.lang.reflect.Field;

public interface BatistyNamingConverter {

    /**
     * 바인딩 표현식 ( #{...} )<br>
     * Mybatis의 설정을 통해 표현식을 변경한 경우 override 해야한다.
     * @param bindName 바인딩 변수명
     * @return 바인딩 표현식
     */
    default String getBindingMarkup(String bindName) {
        return "#{"+bindName+"}";
    }

    /**
     * 바인딩 표현식 ( #{..., jdbcType=...} )<br>
     * insert, update시 오라클의 clob, mysql의 LONGTEXT 등 특수한 경우는 jdbctype을 명시해줘야한다.
     * Mybatis의 설정을 통해 표현식을 변경한 경우 override 해야한다.
     * @param bindName 바인딩 변수명
     * @param field 바인딩 필드
     * @return 바인딩 표현식
     */
    default String getBindingMarkup(String bindName, Field field) {
        StringBuilder sb = new StringBuilder("#{");
        sb.append(bindName);

        if(field.isAnnotationPresent(Param.class)) {
            Param param = field.getAnnotation(Param.class);
            if(param.jdbcType() != null) sb.append(", jdbcType=").append(param.jdbcType());
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 바인딩 표현식 프로시저 혹은 DB함수 용( #{..., mode=..., jdbcType=...} )<br>
     * Mybatis의 설정을 통해 표현식을 변경한 경우 override 해야한다.
     * @param parameter 바인딩 파라미터 정보
     * @return 바인딩 표현식
     */
    default String getBindingMarkup(Parameter parameter) {
        StringBuilder sb = new StringBuilder("#{");
        sb.append(parameter.getName());
        if(parameter.getMode() != null) sb.append(", mode=").append(parameter.getMode());
        if(parameter.getJdbcType() != null) sb.append(", jdbcType=").append(parameter.getJdbcType());
        if(parameter.getJavaType() != null) sb.append(", resultMap=").append(Utils.resultMapId(parameter.getJavaType()));
        sb.append("}");
        return sb.toString();
    }

    /**
     * 대상 타입을 이용해서 테이블명 생성
     * @param type 대상 타입 클래스
     * @param <T> 대상타입
     * @return SQL에 표기되는 그대로의 테이블명
     */
    <T> String getTableName(Class<T> type);

    /**
     * 대상 타입을 이용해서 procedure 혹은 function 이름 생성
     * @param type 대상 타입 클래스
     * @param <T> 대상타입(Procedure 혹은 Function)
     * @return SQL에 표기되는 그대로의 오브젝트명
     */
    <T> String getExecutableName(Class<T> type);

    /**
     * 대상 필드를 이용해서 컬럼명 생성
     * @param field 대상 필드
     * @return SQL에 표기되는 그대로의 컬럼명
     */
    String getColumnName(Field field);
}
