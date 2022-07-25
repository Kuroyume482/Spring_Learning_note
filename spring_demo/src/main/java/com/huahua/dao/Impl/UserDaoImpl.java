package com.huahua.dao.Impl;

import com.huahua.dao.UserDao;
import com.huahua.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
@Repository("userDao")
public class UserDaoImpl implements UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findAll() {
        return jdbcTemplate .query("select * from sys_user",new BeanPropertyRowMapper<User>(User.class));
    }

    @Override
    public Long save(User user) {
        //
        PreparedStatementCreator creator = connection -> {
            //使用原始的jdbc完成PreparedStatement的组件
            PreparedStatement preparedStatement = connection.prepareStatement("insert into sys_user values (?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setObject(1,null);
            preparedStatement.setString(2,user.getUsername());
            preparedStatement.setString(3,user.getEmail());
            preparedStatement.setString(4,user.getPhoneNum());
            preparedStatement.setString(5,user.getPhoneNum());
            return preparedStatement;
        };

        //创建keyHolder
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(creator,keyHolder);
        //获得生成的主键
        long userId = keyHolder.getKey().longValue();
        //返回数据库自动生成的id
        return userId;
    }

    @Override
    public void saveUserRoleRel(Long id, Long[] roleIds) {
        for (Long roleId :roleIds) {
            jdbcTemplate.update("insert into sys_user_role values (?,?)",id,roleId);
        }
    }

    @Override
    public void delUserRoleRel(Long userId) {
        jdbcTemplate.update("delete from sys_user_role where userId=?",userId);
    }

    @Override
    public void delUser(Long userId) {
         jdbcTemplate.update("delete from sys_user where id=?",userId);
    }

    @Override
    public User findByUserNameAndPassword(String username, String password) throws EmptyResultDataAccessException {//抛异常，在下一阶段抓异常
        User user = jdbcTemplate.queryForObject("select * from sys_user where username=? and password=?", new BeanPropertyRowMapper<>(User.class), username, password);
        return user;
    }
}
