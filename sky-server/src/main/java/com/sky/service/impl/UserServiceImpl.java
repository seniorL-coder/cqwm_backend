package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.util.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    public static final String WX_GET_OPENID_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private final WeChatProperties weChatProperties;
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;

    public UserServiceImpl(WeChatProperties weChatProperties, UserMapper userMapper, JwtProperties jwtProperties) {
        this.weChatProperties = weChatProperties;
        this.userMapper = userMapper;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 微信登录
     *
     * @param userLoginDTO
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());
        // 查询数据库是否存在该用户, 如果不存在写入数据库(注册)
        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            // 用户不存在 写入数据库
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.add(user);
        }

        // 创建token, 返回userVO
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        return UserLoginVO.builder().id(user.getId()).openid(openid).token(token).build();

    }

    public String getOpenid(String js_code) {

        HashMap<String, String> param = new HashMap<>();
        param.put("appid", weChatProperties.getAppid());
        param.put("secret", weChatProperties.getSecret());
        param.put("js_code", js_code);
        param.put("grant_type", "authorization_code");

        String response = HttpClientUtil.doGet(WX_GET_OPENID_URL, param);
        // 响应结果是JSON字符串, 需要解析
        JSONObject jsonObject = JSON.parseObject(response);
        // 拿出openid并返回
        return jsonObject.getString("openid");

    }
}


