package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class ReportServiceImpl implements ReportService {
    private final OrderMapper orderMapper;

    public ReportServiceImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        // 日期
        ArrayList<LocalDate> dateList = new ArrayList<>();
        // 营业额
        ArrayList<Double> turnoverList = new ArrayList<>();
        // 循环 如果 begin 小于 end 就 添加 begin 到 dateList, 并且 begin 加 1
        while (begin.isBefore(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        for (LocalDate date : dateList) {
            // 转成 LocalDateTime 格式
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            HashMap<String, Object> map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED); // 已完成的订单 -> 5
            Double turnover = orderMapper.getTurnoverStatistics(map);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }

        // 返回结果
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {

        // 日期
        ArrayList<LocalDate> dateList = new ArrayList<>();
        //  总用户数
        ArrayList<Integer> totalUserList = new ArrayList<>();
        // 新增用户数
        ArrayList<Integer> newUserList = new ArrayList<>();
        // 循环 如果 begin 小于 end 就 添加 begin 到 dateList, 并且 begin 加 1
        while (begin.isBefore(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        for (LocalDate date : dateList) {
            // 转成 LocalDateTime 格式
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            HashMap<String, Object> map = new HashMap<>();

            map.put("endTime", endTime);
            Integer count = orderMapper.getUserStatistics(map); // 当天之前的总用户数,包括当天
            totalUserList.add(count == null ? 0 : count);
            map.put("beginTime", beginTime);
            Integer newUserCount = orderMapper.getUserStatistics(map); // 当天新增用户数
            newUserList.add(newUserCount == null ? 0 : newUserCount);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        // 日期
        ArrayList<LocalDate> dateList = new ArrayList<>();
        //        //每日订单数，以逗号分隔，例如：260,210,215

        ArrayList<Integer> orderCountList = new ArrayList<>(); // 每日订单数
        ArrayList<Integer> validOrderCountList = new ArrayList<>(); // 每日有效订单数
        int totalOrderCount; // 订单总数
        Integer validOrderCount; // 有效订单数
        double orderCompletionRate = 0.0; // 订单完成率

        // 循环 如果 begin 小于 end 就 添加 begin 到 dateList, 并且 begin 加 1
        while (begin.isBefore(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            HashMap<String, Object> map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            Integer ordersStatistics = orderMapper.getOrdersStatistics(map);
            orderCountList.add(ordersStatistics == null ? 0 : ordersStatistics);
            map.put("status", Orders.COMPLETED);
            Integer completedOrdersStatistics = orderMapper.getOrdersStatistics(map);
            validOrderCountList.add(completedOrdersStatistics == null ? 0 : completedOrdersStatistics);
        }

        // 订单总数
        // totalOrderCount = orderCountList.stream().reduce(0, Integer::sum);
        totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        // 有效订单数
        validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        // 订单完成率
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }


        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
}
