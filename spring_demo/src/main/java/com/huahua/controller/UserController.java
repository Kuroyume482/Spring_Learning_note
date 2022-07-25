package com.huahua.controller;

import com.huahua.domain.User;
import com.huahua.service.RoleService;
import com.huahua.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@RequestMapping("/user")
@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @RequestMapping("/list")
    public ModelAndView getUserList(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user-list");
        modelAndView.addObject("userList",userService.list());
        return modelAndView;
    }

    @RequestMapping("/addUI")
    public ModelAndView addUI(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user-add");
        modelAndView.addObject("roleList",roleService.list());
        return modelAndView;
    }

    @RequestMapping("/save")
    public String save(User user,Long[] roleIds){
        userService.save(user,roleIds);
        return "redirect:/user/list";
    }

    @RequestMapping("/del/{id}")
    public String deleteOne(@PathVariable("id") Long userId){
        userService.del(userId);
        return "redirect:/user/list";
    }

    @RequestMapping("/login")
    public String login(String username, String password, HttpSession session){
        User user = userService.login(username,password);
        if (null!=user){
            session.setAttribute("user",user);
            return "redirect:/index.jsp";
        }
        return "redirect:/login.jsp";
    }
}
