package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.annotation.security.PermitAll;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.Cookie;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhyzhu on 17-4-23.
 */
@PermitAll()
@Path("/")
public class AccountController extends BaseController {

    private static Logger logger = Logger.getLogger(AccountController.class);

    public static JsonObject getLoginResponse(String userId) {
        return getLoginResponse(userId, true);
    }

    public static JsonObject getLoginResponse(String userId, boolean genToken) {
        try {

            Document userDoc = MongoUtil.getOneById(MongoUtil.ACCOUNT_COL, userId);
            JsonObject userJson = MongoUtil.document2Json(userDoc);

            JsonObject token = null;
            if (genToken) {
                token = TokenUtil.generateToken(userJson);
                if (token == null) {
                    logger.error("gen token failed: " + userJson.getString("email"));
                    return getResponse(false, 2, "登录错误, 请联系客服");
                }
            }
            JsonObjectBuilder userBuilder = Json.createObjectBuilder()
                    .add("id", userId)
                    .add("username", userJson.getString("username"))
                    .add("email", userJson.getString("email"))
                    .add("role", userJson.getString("role"));
            if (userJson.containsKey("right")) {
                userBuilder.add("right", userJson.get("right"));
            }
            if (userJson.containsKey("head_img")) {
                userBuilder.add("head_img", userJson.getString("head_img"));
            } else {
                userBuilder.add("head_img", "/images/icon_default_avatar.png");
            }
            if (userJson.containsKey("expiry")) {
                userBuilder.add("expiry", userDoc.get("expiry", Date.class).getTime());
            } else {
                userBuilder.add("expiry", TimeUtil.getRelateDate(new Date(), Calendar.DAY_OF_YEAR, 666).getTime());
            }

            AccountUtil.updateLoginStatus(userId);

            JsonObjectBuilder builder = Json.createObjectBuilder()
                    .add("user", userBuilder.build());
            if (token != null) {
                builder.add("token", token);
            }
            if (userJson.containsKey("company") && userJson.get("company") != null) {
                builder.add("company", userJson.get("company"));
            }
            if (userJson.containsKey("nickname") && userJson.get("nickname") != null) {
                builder.add("nickname", userJson.get("nickname"));
            }
            if (userJson.containsKey("authentication")) {
                builder.add("authentication", userJson.get("authentication"));
            }
            if (userJson.containsKey("openid")) {
                builder.add("openid", userJson.get("openid").toString());
            }
            String authName = "";
            if (userJson.containsKey(AccountUtil.Authority.SALE.name) &&
                    userJson.getBoolean(AccountUtil.Authority.SALE.name)) {
                builder.add("manager", AccountUtil.Authority.SALE.id);
                authName += AccountUtil.Authority.SALE.name;
            }
            if (userJson.containsKey(AccountUtil.Authority.ANALYST.name) &&
                    userJson.getBoolean(AccountUtil.Authority.ANALYST.name)) {
                builder.add("manager", AccountUtil.Authority.ANALYST.id);
                authName += AccountUtil.Authority.ANALYST.name;
            }
            if (userJson.containsKey(AccountUtil.Authority.SUPER_ADMIN.name) &&
                    userJson.getBoolean(AccountUtil.Authority.SUPER_ADMIN.name)) {
                builder.add("manager", AccountUtil.Authority.SUPER_ADMIN.id);
                authName += AccountUtil.Authority.SUPER_ADMIN.name;
            }
            if (userJson.containsKey("paid") &&
                    userJson.getBoolean("paid")) {
                builder.add("paid", true);
                builder.add("plan_name", "Official");
                builder.add("plan_name_zh_CN", "正式版");
                authName += " paid";
            } else {
                builder.add("plan_name", "Trial");
                builder.add("plan_name_zh_CN", "试用版");
            }
            logger.info(authName + " account login: " + userJson.getString("email"));
            return getResponse(true, builder.build());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, StringUtils.showError(e));
        }

    }

    /**
     * 注册帐号
     */
    @Path("v1/account/register")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject register(
            @NotNull @FormParam("email") String email,
            @DefaultValue("") @FormParam("username") String username,
            @NotNull @FormParam("password") String password,
            @DefaultValue("user") @FormParam("role") String role,
            @DefaultValue("") @FormParam("company") String companyName,
            @DefaultValue("") @FormParam("lang") String lang
    ) {
        try {

            if (StringUtils.isEmpty(username)) {
                username = email;
            }
            email = email.toLowerCase();

            /* 用户是否已注册 */
            if (AccountUtil.isEmailRegistered(email)) {
                logger.warn("email exist: " + email);
                return getResponse(false, 1, "该邮箱已注册, 请直接登录或去邮箱确认");
            }

            Document userDoc = new Document("email", email)
                    .append("username", username)
                    .append("password", password)
                    .append("role", role)
                    .append("company", companyName);

            Document user = AccountUtil.regist(userDoc);
            if (user == null) {
                logger.warn("user not exist: " + email);
                return getResponse(false, 2, "用户注册失败, 请联系客服");
            }
            boolean sendWorkEmailVerify = MailUtil.SendVerifyWorkEmailEmail(email, lang);
            if (!sendWorkEmailVerify) {
                return getResponse(false, 4, "send verify email failed");
            }

            // 生成登录token
            String userId = user.get("_id").toString();

            // 重设用户产品的起始时间
            ProductUtil.resetUserProductsTerm(userId);
//            Document res = new Document("email", email);
//            return getResponse(true, res);
            JsonObject data = getLoginResponse(userId, false).getJsonObject("data");
            return getResponse(true, 1, "please check your email", data);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            return getResponse(false, 3, StringUtils.showError(e));
        }
    }

    @Path("v1/account/invite")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject invite(
            @DefaultValue("") @FormParam("email") String inviteEmail
    ) {
        if (StringUtils.isEmpty(inviteEmail)) {
            return getResponse(false, 1, "邀请邮箱为空");
        }

        JsonObject userJson = AccountUtil.getUserByEmail(inviteEmail);
        if (userJson != null) {
            return getResponse(false, 2, "该邮箱已注册");
        }

        return getResponse(true);
    }

    @Path("v1/account/login-token")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject loginByToken(
            @NotNull @FormParam("id") String id,
            @NotNull @FormParam("token") String token,
            @DefaultValue("") @FormParam("cid") String companyId,
            @DefaultValue("") @FormParam("bid") String boardId,
            @DefaultValue("") @FormParam("verify") String verify,
            @DefaultValue("") @FormParam("reset-password") String password
    ) {
        JsonObject userJson = AccountUtil.getUserById(id);
        if (userJson == null) {
            logger.warn("user not exist: " + id);
            return getResponse(false, 1, "用户不存在");
        }

        if (!TokenUtil.isValid(token)) {
            logger.warn("user not exist: " + id);
            return getResponse(false, 2, "认证已过期, 请重新确认");
        }

        // for regist verify
        if (!StringUtils.isEmpty(verify)) {
            boolean res = AccountUtil.updateUserVerify(id, true);
            if (!res) {
                logger.error("update verify failed");
                return getResponse(false, 3, "");
            }
        }

        // for reset password
        if (!StringUtils.isEmpty(password)) {
            boolean res = AccountUtil.resetUserPassword(id, password);
            if (!res) {
                logger.warn("reset user password failed: " + id);
                return getResponse(false, 4, "更新密码失败, 请联系客服");
            }
        }

        AccountUtil.dropUserToken(id, token);

        return getLoginResponse(id);
    }

    @Path("v1/account/verify")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject verify(
            @NotNull @FormParam("id") String id,
            @NotNull @FormParam("token") String token
    ) {
        try {

            JsonObject userJson = AccountUtil.getUserById(id);
            if (userJson == null) {
                logger.warn("user not exist: " + id);
                return getResponse(false, 1, "用户不存在");
            }

            if (!TokenUtil.isValid(token)) {
                logger.warn("user not exist: " + id);
                return getResponse(false, 2, "认证已过期, 请重新确认");
            }

            boolean res = AccountUtil.updateUserVerify(id, true);
            if (!res) {
                logger.error("update verify failed");
                return getResponse(false, 3, "验证失败");
            }

            //drop tmp token
            AccountUtil.dropUserToken(id, token);

            //generate new token
            JsonObject newToken = TokenUtil.generateToken(userJson);

            //told new token to client
            JsonObjectBuilder builder = Json.createObjectBuilder();
            JsonObjectBuilder userBuilder = Json.createObjectBuilder();
            userBuilder.add("id", userJson.getString("id"));
            userBuilder.add("username", userJson.getString("username"));
            userBuilder.add("email", userJson.getString("email"));

            builder.add("token", newToken);
            builder.add("user", userBuilder.build());

            return getResponse(true, builder.build());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    @Path("v1/account/login")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject login(
            @FormParam("email") String email,
            @FormParam("password") String password,
            @DefaultValue("") @FormParam("lang") String lang
    ) {
        try {

            if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) {
                return getResponse(false, 1, "邮箱或密码不能为空");
            }
            email = email.toLowerCase();

            Document userDoc = AccountUtil.loginByEmail(email, password);
            if (userDoc == null) {
                logger.warn("user login failed: " + email);
                return getResponse(false, 2, "用户名或密码错误, 登录失败");
            }
            String userId = userDoc.get("_id").toString();
            Document oldUserConfig = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "old_user");
            Date oldUserCreateTime = null;
            if (oldUserConfig != null) {
                oldUserCreateTime = oldUserConfig.getDate("create_time");
            } else {
                try {
                    oldUserCreateTime = TimeUtil.strToDate("2017-11-01", "yyyy-MM-dd");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            Date createAt = (Date) userDoc.get("create_at");
            if (createAt != null && createAt.after(oldUserCreateTime)) {

                if ((!userDoc.containsKey("email_verify") || !userDoc.getBoolean("email_verify"))) {
                    // send verify email
                    MailUtil.SendVerifyWorkEmailEmail(email, lang);
                    JsonObject data = getLoginResponse(userId, false).getJsonObject("data");
                    return getResponse(true, 1, "please ensure your information", data);
                }
            } else {
                MongoUtil.updateOneById(MongoUtil.ACCOUNT_COL, userId, new Document("email_verify", true));
            }

        /*
        if (AccountUtil.isEmailExpired(email)) {
            logger.warn("user login failed by expiry: " + email);
            return getResponse(false, 3, "帐号已过期,请联系工作人员");
        }
        */
            return getLoginResponse(userId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 3, StringUtils.showError(e));
        }
    }

    @Path("v1/account/login-admin")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject loginByAdmin(
            @NotNull @FormParam("email") String email,
            @NotNull @FormParam("pass") String pass
    ) {
        try {

            if (email.isEmpty() || pass.isEmpty()) {
                return getResponse(false, 1, "email or password error");
            }
            String id = AccountUtil.getIdByEmail(email);
            if (StringUtils.isEmpty(id)) {
                logger.error(email + "not exists");
                return getResponse(false, 3, "user not exists");
            }
            String auth = AccountUtil.getUserAuthorityName(id);
            if (AccountUtil.Authority.NULL.name.equalsIgnoreCase(auth)) {
                return getResponse(false, 4, "no operation authority");
            }
            Document res = AccountUtil.loginByAdmin(email, pass);
            if (res == null) {
                logger.error(auth + "<" + email + "> login failed: maybe password error");
                return getResponse(false, 2, auth + "<" + email + "> login failed");
            }
//            logger.info(auth + "<" + email + "> login succeed");
            return getLoginResponse(String.valueOf(res.get("_id")));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    @PermitAll()
    @Path("v1/account/valid")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject valid(
    ) {
        return getResponse(true);
    }

    @Path("v1/account/logout")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject logout(
    ) {
        JsonObject userJson = getCurrentUser();
        String id = userJson.getString("id");
        String token = "";
        for (Cookie cookie : servletRequest.getCookies()) {
            if (cookie.getName().equals("token")) {
                token = cookie.getValue();
                break;
            }
        }

        if (StringUtils.isEmpty(token)) {
            logger.error("get token failed, user_id: " + id);
            return getResponse(false, 1, "用户未登录");
        }

        boolean res = AccountUtil.dropUserToken(id, token);

        logger.info("user logout: " + userJson.getString("email"));
        return getResponse(res);
    }

    /**
     * 修改用户密码
     * 普通用户只能修改自己的密码
     * 普通用户需要原密码确认
     * 管理员可直接设置密码
     * 管理员可修改id所属用户密码
     *
     * @param id
     * @param token
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @Path("v1/account/resetpasswd")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject resetPassword(
            @NotNull @FormParam("id") String id,
            @FormParam("token") String token,
            @FormParam("old_password") String oldPassword,
            @NotNull @FormParam("new_password") String newPassword
    ) {
        try {

            logger.info("user<" + id + "> is resetting password");
            String cuid = getCurrentUserId();
            JsonObject userJson = AccountUtil.getUserById(id);
            if (userJson == null) {
                logger.warn("user not exist: " + id);
                return getResponse(false, 1, "用户不存在");
            }

            if (!TokenUtil.isValid(token)) {
                logger.warn("user not exist: " + id);
                return getResponse(false, 2, "认证已过期, 请重新确认");
            }

            if (!AccountUtil.isSuperAdmin(cuid)) {
                if (!id.equals(cuid)) {
                    return getResponse(false, 3, "普通用户无权修改他人密码");
                }
                if (StringUtils.isEmpty(oldPassword)
                        || StringUtils.isEmpty(newPassword)) {
                    return getResponse(false, 4, "密码不能为空");
                }
                if (AccountUtil.checkUserPassword(id, oldPassword) == null) {
                    return getResponse(false, 5, "原密码错误");
                }
            }

            boolean res = AccountUtil.resetUserPassword(id, newPassword);
            if (!res) {
                logger.warn("user<" + id + "> reset user password failed");
                return getResponse(false, 6, "更新密码失败, 请联系客服");
            }

//        AccountUtil.dropUserToken(id, token);
            logger.info("user<" + id + ">reset password succeed");
            return getResponse(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }


    /**
     * 创建帐号
     *
     * @param email
     * @return
     */
    @Path("v1/account/create")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject create(
            @NotNull @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("pass") String password,
            @DefaultValue("365") @QueryParam("expiry") int expiry,
            @DefaultValue("") @QueryParam("expiry_date") String expiryDate,
            @DefaultValue("") @QueryParam("auth") String authority,
            @DefaultValue("") @QueryParam("tel") String telephone
    ) {
        try {

            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 1, "无操作权限");
            }
            email = email.toLowerCase();

            if (AccountUtil.isEmailRegistered(email)) {
                return getResponse(false, 2, "邮件已注册");
            }

            if (StringUtils.isEmpty(password)) {
                password = RandomStringUtils.randomAlphanumeric(8);
            }

            if (StringUtils.isEmpty(authority)) {
                authority = AccountUtil.Authority.NULL.name();
            }

            if (expiry < 0) {
                return getResponse(false, 5, "expiry can't be less than 0");
            }

            Document user = AccountUtil.createUserDoc(
                    email, DigestUtils.md5Hex(password),
                    expiry, authority, telephone);
            if (user == null || user.isEmpty()) {
                return getResponse(false, 3, "创建帐号失败");
            }

            if (!StringUtils.isEmpty(expiryDate)) {
                try {
                    Date date = TimeUtil.strToDate(expiryDate, "yyyy-MM-dd");
                    if (date.before(new Date())) {
                        return getResponse(false, 6, "expiry_date can't before than now");
                    }
                } catch (Exception ignore){}

                AccountUtil.setExpiry(email, expiryDate);
            }
            user.put("password", password);
            return getResponse(true, user);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 4, StringUtils.showError(e));
        }
    }

    /**
     * 重置帐号
     *
     * @return
     */
    @Path("v1/account/reset")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject resetDemo(
            @NotNull @QueryParam("email") String email
    ) {
        try {

            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 1, "无操作权限");
            }

            String uid = AccountUtil.getIdByEmail(email);
            if (StringUtils.isEmpty(uid)) {
                return getResponse(false, 2, "email尚未注册");
            }

            boolean res = AccountUtil.reset(uid);

            return getResponse(res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    /**
     * 设置用户权限
     *
     * @return
     */
    @Path("v1/account/set-right")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setRight(
            @NotNull @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("marker") String marker,
            @DefaultValue("") @QueryParam("desp") String desp,
            @DefaultValue("") @QueryParam("common") String common,
            @DefaultValue("") @QueryParam("export") String export
    ) {
        try {

            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 1, "无操作权限");
            }

            String uid = AccountUtil.getIdByEmail(email);
            if (StringUtils.isEmpty(uid)) {
                return getResponse(false, 2, "email尚未注册");
            }

            boolean res = false;
            if (!StringUtils.isEmpty(marker)) {
                res = AccountUtil.setAccountRight(email, "marker", Integer.parseInt(marker));
            }
            if (!StringUtils.isEmpty(desp)) {
                res = AccountUtil.setAccountRight(email, "desp", Integer.parseInt(desp));
            }
            if (!StringUtils.isEmpty(export)) {
                res = AccountUtil.setAccountRight(email, "export", Integer.parseInt(export));
            }
            if (!StringUtils.isEmpty(common)) {
                res = AccountUtil.setAccountRight(email, "common", Integer.parseInt(common));
            }

            return getResponse(res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    /**
     * 设置昵称
     * 不能包含空格， 不能重名
     *
     * @return
     */
    @Path("v1/account/set-nickname")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setNickName(
            @DefaultValue("") @FormParam("nickname") String nickname
    ) {
        try {

            String uid = getCurrentUserId();

            boolean res = AccountUtil.setNickName(uid, nickname);

            return getResponse(res, res ? "设置昵称成功" : "设置昵称失败");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    /**
     * 返回是否设置过昵称
     *
     * @return
     */
    @Path("v1/account/have-nickname")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject haveNickName() {
        try {

            String cuid = getCurrentUserId();
            String nickname = AccountUtil.getNicknameById(cuid);
            boolean res = !StringUtils.isEmpty(nickname);
            return getResponse(res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, StringUtils.showError(e));
        }
    }

    /**
     * 查看用户详细信息
     * 用户和管理员查看的信息有区别
     *
     * @param id
     * @return
     */
    @Path("v1/account/{id: [0-9a-zA-Z]+}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject detail(
            @PathParam("id") String id
    ) {
        try {
            String cuid = getCurrentUserId();
            logger.info("user:<" + cuid + "> is getting account<" + id + "> info");
            //只有自己或管理员能查看
            if (AccountUtil.isSale(cuid) || cuid.equals(id)) {

                Document userDoc = AccountUtil.getUserDetail(id);
                if (userDoc == null) {
                    logger.warn("user not exist: " + id);
                    return getResponse(false, 1, "用户不存在");
                }

                return getResponse(true, userDoc);
            } else {
                return getResponse(false, "无查看其他用户权限");

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, "获取用户信息失败" + StringUtils.showError(e));
        }
    }

    /**
     * 获取用户信息
     *
     * @param openid
     * @return
     */
    @Path("v1/account/openid/{openid: [\\w\\d]+}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject detailByOpenId(
            @PathParam("openid") String openid
    ) {
        try {

            Document userDoc = AccountUtil.getUserByOpenId(openid);
            if (userDoc != null) {
                Document data = new Document("email", userDoc.getString("email") == null
                        ? "" : userDoc.getString("email"))
                        .append("username", userDoc.getString("username") == null
                                ? "" : userDoc.getString("username"))
                        .append("company", userDoc.getString("company") == null
                                ? "" : userDoc.getString("company"));
                return getResponse(true, data);
            } else {
                return getResponse(false, 1, "获取用户信息失败");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return getResponse(false, 2, "获取用户信息出错" + StringUtils.showError(e));
        }

    }

    /**
     * 编辑用户信息
     * 普通用户只能修改:
     * company, password
     * 字段，
     * 管理员可修改全部字段
     *
     * @param token       修改密码需要该参数
     * @param oldPassword 修改密码时的原密码
     * @param newPassword 修改密码时的新密码
     * @param pwdEnds     密码提示，格式：[前两位]****[后两位]
     * @return
     */
    @Path("v1/account/edit")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject edit(
            @DefaultValue("") @FormParam("email") String email,
            @DefaultValue("") @FormParam("username") String username,
            @DefaultValue("") @FormParam("company") String company,
            @DefaultValue("") @FormParam("old_password") String oldPassword,
            @DefaultValue("") @FormParam("new_password") String newPassword,
            @DefaultValue("") @FormParam("pwd_ends") String pwdEnds,
            @DefaultValue("") @FormParam("head_img") String head_img,
            @DefaultValue("-1") @FormParam("gender") int gender,
            @DefaultValue("-1") @FormParam("i_f") int investmentFields,
            @DefaultValue("-1") @FormParam("i_m") int investmentMarket,
            @DefaultValue("-1") @FormParam("i_s") int investmentStyle,
            @DefaultValue("-1") @FormParam("asset_size") int assetSize,
            @DefaultValue("") @FormParam("company_full") String companyFullName,
            @DefaultValue("") @FormParam("telephone") String telephone,
            @DefaultValue("") @FormParam("job_title") String job_title,
            @DefaultValue("") @FormParam("department") String department,
            @DefaultValue("") @FormParam("token") String token
    ) {
        try {

            String userId = getCurrentUserId();
            logger.info("edit user information");
            JsonObjectBuilder accountJsonBuilder = Json.createObjectBuilder();
            //管理员可修改以下字段
            if (AccountUtil.isSuperAdmin(userId)) {
                if (!StringUtils.isEmpty(email)) {
                    accountJsonBuilder.add("email", email);
                }
                if (!StringUtils.isEmpty(head_img)) {
                    accountJsonBuilder.add("head_img", head_img);
                }
                if (!StringUtils.isEmpty(username)) {
                    accountJsonBuilder.add("username", username);
                }
                if (!StringUtils.isEmpty(telephone)) {
                    accountJsonBuilder.add("telephone", telephone);
                }
                if (!StringUtils.isEmpty(job_title)) {
                    accountJsonBuilder.add("job_title", job_title);
                }
                if (!StringUtils.isEmpty(department)) {
                    accountJsonBuilder.add("department", department);
                }
                if (gender > 0) {
                    accountJsonBuilder.add("gender", gender);
                }
                if (investmentFields > 0) {
                    accountJsonBuilder.add("i_f", investmentFields);
                }
                if (investmentMarket > 0) {
                    accountJsonBuilder.add("i_m", investmentMarket);
                }
                if (investmentStyle > 0) {
                    accountJsonBuilder.add("i_s", investmentStyle);
                }
                if (assetSize > 0) {
                    accountJsonBuilder.add("asset_size", assetSize);
                }
                if (!companyFullName.isEmpty()) {
                    accountJsonBuilder.add("company_full", companyFullName);
                }
            }
            if (!StringUtils.isEmpty(company)) {
                accountJsonBuilder.add("company", company);
            }
            if (!StringUtils.isEmpty(oldPassword) && !StringUtils.isEmpty(newPassword)) {
                accountJsonBuilder.add("pwd_ends", pwdEnds);
                JsonObject resJson = resetPassword(userId, token, oldPassword, newPassword);
                Document resDoc = MongoUtil.json2Document(resJson);
                if (!resDoc.getBoolean("success")) {
                    return resJson;
                }
            }

            boolean res = AccountUtil.updateAccountInfo(userId, accountJsonBuilder.build());
            if (!res) {
                logger.error("edit user information failed");
                return getResponse(false, 1, "修改用户信息失败");
            }
            logger.info("edit user information succeed: " + userId);
            return getResponse(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 2, StringUtils.showError(e));
        }
    }

    /**
     * 修改付费用户资料
     *
     * @param gender
     * @param investmentFields
     * @param investmentMarket
     * @param investmentStyle
     * @param assetSize
     * @param companyFullName
     * @param telephone
     * @param department
     * @return
     */
    @Path("v1/account/edit-paid")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject editPaidAccount(
            @DefaultValue("") @FormParam("id") final String id,
            @DefaultValue("") @FormParam("password") final String password,
//            @DefaultValue("") @FormParam("username") final String username,
//            @DefaultValue("") @FormParam("company") final String company,
            @DefaultValue("-1") @FormParam("gender") int gender,
            @DefaultValue("-1") @FormParam("i_f") int investmentFields,
            @DefaultValue("-1") @FormParam("i_m") int investmentMarket,
            @DefaultValue("-1") @FormParam("i_s") int investmentStyle,
            @DefaultValue("-1") @FormParam("asset_size") int assetSize,
            @DefaultValue("") @FormParam("company_full") String companyFullName,
            @DefaultValue("") @FormParam("department") final String department,
            @DefaultValue("") @FormParam("job_title") final String jobTitle,
            @DefaultValue("") @FormParam("telephone") final String telephone,
            @DefaultValue("") @FormParam("lang") String lang
    ) {
        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSale(cuid) || !AccountUtil.isPaidAccount(id)) {
                return getResponse(false, 1, "无操作权限");
            }
            if (id.isEmpty()) {
                return getResponse(false, 2, "参数错误");
            }
            Map<String, Object> paramMap = new HashMap<>();
            if (!password.isEmpty()) {
                paramMap.put("password", password);
            }
//            if (!username.isEmpty()) {
//                paramMap.put("username", username);
//            }
//            if (!company.isEmpty()) {
//                paramMap.put("company", company);
//            }
            if (gender > 0) {
                paramMap.put("gender", gender);
            }
            if (investmentFields > 0) {
                paramMap.put("i_f", investmentFields);
            }
            if (investmentMarket > 0) {
                paramMap.put("i_m", investmentMarket);
            }
            if (investmentStyle > 0) {
                paramMap.put("i_s", investmentStyle);
            }
            if (assetSize > 0) {
                paramMap.put("asset_size", assetSize);
            }
            if (!companyFullName.isEmpty()) {
                paramMap.put("company_full", companyFullName);
            }
            if (!department.isEmpty()) {
                paramMap.put("department", department);
            }
            if (!jobTitle.isEmpty()) {
                paramMap.put("job_title", jobTitle);
            }
            if (!telephone.isEmpty()) {
                paramMap.put("telephone", telephone);
            }
            //更新用户信息
            boolean res;
            int userAuthority = AccountUtil.getUserAuthority(cuid);
                logger.info(String.format("user:<%s> is editing paid account<%s> self-info", cuid, id));
                res = AccountUtil.editPaidAccount(userAuthority, id, paramMap);
            return getResponse(res, 0, res ? "编辑账号成功" : "编辑账号失败");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "编辑失败" + StringUtils.showError(e));
        }
    }
    /**
     * 设置过期时限
     *
     * @return
     */
    @Path("v1/account/set-expiry")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setExpiry(
            @NotNull @QueryParam("email") String email,
            @DefaultValue("365") @QueryParam("expiry") int expiry,
            @DefaultValue("") @QueryParam("expiry_date") String expiryDate
    ) {
        try {

            String cuid = getCurrentUserId();
            if (AccountUtil.isNotAnalyst(cuid)) {
                return getResponse(false, 1, "无操作权限");
            }

            String uid = AccountUtil.getIdByEmail(email);
            if (StringUtils.isEmpty(uid)) {
                return getResponse(false, 2, "email尚未注册");
            }

            //分析师无权设置付费账户的期限
            if (AccountUtil.isNotSuperAdmin(cuid) &&
                    AccountUtil.isPaidAccount(uid)) {
                return getResponse(false, 3,
                        "analyst cannot set expiry of paid account");
            }
            boolean res;
            if (!StringUtils.isEmpty(expiryDate)) {
                res = AccountUtil.setExpiry(email, expiryDate);
            } else {
                res = AccountUtil.setExpiry(email, expiry);
            }

            return getResponse(res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    /**
     * 账号列表
     *
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param status 0:注冊 1:未注册 2:全部
     * @param query
     * @param trail  0:正式账号 1:试用账号  2:全部
     * @return
     */
    @Path("v1/accounts")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getAccounts(
            @DefaultValue("create_at") @QueryParam("sort") String sort,
            @DefaultValue("desc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("2") @QueryParam("status") int status,
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("2") @QueryParam("trail") int trail,
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("2") @QueryParam("deleted") int deleted,
            @DefaultValue("2") @QueryParam("paid") int paid
    ) {
        String cuid = getCurrentUserId();
        if (AccountUtil.isNotSale(cuid)) {
            return getResponse(false, 2, "无操作权限");
        }

        try {
            Document document = AccountUtil.getAccounts(
                    cuid, sort, order, offset,
                    limit, status, query, trail,
                    lang, deleted, paid);
            if (document == null || document.isEmpty()) {
                return getResponse(false, 3, "获取账号列表失败");
            }

            return getResponse(true, document);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "获取账号列表失败" + StringUtils.showError(e));
        }

    }

    /**
     * 生成试用账号
     *
     * @return resp 账号列表, 新生成的账号放在列表首位
     */
    @Path("v1/account/create-trail")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject createTrailAccount() {
        try {

            String cuid = getCurrentUserId();
            if (AccountUtil.isNotAnalyst(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            logger.info("user<" + getCurrentUserEmail() + "> is creating trail account");
            Document user = AccountUtil.createTrailAccount();

            if (user == null || user.isEmpty()) {
                return getResponse(false, 3, "create trail account failed");
            }

            logger.info("trail account: " + user.getString("email") + " was created");

            return getResponse(true, user);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 2, StringUtils.showError(e));
        }
    }

    @Path("v1/account/forget-resetpasswd")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject forgetResetPassword(
            @NotNull @FormParam("id") String id,
            @FormParam("token") String token,
            @FormParam("password") String pass,
            @NotNull @FormParam("confirm_password") String conPass
    ) {
        try {

            logger.info("forget-reset user password: ");
//        String cuid = getCurrentUserId();
            JsonObject userJson = AccountUtil.getUserById(id);
            if (userJson == null) {
                logger.warn("user not exist: " + id);
                return getResponse(false, 1, "用户不存在");
            }

            if (!TokenUtil.isValid(token)) {
                logger.warn("user not exist: " + id);
                return getResponse(false, 2, "认证已过期, 请重新确认");
            }

//        if (!AccountUtil.isSuperAdmin(cuid)) {
//            if (!id.equals(cuid)) {
//                return getResponse(false, 3, "普通用户无权修改他人密码");
//            }
            if (StringUtils.isEmpty(pass) ||
                    StringUtils.isEmpty(conPass)
                    ) {
                return getResponse(false, 4, "密码不能为空");
            }
            if (!pass.equalsIgnoreCase(conPass)) {
                return getResponse(false, 5, "两次密码不一致");
            }
//        }

            boolean res = AccountUtil.resetUserPassword(id, pass);
            if (!res) {
                logger.warn("user<" + id + "> reset user password failed");
                return getResponse(false, 6, "更新密码失败, 请联系客服");
            }

//        AccountUtil.dropUserToken(id, token);
            logger.warn("user<" + id + "> reset user password succeed");
            return getLoginResponse(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 7, StringUtils.showError(e));
        }
    }

    /**
     * 分析师编辑试用用户
     *
     * @param id
     * @param plan
     * @return
     */
    @Path("v1/account/edit-trail")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject editTrailAccount(
            @DefaultValue("") @FormParam("id") final String id,
            @DefaultValue("") @FormParam("password") final String password,
//            @DefaultValue("") @FormParam("username") final String username,
//            @DefaultValue("") @FormParam("company") final String company,
            @DefaultValue("") @FormParam("username") final String username,
            @DefaultValue("") @FormParam("company") final String company,
            @DefaultValue("-1") @FormParam("gender") int gender,
            @DefaultValue("-1") @FormParam("i_f") int investmentFields,
            @DefaultValue("-1") @FormParam("i_m") int investmentMarket,
            @DefaultValue("-1") @FormParam("i_s") int investmentStyle,
            @DefaultValue("-1") @FormParam("asset_size") int assetSize,
            @DefaultValue("") @FormParam("company_full") String companyFullName,
            @DefaultValue("") @FormParam("department") final String department,
            @DefaultValue("") @FormParam("job_title") final String jobTitle,
            @DefaultValue("") @FormParam("telephone") final String telephone,
            @DefaultValue("-1") @FormParam("plan") final int plan,
            @DefaultValue("-1") @FormParam("term_long") final int termLong,
            @DefaultValue("year") @FormParam("term_unit") final String termUnit,
            @DefaultValue("") @FormParam("pids[]") List<String> pids,
            @DefaultValue("") @FormParam("lang") String lang
    ) {
        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotAnalyst(cuid) || AccountUtil.isNotTrailUser(id)) {
                return getResponse(false, 1, "无操作权限");
            }
            if (id.isEmpty()) {
                return getResponse(false, 2, "参数错误");
            }
            Map<String, Object> paramMap = new HashMap<>();
            if (!password.isEmpty()) {
                paramMap.put("password", password);
            }
            if (!username.isEmpty()) {
                paramMap.put("username", username);
            }
            if (!company.isEmpty()) {
                paramMap.put("company", company);
            }
//            if (!username.isEmpty()) {
//                paramMap.put("username", username);
//            }
//            if (!company.isEmpty()) {
//                paramMap.put("company", company);
//            }
            if (gender > 0) {
                paramMap.put("gender", gender);
            }
            if (investmentFields > 0) {
                paramMap.put("i_f", investmentFields);
            }
            if (investmentMarket > 0) {
                paramMap.put("i_m", investmentMarket);
            }
            if (investmentStyle > 0) {
                paramMap.put("i_s", investmentStyle);
            }
            if (assetSize > 0) {
                paramMap.put("asset_size", assetSize);
            }
            if (!companyFullName.isEmpty()) {
                paramMap.put("company_full", companyFullName);
            }
            if (!department.isEmpty()) {
                paramMap.put("department", department);
            }
            if (!jobTitle.isEmpty()) {
                paramMap.put("job_title", jobTitle);
            }
            if (!telephone.isEmpty()) {
                paramMap.put("telephone", telephone);
            }
            //更新用户信息
            boolean res;
            pids.removeIf(String::isEmpty);
            if (plan < 0 || termLong < 0 || pids.size() <= 0) {
                logger.info(String.format("user:<%s> is editing account<%s> self-info", cuid, id));
                res = AccountUtil.editTrailAccount(cuid, id, paramMap);
            } else {
                logger.info(String.format("user:<%s> is editing account<%s> user-products", cuid, id));
//                logger.info(Arrays.toString(pids.toArray()) + "  " + pids.size());
                logger.info("pids count: " + pids.size());
                res = AccountUtil.editTrailAccount(
                        cuid, id, paramMap,
                        plan, termLong, termUnit, pids, lang);
            }

            return getResponse(res, 0, res ? "编辑账号成功" : "编辑账号失败");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "编辑失败" + StringUtils.showError(e));
        }
    }


    /**
     * 编辑免费账号
     *
     * @param id
     * @param password
     * @param department
     * @param jobTitle
     * @param telephone
     * @param plan
     * @param termLong
     * @param termUnit
     * @param pids
     * @param lang
     * @return
     */
    @Path("v1/account/edit-unpaid")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject editUnpaidAccount(
            @DefaultValue("") @FormParam("id") final String id,
            @DefaultValue("") @FormParam("password") final String password,
//            @DefaultValue("") @FormParam("username") final String username,
//            @DefaultValue("") @FormParam("company") final String company,
            @DefaultValue("-1") @FormParam("gender") int gender,
            @DefaultValue("-1") @FormParam("i_f") int investmentFields,
            @DefaultValue("-1") @FormParam("i_m") int investmentMarket,
            @DefaultValue("-1") @FormParam("i_s") int investmentStyle,
            @DefaultValue("-1") @FormParam("asset_size") int assetSize,
            @DefaultValue("") @FormParam("company_full") String companyFullName,
            @DefaultValue("") @FormParam("department") final String department,
            @DefaultValue("") @FormParam("job_title") final String jobTitle,
            @DefaultValue("") @FormParam("telephone") final String telephone,
            @DefaultValue("-1") @FormParam("plan") final int plan,
            @DefaultValue("-1") @FormParam("term_long") final int termLong,
            @DefaultValue("year") @FormParam("term_unit") final String termUnit,
            @DefaultValue("") @FormParam("pids[]") final List<String> pids,
            @DefaultValue("") @FormParam("lang") String lang
    ) {
        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSale(cuid) || AccountUtil.isPaidAccount(id)) {
                return getResponse(false, 1, "无操作权限");
            }
            if (id.isEmpty()) {
                return getResponse(false, 2, "参数错误");
            }
            Map<String, Object> paramMap = new HashMap<>();
            if (!password.isEmpty()) {
                paramMap.put("password", password);
            }
//            if (!username.isEmpty()) {
//                paramMap.put("username", username);
//            }
//            if (!company.isEmpty()) {
//                paramMap.put("company", company);
//            }
            if (gender > 0) {
                paramMap.put("gender", gender);
            }
            if (investmentFields > 0) {
                paramMap.put("i_f", investmentFields);
            }
            if (investmentMarket > 0) {
                paramMap.put("i_m", investmentMarket);
            }
            if (investmentStyle > 0) {
                paramMap.put("i_s", investmentStyle);
            }
            if (assetSize > 0) {
                paramMap.put("asset_size", assetSize);
            }
            if (!companyFullName.isEmpty()) {
                paramMap.put("company_full", companyFullName);
            }
            if (!department.isEmpty()) {
                paramMap.put("department", department);
            }
            if (!jobTitle.isEmpty()) {
                paramMap.put("job_title", jobTitle);
            }
            if (!telephone.isEmpty()) {
                paramMap.put("telephone", telephone);
            }
            //更新用户信息
            boolean res;
            if (plan < 0 || termLong < 0 || pids.size() <= 0) {
                res = AccountUtil.editUnpaidAccount(cuid, id, paramMap);
            } else {
                if (AccountUtil.isOnlySale(cuid) && !ProductUtil.isLegalSalePids(id, new ArrayList<>(pids))) {
                    return getResponse(false, 2, "product id count illegal: 2 + x <= 4");
                }
                res = AccountUtil.editUnpaidAccount(
                        cuid, id, paramMap,
                        plan, termLong, termUnit, pids, lang);
            }

            return getResponse(res, 0, res ? "编辑账号成功" : "编辑账号失败");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "编辑失败" + StringUtils.showError(e));
        }
    }

    /**
     * 删除试用用户　需要分析师权限
     *
     * @param userId 　要删除的用户id
     * @return
     */
    @Path("v1/account/delete-trail/{id: [\\d\\w]+}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject delete(
            @PathParam("id") String userId
    ) {

        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotAnalyst(cuid) || AccountUtil.isNotTrailUser(userId)) {
                return getResponse(false, "无删除用户权限");
            }
            Document res = AccountUtil.deleteOneById(userId);
            if (res != null) {
                logger.warn(String.format("user<%s> deleted account<%s>", cuid, userId));
            }
            return getResponse(true, res);

        } catch (Exception e) {
            logger.error(e.getMessage());
            return getResponse(false, "删除用户失败" + StringUtils.showError(e));
        }
    }

    /**
     * 删除用户　需要管理员权限
     *
     * @return
     */
    @Path("v1/account/delete")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject delete(
            @NotNull @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("account") String ac,
            @DefaultValue("") @QueryParam("wechat") String wx
    ) {

        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSuperAdmin(cuid)) {
                return getResponse(false, 3, "not operation authority");
            }
            if (AccountUtil.isNotUserExists(email)) {
                return getResponse(false, 4, "user not exists");
            }
            if (AccountUtil.isPaidAccount(AccountUtil.getIdByEmail(email))) {
                return getResponse(false, 5, "user is paid account");
            }
            logger.info("admin<" + cuid + "> is deleting user<" + email + ">");
            Document res = AccountUtil.deleteOneByEmail(email, ac, wx);
            if (res == null) {
                return getResponse(false, 2, "failed to delete user");
            }
            if (res.isEmpty()) {
                logger.info("user: " + email + " was deleted");
                return getResponse(true, "user: " + email + " was deleted");
            }
            return getResponse(false, res);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, StringUtils.showError(e));
        }
    }

    @Path("v1/account/forget-pass")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject forgetPass(
            @NotNull @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("lang") String lang
    ) {
        try {

            if (email.isEmpty()) {
                return getResponse(false, 2, "email cannot be null");
            }
            boolean res = MailUtil.SendVerifyIdentityEmail(email, lang);
            if (!res) {
                getResponse(false, 3, "send identity verify email error");
            }
            return getResponse(true, "please check you email to verify");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, StringUtils.showError(e));
        }
    }

    /**
     * @param email
     * @param name
     * @param company
     * @param password
     * @param pwdEnds
     * @return
     */
    @POST
    @Path("/v1/account/first-login-bind")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject bindingEmail(
            @FormParam("email") String email,
            @FormParam("name") String name,
            @FormParam("company") String company,
            @FormParam("password") String password,
            @FormParam("pwd_ends") String pwdEnds
    ) {
        logger.info("account bind email: " + email);
        if (email.isEmpty()) {
            email = getCurrentUserEmail();
        }
        Document accountDoc = AccountUtil.getUserDocByEmail(email);
        try {
            if (accountDoc == null && accountDoc.isEmpty()) {
                return getResponse(false, 3, "user not exists");
            }

            Document res = AccountUtil.bindEmail(email, name, company, password, pwdEnds);
            return res != null
                    ? getLoginResponse(getCurrentUserId())
                    : getResponse(false, 2, "bind email error");
        } catch (Exception e) {
            logger.info("bind email error", e);
            return getResponse(false, 1, "账户绑定email失败" + StringUtils.showError(e));
        }

    }

    /**
     * 管理员设置帐号密码，url中手动调用
     *
     * @param email
     * @return
     */
    @Path("v1/account/set-pw")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setPassword(
            @NotNull @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("pass") String password
    ) {
        try {

            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSale(cuid)) {
                return getResponse(false, 1, "无操作权限");
            }
            logger.info(String.format("admin<%s> setting user<%s> password", cuid, email));
            email = email.toLowerCase();

            if (!AccountUtil.isEmailRegistered(email)) {
                return getResponse(false, 2, "邮件未注册");
            }

            if (StringUtils.isEmpty(password)) {
                password = RandomStringUtils.randomAlphanumeric(8);
            }

            JsonObject userJson = AccountUtil.getUserByEmail(email);
            String userId = userJson.getString("id");
            boolean res = AccountUtil.resetUserPassword(userId, DigestUtils.md5Hex(password));
            if (!res) {
                return getResponse(false, 3, "设置密码失败");
            }

            Document resp = new Document()
                    .append("email", email)
                    .append("password", password);

            return getResponse(true, resp);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    /**
     * 在编辑页面中调用
     *
     * @param email
     * @param password
     * @return
     */
    @Path("v1/account/set-password")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setPasswordInEdit(
            @FormParam("email") String email,
            @DefaultValue("") @FormParam("pass") String password,
            @DefaultValue("") @FormParam("pwd_ends") String pwdEnds
    ) {
        try {

            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSale(cuid)) {
                return getResponse(false, 1, "无操作权限");
            }
            logger.info(String.format("admin<%s> setting user<%s> password", cuid, email));
            email = email.toLowerCase();

            if (!AccountUtil.isEmailRegistered(email)) {
                return getResponse(false, 2, "邮件未注册");
            }

            if (StringUtils.isEmpty(password)) {
                password = RandomStringUtils.randomAlphanumeric(8);
            }

            JsonObject userJson = AccountUtil.getUserByEmail(email);
            String userId = userJson.getString("id");
            boolean res = AccountUtil.resetUserPassword(userId, password);
            if (!res) {
                return getResponse(false, 3, "设置密码失败");
            }
            AccountUtil.updateUserInfo(userId, "pwd_ends", pwdEnds);
            Document resp = new Document()
                    .append("email", email)
                    .append("password", password);

            return getResponse(true, resp);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    /**
     * 管理员手动激活用户邮箱账号
     *
     * @param email
     * @return
     */
    @Path("v1/account/active")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject activeAccount(
            @NotNull @QueryParam("email") String email
    ) {
        try {

            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSuperAdmin(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            logger.info("admin<" +
                    getCurrentUserEmail() +
                    "> is active user<" +
                    email + "> account");

            Document updateDoc = new Document("email_verify", true)
                    .append("status", AccountUtil.UserStatus.Registered.ordinal());
            Document res = MongoUtil.updateOneByField(MongoUtil.ACCOUNT_COL, "email", email, updateDoc);
            return res == null
                    ? getResponse(false, 2, "active account error")
                    : getResponse(true, res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 3, StringUtils.showError(e));
        }
    }

    @Path("v1/accounts/managers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getManagers(
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("") @QueryParam("auth") String auth,
            @DefaultValue("create_at") @QueryParam("sort") String sort,
            @DefaultValue("desc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("all") @QueryParam("type") String type,
            @DefaultValue("") @QueryParam("lang") String lang
    ) {
        //TODO: response refactor
        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSuperAdmin(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            Document res = AccountUtil.getManagers(query, auth, sort, order, offset, limit, type, lang);
            if (res == null) {
                return getResponse(false, 2, "get manages failed");
            }
            return getResponse(true, res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    /**
     * 返回当前用户权限等级
     *
     * @return
     */
    @Path("v1/account/auth")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getAuthority(
            @DefaultValue("") @QueryParam("email") String email
    ) {
        try {
            String cuid = getCurrentUserId(), userId;
            if (AccountUtil.isNotSale(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            if (email.isEmpty()) {
                userId = cuid;
            } else {
                userId = AccountUtil.getIdByEmail(email);
            }
            if (!cuid.equals(userId) && AccountUtil.isNotSuperAdmin(cuid)) {
                return getResponse(false, 3,
                        "only super admin have authority to get others auth");
            }
            int auth = AccountUtil.getUserAuthority(userId);
            if (auth < 0) {
                return getResponse(false, 2, "user not exists");
            }
            Document res = new Document("auth_level", auth)
                    .append("auth_name", AccountUtil.Authority.valueOf(auth).name)
                    .append("user_id", userId);
            return getResponse(true, res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    /**
     * 返回当前用户权限等级
     *
     * @return
     */
    @Path("v1/account/set-auth")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setAuthority(
            @DefaultValue("") @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("auth") String auth,
            @DefaultValue("") @QueryParam("tel") String telephone,
            @DefaultValue("") @QueryParam("remove") String remove

    ) {
        try {
            String cuid = getCurrentUserId(), userId = null;
            if (AccountUtil.isNotSuperAdmin(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            userId = AccountUtil.getIdByEmail(email);
            if (AccountUtil.isSuperAdmin(userId)) {
                return getResponse(false, 1, "user is already super admin");
            }
            if (email.isEmpty()) {
                return getResponse(false, 4, "email cannot be empty");
            }
            if (userId == null) {
                return getResponse(false, 2, "user not exists");
            }
            if (!AccountUtil.isLegalAuthority(auth)) {
                return getResponse(false, 3, "auth is illegal");
            }
            Document res = AccountUtil.setUserAuthority(userId, auth, telephone, remove);
            if (res == null) {
                return getResponse(
                        false, 3,
                        "admin<" + cuid + ">set authority for user<" + userId + "> error");
            }
            if (res.containsKey("error")) {
                return getResponse(false, 5,
                        String.valueOf(res.getOrDefault("error", "")));
            }
            return getResponse(true, "set authority for " + email + " succeed");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    /**
     * 管理员指定付费用户
     *
     * @param opt
     * @param email
     * @return
     */
    @Path("v1/account/mark/{opt: paid|unpaid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject markPaidAccount(
            @PathParam("opt") String opt,
            @DefaultValue("") @QueryParam("email") String email
    ) {
        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSuperAdmin(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            if (email.isEmpty()) {
                return getResponse(false, 2, "param error: email cannot be empty");
            }
            if (!AccountUtil.isUserExists(email)) {
                return getResponse(false, 3, "param error: user is not exists");
            }
            Document res = AccountUtil.markPaidAccount(opt, email);
            if (res != null) {
                return getResponse(true, "mark " + opt + " account succeed");
            }
            return getResponse(false, 4, "mark " + opt + " account failed");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    @Path("v1/account/predefined")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getAccountPredefinedInfo() {
        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSale(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            Document resDoc = AccountUtil.getAccountPredefinedInfo();
            if (resDoc == null) {
                return getResponse(false, 2, "get account predefined info failed");
            }
            return getResponse(true, resDoc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }


    @Path("v1/account/sale-user")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject connectSaleToUser(
            @DefaultValue("") @QueryParam("sale") String saleEmail,
            @DefaultValue("") @QueryParam("user") String userEmail,
            @DefaultValue("") @QueryParam("invite_code") String inviteCode
    ) {

        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSuperAdmin(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            if (saleEmail.isEmpty()) {
                return getResponse(false, 3, "email cannot be empty");
            }
            if (userEmail.isEmpty()) {
                return getResponse(false, 4, "invite code cannot be empty");
            }
            String saleId = AccountUtil.getIdByEmail(saleEmail);
            if (AccountUtil.isNotSale(saleId)) {
                return getResponse(false, 5, saleEmail + " is not sale");
            }
            Document resDoc = AccountUtil.connectSaleToUser(saleEmail, userEmail, inviteCode);
            if (resDoc == null) {
                logger.info(String.format("failed: admin<%s> connecting sale<%s> to user<%s>", cuid, saleEmail, userEmail));
                return getResponse(false, 2, "connect error");
            }
            logger.info(String.format("admin<%s> connecting sale<%s> to user<%s>", cuid, saleEmail, userEmail));
            return getResponse(true, "connect succeed");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }

    }
}
