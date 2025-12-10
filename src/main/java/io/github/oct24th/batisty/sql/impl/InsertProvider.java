package io.github.oct24th.batisty.sql.impl;

import io.github.oct24th.batisty.annotation.AutoAudit;
import io.github.oct24th.batisty.annotation.Ignore;
import io.github.oct24th.batisty.annotation.SelectKey;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SqlProvider for insert
 */
@Slf4j
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

                List<Field> allFields = Utils.getAllFields(type);
                Set<Field> excludeAutoAudit = new HashSet<>();
                Set<Field> excludeKeyField = new HashSet<>();

                ds.keySet().forEach(key -> {
                    DataContainer dc = ds.getContainer(key);

                    Field field = dc.getField();
                    if(field.isAnnotationPresent(AutoAudit.class)) excludeAutoAudit.add(field);
                    if(field.isAnnotationPresent(SelectKey.class)) excludeKeyField.add(field);
                    if(!field.isAnnotationPresent(Ignore.class)) {
                        VALUES(converter.getColumnName(field), converter.getBindingMarkup(key, field));
                    }
                });

                //키필드 포함시키기
                allFields.forEach(field -> {
                    if(excludeKeyField.contains(field)) return;
                    SelectKey annotation = field.getAnnotation(SelectKey.class);
                    if(annotation == null || !annotation.before()) return;
                    String columnName = converter.getColumnName(field);
                    VALUES(columnName, converter.getBindingMarkup(field.getName()));
                });

                //auto audit
                Arrays.stream(autoAudits)
                        .filter(autoAudit -> autoAudit.isSupport(SqlCommandKind.INSERT))
                        .findFirst()
                        .ifPresent(autoAudit ->
                            allFields.forEach(field -> {
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
