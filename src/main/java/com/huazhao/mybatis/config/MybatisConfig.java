package com.huazhao.mybatis.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MybatisConfig {

   @Bean
   @ConditionalOnMissingBean
   public ConfigurationCustomizer mybatisConfigurationCustomizer() {
       return configuration -> {
           configuration.addInterceptor(new SqlInterceptor());
       };
   }
}
