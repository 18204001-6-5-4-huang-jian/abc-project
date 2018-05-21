package com.abcft.apes.vitamin.task;

import com.abcft.apes.vitamin.util.*;
import com.mongodb.client.model.Filters;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SendOrderFailedEmailTask implements Runnable {
    private static Logger logger = Logger.getLogger(SendOrderFailedEmailTask.class);

    private static boolean TOGGLE;
    private static boolean FIRST_TIME;
    private static TimeUnit TIME_UNIT;
    private static int INITIAL_DELAY;
    private static int DELAY;
    private static int SENDING_COUNT;
    private static int SENDING_LIMIT;
    private static Date TODAY_DATE;
    private static List<Document> ORDER_CACHE = new ArrayList<>();

    private static final String CHECKED_BY_EMAIL_SENDER = "checked_by_email_sender";
    private static final String CHECKED_LAST = "checked_last";
    private static final String EMAIL_SENDING_COUNT = "email_sending_count";
    private static final String TODAY = "today_date";
    private static final String FIELD_NAME =
            SendOrderFailedEmailTask.class.getSimpleName().toLowerCase();
    private final String _TOGGLE = "toggle";
    private final String _FIRST_TIME = "first_time";
    private final String _TIME_UNIT = "time_unit";
    private final String _INITIAL_DELAY = "initial_delay";
    private final String _DELAY = "delay";
    private final String _SENDING_LIMIT = "email_sending_limit";
    private final String _TODAY_DATE = "today_date";

    @Override
    public void run() {
        firstLoadConfig();          //初次加载配置

        while (true) {
            ensureFirstTime();      //首次运行 延迟INITIAL_DELAY时间

            reloadConfig();         //重载配置

            ensureTaskOpen();       //确认是否开启任务

            sendOrderFailedEmail(); //发送订单失败邮件

            sleep(DELAY);           //每次执行任务 间隔DELAY时间
        }
    }

    private void sendOrderFailedEmail() {
        updateOrderCache();                 //更新 待发送邮件的 失败订单 缓存
        ensureTheLimitOfSendingEmail();     //确认邮件是否达到预设发送上限 SENDING_LIMIT
        Document orderInfo = getOrderInfo();//获取失败订单信息
        sendEmail(orderInfo);               //发送订单失败邮件
        markCheckedOrder(orderInfo);        //标记已检查过的失败订单 避免重复
    }

    private void ensureFirstTime() {
        if (isFirstTime())
            sleep(INITIAL_DELAY);
    }

    private void ensureTaskOpen() {
        if (!TOGGLE) {
            logger.error("send order failed email task is closed");
            throw new RuntimeException("send order failed email task is closed");
        }
    }

    private boolean firstLoadConfig() {
        logger.info("first time loading config");
        TOGGLE = (boolean) getConfig(_TOGGLE);
        TIME_UNIT = TimeUnit.valueOf((String) getConfig(_TIME_UNIT));
        DELAY = Math.toIntExact((long) getConfig(_DELAY));
        SENDING_COUNT = (int) getConfig(EMAIL_SENDING_COUNT);
        SENDING_LIMIT = (int) getConfig(_SENDING_LIMIT);
        TODAY_DATE = (Date) getConfig(_TODAY_DATE);

        INITIAL_DELAY = Math.toIntExact((long) getConfig(_INITIAL_DELAY));
        FIRST_TIME = (boolean) getConfig(_FIRST_TIME);

        logger.info(String.format("TODAY_DATE: %tc, SENDING_COUNT: %d, " +
                        "SENDING_LIMIT: %d, TOGGLE: %s, TIME_UNIT: %s, " +
                        "DELAY: %d, INITIAL_DELAY: %d, FIRST_TIME: %s",
                TODAY_DATE, SENDING_COUNT, SENDING_LIMIT,
                (TOGGLE ? "true" : "false"), TIME_UNIT.name(),
                DELAY, INITIAL_DELAY, (FIRST_TIME ? "true" : "false")
        ));
        return true;
    }

    private boolean reloadConfig() {
        TOGGLE = (boolean) getConfig(_TOGGLE);
        TIME_UNIT = TimeUnit.valueOf((String) getConfig(_TIME_UNIT));
        DELAY = Math.toIntExact((long) getConfig(_DELAY));
        SENDING_COUNT = (int) getConfig(EMAIL_SENDING_COUNT);
        SENDING_LIMIT = (int) getConfig(_SENDING_LIMIT);
        TODAY_DATE = (Date) getConfig(_TODAY_DATE);

        logger.info(String.format("TODAY_DATE: %tc, SENDING_COUNT: %d, " +
                        "SENDING_LIMIT: %d, TOGGLE: %s, TIME_UNIT: %s, " +
                        "DELAY: %d, INITIAL_DELAY: %d, FIRST_TIME: %s",
                TODAY_DATE, SENDING_COUNT, SENDING_LIMIT,
                (TOGGLE ? "true" : "false"), TIME_UNIT.name(),
                DELAY, INITIAL_DELAY, (FIRST_TIME ? "true" : "false")));
        return true;
    }

    private boolean isFirstTime() {
        if (!FIRST_TIME) return false;

        FIRST_TIME = false;
        Document updateDoc = new Document(_FIRST_TIME, false);
        if (MongoUtil.updateOneByField(
                MongoUtil.CONFIG_COL,
                "name",
                FIELD_NAME,
                updateDoc) == null) {
            throw new RuntimeException("update first_time failed");
        }
        logger.info("update first_time succeed");
        return true;
    }

    private void sleep(int delay) {
        try {
            TIME_UNIT.sleep(delay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Object getConfig(String configName) {
        return TaskUtil.getConfig(configName, FIELD_NAME);
    }

    private boolean markCheckedOrder(Document orderInfo) {
        Document updateDoc = new Document(CHECKED_BY_EMAIL_SENDER, true);
        if (MongoUtil.updateOneById(
                MongoUtil.ORDER_COL,
                orderInfo.getString("order_id"),
                updateDoc) == null) {
            throw new RuntimeException("update order with checked_by_email_sender error");
        }
        logger.info("update order with checked_by_email_sender succeed");
        return true;
    }

    private boolean sendEmail(Document orderInfo) {
        increaseSendingCount();
        if (!MailUtil.SendOrderFailedEmail(
                orderInfo.getString("email"),
                orderInfo.getString("name"),
                orderInfo.getString("order_id"),
                orderInfo.getString("lang"))) {
            throw new RuntimeException("send order failed email error");
        }
        logger.info("send order failed email succeed");
        return true;
    }

    private Document getOrderInfo() {
        Document orderDoc = ORDER_CACHE.remove(0);
        if (orderDoc == null) return null;
        String email = orderDoc.getString("user_email");
        String name = AccountUtil.getUsernameByEmail(email);
        String orderId = orderDoc.getString("id");
        String lang = "";
        return new Document("email", email)
                .append("name", name)
                .append("order_id", orderId)
                .append("lang", lang);
    }

    private boolean updateOrderCache() {
        if (!ORDER_CACHE.isEmpty()) return true;
        logger.info("ORDER_CACHE is empty");

        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq(CHECKED_LAST, true));
        conds.add(Filters.exists(CHECKED_BY_EMAIL_SENDER, false));
        List<Document> orderList = MongoUtil.getDBList(MongoUtil.ORDER_COL, conds);

        logger.info("update Order cache");
        ORDER_CACHE.addAll(orderList);

        if (ORDER_CACHE.isEmpty()) throw new RuntimeException("ORDER_CACHE is empty");
        logger.info("ORDER_CACHE size : " + ORDER_CACHE.size());
        return true;
    }

    private boolean increaseSendingCount() {
        SENDING_COUNT++;
        logger.info("SENDING_COUNT: " + SENDING_COUNT);
        //更新发送邮件计数
        Document updateDoc = new Document(EMAIL_SENDING_COUNT, SENDING_COUNT);
        if (MongoUtil.updateOneByField(
                MongoUtil.CONFIG_COL,
                "name",
                FIELD_NAME,
                updateDoc) == null) {
            throw new RuntimeException("update sending count failed");
        }
        logger.info("update sending count success");
        return true;
    }

    private boolean resetEmailSendingCount() {
        SENDING_COUNT = 0;
        Document updateDoc = new Document(EMAIL_SENDING_COUNT, 0);
        if (MongoUtil.updateOneByField(
                MongoUtil.CONFIG_COL,
                "name",
                FIELD_NAME,
                updateDoc) == null) {
            throw new RuntimeException("update order with " + SENDING_COUNT + " error");
        }
        logger.info("update order with " + SENDING_COUNT + " error");
        return true;
    }

    private boolean havePassedADay() {
        boolean res = TimeUtil.getDaysCount(TODAY_DATE, new Date()) >= 1;
        if (!res) return false;
        logger.info("have passed a day");
        TODAY_DATE = new Date();
        Document updateDoc = new Document(TODAY, TODAY_DATE);
        if (MongoUtil.updateOneByField(
                MongoUtil.CONFIG_COL,
                "name",
                FIELD_NAME,
                updateDoc) == null) {
            throw new RuntimeException("update config with TODAY_DATE failed");
        }
        logger.info("update config with TODAY_DATE succeed");
        return true;
    }

    private boolean ensureTheLimitOfSendingEmail() {
        boolean res = SENDING_COUNT >= SENDING_LIMIT;
        logger.info("have " + (res ? "" : "not") + " reached the limit of sending email");
        if (!res) return true;
        boolean res1 = false;
        if (havePassedADay()) res1 = resetEmailSendingCount();
        return res1;
    }
}
