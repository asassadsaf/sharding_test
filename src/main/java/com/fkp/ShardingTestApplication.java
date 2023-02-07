package com.fkp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.fkp.mapper")
public class ShardingTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShardingTestApplication.class, args);
    }

}
