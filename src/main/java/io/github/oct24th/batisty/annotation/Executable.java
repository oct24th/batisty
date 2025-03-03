package io.github.oct24th.batisty.annotation;

import java.lang.annotation.*;

/**
 * BatistyDAO를 이용한 SQL 문장 생성 시<br>
 * db의 procedure 혹은 function name을 설정 한다.<br>
 * Executable 어노테이션이 없으면 BatistyNamingConverter의 룰에 따라 테이블명이 결정된다.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Executable {
    /**
     * @return 오브젝트 이름
     */
    String value() default "";
}
