package com.example.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author peter
 * date: 2019-10-16 15:06
 **/
@Data
@ConfigurationProperties(prefix = "spring.cache.custom")
public class CacheConfigurationProperties {
    private Map<String, Duration> cacheKeyExpireTimes = new HashMap<>();

}
