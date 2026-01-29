package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api("店铺操作相关接口")
@RequiredArgsConstructor
public class ShopController {
    private static final String SHOP_KEY = "SHOP_KEY";
    private final RedisTemplate redisTemplate;


    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<?> getShopStatus() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer status = (Integer) valueOperations.get(SHOP_KEY);
        return Result.success(status);
    }
}
