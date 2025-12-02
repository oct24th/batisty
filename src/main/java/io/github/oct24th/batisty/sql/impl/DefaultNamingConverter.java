package io.github.oct24th.batisty.sql.impl;

import io.github.oct24th.batisty.sql.BatistyNamingConverter;
import org.springframework.jdbc.support.JdbcUtils;

import java.lang.reflect.Field;

/**
 * 디폴트 BatistyNamingConverter
 */
public class DefaultNamingConverter implements BatistyNamingConverter {

    @Override
    public <T> String getTableName(Class<T> type) {
        return JdbcUtils.convertPropertyNameToUnderscoreName(type.getSimpleName());
    }

    @Override
    public <T> String getExecutableName(Class<T> type) {
        return JdbcUtils.convertPropertyNameToUnderscoreName(type.getSimpleName());
    }

    @Override
    public String getColumnName(Field field) {
        return JdbcUtils.convertPropertyNameToUnderscoreName(field.getName());
    }
}
