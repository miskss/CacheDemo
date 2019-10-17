package com.example.cache.service;

import com.example.cache.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author peter
 * date: 2019-10-16 11:10
 **/
@Service
@CacheConfig(cacheNames = "ControllerCache")
@Slf4j
public class ControllerCacheTestService {
    private static final String CONTROLLED_PREFIX = "user_";


    public static String getCacheKey(String relevant) {
        return CONTROLLED_PREFIX + relevant;
    }

    //
    @Cacheable(key = "'user_'.concat(#id)", unless = "#result==null")
    public User getFromCache(Integer id, String name) {
        log.info("=== getFromCache ===");
        return null;
    }

    @CachePut(key = "'user_'.concat(#result.id)")
    public User addUser(Integer id, String name, Integer age) {
        log.info("=== addUser ===");
        return new User(id, name, age);
    }

    @CacheEvict(key = "T(com.example.cache.service.ControllerCacheTestService).getCacheKey(#id)")
    public void delete(Integer id) {
        log.info("=== delete ===");
    }
}
