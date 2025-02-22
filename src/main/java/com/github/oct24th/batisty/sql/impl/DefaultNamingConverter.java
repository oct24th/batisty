package com.github.oct24th.batisty.sql.impl;

import com.github.oct24th.batisty.sql.BatistyNamingConverter;

import java.lang.reflect.Field;

/**
 * 디폴트 BatistyNamingConverter
 */
public class DefaultNamingConverter implements BatistyNamingConverter {

    @Override
    public <T> String getTableName(Class<T> type) {
        return camelToSnake(type.getSimpleName(), String::toLowerCase);
    }

    @Override
    public <T> String getExecutableName(Class<T> type) {
        return camelToSnake(type.getSimpleName(), String::toLowerCase);
    }

    @Override
    public String getColumnName(Field field) {
        return camelToSnake(field.getName(), String::toLowerCase);
    }
}
