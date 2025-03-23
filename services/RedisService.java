package com.postify.main.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public <T> void storeData(String key, T value, Long ttl, TimeUnit unit){
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        if (ttl != null && unit != null){
            valueOps.set(key,value,ttl,unit);
        }
        else {
            valueOps.set(key, value);
        }
    }

    public <T> T retrieveData(String key, Class<T> clasz){
        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        Object value = valueOps.get(key);
        if (value != null){
            return objectMapper.convertValue(value, clasz);
        }
        return null;
    }
}
