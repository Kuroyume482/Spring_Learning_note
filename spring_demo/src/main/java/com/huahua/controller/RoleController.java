package com.huahua.controller;

import com.huahua.domain.Role;
import com.huahua.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RequestMapping("role")
@Controller
public class RoleController {

    @Autowired
    private RoleService roleService;

    @RequestMapping("/save")
    public String save(Role role){
        int save = roleService.save(role);
        return save>0?"redirect:/role/list":"../404";
    }

    @RequestMapping("/list")
    public ModelAndView list(){
        ModelAndView modelAndView = new ModelAndView();
        List<Role> list = roleService.list();
        modelAndView.addObject("roleList",list);
        modelAndView.setViewName("role-list");
        return modelAndView;
    }
}
