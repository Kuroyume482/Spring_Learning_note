package service;



import dao.UserMapper;
import domain.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UserService {
    public static void main(String[] args) throws IOException {
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);


//        List<User> all = mapper.findAll();
//        System.out.println(all);
//        User user = all.get(0);
//        user.setUsername("asdadf");
//        mapper.update(user);
//        System.out.println(mapper.findAll());
//        User user1 = new User();
//        user1.setUsername("kakkaak");
//        user1.setPassword("babab");
//        mapper.save(user1);
//        System.out.println(mapper.findAll());
//        mapper.delete(user.getId());
//        System.out.println(mapper.findAll());

        for (int i = 0; i < 100; i++) {
            User user = new User();
            user.setUsername("user"+i);
            user.setPassword("user"+i);
            mapper.save(user);
        }
        System.out.println(mapper.findAll());
        sqlSession.commit();
    }
}
