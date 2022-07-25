package com.huahua.dao.Impl;

import com.huahua.dao.UserDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

// <bean id="userDao" class="com.Huahua.dao.Impl.UserDaoImpl"></bean>
//@Component("userDao")
@Repository("userDao")
public class UserDaoImpl implements UserDao {
    @Value("${jdbc.driver}")
    private String driver;
    @Override
    public void save() {
        System.out.println(driver);
        System.out.println("UserDaoImpl.save running ...");
    }
}
