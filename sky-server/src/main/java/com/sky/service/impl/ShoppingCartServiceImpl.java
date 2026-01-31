package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;


    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 首先要查询表中有没有存在的相同记录, 有 number+1, 没有 就直接 插入数据
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setDishId(shoppingCartDTO.getDishId());
        shoppingCart.setSetmealId(shoppingCartDTO.getSetmealId());
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 查询 shopping_cart表, 看看有没有相同记录
       List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

       if(list!=null && !list.isEmpty()){
           // 已经有该条商品记录 只需 number++
           ShoppingCart currShopping = list.get(0);
           currShopping.setNumber(currShopping.getNumber()+1);
           shoppingCartMapper.updateShoppingCartNumber(currShopping);
       }else {
           // 需要添加新的数据
           // 判断是菜品 还是 套餐
           Long dishId = shoppingCartDTO.getDishId();
           if(dishId != null) {
               // 添加单个菜品到购物车
               DishVO dish = dishMapper.getById(shoppingCartDTO.getDishId());
               shoppingCart.setName(dish.getName());
               shoppingCart.setAmount(dish.getPrice());
               shoppingCart.setImage(dish.getImage());
           }else {
               // 添加 套餐到购物车
               SetmealVO setmealVO = setmealMapper.getByIdSetmealWithDish(shoppingCartDTO.getSetmealId());
               shoppingCart.setName(setmealVO.getName());
               shoppingCart.setAmount(setmealVO.getPrice());
               shoppingCart.setImage(setmealVO.getImage());
           }

           shoppingCart.setNumber(1);
           shoppingCart.setCreateTime(LocalDateTime.now());

           // 添加
           shoppingCartMapper.insert(shoppingCart);

       }

    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 清空购物车
     */
    @Override
    public void deleteAll() {
        shoppingCartMapper.clean(BaseContext.getCurrentId());
    }
}
