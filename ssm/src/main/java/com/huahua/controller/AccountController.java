package com.huahua.controller;

import com.huahua.domain.Account;
import com.huahua.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping(value = "/account",produces = "text/html;charset=UTF-8")
public class AccountController {

    @Autowired
    private AccountService accountService;

    //保存
    @RequestMapping("/save")
    @ResponseBody
    public String save(Account account){
        try {
            accountService.save(account);
            return "保存成功";
        }catch (Exception e){
            System.out.println(e);
            return "保存失败";
        }

    }

    //查询
    @RequestMapping("findAll")
    public ModelAndView findAll(){
        List<Account> accounts = accountService.findAll();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("accountList",accounts);
        modelAndView.setViewName("accountList");
        return modelAndView;
    }
}
