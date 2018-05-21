package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.*;
import com.mongodb.client.model.Filters;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhyzhu on 17-4-23.
 */
@Path("/test")
public class TestController extends BaseController {

    private static Logger logger = Logger.getLogger(TestController.class);

    @GET
    @Path("hello")
    @Produces(MediaType.APPLICATION_JSON)
    public String Hello(
    ) {
        return "hello!";
    }

    /**
     * 测试邮件发送功能
     *
     * @return
     */
    @Path("email/{type: [\\d\\w]+}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject testEmail(
            @PathParam("type") String type,
            @DefaultValue("") @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("lang") String lang

    ) {
        String cuid = getCurrentUserId();
        if (!AccountUtil.isSuperAdmin(cuid)) {
            return getResponse(false, 1, "no operation authority");
        }
        boolean res = false;
        if (email.isEmpty()) {
            email = getCurrentUserEmail();
        }
        JsonObject userJson = AccountUtil.getUserByEmail(email);
        if (userJson == null) {
            return getResponse(false, 2, "user<" + email + "> not exists");
        }
        logger.info("user<" + email + "> " + userJson);
        String username = userJson.getString("username");
        String userId = userJson.containsKey("id") ?
                userJson.get("id").toString() :
                userJson.containsKey("_id") ?
                        userJson.get("_id").toString() :
                        "";
        if (userId.isEmpty()) {
            return getResponse(false, 2, "user<" + email + "> not exists");
        }
        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("user_id", userId));
        String orderId;
        switch (type.toLowerCase()) {
            case "created":
                conds.add(Filters.eq("status", OrderUtil.OrderStatus2.Unconfirmed.ordinal()));
                orderId = MongoUtil.getDBList(MongoUtil.ORDER_COL, conds,
                        "create_at", "desc", 0, 1)
                        .get(0)
                        .getString("id");
                res = MailUtil.SendOrderCreatedEmail(email, username, orderId, lang);
                break;
            case "done":
                conds.add(Filters.eq("status", OrderUtil.OrderStatus2.Succeed.ordinal()));
                orderId = MongoUtil.getDBList(MongoUtil.ORDER_COL, conds,
                        "create_at", "desc", 0, 1)
                        .get(0)
                        .getString("id");
                res = MailUtil.SendOrderDoneEmail(email, username, orderId, lang);
                break;
            case "failed":
                conds.add(Filters.eq("status", OrderUtil.OrderStatus2.Failed.ordinal()));
                orderId = MongoUtil.getDBList(MongoUtil.ORDER_COL, conds,
                        "create_at", "desc", 0, 1)
                        .get(0)
                        .getString("id");
                res = MailUtil.SendOrderFailedEmail(email, username, orderId, lang);
                break;
            case "identity":
                res = MailUtil.SendVerifyIdentityEmail(email, lang);
                break;
            case "work":
                res = MailUtil.SendVerifyWorkEmailEmail(email, lang);
                break;
        }
        return getResponse(res);

    }

    @Path("configs")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getConfigs(
            @DefaultValue("") @QueryParam("name") String name
    ) {
        try {
            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 2, "no operation authority");
            }
            List<Document> list = new ArrayList<>();
            List<Bson> conds = new ArrayList<>();
            if (!StringUtils.isEmpty(name)) {
                conds.add(Filters.eq("name", name));
            }
            int total = MongoUtil.getDBCount(MongoUtil.CONFIG_COL, conds);
            list = MongoUtil.getDBList(MongoUtil.CONFIG_COL, conds);
            Document res = new Document("total", total)
                    .append("list", list);
            return getResponse(true, res);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return getResponse(false, 1, "some exceptions: " + e.getMessage());
        }
    }

    @Path("config/{name: [\\w\\d]+}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setConfig(
            @NotNull @PathParam("name") String name,
            @DefaultValue("") @QueryParam("key") String key,
            @DefaultValue("") @QueryParam("value") String value,
            @DefaultValue("") @QueryParam("type") String type,
            @DefaultValue("") @QueryParam("insert") String insert
    ) {
        try {
            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 2, "no operation authority");
            }
            String charsetName = System.getProperty("file.encoding");
            key = URLDecoder.decode(key, charsetName);
            value = URLDecoder.decode(value, charsetName);
            Document configDoc = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", name);
            if (configDoc == null) {
                return getResponse(false, 3, "cannot find config named: [" + name + "]");
            }
            if (!configDoc.containsKey(key) && !insert.equalsIgnoreCase("yes")) {
                return getResponse(false, 4, "cannot find config item: <" + key + ">");
            }
            if (value.isEmpty()) {
                return getResponse(false, 5, "value cannot be empty");
            }
            type = type.toLowerCase();
            Object val = null;
            switch (type) {
                case "int":
                    val = Integer.parseInt(value);
                    break;
                case "bool":
                    val = Boolean.parseBoolean(value);
                    break;
                case "double":
                    val = Double.valueOf(value);
                    break;
                case "array":
                    val = StringUtils.CSV2List(value, ",");
                    break;
                case "date":
                    val = TimeUtil.strToDate(value, "yyyy-M-d");
                    break;
                case "oid":
                    val = new ObjectId(value);
                    break;
                default:
                    val = value;
                    break;
            }
            Document updateDoc = new Document(key, val);
            Document res = MongoUtil.updateOneByField(MongoUtil.CONFIG_COL, "name", name, updateDoc);
            return res == null
                    ? getResponse(false, 6, "update config[" + name + "] failed")
                    : getResponse(true, updateDoc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "some exceptions: " + e.getMessage());
        }
    }

}
