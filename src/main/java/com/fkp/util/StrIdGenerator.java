package com.fkp.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 分布式ID生成器，用于生成字符串主键
 */
public class StrIdGenerator {

    private static StrIdGenerator instance = new StrIdGenerator();

    /**
     * 起始的时间戳 2021-01-06 06:08:56
     */
    private static final long START_TIMESTAMP = 1609884536668L;


    // 每一部分占用的位数
    private static final int SEQUENCE_BIT = 9; //序列号占用的位数
    private static final int MACHINE_BIT = 32;   //机器标识占用的位数

    // 每一部分的最大值
    private static final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);
    private static final long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);

    // 每一部分向左的位移
    private static final int MACHINE_LEFT = SEQUENCE_BIT;
    private static final int TIMESTAMP_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    // 最大回拨15ms
    private static final long MAX_CLOCK_MOVED_TIMESTAMP = 15;

    private long machineId;     //机器标识
    private long sequence = 0L; //序列号
    private long lastTimestamp = -1L;//上一次时间戳

    private StrIdGenerator() {
        long instanceId = getMachineId();
        if (instanceId > MAX_MACHINE_NUM || instanceId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.machineId = instanceId;
    }

    /**
     * 产生下一个ID
     *
     * @return
     */
    public synchronized String nextId() {

        long currStmp = getCurrTimestamp();
        if (currStmp < lastTimestamp) {
            // 解决时钟回调问题
            proccessClockForward(currStmp);
        }

        if (currStmp == lastTimestamp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = tillNextMillisecond();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastTimestamp = getCurrTimestamp();

        long idLow = machineId << MACHINE_LEFT | sequence;
        BigInteger idHign = BigInteger.valueOf(currStmp - START_TIMESTAMP).shiftLeft(TIMESTAMP_LEFT);
        BigInteger id = idHign.add(BigInteger.valueOf(idLow));

        return id.toString(10);
    }

    /**
     * 获取指定数量的ID
     * @param maxNum
     * @return
     */
    public List<String> nextIdByBatch(int maxNum) {
        List<String> ids = new ArrayList<>(maxNum);
        for (int i = 0; i < maxNum; i++) {
            ids.add(nextId());
        }
        return ids;
    }

    private void proccessClockForward(long currTimestamp) {
        long backForwardTime = lastTimestamp - currTimestamp;
        while(backForwardTime <= MAX_CLOCK_MOVED_TIMESTAMP) {
            backForwardTime = lastTimestamp - getCurrTimestamp();
            if(backForwardTime < 0) {
                break;
            }
        }

        //  若回拨时间大于 15 ms ，则生成新的
        if(backForwardTime > MAX_CLOCK_MOVED_TIMESTAMP) {
            this.machineId = getMachineId();
        }
    }

    private long tillNextMillisecond() {
        long mill = getCurrTimestamp();
        while (mill <= lastTimestamp) {
            mill = getCurrTimestamp();
        }
        return mill;
    }

    private long getCurrTimestamp() {
        return System.currentTimeMillis();
    }

    private static int getMachineId() {
        Integer uuid = UUID.randomUUID().hashCode();
        uuid = uuid < 0 ? -uuid : uuid;
        return uuid;
    }

    public static synchronized StrIdGenerator getInstance() {
        if(instance == null) {
            instance = new StrIdGenerator();
        }
        return instance;
    }

    /**
     * 获取生成该id时的时间戳
     *
     * @param id id
     * @return 生成id时的时间戳
     */
    public long getTimestamp(String id) {
        BigInteger bigInteger = new BigInteger(id);
        return START_TIMESTAMP + bigInteger.shiftRight(SEQUENCE_BIT + MACHINE_BIT).longValue();
    }

}
