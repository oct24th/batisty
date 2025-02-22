package com.github.oct24th.batisty.annotation;

import com.github.oct24th.batisty.audit.AuditTiming;

import java.lang.annotation.*;

/**
 * BatistyDAO를 이용해서 Insert, Update 수행 시<br>
 * Audit 자동 완성 대상 Column
 */
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AutoAudit {
    /**
     * 자동 완성 시점<br>
     * <strong>EXECUTE</strong>: sql이 실행되는 시점에 데이터 바인딩(디폴트)<br>
     * <strong>SQL</strong>: sql이 생성 시점에 정적으로 픽스(데이터의 입력, 수정일시를 DB 함수를 이용해 설정하기 위해 사용)<br>
     * @return AuditTiming
     */
    AuditTiming value() default AuditTiming.EXECUTE;
}
