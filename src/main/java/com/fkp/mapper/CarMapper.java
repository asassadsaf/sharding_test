package com.fkp.mapper;

import com.fkp.domain.Car;
import com.fkp.domain.CarExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: fkp
 * @time: 2023-02-05 21:41:04
 * @description:
 */
public interface CarMapper {
    long countByExample(CarExample example);

    int deleteByExample(CarExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Car row);

    int insertSelective(Car row);

    List<Car> selectByExample(CarExample example);

    Car selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") Car row, @Param("example") CarExample example);

    int updateByExample(@Param("row") Car row, @Param("example") CarExample example);

    int updateByPrimaryKeySelective(Car row);

    int updateByPrimaryKey(Car row);
}
