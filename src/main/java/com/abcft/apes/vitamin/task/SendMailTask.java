package com.abcft.apes.vitamin.task;

import com.abcft.apes.vitamin.util.MailUtil;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhyzhu on 17-9-14.
 */
public class SendMailTask implements Runnable {

    private static Logger logger = Logger.getLogger(SendMailTask.class);
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    private String to;
    private String content;
    private String subject;

    public SendMailTask(String to, String content, String subject) {
        this.to = to;
        this.content = content;
        this.subject = subject;

    }
    @Override
    public void run() {
        boolean done = false;
        try {
            for( int try_count=10; try_count>0; try_count--) {
                done = MailUtil.SendSMTPMail(to, content, subject);
                if (done){
                    break;
                }
                Thread.sleep(1000*try_count);
            }
        } catch (Exception e) {
            logger.error("send email failed: " , e);
        }
        if (!done) {
            logger.warn("send email failed! to: " + to + " subject: " + subject);
        }
    }

    /**
     * 异步发送邮件
     */
    public static void runOnce(String to, String from, String message){
        SendMailTask task = new SendMailTask(to, from, message);
        executorService.execute(task);
    }

    /**
     * 异步发送邮件
     */
    public static void addTask(Runnable task){
        executorService.execute(task);
    }

    public static void main(String[] args) {
        addTask(new SendMailTask("zhyzhu@abcft.com", "xx", "xxxx"));
    }
}
