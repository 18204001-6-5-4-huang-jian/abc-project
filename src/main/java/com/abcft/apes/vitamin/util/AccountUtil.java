package com.abcft.apes.vitamin.util;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.json.JsonObject;
import javax.ws.rs.NotAuthorizedException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class AccountUtil {
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_ROOT = "super_admin";
    public static String COL = MongoUtil.ACCOUNT_COL;
    private static Logger logger = Logger.getLogger(AccountUtil.class);

    /**
     * 设置昵称
     * 不能为空，不能重名
     *
     * @param uid
     * @param nickname
     * @return
     */
    public static boolean setNickName(String uid, String nickname) {
        nickname = nickname.replaceAll(" ", "")
                .replaceAll("\\$", "＄")
                .replaceAll("\\.", "．")
                .replaceAll("\\*", "＊");
        if (StringUtils.isEmpty(nickname)) {
            return false;
        }
        List<Bson> conds = Arrays.asList(
                ne("_id", new ObjectId(uid)),
                Filters.eq("nickname", nickname)
        );
        Document sameNickName = MongoUtil.getOneByConditions(MongoUtil.ACCOUNT_COL, conds);
        if (sameNickName != null) {
            return false;
        }
        Document doc = new Document("nickname", nickname);
        Document res = MongoUtil.updateOneById(COL, uid, doc);
        if (res == null) {
            return false;
        }

        return true;
    }

    public static boolean setCommonRight(String email, int right) {
        return setAccountRight(email, "common", right);
    }

    /**
     * 编辑试用账户
     *
     * @param userId   用户id
     * @param paramMap 参数　包括username, password
     *                 company, note
     * @param plan     方案
     * @return
     */
    public static boolean editTrailAccount(
            String adminId, String userId, Map<String, Object> paramMap,
            int plan, int termLong, String termUnit, List<String> pids, String lang
    ) {

        try {
            Document trailUserDoc = new Document();
            paramMap.forEach(trailUserDoc::append);
            List<Bson> conds = new ArrayList<>(Arrays.asList(
                    Filters.eq("_id", new ObjectId(userId)),
                    Filters.eq("trail", true)
            ));
            if (!MongoUtil.upsertOne(MongoUtil.ACCOUNT_COL, conds, trailUserDoc)) {
                logger.error("edit user information failed");
            }
            if (plan >= 0 && termLong >= 0 && pids != null) {
                Document document = ProductUtil.editUserProduct(
                        userId, pids,
                        plan,
                        termLong, termUnit, lang, adminId);
                if (document.containsKey("error")) {
                    logger.error(document.get("error").toString());
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }


    /**
     * 创建试用账户
     *
     * @return
     */
    public static Document createTrailAccount() {
        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.regex("email", "vip_\\d{3}@abcft.com"));
        List<String> trailEmails = MongoUtil
                .getDBList(MongoUtil.ACCOUNT_COL, conds)
                .stream()
                .map(document -> document.getString("email"))
                .collect(Collectors.toList());
//        logger.info("trail emails: " + Arrays.toString(trailEmails.toArray()));
        if (trailEmails.size() >= 1000) {
            logger.error("trail email number is exhaustion");
            return null;
        }
        try {
            String email = null;
            int count = 0;
            while (count++ < 1000) {
                email = genRandomEmail();
                if (!trailEmails.contains(email))
                    break;
            }
            if (email == null || email.isEmpty()) {
                return null;
            }
            String username = email.replaceAll("@.*", "");
            String rawPass = RandomStringUtils.randomNumeric(8).toUpperCase();
            //密码提示：密码前两位 + **** + 密码后两位
            String pwdEnds = rawPass.substring(0, 2) + "****" + rawPass.substring(6, 8);
            String password = DigestUtils.md5Hex(rawPass);

            Document trailDoc = new Document()
                    .append("email", email)
                    .append("password", password)
                    .append("pwd_ends", pwdEnds)
                    .append("username", username)
                    .append("head_img", "/images/icon_default_avatar.png")
                    .append("role", ROLE_USER)
                    .append("status", UserStatus.Registered.ordinal())
                    .append("plan", 0)
                    .append("trail", true)
                    .append("email_verify", true);

            Document doc = MongoUtil.insertOne(MongoUtil.ACCOUNT_COL, trailDoc);
            if (doc != null) {
                return new Document("id", doc.get("_id").toString())
                        .append("email", email)
                        .append("password", rawPass)
                        .append("username", username)
                        .append("company", "未填写")
                        .append("department", "未填写")
                        .append("job_title", "未填写")
                        .append("telephone", "未填写")
                        ;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * 生成　vip_[101, 999]@abcft.com 邮箱
     *
     * @return
     */
    public static String genRandomEmail() {
        String head = "vip_";
        String tail = "@abcft.com";
        String num = RandomStringUtils.randomNumeric(3, 4);
        return head + num + tail;
    }

    /**
     * * 获取用户详情
     * 普通用户与管理员可获得的信息有区别
     * pwd_ends: [密码前两位]****[密码后两位]
     * pwd_ends字段需要在创建用户的时候添加，
     * 修改用户密码时级联更新
     *
     * @param id 要查看的用户id
     * @return
     */
    public static Document getUserDetail(String id) {
        Document userDoc = MongoUtil.json2Document(getUserById(id));
        //管理员可获取全部用户信息
//        System.out.println(userDoc);
//        logger.info("user:<" + id + ">" + userDoc);
        if (userDoc == null) {
            return null;
        }
        String openId = userDoc.get("openid") == null ? "" : userDoc.getString("openid");
        Document data = new Document()
                .append("id", userDoc.getOrDefault("_id", "").toString())
                .append("email", userDoc.getOrDefault("email", ""))
                .append("username", userDoc.getOrDefault("username", userDoc.get("email")))
                .append("head_img", "") //占位勿删
                .append("nickname", "") //占位
                .append("pwd_ends", userDoc.getOrDefault("pwd_ends", "********"))

                .append("openid", userDoc.getOrDefault("openid", "未绑定"))
                .append("gender", getExtraAccountField(userDoc, "gender"))
                .append("i_f", getExtraAccountField(userDoc, "i_f"))
                .append("i_m", getExtraAccountField(userDoc, "i_m"))
                .append("i_s", getExtraAccountField(userDoc, "i_s"))
                .append("asset_size", getExtraAccountField(userDoc, "asset_size"))
                .append("company_full", userDoc.getOrDefault("company_full", "未填写"))

                .append("company", userDoc.getOrDefault("company", "未填写"))
                .append("department", userDoc.getOrDefault("department", "未填写"))
                .append("job_title", userDoc.getOrDefault("job_title", "未填写"))
                .append("telephone", userDoc.getOrDefault("telephone", "未填写"))
                .append("trail", userDoc.getOrDefault("trail", false))
                .append("paid", userDoc.getOrDefault("paid", false))
                .append("deleted", userDoc.getOrDefault("deleted", false))
                .append("email_verify", userDoc.getOrDefault("email_verify", false))
                .append("create_at", TimeUtil.date2String(
                        userDoc.getOrDefault("create_at", ""), "yyy-MM-dd HH:mm:ss.SSS"
                ))
                .append("update_at", TimeUtil.date2String(
                        userDoc.getOrDefault("update_at", ""), "yyy-MM-dd HH:mm:ss.SSS"
                ))
                .append("login_at", TimeUtil.date2String(
                        userDoc.getOrDefault("login_at", ""), "yyy-MM-dd HH:mm:ss.SSS"
                ));

        Document wxDoc = MongoUtil.getOneByField(MongoUtil.WX_USER_COL, "openid", openId);
        if (wxDoc != null) {
            data.put("head_img", wxDoc.getOrDefault("head_img", "/images/icon_default_avatar.png"));
            data.put("nickname", wxDoc.containsKey("nickname")
                    ? getNicknameById(id) : "未填写");
        } else {
            data.put("head_img", userDoc.getOrDefault("head_img", "/images/icon_default_avatar.png"));
            data.put("nickname", userDoc.containsKey("nickname")
                    ? getNicknameById(id) : "未填写");
        }
        return data;
    }

    /**
     * 返回用户字段列表，修改过的放在列表首位
     *
     * @param userDoc
     * @param field
     * @return
     */
    @SuppressWarnings("unchecked")
    private static List<Document> getExtraAccountField(Document userDoc, String field) {
        Object ob = userDoc.get(field);
        Document config = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "account_info");
        if (config == null) {
//            return Collections.singletonList(new Document("id", -1).append("name", "未填写"));
            config = new Document("name", "account_info")
                    .append("i_f", new ArrayList<>(Arrays.asList(
                            new Document("id", 1).append("name", "传媒"),
                            new Document("id", 2).append("name", "医药生物"),
                            new Document("id", 3).append("name", "汽车"),
                            new Document("id", 4).append("name", "家用电器"),
                            new Document("id", 5).append("name", "有色金属"),
                            new Document("id", 6).append("name", "化工"),
                            new Document("id", 7).append("name", "计算机"),
                            new Document("id", 8).append("name", "电子"),
                            new Document("id", 9).append("name", "电气设备"),
                            new Document("id", 10).append("name", "机械设备"),
                            new Document("id", 11).append("name", "采掘"),
                            new Document("id", 12).append("name", "商业贸易"),
                            new Document("id", 13).append("name", "建筑装饰"),
                            new Document("id", 14).append("name", "纺织服装"),
                            new Document("id", 15).append("name", "食品饮料"),
                            new Document("id", 16).append("name", "公共事业"),
                            new Document("id", 17).append("name", "轻工制造"),
                            new Document("id", 18).append("name", "交通运输"),
                            new Document("id", 19).append("name", "钢铁"),
                            new Document("id", 20).append("name", "休闲服务"),
                            new Document("id", 21).append("name", "通信"),
                            new Document("id", 22).append("name", "建筑材料"),
                            new Document("id", 23).append("name", "农林牧渔"),
                            new Document("id", 24).append("name", "银行"),
                            new Document("id", 25).append("name", "非银金融"),
                            new Document("id", 26).append("name", "综合"),
                            new Document("id", 27).append("name", "国防军工"),
                            new Document("id", 28).append("name", "房地产")
                    )))
                    .append("i_m", new ArrayList<>(Arrays.asList(
                            new Document("id", 1).append("name", "A股"),
                            new Document("id", 2).append("name", "港股"),
                            new Document("id", 3).append("name", "美股"),
                            new Document("id", 4).append("name", "多市场")
                    )))
                    .append("asset_size", new ArrayList<>(Arrays.asList(
                            new Document("id", 1).append("name", "100亿以下"),
                            new Document("id", 2).append("name", "100亿-500亿"),
                            new Document("id", 3).append("name", "500亿-1000亿"),
                            new Document("id", 4).append("name", "1000亿以上")))
                    )
                    .append("i_s", new ArrayList<>(Arrays.asList(
                            new Document("id", 1).append("name", "蓝筹股"),
                            new Document("id", 2).append("name", "成长股"),
                            new Document("id", 3).append("name", "大盘股"),
                            new Document("id", 4).append("name", "小盘股"),
                            new Document("id", 5).append("name", "混合"))))
                    .append("gender", new ArrayList<>(Arrays.asList(
                            new Document("id", 1).append("name", "男"),
                            new Document("id", 2).append("name", "女")

                    )));
        }
        LinkedList<Document> matchList = new LinkedList<>(config.get(field, List.class));
        if (ob == null) {
            matchList.addFirst(new Document("id", -1).append("name", "未填写"));
            return matchList;
        }
        int fieldValue = Integer.parseInt(String.valueOf(ob));

        Predicate<? super Document> filter = document ->
                Integer.parseInt(String.valueOf(document.get("id"))) == fieldValue;

        Optional<Document> first = matchList.stream().filter(filter).findFirst();

//        logger.info(Arrays.toString(matchList.toArray()));

        matchList.remove(first.orElse(new Document()));
        matchList.addFirst(first.orElse(new Document("id", -1).append("name", "未填写")));

        return matchList;
    }

    public static String getUsernameByEmail(String email) {
        Document document = MongoUtil.getOneByField(MongoUtil.ACCOUNT_COL, "email", email);
        return document.containsKey("username")
                ? document.get("username").toString()
                : email.replaceFirst("@.*", "");
    }

    public static Document getUserDocById(String userId) {
        Document document = MongoUtil.getOneById(MongoUtil.ACCOUNT_COL, userId);
        return document;
    }

    public static Document bindEmail(String email, String name, String company, String password, String pwdEnds) {
        Document userDoc = getUserDocByEmail(email);
        userDoc.remove("_id");
        if (!email.isEmpty()) {
            userDoc.put("email", email);
        }
        if (!name.isEmpty()) {
            userDoc.put("username", name);
        }
        if (!company.isEmpty()) {
            userDoc.put("company", company);
        }
        if (!password.isEmpty()) {

            userDoc.put("password", password);
        }
        if (!pwdEnds.isEmpty()) {

            userDoc.put("pwd_ends", pwdEnds);
        }
        userDoc.put("email_verify", true);
//        logger.info("userDOc: " + userDoc);
        Document res = MongoUtil.updateOneByField(MongoUtil.ACCOUNT_COL, "email", email, userDoc);
//        logger.info("res: " + res);
        return res;
    }

    public static boolean isNotSuperAdmin(String cuid) {
        return !isSuperAdmin(cuid);
    }

    public static Document deleteOneByEmail(String email, String ac, String wx) {
        if (email.isEmpty()) {
            return null;
        }
        Document userDoc1 = getUserDocByEmail(email);
        String openid = String.valueOf(userDoc1.getOrDefault("openid", ""));
        String suffix = String.valueOf(System.currentTimeMillis());
        Document updateDoc = new Document()
                .append("email", email + suffix)
                .append("openid", openid + suffix)
                .append("deleted", true);   //可通过deleted查找已删除账号

        if (!ac.isEmpty() && ac.equalsIgnoreCase("yes")) {
            if (MongoUtil.updateOneByField(MongoUtil.ACCOUNT_COL, "email", email, updateDoc) == null) {
                logger.error("delete failed from ACCOUNT_COL");
            }
        }
        if (!wx.isEmpty() && wx.equalsIgnoreCase("yes")) {
            if (MongoUtil.updateOneByField(MongoUtil.WX_USER_COL, "email", email, updateDoc) == null) {
                logger.error("delete failed from WX_USER_COL");
            }
        }
        Document userDoc = getUserDocByEmail(email);
        Document wxUserDoc = MongoUtil.getOneByField(MongoUtil.WX_USER_COL, "email", email);
        Document res = new Document();
        if (userDoc != null) {
            res.append("account", userDoc);
        }
        if (wxUserDoc != null) {
            res.append("wechat", wxUserDoc);
        }
        return res;
    }

    public static boolean isNotUserExists(String email) {
        return !isUserExists(email);
    }

    public static boolean isUserExists(String email) {
        List<Bson> conds = Arrays.asList(Filters.eq("email", email));
        return MongoUtil.getOneByConditions(MongoUtil.ACCOUNT_COL, conds) != null;
    }

    public static boolean isNotTrailUser(String userId) {
        return !isTrailUser(userId);
    }

    public static boolean isTrailUser(String userId) {
        try {
            return MongoUtil
                    .getOneById(MongoUtil.ACCOUNT_COL, userId)
                    .getBoolean("trail");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public static boolean editTrailAccount(String adminId, String id, Map<String, Object> paramMap) {
        return editTrailAccount(adminId, id, paramMap, -1, -1, "year", null, "");
    }

    public static Document loginByAdmin(String email, String pass) {
        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("email", email));
        conds.add(Filters.eq("password", pass));
        conds.add(ne("deleted", true));
        conds.add(Filters.or(
                Filters.eq(Authority.SUPER_ADMIN.name, true),
                Filters.eq(Authority.ANALYST.name, true),
                Filters.eq(Authority.SALE.name, true)
        ));
        Document userDoc = MongoUtil.getOneByConditions(MongoUtil.ACCOUNT_COL, conds);
        return userDoc;
    }

    public static Document loginByEmail(String email, String password) throws NotAuthorizedException {
//        return checkUserByEmail(email, password);
        List<Bson> conditions = Arrays.asList(
                eq("email", email),
                eq("password", password),
                eq("status", UserStatus.Registered.ordinal()),
                ne("deleted", true)
        );

        Document document = MongoUtil.getOneByConditions(MongoUtil.ACCOUNT_COL, conditions);
        logger.info(String.format("[DB_ACCESS] account: <%s> access MongoDB, password: <%s>", email, password));
        return document;
    }

    /**
     * 帐号是否过期
     */
    public static boolean isEmailExpired(String email) {
        List<Bson> conditions = new ArrayList<>(Arrays.asList(
                eq("email", email)
//                lt("expiry", new Date())
        ));

        Document document = MongoUtil.getOneByConditions(MongoUtil.ACCOUNT_COL, conditions);
        if (document == null) {
            return true;
        }
        String userId = document.get("_id").toString();
        Date expiry;
        Date now = new Date();
        try {
            expiry = (Date) document.get("expiry");
        } catch (Exception e) {
            expiry = null;
        }
        if (expiry == null || expiry.after(now)) {
            return false;
        }
        int dayCount = getAccountRemainingDayCount(userId);
//        logger.error("dayCount: " + dayCount);
        if (dayCount > 0) {
            LocalDateTime localDateTime = LocalDateTime.from(expiry.toInstant().atZone(ZoneId.systemDefault()));
            localDateTime = localDateTime.plusDays(dayCount);
            Date newExpiry = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            setExpiry(email, newExpiry);
            return false;
        }
        return true;
    }

    private static int getAccountRemainingDayCount(String userId) {
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                eq("user_id", userId),
                gte("stop_time", new Date())
        ));
        Optional<Document> stopTime = MongoUtil.getDBList(MongoUtil.USER_PRODUCT_COL, conds)
                .stream().max(Comparator.comparing(document -> document.getDate("stop_time")));
        Document stopTime1 = stopTime.orElseGet(() -> new Document("stop_time", new Date()));
        Date stopDate = stopTime1.getDate("stop_time");
        return Math.toIntExact(TimeUtil.getDaysCount(new Date(), stopDate));
    }

    public static JsonObject getUserById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return getUserByKey("_id", new ObjectId(id));
    }

    /**
     * 创建非注册账号
     *
     * @param userDoc
     * @return
     */
    public static Document createUnRegisterAccount(Document userDoc) {
        if (!userDoc.containsKey("email")) {
            logger.error("user doc don't contain email: ");
            return null;
        }
        String email = userDoc.getString("email");
        Document document = getUserDocByEmail(email);
        if (document != null) {
            return document;
        } else {
            userDoc.put("status", AccountUtil.UserStatus.UnRegister.ordinal());
            Document account = MongoUtil.insertOne(AccountUtil.COL, userDoc);
            return account;
        }
    }

    public static JsonObject getUserByKey(String key, Object value) {
        try {
            MongoCollection<Document> collection = MongoUtil.getCollection(MongoUtil.ACCOUNT_COL);
            Document doc = collection.find(eq(key, value)).first();

            JsonObject docJson = MongoUtil.document2Json(doc);

            return docJson;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public static JsonObject checkUserByEmail(String email, String password) {
        return checkUserByItem("email", email, password);
    }

    public static JsonObject checkUserPassword(String userId, String password) {
        return checkUserByItem("_id", new ObjectId(userId), password);
    }

    public static JsonObject checkUserByItem(String key, Object value, String password) {
        try {
            List<Bson> conditions = new ArrayList<>();
            conditions.add(eq(key, value));
            conditions.add(eq("password", password));
            Document doc = MongoUtil.getOneByConditions(MongoUtil.ACCOUNT_COL, conditions);
            if (doc == null) {
                logger.error("check user by item failed: " + key + " " + value.toString());
                return null;
            }

            JsonObject docJson = MongoUtil.document2Json(doc);
            return docJson;
        } catch (Exception e) {
            //e.printStackTrace();
            logger.info(e.getMessage());
        }

        return null;
    }

    public static boolean updateUserInfo(String id, String key, Object value) {
        try {
            JsonObject userJson = getUserById(id);
            if (userJson == null) {
                return false;
            }
            Document doc = MongoUtil.json2Document(userJson);
            doc.put(key, value);

            Document doc2 = MongoUtil.updateOneById(MongoUtil.ACCOUNT_COL, id, doc);
            if (doc2 != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public static boolean isSuperAdmin(String uid) {
        if (StringUtils.isEmpty(uid)) {
            return false;
        }
        List<Bson> conditions = Arrays.asList(
                eq("_id", new ObjectId(uid)),
                eq(Authority.SUPER_ADMIN.name, true)
        );
        Document document = MongoUtil.getOneByConditions(MongoUtil.ACCOUNT_COL, conditions);

        return (document != null && !document.isEmpty());
    }

    public static boolean resetUserPassword(String id, String password) {
        try {
            JsonObject userJson = getUserById(id);
            if (userJson == null) {
                return false;
            }
            Document doc = MongoUtil.json2Document(userJson);
            doc.put("password", password);
            doc.put("email_verify", true);

            Document doc2 = MongoUtil.updateOneById(MongoUtil.ACCOUNT_COL, id, doc);
            if (doc2 != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    public static boolean updateUserVerify(String userId, boolean verify) {
        return updateUserInfo(userId, "email_verify", verify);
    }

    /* 移除退出的token */
    public static boolean dropUserToken(String id, String token) {
        try {
            MongoCollection<Document> collection = MongoUtil.getCollection(MongoUtil.ACCOUNT_COL);

            Document document = new Document();
            document.append("$pull", new Document("token", new Document("auth_token", token)));

            collection.updateOne(eq("_id", new ObjectId(id)), document);

            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return false;
    }

    /**
     * 更新token
     *
     * @param id
     * @param token
     * @return
     */
    public static boolean updateUserToken(String id, Document token) {
        List<Document> tokens = Arrays.asList(
                token
        );
        Document doc2 = MongoUtil.updateOneById(MongoUtil.ACCOUNT_COL, id, new Document("token", tokens));

        return (doc2 != null);
    }

    /**
     * 检查token是否有效
     *
     * @param userId
     * @param token
     * @return
     */
    public static boolean checkUserToken(String userId, String token) {
        BasicDBObject query = new BasicDBObject("_id", new ObjectId(userId));
        query.append("token.auth_token", token);

//        MongoCollection collection = MongoUtil.getCollection(MongoUtil.ACCOUNT_COL);
        Object document = MongoUtil.getCollection(MongoUtil.ACCOUNT_COL).find(query).first();
        return (document != null);
    }

    /**
     * 用户是否注册
     *
     * @return
     */
    public static boolean isEmailRegistered(String email) {

        List<Bson> conditions = Arrays.asList(
                eq("email", email),
//                eq("status", UserStatus.Registered.ordinal()),
                eq("email_verify", true)    //邮箱验证
        );

        Document document = MongoUtil.getOneByConditions(MongoUtil.ACCOUNT_COL, conditions);

        return (document != null);
    }

    /**
     * @param id
     * @return
     */
    public static String getNicknameById(String id) {
        Document userDoc = MongoUtil.getOneById(MongoUtil.ACCOUNT_COL, id);
        if (userDoc != null && userDoc.containsKey("nickname")) {      //若用户以设置昵称
            return userDoc.getString("nickname");
        }

        return "";      //否则返回空
    }

    /**
     * @param id
     * @return
     */
    public static String getUsernameById(String id) {
        Document userDoc = MongoUtil.getOneById(MongoUtil.ACCOUNT_COL, id);
        if (userDoc != null && userDoc.containsKey("username")) {
            return userDoc.getString("username");
        }

        return "";
    }

    public static String getEmailById(String id) {
        JsonObject doc = getUserById(id);
        if (doc == null || !doc.containsKey("email")) {
            return null;
        }

        return doc.getString("email");
    }

    public static Document getUserDocByEmail(String email) {
        return MongoUtil.getOneByField(MongoUtil.ACCOUNT_COL, "email", email);
    }

    public static JsonObject getUserByEmail(String email) {
        return MongoUtil.getJsonByField(MongoUtil.ACCOUNT_COL, "email", email);
    }

    public static boolean reset(String uid) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("user_id", uid)
        );
        Document updateDoc =
                new Document("$set",
                        new Document("stop_time", TimeUtil.getCurDate()));
//        MongoUtil.getCollection(MongoUtil.ORDER_COL)
//                .updateMany(Filters.and(conditions), updateDoc);
//        MongoUtil.getCollection(MongoUtil.USER_PRODUCT_COL)
//                .updateMany(Filters.and(conditions), updateDoc);
        MongoUtil.updateMany(MongoUtil.ORDER_COL, conditions, updateDoc);//订单
        MongoUtil.deleteMany(MongoUtil.USER_PRODUCT_COL, conditions);   //产品
        MongoUtil.deleteMany(MongoUtil.USER_REPORT_COL, conditions);    //日报
        MongoUtil.getCollection(MongoUtil.DASHBOARD_COL)                //看板
                .deleteMany(Filters.eq("creator_id", uid));

        String pattern = "user_product_" + uid + "*";
        String pattern2 = "expiry_status_" + uid + "*";
//        logger.info("pattern1: " + pattern);
        RedisUtil.del(pattern, pattern2);
        return true;
    }

    /**
     * 设置用户的Marker权限
     *
     * @param email
     * @param marker
     * @return
     */
    public static boolean setMarkerRight(String email, int marker) {
        return setAccountRight(email, "marker", marker);
    }

    /**
     * 设置用户的Marker权限
     *
     * @param email
     * @param marker
     * @return
     */
    public static boolean setDespRight(String email, int marker) {
        return setAccountRight(email, "desp", marker);
    }

    /**
     * 设置用户的Marker权限
     *
     * @param email
     * @param marker
     * @return
     */
    public static boolean setExportRight(String email, int marker) {
        return setAccountRight(email, "export", marker);
    }

    /**
     * 设置用户的权限
     *
     * @param email
     * @return
     */
    public static boolean setAccountRight(String email, String name, int value) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("email", email)
        );
        Document doc = MongoUtil.updateOne(MongoUtil.ACCOUNT_COL, conditions, new Document("right." + name, value));
        return doc != null && !doc.isEmpty();
    }

    /*
     * 创建帐号
     */
    public static Document createUserDoc(String email, String password) {
        Document docUser = createUserDoc(email, password, true);

        return docUser;
    }

    /*
     * 创建帐号
     */
    public static Document createUserDoc(String email, String password, int expiry_days) {
        Date expiryDate = null;
        if (expiry_days >= 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, expiry_days);
            expiryDate = cal.getTime();
        }
        Document docUser = createUserDoc(email, password, expiryDate);

        return docUser;
    }

    public static Document createUserDoc(String email, String password, Date expiry) {
        return createAccountDoc(email, password, email, ROLE_USER, true, expiry);
    }

    public static Document createUserDoc(String email, String password, boolean verify) {
        return createAccountDoc(email, password, email, ROLE_USER, verify, null);
    }

    public static Document createAccountDoc(
            String email, String password, String username,
            String role, boolean verify, Date expiry) {
        return createAccountDoc(email, password, username, role, verify, expiry, Authority.NULL.name(), "");
    }

    public static Document createAccountDoc(
            String email, String password,
            String username, String role,
            boolean verify, Date expiry,
            String authority, String telephone) {
        Document doc = new Document()
                .append("email", email)
                .append("username", username)
                .append("head_img", "/images/icon_default_avatar.png")
                .append("role", role)
                .append("status", UserStatus.Registered.ordinal())
                .append("password", password)
                .append("create_at", TimeUtil.getCurDateTime())
                .append("update_at", TimeUtil.getCurDateTime())
                .append("email_verify", true);
        authority = authority.toLowerCase();
        if (isLegalAuthority(authority)) {      //
            doc.append(authority, true);
        }
        if (Authority.SALE.equals(Authority.valueOf(authority.toUpperCase())) && !telephone.isEmpty()) {
            logger.info("sale---------------------");
            doc.append("telephone", telephone);
        }

        if (expiry != null) {
            doc.append("expiry", expiry);
        }

        List<Bson> conditions = Arrays.asList(
                Filters.eq("email", email)
        );
        List<Document> userList = MongoUtil.getDBList(MongoUtil.ACCOUNT_COL, conditions);
        if (userList.size() > 1) {
            return new Document("error", "have more than one account")
                    .append("total", userList.size())
                    .append("list", userList);
        }
        MongoUtil.upsertOne(MongoUtil.ACCOUNT_COL, conditions, doc);

        Document userDoc = MongoUtil.getOneByField(
                MongoUtil.ACCOUNT_COL, "email", email);
        if (userDoc != null) {
            String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
            userDoc = new Document("email", userDoc.getOrDefault("email", null))
                    .append("password", null)
                    .append("username", userDoc.getOrDefault("username", null))
                    .append("create_at", TimeUtil.date2String(
                            userDoc.getOrDefault("create_at", ""), pattern))
                    .append("expiry", TimeUtil.date2String(
                            userDoc.getOrDefault("expiry", ""), pattern))
                    .append("id", userDoc.getOrDefault("_id", "").toString());
        }
        return userDoc;
    }

    public static boolean isLegalAuthority(String authority) {
        Authority auth;
        try {
            auth = Authority.valueOf(authority.toUpperCase());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        return !auth.equals(Authority.NULL) && !auth.equals(Authority.SUPER_ADMIN);
    }

    public static Document createUserDoc(String email, String password, int expiry_days, String authority, String telephone) {
        Date expiryDate = null;
        if (expiry_days >= 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, expiry_days);
            expiryDate = cal.getTime();
        }
        Document docUser = createAccountDoc(email, password, email, ROLE_USER, true, expiryDate, authority, telephone);

        return docUser;

    }

    public static boolean setExpiry(String email, Date expiryDate) {
        Document doc = new Document();
        if (expiryDate != null) {
            doc.append("expiry", expiryDate);
        }
        logger.info("expiry: " + expiryDate.toString());

        List<Bson> conditions = Arrays.asList(
                Filters.eq("email", email)
        );
        MongoUtil.updateOne(MongoUtil.ACCOUNT_COL, conditions, doc);

        return true;
    }

    public static boolean setExpiry(String email, int expiry_days) {
        Date expiryDate = null;
        if (expiry_days >= 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, expiry_days);
            expiryDate = cal.getTime();
        }
        return setExpiry(email, expiryDate);
    }

    public static boolean setExpiry(String email, String expiry_date) {
        Date expiryDate = null;
        try {
            expiryDate = TimeUtil.strToDate(expiry_date, "yyyy-MM-dd");
        } catch (ParseException e) {
            logger.warn("expiry");
            return false;
        }
        return setExpiry(email, expiryDate);
    }

    public static Document regist(Document userDoc) {
        try {
            String email = userDoc.getString("email");

            userDoc.append("status", UserStatus.Registered.ordinal())
                    .append("reg_date", TimeUtil.getCurDateTime())
                    .append("create_at", new Date());

            List<Bson> conditions = Arrays.asList(
                    Filters.eq("email", email)
//                    Filters.eq("status", UserStatus.UnRegister.ordinal())
            );

            MongoUtil.upsertOne(MongoUtil.ACCOUNT_COL, conditions, userDoc);

            return getUserDocByEmail(email);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return null;
    }

    public static String getIdByEmail(String email) {
        Document user = MongoUtil.getOneByField(MongoUtil.ACCOUNT_COL, "email", email);
        if (user != null) {
            return user.get("_id").toString();
        }

        return null;
    }

    public static boolean setUserRight(String uid, int right) {
        MongoUtil.updateOneById(MongoUtil.ACCOUNT_COL, uid, new Document("right", right));

        return true;
    }

    public static boolean updateAccountInfo(String id, JsonObject accountInfo) {

        MongoCollection<Document> col = MongoUtil.getDatabase().getCollection(MongoUtil.ACCOUNT_COL);

        Document doc = col.findOneAndUpdate(eq("_id", new ObjectId(id)), new Document("$set", MongoUtil.json2Document(accountInfo)));
        if (doc == null) {
            logger.error("update account info error: " + id);
            return false;
        }

        return true;
    }

    public static boolean updateLoginStatus(String id) {
        Document document = new Document("login_date", TimeUtil.getCurDateTime());
        Document doc = MongoUtil.updateOneById(MongoUtil.ACCOUNT_COL, id, document);
        if (doc == null) {
            logger.error("update account info error: " + id);
            return false;
        }

        return true;
    }

    /**
     * 获取账户列表：
     * 默认按照剩余天数排序
     *
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param status
     * @param query
     * @return
     */
    public static Document getAccounts(
            String adminId,
            String sort, String order, int offset, int limit,
            int status, String query, int trail, String lang,
            int deleted, int paid) {
        long s = System.currentTimeMillis();
        ArrayList<Document> accountList = new ArrayList<>();
        List<Bson> conditions = new ArrayList<>();
        Bson nePaid = ne("paid", true);

        moveVIPEmailTOTrial(); //为之前创建过的试用账号添加trail字段

        if (status != 2) {
            conditions.add(Filters.eq("status", status));
        }
        if (!StringUtils.isEmpty(query)) {
            List<Bson> orCond = Arrays.asList(
                    Filters.regex("email", query, "i"),
                    Filters.regex("nickname", query, "i"),
                    Filters.regex("username", query, "i"),
                    Filters.regex("company", query, "i")
            );
            conditions.add(Filters.or(orCond));
        }
        if (1 == trail) {           //返回试用账号
            conditions.add(Filters.eq("trail", true));
            conditions.add(nePaid);
        } else if (0 == trail) {    //返回正式账号
            conditions.add(ne("trail", true));
        }
        if (deleted == 0) {     //返回正常账号
            conditions.add(ne("deleted", true));
        } else if (deleted == 1) { //返回已删除账号
            conditions.add(Filters.eq("deleted", true));
        }
        if (paid == 0) {     //返回免费非试用账号
            conditions.add(nePaid);
            conditions.add(ne("trail", true));
        } else if (paid == 1) { //返回付费账号
            conditions.add(Filters.eq("paid", true));
        }
        Document adminDoc = getUserDocById(adminId);
        //销售人员只能查看自己关联的免费邀请码账号
        if ((boolean) adminDoc.getOrDefault(Authority.SALE.name, false)) {
            //TODO: invite_code not sure
            conditions.add(Filters.eq("invite_code",
                    adminDoc.getOrDefault("telephone", System.currentTimeMillis())));
            conditions.remove(nePaid);
//            conditions.add(nePaid);
//            logger.info(Arrays.toString(conditions.toArray()));
        }
        //分析师只能查看编辑非管理员账号
        if ((boolean) adminDoc.getOrDefault(Authority.ANALYST.name, false)) {
            conditions.add(Filters.exists(Authority.SUPER_ADMIN.name, false));
//            conditions.add(Filters.exists(Authority.ANALYST.name, false));
//            conditions.add(Filters.exists(Authority.SALE.name, false));
        }

        List<Document> userList = MongoUtil.getDBList(MongoUtil.ACCOUNT_COL, conditions, sort, order, offset, limit);
        List<String> userIds = userList.stream()
                .map(document -> String.valueOf(document.getOrDefault("id", "")))
                .collect(Collectors.toList());
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                Filters.in("user_id", userIds)
        ));
        List<Document> upList = MongoUtil.getDBList(MongoUtil.USER_PRODUCT_COL, conds);
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        userList.forEach(accountDoc -> {
            String userId = String.valueOf(accountDoc.getOrDefault(
                    "id", accountDoc.getOrDefault("_id", "")));
//            List<String> bnames = ProductUtil.getUserProductNames(userId);
//            List<String> freeProductNames = ProductUtil.getFreeProductsNames();
//            freeProductNames.removeAll(bnames);
//            bnames.addAll(freeProductNames.stream().map(s1 -> s1 + " [Free]").collect(Collectors.toList()));
//            bnames = new ArrayList<>(new HashSet<>(bnames));
            Document account = new Document("id", userId)
                    .append("email", accountDoc.getOrDefault("email", "未填写"))
                    .append("username", accountDoc.getOrDefault("username", "未填写"))
//                    .append("board_total", bnames.size())
//                    .append("board_names", bnames)
                    .append("status", getAccountStatus(accountDoc, lang))
                    .append("wx_status", getAccountWechatStatus(accountDoc, lang))
                    .append("remaining", getAccountRemaining(accountDoc, upList, lang))
                    .append("telephone", accountDoc.getOrDefault("telephone", "未填写"))
                    .append("invite_code", accountDoc.getOrDefault("invite_code", "未填写"))
                    .append("company", accountDoc.getOrDefault("company", "未填写"))
                    .append("nickname", accountDoc.getOrDefault("nickname", "未填写"))
                    .append("note", accountDoc.getOrDefault("note", "未填写"))
                    .append("admin_id", accountDoc.getOrDefault("admin_id", "未填写"))
                    .append("create_at", TimeUtil.date2String(accountDoc.getOrDefault("create_at", ""), pattern))
                    .append("update_at", TimeUtil.date2String(accountDoc.getOrDefault("update_at", ""), pattern))
                    .append("expiry", TimeUtil.date2String(accountDoc.getOrDefault("expiry", ""), pattern))
                    .append("login_date", TimeUtil.date2String(accountDoc.getOrDefault("login_date", ""), pattern))
                    .append("trail", accountDoc.getOrDefault("trail", false))
                    .append("paid", accountDoc.getOrDefault("paid", false))
                    .append("deleted", accountDoc.getOrDefault("deleted", false))
                    .append("email_verify", accountDoc.getOrDefault("email_verify", false));
            accountList.add(account);
        });

        Document accounts = new Document()
                .append("total", accountList.size())
                .append("list", accountList);
        long e = System.currentTimeMillis();
//        logger.info("get_accounts_total_time" + (e - s) / 1000.0);

        return accounts;
    }

    private static boolean moveVIPEmailTOTrial() {
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                Filters.regex("email", "vip.*@abcft.com", "i"),
                ne("trail", true),
                ne("paid", true)
        ));
        Document updateDoc = new Document("trail", true);
        return MongoUtil.updateMany(MongoUtil.ACCOUNT_COL, conds, updateDoc);
    }

    public static String getAccountStatus(Document accountDoc, String lang) {
        Object status = accountDoc.getOrDefault("status", UserStatus.NULL.ordinal());
        if (status instanceof Integer) {
            return UserStatus.valueOf((Integer) status).getName(lang);
        } else {
            return UserStatus.NULL.getName(lang);
        }
    }

    public static String getAccountWechatStatus(Document accountDoc, String lang) {
        return accountDoc.containsKey("openid") ?
                accountDoc.containsKey("email_verify") ?
                        WeChatBindStatus.WECHAT_BINDED.getName(lang) :
                        WeChatBindStatus.WECHAT_BINDED_NOT_VERIFY.getName(lang) :
                WeChatBindStatus.WECHAT_NOT_BINDED.getName(lang);
    }

    /**
     * 获取账户剩余期限
     *
     * @param accountDoc
     * @return
     */
    public static String getAccountRemaining(Document accountDoc, List<Document> upList, String lang) {
        String userId = String.valueOf(
                accountDoc.getOrDefault("_id", accountDoc.getOrDefault("id", null)));

        boolean isCN = !lang.isEmpty() && lang.matches(".*(?i)zh_cn.*");
        if (userId == null) {
            return "未知";
        }
//        List<Document> upList = ProductUtil.getUserProducts(userId, "");
        Predicate<? super Document> filter = document ->
                String.valueOf(document.getOrDefault("user_id", "")).equalsIgnoreCase(userId);

        upList = upList.stream()
                .filter(filter)
                .collect(Collectors.toList());

        long count;
        Date now = new Date();
        if (upList == null || upList.size() <= 0) {
//            logger.info("upList size < 0");
//            count = TimeUtil.getDaysCount(now, (Date) accountDoc.getOrDefault("expiry", now));
//            return accountDoc.containsKey("expiry") ?
//                    count > 0 ?
//                            count + (isCN ? " 天" : " Days") :
//                            (isCN ? "已过期" : "Expire") :
//                    (isCN ? "无" : "Limitless");
            return "无已开通看板";
        } else {
            Date stopTimeMax = now;
//            Optional<Document> stopTimeMaxDoc = upList.stream()
//                    .sorted(Comparator.comparing(document -> document.getDate("stop_time"),
//                            Comparator.reverseOrder()))
//                    .findFirst();
            Optional<Document> stopTimeMaxDoc = upList.stream()
                    .max(Comparator.comparing(document -> document.getDate("stop_time")));

            Document document = stopTimeMaxDoc.orElse(new Document());
            stopTimeMax = (Date) document.getOrDefault("stop_time", now);
            count = TimeUtil.getDaysCount(now, stopTimeMax);
            return count > 0 ?
                    count + (isCN ? " 天" : " Days") :
                    (isCN ? "已过期" : "Expire");
        }
    }

    /**
     * 根据openid 获取用户
     *
     * @param openId
     * @return
     */
    public static Document getUserByOpenId(String openId) {
        return MongoUtil.getOneByField(MongoUtil.ACCOUNT_COL, "openid", openId);
    }

    public static Document getManagers(
            String query, String auth,
            String sort, String order,
            int offset, int limit,
            String type, String lang
    ) {

        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.or(
                Filters.eq(Authority.SUPER_ADMIN.name, true),
                Filters.eq(Authority.ANALYST.name, true),
                Filters.eq(Authority.SALE.name, true)
        ));
        if (!type.equalsIgnoreCase("all")) {
            conds.add(Filters.eq(type, true));
        }
        if (!query.isEmpty()) {
            conds.add(Filters.or(
                    Filters.regex("email", query, "i"),
                    Filters.regex("nickname", query, "i"),
                    Filters.regex("username", query, "i"),
                    Filters.regex("company", query, "i")
            ));
        }
        if (isLegalAuthority(auth)) {
            conds.add(Filters.eq(auth, true));
        }
        List<Document> userList = MongoUtil.getDBList(MongoUtil.ACCOUNT_COL, conds, sort, order, offset, limit);
        List<String> userIds = userList.stream()
                .map(document -> String.valueOf(document.getOrDefault("id", "")))
                .collect(Collectors.toList());
        List<Bson> conds1 = new ArrayList<>(Arrays.asList(
                Filters.in("user_id", userIds)
        ));
        List<Document> upList = MongoUtil.getDBList(MongoUtil.USER_PRODUCT_COL, conds1);
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        List<Document> managers = MongoUtil.getDBList(
                MongoUtil.ACCOUNT_COL, conds, sort, order, offset, limit)
                .stream()
                .map(accountDoc -> new Document()
                        .append("email", accountDoc.getOrDefault("email", ""))
                        .append("authority", getAccountAuthority(accountDoc, lang))
                        .append("telephone", accountDoc.getOrDefault("telephone", ""))
                        .append("username", accountDoc.getOrDefault("username", "未填写"))
                        .append("status", getAccountStatus(accountDoc, lang))
                        .append("wx_status", getAccountWechatStatus(accountDoc, lang))
                        .append("remaining", getAccountRemaining(accountDoc, upList, lang))
                        .append("company", accountDoc.getOrDefault("company", "未填写"))
                        .append("nickname", accountDoc.getOrDefault("nickname", "未填写"))
                        .append("create_at", TimeUtil.date2String(accountDoc.getOrDefault("create_at", ""), pattern))
                        .append("update_at", TimeUtil.date2String(accountDoc.getOrDefault("update_at", ""), pattern))
                        .append("expiry", TimeUtil.date2String(accountDoc.getOrDefault("expiry", ""), pattern))
                        .append("login_date", TimeUtil.date2String(accountDoc.getOrDefault("login_date", ""), pattern))
                        .append("deleted", accountDoc.getOrDefault("deleted", false))

                )
                .collect(Collectors.toList());

        return new Document("total", managers.size())
                .append("list", managers)
                ;
    }

    private static Document getAccountAuthority(Document accountDoc, String lang) {
        boolean isSuperAdmin = (boolean) accountDoc.getOrDefault(Authority.SUPER_ADMIN.name, false);
        boolean isAnalyst = (boolean) accountDoc.getOrDefault(Authority.ANALYST.name, false);
        boolean isSale = (boolean) accountDoc.getOrDefault(Authority.SALE.name, false);
        String authString = isSuperAdmin ? "super_admin" : isAnalyst ? "analyst" : isSale ? "sale" : "none";
        Document ret = new Document("authority", authString)
                .append("chart_right", accountDoc.getOrDefault("right", "none"));
        if (isSale) {
            String telephone = String.valueOf(accountDoc
                    .getOrDefault("telephone", ""));
            ret.append("binding_user", getBindingUserOfSale(telephone));
        }
        return ret;
    }

    private static Document getBindingUserOfSale(String telephone) {
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                eq("invite_code", telephone),
                ne("deleted", true)
        ));
        Function<? super Document, Document> mapper = document ->
                new Document("id", document.get("id"))
                        .append("email", document.get("email"))
                        .append("trail", document.getOrDefault("trail", false))
                        .append("paid", document.getOrDefault("paid", false))
                        .append("deleted", document.getOrDefault("deleted", false));
        List<Document> list = MongoUtil.getDBList(MongoUtil.ACCOUNT_COL, conds)
                .stream().map(mapper).collect(Collectors.toList());
        return new Document("total", MongoUtil.getDBCount(MongoUtil.ACCOUNT_COL, conds))
                .append("list", list);
    }

    public static boolean isNotAnalyst(String userId) {
        return !isAnalyst(userId);
    }

    public static boolean isAnalyst(String userId) {
        Document userDoc = MongoUtil.getOneById(MongoUtil.ACCOUNT_COL, userId);
        return (boolean) userDoc.getOrDefault(Authority.SUPER_ADMIN.name, false) ||
                (boolean) userDoc.getOrDefault(Authority.ANALYST.name, false);
    }

    public static boolean isNotSale(String userId) {
        return !isSale(userId);
    }

    public static boolean isSale(String userId) {
        Document userDoc = MongoUtil.getOneById(MongoUtil.ACCOUNT_COL, userId);
        if (userDoc == null) return false;
        return (boolean) userDoc.getOrDefault(Authority.SUPER_ADMIN.name, false) ||
                (boolean) userDoc.getOrDefault(Authority.ANALYST.name, false) ||
                (boolean) userDoc.getOrDefault(Authority.SALE.name, false);
    }

    public static Document deleteOneById(String userId) {
        return deleteOneByEmail(getEmailById(userId), "yes", "yes");
    }

    /**
     * 获取
     *
     * @param userId
     * @return
     */
    public static int getUserAuthority(String userId) {
        Document userDoc = getUserDocById(userId);
        if (userDoc == null) {
            return -1;
        }
        if ((boolean) userDoc.getOrDefault(Authority.SUPER_ADMIN.name, false))
            return Authority.SUPER_ADMIN.id;
        if ((boolean) userDoc.getOrDefault(Authority.ANALYST.name, false))
            return Authority.ANALYST.id;
        if ((boolean) userDoc.getOrDefault(Authority.SALE.name, false))
            return Authority.SALE.id;
        return Authority.NULL.id;
    }

    public static Document markPaidAccount(String opt, String email) {
        Document setDoc = new Document("$set", new Document("paid", true));
        Document unsetDoc = new Document("$unset", new Document("paid", 1));
        switch (opt) {
            case "paid":        //设置付费用户
                return MongoUtil.updateOneByField(
                        MongoUtil.ACCOUNT_COL, "email", email, setDoc, false);
            case "unpaid":      //取消付费用户
                return MongoUtil.updateOneByField(
                        MongoUtil.ACCOUNT_COL, "email", email, unsetDoc, false);
            default:
                return null;
        }
    }

    /**
     * 判断是否付费账户
     *
     * @param userId
     * @return
     */
    public static boolean isPaidAccount(String userId) {
        Document userDoc = getUserDocById(userId);
        return (boolean) userDoc.getOrDefault("paid", false);
    }

    public static String getUserAuthorityName(String userId) {
        return Authority.valueOf(getUserAuthority(userId)).name;
    }

    public static boolean editUnpaidAccount(String adminId, String id, Map<String, Object> paramMap) {
        return editUnpaidAccount(adminId, id, paramMap, -1, -1, "year", null, "");
    }

    public static boolean editUnpaidAccount(
            String adminId, String userId, Map<String, Object> paramMap,
            int plan, int termLong, String termUnit, List<String> pids, String lang
    ) {

        try {
            Document trailUserDoc = new Document();
            paramMap.forEach(trailUserDoc::append);
            List<Bson> conds = new ArrayList<>(Arrays.asList(
                    Filters.eq("_id", new ObjectId(userId)),
                    ne("paid", true),
                    ne("trail", true)
            ));
            if (getUserAuthority(adminId) == Authority.SALE.id) {
                //TODO: invite_code not sure
                String inviteCode = String.valueOf(getUserDocById(adminId).getOrDefault("telephone",
                        System.currentTimeMillis()));
                conds.add(Filters.eq("invite_code", inviteCode));
            }
            if (!MongoUtil.upsertOne(MongoUtil.ACCOUNT_COL, conds, trailUserDoc)) {
                logger.error("edit user information failed");
            }
            if (plan >= 0 && termLong >= 0 && pids != null) {
                Document document = ProductUtil.editUserProduct(
                        userId, pids,
                        plan,
                        termLong, termUnit, lang, adminId);
                if (document.containsKey("error")) {
                    logger.error(document.get("error").toString());
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public static Document getAccountPredefinedInfo() {
        Document document = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "account_info");
        if (document == null) {
            return null;
        }
        return new Document("i_f", document.getOrDefault("i_f", Collections.EMPTY_LIST))
                .append("i_m", document.getOrDefault("i_m", Collections.EMPTY_LIST))
                .append("i_s", document.getOrDefault("i_s", Collections.EMPTY_LIST))
                .append("asset_size", document.getOrDefault("asset_size", Collections.EMPTY_LIST))
                .append("gender", document.getOrDefault("gender", Collections.EMPTY_LIST));
    }

    public static Document setUserAuthority(String userId, String auth, String telephone, String remove) {
        try {
            boolean isSale = isStringMatchAuthority(auth, Authority.SALE);
//            logger.info("isSale: " + (isSale ? "yes" : "no"));
            Document updateDoc = new Document(auth, true);
            if (remove.equalsIgnoreCase("yes")) {
                updateDoc.append(auth, false);
            }
            if (isSale && !telephone.isEmpty()) {   //销售加上电话字段作为对外邀请码
                updateDoc.append("telephone", telephone);
            }
            Document res = MongoUtil.updateOneById(
                    MongoUtil.ACCOUNT_COL, userId, updateDoc);
            return res;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    private static boolean isStringMatchAuthority(String authString, Authority authority) {
        return authString.equalsIgnoreCase(authority.name);
    }

    public static boolean isOnlySale(String cuid) {
        return (boolean) getUserDocById(cuid)
                .getOrDefault(Authority.SALE.name, false);
    }

    public static boolean isOnlyAnalyst(String cuid) {
        return (boolean) getUserDocById(cuid)
                .getOrDefault(Authority.ANALYST.name, false);
    }

    public static boolean isOnlySuperAdmin(String cuid) {
        return (boolean) getUserDocById(cuid)
                .getOrDefault(Authority.SUPER_ADMIN.name, false);
    }

    public static Document connectSaleToUser(String saleEmail, String userEmail, String inviteCode) {
        Document saleDoc = getUserDocByEmail(saleEmail);
        Document userDoc = getUserDocByEmail(userEmail);
        String saleId = String.valueOf(saleDoc.getOrDefault("_id", saleDoc.getOrDefault("id", "")));
        String userId = String.valueOf(userDoc.getOrDefault("_id", saleDoc.getOrDefault("id", "")));
        String tel = String.valueOf(saleDoc.getOrDefault("telephone", ""));
        if (tel.isEmpty()) {
            if (inviteCode.isEmpty()) {
                return new Document("error", "telephone and invite code cannot be both empty");
            }
            if (!saleId.isEmpty()) {
                updateUserInfo(saleId, "telephone", inviteCode);
                updateUserInfo(userId, "invite_code", inviteCode);
                return new Document("success", "connect success");
            }
        } else {
            if (!saleId.isEmpty()) {
                updateUserInfo(userId, "invite_code", tel);
                return new Document("success", "connect success");
            }
        }
        return new Document("error", "connect error");
    }

    public static boolean editPaidAccount(int adminAuthority, String userId, Map<String, Object> paramMap) {

        try {
            Document trailUserDoc = new Document();
            paramMap.forEach(trailUserDoc::append);
            List<Bson> conds = new ArrayList<>(Arrays.asList(
                    Filters.eq("_id", new ObjectId(userId)),
                    Filters.eq("paid", true)
            ));
            if (!MongoUtil.upsertOne(MongoUtil.ACCOUNT_COL, conds, trailUserDoc)) {
                logger.error("edit user information failed");
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * id代表权限等级，0为最高权限
     */
    public enum Authority {
        SUPER_ADMIN(0, "super_admin"),
        ANALYST(1, "analyst"),
        SALE(2, "sale"),
        NULL(3, "null"),
        ;

        public int id;
        public String name;

        Authority(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static Authority valueOf(int id) {
            for (Authority authority : values()) {
                if (authority.id == id)
                    return authority;
            }
            return NULL;
        }
    }

    public enum UserStatus {
        Registered("Registered", "已注册"),     //用户已注册
        UnRegister("UnRegister", "未注册"),     //用户尚未注册
        NULL("NULL", "未知"),
        ;

        private String name;
        private String name_zh_CN;


        UserStatus(String name, String name_zh_CN) {
            this.name = name;
            this.name_zh_CN = name_zh_CN;
        }

        public static UserStatus valueOf(Integer status) {
            for (UserStatus userStatus : values()) {
                if (userStatus.ordinal() == status) {
                    return userStatus;
                }
            }
            return NULL;
        }

        public String getName(String lang) {
            if (!lang.isEmpty() && lang.matches(".*(?i)zh_cn.*")) {
                return this.name_zh_CN;
            }
            return this.name;
        }
    }

    public enum WeChatBindStatus {
        WECHAT_BINDED("wechat binded", "微信已绑定"),
        WECHAT_BINDED_NOT_VERIFY("wechat binded, not verify", "邮箱未验证"),
        WECHAT_NOT_BINDED("wechat not binded", "微信未绑定"),
        NULL("NULL", "未知"),
        ;

        private String name, name_zh_CN;

        WeChatBindStatus(String name, String name_zh_CN) {
            this.name = name;
            this.name_zh_CN = name_zh_CN;
        }

        public String getName(String lang) {
            if (!lang.isEmpty() && lang.matches(".*(?i)zh_cn.*")) {
                return this.name_zh_CN;
            }
            return this.name;
        }
    }
}
