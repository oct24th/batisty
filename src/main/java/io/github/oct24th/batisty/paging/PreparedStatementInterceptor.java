package io.github.oct24th.batisty.paging;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
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
public class PreparedStatementInterceptor implements Interceptor {

    protected final Logger logger = LoggerFactory.getLogger(PreparedStatementInterceptor.class);

    private final RowBoundsSqlWrapper rowBoundsSqlWrapper;

    public PreparedStatementInterceptor(RowBoundsSqlWrapper rowBoundsSqlWrapper) {
        logger.debug("Bind rowBoundsSqlWrapper : {}", rowBoundsSqlWrapper);
        this.rowBoundsSqlWrapper = rowBoundsSqlWrapper;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        RowBounds rb = (RowBounds) metaObject.getValue("delegate.rowBounds");

        if(!(rb instanceof EnhancedRowBounds)) return invocation.proceed();

        String sql = ((String) metaObject.getValue("delegate.boundSql.sql")).trim();

        //이 커넥션은 토탈카운트 조회후에 mybatis가 페이징쿼리를 실행한 후 풀에 반납할것
        Connection conn = (Connection) invocation.getArgs()[0];

        //토탈카운트
        try(PreparedStatement stmt = conn.prepareStatement(rowBoundsSqlWrapper.getTotalCountSql(sql))){
            ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("delegate.parameterHandler");
            parameterHandler.setParameters(stmt);

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) ((EnhancedRowBounds) rb).setTotalCount(rs.getInt(1));
            }
        }

        //변경된 쿼리로 바꿔치기
        metaObject.setValue("delegate.boundSql.sql", rowBoundsSqlWrapper.getPagingSql(sql, rb.getOffset(), rb.getLimit()));

        // RowBounds 정보 제거(그냥두면 mybatis의 원래 페이징 기능이 다시 동작하면서 OFFSET 부터 LIMIT 까지만 패치하기 때문에 제거필수)
        metaObject.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
        metaObject.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {}
}
