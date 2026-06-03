package com.smarthome.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类（Redis 不可用时自动降级为本地缓存）
 */
@Slf4j
@Component
public class RedisUtils {

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /** 本地缓存（Redis 不可用时使用） */
    private final Map<String, String> localCache = new ConcurrentHashMap<>();
    private final Map<String, Map<Object, Object>> localHashCache = new ConcurrentHashMap<>();
    private volatile boolean redisAvailable = true;

    /**
     * 检查 Redis 是否可用
     */
    private boolean isRedisAvailable() {
        if (redisTemplate == null) {
            return false;
        }
        if (!redisAvailable) {
            return false;
        }
        try {
            redisTemplate.hasKey("test:ping");
            return true;
        } catch (Exception e) {
            if (redisAvailable) {
                log.warn("Redis 不可用，降级为本地缓存模式: {}", e.getMessage());
                redisAvailable = false;
            }
            return false;
        }
    }

    public void set(String key, String value) {
        if (isRedisAvailable()) {
            try {
                redisTemplate.opsForValue().set(key, value);
                return;
            } catch (Exception e) {
                log.debug("Redis set 失败，使用本地缓存");
            }
        }
        localCache.put(key, value);
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        if (isRedisAvailable()) {
            try {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
                return;
            } catch (Exception e) {
                log.debug("Redis set 失败，使用本地缓存");
            }
        }
        localCache.put(key, value);
    }

    public String get(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.debug("Redis get 失败，使用本地缓存");
            }
        }
        return localCache.get(key);
    }

    public Boolean delete(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.delete(key);
            } catch (Exception e) {
                log.debug("Redis delete 失败，使用本地缓存");
            }
        }
        return localCache.remove(key) != null;
    }

    public Boolean hasKey(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.hasKey(key);
            } catch (Exception e) {
                log.debug("Redis hasKey 失败，使用本地缓存");
            }
        }
        return localCache.containsKey(key) || localHashCache.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public void hSet(String key, String field, String value) {
        if (isRedisAvailable()) {
            try {
                redisTemplate.opsForHash().put(key, field, value);
                return;
            } catch (Exception e) {
                log.debug("Redis hSet 失败，使用本地缓存");
            }
        }
        localHashCache.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(field, value);
    }

    public Object hGet(String key, String field) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForHash().get(key, field);
            } catch (Exception e) {
                log.debug("Redis hGet 失败，使用本地缓存");
            }
        }
        Map<Object, Object> hash = localHashCache.get(key);
        return hash != null ? hash.get(field) : null;
    }

    public Object hGetAll(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForHash().entries(key);
            } catch (Exception e) {
                log.debug("Redis hGetAll 失败，使用本地缓存");
            }
        }
        return localHashCache.getOrDefault(key, new ConcurrentHashMap<>());
    }

    public Long hDelete(String key, String field) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForHash().delete(key, field);
            } catch (Exception e) {
                log.debug("Redis hDelete 失败，使用本地缓存");
            }
        }
        Map<Object, Object> hash = localHashCache.get(key);
        if (hash != null) {
            return hash.remove(field) != null ? 1L : 0L;
        }
        return 0L;
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.expire(key, timeout, unit);
            } catch (Exception e) {
                log.debug("Redis expire 失败");
            }
        }
        return true;
    }
}
