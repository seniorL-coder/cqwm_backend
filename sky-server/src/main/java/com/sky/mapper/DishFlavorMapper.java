package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 插入菜品口味
     *
     * @param flavors
     */
    void insertFlavor(List<DishFlavor> flavors);

    /**
     * 批量删除菜品口味
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
