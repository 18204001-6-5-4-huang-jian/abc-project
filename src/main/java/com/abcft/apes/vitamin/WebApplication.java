package com.abcft.apes.vitamin;

import com.abcft.apes.vitamin.common.CommonUtil;
import com.abcft.apes.vitamin.passport.util.WechatUtil;
import com.abcft.apes.vitamin.task.CheckOrderTask;
import com.abcft.apes.vitamin.task.SendOrderFailedEmailTask;
import com.abcft.apes.vitamin.util.*;
import com.abcft.apes.vitamin.filters.CORSFilter;
import com.abcft.apes.vitamin.filters.JWTSecurityFilter;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class WebApplication extends ResourceConfig {
    static {
        PropertyConfigurator.configure(WebApplication.class.getClassLoader().getResourceAsStream("log4j.properties"));
    }

    private static Logger logger = Logger.getLogger(WebApplication.class);
    private static Properties sConfig = null;

    public WebApplication() {
        packages(getClass().getPackage().getName() + ".controllers");
        register(CORSFilter.class);
        register(JWTSecurityFilter.class);
        register(MultiPartFeature.class);
        register(RolesAllowedDynamicFeature.class);
        register(JsonProcessingFeature.class);

        try {
            Properties config = new Properties();
            config.load(WebApplication.class.getClassLoader().getResourceAsStream("/META-INF/config.properties"));
            sConfig = config;
        } catch (IOException e) {
            logger.error("load config.properties failed");
        }

        CORSFilter.init(sConfig.getProperty("cors.static_host"));
        MongoUtil.init(sConfig.getProperty("web.mongodb"), sConfig.getProperty("web.mongodb.db"));
        MailUtil.init(sConfig.getProperty("mail.host"), sConfig.getProperty("mail.from"), sConfig.getProperty("mail.env"),sConfig.getProperty("mail.user"), sConfig.getProperty("mail.password"));
        StringUtils.init(sConfig.getProperty("string.host_url"));
        RedisUtil.init(sConfig.getProperty("redis.hostName"),
                Integer.parseInt(sConfig.getProperty("redis.port")),
                Integer.parseInt(sConfig.getProperty("redis.databaseIndex")),
                Integer.parseInt(sConfig.getProperty("redis.timeout")),
                sConfig.getProperty("redis.tag"));

        WechatUtil.APPID = sConfig.getProperty("wechat.appid");
        WechatUtil.SECRET = sConfig.getProperty("wechat.appsecret");
        ChartUtil.CHART_API_URL = sConfig.getProperty("chart.api.url");

//        TaskUtil.addTasks(
//                new CheckOrderTask(),
//                new SendOrderFailedEmailTask()
//        );

        logger.info("vitamin start ...");
    }
}
