package io.github.oct24th.batisty;

import io.github.oct24th.batisty.audit.AbstractAutoAudit;
import io.github.oct24th.batisty.common.Executable;
import io.github.oct24th.batisty.common.ExecutableResultKind;
import io.github.oct24th.batisty.common.Function;
import io.github.oct24th.batisty.common.Procedure;
import io.github.oct24th.batisty.paging.EnhancedRowBounds;
import io.github.oct24th.batisty.paging.PagingResult;
import io.github.oct24th.batisty.sql.SqlCommandKind;
import io.github.oct24th.batisty.sql.SqlProvider;
import io.github.oct24th.batisty.proxy.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

/**
 * Batisty 공통 DAO<br>
 * Java객체기반으로 단순 CRUD 쿼리 및 PROCEDURE, FUNCTION을 실행하는 구문이 최초 호출될 때<br>
 * SQL을 생성하여 Mybatis의 MappedStatement로 등록하고 등록된 MappedStatement는 sqlSessionTemplate을 이용해서 실행한다.
 */
@Slf4j
@Component
public class BatistyDAO {

    private final Configuration myBatisConfig;
    private final AbstractAutoAudit[] autoAuditExecutors;
    private final SqlSessionTemplate sqlSessionTemplate;
    private final HashMap<SqlCommandKind, SqlProvider> providerStore = new HashMap<>();

    @Autowired
    private BatistyDAO(AbstractAutoAudit[] autoAuditExecutors, SqlSessionTemplate sqlSessionTemplate, SqlProvider[] builders) {
        this.autoAuditExecutors = autoAuditExecutors;
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.myBatisConfig = sqlSessionTemplate.getConfiguration();
        Arrays.stream(builders).forEach(builder -> providerStore.put(builder.getCommandType(), builder));
    }

    /**
     * <pre>
     * ex)
     *     long cnt = batistyDAO.count(TbOwnType.class, t -&gt; t.equal().setOwnTypeCd("v"));
     * </pre>
     * @param type 대상타입 클래스
     * @param consumer where조건에대한 consumer
     * @param <T> 대상타입
     * @return count 건수
     */
    @SuppressWarnings("unchecked")
    public <T> long count(Class<T> type, Consumer<WhereCauseProxy<T>> consumer) {
        BasicEntityProxy proxy = getBatistyProxy(SqlCommandKind.SELECT, type, (Consumer<T>) consumer);
        String statementId = this.getStatementId(SqlCommandKind.COUNT, proxy, type, Long.class);
        return sqlSessionTemplate.selectOne(statementId, proxy.getDataStores().get(0));
    }

    /**
     * <pre>
     * ex)
     *     TbOwnType e = batistyDAO.selectOne(TbOwnType.class, t -&gt; t.equal().setOwnTypeCd("v"));
     * </pre>
     * @param type 대상타입 클래스
     * @param consumer where조건에대한 consumer
     * @param <T> 대상타입
     * @return 조회결과
     */
    @SuppressWarnings("unchecked")
    public <T> T selectOne(Class<T> type, Consumer<WhereCauseProxy<T>> consumer) {
        BasicEntityProxy proxy = getBatistyProxy(SqlCommandKind.SELECT, type, (Consumer<T>) consumer);
        String statementId = this.getStatementId(SqlCommandKind.SELECT, proxy, type, type);
        return sqlSessionTemplate.selectOne(statementId, proxy.getDataStores().get(0));
    }

    /**
     * <pre>
     * ex)
     *     List&lt;TbOwnType&gt; e = batistyDAO.selectList(TbOwnType.class, t -&gt; t.like().setOwnTypeNm("가%"));
     * </pre>
     * @param type 대상타입 클래스
     * @param consumer where조건에대한 consumer
     * @param <T> 대상타입
     * @return 조회결과
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> selectList(Class<T> type, Consumer<WhereCauseProxy<T>> consumer) {
        BasicEntityProxy proxy = getBatistyProxy(SqlCommandKind.SELECT, type, (Consumer<T>) consumer);
        String statementId = this.getStatementId(SqlCommandKind.SELECT, proxy, type, type);
        return sqlSessionTemplate.selectList(statementId, proxy.getDataStores().get(0));
    }

    /**
     * <pre>
     * ex)
     *    int z = batistyDAO.insert(TbCategory.class, t -&gt; {
     *        t.setCategoryCd("a");
     *        t.setCategoryNm("테스트");
     *    });
     * </pre>
     * @param type 대상타입 클래스
     * @param consumer values 구문에 대한 consumer
     * @param <T> 대상타입
     * @return 저장된 데이터 건수
     */
    public <T> int insert(Class<T> type, Consumer<T> consumer) {
        BasicEntityProxy proxy = getBatistyProxy(SqlCommandKind.INSERT, type, consumer);
        String statementId = this.getStatementId(SqlCommandKind.INSERT, proxy, type, null);
        return sqlSessionTemplate.insert(statementId, proxy.getDataStores().get(0));
    }

    /**
     * <pre>
     * ex)
     *    int d = batistyDAO.delete(TbOwnType.class, t -&gt; {
     *        t.equal().setOwnTypeCd("a");
     *        t.like().setOwnTypeCd("b%"); //like 조건 + 동일컬럼에 복수의 조건
     *    });
     * </pre>
     * @param type 대상타입 클래스
     * @param consumer where조건에대한 consumer
     * @param <T> 대상타입
     * @return 삭제된 데이터 건수
     */
    @SuppressWarnings("unchecked")
    public <T> int delete(Class<T> type, Consumer<WhereCauseProxy<T>> consumer) {
        BasicEntityProxy proxy = getBatistyProxy(SqlCommandKind.DELETE, type, (Consumer<T>) consumer);
        String statementId = this.getStatementId(SqlCommandKind.DELETE, proxy, type, null);
        return sqlSessionTemplate.delete(statementId, proxy.getDataStores().get(0));
    }

    /**
     * <pre>
     * ex)
     *    int f = batistyDAO.update(TbOwnType.class, p -&gt; {
     *        p.set( t -&gt; {
     *            t.setOwnTypeCd("v");
     *            t.setOwnTypeNm("uuuu");
     *            t.setLastUpdatedBy(9876L);
     *        });
     *        p.where( t -&gt; t.equal().setOwnTypeCd("b"));
     *    });
     * </pre>
     * @param type 대상타입 클래스
     * @param consumer UpdateEntityProxy에 대한 consumer, set절과, where절을 설정한다.
     * @param <T> 대상타입
     * @return 변경된 데이터 건수
     */
    @SuppressWarnings("unchecked")
    public <T> int update(Class<T> type, Consumer<UpdateEntityProxy<T>> consumer) {
        BasicEntityProxy proxy = this.getBatistyProxy(SqlCommandKind.UPDATE, type, (Consumer<T>) consumer);
        String statementId = this.getStatementId(SqlCommandKind.UPDATE, proxy, type, null);
        Map<String, Object> param = proxy.getDataStores().get(0);
        param.putAll(proxy.getDataStores().get(1));
        return sqlSessionTemplate.update(statementId, param);
    }

    /**
     * <pre>
     * ex)
     *    ExampleProcedure t = new new ExampleProcedure();
     *    t.setParam1(12);
     *    t.setParam2("tttt");
     *
     *    String r2 = commonDAO.execute(t);
     *
     *    System.out.println(r2);
     *    System.out.println(t.getParam2()); //out변수
     * </pre>
     * @param procedure 프로시저 클래스 객체
     * @param <K> 프로시저를 상속받은 타입
     * @param <T> 리턴타입
     * @return 프로시저가 리턴하는 값 (ExampleProcedure의 리턴타입이 VOID인경우 null)
     * @see Procedure
     */
    public <K extends Procedure<T>, T> T  execute(K procedure) {
        return execute(SqlCommandKind.PROCEDURE, procedure);
    }

    /**
     * <pre>
     * ex)
     *    String r2 = commonDAO.execute(new ExampleProcedure(), t -&gt; {
     *        t.setParam1(12);
     *        t.setParam2("tttt");
     *    });
     *
     *    System.out.println(r2);
     * </pre>
     * @param procedure 프로시저 클래스 객체
     * @param consumer 첫번째 인자로 넘겨받은 프로시저 클래스 객체(procedure), 해당 객체에 값을 설정하는데 사용된다.
     * @param <K> 프로시저를 상속받은 타입
     * @param <T> 리턴타입
     * @return 프로시저가 리턴하는 값 (ExampleProcedure의 리턴타입이 VOID인경우 null)
     * @see Procedure
     */
    public <K extends Procedure<T>, T> T  execute(K procedure, Consumer<K> consumer) {
        consumer.accept(procedure);
        return execute(SqlCommandKind.PROCEDURE, procedure);
    }

    /**
     * <pre>
     * ex)
     *    ExampleFunction t = new new ExampleFunction();
     *    t.setParam1(12);
     *    t.setParam2("tttt");
     *
     *    String r2 = commonDAO.execute(t);
     *
     *    System.out.println(r2);
     * </pre>
     * @param function 함수 클래스 객체
     * @param <K> 함수를 상속받은 타입
     * @param <T> 리턴타입
     * @return 함수가 리턴하는 값
     * @see Function
     */
    public <K extends Function<T>, T> T execute(K function) {
        return execute(SqlCommandKind.FUNCTION, function);
    }


    /**
     * <pre>
     * ex)
     *    String r2 = commonDAO.execute(new ExampleFunction(), t -&gt; {
     *        t.setParam1(12);
     *        t.setParam2("tttt");
     *    });
     *    System.out.println(r2);
     * </pre>
     * @param function 함수 클래스 객체
     * @param consumer 첫번째 인자로 넘겨받은 함수 클래스 객체(function), 해당 객체에 값을 설정하는데 사용된다.
     * @param <K> 함수를 상속받은 타입
     * @param <T> 리턴타입
     * @return 함수가 리턴하는 값
     * @see Function
     */
    public <K extends Function<T>, T> T execute(K function, Consumer<K> consumer) {
        consumer.accept(function);
        return execute(SqlCommandKind.FUNCTION, function);
    }


    //TODO 런타임에 익명함수로 처리되는 func에서 mapper객체의 namespace와 sql id를 알아낼수없다.
    //java.util.function.Function 이 아닌 별도의 functional interface를 구현해서 해결했었는데 기어거이 잘 안난다 나중에 다시...
    public <T> PagingResult<T> getPage(java.util.function.Function<Object, List<T>> func, Object param, int pageNo, int pageSize) {

        int offset = (pageNo - 1) * pageSize;
        EnhancedRowBounds rowBounds = new EnhancedRowBounds(offset, pageSize);
        List<T> list = sqlSessionTemplate.selectList("", param, rowBounds);

        int totalCount = rowBounds.getTotalCount();
        int lastPageNo = (int) Math.ceil((double) totalCount / pageSize);

        return new PagingResult<>(totalCount, lastPageNo, list);
    }


    @SuppressWarnings("unchecked")
    private <K extends Executable<T>, T> T execute(SqlCommandKind sqlCommandKind, K executable) {
        String statementId = this.getStatementId(sqlCommandKind, executable, executable.getClass(), executable.getReturnType());

        switch (executable.getExecutableResultKind()) {
            case ONE: return sqlSessionTemplate.selectOne(statementId, executable);
            case LIST: return (T)sqlSessionTemplate.selectList(statementId, executable);
            case NONE: sqlSessionTemplate.update(statementId, executable);
        }
        return null;
    }

    private <T> BasicEntityProxy getBatistyProxy(SqlCommandKind sqlCommandKind, Class<T> type, Consumer<T> consumer) {
        T proxy = BatistyProxyFactory.createProxyEntity(sqlCommandKind, type);
        if (consumer != null) consumer.accept(proxy);

        Arrays.stream(autoAuditExecutors)
                .filter(autoAudit -> autoAudit.isSupport(sqlCommandKind))
                .findFirst()
                .ifPresent(autoAudit -> autoAudit.execute(proxy));

        return (BasicEntityProxy) proxy;
    }

    private String getStatementId(SqlCommandKind sqlCommandKind, StatementIdSupplier target, Class<?> parameterType, Class<?> returnType) {

        String statementId = target.createStatementId(sqlCommandKind);

        if (!myBatisConfig.hasStatement(statementId)) {
            String sql = providerStore.get(sqlCommandKind).build(target);
            if (log.isDebugEnabled()) log.debug("Registration new MappedStatement({}) : {}", statementId, sql);

            SqlCommandType sqlCommandType = sqlCommandKind.getSqlCommandType();
            if (sqlCommandType == SqlCommandType.UNKNOWN) {
                sqlCommandType = ((Executable<?>) target).getExecutableResultKind() == ExecutableResultKind.NONE ? SqlCommandType.UPDATE : SqlCommandType.SELECT;
            }

            SqlSource sqlSource = new SqlSourceBuilder(myBatisConfig).parse(sql, parameterType, null);
            MappedStatement.Builder statementBuilder = new MappedStatement.Builder(myBatisConfig, statementId, sqlSource, sqlCommandType);

            statementBuilder.statementType(sqlCommandKind.getStatementType());

            if (returnType != null && returnType != Void.class) {
                statementBuilder.resultMaps(Collections.singletonList(
                        new ResultMap.Builder(myBatisConfig, statementId, returnType, new ArrayList<>()).build()
                ));
            }
            myBatisConfig.addMappedStatement(statementBuilder.build());
        }

        return statementId;
    }
}
