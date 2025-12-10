package io.github.oct24th.batisty.sql.impl;

import io.github.oct24th.batisty.annotation.Ignore;
import io.github.oct24th.batisty.proxy.BasicEntityProxy;
import io.github.oct24th.batisty.sql.BatistyNamingConverter;
import io.github.oct24th.batisty.common.DataContainer;
import io.github.oct24th.batisty.common.DataStore;
import io.github.oct24th.batisty.sql.BatistyNamingConverterFactory;
import io.github.oct24th.batisty.enums.SqlCommandKind;
import io.github.oct24th.batisty.sql.SqlProvider;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;

/**
 * SqlProvider for select
 */
@Component
@RequiredArgsConstructor
public class SelectProvider implements SqlProvider {

    private final BatistyNamingConverterFactory converterFactory;

    @Override
    public SqlCommandKind getCommandType() {
        return SqlCommandKind.SELECT;
    }

    @Override
    public String build(Object target) {
        BatistyNamingConverter converter = converterFactory.get();
        return new SQL(){
            {
                SELECT("*");
                FROM(converter.getTableName(target.getClass().getSuperclass()));
                DataStore ds = ((BasicEntityProxy) target).getDataStores().get(0);
                ds.keySet().forEach(key -> {
                    DataContainer dc = ds.getContainer(key);
                    if(!dc.getField().isAnnotationPresent(Ignore.class)) {
                        WHERE(converter.getColumnName(dc.getField()) + " "+dc.getOperator()+" " + converter.getBindingMarkup(key));
                    }
                });
            }
        }.toString();
    }
}
