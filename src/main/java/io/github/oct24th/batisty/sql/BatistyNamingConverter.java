package io.github.oct24th.batisty.sql;

import io.github.oct24th.batisty.common.Parameter;

import java.lang.reflect.Field;
import java.util.function.Function;

public interface BatistyNamingConverter {

    /**
     * Camel Case -&gt; Snake Case 변환
     * @param camel 카멜케이스 문자열
     * @param casing Snake Case에 대한 후처리 함수. null을 주면 대소문자 변경없이 각 단어앞에 _만 추가된체로 리턴된다.
     * @return 스네이크케이스 문자열
     */
     default String camelToSnake(String camel, Function<String, String> casing) {
         StringBuilder sb = new StringBuilder();
         //파라미터의 길이가 너무 길지 않으면 정규식으로 하는것보다 속도도 빠르고 오류도 없다..
         for (int i = 0; i < camel.length(); i++) {
             char ch = camel.charAt(i);
             if(i != 0 && Character.isUpperCase(ch)) sb.append('_');
             sb.append(ch);
         }
         if(casing == null) casing = str -> str;
         return casing.apply(sb.toString());
    }


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
