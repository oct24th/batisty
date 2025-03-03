package io.github.oct24th.batisty.sql.impl;

import io.github.oct24th.batisty.common.Executable;
import io.github.oct24th.batisty.sql.BatistyNamingConverter;
import io.github.oct24th.batisty.sql.BatistyNamingConverterFactory;
import io.github.oct24th.batisty.sql.SqlCommandKind;
import io.github.oct24th.batisty.sql.SqlProvider;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * SqlProvider for function
 */
@Component
public class FunctionProvider implements SqlProvider {

    private final BatistyNamingConverter converter;
    protected String FUNCTION_CALL_SUBFIX = "";

    @Autowired
    public FunctionProvider(BatistyNamingConverterFactory converterFactory, SqlSessionTemplate sqlSessionTemplate) {
        this.converter = converterFactory.get();
        SqlSessionFactory sqlSessionFactory = sqlSessionTemplate.getSqlSessionFactory();
        try (SqlSession session = sqlSessionFactory.openSession()){
            Connection connection = session.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseName = metaData.getDatabaseProductName();
            if (databaseName.equalsIgnoreCase("oracle")) FUNCTION_CALL_SUBFIX = "FROM DUAL";
        } catch (SQLException ignore) {}
    }

    @Override
    public SqlCommandKind getCommandType() {
        return SqlCommandKind.FUNCTION;
    }

    @Override
    public String build(Object target) {
        Class<?> type = target.getClass();
        StringBuilder sb = new StringBuilder(" SELECT ");
        sb.append(converter.getExecutableName(type));
        sb.append(" ( ");
        Executable.executableMetaData.get(type.getName()).forEach(p -> sb.append(converter.getBindingMarkup(p)).append(", "));
        sb.delete(sb.length()-2, sb.length()-1); //마지막 ", "제거
        sb.append(" ) ");
        sb.append(FUNCTION_CALL_SUBFIX);
        return sb.toString();
    }
}
