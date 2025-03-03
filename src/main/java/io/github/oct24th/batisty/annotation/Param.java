package io.github.oct24th.batisty.annotation;

import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.*;

/**
 * Procedure, Function의 파라미터 정보<br>
 * <strong>주의. ParameterMode를 OUT으로 지정하면 JdbcType을 반드시 지정해야한다.</strong>
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Param {
    /**
     * @return JDBC타입
     */
    JdbcType jdbcType() default JdbcType.NULL;

    /**
     * @return IN(default) / OUT / INOUT
     */
    ParameterMode mode() default ParameterMode.IN;
}
