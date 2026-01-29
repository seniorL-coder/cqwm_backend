package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final SetmealMapper setmealMapper;

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

    /**
     * 分页查询菜品
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 拿到页码 每页记录数
        int page = dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize();
        PageHelper.startPage(page, pageSize);

        Page<DishVO> dishVOPage = dishMapper.pageQuery(dishPageQueryDTO);

        long total = dishVOPage.getTotal();
        List<DishVO> result = dishVOPage.getResult();
        return new PageResult(total, result);
    }

    /**
     * 批量删除菜品
     *
     * @param ids 需要删除菜品的id -> List
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        // 删除之前要判断该菜品 1. 是否停售 2. 是否包被某个套餐关联
        // 1. 判断是否停售
        List<Long> dishIds = dishMapper.getDishIds(ids);
        if (!dishIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        //2. 判断是否被某个套餐关联
        List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (!setmealIdsByDishIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 3. 执行删除操作
        // 3.1 删除菜品
        dishMapper.deleteBatch(ids);
        // 3.2 删除菜品对应的口味
        dishFlavorMapper.deleteBatch(ids);


    }

    /**
     * 根据id查询菜品
     *
     * @param id 菜品id
     */
    @Override
    public DishVO getById(Long id) {
        return dishMapper.getById(id);
    }

    /**
     * 修改菜品
     *
     * @param dishVO
     */
    @Override
    public void update(DishVO dishVO) {
        // 1. 修改菜品基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishVO, dish);
        dishMapper.update(dish);
        // 2. 修改口味
        List<DishFlavor> flavors = dishVO.getFlavors();

        Long dishId = dish.getId();
        // 2.1 批量删除口味
        dishFlavorMapper.deleteBatch(List.of(dishId));

        if (!flavors.isEmpty()) {
            flavors.forEach(item -> item.setDishId(dishId));

            // 2.2 重新批量插入口味
            dishFlavorMapper.insertFlavor(flavors);
        }
    }

    /**
     * 菜品起售、停售
     *
     * @param status 菜品状态 菜品状态：1为起售，0为停售
     * @param id     菜品id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 1. 停售菜品
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
        // 2. 停售包含该菜品的套餐
        // 2.1 拿到包含该菜品的套餐id
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(List.of(id));
        if (!setmealIds.isEmpty()) {
            // 2.2 设置status 为 0
            for (Long setmealId : setmealIds) {
                Setmeal setmeal = Setmeal.builder().status(StatusConstant.DISABLE).id(setmealId).build();
                setmealMapper.update(setmeal);
            }
        }


    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId 分类id
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder().status(StatusConstant.ENABLE).categoryId(categoryId).build();

        return dishMapper.list(dish);
    }
}
