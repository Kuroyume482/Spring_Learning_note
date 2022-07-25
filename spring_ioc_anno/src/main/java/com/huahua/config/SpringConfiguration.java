package com.huahua.config;

import com.huahua.config.DataSourceConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

//标志该类时Spring的核心配置类
@Configuration
//组件扫描    <context:component-scan base-package="com.Huahua"></context:component-scan>
@ComponentScan("com.huahua")
//<import resource>
@Import({DataSourceConfiguration.class})
public class SpringConfiguration {

}
