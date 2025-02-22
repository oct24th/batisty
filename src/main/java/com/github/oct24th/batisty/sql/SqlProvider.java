package com.github.oct24th.batisty.sql;

/**
 * SqlCommandType에 따라 SQL을 생성하는 Provider 인터페이스
 */
public interface SqlProvider {

    /**
     * @return 대상 SqlCommandKind
     */
    SqlCommandKind getCommandType();

    /**
     * @param target 대상 객체
     * @return 생성된 SQL
     */
    String build(Object target);
}
