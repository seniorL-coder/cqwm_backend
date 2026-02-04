package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api("店铺操作相关接口")
@RequiredArgsConstructor
public class ShopController {
    private static final String SHOP_KEY = "SHOP_KEY";
    private final RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result<?> setShopStatus(@PathVariable Integer status) {
        ValueOperations<Object, Object> valueOperations = redisTemplate.opsForValue();
        log.info("设置店铺状态为: {}", status);
        valueOperations.set(SHOP_KEY, status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<?> getShopStatus() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer status = (Integer) valueOperations.get(SHOP_KEY);
        if (status == null) {
            status = 0;
        }
        log.info("获取店铺状态: {}", status == 0 ? "打烊": "营业");
        return Result.success(status);
    }
}
