# Batisty 
Batisty(Mybatis + Entity)는 Springframework 환경에서 Mybatis 사용시 단순 CRUD SQL을 매번 XML혹은 메소드로 작성하는 것을 JPA처럼 코드로 처리하기 위한 라이브러리이다.

단순한 형태의 CRUD만을 고려하고있기 때문에 Join이나 서브쿼리가 필요한 경우, insert into select 등 변형된 형태의 sql은 Mybatis의 원래 기능을 그대로 이용해야한다. 

Batisty에의해 생성되는 SQL은 해당 CRUD 코드가 최초 실행될때 Mybatis의 MappedStatement로 저장된다.

MappedStatement에 저장된 SQL은 Mybatis의 기본기능인 XML 혹은 method annotaion을 통해 등록된 SQL과 동일하게 Mybatis에 의해 관리, 실행 된다.

 - 단순 CRUD 쿼리 java entity 기반으로 자동 생성 및 마이바티스에 등록
 - insert 시 키 생성 전략 (SelectKey or UseGeneratedKeys)
 - DB 단위의 페이징 

========================================================================

## 제약사항
Mybatis 3.4.6 버전에서 개발되었으며 3.2이하에서는 프로시저 호출시 오류가 발생할 것으로 예상된다.

현재 내부에서 sqlSessionTemplate을 DI받아 사용하기 때문에 복수의 DB로 구성된 시스템은 고려하지 않고 구현되었다.

## 사용예제
### Batisty Bean 스캔
Springboot 어플리케이션의 시작지점이 되는 Main 클래스의 @SpringBootApplication에 scanBasePackages 설정 필요

해당 설정의 디폴트는 Main 클래스가 위치한 패키지이기 때문에 **io.github.oct24th.batisty**를 추가해주어야 Batisty에서 사용하는 Bean을 스캔한다.
```
package com.example.demo;

@SpringBootApplication(scanBasePackages = {"com.example.demo", "io.github.oct24th.batisty"})  //기본패키지에 io.github.oct24th.batisty 추가
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### Entity 클래스 작성 예제
Entity 클래스는 POJO로 구현한다. (Table 어노테이션도 필요에 따라 사용하고 없어도 무방하다.)

단, 각 필드는 항상 private으로 선언하고 Getter와 Setter를 구현해야한다. (내부적으로 CGLIB PROXY를 통해 setter를 wrappingg 한다.)

AutoAudit 어노테이션은 필요시 사용하고 복수의 엔티티에서 공통적으로 사용하는 컬럼이 있다면 상속을 이용할 수 있다.

UseGeneratedKeys 혹은 SelectKey 어노테이션을 이용하여 마이바티스의 Key 관련 기능 사용가능 ( 3.0.0 이상 )

(UseGeneratedKeys 와 SelectKey는 둘중 하나만 사용해야하면 둘다 사용할 경우 UseGeneratedKeys는 무시됨 )

```
@Getter  //Getter필수
@Setter  //Setter필수
@ToString(callSuper = true)
@Table("public.TB_CATEGORY")  //@Table은 사용하거나 사용하지 않아도 무방, 지정된 이름은 쿼리문장에 그대로 적용된다.
public class TbCategory extends AuditBase {  //AuditBase 상속은 audit컬럼을 모든 엔티티에 공통 적용하기 위함으로 없어도 무방 

    @UseGeneratedKeys   //MySql, Postgres 등 auto increment 를 지원하는 DB에서 DB가 생성한 키를 돌려받음
    private Id;
    
    @SelectKey(statement="select some_sequence.nexval from dual") //오라클 Sequence 등 insert 문 실행 전 또는 실행 후 특정 sql을 실행하여 생성한 키 혹은 생성된 키를 돌려받음
    private Id;    

    private String categoryCd;

    private String categoryNm;

    @Ignore  //insert 혹은 update시 setCategorySeq(..) 를 호출해도 SQL에는 미포함
    private String categorySeq;
    
    @AutoAudit(AuditTiming.SQL)        // SQL생성 시점의 audit 필드로 지정. AbstractAutoAudit를 구현하면 자동완성.
    private LocalDateTime lastUpdated; // insert 혹은 update시 setLastUpdated(..)를 호출하면 설정한 값이 사용되고 아니면 자동. 

    @AutoAudit                    // SQL실행 시점의 audit 필드로 지정. AbstractAutoAudit를 상속받아 구현하면 자동완성.
    private Long lastUpdatedBy;   // insert 혹은 update시 setLastUpdatedBy(..)를 호출하면 설정한 값이 사용되고 아니면 자동 완성.
}

//일부 Audit컬럼을 복수의 Entity에서 공통으로 사용하기 위한 부모클래스로 필요시에 구현
@Getter
@Setter
@ToString
public class AuditBase {

    @AutoAudit(AuditTiming.SQL)
    private LocalDateTime created;

    @AutoAudit
    private Long createdBy;
}

//Function 예제
@Getter
@Setter
@Executable("public.EXAMPLE_FUNCTION")  //오라클이라면 패키지를 사용하는경우, 다른 스키마나 DB링크를 사용하는 경우등에 지정 지정된 문자열 그대로 쿼리문장에 적용된다.
public class ExampleFunction extends Function<List<TbCategory>>{
    private Integer param1;
    private String param2;
}

//Procedure 예제
@Getter
@Setter
@Executable("public.EXAMPLE_PROCEDURE") //오라클이라면 패키지를 사용하는경우, 다른 스키마나 DB링크를 사용하는 경우등에 지정 지정된 문자열 그대로 쿼리문장에 적용된다.
public class ExampleProcedure extends Procedure<Void> {  //DB종류에 따라 Procedure가 리턴값을 가지면 VOID대신 Function처럼 사용
    private Integer param1;

    @Param(mode = ParameterMode.OUT, jdbcType = JdbcType.VARCHAR)  //out 변수
    private String param2;
}
```

### BatistyNamingConverter 구현 및 Bean 등록
Java Class에서 DB 테이블에 사용할 테이블, 컬럼 이름을 생성하는 룰을 구현하는 인터페이스.

등록된 Bean이 없을 경우 DefaultNamingConverter가 적용.

DefaultNamingConverter는 클래스이름, 필드이름을 UPPER CASE SNAKE CASE로 변환.
```
@Component
public class DefaultNamingConverter implements BatistyNamingConverter {

    @Override
    public <T> String getTableName(Class<T> type) {
        return Utils.camelToSnake(type.getSimpleName(), String::toLowerCase);
    }

    @Override
    public <T> String getExecutableName(Class<T> type) {
        return Utils.camelToSnake(type.getSimpleName(), String::toLowerCase);
    }

    @Override
    public String getColumnName(Field field) {
        return Utils.camelToSnake(field.getName(), String::toLowerCase);
    }
}
```
### AbstractAutoAudit 구현 및 Bean 등록
Entity클래스에 AutoAudit 어노테이션으로 Audit 컬럼 명시 가능. 

(예: 데이터 생성 유저아이디, 아이피, 시간, 최종수정 유저 아이디, 아이피, 시간 등)

AbstractAutoAudit를 상속받아 구현된 Bean을 등록하면 Batisty를 이용한 Insert, Update시 Audit 컬럼이 SQL에 자동 포함.
```
@Component
public class InsertAudit extends AbstractAutoAudit {

    /**
     * AbstractAutoAudit내부에서 BatistyNamingConverterFactory를 사용하므로 
     * AbstractAutoAudit의 구현체 개발시 생성자 항상 필요 (BatistyNamingConverterFactory Bean DI)
     */
    @Autowired
    public InsertAudit(BatistyNamingConverterFactory converterFactory) {
        super(converterFactory);
    }
    
    /**
     * 작성된 구현체가 지원하는 SqlCommandKind로 아래와 같이 구현시 INSERT 문에서만 동작
     */
    @Override
    public boolean isSupport(SqlCommandKind sqlCommandKind) {
        return sqlCommandKind == SqlCommandKind.INSERT;
    }

    @Override
    public Object getAuditValue(String columnName) {
        //Spring에서 제공하는 RequestContextHolder를 이용해서 
        //HttpRequest나 Session에 접근해서 로그인 정보 및 접속 아이피 등을 가져와서 사용 
        
        switch (columnName){
            case "CREATED_BY":
            case "LAST_UPDATED_BY": return 1234L; //session에서 찾은 로그인 유저 아이디, 아이피 등
            case "CREATED":
            case "LAST_UPDATED": return "NOW()"; //DB종류에 따라 SYSDATE, NOW() 등
            default: return INVALIDITY;
        }
    }
}
```
### 페이징 설정
1. Mybatis plugin: PreparedStatementInterceptor

    springboot 자동설정을 사용하지 않고 별도의 설정을 가져가는 경우 mybatis에 를 plugin으로 등록하는 설정이 필요하다
    
    (만약 자동설정을 사용할 경우 plugin은 자동으로 등록된다.)


2. RowBoundsSqlWrapper 인터페이스

    별도의 구현체가 빈으로 등록되지 않으면 BasicRowBoundsSqlWrapper 가 사용된다.

    표준 페이징 쿼리 : originalSql + " OFFSET "  + offset + " ROWS FETCH NEXT "+ limit +" ROWS ONLY"

    (오라클 12c, SqlServer, H2 DB등에서 BasicRowBoundsSqlWrapper를 그대로 사용가능)

    만약 DB의 종류나 버전이 다를경우 페이징을위해 wrapping 하는 쿼리가 달라지므로 RowBoundsSqlWrapper 인터페이스를 implement하여
    
    String getTotalCountSql(String originalSql);
    
    String getPagingSql(String originalSql, int offset, int limit)
    
    두개의 메소드를 구현하고 @Component @Primary로 bean으로 등록 해주면 BasicRowBoundsSqlWrapper 대신 해당 bean이 사용된다.


3. 페이지번호 파라미터명 설정

    페이지번호를 처리하는 파라미터의 이름으로 currentPage, rowCountPerPage 가 사용된다.
    
    만약 변경을 원할경우 application.properties 설정파일에 다음과 같이 설정하여 변경 할수 있다. 

    batisty.param.currentPage=pageNo

    batisty.param.rowCountPerPage=pageSize

```
// plugin설정: xml 방식
 <plugins>
    <plugin interceptor="io.github.oct24th.batisty.proxy.PreparedStatementInterceptor"/>
 </plugins>
 
// plugin설정: @Configuration 방식
@Configuration
public class MyBatisConfig {
    ...
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, PreparedStatementInterceptor plugin) throws Exception {
            SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
            ...
            factoryBean.setPlugins({ plugin });
            ...
            return factoryBean.getObject();
    }
    ...
} 
```

### BatistyDAO 사용
Spring의 Service에서 BatistyDAO를 DI받아서 사용한다.
```
//데이터 페이징(DB)
PagingResult<MenuDto> result = batistyDAO.getPage(menuDao::selectMenuList, param);
List<MenuDto> data = result.getData();
int totalCount = result.getTotalCount();
int rowOffset = result.getRowOffset();
int lastPageNo = result.getLastPageNo();

//insert 
TbCategory z = batistyDAO.insert(TbCategory.class, t -> {
    t.setCategoryCd("a");
    t.setCategoryNm("테스트");
});

if(z != null) {
   //insert 시에 Id를 설정하지 않았지만 
   //Entity에 SelectKey 혹은 UseGeneratedKey가 잇으며 저장된후 pk 존재
   System.out.println(z.getId()); 
}


//delete
int d = batistyDAO.delete(TbOwnType.class, t -> {
    t.equal().setOwnTypeCd("a");
    t.like().setOwnTypeCd("b%"); //like 조건 + 동일컬럼에 복수의 조건
});

//update
int f = batistyDAO.update(TbOwnType.class, p -> {
    p.set( t -> {
        t.setOwnTypeCd("v");
        t.setOwnTypeNm("uuuu");
    });
    p.where( t -> t.equal().setOwnTypeCd("b"));
});

//count
long e = batistyDAO.count(TbOwnType.class, t -> t.equal().setOwnTypeCd("v"));

//selectOne
TbOwnType e = batistyDAO.selectOne(TbOwnType.class, t -> t.equal().setOwnTypeCd("v"));

//selectList
List<TbOwnType> c = batistyDAO.selectList(TbOwnType.class, null);

//function call
String r2 = commonDAO.execute(new ExampleFunction(), t -> {
    t.setParam1(12);
    t.setParam2("tttt");
});
System.out.println(r2);

//procedure call
ExampleProcedure r1 = new ExampleProcedure();
r1.setParam1(12345);

commonDAO.execute(r1);

System.out.println(r1.getParam2());
```
## 참고
[https://spring.io/projects/spring-framework](https://spring.io/projects/spring-framework)

[https://mybatis.org/mybatis-3/](https://mybatis.org/mybatis-3/)
