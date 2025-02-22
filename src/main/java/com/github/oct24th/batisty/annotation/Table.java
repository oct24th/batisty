package com.github.oct24th.batisty.annotation;

import java.lang.annotation.*;

/**
 * BatistyDAO를 이용한 SQL 문장 생성 시<br>
 * 테이블 name을 설정 한다.<br>
 * Table 어노테이션이 없으면 BatistyNamingConverter의 룰에 따라 테이블명이 결정된다.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Table {
    /**
     * @return 테이블 이름
     */
    String value() default "";
}
