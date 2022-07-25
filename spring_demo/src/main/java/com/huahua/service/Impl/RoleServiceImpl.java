package com.huahua.service.Impl;

import com.huahua.dao.RoleDao;
import com.huahua.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements com.huahua.service.RoleService {

    @Autowired
    private RoleDao roleDao;

    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @Override
    public List<Role> list() {
        List<Role> list =  roleDao.findAll();
        return list;
    }

    @Override
    public int save(Role role) {
        return roleDao.save(role);
    }
}
