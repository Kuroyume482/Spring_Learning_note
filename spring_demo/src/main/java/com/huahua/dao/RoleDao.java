package com.huahua.dao;

import com.huahua.domain.Role;

import java.util.List;

public interface RoleDao {
    List<Role> findAll();
    int save(Role role);
    List<Role> fingRoleByUserId(Long id);
}
