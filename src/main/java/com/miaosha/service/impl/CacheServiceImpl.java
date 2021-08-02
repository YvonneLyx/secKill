package com.miaosha.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miaosha.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;


@Service
public class CacheServiceImpl implements CacheService {

    private Cache<Object, Object> commonCache = null;

    @PostConstruct  //springbean 加载bean时先加载这个
    public void init() {
        commonCache = CacheBuilder.newBuilder()
                .initialCapacity(10)    //设置缓存容器的初始容量为10
                .maximumSize(100)       //设置缓存中最大可以存储100个key，超过100个会按照LRU的策略移除缓存项
                .expireAfterWrite(60, TimeUnit.SECONDS).build();    //设置写缓存后多少秒过期
    }


    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key, value);
    }

    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }
}
