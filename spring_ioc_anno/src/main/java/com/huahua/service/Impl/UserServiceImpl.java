package com.huahua.service.Impl;


import com.huahua.dao.UserDao;
import com.huahua.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Service("userService")
@Scope("singleton")
//@Scope("prototype")
public class UserServiceImpl implements UserService {
    @Resource(name = "userDao")
//    @Autowired
    private UserDao userDao;
    @Override
    public void save() {
        userDao.save();
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @PostConstruct
    public void init(){
        System.out.println("init");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("destroy");
    }
}
