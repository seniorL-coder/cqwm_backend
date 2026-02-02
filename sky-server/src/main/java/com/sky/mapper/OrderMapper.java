package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据条件查询订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> userPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     *
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据状态统计订单数量
     *
     * @param status
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 根据状态和订单时间查询订单
     *
     * @param pendingPayment
     * @param time
     */
    @Select("select * from orders where status = #{pendingPayment} and order_time < #{time}")
    List<Orders> getStatusAndOrderTimeLT(Integer pendingPayment, LocalDateTime time);

    /**
     * 营业额统计
     * @param map
     * @return
     */
    Double getTurnoverStatistics(Map<String, Object> map);

    /**
     * 用户统计
     * @param map
     * @return
     */
    Integer getUserStatistics(HashMap<String, Object> map);
}
