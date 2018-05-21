package com.abcft.apes.vitamin.websocket;

import org.apache.log4j.Logger;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/wyzs/wechat/socket/{opt_data}")
public class WyzsWechatSocket {
    private static final Logger logger = Logger.getLogger(WyzsWechatSocket.class);

    //<浏览器唯一标识, Session>
    public static Map<String, Session> clients = new ConcurrentHashMap<>();

    public WyzsWechatSocket(){
    }

    @OnOpen
    public void onOpen(@PathParam("opt_data") String opt_data, Session session, EndpointConfig config){
        logger.info("建立websocket连接,收到前台发送的消息:" + opt_data);
        clients.put(opt_data, session);
    }

    @OnClose
    public void onClose(@PathParam("opt_data") String opt_data){
        logger.info("断开websocket连接,收到前台发送的消息:" + opt_data);
        clients.remove(opt_data);
    }

    @OnMessage
    public String onMessage(@PathParam("opt_data") String opt_data,String message, Session session) throws IOException, EncodeException, InterruptedException {
        logger.info("websocket消息推送，前台发送的消息:" + message);
        return "Got your message:" + message + ", Thanks!";
    }

    @OnError
    public void onError(@PathParam("opt_data") String opt_data, Throwable throwable){
        logger.error("websocket主键:" + opt_data + ",连接出现异常:" + throwable.getMessage());
        clients.remove(opt_data);
    }

    public static void broardcast(@PathParam("opt_data") String opt_data, String message){
        if (clients.containsKey(opt_data)) {
            clients.get(opt_data).getAsyncRemote().sendText(message);
        } else {
            logger.error("[" + opt_data +"]Connection does not exist");
        }
    }
}
