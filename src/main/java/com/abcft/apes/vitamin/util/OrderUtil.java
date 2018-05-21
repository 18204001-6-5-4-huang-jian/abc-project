package com.abcft.apes.vitamin.util;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.json.JsonObject;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Sorts.ascending;

/**
 * Created by zhyzhu on 17-4-23.
 */
@SuppressWarnings("deprecation")
public class OrderUtil {
    public static String COL = MongoUtil.ORDER_COL;
    private static Logger logger = Logger.getLogger(OrderUtil.class);

    public static Document createAndPayOrder(
            String email, String type, List<String> names, List<String> stockCodes,
            int plan, int yearLen, String unit, String lang, String adminId
    ) {
        List<String> pids;
        if (type.equalsIgnoreCase("all")) {
            pids = ProductUtil.getAllProductIds();
        } else {
            pids = ProductUtil.getProductIdsByNames(names);
            pids.addAll(ProductUtil.getProductIdsByStockCode(stockCodes));
            pids = new ArrayList<>(new HashSet<>(pids));
        }
        pids.removeIf(String::isEmpty);
        if (pids.isEmpty()) {
            return new Document("error", "product param error");
        }
        return createAndPayOrder(email, pids, plan, yearLen, unit, lang, adminId);
    }

    public static Document createAndPayOrder(
            String email, List<String> pids, int plan,
            int termLong, String termUnit,
            String lang, String adminId) {
        //创建订单
        Document orderData = OrderUtil.getOrderData(email, pids, plan, termLong, termUnit, lang);
        String orderId = createOrder2(orderData, lang);
        if (StringUtils.isEmpty(orderId) || !isOrderExists(orderId)) {
            logger.error("create order failed");
            return new Document("error", "create order failed: " + orderId);
        }
//        logger.info("oid:" + orderId);
        //支付订单
        boolean res = payOrder2(orderId, lang, false, adminId);
        if (!res) {
            logger.error("pay order<" + orderId + "> failed");
            return new Document("error", "pay order<" + orderId + "> failed");
        }
        Document orderDoc = MongoUtil.getOneById(MongoUtil.ORDER_COL, orderId);
        List<String> productsDocList = OrderUtil
                .getOrderProductNames(orderDoc)
                .stream()
                .sorted()
                .collect(Collectors.toList());
        return new Document()
                .append("order_id", orderId)
                .append("total", productsDocList.size())
                .append("list", productsDocList)
                ;
    }

    public static boolean isOrderExists(String orderId) {
        return MongoUtil.getOneById(MongoUtil.ORDER_COL, orderId) != null;
    }

    /**
     * 生成订单信息
     *
     * @return string order_id
     */
    @Deprecated
    public static String createOrder(Document orderData, String lang) throws Exception {
        if (!orderData.containsKey("plan_id") ||
                !orderData.containsKey("level_id") ||
                !orderData.containsKey("term_long") ||
                !orderData.containsKey("term_unit") ||
                !orderData.containsKey("products") ||
                !orderData.containsKey("account") ||
                !orderData.containsKey("payment") ||
                !orderData.containsKey("price_total")
                ) {
            return null;
        }

        Document userDoc = orderData.get("account", Document.class);
        if (!userDoc.containsKey("email")) {
            return null;
        }
        if (userDoc.containsKey("type")) {
            userDoc.remove("type");
        }
        String userEmail = userDoc.getString("email").toLowerCase();
        Document accountDoc = AccountUtil.getUserDocByEmail(userEmail);
        if (accountDoc == null) {
            accountDoc = AccountUtil.createUnRegisterAccount(userDoc);
            if (accountDoc == null) {
                logger.warn("create init user account failed: " + userDoc.toString());
                return null;
            }
        }
        String userId = accountDoc.get("_id").toString();
        String userName = accountDoc.getString("username");

        int termValue = orderData.getInteger("term_long");
        String termUnit = orderData.getString("term_unit");


        Date startTime = TimeUtil.getCurDate();
        Date stopTime = TimeUtil.getStopDate(startTime, termUnit, termValue);

        //创建新订单
        Document newOrder = new Document()
                .append("status", OrderStatus.UnPay.ordinal())
                .append("user_id", userId)
                .append("user_email", userEmail)
                .append("products", orderData.get("products"))
                .append("plan_id", orderData.get("plan_id"))
                .append("level_id", orderData.get("level_id"))
                .append("term_long", termValue)
                .append("term_unit", termUnit)
                .append("start_time", startTime)
                .append("stop_time", stopTime)
                .append("price_total", orderData.get("price_total"))
                .append("payment", orderData.get("payment"));
        if (orderData.containsKey("invite_code_id")) {
            newOrder.append("invite_code_id", orderData.get("invite_code_id"));
        }

        Document order = MongoUtil.insertOne(COL, newOrder);
        String orderId = order.get("_id").toString();

        boolean res = MailUtil.SendOrderCreatedEmail(userEmail, userName, orderId, lang);
        if (!res) {
            throw new Exception("邮件发送失败");
        }

        return orderId;
    }

    public static String createOrder2(Document orderData, String lang) {
        if (!orderData.containsKey("plan_id") ||
                !orderData.containsKey("level_id") ||
                !orderData.containsKey("term_long") ||
                !orderData.containsKey("term_unit") ||
                !orderData.containsKey("products") ||
                !orderData.containsKey("account") ||
                !orderData.containsKey("payment") ||
                !orderData.containsKey("price_total")
                ) {
            return "param error";
        }

        Document userDoc = orderData.get("account", Document.class);
        if (!userDoc.containsKey("email")) {
            return "user have not email";
        }
        if (userDoc.containsKey("type")) {
            userDoc.remove("type");
        }
        String userEmail = userDoc.getString("email").toLowerCase();
        Document accountDoc = AccountUtil.getUserDocByEmail(userEmail);
        if (accountDoc == null) {
            logger.warn("account not exsits: " + userDoc.toString());
            return "user not exists";
        }
        String userId = accountDoc.get("_id").toString();
        String userName = accountDoc.getString("username");

        int termValue = orderData.getInteger("term_long");
        String termUnit = orderData.getString("term_unit");

        termUnit = termUnit.replaceAll("years", "year")
                .replaceAll("months", "month")
                .replaceAll("weeks", "week")
                .replaceAll("days", "day");

        if (termValue < 0 || !termUnit.matches("years?|months?|days?")) {
            return "term_long or term_unit illegal";
        }

        Date startTime = TimeUtil.getCurDate();
        Date stopTime = TimeUtil.getStopDate(startTime, termUnit, termValue);

        //创建新订单
        Document newOrder = new Document()
                .append("status", OrderStatus2.Unpaid.ordinal())
                .append("user_id", userId)
                .append("user_email", userEmail)
                .append("products", orderData.get("products"))
                .append("plan_id", orderData.get("plan_id"))
                .append("level_id", orderData.get("level_id"))
                .append("term_long", termValue)
                .append("term_unit", termUnit)
                .append("start_time", startTime)
                .append("stop_time", stopTime)
                .append("price_total", orderData.get("price_total"))
                .append("payment", orderData.get("payment"));
        if (orderData.containsKey("invite_code_id")) {
            newOrder.append("invite_code_id", orderData.get("invite_code_id"));
        }

        Document order = MongoUtil.insertOne(COL, newOrder);
        String orderId = order.get("_id").toString();
//        boolean res = MailUtil.SendOrderCreatedEmail(userEmail, userName, orderId, lang);
//        if (!res) {
////            throw new Exception("邮件发送失败");
//            logger.error("send email failed");
//        }

        return orderId;
    }

    /**
     * @param orderId
     * @return
     */
    public static int getOrderStatus(String orderId) {
        Document document = MongoUtil.getOneById(COL, orderId);
        if (document == null) {
            return -1;
        }
        return document.getInteger("status");
    }

    /**
     * 取消订单
     *
     * @param orderId
     */
    @Deprecated
    public static boolean cancelOrder(String userId, String orderId) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("_id", new ObjectId(orderId)),
                Filters.eq("user_id", userId),
                Filters.eq("status", OrderStatus.UnPay.ordinal())
        );

        Document document = new Document();
        document.append("status", OrderStatus.Canceled.ordinal());

        Document newOrderDoc = MongoUtil.updateOne(COL, conditions, document);

        return (newOrderDoc != null && !newOrderDoc.isEmpty());
    }

    /**
     * 取消订单2
     *
     * @param orderId
     */
    public static boolean cancelOrder2(String userId, String orderId) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("_id", new ObjectId(orderId)),
                Filters.eq("user_id", userId),
                Filters.eq("status", OrderStatus2.Unpaid.ordinal())
        );

        Document document = new Document();
        document.append("status", OrderStatus2.Canceled.ordinal());

        Document newOrderDoc = MongoUtil.updateOne(COL, conditions, document);

        return (newOrderDoc != null && !newOrderDoc.isEmpty());
    }

    @Deprecated
    public static boolean isOrderUnpay(String orderId) {
        List<Bson> conditions1 = Arrays.asList(
                Filters.eq("_id", new ObjectId(orderId)),
                Filters.eq("status", OrderStatus.UnPay.ordinal())
        );
        Document orderDoc = MongoUtil.getOneByConditions(COL, conditions1);

        return (orderDoc != null && !orderDoc.isEmpty());
    }

    public static boolean isOrderUnpaid(String orderId) {
        List<Bson> conditions1 = Arrays.asList(
                Filters.eq("_id", new ObjectId(orderId)),
                Filters.eq("status", OrderStatus2.Unpaid.ordinal())
        );
        Document orderDoc = MongoUtil.getOneByConditions(COL, conditions1);

        return (orderDoc != null && !orderDoc.isEmpty());
    }

    public static boolean isOrderUnconfirmed(String orderId) {
        List<Bson> conditions1 = Arrays.asList(
                Filters.eq("_id", new ObjectId(orderId)),
                Filters.eq("status", OrderStatus2.Unconfirmed.ordinal())
        );
        Document orderDoc = MongoUtil.getOneByConditions(COL, conditions1);

        return (orderDoc != null && !orderDoc.isEmpty());
    }

    public static boolean isOrderSucceed(String orderId) {
        List<Bson> conditions1 = Arrays.asList(
                Filters.eq("_id", new ObjectId(orderId)),
                Filters.eq("status", OrderStatus2.Succeed.ordinal())
        );
        Document orderDoc = MongoUtil.getOneByConditions(COL, conditions1);

        return (orderDoc != null && !orderDoc.isEmpty());
    }

    public static boolean isOrderFailed(String orderId) {
        List<Bson> conditions1 = Arrays.asList(
                Filters.eq("_id", new ObjectId(orderId)),
                Filters.eq("status", OrderStatus2.Failed.ordinal())
        );
        Document orderDoc = MongoUtil.getOneByConditions(COL, conditions1);

        return (orderDoc != null && !orderDoc.isEmpty());
    }

    /**
     * 支付订单
     *
     * @param orderId
     */
    @Deprecated
    public static boolean payOrder(String orderId, String lang) {
        List<Bson> conditions1 = Arrays.asList(
                Filters.eq("_id", new ObjectId(orderId)),
                Filters.eq("status", OrderStatus.UnPay.ordinal())
        );
        Document orderDoc = MongoUtil.getOneByConditions(COL, conditions1);
        if (orderDoc == null) {
            return false;
        }

        String userId = orderDoc.getString("user_id");
        String planId = orderDoc.getString("plan_id");
        Document planDoc = MongoUtil.getOneById(MongoUtil.PLAN_COL, planId);
        List<Bson> conditions3 = Arrays.asList(
                Filters.exists("board_id", true)
        );
        int planType = ((Number) planDoc.get("type")).intValue();
        if (planType == PlanUtil.PriceType.AllCompany.ordinal()) {
            MongoUtil.getCollection(MongoUtil.PRODUCT_COL)
                    .find(Filters.and(conditions3))
                    .sort(ascending("_id"))
                    .forEach(new Consumer<Document>() {
                        @Override
                        public void accept(Document product) {
                            String productId = product.get("_id").toString();
                            List<Bson> conditions = Arrays.asList(
                                    Filters.eq("product_id", productId),
                                    Filters.eq("user_id", userId)
                            );
                            Document userProductDoc = MongoUtil.getOneByConditions(MongoUtil.USER_PRODUCT_COL, conditions);
                            //产品是否购买过
                            if (userProductDoc == null) {
                                ProductUtil.createUserProduct(orderDoc, productId);
                            } else {
                                String upId = userProductDoc.get("_id").toString();
                                ProductUtil.updateUserProduct(upId, orderDoc);
                            }
                        }
                    });
        } else {
            List<Document> products = orderDoc.get("products", List.class);
            for (Document product : products) {
                String productId = product.getString("id");
                List<Bson> conditions = Arrays.asList(
                        Filters.eq("product_id", productId),
                        Filters.eq("user_id", userId)
                );
                Document userProductDoc = MongoUtil.getOneByConditions(MongoUtil.USER_PRODUCT_COL, conditions);
                //产品是否购买过
                if (userProductDoc == null) {
                    ProductUtil.createUserProduct(orderDoc, productId);
                } else {
                    String upId = userProductDoc.get("_id").toString();
                    ProductUtil.updateUserProduct(upId, orderDoc);
                }
            }
        }

        // 改变订单状态
        Document orderUpdateDoc = new Document();
        orderUpdateDoc.append("status", OrderStatus.Pay.ordinal());
        MongoUtil.updateOneById(COL, orderId, orderUpdateDoc);

        JsonObject userJson = AccountUtil.getUserById(userId);
        boolean res = MailUtil.SendOrderDoneEmail(userJson.getString("email"), userJson.getString("username"), orderId, lang);

        return true;
    }

    /**
     * 支付订单，包括日报和产品
     *
     * @param orderId
     * @param lang
     * @return
     */
    public static boolean payOrder2(String orderId, String lang, boolean isPaidAccount, String adminId) {

        List<Bson> conditions1 = Arrays.asList(
                Filters.eq("_id", new ObjectId(orderId)),
                Filters.or(
                        Filters.eq("status", OrderStatus2.Unconfirmed.ordinal()),
                        Filters.eq("status", OrderStatus2.Unpaid.ordinal())
                )
        );
        Document orderDoc = MongoUtil.getOneByConditions(COL, conditions1);
        if (orderDoc == null) {
            return false;
        }

        String userId = String.valueOf(orderDoc.getOrDefault("user_id", ""));
        String planId = String.valueOf(orderDoc.getOrDefault("plan_id", ""));
        Document planDoc = MongoUtil.getOneById(MongoUtil.PLAN_COL, planId);
        List<Document> products = orderDoc.get("products", List.class);
        String planName = planDoc.getString("name");
        boolean res = true;
        boolean res1 = true;
        if (PlanUtil.Plan.Basic.name().equalsIgnoreCase(planName)) {
            res = ReportUtil.upsertUserReport(userId, orderDoc, products);
        } else if (PlanUtil.Plan.Standard.name().equalsIgnoreCase(planName)) {
            res1 = upsertUserProduct(userId, orderDoc, products);
        } else if (PlanUtil.Plan.Enterprise.name().equalsIgnoreCase(planName)) {
            res = ReportUtil.upsertUserReport(userId, orderDoc, products);
            if (res)
                res1 = upsertUserProduct(userId, orderDoc, products);
        }
        if (!res) {
            logger.error("ReportUtil.upsertUserReport error");
        }
        if (!res1) {
            logger.error("OrderUtil.upsertUserProduct error");
        }

        if (res && res1) {
            // 改变订单状态
            Document orderUpdateDoc = new Document("admin_id", adminId)
                    .append("status", OrderStatus2.Succeed.ordinal());
            MongoUtil.updateOneById(COL, orderId, orderUpdateDoc);

            //改变汇款通知的状态
            Document transferDoc = new Document("status", TransferStatus.Confirmed.ordinal());
            List<Bson> conds = new ArrayList<>(Arrays.asList(
                    Filters.eq("order_id", orderId),
                    Filters.eq("user_id", userId)
            ));
            MongoUtil.updateOne(MongoUtil.TRANSFER_NOTICE_COL, conds, transferDoc);

            Document userDoc = AccountUtil.getUserDocById(userId);
            String email = userDoc.getOrDefault("email", "").toString();
            String username = userDoc.getOrDefault("username", email).toString();
            if (isPaidAccount) {
                boolean res2 = MailUtil.SendOrderDoneEmail(email, username, orderId, lang);
                if (!res2) {
                    logger.error("OrderUtil.SendOrderDoneEmail error");
                }
            } else {
                logger.info("unpaid or trail account's order<" + orderId + "> was paid");
            }
        }
        String pattern = "user_product_" + userId + "*";
        String pattern2 = "expiry_status_" + userId + "*";
//        logger.info("pattern: " + pattern);
        RedisUtil.del(pattern, pattern2);

        return true;
    }

    public static boolean upsertUserProduct(String userId, Document orderDoc, List<Document> products) {
        for (Document product : products) {
            String productId = product.getString("id");
            List<Bson> conditions = Arrays.asList(
                    Filters.eq("user_id", userId),
                    Filters.eq("product_id", productId)
            );
            Document userProductDoc = MongoUtil.getOneByConditions(MongoUtil.USER_PRODUCT_COL, conditions);
            //产品是否购买过
            if (userProductDoc == null) {
                ProductUtil.createUserProduct(orderDoc, productId);
            } else {
                String upId = userProductDoc.get("_id").toString();
                ProductUtil.updateUserProduct(upId, orderDoc);
            }
        }
        return true;
    }

    /**
     * @return
     */
    public static int getActivePlanCount(String userId) {

        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("user_id", userId));
        conds.add(Filters.gt("stop_time", new Date()));

        int res = MongoUtil.getDBCount(COL, conds);

        return res;
    }

    public static int getExpiringPlanCount(String userId) {
        Calendar calender = Calendar.getInstance();
        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("user_id", userId));
        conds.add(Filters.gt("stop_time", new Date()));
        calender.add(Calendar.MONTH, 12);        //剩余时间少于12个月视为即将过期
        conds.add(Filters.lt("stop_time", calender.getTime()));
        int res = MongoUtil.getDBCount(COL, conds);

        return res;
    }

    public static int getExpiredPlanCount(String userId) {

        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("user_id", userId));
        conds.add(Filters.lt("stop_time", new Date()));

        int res = MongoUtil.getDBCount(COL, conds);

        return res;
    }

    /**
     * 获取用户订单
     *
     * @param userId
     * @param status
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    public static Document getUserOrders(String userId, int status, String sort, String order, int offset, int limit) {
        List<Bson> conditions = new ArrayList<>();
        conditions.add(Filters.eq("user_id", userId));
        if (status != -1) {
            conditions.add(Filters.eq("status", status));
        }

        List<Document> list;
        list = MongoUtil.getDBList(MongoUtil.ORDER_COL, conditions, sort, order, offset, limit, new String[]{});

        int total = MongoUtil.getDBCount(MongoUtil.ORDER_COL, conditions);
        Document data = new Document("total", total).append("list", list);

        return data;
    }

    public static List<String> getOrderProductNames(Document orderDoc) {
        List<String> productNames = new ArrayList<>();
        List<Document> products = orderDoc.get("products", List.class);
        products.forEach(new Consumer<Document>() {
            @Override
            public void accept(Document document) {
                String productId = document.getString("id");
                Document productDoc = MongoUtil.getOneById(MongoUtil.PRODUCT_COL, productId);
                productNames.add(productDoc.getString("name"));
            }
        });

        return productNames;
    }

    /**
     * @param orderId
     * @return
     */
    public static Document getOrderDate(String orderId) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("orders.id", orderId)
        );

        Document dates = new Document();
        Document userProductDoc = MongoUtil.getOneByConditions(MongoUtil.USER_PRODUCT_COL, conditions);
        if (userProductDoc == null || !userProductDoc.containsKey("orders")) {

            Document order = MongoUtil.getOneById(MongoUtil.ORDER_COL, orderId);
            Date createTime = order.getDate("create_at");

            dates.append("start_time", createTime);
            dates.append("stop_time", order.get("stop_time"));

        } else {
            List<Document> orders = userProductDoc.get("orders", List.class);
            for (Document order : orders) {
                String id = order.getString("id");
                if (id.equals(orderId)) {
                    dates.append("start_time", order.get("start_time"));
                    dates.append("stop_time", order.get("stop_time"));
                }
            }
        }

        return dates;
    }

    public static Document getOrders(String sort, String order, int offset, int limit, String query, int status) {
        Document data = new Document();

        List<Bson> conditions = new ArrayList<>();
        if (status >= 0 && status < 5) {
            conditions.add(Filters.eq("status", status));
        }
        if (!StringUtils.isEmpty(query)) {
            List<Bson> queryConditions = Arrays.asList(
                    Filters.regex("_id", query, "i"),
                    Filters.regex("user_email", query, "i")
            );
            conditions.add(Filters.or(queryConditions));
        }

        List<Document> orderList = new ArrayList<>();
        FindIterable<Document> findIterable = MongoUtil.getDBFindIterable(MongoUtil.ORDER_COL, conditions, sort, order, offset, limit);
        findIterable.forEach(new Consumer<Document>() {
            @Override
            public void accept(Document order) {
                String upId = order.get("_id").toString();
                String planId = order.getString("plan_id");
                String term = String.format("%s %s", order.get("term_long"), order.getString("term_unit"));
                Document planDoc = MongoUtil.getOneById(MongoUtil.PLAN_COL, planId);
                Document item = new Document()
                        .append("id", upId)
                        .append("status", orderStatusToString(order.getInteger("status")))
                        .append("user_id", order.get("user_id"))
                        .append("user_email", order.get("user_email"))
                        .append("create_time", TimeUtil.date2String(order.getDate("create_at")))
                        .append("plan", new Document()
                                .append("name", planDoc.getString("name"))
                                .append("term", term)
                                .append("products", getOrderProductNames(order))
                        );
                if (order.containsKey("start_time")) {
                    item.append("start_time", TimeUtil.date2String(order.getDate("start_time")))
                            .append("stop_time", TimeUtil.date2String(order.getDate("stop_time")));
                }

                orderList.add(item);
            }
        });

        int total = MongoUtil.getDBCount(MongoUtil.ORDER_COL, conditions);
        data.append("total", total)
                .append("list", orderList);

        return data;
    }

    /**
     * 获取订单历史
     *
     * @param cuid
     * @param lang
     * @param q
     * @param status
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    public static Document getOrderHistory(
            String cuid, String lang, String q, String sort, String order, int offset, int limit, int status
    ) {
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                Filters.eq("user_id", cuid)
        ));

        if (status != 5) {
            conds.add(Filters.eq("status", status));
        }
        if (!StringUtils.isEmpty(lang)) {
            lang = "_" + lang;
        }

        if (!StringUtils.isEmpty(q)) {
        }

        List<Document> list = new ArrayList<>();
        List<Document> dbList = MongoUtil.getDBList(MongoUtil.ORDER_COL, conds, sort, order, offset, limit, new String[]{});
        for (Document document : dbList) {
            Document orderDoc = new Document();
            String time = TimeUtil.date2String(document.getDate("create_at"), "YYYY-M-d");
            String plan = PlanUtil.getPlan(String.valueOf(document.getOrDefault("plan_id", ""))).getString("name" + lang);
            List<String> companyList = ProductUtil.getAllProductNames(document.get("products", ArrayList.class), lang);
            Date stopTime = document.containsKey("stop_time")
                    ? document.getDate("stop_time")
                    : TimeUtil.getRelateDate(
                    document.getDate("create_at"),
                    Calendar.YEAR,
                    document.getInteger("term_long"));
            String deadLine = TimeUtil.date2String(stopTime, "YYYY-M-d");
            String statusStr = orderStatusToString(document.getInteger("status"));
            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            String priceTotal = numberFormat.format(document.get("price_total"));
            orderDoc.append("time", time)
                    .append("plan", plan)
                    .append("company", companyList)
                    .append("deadline", deadLine)
                    .append("price", priceTotal)
                    .append("status", statusStr)
                    .append("id", document.getString("id"));
            list.add(orderDoc);
        }
        int total = MongoUtil.getDBCount(MongoUtil.ORDER_COL, conds);
        Document res = new Document("total", total)
                .append("list", list);
        return res;
    }

    private static String orderStatusToString(Integer status) {
        return OrderStatus2.valueOf(status).name();
    }

    /**
     * 获取用户订单详情
     *
     * @param cuid
     * @param orderId
     * @return
     */
    public static Document getPaymentType(String cuid, String orderId) {
        Document res = new Document();
        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("user_id", cuid));
        conds.add(Filters.eq("_id", new ObjectId(orderId)));
        Document orderDoc = MongoUtil.getOneByConditions(MongoUtil.ORDER_COL, conds);
        if (orderDoc == null) {
            logger.error("can't find the order which's user_id = " + cuid + " and order_id = " + orderId);
            return null;
        }
        Document payment = orderDoc.get("payment", Document.class);
        if (payment.containsKey("type")) {
            //TODO: 付款类型(银行转账、支付宝、微信)
            int type = payment.getInteger("type");
            res = getPaymentType(orderDoc, type);
        } else {
            res = getPaymentType(orderDoc, 0);
        }
        return res;
    }

    /**
     * 获取付款类型
     *
     * @param orderDoc
     * @param type
     * @return
     */
    private static Document getPaymentType(Document orderDoc, int type) {
        Document configDoc = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "bank_info");
        if (configDoc == null) {
            logger.error("can't get bank_info from config col");
            return null;
        }
        String company = configDoc.getString("company");
        String card = configDoc.getString("card");
        String bank = configDoc.getString("bank");
        Document res = new Document("type", type);
        if (type == 0) {
            res.append("price_total", orderDoc.getInteger("price_total"))
                    .append("order_id", orderDoc.getObjectId("_id").toString())
                    .append("company", company)
                    .append("card", card)
                    .append("bank", bank);
        } else if (type == 1) {
            //TODO: add other payment type
            res = getPaymentType(orderDoc, 0);
        } else if (type == 2) {
            res = getPaymentType(orderDoc, 0);
        } else {
            res = getPaymentType(orderDoc, 0);
        }
        return res;
    }

    /**
     * 发起到账通知
     *
     * @param paramMap
     * @return
     */
    public static boolean sendTransferNotice(Map<String, Object> paramMap) throws ParseException {
        Document noticeDoc = new Document();
        paramMap.forEach(noticeDoc::append);
        String orderId = paramMap.get("order_id").toString();
        String email = paramMap.get("email").toString();
        String name = paramMap.get("name").toString();
        String lang = paramMap.get("lang").toString();
        //未确认到账的状态
        noticeDoc.put("status", TransferStatus.Unconfirmed.ordinal());
        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("user_id", noticeDoc.getString("user_id")));
        conds.add(Filters.eq("order_id", noticeDoc.getString("order_id")));
        Document document = MongoUtil.getOneByConditions(MongoUtil.TRANSFER_NOTICE_COL, conds);
        Document res;
        if (document != null) {
            if (document.getInteger("status") == TransferStatus.Confirmed.ordinal()) {
                logger.error("reduplicate transfer notice");
                return false;
            }
            res = MongoUtil.updateOne(MongoUtil.TRANSFER_NOTICE_COL, conds, noticeDoc);
        } else {
            res = MongoUtil.insertOne(MongoUtil.TRANSFER_NOTICE_COL, noticeDoc);
        }
        if (res == null) {
            return false;
        }

        Document updateDoc = new Document("status", OrderStatus2.Unconfirmed.ordinal());
        Document res1 = MongoUtil.updateOneById(
                MongoUtil.ORDER_COL, noticeDoc.getString("order_id"), updateDoc);
        if (res1 == null) logger.error("update order error");

        boolean res2 = MailUtil.SendOrderCreatedEmail(
                email, AccountUtil.getUsernameByEmail(email), orderId, lang);
        if (!res2) logger.error("send order created email failed");
        return res2;
    }

    /**
     * 获取到账通知
     *
     * @param query
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param status
     * @return
     */
    public static Document getTransferNotice(String query, String sort, String order, int offset, int limit, int status) {
        List<Document> data;
        List<Bson> conds = new ArrayList<>();
        //可按照订单email查询
        if (!StringUtils.isEmpty(query)) {
            conds.add(Filters.regex("email", query, "i"));
        }
        if (status == 0 || status == 1) {
            conds.add(Filters.eq("status", status));
        }
        data = MongoUtil.getDBList(MongoUtil.TRANSFER_NOTICE_COL, conds, sort, order, offset, limit, new String[]{});
        data.forEach(document -> {
            document.replace("date", TimeUtil.date2String(document.getDate("date"), "yyyy-MM-dd"));
            document.replace("status", document.getInteger("status") == 0 ? "Unconfirmed" : "Confirmed");
            document.replace("create_at", TimeUtil.date2String(document.getDate("create_at"), "yyyy-MM-dd"));
            document.replace("update_at", TimeUtil.date2String(document.getDate("update_at"), "yyyy-MM-dd"));
        });
        Document res = new Document("total", MongoUtil.getDBCount(MongoUtil.TRANSFER_NOTICE_COL, conds))
                .append("list", data);
        return res;
    }

    /**
     * 获取最新的订单状态
     *
     * @param userId
     * @return
     */
    public static Document getLastestOrderInfo(String userId) {
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                Filters.eq("user_id", userId),
                Filters.ne("checked_last", true),
                Filters.or(
                        Filters.eq("status", OrderStatus2.Succeed.ordinal()),
                        Filters.eq("status", OrderStatus2.Failed.ordinal())
                )
        ));
        List<Document> documentList = MongoUtil.getDBList(COL, conds);
        List<Document> resList = new ArrayList<>();
        for (Document document : documentList) {
            if (document.containsKey("status")) {
                String id = document.getString("id");
                int status = document.getInteger("status");

                Document document1 = new Document();
                document1.append("id", id)
                        .append("status", status)
                        .append("status_str", orderStatusToString(status))
                        .append("price", Integer.valueOf(document.get("price_total").toString()))
                        .append("plan", PlanUtil.getPlan(
                                String.valueOf(document.getOrDefault("plan_id", "")))
                                .getString("name"));

                resList.add(document1);
                //添加已检查标签
                Document updateDoc = new Document("checked_last", true);
                MongoUtil.updateOneById(COL, id, updateDoc);
            }
        }
        Document resDoc = new Document();
        resDoc.append("total", resList.size())
                .append("list", resList);
        return resDoc;
    }

    /**
     * 获取订单方案
     *
     * @param orderId
     * @return
     */
    public static Document getOrderPlan(String orderId) {
        Document document = MongoUtil.getOneById(COL, orderId);
        return PlanUtil.getPlan(String.valueOf(document.getOrDefault("plan_id", "")));
    }

    /**
     * 获取订单方案名称
     *
     * @param orderId
     * @param lang
     * @return
     */
    public static String getOrderPlanName(String orderId, String lang) {
        if (!StringUtils.isEmpty(lang) && !lang.startsWith("_")) {
            lang = "_" + lang;
        }
        return getOrderPlan(orderId).getString("name" + lang);
    }

    public static Document getOrderData(String email, List<String> pids, int plan, int termLong, String termUnit, String lang) {
        //产品id
//        logger.info("pids: " + Arrays.toString(pids.toArray()));
        List<Bson> conds = Arrays.asList(
                Filters.in("_id", pids.stream().map(ObjectId::new).collect(Collectors.toList())),
                Filters.exists("board_id")
        );
        List<Document> productsList = MongoUtil.getDBList(MongoUtil.PRODUCT_COL, conds);
        logger.info("product id count: " + productsList.size());
        productsList = productsList
                .stream()
                .map(document -> new Document("id", document.get("id")))
                .collect(Collectors.toList());
        pids = productsList
                .stream()
                .map(document -> document.getOrDefault("id", "").toString())
                .collect(Collectors.toList());
        //总价
        String planName = PlanUtil.Plan.valueOf(plan).name();
        Document planDoc = PlanUtil.getPlanLevels(pids, planName, lang.isEmpty() ? "" : "_" + lang);
        List<Document> levels = planDoc.get("levels", ArrayList.class);
        String planID = String.valueOf(planDoc.getOrDefault("_id", planDoc.getOrDefault("id", "")));
        int priceTotal = 0;
//        for (Document document : levels) {
//            if (document.getInteger("id").equals(termLong)) {
//                priceTotal = document.getInteger("price");
//                break;
//            }
//        }
        priceTotal = levels.get(0).getInteger("price") * 2;
        if (termUnit.equalsIgnoreCase("year")) {
            priceTotal *= termLong;
        } else if (termUnit.equalsIgnoreCase("month")) {
            priceTotal *= (termLong / 12.0);
        } else if (termUnit.equalsIgnoreCase("day")) {
            priceTotal *= (termLong / 365.0);
        }
        //用户数据
        Document account = new Document("email", email);
        Document payment = new Document("type", 0);
        Document orderData = new Document();
        orderData.append("plan_id", planID)
                .append("level_id", 0)
                .append("term_long", termLong)
                .append("term_unit", termUnit)
                .append("products", productsList)
                .append("account", account)
                .append("payment", payment)
                .append("price_total", priceTotal);

        return orderData;
    }

    public static boolean belongsTo(String orderId, String userId) {
        try {
            Document doc = MongoUtil.getOneById(COL, orderId);
            return doc != null
                    && !doc.isEmpty()
                    && doc.containsKey("user_id")
                    && doc.get("user_id").toString().equalsIgnoreCase(userId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Deprecated
    public enum OrderStatus {
        UnPay,          //未付款
        Pay,            //已付款
        Canceled,       //订单被取消
        NULL;
    }

    public enum OrderStatus2 {
        Unpaid(0),         //提交订单但没有发起汇款通知的
        Succeed(1),        //发起汇款通知且确认已到帐的
        Failed(2),         //发起汇款通知但3个工作日确认未到账的
        Unconfirmed(3),    //发起汇款通知但系统尚未确认的
        Canceled(4),       //订单被取消
        NULL(5);
        private int id;

        OrderStatus2(int id) {
            this.id = id;
        }

        public static OrderStatus2 valueOf(int id) {
            for (OrderStatus2 orderStatus2 : OrderStatus2.values()) {
                if (orderStatus2.id == id) {
                    return orderStatus2;
                }
            }
            return NULL;
        }
    }

    public enum TransferStatus {
        Unconfirmed,    //未确认的汇款通知
        Confirmed,      //已确认的汇款通知
    }
}