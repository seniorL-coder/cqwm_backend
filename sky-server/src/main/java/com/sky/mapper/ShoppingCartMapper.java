package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 查询单条商品购物车数据
     *
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 更新购物车单条商品数量
     *
     * @param currShopping
     */
    @Update("update shopping_cart set  number = #{number} where id = #{id}")
    void updateShoppingCartNumber(ShoppingCart currShopping);

    /**
     * 插入购物车数据
     *
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart " +
            "(name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values " +
            "(#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 删除购物车数据
     *
     * @param currentId
     */
    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void clean(Long currentId);
}
