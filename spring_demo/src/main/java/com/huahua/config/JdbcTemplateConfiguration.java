package com.huahua.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcTemplateConfiguration {
    @Autowired
    ComboPooledDataSource dataSource;

    @Bean("jdbcTemplate")
    public JdbcTemplate getJdbcTemplate(){
        JdbcTemplate jdbcTemplate = new  JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
        return  jdbcTemplate;
    }
}
