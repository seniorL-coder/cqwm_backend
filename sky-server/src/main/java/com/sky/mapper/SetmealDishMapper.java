package com.sky.mapper;

import com.sky.entity.Setmeal;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 获取被关联的菜品id
     * @param ids
     */
    List<Long> getSetmealIdsByDishIds(List<Long> ids);

}
