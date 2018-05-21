package com.abcft.apes.vitamin.util;

import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.*;

public class TaskUtil {
    private static Logger logger = Logger.getLogger(TaskUtil.class);
    private static ExecutorService service =
            Executors.newCachedThreadPool(new HandlerThreadFactory());
    private static Map<String, Object> autoFreshConfig = new HashMap<>();

    public static Object getConfig(String configName, String classSimpleNameLowerCase) {
        return autoFreshConfig.get(configName + "_" + classSimpleNameLowerCase);
    }

    /**
     * 添加并执行任务
     *
     * @param tasks
     */
    public static void addTasks(Runnable... tasks) {
        service.execute(new ConfigAutoRefresher(tasks));
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Runnable task : tasks) {
            service.execute(task);
        }
    }

    private static class ConfigAutoRefresher implements Runnable {

        private List<String> taskNameList = new ArrayList<>();

        ConfigAutoRefresher(Runnable[] tasks) {
            for (Runnable task : tasks) {
                taskNameList.add(task.getClass().getSimpleName().toLowerCase());
            }
        }

        @Override
        public void run() {
            refreshConfig();
        }

        private void refreshConfig() {
            while (Thread.currentThread().isAlive()) {

                if (taskNameList.isEmpty()) return;
                taskNameList.forEach(s -> {
                    Document ci = MongoUtil.getOneByField(
                            MongoUtil.CONFIG_COL, "name", s);
                    if (ci == null) {
                        return;
                    }
                    ci.computeIfPresent("time_unit", (k, v) -> v = String.valueOf(v).toUpperCase());
                    ci.forEach((k, v) -> autoFreshConfig.put(k + "_" + s, v));
                });
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//            logger.info(Arrays.toString(autoFreshConfig.entrySet().toArray()));
            }
        }
    }

    private static class HandlerThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
            return t;
        }

        private class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
