package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.*;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by zhyzhu on 17-4-23.
 */
@SuppressWarnings("deprecation")
@Path("/")
public class OrderController extends BaseController {

    private static Logger logger = Logger.getLogger(OrderController.class);

    /**
     * 生成订单
     *
     * @return
     */
    @POST
    @Path("v1/orders/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public JsonObject createOrder(
            @DefaultValue("") @QueryParam("lang") String lang,
            JsonObject orderData
    ) {
        if (!orderData.containsKey("plan_id") ||
                !orderData.containsKey("level_id") ||
                !orderData.containsKey("term_long") ||
                !orderData.containsKey("term_unit") ||
                !orderData.containsKey("products") ||
                !orderData.containsKey("account") ||
                !orderData.containsKey("payment") ||
                !orderData.containsKey("price_total")
                ) {
            return getResponse(false, 2, "参数错误");
        }

        try {
            String orderId = OrderUtil.createOrder(MongoUtil.json2Document(orderData), getLang(lang));
            if (StringUtils.isEmpty(orderId)) {
                return getResponse(false, 3, "生成订单失败");
            }

            Document respData = new Document();
            respData.put("id", orderId);

            return getResponse(true, respData);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return getResponse(false, 1, "生成订单错误 ");
    }

    /**
     * 生成订单2
     *
     * @return
     */
    @POST
    @Path("v1/orders/create2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject createOrder2(
            @DefaultValue("") @QueryParam("lang") String lang,
            JsonObject orderData
    ) {
        if (!orderData.containsKey("plan_id") ||
                !orderData.containsKey("level_id") ||
                !orderData.containsKey("term_long") ||
                !orderData.containsKey("term_unit") ||
                !orderData.containsKey("products") ||
                !orderData.containsKey("account") ||
                !orderData.containsKey("payment") ||
                !orderData.containsKey("price_total")
                ) {
            return getResponse(false, 2, "参数错误");
        }

        try {
            logger.info(getCurrentUserEmail() + "is creating order");
            String orderId = OrderUtil.createOrder2(MongoUtil.json2Document(orderData), getLang(lang));
            if (StringUtils.isEmpty(orderId) || !OrderUtil.isOrderExists(orderId)) {
                return getResponse(false, 3, "生成订单失败 " + orderId);
            }

            Document respData = new Document();
            respData.put("id", orderId);
            logger.info("order<" + orderId + "> created successfully");
            return getResponse(true, respData);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "生成订单错误" + StringUtils.showError(e));
        }

    }

    /**
     * 取消订单
     *
     * @param id
     * @return
     */
    @POST
    @Path("v1/orders/cancel/{id: [\\d\\w]+}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public JsonObject cancelOrder(
            @PathParam("id") String id
    ) {
        try {
            String userId = getCurrentUserId();
            if (AccountUtil.isNotSuperAdmin(userId)) {
                return getResponse(false, 5, "no operator authority");
            }
            if (StringUtils.isEmpty(id)) {
                return getResponse(false, 2, "订单编号为空");
            }

            int orderStatus = OrderUtil.getOrderStatus(id);
            if (orderStatus != OrderUtil.OrderStatus.UnPay.ordinal()) {
                return getResponse(false, 3, "订单状态错误");
            }

            boolean res = OrderUtil.cancelOrder(userId, id);
            if (!res) {
                return getResponse(false, 4, "取消订单失败");
            }

            return getResponse(true);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return getResponse(false, 1, "取消订单错误");
    }

    /**
     * 取消订单2
     *
     * @param id
     * @return
     */
    @POST
    @Path("v1/orders/cancel2/{id: [\\d\\w]+}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject cancelOrder2(
            @PathParam("id") String id
    ) {
        try {
            String userId = getCurrentUserId();
            if (AccountUtil.isNotSuperAdmin(userId)) {
                return getResponse(false, 5, "no operator authority");
            }
            if (StringUtils.isEmpty(id)) {
                return getResponse(false, 2, "订单编号为空");
            }

            int orderStatus = OrderUtil.getOrderStatus(id);
            if (orderStatus != OrderUtil.OrderStatus2.Unpaid.ordinal()) {
                return getResponse(false, 3, "订单状态错误");
            }

            boolean res = OrderUtil.cancelOrder2(userId, id);
            if (!res) {
                return getResponse(false, 4, "取消订单失败");
            }

            return getResponse(true);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return getResponse(false, 1, "取消订单错误");
    }

    /**
     * 支付订单
     *
     * @return
     */
    @GET
    @Path("v1/orders/pay/{id: [\\d\\w]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public JsonObject payOrder(
            @DefaultValue("") @QueryParam("lang") String lang,
            @PathParam("id") String id
    ) {
        String userId = getCurrentUserId();
        if (!AccountUtil.isSuperAdmin(userId)) {
            return getResponse(false, 1, "无操作权限");
        }
        return payOrder2(id, lang);
//        try {
//            if (!OrderUtil.isOrderUnpay(id)) {
//                return getResponse(false, 3, "非未支付订单");
//            }
//
//            boolean res = OrderUtil.payOrder(id, getLang(lang));
//            if (!res) {
//                return getResponse(false, 2, "支付订单失败");
//            }
//
//            return getResponse(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error(e.toString());
//        }
//
//        return getResponse(false, 1, "订单支付失败");
    }

    /**
     * 支付看板和日报订单
     *
     * @return
     */
    @GET
    @Path("v1/orders/pay2/{id: [\\d\\w]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject payOrder2(
            @PathParam("id") String userId,
            @DefaultValue("") @QueryParam("lang") String lang
    ) {
        String cuid = getCurrentUserId();
        logger.info("<" + getCurrentUserEmail() + "> is paying for order<" + userId + ">");
        if (!AccountUtil.isSuperAdmin(cuid)) {
            logger.error("Payment failure, no operation authority");
            return getResponse(false, 1, "无操作权限");
        }
        try {
            if (OrderUtil.isOrderSucceed(userId)) {
                logger.error("Payment failure, reduplicate payment");
                return getResponse(false, 2, "请勿重复支付订单");
            }
//            if (OrderUtil.isOrderUnpaid(userId)) {
//                logger.error("Payment failure, cannot find transfer notice of the order");
//                return getResponse(false, 3, "订单未发起汇款通知");
//            }
//            if (OrderUtil.isOrderFailed(userId)) {
//                logger.info("Payment failure, status: 5");
//                return getResponse(false, 5, "订单已失效");
//            }
            boolean res = OrderUtil.payOrder2(userId, getLang(lang), true, cuid);
            if (!res) {
                logger.error("Payment failure");
                return getResponse(false, 4, "支付订单失败");
            }
            logger.info("Payment success");
            return getResponse(true);

        } catch (Exception e) {
            logger.error("Payment failure, have some exceptions" + e.getMessage(), e);
            return getResponse(false, 5, "订单支付失败" + StringUtils.showError(e));
        }
    }

    /**
     * 获取订单列表
     *
     * @return
     */
    @GET
    @Path("v1/orders")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getOrders(
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("create_at") @QueryParam("sort") String sort,
            @DefaultValue("desc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("-1") @QueryParam("status") int status
    ) {
        String cuid = getCurrentUserId();
        if (!AccountUtil.isSuperAdmin(cuid)) {
            return getResponse(false, 2, "无操作权限");
        }

        try {
            Document data = OrderUtil.getOrders(sort, order, offset, limit, query, status);
            if (data == null || data.isEmpty()) {
                return getResponse(false, 3, "获取订单失败");
            }

            return getResponse(true, data);
        } catch (Exception e) {
            logger.error(e);
            return getResponse(false, 1, "获取订单列表错误" + StringUtils.showError(e));
        }

    }

    /**
     * 获取订单列表
     *
     * @return
     */
    @GET
    @Path("v1/user-orders")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getUserOrders(
            @DefaultValue("") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("-1") @QueryParam("status") int status
    ) {
        try {
            String userId = getCurrentUserId();
            Document data;
            data = OrderUtil.getUserOrders(userId, status, sort, order, offset, limit);
            if (data != null) {
                return getResponse(false, 2, "获取订单列表失败");
            }
            return getResponse(true, data);
        } catch (Exception e) {
            logger.error(e);
            return getResponse(false, 1, "获取订单列表错误" + StringUtils.showError(e));
        }

    }

    /**
     * 获取订单详情
     *
     * @return
     */
    @GET
    @Path("v1/orders/{id: [\\d\\w]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getOrders(
            @PathParam("id") String id
    ) {
        try {
            String userId = getCurrentUserId();
            if (AccountUtil.isNotSuperAdmin(userId) && !OrderUtil.belongsTo(id, userId)) {
                return getResponse(false, 3, "no operator authority");
            }
            Document document = MongoUtil.getOneById(MongoUtil.ORDER_COL, id);
            if (document == null || document.isEmpty()) {
                return getResponse(false, 2, "获取订单信息失败");
            }

            return getResponse(true, document);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "获取产品列错误" + StringUtils.showError(e));
        }

    }

    /**
     * 管理员创建订单
     *
     * @param email    user email
     * @param namesStr 产品名称，以逗号分隔
     * @param plan     select all products
     * @param lang     0: half year, 1: one year, 2: two year
     * @return
     */
    @Path("v1/orders/cp")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject createAndPayOrder(
            @DefaultValue("") @QueryParam("email") String email,// user email
            @DefaultValue("") @QueryParam("n") String namesStr,              // products' names
            @DefaultValue("") @QueryParam("n[]") List<String> namesList,              // products' names
            @DefaultValue("") @QueryParam("p[]") List<String> productsList,              // products' stock code
            @DefaultValue("") @QueryParam("type") String type,  // all: select all products
            @DefaultValue("1") @QueryParam("plan") int plan,    // 0:basic, 1: standard, 2:enterprise
            @DefaultValue("1") @QueryParam("term_long") int termLong, // 0: half year, 1: one year, 2: two year
            @DefaultValue("year") @QueryParam("term_unit") String termUnit,
            @DefaultValue("") @QueryParam("lang") String lang
    ) {
        try {
            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            namesList.removeIf(String::isEmpty);
            productsList.removeIf(String::isEmpty);
            if (StringUtils.isEmpty(type) && namesList.isEmpty() && productsList.isEmpty() && namesStr.isEmpty()) {
                return getResponse(false, 2, "n should be product names, p should be product stock code");
            }
            if (plan > 2 || plan < 0) {
                return getResponse(false, 3, "plan should be 0, 1 or 2");
            }
            if (AccountUtil.isNotUserExists(email)) {
                return getResponse(false, 7, "user not exists");
            }
            if (!termUnit.trim().matches("years?|months?|days?")) {
                return getResponse(false, 8, "term unit error");
            }
            List<String> names, products = new ArrayList<>();
            if (!StringUtils.isEmpty(namesStr)) {
                names = StringUtils.CSV2List(namesStr);
            } else {
                names = namesList;
                products = productsList;
            }
            Document respData = OrderUtil.createAndPayOrder(
                    email, type, names, products,
                    plan, termLong, termUnit, lang, cuid);
            if (respData == null) {
                return getResponse(false, 5, "生成订单错误 ");
            }
            if (respData.containsKey("error")) {
                return getResponse(false, 8, String.valueOf(respData.get("error")));
            }
            logger.info("order created and payed by admin<" + cuid + ">, id: " +
                    respData.getOrDefault("order_id", ""));

            return getResponse(true, respData);
        } catch (Exception e) {
            logger.info("order create failed " + e.getMessage(), e);
            return getResponse(false, 0, "生成订单错误" + StringUtils.showError(e));
        }
    }

    /**
     * 获取订单历史
     *
     * @param lang
     * @param q
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param status
     * @return
     */
    @Path("v1/orders/history")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getOrderHistory(
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("") @QueryParam("q") String q,
            @DefaultValue("create_at") @QueryParam("sort") String sort,
            @DefaultValue("desc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("5") @QueryParam("status") int status
    ) {
        try {
            String cuid = getCurrentUserId();
            Document res = OrderUtil.getOrderHistory(cuid, lang, q, sort, order, offset, limit, status);
            if (res != null) {
                return getResponse(true, res);
            }
            return getResponse(false, 2, "获取订单历史失败");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "获取订单历史错误" + StringUtils.showError(e));
        }
    }

    /**
     * 获取订单详情
     *
     * @param orderId
     * @return
     */
    @Path("v1/orders/payment/{id: [\\d\\w]+}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getPaymentype(
            @PathParam("id") String orderId
    ) {
        try {

            String cuid = getCurrentUserId();
            Document res = OrderUtil.getPaymentType(cuid, orderId);
            if (res == null) {
                return getResponse(false, 1, "get order info failed");
            }
            return getResponse(true, res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return getResponse(false, 2, "get order info error" + StringUtils.showError(e));
        }
    }

    /**
     * 发起到账通知
     *
     * @return
     */
    @Path("v1/orders/transfer/{order_id: [\\d\\w]+}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject sendTransferNotice(
            @PathParam("order_id") String orderId,
            @FormParam("name") String name,
            @FormParam("card") String card,
            @FormParam("bank") String bank,
            @FormParam("price") String price,
            @FormParam("date") String dateStr,
            @DefaultValue("") @FormParam("lang") String lang
    ) {
        try {
            logger.info(getCurrentUserEmail() + "is sending transfer notice...");
            if (StringUtils.isEmpty(orderId)
                    || StringUtils.isEmpty(name)
                    || StringUtils.isEmpty(card)
                    || StringUtils.isEmpty(bank)
                    || StringUtils.isEmpty(price)
                    || StringUtils.isEmpty(dateStr)) {
                logger.error("missing form param");
                return getResponse(false, 1, "missing form param");
            }
            Date date = TimeUtil.strToDate(dateStr, "yyyy-MM-dd");
            if (date.after(new Date())) {
                logger.error("date error, can't be after today");
                return getResponse(false, 2, "date error, can't be after today");
            }
//            price = price.trim().replaceAll(",", "");
            Map<String, Object> paramMap = new LinkedHashMap<>();
            paramMap.put("user_id", getCurrentUserId());
            paramMap.put("order_id", orderId);
            paramMap.put("email", getCurrentUserEmail());
            paramMap.put("name", name);
            paramMap.put("bank", bank);
            paramMap.put("card", card);
            paramMap.put("price", price);
            paramMap.put("date", date);
            paramMap.put("lang", lang);
            boolean res = OrderUtil.sendTransferNotice(paramMap);
            if (!res) {
                logger.error("fail in send transfer advice");
                return getResponse(false, 3, "fail in send transfer advice");
            }
            logger.info("send transfer advice successfully");
            return getResponse(true, "send transfer advice successfully");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("sent transfer advice wrong");
            return getResponse(false, 4, "sent transfer advice wrong" + StringUtils.showError(e));
        }
    }

    /**
     * 获取到账通知（管理员权限）
     *
     * @param query
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param status 到账状态，0=未到账；1=已到帐；2=全部（默认）
     * @return
     */
    @Path("v1/orders/transfer")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getTransferNotice(
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("create_at") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("999") @QueryParam("limit") int limit,
            @DefaultValue("2") @QueryParam("status") int status
    ) {
        try {
            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            Document res = OrderUtil.getTransferNotice(query, sort, order, offset, limit, status);
            if (res == null) {
                return getResponse(false, 2, "get transfer advice failed");
            }
            return getResponse(true, res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "get transfer advice error" + StringUtils.showError(e));
        }
    }

    /**
     * 每次进入页面都要调用这个api
     * 检查最新的订单状态
     * 返回订单完成或失败的结果
     * 前端根据结果展示对应页面
     *
     * @return
     */
    @Path("v1/orders/lastest")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getLastestOrderInfo() {
        try {
            String cuid = getCurrentUserId();
            if (!AccountUtil.isPaidAccount(cuid)) {
                return getResponse(true, new Document("total", 0)
                        .append("list", Collections.EMPTY_LIST));
            }
            Document document = OrderUtil.getLastestOrderInfo(cuid);
            if (document != null)
                return getResponse(true, document);
            return getResponse(false, 1, "get lastest order info failed");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }
}
