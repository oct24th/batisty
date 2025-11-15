package io.github.oct24th.batisty.paging;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PrepareSqlInterceptor implements Interceptor {
    protected final Logger logger = LoggerFactory.getLogger(PrepareSqlInterceptor.class);

    private final RowBoundsSqlPacker rowBoundsSqlPacker;

    private final ObjectFactory DEFAULT_OBJECT_FACTORY                = new DefaultObjectFactory();
    private final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private final ReflectorFactory DEFAULT_REFLECTOR_FACTORY          = new DefaultReflectorFactory();

    public PrepareSqlInterceptor(RowBoundsSqlPacker rowBoundsSqlPacker) {
        this.rowBoundsSqlPacker = rowBoundsSqlPacker;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
        RowBounds rb = (RowBounds) metaStatementHandler.getValue("delegate.rowBounds");

        if(!(rb instanceof EnhancedRowBounds)) return invocation.proceed();

        String sql = ((String) metaStatementHandler.getValue("delegate.boundSql.sql")).trim();
        logger.debug("delegate.boundSql.sql: {}", sql);

        //이 커넥션은 토탈카운트 조회후에 mybatis가 페이징쿼리를 실행한 후 풀에 반납할것
        Connection conn = (Connection) invocation.getArgs()[0];

        //토탈카운트
        try(PreparedStatement stmt = conn.prepareStatement(rowBoundsSqlPacker.getTotalCountSql(sql))){
            ParameterHandler parameterHandler = (ParameterHandler) metaStatementHandler.getValue("delegate.parameterHandler");
            parameterHandler.setParameters(stmt);

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) ((EnhancedRowBounds) rb).setTotalCount(rs.getInt(1));
            }
        }

        //변경된 쿼리로 바꿔치기
        metaStatementHandler.setValue("delegate.boundSql.sql", rowBoundsSqlPacker.getPagingSql(sql, rb.getOffset(), rb.getLimit()));

        // RowBounds 정보 제거(그냥두면 mybatis의 원래 페이징 기능이 다시 동작하면서 OFFSET 부터 LIMIT 까지만 패치하기 때문에 제거필수)
        metaStatementHandler.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
        metaStatementHandler.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {}
}
