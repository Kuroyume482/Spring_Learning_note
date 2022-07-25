import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dao.OrderMapper;
import dao.UserMapper;
import domain.Order;
import domain.User;
import lombok.SneakyThrows;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class MapperTest {
    private UserMapper userMapper;
    private OrderMapper orderMapper;
    private SqlSession sqlSession;
    @SneakyThrows
    @Before
    public void before(){
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        sqlSession = sessionFactory.openSession();
        userMapper = sqlSession.getMapper(UserMapper.class);
        orderMapper = sqlSession.getMapper(OrderMapper.class);
    }

    @After
    public void after(){
        sqlSession.commit();
        sqlSession.close();
    }

    @SneakyThrows
    @Test
    public void test1() {;
/*        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setUsername("user"+i);
            user.setPassword("root"+i);
            user.setBirthday(new Date());
            userMapper.save(user);
            Thread.sleep(2000);
        }*/
//        设置分页的相关参数
        PageHelper.startPage(1,5);
        List<User> userList = userMapper.findAll();
        for (User user : userList) {
            System.err.println(user);
        }
        //获得与分页相关的惨谁
        PageInfo<User> pageInfo = new PageInfo<>(userList);
        System.out.println("总条数:"+pageInfo.getTotal());
        System.out.println("总页数:"+pageInfo.getPages());
        System.out.println("当前页:"+pageInfo.getPageNum());
        System.out.println("每页显示长度:"+pageInfo.getPageSize());
        System.out.println("是 否 第 一 页 :"+pageInfo.isIsFirstPage());
        System.out.println("是否最后一页:"+pageInfo.isIsLastPage());
        System.out.println("上一页"+pageInfo.getPrePage());
        System.out.println("下一页"+pageInfo.getNextPage());
        System.out.println(pageInfo.toString());
    }

    @SneakyThrows
    @Test
    public void test3() {
        List<User> userList = userMapper.findAll();
        for (User user : userList) {
            System.err.println(user);
        }
    }

    @SneakyThrows
    @Test
    public void test2() {
        for (User user : userMapper.findUserAndRoleAll()) {
            System.err.println(user);
        }
    }

    @SneakyThrows
    @Test
    public void test4() {
        for (Order order : orderMapper.findAll1()) {
            System.out.println(order);
        }
    }

    @Test
    public void testSave(){
        User user = new User();
        user.setUsername("asgvfadsfgv");
        user.setPassword("asdf");
        user.setBirthday(new Date());
        userMapper.save(user);
    }

    @Test
    public void testUpdate(){
        User user = new User();
        user.setId(321);
        user.setUsername("asdfs");
        user.setPassword("asdf");
        user.setBirthday(new Date());
        userMapper.update(user);
    }

    @Test
    public void testDelete(){
        userMapper.delete(321);
    }

    @Test
    public void testSelect(){
        System.out.println(userMapper.findById(318));
    }

    @Test
    public void testFindAll(){
        userMapper.findAll().forEach(user -> {
            System.out.println(user);
        });
    }

    @Test
    public void testOrderFindAll(){
        orderMapper.findAll1().forEach(order -> {
            System.out.println(order);
        });
        orderMapper.findAll2().forEach(order -> {
            System.out.println(order);
        });
    }

    @Test
    public void testOneToMany(){
        userMapper.findAllUserAndOrderList().forEach(user -> {
            System.out.println(user);
        });
    }

    @Test
    public void testManyToMany(){
        userMapper.findUserAndRoleAll().forEach(user -> {
            System.out.println(user);
        });
    }
}
