package io.github.oct24th.batisty.audit;

import io.github.oct24th.batisty.annotation.AutoAudit;
import io.github.oct24th.batisty.sql.BatistyNamingConverterFactory;
import io.github.oct24th.batisty.sql.SqlCommandKind;
import lombok.extern.slf4j.Slf4j;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Audit 자동 완성을 처리하는 추상 클래스<br>
 * 각 프로젝트에 맞도록 AbstractAutoAudit를 구현하여 bean으로 등록하면<br>
 * BatistyDAO를 이용해서 Insert, Update, Procedure 실행 시 Audit정보가 자동으로 추가된다.<br>
 * 구현 예제 )<br>
 * <pre>
 * // @Component
 * // public class InsertAudit extends AbstractAutoAudit {
 * //
 * //     @Autowired
 * //     public InsertAudit(BatistyNamingConverterFactory converterFactory) {
 * //         super(converterFactory);
 * //     }
 * //
 * //     @Override
 * //     public boolean isSupport(SqlCommandKind sqlCommandKind) {
 * //         return sqlCommandKind == SqlCommandKind.INSERT;
 * //     }
 * //
 * //     @Override
 * //     public Object getAuditValue(String columnName) {
 * //         switch (columnName){
 * //             case "CREATED_BY":
 * //             case "LAST_UPDATED_BY": return "...";
 * //             case "CREATED":
 * //             case "LAST_UPDATED": return "SYSDATE";
 * //             default: return INVALIDITY;
 * //         }
 * //     }
 * // }
 * </pre>
 */
@Slf4j
public abstract class AbstractAutoAudit {

    public Invalidity INVALIDITY = new Invalidity();
    private final BatistyNamingConverterFactory converterFactory;

    public AbstractAutoAudit(BatistyNamingConverterFactory converterFactory){
        this.converterFactory = converterFactory;
    }

    /**
     * @param sqlCommandKind SQL 타입
     * @return 지원하는 SQL 타입인지 여부
     */
    public abstract boolean isSupport(SqlCommandKind sqlCommandKind);

    /**
     * @param columnName Auto Audit를 실행 할 컬럼명
     * @return Audit Value, 만약 auto audit에 포함하지 않고싶으면 INVALIDITY를 리턴한다.
     */
    public abstract Object getAuditValue(String columnName);

    /**
     * SQL이 실행되는 시점에 대상 객체에 auto audit를 실행한다.
     * @param target 대상 객체
     */
    public void execute(Object target) {

        Class<?> type = target.getClass();
        List<Field> fields = this.getAllField(type);

        fields.forEach(field -> {
            AutoAudit annotation = field.getAnnotation(AutoAudit.class);
            if(annotation == null || annotation.value() != AuditTiming.EXECUTE) return;

            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), type);
                if(pd.getReadMethod().invoke(target) != null) return;

                String columnName = converterFactory.get().getColumnName(field).replaceAll("[^a-zA-Z0-9_]", "");
                Object value = this.getAuditValue(columnName);

                if(value != INVALIDITY) pd.getWriteMethod().invoke(target, value);
            } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
                log.debug(e.getMessage());
            }
        });
    }

    /**
     * 대상 타입의 상속 구조에서 Object 아래까지 추적하면서 모든 Field를 찾아 List로 리턴한다.
     * @param type 대상타입
     * @return 필드 리스트
     */
    public List<Field> getAllField(Class<?> type){
        List<Field> list = Arrays.stream(type.getDeclaredFields())
                .collect(Collectors.toList());

        Class<?> parent = type.getSuperclass();
        if(parent != Object.class) list.addAll(this.getAllField(parent));

        return list;
    }

    private static class Invalidity {
        private Invalidity() {}
    }
}
