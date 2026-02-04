package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private final OrderMapper orderMapper;
    private final WorkspaceService workspaceService;

    public ReportServiceImpl(OrderMapper orderMapper, WorkspaceService workspaceService) {
        this.orderMapper = orderMapper;
        this.workspaceService = workspaceService;
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

    /**
     * 销量排名统计 top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names, ","))
                .numberList(StringUtils.join(numbers, ","))
                .build();
    }

    /**
     * 导出营业数据
     *
     * @param response
     */
    @Override
    public void export(HttpServletResponse response) {
        // 导出最近30天的营业数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);


        // 获取需要写入文件的模版路径
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        if (in == null) {
            throw new RuntimeException("模板文件不存在");
        }
        try {
            // 通过poi写入excel
            XSSFWorkbook excel = new XSSFWorkbook(in);
            BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);

            XSSFSheet sheet1 = excel.getSheet("Sheet1");

            XSSFRow row = sheet1.getRow(1);
            // 填写第二行的营业数据时间
            row.getCell(1).setCellValue("时间: " +
                    beginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                    "-" +
                    endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            row = sheet1.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row = sheet1.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice().doubleValue());
            row.getCell(5).setCellValue(businessData.getNewUsers());

            for (int i = 0; i < 30; i++) {
                row = sheet1.getRow(i + 7);
                LocalDate currentDate = begin.plusDays(i);  // 明确的当前日期
                LocalDateTime dayBegin = LocalDateTime.of(currentDate, LocalTime.MIN);
                LocalDateTime dayEnd = LocalDateTime.of(currentDate, LocalTime.MAX);
                businessData = workspaceService.getBusinessData(dayBegin, dayEnd);

                // 日期
                row.getCell(1).setCellValue(dayBegin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                // 营业额
                row.getCell(2).setCellValue(businessData.getTurnover());
                // 有效订单数
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                // 订单完成率
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                // 平均客单价
                row.getCell(5).setCellValue(businessData.getUnitPrice().doubleValue());
                // 新增用户数
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 写入输出流
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.flush();
            outputStream.close();
            excel.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("导出失败");
        }


    }
}
