package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
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
}
