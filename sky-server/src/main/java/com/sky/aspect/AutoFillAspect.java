package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    } // 切入点

    @Before("autoFillPointCut()")
    public void autoFile(JoinPoint jp) {
        log.info("准备切入代码...");
        // 获取方法签名对象
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();

        // 获取方法上的注解对象
        AutoFill annotation = methodSignature.getMethod().getAnnotation(AutoFill.class);
        // 拿到注解对象的值, 判断是 insert update
        OperationType operationType = annotation.value();

        // 拿到方法的第一个参数 -> 实体类
        Object[] args = jp.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];

        if (operationType == OperationType.INSERT) {
            // insert
            // 通过反射, 拿到四个set方法
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                LocalDateTime now = LocalDateTime.now();
                Long currentId = BaseContext.getCurrentId();
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (operationType == OperationType.UPDATE) {
            // update
            // 通过反射, 拿到两个set方法
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                LocalDateTime now = LocalDateTime.now();
                Long currentId = BaseContext.getCurrentId();

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }


    }

}
