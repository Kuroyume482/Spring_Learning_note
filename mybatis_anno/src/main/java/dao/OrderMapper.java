package dao;

import domain.Order;
import domain.User;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderMapper {

    //两张表一起查
    @Select("select *,o.id oid from tb_order o,user u where o.uid=u.id")
    @Results({
            @Result(column = "id",property = "id"),
            @Result(column = "ordertime",property = "ordertime"),
            @Result(column = "total",property = "total"),
            @Result(column = "total",property = "total"),
            @Result(column = "uid",property = "user.id"),
            @Result(column = "username",property = "user.username"),
            @Result(column = "password",property = "user.password"),
            @Result(column = "birthday",property = "user.birthday"),
    })
    public List<Order> findAll1();

//    分开查询
    @Select("select * from tb_order")
        @Results({
            @Result(column = "id",property = "id"),
            @Result(column = "ordertime",property = "ordertime"),
            @Result(column = "total",property = "total"),
            @Result(column = "total",property = "total"),
            @Result(
                javaType = User.class, //要封装的实体类型
                property = "user" ,  //要封装的属性名称
                column =  "uid", //根据哪个字段去查询User表内容
                one = @One(select = "dao.UserMapper.findById")
            )
    })
    public List<Order> findAll2();

    @Select("select * from tb_order where uid=#{uid}")
    public List<Order> findByUid();
}
