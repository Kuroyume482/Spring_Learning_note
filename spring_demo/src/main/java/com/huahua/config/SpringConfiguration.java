package com.huahua.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Annotation;

@Configuration
@ComponentScan("com.huahua")
@Import({DataSourceConfiguration.class,JdbcTemplateConfiguration.class,InterceptorConfiguration.class, mvcConfiguration.class})
public class SpringConfiguration {

}
