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
}
