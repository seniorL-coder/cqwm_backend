package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisTemplateConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建 Redis 模版类");
        // 创建RedisTemplate模版对象
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 设置Redis的连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置 key 的 序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // ===== 重点：自定义 ObjectMapper =====
        ObjectMapper objectMapper = new ObjectMapper();
        // 告诉 Jackson：
        //以后遇到 LocalDateTime / LocalDate / LocalTime
        //用 Java 8 时间规则 来序列化！
        objectMapper.registerModule(new JavaTimeModule());

        //这一行的作用就是：
        //❌ 不要时间戳 / 数组 -> "updateTime": [2026,1,31,14,45,29]
        //✅ 用 可读的字符串格式 -> "updateTime": "2026-01-31T14:45:29"
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);

        return redisTemplate;
    }
}
