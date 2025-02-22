package com.github.oct24th.batisty.sql.impl;

import com.github.oct24th.batisty.proxy.BasicEntityProxy;
import com.github.oct24th.batisty.sql.BatistyNamingConverter;
import com.github.oct24th.batisty.common.DataContainer;
import com.github.oct24th.batisty.common.DataStore;
import com.github.oct24th.batisty.sql.BatistyNamingConverterFactory;
import com.github.oct24th.batisty.sql.SqlCommandKind;
import com.github.oct24th.batisty.sql.SqlProvider;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;

/**
 * SqlProvider for delete
 */
@Component
@RequiredArgsConstructor
public class DeleteProvider implements SqlProvider {

    private final BatistyNamingConverterFactory converterFactory;

    @Override
    public SqlCommandKind getCommandType() {
        return SqlCommandKind.DELETE;
    }

    @Override
    public String build(Object target) {
        BatistyNamingConverter converter = converterFactory.get();
        return new SQL(){
            {
                DELETE_FROM(converter.getTableName(target.getClass().getSuperclass()));
                DataStore ds = ((BasicEntityProxy) target).getDataStores().get(0);
                ds.keySet().forEach(key -> {
                    DataContainer dc = ds.getContainer(key);
                    WHERE(converter.getColumnName(dc.getField()) + " "+dc.getOperator()+" " + converter.getBindingMarkup(key));
                });
            }
        }.toString();
    }
}
