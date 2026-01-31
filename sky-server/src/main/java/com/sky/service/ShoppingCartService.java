package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 购物车列表
     * @return
     */
    List<ShoppingCart> list();

    /**
     * 清空购物车
     */
    void deleteAll();

    /**
     * 减少购物车中商品的数量(删除购物车中一个商品)
     * @param shoppingCartDTO
     */
    void subtract(ShoppingCartDTO shoppingCartDTO);
}
