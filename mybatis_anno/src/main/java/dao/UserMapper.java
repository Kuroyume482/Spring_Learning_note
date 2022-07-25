package dao;

import domain.Role;
import domain.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {
    @Insert("insert into user values (#{id},#{username},#{password},#{birthday})")
    public void save(User user);
    @Update("update user set username=#{username},password=#{password},birthday=#{birthday} where id=#{id}")
    public void update(User user);
    @Delete("delete from user where id=#{id}")
    public void delete(int id);
    @Select("select * from user where id=#{id}")
    public User findById(int id);
    @Select("select * from user")
    public List<User> findAll();

//    @Select("select *,o.id oid from user u left join tb_order o on u.id=o.uid")
    @Select("select * from user")
    @Results({
            @Result(id = true,column = "id",property = "id"),
            @Result(column = "username",property = "username"),
            @Result(column = "password",property = "password"),
            @Result(column = "birthday",property = "birthday"),
            @Result(
                    property = "orderList",
                    column = "id",
                    javaType = List.class,
                    many = @Many(select = "dao.OrderMapper.findByUid")
            )
    })
    public List<User> findAllUserAndOrderList();

//    select u.*,r.*,r.id rid from user u left join sys_user_role ur on u.id=ur.userId inner join sys_role r on ur.roleId=r.id;
    @Select("select * from user")
    @Results({
            @Result(id = true,column = "id",property = "id"),
            @Result(column = "username",property = "username"),
            @Result(column = "password",property = "password"),
            @Result(column = "birthday",property = "birthday"),
//            @Result(
//                    many = @Many(resultMap = )
//            )
            @Result(
                    property = "roleList",
                    column = "id",
                    javaType = List.class,
                    many = @Many(select = "dao.UserMapper.findByUid")
            )
    })
    public List<User> findUserAndRoleAll();

    @Select("SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=#{uid}")
    public List<Role> findByUid(int uid);
}
