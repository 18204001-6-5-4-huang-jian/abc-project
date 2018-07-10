package com.abcft.apes.vitamin.util;

import com.mongodb.util.JSON;
import org.apache.log4j.Logger;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * Created by zhyzhu on 17-12-13.
 */
public class RedisUtil {
    private static final Logger logger = Logger.getLogger(RedisUtil.class);
    private static JedisPool jedisPool;
    //private static JedisPoolConfig config;
    private static String TAG;
    private static String host_name;
    private static String PASS;
    private static int port;
    private static int db_id;
    private static int timeout;

    public static void init(String hostName, int p, int databaseIndex, int t, String tag,String password) {
        TAG = tag;
        host_name = hostName;
        port = p;
        db_id = databaseIndex;
        timeout = t;
        TAG = tag;
        PASS = password;
        /*
        config = new JedisPoolConfig();
        config.setMaxTotal(128);
        config.setMaxWaitMillis(20000);
        config.setBlockWhenExhausted(false);
        config.setTestOnBorrow(true);
        try {
            jedisPool =  new JedisPool(config,hostName,port,timeout,null,databaseIndex);
        } catch (Exception e) {
            logger.error("Init msg redis factory error! ", e);
        }*/
    }

    public static JedisPool getPool() {
        if (jedisPool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(128);
            config.setMaxWaitMillis(20000);
            config.setBlockWhenExhausted(false);
            config.setTestOnBorrow(true);
            try {
                jedisPool = new JedisPool(config, host_name, port, timeout, PASS, db_id);
            } catch (Exception e) {
                logger.error("Init msg redis factory error! ", e);
            }
        }
        return jedisPool;
    }

    public static Jedis getRedis() {
        JedisPool pool = getPool();
        try {
            return pool.getResource();
        } catch (Exception e) {
            logger.error("get redis error! ", e);
        }
        return null;
    }

    public static String getRedisKey(String in) {
        return String.format("%s:%s", TAG, in);
    }

    public static String getString(String key) {

        try (Jedis jedis = getRedis()) {
            String result = jedis.get(key);
            if (result == null) {
//                logger.error("cache miss : " + key);
                return null;
            }
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static JsonObject getJsonObject(String key) {
        try {
            String valString = getString(key);
            if (valString == null) {
                return null;
            }
            return StringUtils.parseJson(valString);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public static Document getDocument(String key) {
        try {
            String valStr = getString(key);
            if (valStr == null) {
                return null;
            }
            return Document.parse(valStr);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static void set(@NotNull String key, @NotNull Object value, @NotNull TimeUnit timeUnit, @NotNull int expire) {
        try (Jedis jedis = getRedis()) {
            String valString;
            if (value instanceof JsonObject) {
                valString = JSON.serialize(value);
            } else if (value instanceof Document) {
                valString = ((Document) value).toJson();
            } else {
                valString = String.valueOf(value);
            }
            if (valString != null) {
                jedis.set(key, valString);
                //开发环境缓存1分钟
                if (StringUtils.isDev(StringUtils.getHostUrl())) {
                    timeUnit = TimeUnit.MINUTES;
                    expire = 1;
                }
                //最多不超过2小时
                long time = timeUnit.toSeconds(expire);
                time = time % (TimeUnit.HOURS.toSeconds(2));
                jedis.expire(key, Math.toIntExact(time));
            }
        }
    }

    /**
     * 获取满足指定pattern的key
     *
     * @param pattern
     * @return
     */
    public static Set<String> keys(String... pattern) {
        for (int i = 0, len = pattern.length; i < len; i++) {
            if (!pattern[i].startsWith(TAG)) {
                pattern[i] = getRedisKey(pattern[i]);
            }
        }
        Set<String> keys = new HashSet<>();
        try (Jedis jedis = getRedis()) {
            for (String p : pattern) {
                Set<String> keys1 = jedis.keys(p);
//                logger.info(Arrays.toString(keys1.toArray()));
                keys.addAll(keys1);
            }
//            logger.info("keys: " + Arrays.toString(keys.toArray()));
            return keys;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * 删除满足指定pattern的key
     *
     * @param pattern
     */
    public static void del(String... pattern) {
//        logger.info(Arrays.toString(pattern));
        for (int i = 0, len = pattern.length; i < len; i++) {
            if (!pattern[i].startsWith(TAG)) {
                pattern[i] = getRedisKey(pattern[i]);
            }
        }
//        logger.info(Arrays.toString(pattern));
        Set<String> set = keys(pattern);
        if (set.size() == 0) {
            logger.error("pattern error, can't be empty");
            return;
        }
        String[] keys = set.toArray(new String[set.size()]);
//        logger.info(Arrays.toString(keys));
        try (Jedis jedis = getRedis()) {
            jedis.del(keys);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
