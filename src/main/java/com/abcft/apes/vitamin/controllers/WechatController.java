package com.abcft.apes.vitamin.controllers;

/**
 * Created by zhyzhu on 17-9-4.
 */

import com.abcft.apes.vitamin.passport.util.WechatUtil;
import com.abcft.apes.vitamin.passport.wechat.model.AccessToken;
import com.abcft.apes.vitamin.passport.wechat.model.UserInfo;
import com.abcft.apes.vitamin.util.AccountUtil;
import com.abcft.apes.vitamin.util.MongoUtil;
import com.abcft.apes.vitamin.util.StringUtils;
import com.abcft.apes.vitamin.util.TokenUtil;
import com.mongodb.client.model.Filters;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.abcft.apes.vitamin.controllers.AccountController.getLoginResponse;

@Path("/wechat")
public class WechatController extends BaseController {
    public static final int UNBINDING = 0;
    public static final int BINDING = 1;
    public static final int UNAUTHENTICATED = 0;
    public static final int AUTHENICATED = 1;
    private static Logger logger = Logger.getLogger("passport");

    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject login(@FormParam("code") String code) {
        try {
            logger.info("wechat login with code: " + code);
            return wechatLoginByCode(code);
        } catch (Exception e) {
            logger.info("wechat login error: ", e);
            return getResponse(false, 1, "微信登录失败" + StringUtils.showError(e));
        }

    }

    /**
     * 微信登录
     *
     * @param code
     * @return
     */
    private JsonObject wechatLoginByCode(String code) {
        AccessToken accessToken = WechatUtil.getAccessToken(code);
        String openid = accessToken.getOpenid();

        Document resp = new Document();

        Document accountDoc = AccountUtil.getUserByOpenId(openid);
        if (accountDoc == null || accountDoc.isEmpty()) {
            // 初次登录

            // fetch wx user info
            UserInfo userInfo = WechatUtil.getUserInfo(accessToken.getAccess_token(), openid);

            // save wx user info
            Document userDoc = new Document();
            userDoc.put("username", userInfo.getNickname());
            userDoc.put("nickname", userInfo.getNickname());
            userDoc.put("role", "user");
            userDoc.put("head_img", userInfo.getHeadimgurl());
            userDoc.put("sex", userInfo.getSex());
            userDoc.put("province", userInfo.getProvince());
            userDoc.put("city", userInfo.getCity());
            userDoc.put("country", userInfo.getCountry());
            userDoc.put("openid", userInfo.getOpenid());
            userDoc.put("update_time", new Date());
            List<Bson> conditions = Arrays.asList(
                    Filters.eq("openid", openid)
            );
            MongoUtil.upsertOne(MongoUtil.WX_USER_COL, conditions, userDoc);

            resp.put("status", UNBINDING);      //未绑定email
            resp.put("openid", openid);
        } else {
            boolean isBinding = accountDoc.containsKey("authentication");
            if (!isBinding) {
                resp.put("status", UNBINDING);      //未绑定email
                resp.put("openid", openid);
            } else {
                // 已绑定邮箱, 直接登录
                // fetch wx user info
                UserInfo userInfo = WechatUtil.getUserInfo(accessToken.getAccess_token(), openid);

                // update wexiuser info
                Document updateDoc = new Document()
                        .append("head_img", userInfo.getHeadimgurl());

                List<Bson> cond1 = Arrays.asList(
                        Filters.eq("email", accountDoc.getString("email"))
                );
                MongoUtil.updateOne(MongoUtil.ACCOUNT_COL, cond1, updateDoc);

                String userId = accountDoc.getObjectId("_id").toString();
                return getLoginResponse(userId);
            }
        }

        return getResponse(true, resp);
    }

    /**
     * 微信绑定email
     * <p>
     * 微信扫描登录, 未绑定email时, 调用该api, 绑定email
     *
     * @param openid
     * @param email
     * @param name
     * @param company
     * @param password
     * @return
     */
    @POST
    @Path("email/bind")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject bindingEmail(
            @FormParam("openid") String openid,
            @FormParam("email") String email,
            @FormParam("name") String name,
            @FormParam("company") String company,
            @FormParam("password") String password,
            @FormParam("pwd_ends") String pwdEnds,
            @DefaultValue("") @FormParam("invite_code") String inviteCode
    ) {
        logger.info("wechat bind email: " + email);
        Document accountDoc = AccountUtil.getUserDocByEmail(email);
        try {
            if (accountDoc != null && !accountDoc.isEmpty()) {
                int auth = (accountDoc.get("authentication") == null)
                        ? UNAUTHENTICATED : accountDoc.getInteger("authentication");
                if (auth == AUTHENICATED) {
                    return getResponse(false, 2, "邮件已绑定微信");
                }
            }

            return bindEmail(openid, email, name, company, password, pwdEnds, inviteCode);
        } catch (Exception e) {
            logger.info("bind email error", e);
            return getResponse(false, 1, "微信绑定email失败" + StringUtils.showError(e));
        }

    }

    /**
     * 绑定email
     *
     * @return
     */
    private JsonObject bindEmail(
            String openid, String email, String name, String company,
            String password, String pwdEnds, String inviteCode
    ) {

        try {
            if (StringUtils.isEmpty(inviteCode)) {
                inviteCode = "";
            }
            // save bind email info
            Document wxUserInfo = new Document("email", email)
                    .append("company", company)
                    .append("username", name)
                    .append("password", password)
                    .append("pwd_ends", pwdEnds)
                    .append("authentication", UNAUTHENTICATED)
                    .append("invite_code", inviteCode);
            List<Bson> cond1 = Arrays.asList(
                    Filters.eq("openid", openid)
            );
            MongoUtil.updateOne(MongoUtil.WX_USER_COL, cond1, wxUserInfo);

            // update account status
            Document userInfo = new Document()
                    .append("email", email)
                    .append("username", name)
                    .append("openid", openid)
                    .append("authentication", UNAUTHENTICATED)
                    .append("role", "user")
                    .append("invite_code", inviteCode);

            List<Bson> conditions = Arrays.asList(
                    Filters.or(
                            Filters.eq("email", email),
                            Filters.eq("openid", openid)
                    )
            );
            MongoUtil.upsertOne(MongoUtil.ACCOUNT_COL, conditions, userInfo);

            Document userDoc = MongoUtil.getOneByField(MongoUtil.WX_USER_COL, "openid", openid);
            userDoc.remove("_id");
            userDoc.put("authentication", UNAUTHENTICATED);
            userDoc.put("status", AccountUtil.UserStatus.Registered.ordinal());
            MongoUtil.updateOneByField(MongoUtil.ACCOUNT_COL, "openid", openid, userDoc);
            Document accountDoc = MongoUtil.getOneByConditions(MongoUtil.ACCOUNT_COL, conditions);
            return getLoginResponse(accountDoc.getObjectId("_id").toString());

        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(false, 3, "绑定邮箱失败" + StringUtils.showError(e));
        }
    }

    @Path("email/verify")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject loginByToken(
            @NotNull @FormParam("id") String id,
            @NotNull @FormParam("token") String token
    ) {
        JsonObject userJson = AccountUtil.getUserById(id);
        if (userJson == null) {
            logger.warn("user not exist: " + id);
            return getResponse(false, 1, "用户不存在");
        }

        if (!TokenUtil.isValid(token)) {
            logger.warn("token is invalid");
            return getResponse(false, 2, "认证已过期, 请重新确认");
        }

        // verify email
        String openId = userJson.containsKey("openid") ? userJson.get("openid").toString() : "";
        Document userDoc = MongoUtil.getOneByField(MongoUtil.WX_USER_COL, "openid", openId);
        if (userDoc == null) {
            userDoc = MongoUtil.getOneById(MongoUtil.ACCOUNT_COL, id);
            userDoc.remove("_id");
            userDoc.put("status", AccountUtil.UserStatus.Registered.ordinal());
        } else {

            userDoc.put("authentication", AUTHENICATED);
            userDoc.remove("_id");
            userDoc.put("status", AccountUtil.UserStatus.Registered.ordinal());
        }
        MongoUtil.updateOneById(MongoUtil.ACCOUNT_COL, id, userDoc);

        AccountUtil.dropUserToken(id, token);

        return getLoginResponse(id);
    }
}

