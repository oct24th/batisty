package com.github.oct24th.batisty.sql.impl;

import com.github.oct24th.batisty.audit.AbstractAutoAudit;
import com.github.oct24th.batisty.common.DataContainer;
import com.github.oct24th.batisty.common.DataStore;
import com.github.oct24th.batisty.proxy.BasicEntityProxy;
import com.github.oct24th.batisty.sql.BatistyNamingConverter;
import com.github.oct24th.batisty.annotation.AutoAudit;
import com.github.oct24th.batisty.audit.AuditTiming;
import com.github.oct24th.batisty.sql.BatistyNamingConverterFactory;
import com.github.oct24th.batisty.sql.SqlCommandKind;
import com.github.oct24th.batisty.sql.SqlProvider;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * SqlProvider for insert
 */
@Component
@RequiredArgsConstructor
public class InsertProvider implements SqlProvider {

    private final AbstractAutoAudit[] autoAudits;
    private final BatistyNamingConverterFactory converterFactory;

    @Override
    public SqlCommandKind getCommandType() {
        return SqlCommandKind.INSERT;
    }

    @Override
    public String build(Object target) {
        BatistyNamingConverter converter = converterFactory.get();
        Class<?> type = target.getClass().getSuperclass();
        return new SQL(){
            {
                INSERT_INTO(converter.getTableName(type));
                DataStore ds = ((BasicEntityProxy) target).getDataStores().get(0);

                Set<Field> excludeAutoAudit = new HashSet<>();

                ds.keySet().forEach(key -> {
                    DataContainer dc = ds.getContainer(key);

                    Field field = dc.getField();
                    if(field.isAnnotationPresent(AutoAudit.class)) excludeAutoAudit.add(field);
                    VALUES(converter.getColumnName(field), converter.getBindingMarkup(key));
                });

                Arrays.stream(autoAudits)
                        .filter(autoAudit -> autoAudit.isSupport(SqlCommandKind.INSERT))
                        .findFirst()
                        .ifPresent(autoAudit ->
                            autoAudit.getAllField(type).forEach(field -> {
                                if(excludeAutoAudit.contains(field)) return;

                                AutoAudit annotation = field.getAnnotation(AutoAudit.class);
                                if(annotation == null || annotation.value() != AuditTiming.SQL) return;

                                String columnName = converter.getColumnName(field);
                                Object value = autoAudit.getAuditValue(columnName.replace("\"", ""));
                                if(value != autoAudit.INVALIDITY) VALUES(columnName, (String) value);
                            })
                        );
            }
        }.toString();
    }
}
