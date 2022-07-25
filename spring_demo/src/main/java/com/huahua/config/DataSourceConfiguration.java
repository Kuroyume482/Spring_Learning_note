package com.huahua.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.beans.PropertyVetoException;

@PropertySource("classpath:application.properties")
public class DataSourceConfiguration {

    @Value(("${jdbc.driver}"))
    private String driver;
    @Value(("${jdbc.url}"))
    private String url;
    @Value(("${jdbc.username}"))
    private String username;
    @Value(("${jdbc.password}"))
    private String password;
    @Bean("dataSource")
    public ComboPooledDataSource getDataSource() throws PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(driver);
        dataSource.setJdbcUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }

}
