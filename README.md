# Batisty 
Batisty(Mybatis + Entity)는 Springframework 환경에서 Mybatis 사용시 단순 CRUD SQL을 매번 XML혹은 메소드로 작성하는 것을 JPA처럼 코드로 처리하기 위한 라이브러리이다.

단순한 형태의 CRUD만을 고려하고있기 때문에 Join이나 서브쿼리가 필요한 경우, insert into select 등 변형된 형태의 sql은 Mybatis의 원래 기능을 그대로 이용해야한다. 

Batisty에의해 생성되는 SQL은 해당 CRUD 코드가 최초 실행될때 Mybatis의 MappedStatement로 저장된다.

MappedStatement에 저장된 SQL은 Mybatis의 기본기능인 XML 혹은 method annotaion을 통해 등록된 SQL과 동일하게 Mybatis에 의해 관리, 실행 된다.

### 주요 기능
1. 단순 SQL을  java code 기반으로 자동 생성 및 마이바티스에 등록
2. java 코드 기반 Procedure, Function 호출문 자동 생성 및 마이바티스에 등록
3. insert, update에 대한 auto audit
4. insert 시 키 생성 (SelectKey or UseGeneratedKeys) 사용 가능 
5. DB 레벨 페이징

   페이지번호를 처리하는 파라미터의 이름으로 currentPage, rowCountPerPage 가 사용된다.

   변경을 원할경우 application.properties 설정파일에 다음과 같이 설정하여 변경 할수 있다.

   batisty.param.currentPage=pageNo

   batisty.param.rowCountPerPage=pageSize
6. returnType="map"인 경우 에도 mapUnderscoreToCamelCase 설정 적용
7. returnType="map"인 경우 resultType 처리에대한 확장 포인트 제공

### 제약사항
1. Springframework 6.0이상, JDK17이상
2. Mybatis 3.4.6 이상
3. sqlSessionTemplate이 복수인경우(복수의 DB를 사용하는경우) 고려되지 않음

### 변경이력
#### v4.0.0 
1. returnType="map"일때 key에 대한 mapUnderscoreToCamelCase 설정 적용
2. returnType="map"인 경우 resultType 처리에대한 확장 포인트 추가

- snake_case ↔ camelCase 변환의 일관성 유지를 위해 org.springframework:spring-jdbc에 포함된 JdbcUtils를 사용하면서
springframework 및 jdk 버전이 springframework 6.0이상 jdk이상으로 변경됨
#### v3.0.1
1. INSERT 시 SelectKey, UseGeneratedKeys 기능 추가
2. 직접 작성한 SELECT 쿼리에 대한 페이징 기능 추가
#### v2.0.0
최초배포

### 기본 설정
Springboot 어플리케이션의 시작지점이 되는 Main 클래스의 @SpringBootApplication에 scanBasePackages 설정 필요

해당 설정의 디폴트는 Main 클래스가 위치한 패키지이기 때문에 **io.github.oct24th.batisty**를 추가해주어야 Batisty에서 사용하는 Bean을 스캔한다.

org.mybatis.spring.boot:mybatis-spring-boot-starter 사용시 확장 포인트에대한 구현체를 @Component를 이용해 스프링 Bean으로 등록해주면 되고

수동으로 설정시에는 sqlSessionFactoryBean설정시 PreparedStatementInterceptor를 플러그인으로 등록해주고 CustomMapWrapperFactory를 ObjectWrapperFactory로 등록해주어야한다. 

(org.mybatis.spring.boot:mybatis-spring-boot-starter사용 권장)
```
@SpringBootApplication(scanBasePackages = {"com.example.demo", "io.github.oct24th.batisty"})  //기본패키지에 io.github.oct24th.batisty 추가
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

```
### 주요 확장 포인트
1. [io.github.oct24th.batisty.sql.DefaultNamingConverter](src/main/java/io/github/oct24th/batisty/sql/BatistyNamingConverter.java)

   Java Class에서 DB 테이블에 사용할 테이블, 컬럼 이름을 생성하는 룰을 구현하는 인터페이스.

   확장 Bean을 구현하지 않을경우 없을 경우 디폴트로
   [DefaultNamingConverter.java](src/main/java/io/github/oct24th/batisty/sql/impl/DefaultNamingConverter.java)
   가 적용 된다.


2. [io.github.oct24th.batisty.sql.AbstractAutoAudit](src/main/java/io/github/oct24th/batisty/sql/AbstractAutoAudit.java)

   구현체를 Bean으로 등록하면 Batisty를 이용한 Insert, Update시 Audit 컬럼이 SQL에 자동 포함.
   Entity클래스에 AutoAudit 어노테이션으로(데이터 생성 유저아이디, 아이피, 시간, 최종수정 유저 아이디, 아이피, 시간 등) 명시
   
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

3. [io.github.oct24th.batisty.paging.RowBoundsSqlWrapper](src/main/java/io/github/oct24th/batisty/paging/RowBoundsSqlWrapper.java)

   별도의 구현체가 등록되지 않으면 오라클 12c 이상, SqlServer, H2 DB등에서 사용가능한 
   [BasicRowBoundsSqlWrapper.java](src/main/java/io/github/oct24th/batisty/paging/impl/BasicRowBoundsSqlWrapper.java)
   가 사용된다.
   DB종류나 버전에 따라 페이징을 처리하는 쿼리가 달라지는경우 직접 구현해서 Bean으로 등록해서 사용. 


4. [io.github.oct24th.batisty.result.MapValueTypeHandler](src/main/java/io/github/oct24th/batisty/result/MapValueTypeHandler.java)

   Mybatis의 기본 result type 처리에대한 확장포인트인 BaseTypeHandler<T>는 JdbcType과 JavaType의 매칭 기반으로 동작하기 때문에
   resultType="map"으로 지정되면 value의 javaType이 특정되지 않아 BaseTypeHandler가 동작하지 않는다.

   이경우 MapValueTypeHandler를 등록하여 결과 타입을 변환 할 수있다.

   예) [ClobToStringMapValueTypeHandler.java](src/main/java/io/github/oct24th/batisty/result/impl/ClobToStringMapValueTypeHandler.java)

### Entity 클래스 작성 방법
Entity 클래스는 POJO로 구현한다.

단, 각 필드는 항상 private으로 선언하고 Getter와 Setter를 구현해야한다. (내부적으로 CGLIB PROXY를 통해 setter를 wrappingg 한다.)

AutoAudit 어노테이션은 필요시 사용하고 복수의 엔티티에서 공통적으로 사용하는 컬럼이 있다면 상속을 이용할 수 있다.

UseGeneratedKeys 혹은 SelectKey 어노테이션을 이용하여 마이바티스의 Key 관련 기능 사용가능 ( v3.0.1 이상 )

(UseGeneratedKeys 와 SelectKey는 둘중 하나만 사용해야하며 둘다 사용할 경우 UseGeneratedKeys는 무시됨 )
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

### BatistyDAO 사용 예시
Spring의 Service에서 BatistyDAO를 DI받아서 사용한다.
```
//데이터 페이징(DB)
PagingResult<MenuDto> result = batistyDAO.selectPage(menuDao::selectMenuList, param);
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
   //Entity에 SelectKey 혹은 UseGeneratedKey가 있으면 저장된후 pk 존재
   System.out.println(z.getId()); 
}


//delete
int d = batistyDAO.delete(TbOwnType.class, t -> {
    t.equal().setOwnTypeCd("a");
    t.like().setOwnTypeCd("b%"); //like 조건 
    t.notLike().setOwnTypeCd("%c"); //like 조건 + 동일컬럼에 복수의 조건 
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
