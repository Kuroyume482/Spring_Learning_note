package com.Huahua;


import com.huahua.config.SpringConfiguration;
import com.huahua.service.UserService;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("classpath:applicationContext.xml")
@ContextConfiguration(classes = {SpringConfiguration.class})
public class SpringJunitTestCase {
    @Autowired
    private UserService userService;

    @Autowired
    private ComboPooledDataSource dataSources;

    @Test
    public void test1() throws SQLException {
        userService.save();
        System.out.println(dataSources.getConnection());
    }
}
