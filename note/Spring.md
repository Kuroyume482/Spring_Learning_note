# 1、Spring配置文件

## 1.1 Bean标签基本配置

用于配制对象交由Spring来创建。

默认情况下他调用的是类中的无参构造函数，如果没有无参构造函数则不能创建成功。

基本属性：

- id：Bean实例在Spring容器中的唯一标识
- class：Bean的全限定名称

## 1.2 Bean标签范围配置

scope：指对象的作用范围，取值如下：

| 取值范围       | 说明                                                         |
| -------------- | ------------------------------------------------------------ |
| singleton      | 默认取值（不指定则为该值），单例的                           |
| prototype      | 多例的                                                       |
| request        | WEB项目中，Spring创建一个Bean的对象，将对象存入到request域中 |
| session        | WEB项目中，Spring创建一个Bean对象，将对象存入到session域中   |
| global session | WEB项目中，应用在Portlet环境，如果没有Portlet环境那么globalSession相当于session |

配置：

```xml
<bean id="userDao" class="com.huahua.dao.impl.UserDaoImpl" scope="singleton"></bean>
<!--实例只有一个地址-->
<bean id="userDao" class="com.huahua.dao.impl.UserDaoImpl" scope="prototype"></bean>
```

测试：

```java
@Test
    public void test1(){
        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        UserDao userDao1 = (UserDao) app.getBean("userDao");
        UserDao userDao2 = (UserDao) app.getBean("userDao");
      //打印地址看看是否一样
        System.out.println(userDao1);
        System.out.println(userDao2);
    }
```

结果：

```basic
singleton:
com.huahua.dao.impl.UserDaoImpl@b7dd107
com.huahua.dao.impl.UserDaoImpl@b7dd107
prototype:
com.huahua.dao.impl.UserDaoImpl@4671e53b
com.huahua.dao.impl.UserDaoImpl@2db7a79b
```

创建时机区别：

在创建ApplicationContext实例的时候就创建了singleton取值的Bean，且该取值的Bean指创建一次（无参构造函数只调用一次）；

在加载ApplicationContext配置文件的时候不创建prototype取值的Bean，每次获取prototype对象的时候创建Bean



总结：

### 1）当scope值为singleton时

Bean的实例化个数：1个

Bean的实例化时机：当Spring核心文件被加载时，实例化配置Bean实例。

Bean的生命周期：

- 对象创建：当应用加载，创建容器时吗，对象创建
- 对象运行：只要容器在，对象一直活着
- 对象销毁：当应用卸载，销毁容器时，对象被销毁

### 2）当scope值为prototpe时

Bean的实例化个数：多个

Bean的实例化时机：当应用调用getBean()方法时

生命周期：

- 创建对象：当使用对象时，创建新的对象实例
- 对象运行：只要对象在使用中，就一直活着
- 对象销毁：当对象长时间不使用时，被Java垃圾回收器回收。

## 1.3 Bean生命周期配置

- Init-mathod：指定类的初始化方法名称，初始化方法应在类中存在
- destroy-method：指定类中销毁方法名称，销毁方法在主动关闭的时候才会调用，被动销毁时来不及调用

```xml
<bean id="userDao" 
class="com.huahua.dao.impl.UserDaoImpl" 
scope="singleton" 
init-method="init" 
destroy-method="destroy"/>
```

```java
package com.huahua.dao.impl;

import com.huahua.dao.UserDao;

public class UserDaoImpl implements UserDao {

    public UserDaoImpl(){
        System.out.println("UserDaoImpl Creating...");
    }

    public void init(){
        System.out.println("Initializing...");
    }

    public void destroy(){
        System.out.println("Destroying...");
    }

    @Override
    public void save() {
        System.out.println("UserDaoImpl.save is Running...");
    }
}

```

## 1.4 Bean实例化的三种方式

- 无参构造方法实例化

    默认配置，通过全限定名称指定的类的无参构造方法来实例化Bean

- 工厂静态方法实例化

- 工厂实例方法实例化

### 1.4.1 无参构造方法实例化

见前文

### 1.4.2 工厂静态方法实例化

```java
package com.huahua.factory;

import com.huahua.dao.UserDao;
import com.huahua.dao.impl.UserDaoImpl;

public class StaticFactory {
    public static UserDao getUserDao(){
        return new UserDaoImpl();
    }
}

```

```xml
<bean id="userDao" class="com.huahua.factory.StaticFactory" factory-method="getUserDao"/>
```

调用userDao的bean的id即返回getUserDao工厂方法

### 1.4.3 工厂实例方法实例化

```java
package com.huahua.factory;

import com.huahua.dao.UserDao;
import com.huahua.dao.impl.UserDaoImpl;

public class DynamicFactory {
    public UserDao getUserDao(){
        return new UserDaoImpl();
    }
}
```

```xml
    <bean id="factory" class="com.huahua.factory.DynamicFactory"/>
    <bean id="userDao"  factory-bean="factory" factory-method="getUserDao"/>
```

## 1.5 Bean的依赖注入分析

### 1.5.1 基于上述知识量写Service层

```java
package com.huahua.service.impl;

import com.huahua.dao.UserDao;
import com.huahua.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserServiceImpl implements UserService {

    @Override
    public void save() {
        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        UserDao userDao = (UserDao) app.getBean("userDao");
        userDao.save();
    }
}
```

```xml
<bean id="userDao" class="com.huahua.dao.impl.UserDaoImpl" 
init-method="init" destroy-method="destroy"/>
<bean id="userService" class="com.huahua.service.impl.UserServiceImpl"></bean>
```

```java
package com.huahua.demo;

import com.huahua.service.UserService;
import com.huahua.service.impl.UserServiceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserController {
    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        UserService userService = (UserService) app.getBean("userService");
        userService.save();
    }
}
```

弊端：controller层调用service层时访问了核心配置文件，service层调用dao层的时候也访问了核心配置文件，经过多次来回访问才将对象获取到，路程较长。

改善方法：使用依赖注入，将dao层直接注入到service层

### 1.5.2  依赖注入概念

依赖注入(Dependency Injection)：它是Spring框架核心IOC的具体实现。

注：在编写程序时，通过控制反转，把对象的创建权交给Spring，但是代码中不会出现没有依赖的情况。

IOC解耦只是降低他们的依赖关系，但不会消除。例如：业务层任会调用持久层方法。

依赖注入的一大作用就是来维护业务层和持久层的关系。

### 1.5.3 依赖注入的两种方式

- 构造方法
- set方法

####  set法法注入：

给对象一个set方法

```java
package com.huahua.service.impl;

import com.huahua.dao.UserDao;
import com.huahua.service.UserService;

public class UserServiceImpl implements UserService {
    private UserDao userDao;

    @Override
    public void save() {
        userDao.save();
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
```

给userService的实例化标签中设置属性，name为setUserDao方法去掉set前缀并将首字母小写，ref为需要注入的对象的标签id

```xml
<bean id="userDao" class="com.huahua.dao.impl.UserDaoImpl" scope="singleton" init-method="init" destroy-method="destroy"/>
    <bean id="userService" class="com.huahua.service.impl.UserServiceImpl">
        <property name="userDao" ref="userDao"/>
    </bean>
```

#### set方法的p命名空间注入：

1. 引入p命名空间：

   ```xml
   xmlns:p="http://www.springframework.org/schema/p"
   ```

2. 修改注入方式：

   ```xml
   <bean id="userService" class="com.huahua.service.impl.UserServiceImpl" p:userDao-ref="userDao"></bean>
   ```

   

#### 构造方法注入：

为service方法添加有参构造方法（用于实例化与注入依赖）和无参构造方法（用于实例化）

```java
package com.huahua.service.impl;

import com.huahua.dao.UserDao;
import com.huahua.service.UserService;

public class UserServiceImpl implements UserService {
    private UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserServiceImpl() {
    }

    @Override
    public void save() {
        userDao.save();
    }
}
```

在bean中设置构造方法注入，name为构造方法的参数名，ref为需要注入的对象的标签id

```java
    <bean id="userDao" class="com.huahua.dao.impl.UserDaoImpl" scope="singleton" init-method="init" destroy-method="destroy"/>
    <bean id="userService" class="com.huahua.service.impl.UserServiceImpl">
        <constructor-arg name="userDao" ref="userDao"></constructor-arg>
    </bean>
```

## 1.6 Bean依赖注入的数据类型

- 普通数据类型
- 引用数据类型
- 集合数据类型

### 1.6.1 普通属性注入

给类中添加普通属性，与其set方法

```java
package com.huahua.dao.impl;

import com.huahua.dao.UserDao;

public class UserDaoImpl implements UserDao {
    private String username;
    private int age;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public UserDaoImpl(){
        System.out.println("UserDaoImpl Creating...");
    }

    public void init(){
        System.out.println("Initializing...");
    }

    public void destroy(){
        System.out.println("Destroying...");
    }

    @Override
    public void save() {
        System.out.println(username+"------------"+age);
        System.out.println("UserDaoImpl.save is Running...");
    }
}
```

在配置文件中通过设值注入添加属性value

```xml
  <bean id="userDao" class="com.huahua.dao.impl.UserDaoImpl" scope="singleton" init-method="init" destroy-method="destroy">
        <property name="username" value="huahua"/>
        <property name="age" value="18"/>
    </bean>
```

测试：

```
UserDaoImpl Creating...
Initializing...
huahua------------18
UserDaoImpl.save is Running...

进程已结束，退出代码为 0
```

### 1.6.2 引用数据类型注入

见1.5.3

### 1.6.3 集合数据类型注入

给类添加数据集合，List，Map<String, User>，Properties分别进行数据注入和打印

```
package com.huahua.dao.impl;

import com.huahua.dao.UserDao;
import com.huahua.domain.User;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class UserDaoImpl implements UserDao {
    private List<String> stringList;
    private Map<String, User> userMap;
    private Properties properties;

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public void setUserMap(Map<String, User> userMap) {
        this.userMap = userMap;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void save() {
        System.out.println("UserDaoImpl.save is Running...");
        System.out.println(stringList);
        System.out.println(userMap);
        System.out.println(properties);
    }
}

```

设置一个类作为Map的元素值类型

```java
package com.huahua.domain;

public class User {
    private String name;
    private String addr;

    public void setName(String name) {
        this.name = name;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getName() {
        return name;
    }

    public String getAddr() {
        return addr;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", addr='" + addr + '\'' +
                '}';
    }
}
```

分别给list集合注入数据（普通类型数据），map集合注入数据（引用类型），Properties（字符串类型的键值对，普通数据类型

```xml
<bean id="user1" class="com.huahua.domain.User" p:addr="123123" p:name="tom"></bean>
    <bean id="user2" class="com.huahua.domain.User" p:addr="323123" p:name="jin"></bean>

    <bean id="userDao" class="com.huahua.dao.impl.UserDaoImpl" scope="singleton" init-method="init" destroy-method="destroy">
        <property name="stringList">
            <list>
                <value>aaa</value>
                <value>bbb</value>
                <value>ccc</value>
            </list>
        </property>
        <property name="userMap">
            <map>
                <entry key="user1" value-ref="user1"></entry>
                <entry key="user2" value-ref="user2"></entry>
            </map>
        </property>
        <property name="properties">
            <props>
                <prop key="p1">ppp1</prop>
                <prop key="p2">ppp2</prop>
                <prop key="p3">ppp3</prop>
                <prop key="p4">ppp4</prop>
            </props>
        </property>
    </bean>
```

测试：

```
UserDaoImpl.save is Running...
[aaa, bbb, ccc]
{user1=User{name='tom', addr='123123'}, user2=User{name='jin', addr='323123'}}
{p4=ppp4, p3=ppp3, p2=ppp2, p1=ppp1}

进程已结束，退出代码为 0
```



## 1.7 引入其他配置文件

通过配置引入，实现配置文件的分工

```xml
 <import resource="applicationContext-user.xml"/>
 <import resource="applicationContext-product.xml"/>
```

## 1.8 Spring相关的API

### 1.8.1 ApplicationContext的继承体系

applicationContext：接口类型，代表应用上下文，可以通过其实例获得Spring容器中的Bean对象 

![截屏2022-07-15 20.21.49](/Users/kuroyume/Spring/Spring/note/截屏2022-07-15 20.21.49.png)

### 1.8.2 ApplicationContext的实现类

1） ClassPathXmlApplicationContext

​      从类的根路径下加载配置文件推荐使用

2） FileSystemXmlApplicationContext

​      从磁盘路径上加载配置文件，任意可访问位置

3） AnnotationConfigAppicationContext

​      当使用注解配置容器对象时，需要使用此类来创建spring容器。它用来读取注解。

### 1.8.3 getBean()方法使用

- 通过id的方式获取Bean

  ```java
       UserService userService = (UserService) app.getBean("userService");
       userService.save();
  ```

- 通过字节码对象类型获取Bean

  ```java
  UserService bean = app.getBean(UserService.class);
  bean.save();
  ```

  

```java
public Object getBean(String name) throws BeanException{
	assertBeanFactoryActive();
  return getBeanfactory().getBean(name);
}

public <T> T getBean(Class<T> requirdType) throws BeanException{
  assertBeanFactoryActive();
  return getBeanFactory().getBean(requiredType);
}
```

区别：

当配置文件中存在同一个类的多个实例化标签，只能通过id获取Bean，

当一个类只存在一个实例化标签于配置文件中时，可以使用字节码对象类型获取Bean



# 2、Spring配置数据源

## 2.1 数据源(连接池)的作用

- 为提高程序性能而出现
- 实现实例化数据源，初始化部分连接资源
- 使用连接资源时，从数据源中获取
- 使用完毕之后将连接资源归还给数据源

常见的数据源：DBCP、C3P0、BoneCP、Druid等

## 2.2 数据源的开发步骤

1. 倒入数据源的坐标和数据库驱动坐标
2. 创建数据源对象
3. 设置数据源的基本连接信息
4. 使用数据源获取连接资源和归还资源

## 2.3 数据源的手动创建

```java
@Test
    public void test1() throws Exception{
    //手动创建c3p0数据源
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/demo");
        dataSource.setUser("root");
        dataSource.setPassword("root");
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Test
    public void test2() throws Exception {
        //手动创建Druid数据源
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/demo");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        DruidPooledConnection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }
```

## 2.4 配置文件的抽取

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/demo
jdbc.username=root
jdbc.password=root
```

再配置数据源

```java
  @Test
    public void test3() throws Exception{
        //手动创建c3p0数据源(加载properties配置文件）
        //读取配置文件
        ResourceBundle resourceBundle = ResourceBundle.getBundle("application");
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(resourceBundle.getString("jdbc.driver"));
        dataSource.setJdbcUrl(resourceBundle.getString("jdbc.url"));
        dataSource.setUser(resourceBundle.getString("jdbc.username"));
        dataSource.setPassword(resourceBundle.getString("jdbc.password"));
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Test
    public void test4() throws Exception {
        //手动创建Druid数据源(加载properties配置文件）
        ResourceBundle resourceBundle = ResourceBundle.getBundle("application");
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(resourceBundle.getString("jdbc.driver"));
        dataSource.setUrl(resourceBundle.getString("jdbc.url"));
        dataSource.setUsername(resourceBundle.getString("jdbc.username"));
        dataSource.setPassword(resourceBundle.getString("jdbc.password"));
        DruidPooledConnection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }
```

## 2.5 Spring 配置数据源

将Data Source的创建权交给Spring容器

通过set注入获得数据源

```xml
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
        <property name="driverClass" value="com.mysql.cj.jdbc.Driver"></property>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo"></property>
        <property name="user" value="root"></property>
        <property name="password" value="root"></property>
    </bean>
```

```java
@Test
public void test5() throws Exception {
    //测试Spring容器生成数据源对象
    ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
    ComboPooledDataSource dataSource = app.getBean(ComboPooledDataSource.class);
    Connection connection = dataSource.getConnection();
    System.out.println(connection);
    connection.close();
}
```

## 2.6 核心配置文件中抽取properties

1. 添加context命名空间到核心配置文件中去

   ```xml
   xmlns:context="http://www.springframework.org/schema/context"
          xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
          http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd"
   ```

   

2. 使用<context:property-placeholder指定配置文件的位置

   ```xml
    <context:property-placeholder location="classpath:application.properties"/>
   ```

   

3. 通过el表达式引用属性值

```xml
<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
    <property name="driverClass" value="${jdbc.driver}"></property>
    <property name="jdbcUrl" value="${jdbc.url}"></property>
    <property name="user" value="${jdbc.username}"></property>
    <property name="password" value="${jdbc.password}"></property>
</bean>
```

# 3、Spring注解开发

Spring时轻代码重配置的框架，配置比较繁重，影响开发效率，所以注解开发是一种趋势，注解代替Xml文件可以简化配置，提高开发效率。

## 3.1 Spring原始注解

Spring原始注解主要替代Bean的配置

|             注解             |                             说明                             |
| :--------------------------: | :----------------------------------------------------------: |
|      @Component（组件）      |                   使用在类上用于实例化Bean                   |
|    @Controller（控制器）     |                使用在WEB层类上用于实例化Bean                 |
|       @Service（服务）       |              使用在Service层类上用于实例化Bean               |
|    @Repository（存储库）     |      使用在Dao（Date Access Object）层上用于实例化Bean       |
| @Autowired（自动装配，注入） |           单一使用时，在字段上用于根据类型依赖注入           |
|          @Qualifier          | 结合@Autowired一起使用，附加在其后，用于根据名称进行依赖注入 |
|          @Resource           |        相当于@Autowired+@Qualifier，按照名称进行注入         |
|            @Value            |                         注入普通属性                         |
|            @Scope            |                      标注Bean的作用范围                      |
|        @PostConstruct        |           使用在方法上标注该方法是Bean的初始化方法           |
|         @PreDestory          |            使用在方法上标注该方法是Bean的销毁方法            |

注：Controller、service、Repository是Component的三个衍生注解，以提高代码可读性

### 3.1.1 演示

1. 添加组件扫描以寻找所有的注解

   ```xml
       <context:component-scan base-package="com.huahua"></context:component-scan>
   ```

2. 将配置标签替换为实例化Bean的注解

   ```java
   package com.huahua.dao.Impl;
   
   import com.huahua.dao.UserDao;
   import org.springframework.stereotype.Component;
   
   // <bean id="userDao" class="com.huahua.dao.Impl.UserDaoImpl"></bean>
   @Component("userDao")
   public class UserDaoImpl implements UserDao {
       @Override
       public void save() {
           System.out.println("UserDaoImpl.save running ...");
       }
   }
   ```

3. 给需要注入的数据配置自动装配注解

   ```java
   package com.huahua.service.Impl;
   
   import com.huahua.dao.UserDao;
   import com.huahua.service.UserService;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.beans.factory.annotation.Qualifier;
   import org.springframework.stereotype.Component;
   
   //<bean id="userService" class="com.huahua.service.Impl.UserServiceImpl">
   @Component("userService")
   public class UserServiceImpl implements UserService {
       //<property name="userDao" ref="userDao"></property>
       @Autowired
       @Qualifier("userDao")
       private UserDao userDao;
       @Override
       public void save() {
           userDao.save();
       }
   
       public void setUserDao(UserDao userDao) {
           this.userDao = userDao;
       }
   }
   ```

4. 将上面的注解替换为衍生注解

   ```java
   package com.huahua.dao.Impl;
   
   import com.huahua.dao.UserDao;
   import org.springframework.stereotype.Repository;
   
   // <bean id="userDao" class="com.huahua.dao.Impl.UserDaoImpl"></bean>
   //@Component("userDao")
   @Repository("userDao")
   public class UserDaoImpl implements UserDao {
       @Override
       public void save() {
           System.out.println("UserDaoImpl.save running ...");
       }
   }
   ```

   ```java
   package com.huahua.service.Impl;
   
   import com.huahua.dao.UserDao;
   import com.huahua.service.UserService;
   import org.springframework.stereotype.Service;
   
   import javax.annotation.Resource;
   
   //<bean id="userService" class="com.huahua.service.Impl.UserServiceImpl">
   //@Component("userService")
   @Service("userService")
   public class UserServiceImpl implements UserService {
       //<property name="userDao" ref="userDao"></property>
   //    @Autowired
   //    @Qualifier("userDao")
       @Resource(name = "userDao")
       private UserDao userDao;
       @Override
       public void save() {
           userDao.save();
       }
   
       public void setUserDao(UserDao userDao) {
           this.userDao = userDao;
       }
   }
   ```

### 3.1.2 普通类型注入

```java
package com.huahua.dao.Impl;

import com.huahua.dao.UserDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

// <bean id="userDao" class="com.huahua.dao.Impl.UserDaoImpl"></bean>
//@Component("userDao")
@Repository("userDao")
public class UserDaoImpl implements UserDao {
   @Value("${jdbc.driver}")
   private String driver;

   @Override
   public void save() {
      System.out.println(driver);
      System.out.println("UserDaoImpl.save running ...");
   }
}
```

通过spel表达式获取properties配置文件中的信息，用@Value注解进行普通类型注入

测试：

```
com.mysql.cj.jdbc.Driver
UserDaoImpl.save running ...

进程已结束，退出代码为 0
```

### 3.1.3 @Scope注解、初始化、销毁

```java
package com.huahua.service.Impl;

import com.huahua.dao.UserDao;
import com.huahua.service.UserService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Service("userService")
@Scope("singleton")
//@Scope("prototype")
public class UserServiceImpl implements UserService {
   @Resource(name = "userDao")
   private UserDao userDao;

   @Override
   public void save() {
      userDao.save();
   }

   public void setUserDao(UserDao userDao) {
      this.userDao = userDao;
   }

   @PostConstruct
   public void init() {
      System.out.println("init");
   }

   @PreDestroy
   public void destroy() {
      System.out.println("destroy");
   }
}

```

```java
package com.huahua.web;

import com.huahua.service.UserService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserController {
   public static void main(String[] args) {
      ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
      UserService userService = app.getBean(UserService.class);
      userService.save();
      app.close();
   }
}
```

测试：

```java
init
com.mysql.cj.jdbc.Driver
UserDaoImpl.save running ...
destroy
```

## 3.2 Spring新注解

原始注解不能全部替代xml配置文件，如：

- 非自定义的Bean的配置：<Bean>
- 加载properties文件的配置：<<context:proerty-laceholder>>
- 组件扫描配置:<<context:compent-scan>>
- 导入其他配置:<import resource>

|      注解       |                             说明                             |
| :-------------: | :----------------------------------------------------------: |
| @Configuration  | 用于指定当前类是一个Spring配置类，当创建容器时会从该类上加载注解 |
| @ComponentScan  | 用于指定Spring在初始化容器时要扫描的包。作用和在Spring的xml配置文件中的<context:component-scan base-package="com.huahua"><</context:component-scan>>一样 |
|      @Bean      |     使用在方法上，标注将该方法的返回值存储到Spring容器中     |
| @PropertySource |                用于加载properties文件中的配置                |
|     @Import     |                      用于导入其他配置类                      |

### 3.2.1 替代

1. 数据源替代

   替换配置文件加载和数据源加载

   ```java
   package com.huahua.config;
   
   import com.mchange.v2.c3p0.ComboPooledDataSource;
   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.PropertySource;
   
   import java.beans.PropertyVetoException;
   
   //<context:property-placeholder location="classpath:application.properties"/>
   @PropertySource("classpath:application.properties")
   public class DataSourceConfiguration {
       //    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
   //        <property name="driverClass" value="${jdbc.driver}"></property>
   //        <property name="jdbcUrl" value="${jdbc.url}"></property>
   //        <property name="user" value="${jdbc.username}"></property>
   //        <property name="password" value="${jdbc.password}"></property>
   //    </bean>
   
       @Value(("${jdbc.driver}"))
       private String driver;
       @Value(("${jdbc.url}"))
       private String url;
       @Value(("${jdbc.username}"))
       private String username;
       @Value(("${jdbc.password}"))
       private String password;
       @Bean("dataSource")
       public ComboPooledDataSource getDataSource() throws PropertyVetoException {
           ComboPooledDataSource dataSource = new ComboPooledDataSource();
           dataSource.setDriverClass(driver);
           dataSource.setJdbcUrl(url);
           dataSource.setUser(username);
           dataSource.setPassword(password);
           return dataSource;
       }
   }
   ```

2. 代入配置替代

   设置文核心配置类

   设置组件扫描范围

   导入其他配置

   ```java
   package com.huahua.config;
   
   import org.springframework.context.annotation.ComponentScan;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.context.annotation.Import;
   
   //标志该类时Spring的核心配置类
   @Configuration
   //组件扫描    <context:component-scan base-package="com.huahua"></context:component-scan>
   @ComponentScan("com.huahua")
   //<import resource>
   @Import({DataSourceConfiguration.class})
   public class SpringConfiguration {
   
   }
   ```

3. 测试类配置

   ```java
   package com.huahua.web;
   
   import com.huahua.config.SpringConfiguration;
   import com.huahua.service.UserService;
   import org.springframework.context.annotation.AnnotationConfigApplicationContext;
   
   public class UserController {
       public static void main(String[] args) {
   //        ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
           AnnotationConfigApplicationContext app = new AnnotationConfigApplicationContext(SpringConfiguration.class);
           UserService userService  = app.getBean(UserService.class);
           userService.save();
           app.close();
       }
   }
   ```

# 4、Spring继承Junit步骤

1. 导入spring继承Junit的坐标
2. 使用@RunWith注解替换原来的运行期
3. 使用@ContextConfiguration指定配置文件或配置类
4. 创建测试方法进行测试

## 4.1 代码实现

### 4.1.1 导包

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <version>5.3.18</version>
</dependency>
```

### 4.1.2 使用@RunWith注解替换原来的运行期

```java
@RunWith(SpringJUnit4ClassRunner.class)
```

### 4.1.3 使用@ContextConfiguration指定配置文件或配置类

```java
//@ContextConfiguration("classpath:applicationContext.xml")
@ContextConfiguration(classes = {SpringConfiguration.class})
```

### 4.1.4 创建测试方法进行测试

```java
package com.huahua;

import com.huahua.config.SpringConfiguration;
import com.huahua.service.UserService;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("classpath:applicationContext.xml")
@ContextConfiguration(classes = {SpringConfiguration.class})
public class SpringJunitTestCase {
   @Autowired
   private UserService userService;

   @Autowired
   private ComboPooledDataSource dataSources;

   @Test
   public void test1() throws SQLException {
      userService.save();
      System.out.println(dataSources.getConnection());
   }
}
```

# 5、Spring 继承web环境

## 5.1 基础web环境的集成

1. 创建maven项目

2. 导入Spring配置和jsp配置

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <parent>
           <artifactId>Spring</artifactId>
           <groupId>org.example</groupId>
           <version>1.0-SNAPSHOT</version>
       </parent>
       <modelVersion>4.0.0</modelVersion>
   
       <artifactId>spring_mvc</artifactId>
   
       <properties>
           <maven.compiler.source>8</maven.compiler.source>
           <maven.compiler.target>8</maven.compiler.target>
       </properties>
       <dependencies>
           <dependency>
               <groupId>mysql</groupId>
               <artifactId>mysql-connector-java</artifactId>
               <version>8.0.22</version>
           </dependency>
           <dependency>
               <groupId>c3p0</groupId>
               <artifactId>c3p0</artifactId>
               <version>0.9.1.2</version>
           </dependency>
           <dependency>
               <groupId>com.alibaba</groupId>
               <artifactId>druid</artifactId>
               <version>1.2.9</version>
           </dependency>
           <dependency>
               <groupId>junit</groupId>
               <artifactId>junit</artifactId>
               <version>4.13.2</version>
               <scope>test</scope>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-context</artifactId>
               <version>5.3.18</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-test</artifactId>
               <version>5.3.18</version>
           </dependency>
           <dependency>
               <groupId>javax.servlet</groupId>
               <artifactId>javax.servlet-api</artifactId>
               <version>4.0.1</version>
           </dependency>
           <dependency>
               <groupId>javax.servlet.jsp</groupId>
               <artifactId>javax.servlet.jsp-api</artifactId>
               <version>2.3.3</version>
           </dependency>
       </dependencies>
   </project>
   ```

3. 添加web模块,设置统一部署描述符，添加Spring应用程序上下文

   ![截屏2022-07-16 13.22.57](/Users/kuroyume/Spring/Spring/note/截屏2022-07-16 13.22.57.png)

   ![截屏2022-07-16 14.47.32](/Users/kuroyume/Spring/Spring/note/截屏2022-07-16 14.47.32.png)

4. 添加工件

   ![截屏2022-07-16 13.23.50](/Users/kuroyume/Spring/Spring/note/截屏2022-07-16 13.23.50.png)

5. 配置tomcat，添加部署工件

   ![截屏2022-07-16 13.24.52](/Users/kuroyume/Spring/Spring/note/截屏2022-07-16 13.24.52.png)

6. 写HttpServlet类

   ```java
   package com.huahua.web;
   
   import com.huahua.service.UserService;
   import org.springframework.context.ApplicationContext;
   import org.springframework.context.support.ClassPathXmlApplicationContext;
   
   import javax.servlet.http.HttpServlet;
   import javax.servlet.http.HttpServletRequest;
   import javax.servlet.http.HttpServletResponse;
   
   public class UserServlet extends HttpServlet {
       @Override
       protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
           ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
           UserService userService = app.getBean(UserService.class);
           userService.save();
       }
   }
   ```

7. 在模块部署描述符(web.xml)中添加相关路径

   ```xml
   <servlet>
       <servlet-name>UserServlet</servlet-name>
       <servlet-class>com.huahua.web.UserServlet</servlet-class>
   </servlet>
   <servlet-mapping>
       <servlet-name>UserServlet</servlet-name>
       <url-pattern>/userServlet</url-pattern>
   </servlet-mapping>
   ```

8. 启动tomcat测试

   浏览器中输入

   ```html
   http://localhost:8080/userServlet
   ```

9. 查看控制台输出

   ```apl
   init
   com.mysql.cj.jdbc.Driver
   UserDaoImpl.save running ...
   ```

   

## 5.2 获取应用上下文

在web项目中，可以使用ServletContextListener监听Web应用程序的启动，我们可以在web应用程序启动时，就加载Spring配置文件，创建应用程序上下文对象ApplicationContext，再将其存储到最大域servletContext域中，这样就可以在任意位置从域中获取到应用程序上下文对象了

1. 在资源部署符中添加全局初始化参数，以解耦合

```xml
<!--    全局初始化参数-->
    <context-param>
        <param-name>contextConfig</param-name>
        <param-value>applicationContext.xml</param-value>
    </context-param>
```

2.  创建应用程序上下文监听器，在监听器初始化时获取应用程序上下文并将它保存到上下文域中，程序启动时就会调用该方法

   ```java
   package com.huahua.listener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextLoaderListener implements ServletContextListener {
   @Override
   public void contextInitialized(ServletContextEvent sce) {
      //读取web.xml参数
      String contextConfigLocation = sce.getServletContext().getInitParameter("contextConfig");
      ApplicationContext app = new ClassPathXmlApplicationContext(contextConfigLocation);
      //将Sring应用上下文存储到最大域当中
      ServletContext servletContext = sce.getServletContext();
      servletContext.setAttribute("app", app);
      System.out.println("Spring容器以已经创建...");
   }

   @Override
   public void contextDestroyed(ServletContextEvent sce) {
      ServletContextListener.super.contextDestroyed(sce);
   }
}
   ```

3. 创建工具类以最低耦合度获取应用程序上下文

   ```java
   package com.huahua.listener;
   
   import org.springframework.context.ApplicationContext;
   import javax.servlet.ServletContext;
   
   public class WebApplicationContextUtils {
       public static ApplicationContext getWebApplicationContext(ServletContext servletContext){
           return (ApplicationContext) servletContext.getAttribute("app");
       }
   }
   ```

4. 在servlet中获取上下文

   ```java
   package com.huahua.web;
   
   import com.huahua.listener.WebApplicationContextUtils;
   import com.huahua.service.UserService;
   import org.springframework.context.ApplicationContext;
   
   import javax.servlet.http.HttpServlet;
   import javax.servlet.http.HttpServletRequest;
   import javax.servlet.http.HttpServletResponse;
   
   public class UserServlet extends HttpServlet {
       @Override
       protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
   ApplicationContext app = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
   UserService userService = app.getBean(UserService.class);
   userService.save();
       }
   }
   ```

## 5.3 Spring 提供的应用程序上下文工具

Spring自带的监听器名字叫做：ContextLoaderListener，

提供的客户端工具叫做：WebApplicationContextUtils

使用方法：

1. 导入spring-web包
2. 在web.xml中配置ContextLoaderListener监听器
3. 使用Web ApplicationContextUtils获取应用上下文对象ApplicationContext

5.3.1 导包

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>5.3.18</version>
</dependency>
```

5.3.2 配置web.xml

```xml
<!--    全局初始化参数-->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:applicationContext.xml</param-value>
    </context-param>

<!--    配置监听器-->
    <listener>
<!--        <listener-class>com.huahua.listener.ContextLoaderListener</listener-class>-->
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>
```

5.3.3 Servlet中调用工具类方法

```java
package com.huahua.web;

import com.huahua.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserServlet extends HttpServlet {
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
      ApplicationContext app = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
      UserService userService = app.getBean(UserService.class);
      userService.save();
   }
}
```

# 6、SpringMVC简介

SpringMVC是一种基于Java的实现MVC设计模型的请求驱动类型轻量级Web框架，属于SpringFrameWork的后续产品，已经融合在Spring Web Flow中。

Spring具有一套比较完善的注解机制

SpringMVC通过一个注解让一个简单的Java类成为处理请求的控制器，而无需任何接口，同时支持Reftful风格的请求。

M：Model、V：View、C：Controller

![image-20220716160214241](/Users/kuroyume/Spring/Spring/note/image-20220716160214241.png)

## 6.1 开发步骤：

1. 导入SpringMVC坐标
2. 配置SpringMVC核心控制器DispathcerDervlet
3. 创建Controller类和视图页面
4. 使用注解配置Controller类中业务方法的映射地址
5. 配置SpringMVC核心文件spring-mvc.xml
6. 客户端发起请求测试

代码实现： 

1.  导入坐标

   ```xml
   <dependency>
       <groupId>org.springframework</groupId>
       <artifactId>spring-webmvc</artifactId>
       <version>5.3.18</version>
   </dependency>
   ```

2. 配置SpringMVC核心控制器DispathcerDervlet

   ```xml
       <!--    配置SpringMVC的前段控制器-->
       <servlet>
           <servlet-name>DispatcherServlet</servlet-name>
           <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
   <!--        给前端控制器指定配置文件-->
           <init-param>
               <param-name>contextConfigLocation</param-name>
               <param-value>classpath:spring-mvc.xml</param-value>
           </init-param>
           <load-on-startup>1</load-on-startup>
       </servlet>
       <servlet-mapping>
           <servlet-name>DispatcherServlet</servlet-name>
           <url-pattern>/</url-pattern>
       </servlet-mapping>
   ```

3. 创建Controller类和视图页面

   ```html
   <%--
     Created by IntelliJ IDEA.
     User: kuroyume
     Date: 2022/7/16
     Time: 16:11
     Name:success.jsp
     To change this template use File | Settings | File Templates.
   --%>
   <%@ page contentType="text/html;charset=UTF-8" language="java" %>
   <html>
   <head>
       <title>Title</title>
   </head>
   <body>
   <h1>Success</h1>
   </body>
   </html>
   ```

   ```java
   package com.huahua.controller;
   
   import org.springframework.stereotype.Controller;
   import org.springframework.web.bind.annotation.RequestMapping;
   
   @Controller
   public class UserController {
   
       @RequestMapping("/quick")
       public String save(){
           System.out.println("Controller save running ...");
           return "success.jsp";
         //success前面加forward:即代表转发
         //success前面加redirect:即代表重定向
       }
   }
   ```

4. 配置SpringMVC核心文件spring-mvc.xml,对controller层进行扫描

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:context="http://www.springframework.org/schema/context"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
   <!--组件扫描-->
       <context:component-scan base-package="com.huahua.controller"></context:component-scan>
   </beans>
   ```

5. 启动，发起客户端请求

   ![截屏2022-07-16 16.21.24](/Users/kuroyume/Spring/Spring/note/截屏2022-07-16 16.21.24.png)

6. 结果

   ![截屏2022-07-16 16.21.46](/Users/kuroyume/Spring/Spring/note/截屏2022-07-16 16.21.46.png)

   控制台：Controller save running ...

## 6.2 使用时图解析器配置自定义前后缀

将自定义前后缀配置到spring-mvc.xml中

```xml
    <bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/page/"></property>
        <property name="suffix" value=".jsp"></property>
    </bean>
```

```java
return "/page/success.jsp"  ==>   return "success";

package com.huahua.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/quick")
    public String save(){
        System.out.println("Controller save running ...");
        return "success";
    }
}
```

测试：

![截屏2022-07-16 19.32.25](/Users/kuroyume/Spring/Spring/note/截屏2022-07-16 19.32.25.png)

# 7、SpringMVC的组件解析

## 7.1 SpringMVC的执行流程

1. 用户发送请求至前段控制器DispatcherServlet
2. DispatcherServlet收到请求调用HandlerMapping处理器映射器。
3. 处理器映射器找到具体的处理器(可以根据xml配置、注解进行查找)，生成处理器对象及处理器拦截器(如果有责生成)一并返回给DispatcherServlet。
4. DispatcherServlet调用HandlerAdapter处理器适配器。
5. HandlerAdapter经过适配调用具体的处理器(Controller,也好后端控制器)。
6. Controller执行完成返回ModelAndView。
7. HandlerAdapter将controller执行结果ModelAndView返回给DispatcherServlet
8. DispatcherServlet将ModelAndView传给ViewReslover视图解析器。
9. ViewReslover解析后返回具体view。
10. DispatcherServlet根据View进行渲染视图(将模型数据填充至视图中)。DispatcherServlet响应用户。

![截屏2022-07-16 17.11.06](/Users/kuroyume/Spring/Spring/note/截屏2022-07-16 17.11.06.png)

## 7.2 SpringMVC的注解解析

**@RequestMapping**

作用：用于建立请求URL和处理方法之间的对应关系

位置：

- 类上，请求URL的第一级访问目录。不写则相当于根目录/
- 方法上，请求URL的第二级访问目录，与类上的使用**@RequestMapping**标注的一级目录组成访问路径

属性：

- value：用于指定请求的URL。它和path属性的作用相同，也是默认属性
- method：用于指定请求方式
- params：用于指定限制请求参数的条件。它支持简单的表达式。要求请求参数的key和value必须和配置的一摸一样。eg、params={"accountName"},表示请求参数必须有accountName，params={"money!100"}，表示请求参数money不能是100

# 8、SpringMVC的数据响应

## 8.1 SpringMVC的数据响应方式

1） 页面跳转

- 直接返回字符串
- 通过ModelAndView对象返回

2） 返回数据

- 直接返回字符串
- 直接返回对象或集合

## 8.2 页面跳转---直接返回字符串 

  见第六节SpringMVC简介

## 8.3 页面跳转--通过ModelAndView

设置视图模型的视图名称与数据模型

```java
    @RequestMapping("/quick2")
    public ModelAndView save2(){
//        Model: 模型 封装数据
//        View: 视图 展示数据
        System.out.println("Controller save2 is running ...");
        ModelAndView modelAndView = new ModelAndView();
        //设置数据模型
        modelAndView.addObject("username","itcast");
        //设置视图名称
        modelAndView.setViewName("success");
        return modelAndView;
    }
		//SpringMVC自动检测并注入对象参数
 		@RequestMapping("/quick3")
    public ModelAndView save3(ModelAndView modelAndView){
        modelAndView.addObject("username","huahua");
        modelAndView.setViewName("success");
        return modelAndView;
    }

		//第三种形式，与save2原理相同，只是将Model和View分开了 
		@RequestMapping("/quick4")
    public String save4(Model model){
        model.addAttribute("username","save4");
        return "success";
    }

   //使用原始方式往域中存数据
    //Spring对原生的JavaWeb产生的域也能自动注入，但不常用
    @RequestMapping("/quick5")
    public String save5(HttpServletRequest request){
        request.setAttribute("username","save5");
        return "success";
    }
```

在/page/下创建对应名称的视图，接收数据

```html
<%--
  Created by IntelliJ IDEA.
  User: kuroyume
  Date: 2022/7/16
  Time: 16:11
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<h1>Success!</h1>
<h1>${username}</h1>
</body>
</html>
```

测试：

![截屏2022-07-17 10.39.12](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 10.39.12.png)

![截屏2022-07-17 10.43.11](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 10.43.11.png)

![截屏2022-07-17 10.53.25](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 10.53.25.png)

![image-20220717105402434](/Users/kuroyume/Spring/Spring/note/image-20220717105402434.png)



## 8.4 回写数据--直接返回字符串

1. 通过SpringMVC框架注入的reponse对象，使用response.getWriter().print("hello word")回写数据，此时不需要视图跳转，业务返回值为void。

   ```java
   @RequestMapping("/quick6")
   public void save6(HttpServletResponse response) throws IOException {
       response.getWriter().print("hello http save6");
   }
   ```

   **![截屏2022-07-17 11.02.37](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 11.02.37.png)**

2. 将需要回写的字符串直接返回，但此时需要通过@ResponseBody注解告知SpringMVC框架，方法返回的字符串不是跳转是直接在http响应体中返回。

   ```java
   @RequestMapping("/quick7")
   @ResponseBody//告知SpringMVC框架，不跳转视图 直接进行数据响应。
   public String save7() {
       return "hello http save7";
   }
   
   		//返回的Json字符串，需要导入Json相关配置	
       @RequestMapping("/quick8")
       @ResponseBody
       public String save8() throws JsonProcessingException {
           User user = new User();
           user.setUsername("zhangsan");
           user.setAge(18);
           //使用Json的转换工具将对象转换成Json格式的字符串
           ObjectMapper objectMapper = new ObjectMapper();
           return         objectMapper.writeValueAsString(user);
   //相当于       return "{\"username\":\"zhangsan\",\"age\":18}";
       }
   ```

   ```xml
   <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-core</artifactId>
       <version>2.13.3</version>
   </dependency>
   <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-databind</artifactId>
       <version>2.13.3</version>
   </dependency>
   <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-annotations</artifactId>
       <version>2.13.3</version>
   </dependency>
   ```

   ![截屏2022-07-17 11.08.54](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 11.08.54.png)

   

## 8.5 回写数据--返回对象或集合

将对象或集合转换成Json字符串惊醒数据回写

```xml
<!--    配置处理器映射器,设置消息处理，将对象转换为Json串,位置Spring-mvc.xml-->

    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
        <property name="messageConverters">
            <list>
                <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"/>
            </list>
        </property>
    </bean>
```

直接返回对象或集合

```java
  @RequestMapping("/quick9")
  @ResponseBody
  public User save9() {
      User user = new User();
      user.setUsername("zhangsan");
      user.setAge(18);
      return user;
  }

    @RequestMapping("/quick10")
    @ResponseBody
    public List<User> save10() {
        User user = new User();
        user.setUsername("张三");
        user.setAge(18);
        List<User> list = new ArrayList<>();
        list.add(user);
        list.add(user);
        return list;
    }
```

测试：

![截屏2022-07-17 12.04.54](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 12.04.54.png)

![截屏2022-07-17 12.07.57](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 12.07.57-8030919.png)

注：上述mvc配置已被封装，可使用mvc注解驱动替换

```xml
<!--    mvc的注解驱动-->
    <mvc:annotation-driven/>
```

# 9、SpringMVC获得请求数据

## 9.1 获得请求参数

客户端请求参数格式：name=value&name2=value2... ...

服务器端要获得请求参数，有时候还需要进行数据的封装，SpringMVC可以接收如下类型的参数：

- 基本类型参数
- POJO(简单java Bean)类型参数
- 数组类型参数
- 集合类型参数

## 9.2 获得基本类型参数

Conteoller中的业方法要与请求参数的那么一致，参数值会自动映射匹配。

```java
@RequestMapping("/quick11")
@ResponseBody
public User save11(String username,int age) {
    return new User(username,age);
}
```

测试：

![截屏2022-07-17 14.55.04](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 14.55.04.png)

## 9.3 获得POJO类型参数

Spring会自动将解析的参数封装并注入到形参里面，只需打印验证即可

```java
@RequestMapping("/quick12")
@ResponseBody
public User save12(User user) {
    return user;
}
```

测试：

![截屏2022-07-17 15.29.05](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 15.29.05.png)

## 9.4 获得数组类型参数

Controller中的业务方法数组名称与请求参数的name一致，参数值会自动映射匹配。

```java
@RequestMapping("/quick13")
@ResponseBody
public List<String> save13(String[] strs) {
    return Arrays.asList(strs);
}
```

测试：

![截屏2022-07-17 15.50.42](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 15.50.42.png)

## 9.5 获得集合类型参数

1.需将集合参数包装到一个POJO中才可以获取参数

创建POJO

```java
package com.huahua.domain;

import java.util.List;

public class VO {
   private List<User> userList;

   public List<User> getUserList() {
      return userList;
   }

   public void setUserList(List<User> userList) {
      this.userList = userList;
   }
}
```

写接受方法

```java
@RequestMapping("/quick14")
@ResponseBody
public VO save14(VO vo) {
    System.out.println(vo);
    return vo;
}
```

创建表单提交数据

```html
<%--
  Created by IntelliJ IDEA.
  User: kuroyume
  Date: 2022/7/17
  Time: 15:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/user/quick14" method="post">
<%--    表明是第几个User对象的Username--%>
    <input type="text" name="userList[0].username"><br>
    <input type="text" name="userList[0].age"><br>
    <input type="text" name="userList[1].username"><br>
    <input type="text" name="userList[1].age"><br>
    <input type="text" name="userList[2].username"><br>
    <input type="text" name="userList[2].age"><br>
    <input type="submit" value="提交">
</form>

</body>
</html>
```

测试：

![截屏2022-07-17 16.09.37](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 16.09.37.png)

![截屏2022-07-17 16.09.54](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 16.09.54.png)

2.当使用ajax提交请求时，可以指定contentType为json形式，那么在方法参数位置使用@RequestBody可以直接接收集合数据而无需使用POJO进行包装。

1. 创建页面使用ajax发送json内容形式的请求

   ```html
   <%--
     Created by IntelliJ IDEA.
     User: kuroyume
     Date: 2022/7/17
     Time: 16:23
     To change this template use File | Settings | File Templates.
   --%>
   <%@ page contentType="text/html;charset=UTF-8" %>
   <html>
   <head>
       <title>Title</title>
       <script src="${pageContext.request.contextPath}/js/jquery.js"></script>
       <script>
           var userList = new Array();
           userList.push({username:"zhangsan",age:18})
           userList.push({username:"lisi",age:29})
   
           $.ajax({
               type:"POST",
               url:"${pageContext.request.contextPath}/user/quick15",
               data:JSON.stringify(userList),
               contentType:"application/json;charset=utf-8"
           });
       </script>
   </head>
   <body>
   
   </body>
   </html>
   ```

2. 通过<<mvc:resources/>>标签，配置一个Handler来处理静态资源,使jquery.js可访问到

   ```xml
   <!--开放静态资源的访问权限-->
   <mvc:resources mapping="/js/**" location="/js/"/>
   ```

3. 写接受方法，使用RequestBody注释修饰形参

   ```java
   @RequestMapping("/quick15")
   @ResponseBody
   public void save15(@RequestBody List<User> userList) {
       System.out.println(userList);
   }
   ```

4. 测试

   访问[Title](http://localhost:8080/ajax.jsp)

   查看服务器控制台输出：[User{username='zhangsan', age=18}, User{username='lisi', age=29}]则正常

注：<<mvc:resources/>>标签也可以使用<<mvc:default-servlet-handler/>>来代替,作用时将资源访问工作交给原始容器tomcat。

## 9.6 请求数据中文乱码问题

当post请求时，数据会出现乱码，我们可以设置一个过滤器来惊醒编码的过滤

## ![截屏2022-07-17 16.59.34](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 16.59.34.png)

![截屏2022-07-17 16.59.34](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 17.00.43.png)

1. 配置过滤器

   ```xml
   <!--    配置一个全局的过滤器filter-->
       <filter>
           <filter-name>CharacterEncodingFilter</filter-name>
           <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
           <init-param>
               <param-name>encoding</param-name>
               <param-value>UTF-8</param-value>
           </init-param>
       </filter>
       <filter-mapping>
           <filter-name>CharacterEncodingFilter</filter-name>
           <url-pattern>/*</url-pattern>
       </filter-mapping>
   ```

2. 测试

   ![截屏2022-07-17 17.06.49](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 17.06.49.png)

## 9.7 参数绑定注解@RequestParam

当请求参数的名称与Controller的业务方法参数不一致时，需要@RequestParam注解显式绑定。

@RequestParam有如下参数：

- value:写请求参数的名称，为默认参数
- required:定义是否必须包括该参数，当为默认值true时，若提交时没有该参数则会报错
- defaultValue：当没有指定请求参数时，则使用指定的默认赋值

```java
@RequestMapping("/quick16")
@ResponseBody
public String save16(@RequestParam(value = "name",required = false,defaultValue = "huahua") String username) {
    return username;
}
```

![截屏2022-07-17 17.18.48](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 17.18.48.png)

![截屏2022-07-17 17.19.40](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 17.19.40.png)

## 9.8 获取Restful风格的参数

Restful是一种软件架构风格，设计风格，而不是标准，只是提供了一组设计原则和约束条件。主要用于客户端和服务器交互类的软件，基于这个风格设计的软件可以更简洁，更有层次，更易于实现缓存机制等。

Restful风格的请求时使用“url+请求方式”表示一次请求目的，HTTP里面四个表示操作的请求方式如下：

- GET：用于获取资源
- POST：用于新建资源
- PUT：用于更新资源
- DELETE：用于删除资源

举例：

- /user/1 GET:获取id为1的user
- /user/1 DELETE: 删除id=1的user
- /user/1 PUT：更新id=1的user
- /user POST：新增user 

在SpringMVC中可以使用占位符绑定参数。地址/user/1可以写成/user/{id}。在业务方法中可以使用@PathVariable注解进行占位符的匹配。



实现：

```java
@RequestMapping("/quick17/{username}")
@ResponseBody
public String save17(@PathVariable("username") String username) {
    return username;
}
```

![截屏2022-07-17 17.38.54](/Users/kuroyume/Spring/Spring/note/截屏2022-07-17 17.38.54.png)

## 9.8 自定义类型转换器

1. SpringMVC默认已经提供了一些常用的类型转换器，例如客户端提交的字符串转换成int型进行参数设置。

2. 但是不是所有的数据类型都提供了转换器，没有提供的就需要自定义类型转换器，例如：日期类型的数据需要自定义转换器。

3. 自定义类型转换器步骤：

   1. 定义转换器类实现Converter接口

      ```java
      package com.huahua.converter;
      
      import org.springframework.core.convert.converter.Converter;
      
      import java.text.ParseException;
      import java.text.SimpleDateFormat;
      import java.util.Date;
      
      public class DateConverter implements Converter<String,Date> {
      
          @Override
          public Date convert(String source) {
              SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
              Date date = null;
              try {
                 date = format.parse(source);
              } catch (ParseException e) {
                  e.printStackTrace();
              }
              return date;
          }
      }
      ```

   2. 在配置文件中声明转换器

      ```xml
      <!--    声明转换器-->
          <bean id="myConversionService" class="org.springframework.context.support.ConversionServiceFactoryBean">
              <property name="converters">
                  <list>
                      <bean class="com.huahua.converter.DateConverter"/>
                  </list>
              </property>
          </bean>
      ```

   3. 在<annotation-driven/>中引用转换器

      ```xml
      <mvc:annotation-driven conversion-service="myConversionService"/>
      ```

测试：

```java
@RequestMapping("/quick18")
@ResponseBody
public Date save18(Date date) {
    System.out.println(date);
    return date;
}
```

输入url

```html
http://localhost:8080/user/quick18?date=2010-12-21
```

控制台输出：

```java
Tue Dec 21 00:00:00 CST 2010
```

## 9.10 获得servlet相关API

SpringMVC支持使用原始ServletAPI对象作为控制器方法的参数进行注入，常用对象如下：

- HttpServletRequest
- HttpServletResponse
- HttpSession

使用时在Conteroller业务方法上添加相关形参即可，SpringMVC会进行自动注入

```java
@RequestMapping("/quick19")
@ResponseBody
public void save19(HttpServletResponse response, HttpServletRequest request, HttpSession session) {
    System.out.println(response);
    System.out.println(request);
    System.out.println(session);
}
```

测试：

[localhost:8080/user/quick19](http://localhost:8080/user/quick19)

org.apache.catalina.connector.ResponseFacade@45110429
org.apache.catalina.connector.RequestFacade@2e923167
org.apache.catalina.session.StandardSessionFacade@7b08c223

## 9.11 获取请求头

1. @RequestHeader

   使用@RequestHeader可以获得请求头信息，相当于web阶段学习的request.getHeader(name)

   注解属性如下：

   - value：请求头的名称
   - requested：是否必须携带此请求头

   

2. @CookieValue

   使用@CookieValue可以获得指定Cookie的值

   @CookieValue注解的属性如下：

   - value：指定Cookie的名称
   - requires：是否必须携带此请求头

3. 

@RequestHeader使用：

http://localhost:8080/user/quick20

```apl
1.General:
Request URL: http://localhost:8080/user/quick20
Request Method: GET
Status Code: 200 
Remote Address: [::1]:8080
Referrer Policy: strict-origin-when-cross-origin

2.ResponseHeaders:
Connection: keep-alive
Content-Length: 0
Date: Mon, 18 Jul 2022 06:48:53 GMT
Keep-Alive: timeout=20

3.RequestHeaders:
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
Accept-Encoding: gzip, deflate, br
Accept-Language: zh-CN,zh;q=0.9
Cache-Control: max-age=0
Connection: keep-alive
Cookie: JSESSIONID=F7BCD71F87DC70CB86422693D585B950
Host: localhost:8080
sec-ch-ua: ".Not/A)Brand";v="99", "Google Chrome";v="103", "Chromium";v="103"
sec-ch-ua-mobile: ?0
sec-ch-ua-platform: "macOS"
Sec-Fetch-Dest: document
Sec-Fetch-Mode: navigate
Sec-Fetch-Site: none
Sec-Fetch-User: ?1
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36

```

```java
@RequestMapping("/quick20")
@ResponseBody
public void save20(@RequestHeader(value = "User-Agent",required = false) String User_Agent) {
    System.out.println(User_Agent);
}
```

服务器控制台打印：

```
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36
```

@CookieValue使用：

```java
@RequestMapping("/quick21")
@ResponseBody
public void save21(@CookieValue(value = "JSESSIONID") String jssessionid,@RequestHeader(value = "Cookie",required = false) String cookie) {
    System.out.println(jssessionid);
    System.out.println(cookie);
}
```

访问:http://localhost:8080/user/quick21

服务器控制台打印：

```apl
5B67ABD5183C454DCBDEAA44DE627757
JSESSIONID=5B67ABD5183C454DCBDEAA44DE627757
```

## 9.12 文件上传

1. 文件上传客户端三要素
   - 表单type="file"
   - 表单的提交方式post
   - 表单的enctype属性时多部分表单形式，及enctype="multipart/form-data"
2. 文件上传原理
   - 当form表单修改为多部分表单时，request.getParameter将失效
   - enctype="application/x-www-from-urlencoded"时，form表单的正文内容格式是：key=value&key=value&key=value
   - 当from表单的enctype取值为Mutilpart/from-data时，请求正文内容就变成多部分形式

3. 单文件上传步骤
   1. 导入fileupload和io坐标
   2. 配置文件上传解析器
   3. 编写文件上传代码
4. 

导入fileupload和io坐标

```xml
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.4</version>
</dependency>
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.6</version>
</dependency>
```

配置文件上传解析器

```xml
<!--    配置文件上传解析器-->
    <bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <property name="defaultEncoding" value="UTF-8"/>
        <property name="maxUploadSize" value="52428800"/>
        <property name="maxUploadSizePerFile" value="5242880"/>
    </bean>
```

编写文件上传代码,注接收内容的形参名称需要和表单中标签的名称一致

```java
@RequestMapping("/quick22")
@ResponseBody
public void save22(String username, MultipartFile uploadFile) throws IOException {
    String originalFilename = uploadFile.getOriginalFilename();
    uploadFile.transferTo(new File("/Users/kuroyume/Spring/uploadFileStore/"+originalFilename));
}
```

```html
<%--
  Created by IntelliJ IDEA.
  User: kuroyume
  Date: 2022/7/18
  Time: 15:06
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>upload</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/user/quick22" method="post" enctype="multipart/form-data">
    名称：<input type="text" name="username"><br>
    文件：<input type="file" name="uploadFile"><br>
    <input type="submit" value="upload">
</form>
</body>
</html>
```

测试：

![截屏2022-07-18 16.05.28](/Users/kuroyume/Spring/Spring/note/截屏2022-07-18 16.05.28.png)

![截屏2022-07-18 16.06.48](/Users/kuroyume/Spring/Spring/note/截屏2022-07-18 16.06.48.png)

## 9.13 多文件上传

以数组形式接收：

```java
@RequestMapping("/quick23")
@ResponseBody
public void save23(String username, MultipartFile[] uploadFile) throws IOException {
    System.out.println(username);
    for (MultipartFile multipartFile:uploadFile) {
        multipartFile.transferTo(new File("/Users/kuroyume/Spring/uploadFileStore/"+multipartFile.getOriginalFilename()));
    }
}
```

表单以相同名称上传

```html
<h1>quick23</h1>
<form action="${pageContext.request.contextPath}/user/quick23" method="post" enctype="multipart/form-data">
    名称：<input type="text" name="username"><br>
    文件：<input type="file" name="uploadFile"><br>
    <input type="file" name="uploadFile"><br>
    <input type="submit" value="upload">
</form>
```

测试：

![截屏2022-07-18 16.15.36](/Users/kuroyume/Spring/Spring/note/截屏2022-07-18 16.15.36.png)

![截屏2022-07-18 16.15.52](/Users/kuroyume/Spring/Spring/note/截屏2022-07-18 16.15.52.png)

# 10、Spring jdbcTemplate基本使用

1. 概述

   是Spring框架中提供的一个对象，是原始繁琐的Jdbc API对象的简单封住。Spring框架为我们提供了很多操作模版类。例如：操作关系型数据的Jdbc Template和Hibernatetemplate，操作nosql数据库的RedisTemplate，操作消息队列的JmsTemplate等等。

2. 开发步骤

   - 导入spring-jdbc和spring-tx坐标
   
     ```xml
     <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-jdbc</artifactId>
         <version>5.3.18</version>
     </dependency>
     <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-tx</artifactId>
         <version>5.3.18</version>
     </dependency>
     ```
   
   - 创建数据库表和实体

     ```java
     package com.huahua.domain;
     
     public class Account {
         private String name;
         private double money;
     
         @Override
         public String toString() {
             return "Account{" +
                     "name='" + name + '\'' +
                     ", money=" + money +
                     '}';
         }
     
         public String getName() {
             return name;
         }
     
         public void setName(String name) {
             this.name = name;
         }
     
         public double getMoney() {
             return money;
         }
     
         public void setMoney(double money) {
             this.money = money;
         }
     }
     ```
   
   - 创建jdbcTemplate对象
   
   - 执行数据库操作
   
     ```java
     @Test
     public void test1() throws PropertyVetoException {
         //创建数据源对象
         ComboPooledDataSource dataSource = new ComboPooledDataSource();
         dataSource.setDriverClass("com.mysql.cj.jdbc.Driver");
         dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/demo");
         dataSource.setUser("root");
         dataSource.setPassword("root");
         //创建模版对象
         JdbcTemplate jdbcTemplate = new JdbcTemplate();
         //设置数据源
         jdbcTemplate.setDataSource(dataSource);
         //执行操作
         int row = jdbcTemplate.update("insert into account values(?,?)", "tom", "5000");
         System.out.println(row);
     }
     ```
   
     
   
   ![截屏2022-07-19 09.00.10](/Users/kuroyume/Spring/Spring/note/截屏2022-07-19 09.00.10.png)

## 10.1 配置JDBC模版对象

1. 在xml文件中配置jdbc模版对象

   ```java
   <!--    加载外部的propertis文件-->
       <context:property-placeholder location="classpath:application.properties"/>
   <!--    配置数据源对象-->
       <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
           <property name="driverClass" value="${jdbc.driver}"/>
           <property name="jdbcUrl" value="${jdbc.url}"/>
           <property name="user" value="${jdbc.username}"/>
           <property name="password" value="${jdbc.password}"/>
       </bean>
   <!--    jdbc模版对象-->
       <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
           <property name="dataSource" ref="dataSource"/>
       </bean>
   ```

2. 在配置类中配置jdbc模版对象

```java
@Autowired
ComboPooledDataSource dataSource;
@Bean("jdbcTemplate")
public JdbcTemplate getJdbcTemplate(){
    JdbcTemplate jdbcTemplate = new  JdbcTemplate();
    jdbcTemplate.setDataSource(dataSource);
    return  jdbcTemplate;
}
```

导入配置类到主类

```xml
<!--    配置组件扫描用于注解开发-->
<context:component-scan base-package="com.huahua"></context:component-scan>
```

```java
@Import({DataSourceConfiguration.class,JdbcTemplateConfiguration.class})
public class SpringConfiguration {

}
```

```java
    @Test
    //测试Spring产生jdbc模版对象
    public void test2() {
//        ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        ApplicationContext app = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        JdbcTemplate jdbcTemplate = app.getBean(JdbcTemplate.class);
        //执行操作
        int row = jdbcTemplate.update("insert into account values(?,?)", "Jhansi", "8000");
        System.out.println(row);
    }
```

## 10.2 jdbc模版的基本使用

1. 导入spring-jdbc和spring-tx包坐标
2. 创建数据库和实体
3. 创建Jdbc模版对象
4. 执行数据库操作

```java
package com.huahua;

import com.huahua.domain.Account;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JdbcTemplateCRUDTest {

   @Autowired
   private JdbcTemplate jdbcTemplate;

   @Test
   public void testUpdate() {
      jdbcTemplate.update("update account set money=? where name=?", 1000, "tom");
   }

   @Test
   public void testDelete() {
      jdbcTemplate.update("delete from account where money > ?", 1000);
   }


   @Test
   //查询
   public void testQuery() {
      //通过实体属性的行映射
      BeanPropertyRowMapper<Account> rowMapper = new BeanPropertyRowMapper<Account>(Account.class);
      //查询全部
      List<Account> allAccount = jdbcTemplate.query("select * from account", rowMapper);
      System.out.println(allAccount);
      //查询一条
      Account account = jdbcTemplate.queryForObject("select * from account where name=?", rowMapper, "tom");
      System.out.println(account);
      //查询总条数，简单类型指定方法即可
      Long count = jdbcTemplate.queryForObject("select count(*) from account", Long.class);
      System.out.println(count);
   }

}
```

# 11、Spring的小Demo(spring_demo)

## 11.1 Spring联系环境搭建

1. 创建工程(Project&Model)
2. 导入静态页面
3. 导入需要的依赖
4. 创建包结构
5. 导入数据库脚本
6. 创建POJO类
7. 创建配置文件(applicationContext.xml,spring-mvc.xml,jdbc.properties,log4j.proper)

## 11.2 角色列表的展示步骤分析

1. 点击角色管理菜单发送请求到服务器端(修改角色管理菜单的url地址) 
2. 创建RoleController和showList()方法
3. 创建RoleService和showList()方法
4. 创建RoleDao和findAll()方法
5. 使用JdbcTemplate完成查询操作 
6. 将查询数据存储到Model中
7. 转发到role-list.jsp页面进行展示

# 12、SpringMVC拦截器

SpringMVC拦截器类似于Servlet开发中的过滤器Filter，用于对处理器进行预处理和后处理。

将拦截器按一定顺序联结成一条链，这条链称为拦截器链(interceptor Chain)。在访问拦截的方法或字段时，拦截器链中的拦截器就会按其之前定义的顺序被调用。拦截器也是AOP思想的具体实现。

## 12.1 拦截器和过滤器的区别

|   区别   |                       过滤器(Filter)                       | 拦截器(Interceptor)                                          |
| :------: | :--------------------------------------------------------: | ------------------------------------------------------------ |
| 使用范围 |  是 servlet 规范中的一部分，任何 Java Web 工程都可以使用   | 是 SpringMVC 框架自己的，只有使用了 SpringMVC 框架的工程才能用 |
| 拦截范围 | 在 url-pattern 中配置了/*之后， 可以对所有要访问的资源拦截 | 在<mvc:mapping path=“”/>中配置了/**之 后，也可以多所有资源进行拦截，但 是可以 通 过<mvc:exclude-mapping path=“”/>标签 排除不需要拦截的资源 |

## 12.3   自定义拦截器

  自定义拦截器很简单，只有如下三步:

1. 创建拦截器类实现HandlerInterceptor接口 
2. 配置拦截器
3. 测试拦截器的拦截效果

创建拦截器类实现HandlerInterceptor接口

```java
package com.huahua.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyInterceptor1 implements HandlerInterceptor {
   //在目标方法执行之前 执行
   @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException {
      System.out.println("preHandle.....");
      if ("yes".equals(request.getParameter("param"))) {
         return true;
      } else {
         request.getRequestDispatcher("/error.jsp").forward(request, response);
         return false;
      }
   }

   //在目标方法执行之后 视图返回之前执行，可以获取Controller的视图模型，进一步加工
   @Override
   public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
      modelAndView.addObject("name", "byPostHandle");
      System.out.println("postHandle...");
      HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
   }

   //在整个流程都执行完毕之后做收尾工作
   @Override
   public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
      System.out.println("afterCompletion...");
      HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
   }
}
```

在xml核心配置文件中配置拦截器：

```xml
<!--    配置拦截器-->
<mvc:interceptors>
   <mvc:interceptor>
      <mvc:mapping path="/**"/>
      <bean class="com.huahua.interceptor.MyInterceptor1"/>
   </mvc:interceptor>
</mvc:interceptors>
```

在核心配置类中配置拦截器：

```java
package com.huahua.config;

import com.huahua.interceptor.MyInterceptor1;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class InterceptorConfiguration implements WebMvcConfigurer {
   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      WebMvcConfigurer.super.addInterceptors(registry);
      HandlerInterceptor interceptor = new MyInterceptor1();
      registry.addInterceptor(interceptor).addPathPatterns("/**").excludePathPatterns("/user/**");
   }
}
```

```java
package com.huahua.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

//标志该类时Spring的核心配置类
@Configuration
//组件扫描    <context:component-scan base-package="com.huahua"></context:component-scan>
@ComponentScan("com.huahua")
//<import resource>
@Import({DataSourceConfiguration.class, JdbcTemplateConfiguration.class, InterceptorConfiguration.class})
public class SpringConfiguration {

}
```

测试用的控制器类：

```java
package com.huahua.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TargetController {

   @RequestMapping("/target")
   public ModelAndView show() {
      System.out.println("目标资源执行。。。");
      ModelAndView modelAndView = new ModelAndView();
      modelAndView.setViewName("index1");
      modelAndView.addObject("name", "target");
      return modelAndView;
   }
}
```

测试用的视图：

```html
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>
<h1><%= "Hello World!" %>
</h1>
<br/>
<h1>${name}</h1><br>
<h1>${postHandle}</h1>
</body>
</html>
```

```html
<%--
  Created by IntelliJ IDEA.
  User: kuroyume
  Date: 2022/7/20
  Time: 15:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>error</title>
</head>
<body>
<h1>error!!!!</h1>
</body>
</html>
```

测试：

![截屏2022-07-20 19.13.28](/Users/kuroyume/Spring/Spring/note/截屏2022-07-20 19.13.28.png)

![截屏2022-07-20 19.13.50](/Users/kuroyume/Spring/Spring/note/截屏2022-07-20 19.13.50.png)

## 12.4 拦截器方法说明

|      方法名       | 说明                                                         |
| :---------------: | :----------------------------------------------------------- |
|    preHandle()    | 方法将在请求处理之前进行调用，该方法的返回值是布尔值Boolean类型的， 当它返回为false 时，表示请求结束，后续的Interceptor 和Controller 都不会再执行;当返回值为true 时就会继续调用下一个Interceptor 的preHandle 方法 |
|   postHandle()    | 该方法是在当前请求进行处理之后被调用，前提是preHandle方法的返回值为 true 时才能被调用，且它会在DispatcherServlet 进行视图返回渲染之前被调用，所以我们可以在这个方法中对Controller 处理之后的ModelAndView 对象进行操作 |
| afterCompletion() | 该方法将在整个请求结束之后，也就是在DispatcherServlet 渲染了对应的视图之后执行，前提是preHandle 方法的返回值为true 时才能被调用 |

## 12.5 给Demo项目添加登录拦截器

session中不包含用户字段就判为未登录，重定向到登录页面

```java
package com.huahua.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PrivilegeInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断session中有没有user
        HttpSession session = request.getSession();
        Object user = session.getAttribute("user");
        if (user==null){
            response.sendRedirect(request.getContextPath()+"/login.jsp");
            return false;
        }
        //放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
```

放行css，img，plugins等静态资源和登录接口

```xml
<mvc:interceptors>
    <mvc:interceptor>
        <mvc:mapping path="/**/*"/>
        <mvc:exclude-mapping path=" /css/**"/>
        <mvc:exclude-mapping path="/img/**"/>
        <mvc:exclude-mapping path="/plugins/**"/>
        <mvc:exclude-mapping path="/user/login"/>
        <bean class="com.huahua.interceptor.PrivilegeInterceptor"/>
    </mvc:interceptor>
</mvc:interceptors>
```

# 13、Spring异常处理机制

## 1.1 异常处理思路

系统中异常包括两类:预期异常和运行时异常RuntimeException，前者通过捕获异常从而获取异常信息，后 者主要通过规范代码开发、测试等手段减少运行时异常的发生。

系统的Dao、Service、Controller出现都通过throws Exception向上抛出，最后由SpringMVC前端控制器交 由异常处理器进行异常处理，如下图:

![截屏2022-07-21 08.33.12](/Users/kuroyume/Spring/Spring/note/截屏2022-07-21 08.33.12.png)

## 1.2 异常处理的两种方式

- 使用Spring MVC提供的简单异常处理器SimpleMappingExceptionResolver
- 实现Spring的异常处理接口HandlerExceptionResolver 自定义自己的异常处理器

## 1.3 配置简单映射异常处理器

SpringMVC已经定义好了该类型转换器，在使用时可以根据项目情况进行相应异常与视图的映射配置

```xml
<!--    配置简单映射异常处理器-->
<bean class="org.springframework.web.servlet.handler.SimpleMappingExceptionResolver">
   <!--        <property name="defaultErrorView" value="excErr"/>-->
   <property name="exceptionMappings">
      <map>
         <entry key="java.lang.ClassCastException" value="excErr-typeConversion"/>
         <entry key="com.huahua.exception.MyException" value="excErr-customize"/>
      </map>
   </property>
</bean>
```

可以配置一个通用的异常页面，将所有的异常都指向该页面，也可以做简单映射，让指定异常指向指定页面。

```java
package com.huahua.exception;

public class MyException extends Exception {
}
```

```java
package com.huahua.service.Impl;

import com.huahua.exception.MyException;
import com.huahua.service.ExceptionDemoService;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Service
public class ExceptionDemoServiceImpl implements ExceptionDemoService {
   @Override
   public void show1() {
      System.out.println("抛出类型转换异常....");
      Object str = "zhangsan";
      Integer num = (Integer) str;
   }

   @Override
   public void show2() {
      System.out.println("抛出除零异常....");
      int i = 1 / 0;
   }

   @Override
   public void show3() throws FileNotFoundException {
      System.out.println("文件找不到异常");
      InputStream inputStream = new FileInputStream("/xx/XX/XX");
   }

   @Override
   public void show4() {
      System.out.println("空指针异常");
      String str = null;
      str.length();
   }

   @Override
   public void show5() throws MyException {
      System.out.println("自定义异常");
      throw new MyException();
   }
}
```

访问不太测试方法，返回不同视图

```java
package com.huahua.controller;

import com.huahua.exception.MyException;
import com.huahua.service.ExceptionDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExceptionDemoController {

   @Autowired
   ExceptionDemoService exceptionDemoService;

   @RequestMapping("/show")
   public String show() throws MyException {
      System.out.println("show running...");
//        exceptionDemoService.show1();
      exceptionDemoService.show5();
      return "index";
   }
}
```

测试：

Show5

![截屏2022-07-21 10.15.20](/Users/kuroyume/Spring/Spring/note/截屏2022-07-21 10.15.20.png)

Show1

![截屏2022-07-21 10.16.28](/Users/kuroyume/Spring/Spring/note/截屏2022-07-21 10.16.28.png)

## 1.4 自定义异常处理步骤

1. 创建异常处理器类实现HandlerExceptionResolver 
2. 配置异常处理器
3. 编写异常页面
4. 测试异常跳转

```java
package com.huahua.resolver;

import com.huahua.exception.MyException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyExceptionResolver implements HandlerExceptionResolver {
   /*
   参数Exception：是异常对象
   返回值ModelAndView：跳转到错误视图信息
    */
   @Override
   public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
      ModelAndView modelAndView = new ModelAndView();
      if (ex instanceof MyException) {
         modelAndView.addObject("info", "自定义异常");
      } else if (ex instanceof ClassCastException) {
         modelAndView.addObject("info", "类型转换异常");
      }
      modelAndView.setViewName("excErr");
      return modelAndView;
   }
}
```

```java
package com.huahua.controller;

import com.huahua.exception.MyException;
import com.huahua.service.ExceptionDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExceptionDemoController {

   @Autowired
   ExceptionDemoService exceptionDemoService;

   @RequestMapping("/show1")
   public String show1() throws MyException {
      System.out.println("show1 running...");
      exceptionDemoService.show1();
      return "index";
   }

   @RequestMapping("/show5")
   public String show5() throws MyException {
      System.out.println("show5 running...");
      exceptionDemoService.show5();
      return "index";
   }
}
```

```xml
<!--    自定义异常处理器-->
<bean class="com.huahua.resolver.MyExceptionResolver"/>
```

测试：

![截屏2022-07-21 10.36.59](/Users/kuroyume/Spring/Spring/note/截屏2022-07-21 10.36.59.png)

![截屏2022-07-21 10.37.16](/Users/kuroyume/Spring/Spring/note/截屏2022-07-21 10.37.16.png)

# 14、SpringAOP

## 14.1 简介

AOP 为 Aspect Oriented Programming 的缩写，意思为面向切面编程，是通过预编译方式和运行期动态代理实现程序功能的统一维护的一种技术。

AOP 是 OOP 的延续，是软件开发中的一个热点，也是Spring框架中的一个重要内容，是函数式编程的一种衍生范型。利用AOP可以对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的耦合度降低，提高程序的可重用性，同时提高了开发的效率。

## 14.2 AOP的作用及优势

- 作用:在程序运行期间，在不修改源码的情况下对方法进行功能增强 （如，日志控制增强）
- 优势:减少重复代码，提高开发效率，并且便于维护

## 14.3 AOP的底层实现

实际上，AOP 的底层是通过 Spring 提供的的动态代理技术实现的。在运行期间，Spring通过动态代理技术动态的生成代理对象，代理对象方法执行时进行增强功能的介入，在去调用目标对象的方法，从而完成功能的增强。

## 14.4 AOP的动态代理技术

- JDK 代理 : 基于接口的动态代理技术

- cglib代理:基于父类的动态代理技术

  ![截屏2022-07-21 13.31.53](/Users/kuroyume/Spring/Spring/note/截屏2022-07-21 13.31.53.png)

1. 基于jdk的proxy

   1. 目标类接口

      ```java
      package com.huahua.proxy.jdk;
      
      public interface TargetInterface {
          public void save();
      }
      ```

   2. 目标类

      ```java
      package com.huahua.proxy.jdk;
      
      public class Target implements TargetInterface{
          @Override
          public void save() {
              System.out.println("Save running...");
          }
      }
      ```

   3. 增强类

      ```java
      package com.huahua.proxy.jdk;
      
      public class Advice {
          public void before(){
              System.out.println("前置增强");
          }
          public void afterReturning(){
              System.out.println("后置增强");
          }
      }
      ```

   4. 动态代理对象

      ```java
      package com.huahua.proxy.jdk;
      import java.lang.reflect.InvocationHandler;
      import java.lang.reflect.Method;
      import java.lang.reflect.Proxy;
      public class ProxyTest {
          public static void main(String[] args) {
              //目标对象
              Target target = new Target();
              //增强对象
              Advice advice = new Advice();
              //返回值是动态生成的代理对象,为接口的子类，但非Target
              TargetInterface proxy = (TargetInterface) Proxy.newProxyInstance(
                      target.getClass().getClassLoader(), //目标对象类加载器
                      target.getClass().getInterfaces(), //目标对象相同接口字节码对象数组
                      new InvocationHandler() {
                          //调用代理对象的任何方法 实质执行的是该方法
                          @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                              advice.before(); //前置增强
                              Object invoke = method.invoke(target, args);//执行目标方法
                              advice.afterReturning();//后置增强
                              return invoke;
                          }
                      }
              );
              //调用代理对象的方法
              proxy.save();
          }
      }
      ```

      

   5. 结果

      ```java
      前置增强
      Save running...
      后置增强
      ```

2. 基于cglib的动态代理

   1. 导入cglib依赖

      Spring5已经将cglib继承于spring-core中

   2. 目标类

      ```java
      package com.huahua.proxy.cglib;
      
      public class Target {
          public void save() {
              System.out.println("Save running...");
          }
      }
      ```

   3. 增强类

      ```java
      package com.huahua.proxy.cglib;
      
      public class Advice {
          public void before(){
              System.out.println("前置增强");
          }
          public void afterReturning(){
              System.out.println("后置增强");
          }
      }
      ```

   4. 测试类

      ```java
      package com.huahua.proxy.cglib;
      
      import org.springframework.cglib.proxy.Enhancer;
      import org.springframework.cglib.proxy.MethodInterceptor;
      import org.springframework.cglib.proxy.MethodProxy;
      
      import java.lang.reflect.Method;
      
      public class ProxyTest {
          public static void main(String[] args) {
              //目标对象
              Target target = new Target();
              //增强对象
              Advice advice = new Advice();
      
      
              //返回值是动态生成的代理对象
              //1.创建增强器
              Enhancer enhancer = new Enhancer();
              //2.设置父类(目标)
              enhancer.setSuperclass(Target.class);
              //3.设置回调
              enhancer.setCallback(new MethodInterceptor() {
                  @Override
                  public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                      //执行前置
                      advice.before();
                      //执行目标
                      Object invoke = method.invoke(target, args);
                      //执行后置
                      advice.afterReturning();
                      return null;
                  }
              });
              //4.创建代理对象,使用Target接收生成的子类
              Target proxy = (Target) enhancer.create();
      
              proxy.save();
          }
      
      }
      ```

   5. 测试

      ```
      前置增强
      Save running...
      后置增强
      
      进程已结束，退出代码为 0
      ```

   ## 14.5 AOP的相关概念

   Spring 的 AOP 实现底层就是对上面的动态代理的代码进行了封装，封装后我们只需要对需要关注的部分进行代码编写，并通过配置的方式完成指定目标的方法增强。

   AOP 的相关常用的术语如下:

-  Target(目标对象):代理的目标对象
-   Proxy (代理):一个类被 AOP 织入增强后，就产生一个结果代理类
-   Joinpoint(连接点):所谓连接点是指那些被拦截到的点（点，理解为方法）。在spring中,这些点指的是方法，因为spring只支持方法类型的连接点
- Pointcut(切入点):所谓切入点是指我们要对哪些 Joinpoint 进行拦截的定义
- Advice(通知/ 增强):所谓通知是指拦截到 Joinpoint 之后所要做的事情就是通知
- Aspect(切面):是切入点和通知(引介)的结合
- Weaving(织入):是指把增强应用到目标对象来创建新的代理对象的过程。spring采用动态代理织入，而AspectJ采用编译期织入和类装载期织入

## 14.6 AOP开发明确的事项

1. 需要编写的内容

-  编写核心业务代码(目标类的目标方法)
-  编写切面类，切面类中有通知(增强功能方法)
-  在配置文件中，配置织入关系，即将哪些通知与哪些连接点进行结合

2. AOP 技术实现的内容

Spring 框架监控切入点方法的执行。一旦监控到切入点方法被运行，使用代理机制，动态创建目标对象的代理对象，根据通知类别，在代理对象的对应位置，将通知对应的功能织入，完成完整的代码逻辑运行。

3. AOP 底层使用哪种代理方式

在 spring 中，框架会根据目标类是否实现了接口来决定采用哪种动态代理的方式。

## 14.7 知识要点

- aop:面向切面编程

- aop底层实现:基于JDK的动态代理 和 基于Cglib的动态代理  

- aop的重点概念:
  - Pointcut(切入点):被增强的方法 
  - Advice(通知/ 增强):封装增强业务逻辑的方法 
  - Aspect(切面):切点+通知 
  - Weaving(织入):将切点与通知结合的过程
  
- 开发明确事项:
  -   谁是切点(切点表达式配置) 
  - 谁是通知(切面类中的增强方法) 
  - 将切点和通知进行织入配置
  
  

## 14.8 开发明确事项

1. 需要编写的内容

   -   编写核心业务代码(目标类的目标方法)

   -   编写切面类，切面类中有通知(增强功能和方法)

   -   在配置文件中，配置织入关系，即将哪些通知与哪些连接点进行结合

2. AOP技术实现的内容

   Spring框架监控切入点方法的执行。一旦监控到切入点方法被运行，使用代理机制，动态创建目标对象的代理对象，根据通知类别，在代理对西那个的对应位置，将通知对应的功能织入，完成完整的代码逻辑运行。

3. AOP底层使用哪种代理方式

在Spring中，框架会根据目标类是否实现了接口来决定采用哪种动态代理的方式。

## 14.9 基于XML的AOP开发

1. 导入坐标

   ```xml
   <dependency>
       <groupId>org.aspectj</groupId>
       <artifactId>aspectjweaver</artifactId>
       <version>1.8.13</version>
   </dependency>
   ```

2.  创建切面类

```java
package com.huahua.proxy.aop;

public class MyAspect {
    public void before(){
        System.out.println("前置增强。。。。。。。。。");
    }
}
```

3. 创建目标对象

   沿用前面的Target和TargetInterface

4. 在Xml文件中配置目标对象，切面对象及其织入

   ```xml
   <!--    配置目标对象-->
       <bean id="target" class="com.huahua.proxy.aop.Target"/>
   <!--    切面对象-->
       <bean id="myAspect" class="com.huahua.proxy.aop.MyAspect"/>
   <!--    配置织入，告诉spring框架 哪些方法需要进行哪些增强(前置、后置。。。)-->
       <aop:config>
   <!--        声明切面-->
           <aop:aspect ref="myAspect">
   <!--            切面：切点+通知-->
               <aop:before method="before" pointcut="execution(public void com.huahua.proxy.aop.Target.save())"></aop:before>
           </aop:aspect>
       </aop:config>
   ```

5. 测试类

   ```java
   package com.huahua;
   
   import com.huahua.proxy.aop.TargetInterface;
   import org.junit.Test;
   import org.junit.runner.RunWith;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.test.context.ContextConfiguration;
   import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
   
   @RunWith(SpringJUnit4ClassRunner.class)
   @ContextConfiguration("classpath:applicationContext.xml")
   public class AopTest {
       @Autowired
       private TargetInterface targetInterface;
       @Test
       public void test1(){
           targetInterface.save();
       }
   }
   ```

6. 测试

   ```apl
   前置增强。。。。。。。。。
   Save running...
   ```

   有配置织入就有增强，没有配置织入就没有增强  

7. 切点表达式

   ```xml
   <!--            切面：切点+通知-->
   <aop:before method="before" pointcut="execution(public void com.huahua.proxy.aop.Target.save())"/>
    <!--该写法不由普遍性-->
   <aop:before method="before" pointcut="execution(* com.huahua.proxy.aop.*.*(..))"></aop:before>
   <!--解释：不论是否为public，是否有返回值，aop包下任意类的任意方法不论有几个参数，都会被执行，不包含其子包-->
   ```

## 14.10 切点表达式的写法

表达式语法:

```apl
execution([修饰符] 返回值类型 包名.类名.方法名(参数))
```

- 访问修饰符可以省略
-  返回值类型、包名、类名、方法名可以使用星号* 代表任意
-  包名与类名之间一个点 . 代表当前包下的类，两个点 .. 表示当前包及其子包下的类
- 参数列表可以使用两个点 .. 表示任意个数，任意类型的参数列表

例如：

```apl
execution(public void com.itheima.aop.Target.method()) execution(void com.itheima.aop.Target.*(..)) execution(* com.itheima.aop.*.*(..))
execution(* com.itheima.aop..*.*(..))
execution(* *..*.*(..))
```

## 14.11 通知的类型

语法：

```xml
<aop:通知类型 method=“切面类中方法名” pointcut=“切点表达式"></aop:通知类型>
```

| 名称         | 标签                  | 说明                                                         |
| ------------ | --------------------- | ------------------------------------------------------------ |
| 前置通知     | <aop:before>          | 用于配置前置通知。指定增强的方法在切入点方法之前执行         |
| 后置通知     | <aop:after-returning> | 用于配置后置通知。指定增强的方法在切入点方法之后执行         |
| 环绕通知     | <aop:around>          | 用于配置环绕通知。指定增强的方法在切入点方法之前和之后都执行 |
| 异常抛出通知 | <aop:throwing>        | 用于配置异常抛出通知。指定增强的方法在出现异常时执行         |
| 最终通知     | <aop:after>           | 用于配置最终通知。无论增强方式执行是否有异常都会执行         |

演示：

切面对象：

```java
package com.huahua.proxy.aop;

import org.aspectj.lang.ProceedingJoinPoint;

public class MyAspect {
   public void before() {
      System.out.println("前置增强(before)");
   }

   public void afterReturning() {
      System.out.println("后置增强(after-returning)");
   }

   //参数：正在执行的连接点
   public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
      System.out.println("环绕前增强(around)");
      Object proceed = proceedingJoinPoint.proceed();
      System.out.println("环绕后增强(around)");
      return proceed;
   }

   //异常抛出增强
   public void afterThrowing() {
      System.out.println("异常抛出增强(after-throwing)");
   }

   //最终增强
   public void after() {
      System.out.println("最终增强(after)");
   }
}
```

目标对象

```java
package com.huahua.proxy.aop;

public class Target implements TargetInterface {
   @Override
   public void save() {
//        int i = 1/0;
      System.out.println("目标方法执行中。。。");
   }
}
```

xml配置：

```xml

<aop:config>
   <!--        声明切面-->
   <aop:aspect ref="myAspect">
      <!--            切面：切点+通知-->
      <aop:before method="before" pointcut="execution(* com.huahua.proxy.aop.*.*(..))"></aop:before>
      <aop:after-returning method="afterReturning" pointcut="execution(* com.huahua.proxy.aop.*.*(..))"/>
      <aop:around method="around" pointcut="execution(* com.huahua.proxy.aop.*.*(..))"/>
      <aop:after-throwing method="afterThrowing" pointcut="execution(* com.huahua.proxy.aop.*.*(..))"/>
      <aop:after method="after" pointcut="execution(* com.huahua.proxy.aop.*.*(..))"/>
   </aop:aspect>
</aop:config>
```

测试类；

```java
package com.huahua;

import com.huahua.proxy.aop.TargetInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class AopTest {
   @Autowired
   private TargetInterface targetInterface;

   @Test
   public void test1() {
      targetInterface.save();
   }
}
```

给目标对象制造除零异常测试：

```apl
前置增强(before)
环绕前增强(around)
最终增强(after)
异常抛出增强(after-throwing)

java.lang.ArithmeticException: / by zero
at com.huahua.proxy.aop.Target.save(Target.java:6)
at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
at java.lang.reflect.Method.invoke(Method.java:498)
```

正常测试：

```apl
前置增强(before)
环绕前增强(around)
目标方法执行中。。。
最终增强(after)
环绕后增强(around)
后置增强(after-returning)
```

## 14.12 抽取切点表达式

当多个增强的切点表达式相同时，可以将切点表达式进行抽取，在增强中使用 pointcut-ref 属性代替 pointcut 属性来引用抽 取后的切点表达式。

```xml
<!--    配置织入，告诉spring框架 哪些方法需要进行哪些增强(前置、后置。。。)-->
<aop:config>
   <aop:pointcut id="myPointcut" expression="execution(* com.huahua.proxy.aop.*.*(..))"/>
   <!--        声明切面-->
   <aop:aspect ref="myAspect">
      <!--            切面：切点+通知-->
      <aop:before method="before" pointcut-ref="myPointcut"></aop:before>
      <aop:after-returning method="afterReturning" pointcut-ref="myPointcut"/>
      <aop:around method="around" pointcut-ref="myPointcut"/>
      <aop:after-throwing method="afterThrowing" pointcut-ref="myPointcut"/>
      <aop:after method="after" pointcut-ref="myPointcut"/>
   </aop:aspect>
</aop:config>
```

## 14.13 基于注解的AOP开发

1. 步骤

   1. 创建目标接口和目标类(内部有切点)
   2.  创建切面类(内部有增强方法)
   3. 将目标类和切面类的对象创建权交给 spring
   4. 在切面类中使用注解配置织入关系
   5. 在配置文件中开启组件扫描和 AOP的自动代理 
   6. 测试

2. 实现

   1. 目标对象,注解名称不可与xml配置重复

      ```java
      package com.huahua.proxy.anno;
      
      import org.springframework.stereotype.Component;
      
      @Component("target1")
      public class Target implements TargetInterface {
          @Override
          public void save() {
      //        int i = 1/0;
              System.out.println("目标方法执行中......\tby com.huahua.proxy.anno.Target");
          }
      }
      ```

   2. 切面类

      ```java
      package com.huahua.proxy.anno;
      
      import org.aspectj.lang.ProceedingJoinPoint;
      import org.aspectj.lang.annotation.*;
      import org.springframework.stereotype.Component;
      
      @Component("myAspect1")
      @Aspect  //标注为切面类
      public class MyAspect {
      
          @Pointcut("execution(* com.huahua.proxy.anno.*.*(..))")
          public void Pointcut(){}
      
          @Before("Pointcut()")
          public void before(){
              System.out.println("前置增强(before)");
          }
          @AfterReturning(value = "execution(* com.huahua.proxy.anno.*.*(..))")
          public void afterReturning(){
              System.out.println("后置增强(after-returning)");
          }
      
          //参数：正在执行的连接点
          @Around("Pointcut()")
          public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
              System.out.println("环绕前增强(around)");
              Object proceed = proceedingJoinPoint.proceed();
              System.out.println("环绕后增强(around)");
              return proceed;
          }
          //异常抛出增强
          @AfterThrowing("Pointcut()")
          public void afterThrowing(){
              System.out.println("异常抛出增强(after-throwing)");
          }
      
          //最终增强
          @After("Pointcut()")
          public void after(){
              System.out.println("最终增强(after)");
          }
      }
      ```

   3. xml配置组件扫描和aop自动代理

      ```xml
      <!--    配置组件扫描,用于注解开发-->
      <context:component-scan base-package="com.huahua"></context:component-scan>
      <!--    自动代理-->
      <aop:aspectj-autoproxy/>
      ```

   4. 测试类

      ```java
      package com.huahua;
      
      import com.huahua.proxy.anno.TargetInterface;
      import org.junit.Test;
      import org.junit.runner.RunWith;
      import org.springframework.beans.factory.annotation.Autowired;
      import org.springframework.beans.factory.annotation.Qualifier;
      import org.springframework.test.context.ContextConfiguration;
      import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
      
      @RunWith(SpringJUnit4ClassRunner.class)
      @ContextConfiguration("classpath:applicationContext.xml")
      public class AnnoTest {
          @Autowired
          @Qualifier("target1")
          private TargetInterface targetInterface;
          @Test
          public void test1(){
              targetInterface.save();
          }
      }
      ```

   5. 测试

      ```apl
      环绕前增强(around)
      前置增强(before)
      目标方法执行中......	by com.huahua.proxy.anno.Target
      后置增强(after-returning)
      最终增强(after)
      环绕后增强(around)
      ```

      

3. 抽取切点表达式

   ```java
   @Pointcut("execution(* com.huahua.proxy.anno.*.*(..))") //抽取
   public void Pointcut(){}
   @Before("Pointcut()") //引用
   public void before(){
       System.out.println("前置增强(before)");
   }
   ```

# 15、Spring申明式事务控制

## 15.1 编程式事务控制相关对象

### 15.1.1PlatformTransactionManager

PlatformTransactionManager 接口是 spring 的事务管理器，它里面提供了我们常用的操作事务的方法。

|                             方法                             |        说明        |
| :----------------------------------------------------------: | :----------------: |
| TransactionStatus getTransaction(TransactionDefination defination) | 获取事务的状态信息 |
|            void commit(TransactionStatus status)             |      提交事务      |
|           void rollback(TransactionStatus status)            |      回滚事务      |

**注意:**PlatformTransactionManager 是接口类型，不同的 Dao 层技术则有不同的实现类，例如:Dao 层技术是jdbc 或 mybatis 时:org.springframework.jdbc.datasource.DataSourceTransactionManager
 Dao 层技术是hibernate时:org.springframework.orm.hibernate5.HibernateTransactionManager

### 15.1.2TransactionDefinition

TransactionDefinition 是事务的定义信息对象，里面有如下方法:

| 方法                         | 说明               |
| ---------------------------- | ------------------ |
| int getIsolationLevel()      | 获得事务的隔离级别 |
| int getPropogationBehavior() | 获得事务的传播行为 |
| int getTimeout()             | 获得超时时间       |
| boolean isReadOnly()         | 是否只读           |

1. 事务隔离级别

   设置隔离级别，可以解决事务并发产生的问题，如脏读、不可重复读和虚读。

   - ISOLATION_DEFAULT

     默认

   - ISOLATION_READ_UNCOMMITTED  

     读、未提交

   - ISOLATION_READ_COMMITTED

     读、已提交、可解决脏读

   - ISOLATION_REPEATABLE_READ

     不可重复度  

   - ISOLATION_SERIALIZABLE

     串行化(序列化)，可解决事务并发的所有问题，但效率太低

2. 事务传播行为

   - REQUIRED:如果当前没有事务，就新建一个事务，如果已经存在一个事务中，加入到这个事务中。一般的选择(默认值)  
   - SUPPORTS:支持当前事务，如果当前没有事务，就以非事务方式执行(没有事务)
   - MANDATORY:使用当前的事务，如果当前没有事务，就抛出异常
   - REQUERS_NEW:新建事务，如果当前在事务中，把当前事务挂起。
   - NOT_SUPPORTED:以非事务方式执行操作，如果当前存在事务，就把当前事务挂起
   - NEVER:以非事务方式运行，如果当前存在事务，抛出异常
   - NESTED:如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则执行REQUIRED类似的操作  
   - 超时时间:默认值是-1，没有超时限制。如果有，以秒为单位进行设置
   - 是否只读:建议查询时设置为只读

### 15.1.3TransactionStatus

TransactionStatus 接口提供的是事务具体的运行状态，方法介绍如下。

| 方法                       | 说明           |
| -------------------------- | -------------- |
| boolean hasSavepoint()     | 是否存储回滚点 |
| boolean isCompleted()      | 事务是否完成   |
| boolean isNewTransaction() | 是否是新事务   |
| boolean isRollbackOnly()   | 事务是否回滚   |

## 15.2 基于XML的声明式事务控制 

### 1、 概念：

Spring 的声明式事务顾名思义就是采用声明的方式来处理事务。这里所说的声明，就是指在配置文件中声明 ，用在 Spring 配置文件中声明式的处理事务来代替代码式的处理事务。

### 2、作用

- 事务管理不侵入开发的组件。具体来说，业务逻辑对象就不会意识到正在事务管理之中，事实上也应该如此，因为事务管理是属于系统层面的服务，而不是业务逻辑的一部分，如果想要改变事务管理策划的话， 也只需要在定义文件中重新配置即可
- 在不需要事务管理的时候，只要在设定文件上修改一下，即可移去事务管理服务，无需改变代码重新编译 ，这样维护起来极其方便

注意：Spring申明式事物控制底层就是AOP

### 3、实现

声明式事务控制明确事项: 

-  谁是切点?
-  谁是通知?
-  配置切面?

1. 引入spring事务控制依赖

   ```xml
   <dependencies>
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-context</artifactId>
           <version>5.3.19</version>
       </dependency>
       <dependency>
           <groupId>org.aspectj</groupId>
           <artifactId>aspectjweaver</artifactId>
           <version>1.8.13</version>
       </dependency>
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-jdbc</artifactId>
           <version>5.3.19</version>
       </dependency>
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-tx</artifactId>
           <version>5.3.19</version>
       </dependency>
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-test</artifactId>
           <version>5.3.19</version>
       </dependency>
       <dependency>
           <groupId>c3p0</groupId>
           <artifactId>c3p0</artifactId>
           <version>0.9.1.2</version>
       </dependency>
       <dependency>
           <groupId>mysql</groupId>
           <artifactId>mysql-connector-java</artifactId>
           <version>8.0.22</version>
       </dependency>
       <dependency>
           <groupId>junit</groupId>
           <artifactId>junit</artifactId>
           <version>4.13.1</version>
           <scope>test</scope>
       </dependency>
   </dependencies>
   ```

2. 准备数据库

   ```sql
   create table account
   (
       name  varchar(20) null,
       money int         null
   );
   ```

3. 数据库对应的实体类

   ```java
   package com.huahua.domain;
   
   public class Account {
   
       private String name;
       private double money;
   
       public String getName() {
           return name;
       }
   
       public void setName(String name) {
           this.name = name;
       }
   
       public double getMoney() {
           return money;
       }
   
       public void setMoney(double money) {
           this.money = money;
       }
   }
   ```

4. DAO层

   ```java
   package com.huahua.dao.impl;
   
   
   import com.huahua.dao.AccountDao;
   import org.springframework.jdbc.core.JdbcTemplate;
   
   public class AccountDaoImpl implements AccountDao {
   
       private JdbcTemplate jdbcTemplate;
       public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
           this.jdbcTemplate = jdbcTemplate;
       }
   
       public void out(String outMan, double money) {
           jdbcTemplate.update("update account set money=money-? where name=?",money,outMan);
       }
   
       public void in(String inMan, double money) {
           jdbcTemplate.update("update account set money=money+? where name=?",money,inMan);
       }
   }
   ```

5. 业务层

   ```java
   package com.huahua.service.impl;
   
   
   import com.huahua.dao.AccountDao;
   import com.huahua.service.AccountService;
   
   public class AccountServiceImpl implements AccountService {
   
       private AccountDao accountDao;
       public void setAccountDao(AccountDao accountDao) {
           this.accountDao = accountDao;
       }
   
       public void transfer(String outMan, String inMan, double money) {
           accountDao.out(outMan,money);
   //        int i = 1/0;
           accountDao.in(inMan,money);
       }
   }
   ```

6. 访问层

   ```java
   package com.huahua.controller;
   
   
   import com.huahua.service.AccountService;
   import org.springframework.context.ApplicationContext;
   import org.springframework.context.support.ClassPathXmlApplicationContext;
   
   public class AccountController {
   
       public static void main(String[] args) {
           ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
           AccountService accountService = app.getBean(AccountService.class);
           accountService.transfer("tom","lucy",500);
       }
   
   }
   ```

7. 全局xml文件配置

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xmlns:context="http://www.springframework.org/schema/context"
          xmlns:aop="http://www.springframework.org/schema/aop"
          xmlns:tx="http://www.springframework.org/schema/tx"
          xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
           http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd
           http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd">
   
       <!--    配置组件扫描,用于注解开发-->
       <context:component-scan base-package="com.huahua"/>
       <!--    自动代理-->
       <aop:aspectj-autoproxy/>
   
   <!--    加载外部的properties文件-->
       <context:property-placeholder location="classpath:application.properties"/>
   
   <!--    配置数据源对象-->
       <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
           <property name="driverClass" value="${jdbc.driver}"/>
           <property name="jdbcUrl" value="${jdbc.url}"/>
           <property name="user" value="${jdbc.username}"/>
           <property name="password" value="${jdbc.password}"/>
       </bean>
   <!--    jdbc模版对象-->
       <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
           <property name="dataSource" ref="dataSource"/>
       </bean>
   
   
       <bean class="com.huahua.dao.impl.AccountDaoImpl" id="accountDao">
           <property name="jdbcTemplate" ref="jdbcTemplate"/>
       </bean>
   
   <!--    切点，目标对象-->
       <bean id="accountService" class="com.huahua.service.impl.AccountServiceImpl">
           <property name="accountDao" ref="accountDao"/>
       </bean>
   
   <!--    配置平台事务管理器-->
       <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
           <property name="dataSource" ref="dataSource"/>
       </bean>
   <!--    通知  事务增强-->
       <tx:advice id="txAdvice" transaction-manager="transactionManager">
         <!--        设置事务的属性信息-->
           <tx:attributes>
   <!--  method:覆盖，指定的方法，isolation：隔离级别，propagation：传播行为，timeout：失效时间，read-only：是否只读，可配根据不同的方法，配置其不同的隔离级别等属性-->
               <tx:method name="*"/>
           </tx:attributes>
       </tx:advice>
   
   <!--    配置Aop的事务织入-->
       <aop:config>
           <aop:advisor advice-ref="txAdvice" pointcut="execution(* com.huahua.service.impl.*.*(..))"/>
       </aop:config>
   </beans>
   ```

8. 测试事务的原子性

   查看数据库tom5000，lucy0

   

   给两业务之间制造异常

   ```xml
   public void transfer(String outMan, String inMan, double money) {
       accountDao.out(outMan,money);
       int i = 1/0;
       accountDao.in(inMan,money);
   }
   ```

   执行访问后查看

   控制台：/ by zero除零异常

   数据库：数据未改变，事务具有原子性

## 15.3 基于注解的声明式事务控制

### 1、将自定义的bean配置替换为注解

```java
package com.huahua.dao.impl;


import com.huahua.dao.AccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("accountDao")
public class AccountDaoImpl implements AccountDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void out(String outMan, double money) {
        jdbcTemplate.update("update account set money=money-? where name=?",money,outMan);
    }

    public void in(String inMan, double money) {
        jdbcTemplate.update("update account set money=money+? where name=?",money,inMan);
    }
}
```

### 2、将通知和AOP事务织入替换为注解

```java
package com.huahua.service.impl;


import com.huahua.dao.AccountDao;
import com.huahua.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service("accountService")
//配置全类的事务属性
@Transactional(isolation = Isolation.REPEATABLE_READ)
@EnableTransactionManagement
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountDao accountDao;

//    配置单个方法上的事务属性，优先级高于全类的
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED,rollbackFor = {Error.class,Exception.class})
    public void transfer(String outMan, String inMan, double money) {
        accountDao.out(outMan,money);
//        int i = 1/0;
        accountDao.in(inMan,money);
    }
}
```

### 3、上下文中添加事务的注解驱动

```xml
<!--    事务的注解驱动-->
    <tx:annotation-driven transaction-manager="transactionManager"/>
```

### 4、解析

1. 使用 @Transactional 在需要进行事务控制的类或是方法上修饰，注解可用的属性同 xml 配置方式，例如隔离级别、传播行为等。
2. 注解使用在类上，那么该类下的所有 方法都 使用同 一套注 解参数 配置。 
3. 使用在方法上，不同的方法可以采用 不同的 事务参 数配置 。
4. Xml配置文件中要开启事务的注解驱动<tx:annotation-driven />

# 16、MyBatis入门操作

## 1、MyBatis简介

### 1.1 原始jdbc操作分析

![截屏2022-07-22 22.23.39](/Users/kuroyume/Spring/Spring/note/截屏2022-07-22 22.23.39.png)

原始数据开发存在的问题如下：

1. 数据库连接创建、释放频繁造成系统资源浪费，影响系统性能
2. sql语句在代码中硬编码，造成代码不易维护，实际应用sql变化的可能较大，sql变动需要改变java代码。
3. 查询操作时，需要手动将结果集中的数据封装到实体中。插入操作时，需要手动将实体的数据设置到sql语句的占位符位置

应对上述问题给出的解决方案：

1. 使用数据库连接池初始化连接资源
2. 将sql语句抽取到xml配置文件中
3. 使用反射、内省等底层技术，自动将实体与表进行属性与字段的映射

### 1.2 MyBatis简介

- mybatis 是一个优秀的基于java的**持久层框架**，它内部封装了 jdbc，使开发者只需要关注sql语句本身，而不需要花费精力 去处理加载驱动、创建连接、创建statement等繁杂的过程。
- mybatis通过xml或注解的方式将要执行的各种statement配置起来，并通过java对象和statement中sql的动态参数进行 映射生成最终执行的sql语句。
- 最后mybatis框架执行sql并将结果映射为java对象并返回。采用ORM（对象-关系-映射）思想解决了实体和数据库映射的问题，对jdbc进了封装，屏蔽了jdbc api 底层访问细节，使我们不用与jdbc api 打交道，就可以完成对数据库的持久化操作。

## 2、 MyBatis快速入门

### 2.1 MyBatis开发步骤

1. 添加MyBatis的坐标
2. 创建user数据表
3. 编写User实体类
4. 编写映射文件UserMapper.xml
5. 编写核心文件SqlMapConfig.xml 
6. 编写测试类

1、数据库

```sql
create table user
(
    id       int auto_increment,
    username varchar(20) null,
    password varchar(20) null,
    constraint user_id_uindex
        unique (id)
);

alter table user
    add primary key (id);
```

2、实体类

```java
package domain;

public class User {
    private int id;
    private String username;
    private String password;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
```

3、映射文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="userMapper">
    <select id="findAll" resultType="domain.User">
        select * from user
    </select>
</mapper>
```

4、核心文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
<!--    配置数据源的环境,可配置多个环境，将默认环境放进default字段中-->
            <environments default="development">
                <environment id="development">
                    <transactionManager type="JDBC"/>
                    <dataSource type="POOLED">
                        <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                        <property name="url" value="jdbc:mysql://localhost:3306/demo"/>
                        <property name="username" value="root"/>
                        <property name="password" value="root"/>
                    </dataSource>
                </environment>
            </environments>

<!--    加载映射文件-->
    <mappers>
        <mapper resource="mapper/UserMapper.xml"/>
    </mappers>
</configuration>
```

5、测试类

```java
package test;

import domain.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MyBatisTest {
    @Test
    public void test1() throws IOException {
        //获得核心配置文件
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        //获得sesion工厂对象
        SqlSessionFactory build = new SqlSessionFactoryBuilder().build(resourceAsStream);
        //获得session会话对象
        SqlSession sqlSession = build.openSession();
        //执行操作
        List<User> userList = sqlSession.selectList("userMapper.findAll");
        //测试
        System.out.println(userList);
        //释放资源
        sqlSession.close();
    }
}
```

### 2.2 MyBatis增删改查操作

1、 插入操作

```xml
<!--    插入-->
    <insert id="save" parameterType="domain.User">
        insert into user values(#{id},#{username},#{password})
    </insert>
```

```java
@Test
public void test2() throws IOException {
    User user = new User();
    user.setUsername("ton");
    user.setPassword("asfd");
    //获得核心配置文件
    InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
    //获得sesion工厂对象
    SqlSessionFactory build = new SqlSessionFactoryBuilder().build(resourceAsStream);
    //获得session会话对象
    SqlSession sqlSession = build.openSession();
    //执行操作
    int insert = sqlSession.insert("userMapper.save", user);
    //mybatis默认事务不提交，需要手动提交
    sqlSession.commit();
    //测试
    System.out.println(insert);
    //释放资源
    sqlSession.close();
}
```

2、插入操作注意问题

• 插入语句使用insert标签
 • 在映射文件中使用parameterType属性指定要插入的数据类型
 • Sql语句中使用#{实体属性名}方式引用实体中的属性值
 • 插入操作使用的API是sqlSession.insert(“命名空间.id”,实体对象);
 • 插入操作涉及数据库数据变化，所以要使用sqlSession对象显示的提交事务，即sqlSession.commit()

3、修改操作

```xml
<update id="update" parameterType="domain.User">
    update user set username=#{username},password=#{password} where id=#{id}
</update>
```

```java
@Test
public void test3() throws IOException {
    User user = new User();
    user.setId(2);
    user.setUsername("lucy");
    user.setPassword("123");
    //获得核心配置文件
    InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
    //获得sesion工厂对象
    SqlSessionFactory build = new SqlSessionFactoryBuilder().build(resourceAsStream);
    //获得session会话对象
    SqlSession sqlSession = build.openSession();
    //执行操作
    sqlSession.update("userMapper.update",user);
    //mybatis默认事务不提交，需要手动提交
    sqlSession.commit();
    //释放资源
    sqlSession.close();
}
```

4、修改操作注意问题

• 修改语句使用update标签
 • 修改操作使用的API是sqlSession.update(“命名空间.id”,实体对象);

5、删除操作

```xml
<delete id="delete" parameterType="java.lang.Integer">
    delete from user where id=#{id}
</delete>
```

```java
@Test
public void test4() throws IOException {
    //获得核心配置文件
    InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
    //获得sesion工厂对象
    SqlSessionFactory build = new SqlSessionFactoryBuilder().build(resourceAsStream);
    //获得session会话对象
    SqlSession sqlSession = build.openSession();
    //执行操作
    sqlSession.delete("userMapper.delete",2);
    //mybatis默认事务不提交，需要手动提交
    sqlSession.commit();
    //释放资源
    sqlSession.close();
}
```

## 3、MyBatis核心配置文件概述

### 3.1、MyBatis常用配置解析

![截屏2022-07-23 11.39.33](/Users/kuroyume/Spring/Spring/note/截屏2022-07-23 11.39.33.png)

#### environments标签

其中，事务管理器(transactionManager)类型有两种:

- JDBC:这个配置就是直接使用了JDBC 的提交和回滚设置，它依赖于从数据源得到的连接来管理事务作用域。

- MANAGED:这个配置几乎没做什么。它从来不提交或回滚一个连接，而是让容器来管理事务的整个生命周期(比如 JEE应用服务器的上下文)。 默认情况下它会关闭连接，然而一些容器并不希望这样，因此需要将 closeConnection 属性设置 为 false 来阻止它默认的关闭行为。

  

- 其中，数据源(dataSource)类型有三种:

  - UNPOOLED:这个数据源的实现只是每次被请求时打开和关闭连接。

  - POOLED:这种数据源的实现利用“池”的概念将 JDBC 连接对象组织起来。

  - JNDI:这个数据源的实现是为了能在如 EJB 或应用服务器这类容器中使用，容器可以集中或在外部配置数据源，然后放置一个 JNDI 上下文的引用。

![截屏2022-07-23 11.41.00](/Users/kuroyume/Spring/Spring/note/截屏2022-07-23 11.41.00.png)

#### mapper标签

该标签的作用是加载映射的，加载方式有如下几种:

- 使用相对于类路径的资源引用，例如:

  <mapper resource="org/mybatis/builder/AuthorMapper.xml"/>

- 使用完全限定资源定位符(URL)，例如:

  <mapper url="file:///var/mappers/AuthorMapper.xml"/>

- 使用映射器接口实现类的完全限定类名，例如:

  <mapper class="org.mybatis.builder.AuthorMapper"/>

- 将包内的映射器接口实现全部注册为映射器，例如:

  <package name="org.mybatis.builder"/>

#### Properties标签

实际开发中，习惯将数据源的配置信息单独抽取成一个properties文件，该标签可以加载额外配置的properties文件

![截屏2022-07-23 11.47.03](/Users/kuroyume/Spring/Spring/note/截屏2022-07-23 11.47.03.png)

####  typeAliases标签

类型别名是为Java 类型设置一个短的名字。原来的类型名称配置如下

![截屏2022-07-23 11.48.02](/Users/kuroyume/Spring/Spring/note/截屏2022-07-23 11.48.02.png)

配置typeAliases，为com.itheima.domain.User定义别名为user

![截屏2022-07-23 11.48.33](/Users/kuroyume/Spring/Spring/note/截屏2022-07-23 11.48.33.png)

![截屏2022-07-23 11.48.50](/Users/kuroyume/Spring/Spring/note/截屏2022-07-23 11.48.50.png)

上面我们是自定义的别名，mybatis框架已经为我们设置好的一些常用的类型的别名

![截屏2022-07-23 11.49.37](/Users/kuroyume/Spring/Spring/note/截屏2022-07-23 11.49.37.png)



# 17、myBatis的DAO层实现

## 1、传统开发方式

编写UserDao接口

```java
package dao.Impl;

import dao.UserMapper;
import domain.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UserMapperImpl implements UserMapper {
    @Override
    public List<User> findAll() throws IOException {
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = factory.openSession();
        List<User> list = sqlSession.selectList("userMapper.findAll");
        return list;
    }
}
```

## 2、代理开发方式

### 代理开发方式介绍

采用 Mybatis 的代理开发方式实现 DAO 层的开发，这种方式是我们后面进入企业的主流。
 Mapper 接口开发方法只需要程序员编写Mapper 接口(相当于Dao 接口)，由Mybatis 框架根据接口定义创建接 口的动态代理对象，代理对象的方法体同上边Dao接口实现类方法。
 Mapper 接口开发需要遵循以下规范:
 1、 Mapper.xml文件中的namespace与mapper接口的全限定名相同
 2、 Mapper接口方法名和Mapper.xml中定义的每个statement的id相同
 3、 Mapper接口方法的输入参数类型和mapper.xml中定义的每个sql的parameterType的类型相同
 4、 Mapper接口方法的输出参数类型和mapper.xml中定义的每个sql的resultType的类型相同

```java
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
```

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="dao.UserMapper">
    <select id="findAll" resultType="user">
        select * from user
    </select>
    <select id="findById" parameterType="int" resultType="user">
        select * from user where id=#{id}
    </select>
<!--    插入-->
    <insert id="save" parameterType="user">
        insert into user values(#{id},#{username},#{password})
    </insert>
    <update id="update" parameterType="user">
        update user set username=#{username},password=#{password} where id=#{id}
    </update>
<!--    java.lang.Integer int-->
    <delete id="delete" parameterType="int">
        delete from user where id=#{id}
    </delete>
</mapper>
```

```java
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
        
        
        List<User> all = mapper.findAll();
        System.out.println(all);
        User user = all.get(0);
        user.setUsername("asdadf");
        mapper.update(user);
        System.out.println(mapper.findAll());
        User user1 = new User();
        user1.setUsername("kakkaak");
        user1.setPassword("babab");
        mapper.save(user1);
        System.out.println(mapper.findAll());
        mapper.delete(user.getId());
        System.out.println(mapper.findAll());
    }
}
```

结果：

```apl
[User{id=1, username='ton', password='asfd'}, User{id=3, username='tom', password='asfd'}]
[User{id=1, username='asdadf', password='asfd'}, User{id=3, username='tom', password='asfd'}]
[User{id=1, username='asdadf', password='asfd'}, User{id=3, username='tom', password='asfd'}, User{id=4, username='kakkaak', password='babab'}]
[User{id=3, username='tom', password='asfd'}, User{id=4, username='kakkaak', password='babab'}]
```

# 18、MyBatis映射文件的深入

## 1、动态SQL语句

### 1.1 概述

Mybatis 的映射文件中，前面我们的 SQL 都是比较简单的，有些时候业务逻辑复杂时，我们的 SQL是动态变化的， 此时在前面的学习中我们的 SQL 就不能满足要求了。

参考的官方文档，描述如下:

![截屏2022-07-23 18.21.29](/Users/kuroyume/Spring/Spring/note/截屏2022-07-23 18.21.29.png)

### 1.2 if标签

我们根据实体类的不同取值，使用不同的 SQL语句来进行查询。比如在 id如果不为空时可以根据id查询，如果 username 不同空时还要加入用户名作为条件。这种情况在我们的多条件组合查询中经常会碰到。

接口：public List<User> findByCondition(User user);

映射文件：

```xml
<mapper namespace="dao.UserMapper">

    <select id="findByCondition" resultType="user" parameterType="user">
        select * from user
        <where>
            <if test="id!=null">
                and id=#{id}
            </if>
            <if test="username!=null">
                and username=#{username}
            </if>
            <if test="password!=null">
                and password=#{password}
            </if>
        </where>
    </select>
</mapper>
```

测试：

```java
    @Test
    public void test1() throws IOException {
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        User user = new User();
//        user.setUsername("tom");
//        user.setPassword("asfd");
        user.setId(3);
        System.out.println(mapper.findByCondition(user));
    }
```

当查询条件id和username都存在时，控制台打印的sql语句如下:

![截屏2022-07-23 18.49.36](/Users/kuroyume/Spring/Spring/note/截屏2022-07-23 18.49.36.png)

当查询条件只有id存在时，控制台打印的sql语句如下:

![截屏2022-07-23 18.50.19](/Users/kuroyume/Spring/Spring/note/截屏2022-07-23 18.50.19.png)

### 1.3 foreach标签

循环执行sql的拼接操作，例如:SELECT * FROM USER WHERE id IN (1,2,5)。

```xml
<!--   传入为链表集合，list，传入为数组array，open拼接头，close拼接尾部，item接收collection的像，separator:item的分隔符-->
<!--select * from user where id in(?,?,?,...)-->
    <select id="findByIds" parameterType="list" resultType="user">
        select * from user
        <where>
            <foreach collection="list" open="id in(" close=")" item="id" separator=",">
                #{id}
            </foreach>
        </where>
    </select>
```

```java
@Test
public void test2() throws IOException {
    InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
    SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sessionFactory.openSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);

    List<Integer> list = new ArrayList<>();
    list.add(206);
    list.add(208);
    list.add(222);
    System.out.println(mapper.findByIds(list));
}
```

结果：

```apl
08:55:15,347 DEBUG JdbcTransaction:101 - Setting autocommit to false on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@302552ec]
08:55:15,350 DEBUG findByIds:137 - ==>  Preparing: select * from user WHERE id in( ? , ? , ? )
08:55:15,384 DEBUG findByIds:137 - ==> Parameters: 206(Integer), 208(Integer), 222(Integer)
08:55:15,421 DEBUG findByIds:137 - <==      Total: 3
[User{id=206, username='user1', password='user1'}, User{id=208, username='user3', password='user3'}, User{id=222, username='user17', password='user17'}]
```

#### foreach标签的属性含义如下:

 <foreach>标签用于遍历集合，它的属性:
 • collection:代表要遍历的集合元素，注意编写时不要写#{} • open:代表语句的开始部分
 • close:代表结束部分
 • item:代表遍历集合的每个元素，生成的变量名
 • sperator:代表分隔符

## 2、SQL片段抽取

Sql 中可将重复的 sql 提取出来，使用时用 include 引用即可，最终达到 sql 重用的目的

```xml
<!--    sql语句的抽取-->
    <sql id="selectUser">select * from user</sql>

    <select id="findByCondition" resultType="user" parameterType="user">
        <include refid="selectUser"></include>
```

# 19、MyBatis核心配置文件深入

## 1、typeHandlers标签

无论是 MyBatis 在预处理语句(PreparedStatement)中设置一个参数时，还是从结果集中取出一个值时，都会用类型处理器将获取的值以合适的方式转换成 Java 类型。下表描述了一些默认的类型处理器(截取部分)。

![截屏2022-07-24 09.30.49](/Users/kuroyume/Spring/Spring/note/截屏2022-07-24 09.30.49.png)

你可以重写类型处理器或创建你自己的类型处理器来处理不支持的或非标准的类型。

具体做法为:

实现 org.apache.ibatis.type.TypeHandler 接口， 或继承一个很便利的类 org.apache.ibatis.type.BaseTypeHandler， 然后可以选择性地将它映射到一个JDBC类型。例如需求:一个Java中的Date数据类型，我想将之存到数据库的时候存成一 个1970年至今的毫秒数，取出来时转换成java的Date，即java的Date与数据库的varchar毫秒值之间转换。

开发步骤:

1. 定义转换类继承类BaseTypeHandler<T>
2. 覆盖4个未实现的方法，其中setNonNullParameter为java程序设置数据到数据库的回调方法，getNullableResult为查询时 mysql的字符串类型转换成 java的Type类型的方法 
3. 在MyBatis核心配置文件中进行注册
4. 测试转换是否正确

依赖：

```xml
<dependencies>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.22</version>
    </dependency>
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis</artifactId>
        <version>3.5.9</version>
    </dependency>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.2</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>commons-logging</groupId>
        <artifactId>commons-logging</artifactId>
        <version>1.2</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-log4j12</artifactId>
        <version>1.7.7</version>
    </dependency>
    <dependency>
        <groupId>log4j</groupId>
        <artifactId>log4j</artifactId>
        <version>1.2.17</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.24</version>
    </dependency>
</dependencies>
```

实体类：

```java
package domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@ToString
public class User {
    private Integer id;
    private String username;
    private String password;
    private Date birthday;
}
```

Mapper接口：

```java
public interface UserMapper {
    public void save(User user);
    public List<User> findAll();
}
```

UserMapper配置文件：

```xml
  <insert id="save" parameterType="user">
        insert into user values (#{id},#{username},#{password},#{birthday})
    </insert>
    <select id="findAll" resultType="user">
        select * from   user
    </select>
```

定义转换类继承类BaseTypeHandler<T>:

```java
package handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DateTypeHandler extends BaseTypeHandler<Date> {
    //将java类型转换成数据库需要的类型
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Date date, JdbcType jdbcType) throws SQLException {
        long time = date.getTime();
        preparedStatement.setLong(i,time);
    }


    //三个get都是将数据库中的类型转换成java类型
    //String 要转换的字段名称
    //ResultSet结果集
    @Override
    public Date getNullableResult(ResultSet resultSet, String s) throws SQLException {
        long aLong = resultSet.getLong(s);
        Date date = new Date(aLong);
        return date;
    }

    @Override
    public Date getNullableResult(ResultSet resultSet, int i) throws SQLException {
        long aLong = resultSet.getLong(i);
        Date date = new Date(aLong);
        return date;
    }

    @Override
    public Date getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        long aLong = callableStatement.getLong(i);
        return new Date(aLong);
    }
}
```

在核心文件中配置：

```xml
<!--    自定义注册类型处理器-->
    <typeHandlers>
        <typeHandler handler="handler.DateTypeHandler"/>
    </typeHandlers>
```

测试：

```java
    @Test
    public void test1() throws IOException {
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        User user = new User();
        user.setUsername("huahua");
        user.setPassword("root");
        user.setBirthday(new Date());
        mapper.save(user);
        System.err.println(mapper.findAll());

        sqlSession.commit();
        sqlSession.close();
    }
```

结果：

```apl
10:58:13,964 DEBUG save:137 - ==>  Preparing: insert into user values (?,?,?,?)
10:58:14,002 DEBUG save:137 - ==> Parameters: null, huahua(String), root(String), 1658631493501(Long)
10:58:14,006 DEBUG save:137 - <==    Updates: 1
10:58:14,010 DEBUG findAll:137 - ==>  Preparing: select * from user
10:58:14,011 DEBUG findAll:137 - ==> Parameters: 
10:58:14,041 DEBUG findAll:137 - <==      Total: 2
[User(id=305, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022), User(id=306, username=huahua, password=root, birthday=Sun Jul 24 10:58:13 CST 2022)]
```

数据库：

![截屏2022-07-24 11.01.03](/Users/kuroyume/Spring/Spring/note/截屏2022-07-24 11.01.03.png)

## 2、plugins标签

MyBatis可以使用第三方的插件来对功能进行扩展，分页助手PageHelper是将分页的复杂操作进行封装，使用简单的方式即 可获得分页的相关数据
 开发步骤:

1. 导入通用PageHelper的坐标
2. 在mybatis核心配置文件中配置PageHelper插件
3. 测试分页数据获取

导入通用PageHelper的坐标

```xml
     <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper</artifactId>
            <version>3.7.5</version>
        </dependency>
        <dependency>
            <groupId>com.github.jsqlparser</groupId>
            <artifactId>jsqlparser</artifactId>
            <version>0.9.1</version>
        </dependency>
```

在mybatis核心配置文件中配置PageHelper插件

```xml
<!--    配置PageHelper插件-->
    <plugins>
        <plugin interceptor="com.github.pagehelper.PageHelper">
<!--            指定方言-->
            <property name="dialect" value="mysql"/>
        </plugin>
    </plugins>
```

测试分页数据获取

```java
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

public class MapperTest {
    @SneakyThrows
    @Test
    public void test1() {
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

//        设置分页的相关参数
        PageHelper.startPage(1,3);
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
```

```apl
11:43:35,953 DEBUG findAll_PageHelper_Count:137 - ==>  Preparing: SELECT count(*) FROM user
11:43:35,992 DEBUG findAll_PageHelper_Count:137 - ==> Parameters: 
11:43:36,016 DEBUG findAll_PageHelper_Count:137 - <==      Total: 1
11:43:36,020 DEBUG findAll_PageHelper:137 - ==>  Preparing: select * from user limit ?,?
11:43:36,021 DEBUG findAll_PageHelper:137 - ==> Parameters: 0(Integer), 5(Integer)
11:43:36,024 DEBUG findAll_PageHelper:137 - <==      Total: 5
User(id=305, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022)
User(id=306, username=huahua, password=root, birthday=Sun Jul 24 10:58:13 CST 2022)
User(id=307, username=user0, password=root0, birthday=Sun Jul 24 11:24:38 CST 2022)
User(id=308, username=user1, password=root1, birthday=Sun Jul 24 11:24:41 CST 2022)
User(id=309, username=user2, password=root2, birthday=Sun Jul 24 11:24:43 CST 2022)
总条数:12
总页数:3
当前页:1
每页显示长度:5
是 否 第 一 页 :true
是否最后一页:false
上一页0
下一页2
PageInfo{pageNum=1, pageSize=5, size=5, startRow=1, endRow=5, total=12, pages=3, list=Page{pageNum=1, pageSize=5, startRow=0, endRow=5, total=12, pages=3, reasonable=false, pageSizeZero=false}, firstPage=1, prePage=0, nextPage=2, lastPage=3, isFirstPage=true, isLastPage=false, hasPreviousPage=false, hasNextPage=true, navigatePages=8, navigatepageNums=[1, 2, 3]}

```

# 20、MyBatis的多表操作

## 1、一对一查询

### 1.1  一对一查询的模型

用户表和订单表的关系为，一个用户有多个订单，一个订单只从属于一个用户 一对一查询的需求:查询一个订单，与此同时查询出该订单所属的用户

![截屏2022-07-24 14.47.28](/Users/kuroyume/Spring/Spring/note/截屏2022-07-24 14.47.28.png)

### 1.2 一对一查询的语句

对应的sql语句:select *,o.id oid from tb_order o,user u where o.uid=u.id;

查询的结果如下:

![截屏2022-07-24 15.34.49](/Users/kuroyume/Spring/Spring/note/截屏2022-07-24 15.34.49.png)

### 1.3 一对一查询实现

- 创建实体

  ```java
  package domain;
  
  import com.sun.istack.internal.Nullable;
  import lombok.Data;
  import lombok.NoArgsConstructor;
  import lombok.ToString;
  
  import java.util.Date;
  
  @Data
  @ToString
  @NoArgsConstructor
  public class Order {
      private int id;
      private Date orderTime;
      private double total;
      //订单属于的用户
      private User user;
  }
  ```

- 创建数据库

  ```sql
  create table tb_order(
      id int primary key auto_increment,
      ordertime varchar(50),
      total double,
      uid int,
      foreign key (uid) references user(id)
  );
  ```

- 创建Mapper层接口

  ```java
  public interface OrderMapper {
      public List<Order> findAll();
  }
  ```

- 创建Mapper，配置联合查询,两种自定义结果集合

  ```xml
  <!--
      手动指定字段与实体属性的映射关系
      column:数据表的字段名称
      property:实体的属性名称
  -->
   <!--   <resultMap id="orderMap" type="order">
          <id column="oid" property="id"></id>
          <result column="ordertime" property="orderTime"/>
          <result column="total" property="total"/>
          <result column="uid" property="user.id"/>
          <result column="username" property="user.username"/>
          <result column="password" property="user.password"/>
          <result column="birthday" property="user.birthday"/>
      </resultMap>-->
  
      <resultMap id="orderMap" type="order">
          <id column="oid" property="id"></id>
          <result column="ordertime" property="orderTime"/>
          <result column="total" property="total"/>
  <!--       property： 实体属性名成，javaType：属性在java中的数据类型-->
          <association property="user" javaType="user">
              <id column="id" property="id"/>
              <result column="username" property="username"/>
              <result column="password" property="password"/>
              <result column="birthday" property="birthday"/>
          </association>
      </resultMap>
  
      <select id="findAll" resultMap="orderMap">
          select *,o.id oid from tb_order o,user u where o.uid=u.id;
      </select>
  ```

- 核心配置设置别名和映射文件

  ```xml
  <!--配置别名-->
      <typeAliases>
          <typeAlias type="domain.User" alias="user"/>
          <typeAlias type="domain.Order" alias="order"/>
      </typeAliases>
      <!--    加载映射文件-->
      <mappers>
          <mapper resource="mapper/UserMapper.xml"/>
          <mapper resource="mapper/OrderMapper.xml"/>
      </mappers>
  ```

- 修改类型处理器,让Long类型和varchar类型的日期都能转换

  ```java
  package handler;
  
  import org.apache.ibatis.type.BaseTypeHandler;
  import org.apache.ibatis.type.JdbcType;
  
  import java.sql.CallableStatement;
  import java.sql.PreparedStatement;
  import java.sql.ResultSet;
  import java.sql.SQLException;
  import java.text.ParseException;
  import java.text.SimpleDateFormat;
  import java.util.Date;
  
  public class DateTypeHandler extends BaseTypeHandler<Date> {
      //将java类型转换成数据库需要的类型
      @Override
      public void setNonNullParameter(PreparedStatement preparedStatement, int i, Date date, JdbcType jdbcType) throws SQLException {
          long time = date.getTime();
          preparedStatement.setLong(i,time);
      }
      //三个get都是将数据库中的类型转换成java类型
      //String 要转换的字段名称
      //ResultSet结果集
      @Override
      public Date getNullableResult(ResultSet resultSet, String s) throws SQLException {
          long aLong;
          String str;
          Date date = null;
          try{
              aLong = resultSet.getLong(s);
              date = new Date(aLong);
          }catch (Exception e){
              str = resultSet.getString(s);
              SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
              try {
                  date = format.parse(str);
              } catch (ParseException ex) {
                  ex.printStackTrace();
              }
          }
          return date;
      }
  
      @Override
      public Date getNullableResult(ResultSet resultSet, int i) throws SQLException {
          long aLong;
          String str;
          Date date = null;
          try{
              aLong = resultSet.getLong(i);
              date = new Date(aLong);
          }catch (Exception e){
              str = resultSet.getString(i);
              SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
              try {
                  date = format.parse(str);
              } catch (ParseException ex) {
                  ex.printStackTrace();
              }
          }
          return date;
      }
  
      @Override
      public Date getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
          long aLong;
          String str;
          Date date = null;
          try{
              aLong = callableStatement.getLong(i);
              date = new Date(aLong);
          }catch (Exception e){
              str = callableStatement.getString(i);
              SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
              try {
                  date = format.parse(str);
              } catch (ParseException ex) {
                  ex.printStackTrace();
              }
          }
          return date;
      }
  }
  ```

- 测试

  ```java
  @SneakyThrows
  @Test
  public void test2() {
      InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
      SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
      SqlSession sqlSession = sessionFactory.openSession();
      OrderMapper mapper = sqlSession.getMapper(OrderMapper.class);
  
      for (Order order : mapper.findAll()) {
          System.out.println(order);
      }
  
      sqlSession.commit();
      sqlSession.close();
  }
  ```

- 结果

```apl
15:41:34,737 DEBUG findAll:137 - ==>  Preparing: select *,o.id oid from tb_order o,user u where o.uid=u.id;
15:41:34,772 DEBUG findAll:137 - ==> Parameters: 
15:41:34,806 DEBUG findAll:137 - <==      Total: 2
Order(id=1, orderTime=Fri Feb 15 14:59:37 CST 2019, total=3000.0, user=User(id=1, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022))
Order(id=2, orderTime=Wed Oct 10 15:00:00 CST 2018, total=5800.0, user=User(id=2, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022))
```

## 2、一对多查询

### 2.1 一对多查询的模型

用户表和订单表的关系为，一个用户有多个订单，一个订单只从属于一个用户 一对多查询的需求:查询一个用户，与此同时查询出该用户具有的订单

![截屏2022-07-24 16.09.28](/Users/kuroyume/Spring/Spring/note/截屏2022-07-24 16.09.28.png)

### 2.2 一对多查询的语句

对应的sql语句:select *,o.id oid from user u left join orders o on u.id=o.uid;

查询的结果如下:

![截屏2022-07-24 16.41.16](/Users/kuroyume/Spring/Spring/note/截屏2022-07-24 16.41.16.png)

### 2.3 一对多查询的实现

- User类

  ```java
  package domain;
  
  import lombok.Data;
  import lombok.NoArgsConstructor;
  import lombok.ToString;
  
  import java.util.Date;
  import java.util.List;
  
  @Data
  @NoArgsConstructor
  @ToString
  public class User {
      private Integer id;
      private String username;
      private String password;
      private Date birthday;
  
      private List<Order> orderList;
  }
  ```

- Mapper配置文件

  ```xml
  <resultMap id="userMap" type="user">
          <id column="id" property="id"/>
          <result column="username" property="username"/>
          <result column="password" property="password"/>
          <result column="birthday" property="birthday"/>
  <!--        配置集合,property：集合名称，ofType集合封装的类型-->
          <collection property="orderList" ofType="order">
              <id column="oid" property="id"/>
              <result column="ordertime" property="orderTime"/>
              <result column="total" property="total"/>
          </collection>
      </resultMap>
      <select id="findAll" resultMap="userMap">
          select *,o.id oid from user u left join tb_order o on u.id=o.uid;
      </select>
  ```

- 测试类

  ```java
  @SneakyThrows
  @Test
  public void test3() {
      InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
      SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
      SqlSession sqlSession = sessionFactory.openSession();
      UserMapper mapper = sqlSession.getMapper(UserMapper.class);
  
      List<User> userList = mapper.findAll();
      for (User user : userList) {
          System.err.println(user);
      }
  
      sqlSession.commit();
      sqlSession.close();
  }
  ```

- 结果

  ```apl
  Preparing: select *,o.id oid from user u left join tb_order o on u.id=o.uid;
  
  User(id=305, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022, orderList=[Order(id=2, orderTime=Wed Oct 10 15:00:00 CST 2018, total=5800.0, user=null), Order(id=1, orderTime=Fri Feb 15 14:59:37 CST 2019, total=3000.0, user=null)])
  User(id=306, username=huahua, password=root, birthday=Sun Jul 24 10:58:13 CST 2022, orderList=[Order(id=3, orderTime=Mon Jan 01 12:12:12 CST 2018, total=200.0, user=null)])
  ```



## 3、多对多查询

### 3.1 多对多查询的模型

用户表和角色表的关系为，一个用户有多个角色，一个角色被多个用户使用 多对多查询的需求:查询用户同时查询出该用户的所有角色

![截屏2022-07-24 16.48.18](/Users/kuroyume/Spring/Spring/note/截屏2022-07-24 16.48.18.png)

### 3.2 多对多查询的语句

对应的sql语句:select u.*,r.*,r.id rid from user u left join user_role ur on u.id=ur.user_id inner join role r on ur.role_id=r.id;

查询的结果如下:

![截屏2022-07-24 17.10.41](/Users/kuroyume/Spring/Spring/note/截屏2022-07-24 17.10.41.png)

### 3.3 多对多查询的实现

- 创建Role和中间表

  ```sql
  create table sys_role
  (
      id       bigint auto_increment
          primary key,
      roleName varchar(50) null,
      roleDesc varchar(50) null
  );
  ```

  ```sql
  create table sys_user_role
  (
      userId int    not null,
      roleId bigint not null,
      primary key (userId, roleId),
      constraint sys_user_role_ibfk_1
          foreign key (userId) references user (id),
      constraint sys_user_role_ibfk_2
          foreign key (roleId) references sys_role (id)
  );
  
  create index roleId
      on sys_user_role (roleId);
  ```

- 创建Role类

  ```java
  package domain;
  
  import lombok.Data;
  import lombok.NoArgsConstructor;
  import lombok.ToString;
  
  @Data
  @NoArgsConstructor
  @ToString
  public class Role {
      private int id;
      private String roleName;
      private String roleDesc;
  }
  ```

- 修改User类

  ```java
  package domain;
  
  import lombok.Data;
  import lombok.NoArgsConstructor;
  import lombok.ToString;
  
  import java.util.Date;
  import java.util.List;
  
  @Data
  @NoArgsConstructor
  @ToString
  public class User {
      private Integer id;
      private String username;
      private String password;
      private Date birthday;
  
      private List<Order> orderList;
  
      private List<Role> roleList;
  }
  ```

- 编写UserMapper.xml

  ```xml
  <resultMap id="userAndRole" type="user">
      <id column="id" property="id"/>
      <result column="username" property="username"/>
      <result column="password" property="password"/>
      <result column="birthday" property="birthday"/>
      <collection property="roleList" ofType="role">
          <id column="id" property="id"/>
          <result column="roleName" property="roleName"/>
          <result column="roleDesc" property="roleDesc"/>
      </collection>
  </resultMap>
  <select id="findUserAndRoleAll" resultMap="userAndRole">
      select u.*,r.*,r.id rid from user u left join sys_user_role ur on u.id=ur.userId inner join sys_role r on ur.roleId=r.id;
  </select>
  ```

- 测试类

  ```java
  @SneakyThrows
  @Test
  public void test4() {
      InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
      SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
      SqlSession sqlSession = sessionFactory.openSession();
      OrderMapper mapper = sqlSession.getMapper(OrderMapper.class);
  
      for (Order order : mapper.findAll()) {
          System.out.println(order);
      }
  
      sqlSession.commit();
      sqlSession.close();
  }
  ```

- 结果

  ```apl
  17:09:32,727 DEBUG findUserAndRoleAll:137 - ==>  Preparing: select u.*,r.*,r.id rid from user u left join sys_user_role ur on u.id=ur.userId inner join sys_role r on ur.roleId=r.id;
  17:09:32,780 DEBUG findUserAndRoleAll:137 - ==> Parameters: 
  17:09:32,809 DEBUG findUserAndRoleAll:137 - <==      Total: 8
  User(id=305, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022, orderList=null, roleList=[Role(id=305, roleName=院长, roleDesc=负责全面工作)])
  User(id=309, username=user2, password=root2, birthday=Sun Jul 24 11:24:43 CST 2022, orderList=null, roleList=[Role(id=309, roleName=院长, roleDesc=负责全面工作)])
  User(id=306, username=huahua, password=root, birthday=Sun Jul 24 10:58:13 CST 2022, orderList=null, roleList=[Role(id=306, roleName=助教, roleDesc=协助解决学生的问题)])
  User(id=307, username=user0, password=root0, birthday=Sun Jul 24 11:24:38 CST 2022, orderList=null, roleList=[Role(id=307, roleName=班主任, roleDesc=负责学生的日常)])
  User(id=308, username=user1, password=root1, birthday=Sun Jul 24 11:24:41 CST 2022, orderList=null, roleList=[Role(id=308, roleName=班主任, roleDesc=负责学生的日常)])
  ```


# 21、MyBatis注解开发

## 1、MyBatis的常用注解

这几年来注解开发越来越流行，Mybatis也可以使用注解开发方式，这样我们就可以减少编写Mapper 映射文件了。我们先围绕一些基本的CRUD来学习，再学习复杂映射多表操作。

```
@Insert:实现新增
@Update:实现更新
@Delete:实现删除
@Select:实现查询
@Result:实现结果集封装
@Results:可以与@Result 一起使用，封装多个结果集 
@One:实现一对一结果集封装 
@Many:实现一对多结果集封装
```

## 2、MyBatis的增删改查

我们完成简单的user表的增删改查的操作

编写注释

```java
package dao;

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
}
```

加载映射关系，不加载映射文件

```xml
<!--    加载映射关系 TODO-->
    <mappers>
<!--        指定接口所在的包-->
        <package name="dao"/>
    </mappers>
```

测试：

```java
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
}
```

结果略

## 3、一对一注解查询配置



### 两种注解配置

```java
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
}
```

### 测试

在上述测试方法中添加

```java
@Test
public void testOrderFindAll(){
    orderMapper.findAll1().forEach(order -> {
        System.out.println(order);
    });
    orderMapper.findAll2().forEach(order -> {
        System.out.println(order);
    });
}
```

### 结果：

```apl
17:14:27,518 DEBUG JdbcTransaction:101 - Setting autocommit to false on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@1aafa419]
17:14:27,523 DEBUG findAll1:137 - ==>  Preparing: select *,o.id oid from tb_order o,user u where o.uid=u.id
17:14:27,566 DEBUG findAll1:137 - ==> Parameters: 
17:14:27,598 DEBUG findAll1:137 - <==      Total: 3
Order(id=1, ordertime=Fri Feb 15 14:59:37 CST 2019, total=3000.0, user=User(id=305, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022, orderList=null, roleList=null))
Order(id=2, ordertime=Wed Oct 10 15:00:00 CST 2018, total=5800.0, user=User(id=305, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022, orderList=null, roleList=null))
Order(id=3, ordertime=Mon Jan 01 12:12:12 CST 2018, total=200.0, user=User(id=306, username=huahua, password=root, birthday=Sun Jul 24 10:58:13 CST 2022, orderList=null, roleList=null))
17:14:27,601 DEBUG findAll2:137 - ==>  Preparing: select * from tb_order
17:14:27,601 DEBUG findAll2:137 - ==> Parameters: 
17:14:27,605 DEBUG findById:137 - ====>  Preparing: select * from user where id=?
17:14:27,605 DEBUG findById:137 - ====> Parameters: 305(Integer)
17:14:27,607 DEBUG findById:137 - <====      Total: 1
17:14:27,609 DEBUG findById:137 - ====>  Preparing: select * from user where id=?
17:14:27,609 DEBUG findById:137 - ====> Parameters: 306(Integer)
17:14:27,611 DEBUG findById:137 - <====      Total: 1
17:14:27,611 DEBUG findAll2:137 - <==      Total: 3
Order(id=1, ordertime=Fri Feb 15 14:59:37 CST 2019, total=3000.0, user=User(id=305, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022, orderList=null, roleList=null))
Order(id=2, ordertime=Wed Oct 10 15:00:00 CST 2018, total=5800.0, user=User(id=305, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022, orderList=null, roleList=null))
Order(id=3, ordertime=Mon Jan 01 12:12:12 CST 2018, total=200.0, user=User(id=306, username=huahua, password=root, birthday=Sun Jul 24 10:58:13 CST 2022, orderList=null, roleList=null))

```

## 4、一对多查询注解配置

### 配置：

先查询user表，在通过id作为外键查询order表

```java
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

```

```java
@Select("select * from tb_order where uid=#{uid}")
public List<Order> findByUid();
```

### 测试：

```java
@Test
public void testOneToMany(){
    userMapper.findAllUserAndOrderList().forEach(user -> {
        System.out.println(user);
    });
}
```

结果：

```apl
17:33:16,182 DEBUG findAllUserAndOrderList:137 - ==>  Preparing: select * from user
17:33:16,215 DEBUG findAllUserAndOrderList:137 - ==> Parameters: 
17:33:16,241 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,242 DEBUG findByUid:137 - ====> Parameters: 305(Integer)
17:33:16,252 DEBUG findByUid:137 - <====      Total: 2
17:33:16,255 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,255 DEBUG findByUid:137 - ====> Parameters: 306(Integer)
17:33:16,257 DEBUG findByUid:137 - <====      Total: 1
17:33:16,257 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,258 DEBUG findByUid:137 - ====> Parameters: 307(Integer)
17:33:16,259 DEBUG findByUid:137 - <====      Total: 0
17:33:16,259 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,259 DEBUG findByUid:137 - ====> Parameters: 308(Integer)
17:33:16,260 DEBUG findByUid:137 - <====      Total: 0
17:33:16,261 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,261 DEBUG findByUid:137 - ====> Parameters: 309(Integer)
17:33:16,262 DEBUG findByUid:137 - <====      Total: 0
17:33:16,263 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,263 DEBUG findByUid:137 - ====> Parameters: 310(Integer)
17:33:16,264 DEBUG findByUid:137 - <====      Total: 0
17:33:16,266 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,267 DEBUG findByUid:137 - ====> Parameters: 311(Integer)
17:33:16,268 DEBUG findByUid:137 - <====      Total: 0
17:33:16,268 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,269 DEBUG findByUid:137 - ====> Parameters: 312(Integer)
17:33:16,270 DEBUG findByUid:137 - <====      Total: 0
17:33:16,270 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,271 DEBUG findByUid:137 - ====> Parameters: 313(Integer)
17:33:16,272 DEBUG findByUid:137 - <====      Total: 0
17:33:16,273 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,273 DEBUG findByUid:137 - ====> Parameters: 314(Integer)
17:33:16,275 DEBUG findByUid:137 - <====      Total: 0
17:33:16,275 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,276 DEBUG findByUid:137 - ====> Parameters: 315(Integer)
17:33:16,277 DEBUG findByUid:137 - <====      Total: 0
17:33:16,278 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,278 DEBUG findByUid:137 - ====> Parameters: 316(Integer)
17:33:16,280 DEBUG findByUid:137 - <====      Total: 0
17:33:16,280 DEBUG findByUid:137 - ====>  Preparing: select * from tb_order where uid=?
17:33:16,281 DEBUG findByUid:137 - ====> Parameters: 318(Integer)
17:33:16,282 DEBUG findByUid:137 - <====      Total: 0
17:33:16,282 DEBUG findAllUserAndOrderList:137 - <==      Total: 13
User(id=305, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022, orderList=[Order(id=1, ordertime=Fri Feb 15 14:59:37 CST 2019, total=3000.0, user=null), Order(id=2, ordertime=Wed Oct 10 15:00:00 CST 2018, total=5800.0, user=null)], roleList=null)
User(id=306, username=huahua, password=root, birthday=Sun Jul 24 10:58:13 CST 2022, orderList=[Order(id=3, ordertime=Mon Jan 01 12:12:12 CST 2018, total=200.0, user=null)], roleList=null)
User(id=307, username=user0, password=root0, birthday=Sun Jul 24 11:24:38 CST 2022, orderList=[], roleList=null)
User(id=308, username=user1, password=root1, birthday=Sun Jul 24 11:24:41 CST 2022, orderList=[], roleList=null)
User(id=309, username=user2, password=root2, birthday=Sun Jul 24 11:24:43 CST 2022, orderList=[], roleList=null)
User(id=310, username=user3, password=root3, birthday=Sun Jul 24 11:24:45 CST 2022, orderList=[], roleList=null)
User(id=311, username=user4, password=root4, birthday=Sun Jul 24 11:24:47 CST 2022, orderList=[], roleList=null)
User(id=312, username=user5, password=root5, birthday=Sun Jul 24 11:24:49 CST 2022, orderList=[], roleList=null)
User(id=313, username=user6, password=root6, birthday=Sun Jul 24 11:24:51 CST 2022, orderList=[], roleList=null)
User(id=314, username=user7, password=root7, birthday=Sun Jul 24 11:24:53 CST 2022, orderList=[], roleList=null)
User(id=315, username=user8, password=root8, birthday=Sun Jul 24 11:24:55 CST 2022, orderList=[], roleList=null)
User(id=316, username=user9, password=root9, birthday=Sun Jul 24 11:24:57 CST 2022, orderList=[], roleList=null)
User(id=318, username=hahah, password=asdf, birthday=Mon Jul 25 15:56:07 CST 2022, orderList=[], roleList=null)
```

## 5、多对多查询注解配置

### 配置：

```java
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
                    many = @Many(select = "dao.UserMapper.findById")
            )
    })
    public List<User> findUserAndRoleAll();

    @Select("SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=#{uid}")
    public List<Role> findByUid(int uid);
```

### 测试：

```java
@Test
public void testManyToMany(){
    userMapper.findUserAndRoleAll().forEach(user -> {
        System.out.println(user);
    });
}
```

### 结果：

```apl
18:10:54,267 DEBUG findUserAndRoleAll:137 - ==>  Preparing: select * from user
18:10:54,302 DEBUG findUserAndRoleAll:137 - ==> Parameters: 
18:10:54,324 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,325 DEBUG findByUid:137 - ====> Parameters: 305(Integer)
18:10:54,328 DEBUG findByUid:137 - <====      Total: 3
18:10:54,337 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,338 DEBUG findByUid:137 - ====> Parameters: 306(Integer)
18:10:54,339 DEBUG findByUid:137 - <====      Total: 1
18:10:54,339 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,340 DEBUG findByUid:137 - ====> Parameters: 307(Integer)
18:10:54,341 DEBUG findByUid:137 - <====      Total: 1
18:10:54,342 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,342 DEBUG findByUid:137 - ====> Parameters: 308(Integer)
18:10:54,344 DEBUG findByUid:137 - <====      Total: 2
18:10:54,345 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,345 DEBUG findByUid:137 - ====> Parameters: 309(Integer)
18:10:54,347 DEBUG findByUid:137 - <====      Total: 1
18:10:54,347 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,347 DEBUG findByUid:137 - ====> Parameters: 310(Integer)
18:10:54,349 DEBUG findByUid:137 - <====      Total: 0
18:10:54,349 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,350 DEBUG findByUid:137 - ====> Parameters: 311(Integer)
18:10:54,352 DEBUG findByUid:137 - <====      Total: 0
18:10:54,352 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,353 DEBUG findByUid:137 - ====> Parameters: 312(Integer)
18:10:54,354 DEBUG findByUid:137 - <====      Total: 0
18:10:54,355 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,355 DEBUG findByUid:137 - ====> Parameters: 313(Integer)
18:10:54,357 DEBUG findByUid:137 - <====      Total: 0
18:10:54,357 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,358 DEBUG findByUid:137 - ====> Parameters: 314(Integer)
18:10:54,359 DEBUG findByUid:137 - <====      Total: 0
18:10:54,360 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,360 DEBUG findByUid:137 - ====> Parameters: 315(Integer)
18:10:54,363 DEBUG findByUid:137 - <====      Total: 0
18:10:54,364 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,365 DEBUG findByUid:137 - ====> Parameters: 316(Integer)
18:10:54,366 DEBUG findByUid:137 - <====      Total: 0
18:10:54,366 DEBUG findByUid:137 - ====>  Preparing: SELECT * FROM sys_user_role ur,sys_role r WHERE ur.roleId=r.id AND ur.userId=?
18:10:54,367 DEBUG findByUid:137 - ====> Parameters: 318(Integer)
18:10:54,368 DEBUG findByUid:137 - <====      Total: 0
18:10:54,368 DEBUG findUserAndRoleAll:137 - <==      Total: 13
User(id=305, username=tom, password=asfd, birthday=Sun Jul 24 10:47:57 CST 2022, orderList=null, roleList=[Role(id=1, roleName=院长, roleDesc=负责全面工作), Role(id=2, roleName=研究员, roleDesc=课程研发工作), Role(id=3, roleName=讲师, roleDesc=授课工作)])
User(id=306, username=huahua, password=root, birthday=Sun Jul 24 10:58:13 CST 2022, orderList=null, roleList=[Role(id=4, roleName=助教, roleDesc=协助解决学生的问题)])
User(id=307, username=user0, password=root0, birthday=Sun Jul 24 11:24:38 CST 2022, orderList=null, roleList=[Role(id=5, roleName=班主任, roleDesc=负责学生的日常)])
User(id=308, username=user1, password=root1, birthday=Sun Jul 24 11:24:41 CST 2022, orderList=null, roleList=[Role(id=5, roleName=班主任, roleDesc=负责学生的日常), Role(id=6, roleName=就业指导, roleDesc=负责学生的就业工作)])
User(id=309, username=user2, password=root2, birthday=Sun Jul 24 11:24:43 CST 2022, orderList=null, roleList=[Role(id=1, roleName=院长, roleDesc=负责全面工作)])
User(id=310, username=user3, password=root3, birthday=Sun Jul 24 11:24:45 CST 2022, orderList=null, roleList=[])
User(id=311, username=user4, password=root4, birthday=Sun Jul 24 11:24:47 CST 2022, orderList=null, roleList=[])
User(id=312, username=user5, password=root5, birthday=Sun Jul 24 11:24:49 CST 2022, orderList=null, roleList=[])
User(id=313, username=user6, password=root6, birthday=Sun Jul 24 11:24:51 CST 2022, orderList=null, roleList=[])
User(id=314, username=user7, password=root7, birthday=Sun Jul 24 11:24:53 CST 2022, orderList=null, roleList=[])
User(id=315, username=user8, password=root8, birthday=Sun Jul 24 11:24:55 CST 2022, orderList=null, roleList=[])
User(id=316, username=user9, password=root9, birthday=Sun Jul 24 11:24:57 CST 2022, orderList=null, roleList=[])
User(id=318, username=hahah, password=asdf, birthday=Mon Jul 25 15:56:07 CST 2022, orderList=null, roleList=[])
```

# 22、SSM框架整合

## 1、准备工作

### 1.1 导入依赖

```xml
<dependencies>
<!--        servelet,jsp-->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.servlet.jsp</groupId>
            <artifactId>javax.servlet.jsp-api</artifactId>
            <version>2.2.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jstl</groupId>
            <artifactId>jstl</artifactId>
            <version>1.2</version>
        </dependency>

<!--        junit-->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>

<!--        AOP-->
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.8.7</version>
        </dependency>
<!--     数据库   -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.22</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.5.9</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>2.0.6</version>
        </dependency>
        <dependency>
            <groupId>c3p0</groupId>
            <artifactId>c3p0</artifactId>
            <version>0.9.1.2</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.1.10</version>
        </dependency>

<!--上传文件-->
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.3</version>
        </dependency>
        <dependency>
            <groupId>commons-fileupload</groupId>
            <artifactId>commons-fileupload</artifactId>
            <version>1.3.1</version>
        </dependency>

<!--     日志   -->
        <dependency>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
            <version>1.2</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.7</version>
        </dependency>
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.17</version>
        </dependency>

<!--        -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.24</version>
        </dependency>

<!--        分页-->
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper</artifactId>
            <version>3.7.5</version>
        </dependency>
        <dependency>
            <groupId>com.github.jsqlparser</groupId>
            <artifactId>jsqlparser</artifactId>
            <version>0.9.1</version>
        </dependency>

<!--        spring-->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>5.3.18</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.3.18</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>5.3.18</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>5.3.18</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>5.3.18</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>5.3.18</version>
        </dependency>

<!--        json数据封装-->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>2.9.0</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.9.0</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>2.9.0</version>
        </dependency>
    </dependencies>
```

### 1.2 构建框架

![截屏2022-07-26 09.58.23](/Users/kuroyume/Spring/Spring_Learning_note/note/截屏2022-07-26 09.58.23.png)

### 1.3 原始方式整合

其他基础代码使用Spring自动注入，业务层使用原始方法构建mybatis的会话工厂

```java
package com.huahua.service.Impl;

import com.huahua.domain.Account;
import com.huahua.mapper.AccountMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class AccountService implements com.huahua.service.AccountService {


    @Override
    public void save(Account account) {
        try {
            InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
            SqlSession sqlSession = sqlSessionFactory.openSession();
            AccountMapper mapper = sqlSession.getMapper(AccountMapper.class);
            mapper.save(account);
            sqlSession.commit();
            sqlSession.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Account> findAll() {
        try {
            InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
            SqlSession sqlSession = sqlSessionFactory.openSession();
            AccountMapper mapper = sqlSession.getMapper(AccountMapper.class);
            List<Account> all = mapper.findAll();
            sqlSession.commit();
            sqlSession.close();
            return all;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

## 2、整合思路

将Session工厂交给Spring容器管理，从容器中获得执行操作的Mapper实例

applicationContext.xml

```xml
<!--    加载properties文件-->
    <context:property-placeholder location="classpath:jdbc.properties"/>
<!--    配置数据源-->
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
        <property name="driverClass" value="${jdbc.driver}"/>
        <property name="jdbcUrl" value="${jdbc.url}"/>
        <property name="user" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>
    </bean>
<!--    配置会话工厂sessionFactory-->
    <bean id="sqlSessionFactoryBean" class="org.mybatis.spring.SqlSessionFactoryBean">
<!--        注入datasource-->
        <property name="dataSource" ref="dataSource"/>
<!--        加载MyBatis的核心文件-->
        <property name="configLocation" value="sqlMapConfig-spring.xml"/>
    </bean>
    <!--    加载mapper.xml映射文件,自动生成mapper对应的bean可装配使用-->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage"  value="com.huahua.mapper"/>
    </bean>
```

sqlMapConfig.xml

```xml
<configuration>
    <!--配置别名-->
    <typeAliases>
<!--        <typeAlias type="com.huahua.domain.Account" alias="user"/>-->
<!--        扫描包，统一指配默认别名一-->
        <package name="com.huahua.domain"/>
    </typeAliases>

    <!--    配置PageHelper插件-->
    <plugins>
        <plugin interceptor="com.github.pagehelper.PageHelper">
            <!--            指定方言-->
            <property name="dialect" value="mysql"/>
        </plugin>
    </plugins>
</configuration>
```

将事务的控制交给SpringAOP，进行申明式事物控制
