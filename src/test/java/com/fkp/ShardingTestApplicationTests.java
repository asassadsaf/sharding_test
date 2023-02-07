package com.fkp;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fkp.domain.Car;
import com.fkp.domain.User;
import com.fkp.domain.UserExample;
import com.fkp.domain.UserVO;
import com.fkp.mapper.CarMapper;
import com.fkp.mapper.UserMapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

@SpringBootTest
public class ShardingTestApplicationTests {

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void save() {
        Car car = new Car(1L,"dazhong",10);
        Car car2 = new Car(2L,"aodi",20);
        Car car3 = new Car(3L,"bieke",30);
        carMapper.insertSelective(car);
        carMapper.insertSelective(car2);
        carMapper.insertSelective(car3);
        User user = new User(null,"jinan","zhangsan",1L);
        User user2 = new User(null,"zibo","lisi",2L);
        User user3 = new User(null,"qingdao","wangwu",3L);
        userMapper.insertSelective(user);
        userMapper.insertSelective(user2);
        userMapper.insertSelective(user3);
    }

    @Test
    void select(){
        //通过主键查询只查询一个表
        User user = userMapper.selectByPrimaryKey(829830178883502081L);
        System.out.println(user);
    }

    @Test
    void select2(){
        //通过非主键查询需要查询三个表
        UserExample example = new UserExample();
        example.createCriteria().andCarIdEqualTo(1L);
        List<User> users = userMapper.selectByExample(example);
        System.out.println(users);
    }

    @Test
    void page(){
        //分页查询，三个表各查一次总数，三个表分别分页查询，汇总结果处理后返回
        Page<User> page = new Page<>();
        page.setCurrent(1);
        page.setSize(2);
        OrderItem orderItem = new OrderItem();
        orderItem.setColumn("id");
        orderItem.setAsc(false);
        page.setOrders(Collections.singletonList(orderItem));
        long start = System.currentTimeMillis();
        Page<List<User>> res = userMapper.findPage(page);
        long end = System.currentTimeMillis();
        System.out.println("time" + (end - start) + "\npageNum:"+ res.getCurrent() + "\npageSize:" + res.getSize() + "\ntotal:" + res.getTotal() + "\nrecords:" + res.getRecords());
    }

    @Test
    void selectUserVO(){
        //关联查询，分别从三个表关联查询，结果汇总,即使查询条件制定了主键字段也会查询三个表
        List<UserVO> userCar = userMapper.findUserCar();
        System.out.println(userCar);
    }

    @Test
    void saveBatch() {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH,false);
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        CarMapper carMapper = sqlSession.getMapper(CarMapper.class);
        long start = System.currentTimeMillis();
        for (int i=100000;i<200000;i++){
//            Car car = new Car(1L + i,"dazhong_" + i,10 + i);
//            carMapper.insert(car);
            User user = new User(null,"jinan_" + i,"zhangsan_" + i,1L + i);
            mapper.insertNoId(user);
        }
        sqlSession.commit();
        sqlSession.close();
        long end = System.currentTimeMillis();
        System.out.println((end - start)/1000);
    }

}
