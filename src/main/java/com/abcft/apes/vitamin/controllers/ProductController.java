package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.*;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.annotation.security.PermitAll;
import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;


/**
 * Created by zhyzhu on 17-4-23.
 */
@PermitAll()
@Path("/")
public class ProductController extends BaseController {
    private static Logger logger = Logger.getLogger(ProductController.class);

    @GET
    @Path("v1/products")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getProducts(
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("3") @QueryParam("type") int type,
            @DefaultValue("0") @QueryParam("capitalize") int capitalize
    ) {
        if (!StringUtils.isEmpty(lang)) {
            lang = "_" + lang;
        }
        try {
            Document products = ProductUtil.getProducts(sort, order, offset, limit, query, lang, type, capitalize);
            if (products == null || products.isEmpty()) {
                return getResponse(false, 2, "获取产品列表失败");
            }

            return getResponse(true, products);
        } catch (Exception e) {
            logger.error(e);
        }

        return getResponse(false, 1, "获取产品列表失败");
    }

    /**
     * 获取产品详情
     *
     * @param id
     * @return
     */
    @GET
    @Path("v1/products/{id: [\\d\\w]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getProduct(
            @PathParam("id") String id
    ) {
        try {

            Document product = ProductUtil.getProduct(id);
            if (product == null || product.isEmpty()) {
                return getResponse(false, 2, "获取产品列表失败");
            }

            return getResponse(true);
        } catch (Exception e) {
            logger.error(e);
        }

        return getResponse(false, 1, "获取产品列表失败");
    }

    /**
     * 获取用户已购产品
     *
     * @param query
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    @GET
    @Path("v1/user-products")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getUserProducts(
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("") @QueryParam("category") String category,
            @DefaultValue("") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("minute") @QueryParam("cache_time_unit") String cacheTimeUnit,
            @DefaultValue("5") @QueryParam("cache_expiry") int cacheExpiry
    ) {
        if (!StringUtils.isEmpty(lang)) {
            lang = "_" + lang;
        }
        String userId = getCurrentUserId();
        try {
            long s = System.currentTimeMillis();
            Document products = ProductUtil.
                    getCachedProducts(userId, sort, order, offset, limit, category, query, lang, cacheTimeUnit, cacheExpiry);
            long e = System.currentTimeMillis();
//            logger.error("get_user_products_time: " + (e - s) / 1000.0);
            if (products == null || products.isEmpty()) {
                return getResponse(false, 2, "获取产品列表失败");
            }

            return getResponse(true, products);
        } catch (Exception e) {
            logger.error(e);
        }

        return getResponse(false, 1, "获取产品列表失败");
    }

    /**
     * 获取用户已购产品
     *
     * @return
     */
    @GET
    @Path("v1/user-products/{id: [\\d\\w]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getUserProduct(
            @PathParam("id") String id,
            @DefaultValue("") @QueryParam("lang") String lang
    ) {
        if (!StringUtils.isEmpty(lang)) {
            lang = "_" + lang;
        }
        String userId = getCurrentUserId();
        try {

            Document document = ProductUtil.getUserProductBoard(id, userId, lang);
            if (document == null) {
//                logger.info("is not user product");
                document = ProductUtil.getFreeUserProductBoard(id, userId, lang);
            }
            if (document == null) {
//                logger.info("is not free product");
                return getResponse(false, 1, "获取用户产品失败");
            }
            return getResponse(true, document);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            return getResponse(false, 1, StringUtils.showError(e));
        }

    }

    /**
     * 升级用户产品
     *
     * @return
     */
    @POST
    @Path("v1/user-products/upgrade/{id: [\\d\\w]+}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject upgradeUserProduct(
            @PathParam("id") String id,
            @FormParam("plan_id") String planId
    ) {
        String userId = getCurrentUserId();
        try {

            Document upDoc = MongoUtil.getOneById(MongoUtil.USER_PRODUCT_COL, id);
            if (upDoc == null) {
                return getResponse(false, 1, "产品不存在");
            }
            int status = ProductUtil.getUserProductStatus(upDoc);
            if (status != ProductUtil.ProductStatus.Avail.ordinal()) {
                return getResponse(false, 2, "产品无效");
            }
            Document document = ProductUtil.upgradeUserProduct(id, userId, planId);
            if (document == null) {
                return getResponse(false, 3, "获取");
            }
            return getResponse(true, document);
        } catch (Exception e) {
            logger.error(e);
        }

        return getResponse(false, 1, "获取产品列表失败");
    }

    /**
     * 编辑用户看板
     *
     * @param id
     * @param productIds
     * @param plan
     * @param termLong
     * @param termUnit
     * @param lang
     * @return
     */
    @Path("v1/user-products/edit")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject editTrailUserProduct(
            @NotNull @FormParam("id") final String id,
            @DefaultValue("") @FormParam("products[]") final List<String> productIds,
            @DefaultValue("0") @FormParam("plan") final int plan,
            @DefaultValue("0") @FormParam("term_long") final int termLong,
            @DefaultValue("day") @FormParam("term_unit") final String termUnit,
            @DefaultValue("") @FormParam("lang") final String lang
    ) {
        try {
            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 1, "无编辑权限");
            }
            Document res = ProductUtil.editUserProduct(id, productIds, plan, termLong, termUnit, lang, cuid);
            if (res == null)
                return getResponse(false, "edit user product failed");
            return getResponse(res.containsKey("error"), res.containsKey("error") ? res.get("error") : res.get("success"));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return getResponse(false, 2, "编辑用户产品失败" + StringUtils.showError(e));
        }
    }

    /**
     * 获取用户看板详情
     *
     * @param id
     * @return
     */
    @Path("v1/user-products/detail/{id:[\\d\\w]+}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getUserProductBoards(
            @PathParam("id") String id
    ) {
        try {
            String cuid = getCurrentUserId();
            if (cuid.equals(id) || AccountUtil.isSuperAdmin(cuid)) {
                Document data = ProductUtil.getUserProductBoards(id);
                if (data != null && !data.isEmpty()) {
                    return getResponse(true, data);
                } else {
                    return getResponse(false, 1, "获取用户看板失败");
                }
            } else {
                return getResponse(false, 2, "无查看权限");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return getResponse(false, 3, "获取用户看板详情失败");
    }

    /**
     * 获取用户看板权限状态
     *
     * @return 0:用户未购买产品，1：用户权限到期，2：用户权限未到期
     */
    @Path("v1/user-products/expiry")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getExpiryStatus(
            @DefaultValue("") @QueryParam("category") String category,
            @DefaultValue("minute") @QueryParam("cache_time_unit") String cacheTimeUnit,
            @DefaultValue("5") @QueryParam("cache_expiry") int cacheExpiry
    ) {
        String cuid = getCurrentUserId();
//        int res = ProductUtil.getExpiryStatus(cuid, category);
        long s = System.currentTimeMillis();
        int res = ProductUtil.getCachedExpiryStatus(cuid, category, cacheTimeUnit, cacheExpiry);
        long e = System.currentTimeMillis();
//        logger.info("get expiry status time: " + (e - s) / 1000.0);
        String msg;
        msg = ProductUtil.ExpiryStatus.valueOf(res).name;
        return getResponse(true, res, msg);
    }

    /**
     * 获取预测成果
     *
     * @return
     */
    @Path("v1/products/predictions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getPredictions(
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("") @QueryParam("sort") String sort,
            @DefaultValue("") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("") @QueryParam("lang") String lang
    ) {

        try {
            Document document = ProductUtil.getPredictions(query, sort, order, offset, limit, lang);
            return document == null
                    ? getResponse(false, 2, "get predictions failed")
                    : getResponse(true, document);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return getResponse(false, 1, "get predictions had exceptions");
        }
    }

    /**
     * 设置预测成果
     *
     * @return
     */
    @Path("v1/products/set-predictions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setPredictions(
            @DefaultValue("") @QueryParam("name") String name,
            @DefaultValue("") @QueryParam("name_zh_CN") String nameCN,
            @DefaultValue("") @QueryParam("image") String imageUrl,
            @DefaultValue("0.0") @QueryParam("correct") double correct,
            @DefaultValue("") @QueryParam("unit") String revenueUnit,
            @DefaultValue("0") @QueryParam("rev_exp") int revenueExp,
            @DefaultValue("0") @QueryParam("rev_act") int revenueAct,
            @DefaultValue("") @QueryParam("date_ours") String dateOurs,
            @DefaultValue("") @QueryParam("date_offi") String dateOffi
    ) {

        try {

            if (StringUtils.isEmpty(name)) {
                return getResponse(false, 4, "name can't be null");
            }
            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 3, "no operation authority");
            }
            Document document = ProductUtil.setPredictions(name, nameCN, imageUrl, revenueUnit,
                    dateOurs, dateOffi, revenueExp, revenueAct, correct);
            return document == null
                    ? getResponse(false, 2, "set predictions failed")
                    : getResponse(true, document);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return getResponse(false, 1, "set predictions had exceptions");
        }
    }

    /**
     * 增删用户产品（管理员权限）
     *
     * @param opt
     * @param email
     * @param names
     * @param plan
     * @param lang
     * @return
     */
    @Path("v1/user-product/{opt: (append|remove)}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject editUserProduct(
            @PathParam("opt") String opt,
            @NotNull @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("n") String names,
            @DefaultValue("") @QueryParam("type") String type,
            @DefaultValue("1") @QueryParam("plan") int plan,
            @DefaultValue("1") @QueryParam("term_long") int termLong,
            @DefaultValue("year") @QueryParam("term_unit") String termUnit,
            @DefaultValue("") @QueryParam("lang") String lang

    ) {
        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSuperAdmin(cuid)) {
                getResponse(false, 1, "no operator authority");
            }
            List<String> nameList = StringUtils.parseCSVQueryParam(names);
            List<String> pids;
            if (type.equalsIgnoreCase("all")) {
                pids = ProductUtil.getAllProductIds();
            } else {
                pids = ProductUtil.getProductIdsByNames(nameList);
            }

            Document resData = null;
            switch (opt.toLowerCase()) {
                case "remove":
                    List<String> productsNamesByIds = ProductUtil.getProductsNamesByIds(pids);
                    boolean res = ProductUtil.setUserProductsTerm(
                            AccountUtil.getIdByEmail(email), pids,
                            new Date(), new Date());
                    if (res) {
                        resData = new Document("email", email)
                                .append("total", productsNamesByIds.size())
                                .append("list", productsNamesByIds);
                    } else {
                        resData = new Document("error", "remove user products failed");
                    }
                    break;
                case "append":
                    resData = OrderUtil.createAndPayOrder(email, pids, plan, termLong, termUnit, lang, cuid);
                    break;
                default:
            }

            return resData != null ?
                    getResponse(true, resData) :
                    getResponse(false, 2, opt + "products failed");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 3, StringUtils.showError(e));
        }
    }

    @Path("/v1/user-products/detail")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject detail(
            @DefaultValue("") @QueryParam("email") String email
    ) {
        try {
            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSale(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            if (StringUtils.isEmpty(email)) {
                return getResponse(false, 3, "email cannot be empty");
            }
            int auth = AccountUtil.getUserAuthority(cuid);
            Document res = ProductUtil.getUserProductsDetails(AccountUtil.Authority.valueOf(auth), email);
            if (res == null) {
                return getResponse(false, 2, "get user products details failed");
            }
            return getResponse(true, res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    @Path("v1/product/set-free")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setFreeProducts(
            @DefaultValue("") @QueryParam("start") String start,
            @DefaultValue("") @QueryParam("stop") String stop,
            @DefaultValue("") @QueryParam("category") String category,
            @DefaultValue("") @QueryParam("name") String name
    ) {
        try {

            String cuid = getCurrentUserId();
            if (AccountUtil.isNotSuperAdmin(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            String pattern1 = "yyyy-MM-dd";
            String pattern2 = "yyyy/MM/dd";
            String pattern3 = "yyyyMMdd";
            Date startDate = TimeUtil.strToDate(start, pattern1, pattern2, pattern3);
            Date stopDate = TimeUtil.strToDate(stop, pattern1, pattern2, pattern3);
            List<String> cates = StringUtils.CSV2List(category);
            List<String> names = StringUtils.CSV2List(name);
            if (cates.isEmpty() && names.isEmpty()) {
                return getResponse(false, 2, "param error");
            }
            Document ret = ProductUtil.setFreeProducts(startDate, stopDate, cates, names);
            if (ret == null || ret.containsKey("error_msg")) {
                return getResponse(false, 3, String.valueOf(ret));
            }
            return getResponse(true, ret);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }
}
