package com.huahua.dao;

import com.huahua.domain.User;
import org.springframework.context.annotation.Bean;

import java.util.List;

public interface UserDao {


    List<User> findAll();

    Long save(User user);

    void saveUserRoleRel(Long id, Long[] roleIds);

    void delUserRoleRel(Long userId);

    void delUser(Long userId);

    User findByUserNameAndPassword(String username, String password);
}
