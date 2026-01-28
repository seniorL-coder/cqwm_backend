package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;

    /**
     * 添加菜品和口味
     *
     * @param dishDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFlavor(DishDTO dishDTO) {

        // 1. 先保存菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insertDish(dish);

        Long id = dish.getId();

        // 2. 保存口味
        //2.1 拿到口味List
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            // 给每一个口味赋值 菜品id
            flavors.forEach(item -> item.setDishId(id));
            dishFlavorMapper.insertFlavor(flavors);
        }

    }
}
