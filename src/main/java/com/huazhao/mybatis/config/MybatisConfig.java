package com.huazhao.mybatis.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 *      MybatisPlus配置类 分页配置加上 sql 的打印的配置
 * </p>
 *
 * @author: 花朝
 * @date 2024/7/29  14:39:57
 * @description:
 */
@Configuration
public class MybatisConfig {

   @Bean
   public ConfigurationCustomizer mybatisConfigurationCustomizer() {
       return configuration -> {
           configuration.addInterceptor(new SqlInterceptor());
       };
   }
}
