package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.AccountUtil;
import com.abcft.apes.vitamin.util.MongoUtil;
import com.abcft.apes.vitamin.util.StringUtils;
import com.abcft.apes.vitamin.util.TokenUtil;
import org.bson.Document;

import javax.json.*;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class BaseController {

    @Context
    protected ServletContext context;

    @Context
    protected HttpServletResponse servletResponse;

    @Context
    protected HttpServletRequest servletRequest;

    public  String getCurrentUserEmail() {
        String userId = getCurrentUserId();
        return AccountUtil.getEmailById(userId);
    }

    public static String getLang(String lang) {
        if (!StringUtils.isEmpty(lang)) {
            lang = "_" + lang;
        }
        return lang;
    }

    public  String getCurrentUserId() {
        if (servletRequest == null || servletRequest.getCookies() == null) {
            return null;
        }
        for(Cookie cookie: servletRequest.getCookies()) {
            if (cookie.getName().equals("token")) {
                return TokenUtil.getName(cookie.getValue());
            }
        }

        return null;
    }

    public  JsonObject getCurrentUser() {
        String userId = getCurrentUserId();

        if (!StringUtils.isEmpty(userId)) {
            return AccountUtil.getUserById(userId);
        }

        return null;
    }

    public static JsonObject getResponse(boolean res) {
        return getResponse(res, 0);
    }

    public static JsonObject getResponse(boolean res, int status) {
        return getResponse(res, status, "");
    }

    public static JsonObject getResponse(boolean res, String msg) {
        return getResponse(res, 0, msg);
    }

    public static JsonObject getResponse(boolean res, int status, String msg) {
        return getResponse(res, status, msg, null);
    }

    public static JsonObject getResponse(boolean res, JsonStructure data) {
        return getResponse(res, "data", data);
    }

    public static JsonObject getResponse(boolean res, String key, JsonStructure data) {
        return getResponse(res, 0, "", key, data);
    }

    public static JsonObject getResponse(boolean res, int status, JsonStructure data) {
        return getResponse(res, status, "", data);
    }

    public static JsonObject getResponse(boolean res, int status, String msg, JsonStructure data) {
        return getResponse(res, status, msg, "data", data);
    }

    public static JsonObject getResponse(boolean res, Object data) {
        return getResponse(res, 0, "", "", data);
    }

    public static JsonObject getResponse(boolean res, int status, String msg, String key, Object data) {
        Document document = new Document();
        document.put("success", res);
        document.put("message", msg);
        document.put("status", status);
        if (StringUtils.isEmpty(key)) {
            document.put("data", data);
        } else {
            document.put(key, data);
        }

        return MongoUtil.document2Json(document);
    }

    public static JsonObject getResponse(boolean res, int status, String msg, String key, JsonStructure data) {
        JsonObjectBuilder respBuilder = Json.createObjectBuilder();

        respBuilder.add("success", res);
        respBuilder.add("message", msg);
        respBuilder.add("status", status);
        if (data != null) {
            respBuilder.add(key, data);
        }

        return respBuilder.build();
    }
    public static Document getResponseDoc(boolean res, Document data) {
        return getResponseDoc(res, 0, "", data);
    }

    public static Document getResponseDoc(boolean res, int status, String msg, Document data) {
        return getResponseDoc(res, status, msg, null, data);
    }

    public static Document getResponseDoc(boolean res, int status, String msg, String key, Document data) {
        Document respDoc = new Document();
        respDoc.put("success", res);
        respDoc.put("message", msg);
        respDoc.put("status", status);
        if (!StringUtils.isEmpty(key)) {
            respDoc.put(key, data);
        } else {
            for(String k: data.keySet()) {
                respDoc.put(k, data.get(k));
            }
        }

        return respDoc;
    }

    protected String getHost() {
        String host = servletRequest.getScheme() + "://" + servletRequest.getServerName();
        Integer port = servletRequest.getServerPort();
        if (port != 80 && port != 443) {
            host += ":" + port.toString();
        }
        return host;
    }

    public void validateLogin() throws Exception {
        String uid = getCurrentUserId();
        if (StringUtils.isEmpty(uid)) {
            throw new Exception("未授权的操作");
        }
    }
}
