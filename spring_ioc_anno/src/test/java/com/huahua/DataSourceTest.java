package com.huahua;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;
import java.util.ResourceBundle;

public class DataSourceTest {
    @Test
    public void test1() throws Exception{
    //手动创建c3p0数据源
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/demo");
        dataSource.setUser("root");
        dataSource.setPassword("root");
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Test
    public void test2() throws Exception {
        //手动创建Druid数据源
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/demo");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        DruidPooledConnection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Test
    public void test3() throws Exception{
        //手动创建c3p0数据源(加载properties配置文件）
        //读取配置文件
        ResourceBundle resourceBundle = ResourceBundle.getBundle("application");
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(resourceBundle.getString("jdbc.driver"));
        dataSource.setJdbcUrl(resourceBundle.getString("jdbc.url"));
        dataSource.setUser(resourceBundle.getString("jdbc.username"));
        dataSource.setPassword(resourceBundle.getString("jdbc.password"));
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Test
    public void test4() throws Exception {
        //手动创建Druid数据源(加载properties配置文件）
        ResourceBundle resourceBundle = ResourceBundle.getBundle("application");
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(resourceBundle.getString("jdbc.driver"));
        dataSource.setUrl(resourceBundle.getString("jdbc.url"));
        dataSource.setUsername(resourceBundle.getString("jdbc.username"));
        dataSource.setPassword(resourceBundle.getString("jdbc.password"));
        DruidPooledConnection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Test
    public void test5() throws Exception {
        //测试Spring容器生成数据源对象
        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        ComboPooledDataSource dataSource = app.getBean(ComboPooledDataSource.class);
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }
}
