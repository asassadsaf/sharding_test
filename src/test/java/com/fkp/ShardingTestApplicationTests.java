package com.fkp;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fkp.domain.*;
import com.fkp.mapper.CarMapper;
import com.fkp.mapper.UserMapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
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

    /**
     * 2kw数据分表分页查询559ms
     */
    @Test
    void page(){
        //分页查询，三个表各查一次总数，三个表分别分页查询，汇总结果处理后返回
        Page<User> page = new Page<>();
        page.setCurrent(1);
        page.setSize(10);
        OrderItem orderItem = new OrderItem();
        orderItem.setColumn("id");
        orderItem.setAsc(false);
//        page.setOrders(Collections.singletonList(orderItem));
        long start = System.currentTimeMillis();
        //大数据量下若查询total会耗费大量时间，通过指定setSearchCount为false关闭对总数的查询
//        page.setSearchCount(false);
        Page<List<User>> res = userMapper.findPage(page);
        long end = System.currentTimeMillis();
        System.out.println("time" + (end - start) + "\npageNum:"+ res.getCurrent() + "\npageSize:" + res.getSize() + "\ntotal:" + res.getTotal() + "\nrecords:" + res.getRecords());
    }

    /**
     * 2kw数据单表分页查询538ms
     */
    @Test
    void carPage(){
        Page<Car> page = new Page<>();
        page.setCurrent(1);
        page.setSize(10);
        page.setSearchCount(false);
        long start = System.currentTimeMillis();
        Page<Car> cars = carMapper.findPage(page);
        long end = System.currentTimeMillis();
        System.out.println("time:" + (end - start));
        System.out.println(cars.getRecords());
    }

    @Test
    void personPage(){
        Page<Person> page = new Page<>();
        page.setCurrent(1);
        page.setSize(1);
        Page<Person> cars = carMapper.findPersonPage(page);
        System.out.println(cars.getRecords());
    }

    @Test
    void selectUserVO(){
        //关联查询，分别从三个表关联查询，结果汇总,即使查询条件制定了主键字段也会查询三个表
        List<UserVO> userCar = userMapper.findUserCar();
        System.out.println(userCar);
    }

    /**
     * 批量插入，在一个事务中，数据过大会导致OOM，速度较动态拼接sql慢
     */
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

    /**
     * 动态拼接sql方式批量插入，10W->209s    20W * 50批次  1000W -> 69705s
     */
    @Test
    void saveBatch2(){

        long time = 0;
        long time2 = 0;
        int index = 10000000;
        for (int j = 0;j<50;j++){
            System.out.println("open:" + j);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH,false);
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            List<User> userList = new ArrayList<>();
            long begin = System.currentTimeMillis();
            for (int i=0;i<200000;i++){
                userList.add(new User(null,"jinan_" + index,"zhangsan_" + index, (long) index));
                index++;
            }
            long start = System.currentTimeMillis();
            mapper.insertBatch(userList);
            sqlSession.commit();
            sqlSession.clearCache();
            sqlSession.close();
            long end = System.currentTimeMillis();
            time += (end - start);
            time2 += (start - begin);
        }
        System.out.println(time2);
        System.out.println(time/1000);
    }

    /**
     * 动态拼接sql方式批量插入，10W->4s
     */
    @Test
    void saveCarBatch(){
        long time = 0;
        long time2 = 0;
        int index = 10000000;
        for (int j = 0;j<100;j++){
            System.out.println("open:" + j);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH,false);
            CarMapper mapper = sqlSession.getMapper(CarMapper.class);
            List<Car> carList = new ArrayList<>();
            long begin = System.currentTimeMillis();
            for (int i=0;i<200000;i++){
                carList.add(new Car((long) index,"dazhong_" + index, index));
                index++;
            }
            long start = System.currentTimeMillis();
            mapper.insertBatch(carList);
            sqlSession.commit();
            sqlSession.clearCache();
            sqlSession.close();
            long end = System.currentTimeMillis();
            time += (end - start);
            time2 += (start - begin);
        }
        System.out.println(time2);
        System.out.println(time/1000);
    }

    /**
     * 测试通过主键查询对比
     */
    @Test
    void selectTest(){
        long start = System.currentTimeMillis();
        User user = userMapper.selectByPrimaryKey(831182741469921282L);
        long end = System.currentTimeMillis();
        System.out.println("time:" + (end - start));    //498ms
        System.out.println(user);

        long start2 = System.currentTimeMillis();
        Car cars = carMapper.selectByPrimaryKey(10000012L);
        long end2 = System.currentTimeMillis();
        System.out.println("time:" + (end2 - start2));  //6ms
        System.out.println(cars);
    }

    /**
     * 通过非主键查询对比
     */
    @Test
    void selectTest2(){
        UserExample example = new UserExample();
        example.createCriteria().andCarIdEqualTo(19999992L);
        long start = System.currentTimeMillis();
        List<User> user = userMapper.selectByExample(example);
        long end = System.currentTimeMillis();
        System.out.println("time:" + (end - start));    //8270ms
        System.out.println(user);

        CarExample example1 = new CarExample();
        example1.createCriteria().andNameEqualTo("dazhong_10000012");
        long start2 = System.currentTimeMillis();
        List<Car> cars = carMapper.selectByExample(example1);
        long end2 = System.currentTimeMillis();
        System.out.println("time:" + (end2 - start2));  //5530ms
        System.out.println(cars);
    }

    /**
     * 比较分表和不分表查询全部数据耗时,数据量大发生OOM
     */
    @Test
    void selectTest3(){
        long a = System.currentTimeMillis();
        List<User> users = userMapper.selectByExample(null);
        long b = System.currentTimeMillis();
        List<Car> cars = carMapper.selectByExample(null);
        long c = System.currentTimeMillis();
        System.out.println("users time:" + (b - a));
        System.out.println("cars time:" + (c - b));
    }

}
