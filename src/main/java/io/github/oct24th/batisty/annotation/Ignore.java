package io.github.oct24th.batisty.annotation;

import java.lang.annotation.*;

/**
 * BatistyDAO를 이용한 SQL 문장 생성 시<br>
 * 무시 할 필드<br>
 * Ignore로 설정되면 값을 할당해도 SQL에 포함되지 않는다.<br>
 * 단, select 결과에는 아무런 영향을 주지 않는다.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Ignore {}
