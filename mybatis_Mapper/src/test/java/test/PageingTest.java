package test;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dao.UserMapper;
import domain.User;
import lombok.SneakyThrows;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

public class PageingTest {

    @SneakyThrows
    @Test
    public void test1() {
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

/*        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setUsername("user"+i);
            user.setPassword("root"+i);
            user.setBirthday(new Date());
            mapper.save(user);
            Thread.sleep(2000);
        }*/
//        设置分页的相关参数
        PageHelper.startPage(1,5);
        List<User> userList = mapper.findAll();
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

        sqlSession.commit();
        sqlSession.close();
    }
}
