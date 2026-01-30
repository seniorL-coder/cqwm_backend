package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.vo.UserLoginVO;

public interface UserService {

    /**
     * 微信登陆
     * @param userLoginDTO
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);
}
