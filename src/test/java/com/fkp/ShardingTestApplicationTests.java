package com.fkp;

import com.fkp.domain.Car;
import com.fkp.mapper.CarMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ShardingTestApplicationTests {

    @Autowired
    private CarMapper carMapper;

    @Test
    void contextLoads() {
        List<Car> cars = carMapper.selectByExample(null);
        System.out.println(cars);
    }

}
