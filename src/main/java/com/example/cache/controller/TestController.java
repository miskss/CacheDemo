package com.example.cache.controller;

import com.example.cache.entity.User;
import com.example.cache.service.ControllerCacheTestService;
import com.example.cache.service.SimpleCacheTestService;
import com.example.cache.service.SimpleControllerCacheTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author peter
 * date: 2019-10-16 09:59
 **/
@RestController
@RequestMapping
@Slf4j
public class TestController {

    private SimpleCacheTestService simpleCacheTestService;
    private SimpleControllerCacheTestService simpleControllerCacheTestService;
    private ControllerCacheTestService controllerCacheTestService;

    public TestController(SimpleCacheTestService simpleCacheTestService, SimpleControllerCacheTestService simpleControllerCacheTestService, ControllerCacheTestService controllerCacheTestService) {
        this.simpleCacheTestService = simpleCacheTestService;
        this.simpleControllerCacheTestService = simpleControllerCacheTestService;
        this.controllerCacheTestService = controllerCacheTestService;
    }

    @GetMapping("/test")
    public User test() {
        return simpleCacheTestService.cache();
    }


    @GetMapping("/test1")
    public User test1() {
        User fromCache = simpleControllerCacheTestService.getFromCache();
        if (fromCache == null) {
            log.info("缓存为空，查库填充");
            User newValue = simpleControllerCacheTestService.populateCache();
            log.info("查库填充: {}", newValue);
            return newValue;
        }
        log.info("Returning from Cache: {}", fromCache);
        return fromCache;
    }

    @GetMapping("/remove1")
    public void removeTest1(){
        simpleControllerCacheTestService.removeCache();
    }

    @GetMapping("/test2")
    public User test2(){
      return   controllerCacheTestService.getFromCache(1,"iii");
    }

    @GetMapping("/test3")
    public User test3(){
        return   controllerCacheTestService.addUser(1,"iii",20);
    }

    @GetMapping("/remove2")
    public void removeTest2(){
        controllerCacheTestService.delete(1);
    }

    @GetMapping("/test4")
    public User test4(){
        return simpleControllerCacheTestService.cacheInvalid();
    }

}
