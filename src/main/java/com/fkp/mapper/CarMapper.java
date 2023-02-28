package com.fkp.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fkp.domain.Car;
import com.fkp.domain.CarExample;
import com.fkp.domain.Person;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: fkp
 * @time: 2023-02-05 21:41:04
 * @description:
 */
public interface CarMapper {

    Page<Car> findPage(Page<Car> page);

    Page<Person> findPersonPage(Page<Person> page);

    long countByExample(CarExample example);

    int deleteByExample(CarExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Car row);

    int insertBatch(List<Car> carList);

    int insertSelective(Car row);

    List<Car> selectByExample(CarExample example);

    Car selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") Car row, @Param("example") CarExample example);

    int updateByExample(@Param("row") Car row, @Param("example") CarExample example);

    int updateByPrimaryKeySelective(Car row);

    int updateByPrimaryKey(Car row);
}
