package com.fkp.mapper;

import com.fkp.domain.User;
import com.fkp.domain.UserExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: fkp
 * @time: 2023-02-05 21:41:04
 * @description:
 */
public interface UserMapper {
    long countByExample(UserExample example);

    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(User row);

    int insertSelective(User row);

    List<User> selectByExample(UserExample example);

    User selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") User row, @Param("example") UserExample example);

    int updateByExample(@Param("row") User row, @Param("example") UserExample example);

    int updateByPrimaryKeySelective(User row);

    int updateByPrimaryKey(User row);
}
