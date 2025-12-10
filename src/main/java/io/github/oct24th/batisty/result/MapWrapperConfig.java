package io.github.oct24th.batisty.result;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapWrapperConfig {

    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer(CustomMapWrapperFactory customMapWrapperFactory) {
        return configuration -> configuration.setObjectWrapperFactory(customMapWrapperFactory);
    }
}
