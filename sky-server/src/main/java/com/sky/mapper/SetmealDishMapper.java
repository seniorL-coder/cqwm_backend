package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 获取被关联的菜品id
     * @param ids
     */
    List<Long> getSetmealIdsByDishIds(List<Long> ids);

    /**
     * 保存套餐和菜品的关联关系
     * @param setmealDishes
     */
    @AutoFill(OperationType.INSERT)
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 批量删除套餐 菜品 关联表的菜品信息
     * @param ids
     */
    void deleteBatch(List<Integer> ids);
}
