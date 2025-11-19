package io.github.oct24th.batisty;

import io.github.oct24th.batisty.audit.AbstractAutoAudit;
import io.github.oct24th.batisty.common.Executable;
import io.github.oct24th.batisty.common.ExecutableResultKind;
import io.github.oct24th.batisty.common.Function;
import io.github.oct24th.batisty.common.Procedure;
import io.github.oct24th.batisty.paging.EnhancedRowBounds;
import io.github.oct24th.batisty.paging.PagingResult;
import io.github.oct24th.batisty.paging.SerializableFunction;
import io.github.oct24th.batisty.proxy.*;
import io.github.oct24th.batisty.sql.SqlCommandKind;
import io.github.oct24th.batisty.sql.SqlProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    @Value("${batisty.param.currentPage:currentPage}")
    private String CURRENT_PAGE;

    @Value("${batisty.param.rowCountPerPage:rowCountPerPage}")
    private String ROW_COUNT_PER_PAGE;

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
     *    String r2 = batistyDAO.execute(t);
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
     *    String r2 = batistyDAO.execute(new ExampleProcedure(), t -&gt; {
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
     *    String r2 = batistyDAO.execute(t);
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
     *    String r2 = batistyDAO.execute(new ExampleFunction(), t -&gt; {
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


    /**
     * <pre>
     * ex) PagingResult&lt;MenuDto&gt; result = batistyDAO.getPage(menuDao::selectMenuList, param);
     * List&lt;MenuDto&gt; data = result.getData();
     * int totalCount = result.getTotalCount();
     * int rowOffset = result.getRowOffset();
     * int lastPageNo = result.getLastPageNo();
     * </pre>
     * @param func mybatis mapper dao의 select 쿼리에 해당하는 메소드 람다식
     * @param param 쿼리에 사용할 파라미터
     * @return PagingResult
     * @param <T> 쿼리결과를 처리하는 result type (xml에 지정하는 resultType과 동일)
     */
    public <T> PagingResult<T> getPage(SerializableFunction<Object, List<T>> func, Object param) {

        EnhancedRowBounds rowBounds = this.getRowBounds(param);

        try {
            Method writeReplace = func.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object form = writeReplace.invoke(func);

            if(form instanceof SerializedLambda lambda) {
                String nameSpace = lambda.getImplClass().replace("/", ".");
                String statementId = nameSpace + "." + lambda.getImplMethodName();

                List<T> list = sqlSessionTemplate.selectList(statementId, param, rowBounds);

                return PagingResult.<T>builder()
                        .data(list)
                        .totalCount(rowBounds.getTotalCount())
                        .rowOffset(rowBounds.getRowOffset())
                        .lastPageNo(rowBounds.getLastPageNo())
                        .build();
            }

            return PagingResult.<T>builder().data(new ArrayList<>()).build();

        }catch (Exception e) {
            log.debug(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private EnhancedRowBounds getRowBounds(Object param) {

        int currentPage,rowCountPerPage;

        if(param instanceof Map<?, ?> map){
            currentPage = map.containsKey(CURRENT_PAGE) ? (int) map.get(CURRENT_PAGE) : 1;
            rowCountPerPage = map.containsKey(ROW_COUNT_PER_PAGE) ? (int) map.get(ROW_COUNT_PER_PAGE) : 100;
        }else{
            currentPage = (int) this.readObjectProperty(param, CURRENT_PAGE, 1);
            rowCountPerPage = (int) this.readObjectProperty(param, ROW_COUNT_PER_PAGE, 100);
        }

        return new EnhancedRowBounds((currentPage - 1) * rowCountPerPage, rowCountPerPage);
    }

    private Object readObjectProperty(Object obj, String propertyName, Object defaultValue){
        try {
            Class<?> clazz = obj.getClass();

            Method getter = clazz.isRecord() ? clazz.getMethod(propertyName)
                    : new PropertyDescriptor(propertyName, clazz).getReadMethod();

            if(getter != null) return getter.invoke(obj);

            Field field;

            try {
                field = clazz.getField(propertyName);
            }catch (NoSuchFieldException e) {
                field = clazz.getDeclaredField(propertyName);
                field.setAccessible(true);
            }

            return field.get(obj);

        } catch (Exception e) {
            log.debug(e.getMessage());
            return defaultValue;
        }
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
