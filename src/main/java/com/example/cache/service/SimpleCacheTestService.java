package com.example.cache.service;

import com.example.cache.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


/**
 * @author peter
 * date: 2019-10-16 09:52
 **/
@Service
@Slf4j
public class SimpleCacheTestService {

    @Cacheable(cacheNames = "cache")
    public User cache() {
        log.info("==== 方法内查询数据 ====");
        return new User(1, "xi", 19);
    }
}
