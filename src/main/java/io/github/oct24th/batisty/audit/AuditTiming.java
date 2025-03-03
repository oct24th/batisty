package io.github.oct24th.batisty.audit;

/**
 * Auto Audit 시점
 */
public enum AuditTiming {
    /** SQL 생성 시점 */ SQL,
    /** SQL 실행 시점 */ EXECUTE,
}
