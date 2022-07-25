package com.huahua.service.impl;


import com.huahua.dao.AccountDao;
import com.huahua.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service("accountService")
//配置全类的事务属性
@Transactional(isolation = Isolation.REPEATABLE_READ)
@EnableTransactionManagement
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountDao accountDao;

//    配置单个方法上的事务属性，优先级高于全类的
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED,rollbackFor = {Error.class,Exception.class})
    public void transfer(String outMan, String inMan, double money) {
        accountDao.out(outMan,money);
        int i = 1/0;
        accountDao.in(inMan,money);
    }
}