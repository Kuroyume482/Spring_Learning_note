package dao;

import domain.User;

import java.io.IOException;
import java.util.List;

public interface UserMapper {
    public List<User> findAll() throws IOException;
    public User findById(int id);
    public void save(User user);
    public void update(User user);
    public void delete(int id);
}
