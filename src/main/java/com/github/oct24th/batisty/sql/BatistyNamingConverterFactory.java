package com.github.oct24th.batisty.sql;

import com.github.oct24th.batisty.annotation.Executable;
import com.github.oct24th.batisty.annotation.Table;
import com.github.oct24th.batisty.sql.impl.DefaultNamingConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * BatistyNamingConverter를 제공하는 bean<br>
 * BatistyNamingConverter를 상속받은 bean을 등록하면 해당 bean을 리턴하고<br>
 * 등록된 bean이 없으면 DefaultNamingConverter을 생성해서 리턴한다.
 */
@Component
public class BatistyNamingConverterFactory {

    private final BatistyNamingConverter converter;

    @Autowired
    public BatistyNamingConverterFactory(Optional<BatistyNamingConverter> customConverter){
        BatistyNamingConverter temp = customConverter.orElseGet(DefaultNamingConverter::new);
        converter = new BatistyNamingConverterWrapper(temp);
    }

    public BatistyNamingConverter get(){
        return converter;
    }

    /**
     * BatistyNamingConverter의 Wrapper클래스<br>
     * &nbsp;&nbsp;Table 어노테이션에의해 스키마, 테이블 이름이 지정된경우 테이블 이름을 처리한다.<br>
     * &nbsp;&nbsp;스키마, 테이블, 커럼명을 " 로 감싸준다.
     */
    private static class BatistyNamingConverterWrapper implements BatistyNamingConverter{

        private final BatistyNamingConverter origin;

        public BatistyNamingConverterWrapper(BatistyNamingConverter converter){
            this.origin = converter;
        }

        @Override
        public <T> String getTableName(Class<T> type) {
            Table table = type.getAnnotation(Table.class);
            return table != null && !"".equals(table.value()) ? table.value() : origin.getTableName(type);
        }

        @Override
        public <T> String getExecutableName(Class<T> type) {
            Executable executable = type.getAnnotation(Executable.class);
            return executable != null && !"".equals(executable.value()) ? executable.value() : origin.getExecutableName(type);
        }

        @Override
        public String getColumnName(Field field) {
            return origin.getColumnName(field);
        }

        @Override
        public String getBindingMarkup(String bindName) {
            return origin.getBindingMarkup(bindName);
        }
    }
}
