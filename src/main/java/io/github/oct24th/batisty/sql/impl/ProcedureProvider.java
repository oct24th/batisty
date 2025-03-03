package io.github.oct24th.batisty.sql.impl;

import io.github.oct24th.batisty.common.Executable;
import io.github.oct24th.batisty.sql.BatistyNamingConverter;
import io.github.oct24th.batisty.sql.BatistyNamingConverterFactory;
import io.github.oct24th.batisty.sql.SqlCommandKind;
import io.github.oct24th.batisty.sql.SqlProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SqlProvider for procedure
 */
@Component
@RequiredArgsConstructor
public class ProcedureProvider implements SqlProvider {

    private final BatistyNamingConverterFactory converterFactory;

    @Override
    public SqlCommandKind getCommandType() {
        return SqlCommandKind.PROCEDURE;
    }

    @Override
    public String build(Object target) {
        BatistyNamingConverter converter = converterFactory.get();
        Class<?> type = target.getClass();
        StringBuilder sb = new StringBuilder(" CALL ");
        sb.append(converter.getExecutableName(type));
        sb.append(" ( ");
        Executable.executableMetaData.get(type.getName()).forEach(p -> sb.append(converter.getBindingMarkup(p)).append(", "));
        sb.delete(sb.length()-2, sb.length()-1); //마지막 ", "제거
        sb.append(" ) ");
        return sb.toString();
    }
}
