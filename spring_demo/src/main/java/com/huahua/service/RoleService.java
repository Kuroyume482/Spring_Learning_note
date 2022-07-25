package com.huahua.service;

import com.huahua.domain.Role;

import java.util.List;

public interface RoleService {
    List<Role> list();
    int save(Role role);
}
