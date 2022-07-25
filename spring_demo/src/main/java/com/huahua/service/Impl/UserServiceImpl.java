package com.huahua.service.Impl;

import com.huahua.dao.RoleDao;
import com.huahua.dao.UserDao;
import com.huahua.domain.Role;
import com.huahua.domain.User;
import com.huahua.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleDao roleDao;

    @Override
    public List<User> list() {
        List<User> userList = userDao.findAll();
        //封装userList中每一个User的roles数据
        for (User user:userList){
            //获得User的id
            Long id = user.getId();
            //将id作为参数 查询当前userId对应的role对象
            List<Role> roles =  roleDao.fingRoleByUserId(id);
            user.setRoles(roles);
        }
        return userList;
    }

    @Override
    public void save(User user, Long[] roleIds) {
        //一 向sys_user存储数据
        Long i = userDao.save(user);
        //二 向sys_user_role关系表中存储多条数据
        userDao.saveUserRoleRel(i,roleIds);
    }

    @Override
    public void del(Long userId) {
        userDao.delUserRoleRel(userId);
        userDao.delUser(userId);
    }

    @Override
    public User login(String username, String password) {
        //如果无异常正常反值
        try {
            User user = userDao.findByUserNameAndPassword(username,password);
            return user;
        }catch (EmptyResultDataAccessException e){
            //有异常将异常转为null
            return null;
        }


    }
}
