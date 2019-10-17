[TOC]


## 前言
   
最近新项目中要使用缓存，但是发现的之前的缓存知识忘的差不多了，所以又重新梳理了一下记录下来。

## Redis安装

由于Redis官方目前没有提供windows的版本，所以使用微软提供的redis版本
 [redis下载地址](https://github.com/MicrosoftArchive/redis/releases)   
 
## 在Springboot中引入缓存
#### 1.依赖
```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
```
#### 2.配置缓存
在`application.yml`中 配置redis和cache
```yaml
spring:
  redis:
    database: 2
    lettuce:
      pool:
        max-wait: 1000
        max-idle: 200
        max-active: 200
    timeout: 1000
  cache:
    redis:
    ## 不缓存null值 
       cache-null-values: false
    
```
在`@SpringBootApplication`类上加入`@EnableCaching`即可开启系统中的缓存
Spring Cache Abstraction 相关注解 
* `@Cacheable` 触发缓存添加
* `@CacheEvict`触发移除缓存
* `@CachePut`  更新缓存，但不会干扰方法的执行
* `@Caching`   组合要应用于方法的多个缓存操作
* `@CacheConfig` 在类级别共享一些与缓存相关的常见设置

[Spring Cache Abstraction 官方文档](https://docs.spring.io/spring/docs/4.1.x/spring-framework-reference/html/cache.html)

#### 3.简单示例
<span id="user">实体类User</span>
```java
@Data
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 6119134703242129065L;
    private Integer id;
    private String name;
    private Integer age;
}

```
创建一个`SimpleCacheTestService` 类 
```java
  @Service
  @Slf4j
  public class SimpleCacheTestService {
  
      @Cacheable(cacheNames = "cache")
      public User cache() {
          log.info("==== 方法内查询数据 ====");
          //模拟查库的接口
          return new User(1, "xi", 19);
      }
  }
```
测试接口
```java
    @GetMapping("/test")
    public User test() {
        return simpleCacheTestService.cache();
    }
```
第一次调用`/test`接口后，即可缓存结果，第二次请求时会走缓存，不会再执行方法，在redis中也可以看到缓存的结果。

```
C:\Users\Administrator>redis-cli -h 127.0.0.1
127.0.0.1:6379> select 2
OK
127.0.0.1:6379[2]> keys *
1) "cache::SimpleKey []"
127.0.0.1:6379[2]>

```
## 4.简单控制的示例
创建一个新的`SimpleControllerCacheTestService`类

```java
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
}
```
接口
```java
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
```
调用两次`/test1`接口，在调用`/remove1`接口
```text
  INFO 10204 --- [nio-1025-exec-4] c.e.cache.controller.TestController      : 缓存为空，查库填充
  INFO 10204 --- [nio-1025-exec-4] c.e.cache.controller.TestController      : 查库填充: User(id=1, name=ii, age=20)
  INFO 10204 --- [nio-1025-exec-5] c.e.cache.controller.TestController      : Returning from Cache: User(id=1, name=ii, age=20)
```
## 根据方法的参数或结果来插入缓存的示例

创建一个新的`ControllerCacheTestService`类

```java
@Service
@CacheConfig(cacheNames = "ControllerCache")
@Slf4j
public class ControllerCacheTestService {
    private static final String CONTROLLED_PREFIX = "user_";

    public static String getCacheKey(String relevant) {
        return CONTROLLED_PREFIX + relevant;
    }


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

```
+ 缓存key的生成方式使用的是 [SpEL表达式](https://docs.spring.io/spring/docs/5.2.0.RELEASE/spring-framework-reference/core.html#expressions),
+ 在redis中存储的`key = cacheNames+key`  
+ `#result`  方法返回的结果

接口测试
```java
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
```
## 缓存的TTL(过期时间)
#### 1.全局的TTL设置
```YAML
  cache:
    redis:
       ## 缓存的存活时间 100 秒
      time-to-live: 100s
```
#### 2.对某些cacheNames进行定制
考虑到后期扩展，所以将需要定制的cacheName 写入到配置文件中 去。
 ```yaml
spring:
  cache:
    ##定制缓存 的到期时间
    custom:
      cache-key-expire-times:
        ##myCache 的缓存时间为20s
        myCache: 20s
```
定义一个spring.cache.custom.cacheKeyExpireTimes 的key,值为Map(cacheName->过期时间).
创建一个配置文件`CacheConfigurationProperties`用于读取这些配置
```java
@Data
@ConfigurationProperties(prefix = "spring.cache.custom")
public class CacheConfigurationProperties {
    private Map<String, Duration> cacheKeyExpireTimes = new HashMap<>();
}
```
配置CacheManager
```java
@Configuration
@Import({CacheConfigurationProperties.class, CacheProperties.class})
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, CacheConfigurationProperties properties, CacheProperties cacheProperties) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        for (Map.Entry<String, Duration> cacheNameAndTimeout : properties.getCacheKeyExpireTimes().entrySet()) {
            cacheConfigurations.put(cacheNameAndTimeout.getKey(), createCacheConfiguration(cacheNameAndTimeout.getValue()));
        }

        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(createCacheConfiguration(cacheProperties.getRedis().getTimeToLive()))
                .withInitialCacheConfigurations(cacheConfigurations).build();
    }
    private static RedisCacheConfiguration createCacheConfiguration(Duration timeoutInSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(timeoutInSeconds);
    }
}
```
## 使用缓存方法的注意事项

** 绝对不要在同一类中调用缓存的方法 。**因为 Spring Cache 代理了缓存方法的访问，以使Cache Abstraction起作用，在同一个类中调用时会使代理失效。

## [源码地址](https://github.com/miskss/CacheDemo)
