package com.sky.config;

import com.sky.utils.SnowflakeIdWorker;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sky.snowflake")
@Data // Lombok：生成 getter / setter
public class SnowflakeConfig {

    private long workerId;

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() {
        return new SnowflakeIdWorker(workerId);
    }
}
