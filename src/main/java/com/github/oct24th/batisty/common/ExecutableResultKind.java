package com.github.oct24th.batisty.common;

/**
 * 프로시저, 함수의 리턴에 따른 구분<br>
 * SqlSessionTemplate 사용시 메소드가 달라지기 때문에 객체 생성시 구분해둔다.
 */
public enum ExecutableResultKind {
    NONE,
    ONE,
    LIST,
}
