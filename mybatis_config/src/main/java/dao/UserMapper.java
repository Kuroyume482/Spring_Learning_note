package dao;

import domain.User;

import java.util.List;

public interface UserMapper {
    public void save(User user);
    public List<User> findAll();
    public List<User> findUserAndRoleAll();
}
