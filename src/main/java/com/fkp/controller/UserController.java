package com.fkp.controller;

import com.alibaba.druid.pool.DruidDataSource;
import com.fkp.domain.DataSourceConfig;
import com.fkp.domain.User;
import com.fkp.mapper.UserMapper;
import com.fkp.util.ManualRegistBeanUtils;
import com.fkp.util.ReflectionUtils;
import com.fkp.util.Snowflake;
import com.fkp.util.StrIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.*;

@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConfigurableApplicationContext applicationContext;


    @GetMapping(value = "/save")
    public int save(String id, String city, String name){
        if(id == null){
//            id = StrIdGenerator.getInstance().nextId();
//            System.out.println(id);
//            System.out.println(id.hashCode());
//            System.out.println(Math.abs(id.hashCode()) % 3);
        }
        return userMapper.insert(new User(id, city, name, null));
    }

    @GetMapping(value = "/findAll")
    public List<User> findAll(){
        return userMapper.selectByExample(null);
    }

    @GetMapping(value = "/findById")
    public User findById(Long id){
        return userMapper.selectByPrimaryKey(id);
    }

    @PostMapping(value = "/addDataSource")
    public String addDataSource(@RequestBody DataSourceConfig config) {
        try {
            ShardingDataSource shardingDataSource = (ShardingDataSource) applicationContext.getBean("shardingDataSource");
            ShardingRuleConfiguration ruleConfiguration = shardingDataSource.getRuntimeContext().getRule().getRuleConfiguration();
            Properties props = shardingDataSource.getRuntimeContext().getProperties().getProps();
            Map<String, DataSource> dataSourceMap = shardingDataSource.getDataSourceMap();
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setDriverClassName(config.getDriverClassName());
            druidDataSource.setUrl(config.getUrl());
            druidDataSource.setUsername(config.getUsername());
            druidDataSource.setPassword(config.getPassword());
            dataSourceMap.put("ds" + dataSourceMap.size(), druidDataSource);
            DruidDataSource ds0 = (DruidDataSource) dataSourceMap.get("ds0");
            Object ds0New = ds0.clone();
            dataSourceMap.replace("ds0", (DataSource) ds0New);

            Collection<TableRuleConfiguration> tableRuleConfigs = new LinkedList<>();
            for (TableRuleConfiguration tableRuleConfig : ruleConfiguration.getTableRuleConfigs()) {
                TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration(tableRuleConfig.getLogicTable(), StringUtils.replaceOnce(tableRuleConfig.getActualDataNodes(), "0..0", "0.." + (dataSourceMap.size() - 1)));
                tableRuleConfiguration.setTableShardingStrategyConfig(tableRuleConfig.getTableShardingStrategyConfig());
                tableRuleConfiguration.setKeyGeneratorConfig(tableRuleConfig.getKeyGeneratorConfig());
                String shardingColumn = ((InlineShardingStrategyConfiguration) tableRuleConfig.getDatabaseShardingStrategyConfig()).getShardingColumn();
                String algorithmExpression = ((InlineShardingStrategyConfiguration) tableRuleConfig.getDatabaseShardingStrategyConfig()).getAlgorithmExpression();
                System.out.println(algorithmExpression);
                ShardingStrategyConfiguration databaseShardingStrategyConfig = new InlineShardingStrategyConfiguration(shardingColumn, "ds$->{id%2}");
                tableRuleConfiguration.setDatabaseShardingStrategyConfig(databaseShardingStrategyConfig);
                tableRuleConfigs.add(tableRuleConfiguration);
            }
            ruleConfiguration.setTableRuleConfigs(tableRuleConfigs);
            ShardingDataSource dataSource = ManualRegistBeanUtils.replaceBean(applicationContext, "shardingDataSource", ShardingDataSource.class, dataSourceMap, new ShardingRule(ruleConfiguration, dataSourceMap.keySet()), props);
            System.out.println(applicationContext.getBean(DataSource.class) == dataSource);
        }catch (Exception e){
            return "fail,msg: " + e.getMessage();
        }
        return "success";
    }

    @PostMapping(value = "/addDataSource2")
    public String addDataSource2(@RequestBody DataSourceConfig config){
        try {
            ShardingDataSource shardingDataSource = (ShardingDataSource) applicationContext.getBean("shardingDataSource");
            ShardingRuntimeContext runtimeContext = shardingDataSource.getRuntimeContext();
            Properties props = runtimeContext.getProperties().getProps();
            DatabaseType databaseType = runtimeContext.getDatabaseType();
            ShardingRuleConfiguration ruleConfiguration = runtimeContext.getRule().getRuleConfiguration();
            Map<String, DataSource> dataSourceMap = shardingDataSource.getDataSourceMap();
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setDriverClassName(config.getDriverClassName());
            druidDataSource.setUrl(config.getUrl());
            druidDataSource.setUsername(config.getUsername());
            druidDataSource.setPassword(config.getPassword());
            dataSourceMap.put("ds1", druidDataSource);
            for (TableRuleConfiguration tableRuleConfig : ruleConfiguration.getTableRuleConfigs()) {
                ReflectionUtils.setFieldValue(tableRuleConfig, "actualDataNodes", "ds$->{0..1}.user_$->{0..2}");
                InlineShardingStrategyConfiguration databaseShardingStrategyConfig = (InlineShardingStrategyConfiguration) tableRuleConfig.getDatabaseShardingStrategyConfig();
                String currId = StrIdGenerator.getInstance().nextId();
                ReflectionUtils.setFieldValue(databaseShardingStrategyConfig, "algorithmExpression", "ds$->{id>" + currId + "?1:0}");
            }
            ShardingRuntimeContext shardingRuntimeContext = new ShardingRuntimeContext(dataSourceMap, new ShardingRule(ruleConfiguration, dataSourceMap.keySet()), props, databaseType);
            ReflectionUtils.setFieldValue(shardingDataSource, "runtimeContext", shardingRuntimeContext);
            System.out.println(applicationContext.getBean(ShardingDataSource.class).getRuntimeContext() == shardingRuntimeContext);
        }catch (Exception e){
            e.printStackTrace();
            return "fail,msg: " + e.getMessage();
        }
        return "success";
    }

//    public static void main(String[] args) {
//        String s = "123456789012345678901234";
////        BigInteger bigInteger = new BigInteger(s);
////        System.out.println(bigInteger.compareTo(new BigInteger("0")));
//        System.out.println(s.compareTo(0));
//    }

}
