package com.huahua.dao.Impl;

import com.huahua.dao.RoleDao;
import com.huahua.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoleDaoImpl implements RoleDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Role> findAll() {
        List<Role> list = jdbcTemplate.query("select * from sys_role", new BeanPropertyRowMapper<Role>(Role.class));
        return list;
    }

    @Override
    public int save(Role role) {
        return  jdbcTemplate.update("insert into sys_role value (?,?,?)", null, role.getRoleName(), role.getRoleDesc());
    }

    @Override
    public List<Role> fingRoleByUserId(Long id) {
        List<Role> roleList = jdbcTemplate
                .query("select * from sys_user_role ur,sys_role r where ur.roleId=r.id and ur.userId=?"
                        , new BeanPropertyRowMapper<Role>(Role.class), id);
        return roleList;
    }
}
