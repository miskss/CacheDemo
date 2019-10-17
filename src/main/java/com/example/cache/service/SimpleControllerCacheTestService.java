package com.example.cache.service;

import com.example.cache.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author peter
 * date: 2019-10-16 10:33
 **/
@Service
@Slf4j
public class SimpleControllerCacheTestService {

    //先从缓存中查找，如果没有则执行方法，最后将结果放入缓存中去
    @Cacheable(cacheNames = "myCache")
    public User getFromCache() {
        return null;
    }

    //@CachePut注解，它执行了方法并将返回值被放入缓存中
    @CachePut(cacheNames = "myCache")
    public User populateCache() {
        return new User(1, "ii", 20);
    }

    //删除缓存
    @CacheEvict(cacheNames = "myCache")
    public void removeCache() {
    }

    public User cacheInvalid(){
        User fromCache = getFromCache();
        if (fromCache == null) {
            log.info("缓存为空，查库填充");
            User newValue = populateCache();
            log.info("查库填充: {}", newValue);
            return newValue;
        }
        log.info("Returning from Cache: {}", fromCache);
        return fromCache;
    }




}
