package com.abcft.apes.vitamin.task;


import com.abcft.apes.vitamin.util.MongoUtil;
import com.abcft.apes.vitamin.util.OrderUtil;
import com.abcft.apes.vitamin.util.TaskUtil;
import com.abcft.apes.vitamin.util.TimeUtil;
import com.mongodb.client.model.Filters;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CheckOrderTask implements Runnable {
    private static Logger logger = Logger.getLogger(CheckOrderTask.class);
    private static final String EXPIRE = "expire";
    private static final long TIME_OUT = 5;
    private static final String TIME_UNIT = "time_unit";
    private static final String INITIAL_DELAY = "initial_delay";
    private static final String TOGGLE = "toggle";
    private static final String DELAY = "delay";
    private static final String CHECKED_LAST = "checked_last";
    private static final String FILED_NAME = CheckOrderTask.class.getSimpleName().toLowerCase();
    private static int EXPIRE_INT;

    @Override
    public void run() {
        try {
            TimeUnit timeUnit = TimeUnit.valueOf((String) getConfig(TIME_UNIT));
            timeUnit.sleep((Long) getConfig(INITIAL_DELAY));
            while (true) {
                try {
                    EXPIRE_INT = (int) getConfig(EXPIRE);
                    if ((boolean) getConfig(TOGGLE)) {
                        checkFailedOrder();
                    }
                    timeUnit.sleep(
                            (Long) getConfig(DELAY));
                } catch (Exception e) {
                    logger.error(e);
                    TimeUnit.SECONDS.sleep(TIME_OUT);
                }
            }
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    private Object getConfig(String configName) {
        return TaskUtil.getConfig(configName, FILED_NAME);
    }

    /**
     * 检查失败订单
     */
    private static void checkFailedOrder() {
        long s = System.currentTimeMillis();
        Date today = new Date();

        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.exists(CHECKED_LAST, false));
        conds.add(Filters.ne("status", OrderUtil.OrderStatus2.Succeed.ordinal()));
        conds.add(Filters.ne("status", OrderUtil.OrderStatus2.Failed.ordinal()));
        conds.add(Filters.lt("update_at", getExpireDate(today)));

        List<Document> orderList = MongoUtil.getDBList(MongoUtil.ORDER_COL, conds);
//        logger.info("ordersList size: " + orderList.size());
        List<ObjectId> oids = orderList
                .stream()
                .filter(document -> {
                    Date updateTime = document.getDate("update_at");
                    int workdays;
                    try {
                        workdays = TimeUtil.getWorkdays(updateTime, today);
//                        logger.info(String.format("%tF, %tF", updateTime, today));
//                        logger.info("workdays: " + workdays);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        workdays = -1;
                    }
//                    logger.info(workdays >= EXPIRE_INT ? "workdays >= EXPIRE" : "workdays < EXPIRE");
                    return workdays >= EXPIRE_INT;
                })
                .map(document -> new ObjectId(document.getString("id")))
                .collect(Collectors.toList());

//        logger.info("oids size: " + oids.size());

        if (!oids.isEmpty()) {

            Document updateDoc = new Document(CHECKED_LAST, true)
                    .append("status", OrderUtil.OrderStatus2.Failed.ordinal());
            List<Bson> conds1 = new ArrayList<>();
            conds1.add(Filters.in("_id", oids));
            boolean res = MongoUtil.updateMany(MongoUtil.ORDER_COL, conds1, updateDoc);
            if (!res) {
                logger.error("update orders with oids failed");
            }
            logger.info("update orders success");
        } else {
            logger.info("no orders was checked");
        }
        long e = System.currentTimeMillis();
//        logger.info("time: " + (e - s) / 1000.0);
    }

    private static Date getExpireDate(Date today) {
        Date date = TimeUtil
                .getRelateDate(today, Calendar.DAY_OF_YEAR, -EXPIRE_INT);
//        logger.info(String.format("date is %tc", date));
        return date;
    }
}
