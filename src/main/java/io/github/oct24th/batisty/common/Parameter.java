package io.github.oct24th.batisty.common;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.type.JdbcType;

/**
 * Executable의 파라미터 스펙
 */
@Getter
@Builder
@RequiredArgsConstructor
public class Parameter {
    private final String name;
    private final Class<?> javaType;
    private final ParameterMode mode;
    private final JdbcType jdbcType;
}
