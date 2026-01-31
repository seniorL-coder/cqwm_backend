package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SetmealServiceImpl implements SetmealService {
    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final DishMapper dishMapper;


    /**
     * 新增套餐
     *
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SetmealDTO setmealDTO) {
        // 1. 新增套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //向套餐表插入数据
        setmealMapper.insert(setmeal);

        // 2. 新增套餐菜品的对应关系信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        Long setmealId = setmeal.getId();
        if (setmealDishes.isEmpty()) {
            return;
        }
        setmealDishes.forEach(item -> item.setSetmealId(setmealId));
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        int page = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();
        PageHelper.startPage(page, pageSize);

        Page<SetmealVO> pageInfo = setmealMapper.page();
        long total = pageInfo.getTotal();
        List<SetmealVO> result = pageInfo.getResult();
        return new PageResult(total, result);
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        // 起售中的套餐不允许删除
        List<Integer> setmealIds = setmealMapper.getByIds(ids);
        // 判断套餐是否是起售状态
        if (!setmealIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        // 删除套餐
        setmealMapper.deleteBatch(ids);
        // 删除套餐要同步删除关联的菜品
        setmealDishMapper.deleteBatch(ids);
    }

    /**
     * 套餐起售停售
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 起售套餐时，如果套餐内包含停售的菜品，则不能起售
        if (Objects.equals(status, StatusConstant.ENABLE)) {
            // 获取该套餐的所有菜品id
            List<Long> currSetmealDishIds = setmealDishMapper.getDishIdsBySetmealId(id);
            List<Long> dishIds = dishMapper.getDishIds(currSetmealDishIds);
            if (!CollectionUtils.isEmpty(currSetmealDishIds)) { // 判断dishIds是否为空
                // 先查套餐所有的菜品id, 再去查dish所有状态status = 1 的菜品id, 比对二者, 如果不同, 说明有停售菜品, 直接抛出异常
                if ((dishIds.size() != currSetmealDishIds.size())) {
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealMapper.update(setmeal);

    }

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdSetmealWithDish(Long id) {
        return setmealMapper.getByIdSetmealWithDish(id);
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SetmealDTO setmealDTO) {
        // 1. 修改套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        // 2. 修改套餐关联的菜品信息
        // 2.1 删除原有的套餐信息
        Long setmealId = setmealDTO.getId();
        setmealDishMapper.deleteBatch(List.of(setmealId));
        // 2.2 插入最新传入的套餐信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(item -> item.setSetmealId(setmealId));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

}
