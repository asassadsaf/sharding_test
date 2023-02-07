package com.fkp;

import com.fkp.domain.Car;
import com.fkp.domain.User;
import com.fkp.mapper.CarMapper;
import com.fkp.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ShardingTestApplicationTests {

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        List<User> users = userMapper.selectByExample(null);
        System.out.println(users);
    }

}
