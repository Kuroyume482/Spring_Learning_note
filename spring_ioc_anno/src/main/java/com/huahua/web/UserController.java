package com.huahua.web;

import com.huahua.service.UserService;
import com.huahua.config.SpringConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserController {
    public static void main(String[] args) {
//        ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        AnnotationConfigApplicationContext app = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        UserService userService  = app.getBean(UserService.class);
        userService.save();
        app.close();
    }
}
