package com.fkp.controller;

import com.alibaba.druid.pool.DruidDataSource;
import com.fkp.domain.DataSourceConfig;
import com.fkp.domain.User;
import com.fkp.mapper.UserMapper;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.spring.boot.common.SpringBootPropertiesConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.spring.boot.sharding.SpringBootShardingRuleConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConfigurableApplicationContext applicationContext;


    @GetMapping(value = "/save")
    public int save(Long id, String city, String name){
        return userMapper.insert(new User(id, city, name, null));
    }

    @GetMapping(value = "/findAll")
    public List<User> findAll(){
        return userMapper.selectByExample(null);
    }

    @GetMapping(value = "/addDataSource")
    public String addDataSource(DataSourceConfig config) {
        try {
            ShardingDataSource shardingDataSource = (ShardingDataSource) applicationContext.getBean("shardingDataSource");
            SpringBootShardingRuleConfigurationProperties ruleConfigurationProperties = applicationContext.getBean(SpringBootShardingRuleConfigurationProperties.class);
            SpringBootPropertiesConfigurationProperties propertiesConfigurationProperties = applicationContext.getBean(SpringBootPropertiesConfigurationProperties.class);
            Map<String, DataSource> dataSourceMap = shardingDataSource.getDataSourceMap();
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setDriverClassName(config.getDriverClassName());
            druidDataSource.setUrl(config.getUrl());
            druidDataSource.setUsername(config.getUsername());
            druidDataSource.setPassword(config.getPassword());
            dataSourceMap.put("ds1", druidDataSource);
            DruidDataSource ds0 = (DruidDataSource) dataSourceMap.get("ds0");
            Object ds0New = ds0.clone();
            dataSourceMap.replace("ds0", (DataSource) ds0New);
            ((BeanDefinitionRegistry) applicationContext).removeBeanDefinition("shardingDataSource");
            DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, new ShardingRuleConfigurationYamlSwapper().swap(ruleConfigurationProperties), propertiesConfigurationProperties.getProps());
            applicationContext.getBeanFactory().registerSingleton("shardingDataSource", dataSource);
        }catch (Exception e){
            return "fail,msg: " + e.getMessage();
        }
        return "success";
    }

}
