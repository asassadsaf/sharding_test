package com.fkp.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * <p>
 * 雪花算法生成id帮助类
 * </p>
 *
 * @author xiaovcloud
 * @since 2021/7/8 16:42
 */
@Component
public class SnowflakeHelper {
    /**
     * 起始时间
     */
    private static final long START_STMP = System.currentTimeMillis();
    /**
     * 机器码
     */
    private static long machineId;
    /**
     * 数据中心
     */
    private static long datacenterId;

    @Value("${id.snowflake.machineId:1}")
    public void setMachineId(long machine) {
        machineId = machine;
    }

    @Value("${id.snowflake.datacenterId:1}")
    public void setDatacenterId(long datacenter) {
        datacenterId = datacenter;
    }

    /**
     * 获取id
     *
     * @return id
     */
    public static long nextId() {
        Snowflake snowflake = new Snowflake(datacenterId, machineId);
        return snowflake.nextId();
    }

    /**
     * 根据雪花id 反推时间
     *
     * @param id 雪花id
     * @return 时间
     */
    public static Date getDateById(long id) {
        long timeStamp = getTimestampById(id);
        return new Date(timeStamp);
    }

    /**
     * 根据雪花id 反推时间戳
     *
     * @param id 雪花id
     * @return 时间戳
     */
    public static long getTimestampById(long id) {
        long timeStamp = id >> 22;
        timeStamp = timeStamp + START_STMP;
        return timeStamp;
    }
}
