package io.github.oct24th.batisty.sql.impl;

import io.github.oct24th.batisty.annotation.AutoAudit;
import io.github.oct24th.batisty.common.DataContainer;
import io.github.oct24th.batisty.common.DataStore;
import io.github.oct24th.batisty.enums.AuditTiming;
import io.github.oct24th.batisty.enums.SqlCommandKind;
import io.github.oct24th.batisty.proxy.BasicEntityProxy;
import io.github.oct24th.batisty.sql.AbstractAutoAudit;
import io.github.oct24th.batisty.sql.BatistyNamingConverter;
import io.github.oct24th.batisty.sql.BatistyNamingConverterFactory;
import io.github.oct24th.batisty.sql.SqlProvider;
import io.github.oct24th.batisty.util.Utils;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * SqlProvider for update
 */
@Component
@RequiredArgsConstructor
public class UpdateProvider implements SqlProvider {

    private final AbstractAutoAudit[] autoAudits;
    private final BatistyNamingConverterFactory converterFactory;

    @Override
    public SqlCommandKind getCommandType() {
        return SqlCommandKind.UPDATE;
    }

    @Override
    public String build(Object target) {
        BatistyNamingConverter converter = converterFactory.get();
        Class<?> type = target.getClass().getSuperclass();
        return new SQL(){
            {
                BasicEntityProxy proxy = (BasicEntityProxy) target;
                UPDATE(converter.getTableName(type));

                Set<Field> excludeAutoAudit = new HashSet<>();

                DataStore ds0 = proxy.getDataStores().get(0);
                ds0.keySet().forEach(key -> {
                    DataContainer dc = ds0.getContainer(key);
                    Field field = dc.getField();
                    if(field.isAnnotationPresent(AutoAudit.class)) excludeAutoAudit.add(field);
                    SET(converter.getColumnName(field) + " = " + converter.getBindingMarkup(key));
                });

                Arrays.stream(autoAudits)
                        .filter(autoAudit -> autoAudit.isSupport(SqlCommandKind.UPDATE))
                        .findFirst()
                        .ifPresent(autoAudit ->
                            Utils.getAllFields(type).forEach(field -> {
                                if(excludeAutoAudit.contains(field)) return;
                                AutoAudit annotation = field.getAnnotation(AutoAudit.class);
                                if(annotation == null || annotation.value() != AuditTiming.SQL) return;

                                String columnName = converter.getColumnName(field);
                                Object value = autoAudit.getAuditValue(columnName.replace("\"", ""));
                                if(value != autoAudit.INVALIDITY) SET(columnName + " = " + value);
                            })
                        );


                DataStore ds1 = proxy.getDataStores().get(1);
                ds1.keySet().forEach(key -> {
                    DataContainer dc = ds1.getContainer(key);
                    WHERE(converter.getColumnName(dc.getField()) + " "+dc.getOperator()+" " + converter.getBindingMarkup(key));
                });
            }
        }.toString();
    }
}
