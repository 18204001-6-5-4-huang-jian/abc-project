package com.abcft.apes.vitamin.util;

import com.mongodb.client.FindIterable;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class ProductUtil {
    public static String COL = MongoUtil.PRODUCT_COL;
    public static String USER_PRODUCT_COL = MongoUtil.USER_PRODUCT_COL;
    private static Logger logger = Logger.getLogger(ProductUtil.class);

    /**
     * 编辑用户产品
     *
     * @param id
     * @param productIds
     * @param plan
     */
    public static Document editUserProduct(
            String id, List<String> productIds,
            int plan, int termLong, String termUnit,
            String lang, String adminId
    ) {
        try {
            if (AccountUtil.reset(id)) {
                logger.info("reset account<" + id + "> succeed");
            } else {
                logger.info("reset account<" + id + "> error");
                return new Document("error", "reset account<" + id + "> error");
            }
            productIds.removeIf(String::isEmpty);
            if (productIds.isEmpty()) {
                return new Document("success",
                        "edit user products succeed: pids is empty, just reset account");
            }
            Document document = OrderUtil.createAndPayOrder(
                    AccountUtil.getEmailById(id), productIds,
                    plan, termLong, termUnit, lang, adminId);

            if (document.containsKey("error")) {
                logger.error(document.get("error").toString());
                return document;
            }
            return new Document("success", "edit user products succeed");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new Document("error", e.getMessage());
        }
    }

    public static List<String> getAllProductIds() {
        return getAllProductIds("", true);
    }

    public static List<String> getAllProductIds(String category, boolean containBoardId) {
        return getAllProducts(category, containBoardId)
                .stream()
                .map(document -> String.valueOf(document.getOrDefault("id",
                        document.getOrDefault("_id", null))))
                .collect(Collectors.toList());
    }

    public static List<String> getAllProductNames() {

        List<Document> proDoc = MongoUtil.getDBList(MongoUtil.PRODUCT_COL);
        List<String> res = new ArrayList<>();
        proDoc.forEach(document -> res.add(document.getString("name")));
        return res;
    }

    public static List<String> getAllProductNames(List<Document> products, String lang) {
        List<ObjectId> ids = new ArrayList<>();
        products.forEach(document -> ids.add(new ObjectId(String.valueOf(document.getOrDefault("id",
                document.getOrDefault("_id", null))))));
        List<String> names = new ArrayList<>();
        List<Bson> conds = new ArrayList<>();
        conds.add(in("_id", ids));
        List<Document> proList = MongoUtil.getDBList(MongoUtil.PRODUCT_COL, conds);
        proList.forEach(document -> names.add(org.apache.commons.lang3.
                StringUtils.capitalize(document.getString("name" + lang))));
        return names;
    }

    public static List<String> getAllProductStockCodes() {

        List<Document> proDoc = MongoUtil.getDBList(MongoUtil.PRODUCT_COL);
        List<String> res = new ArrayList<>();
        proDoc.forEach(document -> res.add(document.getString("stock_code")));
        return res;
    }

    public static List<String> getProductsNames(List<Document> productsList) {
        List<String> ids = extractIDListFromDocList(productsList);
        List<String> names = new ArrayList<>();
        ids.forEach(id -> {
            String name = MongoUtil.getOneById(MongoUtil.PRODUCT_COL, id).getString("name");
            names.add(name);
        });
        return names;
    }

    private static List<String> extractIDListFromDocList(List<Document> productsList) {
        List<String> ids = new ArrayList<>();
        productsList.forEach(document -> ids.add(String.valueOf(document.getOrDefault("id",
                document.getOrDefault("_id", null)))));
        return ids;
    }

    public static List<Document> getProducts(List<String> pids) {
        List<ObjectId> poidList = new ArrayList<>();
        pids.forEach(s -> poidList.add(new ObjectId(s)));
        List<Bson> conds = new ArrayList<>();
        conds.add(in("_id", poidList));
        return MongoUtil.getDBList(COL, conds);
    }

    /**
     * 获取预测成果
     *
     * @param query
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param lang
     * @return
     */
    public static Document getPredictions(String query, String sort, String order, int offset, int limit, String lang) {
        try {
            if (!lang.isEmpty() && !lang.startsWith("_")) {
                lang = "_" + lang;
            }
            List<Bson> conds = new ArrayList<>();
            conds.add(exists("date", false));
            if (!query.isEmpty())
                conds.add(or(
                        regex("name" + lang, query, "i"),
                        regex("name", query, "i")
                ));
            List<Document> list = MongoUtil.getDBList(MongoUtil.PREDICTION_COL, conds, sort, order, offset, limit);
            if (list.isEmpty()) {
                logger.error("can't find predictions from db");
                return null;
            }
            list.forEach(document -> {
                document.put("date_release_ours", TimeUtil.date2String(document.getDate("date_release_ours"), "yyyy-MM-dd"));
                document.put("date_release_official", TimeUtil.date2String(document.getDate("date_release_official"), "yyyy-MM-dd"));
            });
            int total = MongoUtil.getDBCount(MongoUtil.PREDICTION_COL, conds);
            return new Document("total", total)
                    .append("list", list);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
            return null;
        }
    }

    /**
     * 获取预测成果
     *
     * @param query
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param lang
     * @return
     */
    public static Document getPredictions2(String query, String sort, String order, int offset, int limit, String lang) {
        try {
            if (!lang.isEmpty() && !lang.startsWith("_")) {
                lang = "_" + lang;
            }
            List<Bson> conds = new ArrayList<>();
            conds.add(exists("date"));
            if (!query.isEmpty())
                conds.add(or(
                        regex("name" + lang, query, "i"),
                        regex("name", query, "i")
                ));
            List<Document> list = MongoUtil.getDBList(MongoUtil.PREDICTION_COL, conds, sort, order, offset, limit);
            if (list.isEmpty()) {
                logger.error("can't find predictions from db");
                return null;
            }
            int total = MongoUtil.getDBCount(MongoUtil.PREDICTION_COL, conds);
            return new Document("total", total)
                    .append("list", list);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
            return null;
        }
    }

    /**
     * 设置预测成果
     *
     * @param name
     * @param nameCN
     * @param imageUrl
     * @param revenueUnit
     * @param dateOurs
     * @param dateOffi
     * @param revenueExp
     * @param revenueAct
     * @param correct
     * @return
     * @throws ParseException
     */
    public static Document setPredictions(
            String name, String nameCN, String imageUrl,
            String revenueUnit, String dateOurs, String dateOffi,
            int revenueExp, int revenueAct, double correct
    ) throws ParseException {
        Document document = new Document("name", name);
        if (!StringUtils.isEmpty(nameCN)) document.append("name_zh_CN", nameCN);
        if (!StringUtils.isEmpty(imageUrl)) document.append("image_url", imageUrl);
        if (!StringUtils.isEmpty(revenueUnit)) document.append("revenue_unit", revenueUnit);
        if (revenueExp != 0) document.append("revenue_expected", revenueExp);
        if (revenueAct != 0) document.append("revenue_actual", revenueAct);
        if (correct != 0.0) document.append("correct", correct);
        if (!StringUtils.isEmpty(dateOurs)) {
            Date dateOursDate = TimeUtil.strToDate(dateOurs, "yyyy-M-d");
            document.append("date_release_ours", dateOursDate);
        }
        if (!StringUtils.isEmpty(dateOffi)) {
            Date dateOffiDate = TimeUtil.strToDate(dateOffi, "yyyy-M-d");
            document.append("date_release_official", dateOffiDate);
        }
        List<Bson> conds = new ArrayList<>();
        conds.add(regex("name", name, "i"));
        boolean res = MongoUtil.upsertOne(MongoUtil.PREDICTION_COL, conds, document);
        return res ? document : null;
    }

    public static List<String> getFreeProductsIds() {
        return getFreeProductsIds("");
    }

    public static List<String> getFreeProductsIds(String category) {
        return getFreeProducts(category)
                .stream()
                .map(document -> document.get("id").toString())
                .collect(Collectors.toList());
    }

    public static List<String> getProductIdsByNames(List<String> nameList) {
        if (nameList == null) {
            return new ArrayList<>();
        }
        List<Bson> conds = new ArrayList<>();
        nameList.removeIf(String::isEmpty);
        if (nameList.isEmpty()) {
            return new ArrayList<>();
        }
        conds.add(in("name", nameList));
        return MongoUtil
                .getDBList(MongoUtil.PRODUCT_COL, conds)
                .stream()
                .map(document -> String.valueOf(document.getOrDefault("id",
                        document.getOrDefault("_id", null))))
                .collect(Collectors.toList());
    }

    public static Document getProducts(String sort, String order, int offset, int limit, String query, String lang, int type, int capitalize) {
        Document document = new Document();
        List<Bson> conditions = new ArrayList<>();
        conditions.add(exists("board_id"));
        if (StringUtils.isEmpty(query)) {
            conditions.add(regex("name", query, "i"));
        }
        if (type != 3) {

            if (type == PlanUtil.Plan.Basic.ordinal()) {
                conditions.add(nin("_id", ReportUtil.getFreeReportsProductsOIds()));
            } else if (type == PlanUtil.Plan.Standard.ordinal()) {
                conditions.add(nin("_id", getFreeProductsOIds()));
            } else if (type == PlanUtil.Plan.Enterprise.ordinal()) {
                List<ObjectId> ninList = ReportUtil.getFreeReportsProductsOIds();
                ninList.retainAll(getFreeProductsIds());
                conditions.add(nin("_id", ninList));
            }
        }
        List<Document> productList = new ArrayList<>();
        if (StringUtils.isEmpty(sort)) {
            sort = "idx";
            order = "asc";
        }
        FindIterable<Document> findIterable = MongoUtil.getDBFindIterable(MongoUtil.PRODUCT_COL, conditions, sort, order, offset, limit);
        findIterable.forEach(new Consumer<Document>() {
            @Override
            public void accept(Document document) {
                String name = StringUtils.isEmpty(lang)
                        ? capitalize == 1
                        ? org.apache.commons.lang3.StringUtils.capitalize(document.getString("name"))
                        : document.getString("name")
                        : document.getString("name" + lang);
                Document product = new Document()
                        .append("id", String.valueOf(document.getOrDefault("_id",
                                document.getOrDefault("id", null))))
                        .append("name", name)
                        .append("image_url", document.getString("image_url"));
                productList.add(product);
            }
        });
        int total = MongoUtil.getDBCount(COL, conditions);
        document.put("total", total);
        document.put("list", productList);

        return document;
    }

    public static List<ObjectId> getFreeProductsOIds() {
        return getFreeProductsIds()
                .stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());
    }

    public static List<String> getUserProductNames(String userId) {
        Date now = new Date();
        List<Bson> conds = Arrays.asList(
                eq("user_id", userId),
                lte("start_time", now),
                gte("stop_time", now));
        return MongoUtil.getDBList(MongoUtil.USER_PRODUCT_COL, conds)
                .stream()
                .map(document -> document.getOrDefault("name", "").toString())
                .collect(Collectors.toList());
    }

    public static List<String> getProductIdsByStockCode(List<String> stockCodes) {
        List<Bson> conds = new ArrayList<>();
        stockCodes.removeIf(String::isEmpty);
        if (stockCodes.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        conds.add(in("stock_code", stockCodes));
        return MongoUtil.getDBList(MongoUtil.PRODUCT_COL, conds)
                .stream()
                .map(document -> document.getOrDefault("id", "").toString())
                .collect(Collectors.toList());
    }

    public static List<String> getFreeProductsNames() {
        return getFreeProducts()
                .stream()
                .map(document -> document.getOrDefault("name", "").toString())
                .collect(Collectors.toList());
    }

    public static List<Document> getFreeProducts() {
        return getFreeProducts("");
    }

    public static List<Document> getFreeProducts(String category) {
        try {
            Document config = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "free_product");
            Date startTime = config.getDate("start_time");
            Date stopTime = config.getDate("stop_time");
            Date now = new Date();
            if (startTime.after(now) || stopTime.before(now)) {
                return Collections.EMPTY_LIST;
            }
            List<String> ctg = config.get("category", ArrayList.class);
            List<String> pids = config.get("pids", ArrayList.class);
            ctg.removeIf(String::isEmpty);
            pids.removeIf(String::isEmpty);
            if (!category.isEmpty()) {
                pids.retainAll(getAllProductIds(category, true));
                ctg.retainAll(Arrays.asList(category));
            }
            List<Bson> conds = new ArrayList<>(Arrays.asList(
                    or(
                            in("_id", pids.stream().map(ObjectId::new)
                                    .collect(Collectors.toList())),
//                            Filters.in("stock_category.0", ctg),
//                            Filters.in("stock_category.1", ctg),
//                            Filters.in("stock_category.2", ctg)
                            in("stock_category", ctg)
                    )
            ));
            return MongoUtil.getDBList(MongoUtil.PRODUCT_COL, conds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.EMPTY_LIST;
        }
    }

    public static List<String> getProductsNamesByIds(List<String> pids) {
        return getProducts(pids)
                .stream()
                .map(document -> document.get("name").toString())
                .collect(Collectors.toList());
    }

    public static Document getUserProductsDetails(AccountUtil.Authority authority, String email) {
        String userId = AccountUtil.getIdByEmail(email);
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        List<Document> freeProducts = getFreeProducts();
        List<Document> userProducts = getProductsMatchUserProducts(userId, "");
//        logger.info("userProducts size: " + userProducts.size());
        List<String> allPids = getAllProductIds();
        List<String> notOwnedPids = new ArrayList<>(allPids);

        Function<? super Document, String> filter1 = document ->
                document.getOrDefault("id", "").toString();
        List<String> freePids = freeProducts.stream().map(filter1).collect(Collectors.toList());
        List<String> uPids = userProducts.stream().map(filter1).collect(Collectors.toList());

        notOwnedPids.removeAll(freePids);
        notOwnedPids.removeAll(uPids);
        List<Document> unpaidProducts = getProducts(notOwnedPids, "");

        Function<? super Document, Document> mapper = document ->
                new Document("id", document.get("id"))
                        .append("name", document.get("name"));
        List<Document> userProductsMap = userProducts.stream().map(mapper).collect(Collectors.toList());
        List<Document> unpaidProductsMap = unpaidProducts.stream().map(mapper).collect(Collectors.toList());
//        logger.info(String.format("%d, %d, %d", freePids.size(), uPids.size(), notOwnedPids.size()));
        Document config = getRecommendBoardsConfig();
        List<String> recommendPids = config.get("pids", ArrayList.class);
        List<Document> recommendProductsMap = getProducts(recommendPids)
                .stream().map(mapper).collect(Collectors.toList());
        List<Document> recommendPCopy = new ArrayList<>(recommendProductsMap);
        Collections.reverse(recommendPCopy);
        recommendProductsMap.removeAll(userProductsMap);
        unpaidProductsMap.removeAll(recommendProductsMap);
        recommendPCopy.forEach(document -> {
            if (userProductsMap.contains(document)) {
                userProductsMap.remove(userProductsMap.indexOf(document));
                userProductsMap.add(0, document);
            }
        });
        if (authority.equals(AccountUtil.Authority.SALE)) {
            Document config1 = getSaleBoardsRangeConfig();
            List<String> rangePids = config1.get("pids", ArrayList.class);
            List<Document> rangeMap = getProducts(rangePids)
                    .stream().map(mapper).collect(Collectors.toList());
            userProductsMap.retainAll(rangeMap);
            unpaidProductsMap.retainAll(rangeMap);
            recommendProductsMap.retainAll(rangeMap);
        }
        return new Document()
                .append("user_board_size", userProductsMap.size())
                .append("user_board", userProductsMap)
                .append("unpaid_board_size", unpaidProductsMap.size())
                .append("unpaid_board", unpaidProductsMap)
                .append("recommend_board_size", recommendProductsMap.size())
                .append("recommend_board", recommendProductsMap)
                ;
    }

    private static Document getSaleBoardsRangeConfig() {
        Document config = MongoUtil
                .getOneByField(MongoUtil.CONFIG_COL, "name", "sale_boards_range");
        if (config == null ||
//                !config.containsKey("max_len") ||
                !config.containsKey("pids") ||
                !(config.get("pids") instanceof List)) {
            config = new Document("pids", new ArrayList<>(Arrays.asList(
                    "590152a1b5d2294d6b773b6d", //MOMO
                    "591962dcc6a32b4f3ed5ba1c",  //TAL
                    "590a9d0b478ad070435324cc",     //Baidu
                    "590a9d33478ad070435324cd",   //CMCM
                    "596743ccfecc8ee195f43ca4",   //EDU
                    "590a9dc2478ad070435324d2",   //vip
                    "590a9d91478ad070435324d0",   //netease
                    "5a0d5f0bd5b21ece0ba791f2"   //ebay
            )))
//                    .append("max_len", 4)
            ;
        }
        return config;
    }

    private static Document getRecommendBoardsConfig() {
        Document config = MongoUtil
                .getOneByField(MongoUtil.CONFIG_COL, "name", "recommend_board");
        if (config == null ||
                !config.containsKey("max_len") ||
                !config.containsKey("pids") ||
                !(config.get("pids") instanceof List)) {
            config = new Document("pids", new ArrayList<>(Arrays.asList(
                    "590152a1b5d2294d6b773b6d", //MOMO
                    "591962dcc6a32b4f3ed5ba1c"  //TAL
            ))).append("max_len", 4);
        }
        return config;
    }

    public static boolean resetUserProductsTerm(String userId, List<String> productIds) {
        List<Bson> conditions = Arrays.asList(
                eq("user_id", userId),
                in("product_id", productIds)
        );
        try {
            MongoUtil.getCollection(MongoUtil.USER_PRODUCT_COL)
                    .find(and(conditions))
                    .forEach(new Consumer<Document>() {
                        @Override
                        public void accept(Document userProductDoc) {
                            String userProductId = userProductDoc.get("_id").toString();
                            List<Document> orders = userProductDoc.get("orders", List.class);
                            Document orderDoc = orders.get(0);

                            int termValue = orderDoc.getInteger("term_long");
                            String termUnit = orderDoc.getString("term_unit");
                            LocalDate startDate = LocalDate.now();
                            LocalDate stopDate = TimeUtil.getStopDate(startDate.plusDays(1), termUnit, termValue);

                            Document document1 = new Document()
                                    .append("start_time", TimeUtil.convertLocalDate2Date(startDate))
                                    .append("stop_time", TimeUtil.convertLocalDate2Date(stopDate));

                            MongoUtil.updateOneById(MongoUtil.USER_PRODUCT_COL, userProductId, document1);
                        }
                    });
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public static boolean setUserProductsTerm(String userId, List<String> pids, Date startTime, Date stopTime) {
        List<Bson> conds = new ArrayList<>();
        conds.add(eq("user_id", userId));
        conds.add(in("product_id", pids));
        Document updateDoc =
                new Document("$set",
                        new Document("start_time", startTime)
                                .append("stop_time", stopTime));
        return MongoUtil.updateMany(MongoUtil.USER_PRODUCT_COL, conds, updateDoc);
    }

    public static void setUserProductLastOrderTime(String userId, Date startDate, Date stopDate) {
        List<Document> upList = getUserProducts(userId, "");
        upList.forEach(document -> {
            String upId = String.valueOf(document.getOrDefault("id",
                    document.getOrDefault("_id", null)));
            List orders = document.get("orders", ArrayList.class);
            Document updateDoc = orders != null && orders.size() > 0
                    ? null : (Document) orders.get(orders.size() - 1);
            if (updateDoc != null) {
                updateDoc.append("start_time", startDate);
                updateDoc.append("stop_time", stopDate);
                MongoUtil.updateOneById(MongoUtil.USER_PRODUCT_COL, upId, updateDoc);
            }
        });
    }

    public static boolean isLegalSalePids(String userId, List<String> pids) {
        Document config = MongoUtil
                .getOneByField(MongoUtil.CONFIG_COL, "name", "recommend_board");
        if (config == null ||
                !config.containsKey("max_len") ||
                !config.containsKey("pids") ||
                !(config.get("pids") instanceof List)) {
            config = new Document("pids", new ArrayList<>(Arrays.asList(
                    "590152a1b5d2294d6b773b6d", //MOMO
                    "591962dcc6a32b4f3ed5ba1c"  //TAL
            )))
                    .append("max_len", 4);
            //2 + x <= 4 policy, if you don't get it, go to ask PM
        }
        int maxLen = config.getInteger("max_len");
        int size = 0;
        if (pids.size() > maxLen) {
            List<Bson> conds = new ArrayList<>(Arrays.asList(
                    eq("user_id", userId)
            ));
            Optional<Document> lastOrderDoc = MongoUtil.getDBList(
                    MongoUtil.ORDER_COL, conds, "create_at", "desc"
            ).stream().findFirst();

            if (lastOrderDoc.isPresent()) {
                Document orderDoc = lastOrderDoc.get();
                String adminId = String.valueOf(orderDoc.getOrDefault("admin_id", ""));
                if (adminId.isEmpty() || AccountUtil.isNotAnalyst(adminId)) {
                    return false;
                }
                List<Document> products = orderDoc.get("products", ArrayList.class);
                size = products.size();     //2 + x <= 4 policy v2.0, if you don't
            } else {
                return false;
            }
        }
        List<String> recommendPids = config.get("pids", ArrayList.class);
        pids.removeAll(recommendPids);
        List<String> calculatedPids = new ArrayList<>(pids);
        int randomBoardMaxSize = maxLen + size - recommendPids.size();
        return calculatedPids.size() <= randomBoardMaxSize;
    }

    public static Map<String, String> getMapOfAllProducts(
            Function<? super Document, String> keyMapper,
            Function<? super Document, String> valMapper
    ) {
        return getMapOfAllProducts(keyMapper, valMapper, document -> true);
    }

    public static Map<String, String> getMapOfAllProducts(
            Function<? super Document, String> keyMapper,
            Function<? super Document, String> valMapper,
            Predicate<? super Document> filter
    ) {
        return ProductUtil
                .getAllProducts("", true)
                .stream().filter(filter)
                .collect(Collectors.toMap(keyMapper, valMapper));
    }

    public static Document getCachedProducts(
            String userId, String sort, String order, int offset, int limit,
            String category, String query, String lang, String timeUnit, int expire) {
        try {
            String in = "user_product_" + userId + "_" + category;
//            logger.info("in: " + in);
            String key = RedisUtil.getRedisKey(in.toLowerCase());

            Document data = RedisUtil.getDocument(key);

            if (data == null) {
                data = getUserProducts(userId, sort, order, offset, limit, category, query, lang);
                RedisUtil.set(key, data, TimeUtil.strToTimeUnit(timeUnit), expire);
            }

            return data;
        } catch (Exception e) {
            logger.error(e);
            return getUserProducts(userId, sort, order, offset, limit, category, query, lang);
        }
    }

    public static String getStockCodes(String pid) {
        return String.valueOf(MongoUtil.getOneById(MongoUtil.PRODUCT_COL, pid)
                .getOrDefault("stock_code", ""));
    }

    public static String getProductsNameById(String pid) {
        return String.valueOf(MongoUtil.getOneById(MongoUtil.PRODUCT_COL, pid)
                .getOrDefault("name", ""));
    }

    public static int getCachedExpiryStatus(String cuid, String category, String timeUnit, int expire) {
        try {

            String key = RedisUtil.getRedisKey("expiry_status_" + cuid + "_" + category);
            String valStr = RedisUtil.getString(key);
            if (valStr == null) {
                int result = getExpiryStatus(cuid, category);
                RedisUtil.set(key, result, TimeUtil.strToTimeUnit(timeUnit), expire);
                return result;
            }
            return Integer.parseInt(valStr);
        } catch (Exception e) {
            logger.error(e);
            return getExpiryStatus(cuid, category);
        }
    }

    public static List<String> getAllProductsIds(List<String> categorys, boolean containBoardId) {

        if (categorys == null) {
            return Collections.EMPTY_LIST;
        }
        categorys.removeIf(String::isEmpty);
        if (categorys.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<Bson> conds = new ArrayList<>();

        List<Bson> orConds = new ArrayList<>();
        categorys.forEach(s -> orConds.add(eq("stock_category", s)));
        conds.add(or(orConds));

        if (containBoardId) {
            conds.add(exists("board_id"));
        }

        Function<? super Document, ? extends String> mapper = document ->
                String.valueOf(document.
                        getOrDefault("id", document.
                                getOrDefault("_id", null)));

        return MongoUtil.getDBList(MongoUtil.PRODUCT_COL, conds)
                .stream().map(mapper).collect(Collectors.toList());
    }

    public static Map<Object, Object> getMapOfUserProducts(
            String userId,
            Function<? super Object, Object> keyMapper,
            Function<? super Object, Object> valMapper,
            Predicate<? super Object> filter
    ) {
        return getUserProducts(userId, "").stream()
                .filter(filter).collect(Collectors.toMap(keyMapper, valMapper));
    }

    public static Document setFreeProducts(Date startDate, Date stopDate, List<String> cates, List<String> names) {

        Document updateDoc = new Document();
        updateDoc.append("start_time", startDate)
                .append("stop_time", stopDate)
                .append("category", cates)
                .append("pids", ProductUtil.getProductIdsByNames(names));

        Document document = MongoUtil.updateOneByField(MongoUtil.CONFIG_COL, "name", "free_product", updateDoc);
        if (document != null) {
            return MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "free_product");
        }
        return new Document("error_msg", "update free_product error");
    }

    public static Document getProduct(String id) {
        Document document = new Document();

        Document productDoc = MongoUtil.getOneById(COL, id);
        document.put("id", productDoc.getObjectId("_id").toString());
        document.put("name", productDoc.getString("name"));

        return document;
    }

    /**
     * 获取用户产品剩余天数
     *
     * @return
     */
    public static long getUserProductRemaining(Document productDoc) {
        int status = getUserProductStatus(productDoc);
        if (status != ProductStatus.Avail.ordinal()) {
            return 0;
        }
//        LocalDate stopDate = TimeUtil.convertDate2LocalDate(productDoc.getDate("stop_time"));
//        LocalDate today = LocalDate.now();
//
//        return ChronoUnit.DAYS.between(today, stopDate);
        return TimeUtil.getDaysCount(new Date(), productDoc.getDate("stop_time"));
    }

    /**
     * 获取用户产品状态
     *
     * @return
     */
    public static int getUserProductStatus(Document productDoc) {
        if (productDoc == null || productDoc.isEmpty()) {
            return ProductStatus.UnOrder.ordinal();
        }
        long start_time = productDoc.getDate("start_time").getTime();
        long stop_time = productDoc.getDate("stop_time").getTime();
        long now_time = new Date().getTime();
        if (now_time < start_time) {
            return ProductStatus.UnAvail.ordinal();
        } else if (now_time >= start_time && now_time < stop_time) {
            return ProductStatus.Avail.ordinal();
        } else {
            return ProductStatus.Expired.ordinal();
        }
    }

    public static List<String> getProductLangs(String productId) {
        Document productDoc = MongoUtil.getOneById(MongoUtil.PRODUCT_COL, productId);
        if (productDoc.containsKey("lang")) {
            return productDoc.get("lang", List.class);
        }

        return null;
    }

    /**
     * 获取用户产品当前有效订单
     *
     * @param userProductId
     * @return
     */
    public static Document getUserProductCurrentOrder(String userProductId) {

        Document userProductDoc = MongoUtil.getOneById(MongoUtil.USER_PRODUCT_COL, userProductId);
        if (userProductDoc == null) {
            logger.warn("user product not exist: " + userProductId);
            return null;
        }

        List<Document> orders = userProductDoc.get("orders", List.class);
        Date now = new Date();
        for (Document order : orders) {
            Date startTime = order.getDate("start_time");
            Date stopTime = order.getDate("stop_time");
            if (now.compareTo(startTime) >= 0 && now.compareTo(stopTime) < 0) {
                Document orderDoc = MongoUtil.getOneById(MongoUtil.ORDER_COL, String.valueOf(order.getOrDefault("id",
                        order.getOrDefault("_id", null))));
                orderDoc.put("right", order.get("right"));
                return orderDoc;
            }
        }

        return null;
    }

    /**
     * 获取用户产品
     *
     * @param userId
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param query
     * @return
     */
    public static Document getUserProducts(
            String userId, String sort, String order, int offset,
            int limit, String category, String query, String lang
    ) {
        Document document = new Document();

        try {
            List<Document> productList = new ArrayList<>();
            Set<ObjectId> pIdList = new HashSet<>();
            int total = 0;

            //账户是否过期
            String email = AccountUtil.getEmailById(userId);
            List<String> fpids = getFreeProductsIds(category);
            Function<? super Document, String> keyMapper = document1 ->
                    String.valueOf(document1.getOrDefault("id",
                            document1.getOrDefault("_id", "")));

            Function<? super Document, Document> valMapper = document1 -> document1;
            Map<String, Document> cateProductsMap =
                    getMapOfProductsMatchUserProducts(userId, category, keyMapper, valMapper);
            if (!AccountUtil.isEmailExpired(email)) {
//                logger.error("!AccountUtil.isEmailExpired(email)");
                List<Bson> conditions = new ArrayList<>();
                conditions.add(eq("user_id", userId));
                conditions.add(gt("stop_time", new Date()));

                if (!StringUtils.isEmpty(query)) {
                    conditions.add(regex("name", query, "i"));
                }


                List<String> catePids = new ArrayList<>(cateProductsMap.keySet());
                catePids.removeAll(fpids);
                if (!catePids.isEmpty()) {
                    conditions.add(in("product_id", catePids));
                    FindIterable<Document> findIterable = MongoUtil
                            .getDBFindIterable(USER_PRODUCT_COL, conditions, sort, order, offset, limit);
                    findIterable.forEach((Consumer<Document>) document1 -> {
                        Document item = new Document();
                        String upId = document1.get("_id").toString();
                        String productId = String.valueOf(document1.get("product_id"));
                        String imageUrl = String.valueOf(
                                document1.getOrDefault("image_url", "/img/logo.png"));
                        String imageUrlNoColor = imageUrl
                                .replaceFirst(".png", "_nocolor.png");
                        item.put("id", upId);
                        item.put("product_id", productId);
                        item.put("name", document1.getString("name" + lang));
                        item.put("status", getUserProductStatus(document1));
                        item.put("remaining", getUserProductRemaining(document1));
                        item.put("image_url", imageUrl);
                        item.put("image_url_nocolor", imageUrlNoColor);
//                        List<String> lang1 = getProductLangs(productId);
                        List<String> lang1 = cateProductsMap.getOrDefault(productId,
                                new Document("lang", Collections.EMPTY_LIST))
                                .get("lang", List.class);
                        if (lang1 != null) {
//                            logger.error("lang is not empty");
                            item.put("lang", lang1);
                        }

                        Document orderDoc = getUserProductCurrentOrder(upId);
                        if (orderDoc != null) {
                            String planId = String.valueOf(orderDoc.getOrDefault("plan_id", ""));
                            Document planDoc = MongoUtil.getOneById(MongoUtil.PLAN_COL, planId);
                            item.put("plan_name", planDoc.getString("name" + lang1));
                        }
                        productList.add(item);
                        pIdList.add(new ObjectId(String.valueOf(document1.get("product_id"))));
                    });
                    total = MongoUtil.getDBCount(USER_PRODUCT_COL, conditions);
                }
            }

//            List<String> fpids = getFreeProductsIds(category);
            List<ObjectId> fpoids = fpids.stream().map(ObjectId::new).collect(Collectors.toList());
            fpoids.removeAll(pIdList);
            List<Bson> conds3 = Arrays.asList(in("_id", fpoids));
            FindIterable<Document> findIterable3 = MongoUtil.getDBFindIterable(MongoUtil.PRODUCT_COL, conds3);
            total += MongoUtil.getDBCount(COL, conds3);
            findIterable3.forEach((Consumer<Document>) document12 -> {
                String imageUrl = String.valueOf(
                        document12.getOrDefault("image_url", "/img/logo.png"));
                String imageUrlNoColor = imageUrl
                        .replaceFirst(".png", "_nocolor.png");
                Document item = new Document();
                String itemId = document12.get("_id").toString();
                item.put("id", itemId);
                item.put("product_id", itemId);
                item.put("name", document12.getString("name"));
                item.put("image_url", imageUrl);
                item.put("image_url_nocolor", imageUrlNoColor);
                item.put("status", ProductStatus.Avail.ordinal());
                item.put("remaining", Integer.MAX_VALUE);
//                List<String> lang1 = getProductLangs(itemId);
                List<String> lang1 = cateProductsMap.getOrDefault(itemId,
                        new Document("lang", Collections.EMPTY_LIST))
                        .get("lang", List.class);
                if (lang1 != null) {
                    item.put("lang", lang1);
                }
                productList.add(item);
                pIdList.add(document12.getObjectId("_id"));
            });

            List<Bson> conditions2 = new ArrayList<>();
            conditions2.add(nin("_id", pIdList));
            if (!StringUtils.isEmpty(query)) {
                conditions2.add(regex("name", query, "i"));
            }
            if (!StringUtils.isEmpty(category)) {
                conditions2.add(
                        or(
                                eq("stock_category.0", category.toUpperCase())));
            }
            if (StringUtils.isEmpty(sort)) {
                sort = "idx";
                order = "asc";
            }

            FindIterable<Document> findIterable2 = MongoUtil
                    .getDBFindIterable(MongoUtil.PRODUCT_COL, conditions2, sort, order, offset, limit);
            findIterable2.forEach(new Consumer<Document>() {
                @Override
                public void accept(Document document) {
                    String imageUrl = String.valueOf(
                            document.getOrDefault("image_url", "/img/logo.png"));
                    String imageUrlNoColor = imageUrl
                            .replaceFirst(".png", "_nocolor.png");
                    Document item = new Document();
                    String itemId = document.get("_id").toString();
                    item.put("id", itemId);
                    item.put("name", document.getString("name"));
                    item.put("image_url", imageUrl);
                    item.put("image_url_nocolor", imageUrlNoColor);
                    item.put("remaining", 0);
                    if (document.containsKey("board_id")) {
                        item.put("status", ProductStatus.UnOrder.ordinal());
                    } else {
                        item.put("status", ProductStatus.ComingSoon.ordinal());
                    }
                    productList.add(item);
                }
            });
            total += MongoUtil.getDBCount(COL, conditions2);
            document.put("total", total);
            document.put("list", productList);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return document;
    }

    public static List<Object> getProductIdsMatchUserProducts(String userId, Class<?> clazz) {
        List<Bson> conds = new ArrayList<>();
        conds.add(eq("user_id", userId));
        List<Object> ids = new ArrayList<>();
        List<Document> list = MongoUtil.getDBList(MongoUtil.USER_PRODUCT_COL, conds);
        if (clazz.isInstance("")) {
            list.forEach(document -> ids.add(String.valueOf(document.get("product_id"))));
        } else if (clazz.isInstance(new ObjectId())) {
            list.forEach(document -> ids.add(new ObjectId(String.valueOf(document.get("product_id")))));
        }
        return ids;
    }

    public static List<String> getProductIdsMatchUserProducts(String userId, String category) {
        return getProductsMatchUserProducts(userId, category)
                .stream()
                .map(document -> String.valueOf(document.get("id")))
                .collect(Collectors.toList());
    }

    public static List<Document> getProductsMatchUserProducts(String userId, String category) {

        List<Bson> conds1 = new ArrayList<>();
        Date today = new Date();
        conds1.add(eq("user_id", userId));
        conds1.add(gte("stop_time", today));
        List<String> upids = MongoUtil.getDBList(MongoUtil.USER_PRODUCT_COL, conds1)
                .stream()
                .map(document -> String.valueOf(document.get("product_id")))
                .collect(Collectors.toList());
        List<Bson> conds = new ArrayList<>();
        conds.add(in("_id", upids
                .stream()
                .map(ObjectId::new)
                .collect(Collectors.toList())));
        if (!StringUtils.isEmpty(category)) {
            conds.add(or(
                    eq("stock_category.0", category.toUpperCase()),
                    eq("stock_category.1", category.toUpperCase()),
                    eq("stock_category.2", category.toUpperCase())
            ));
        }
        return MongoUtil.getDBList(MongoUtil.PRODUCT_COL, conds);
    }

    /**
     * 获取用户产品最后一个订单
     *
     * @return
     */
    public static Document getUserProdctLastOrder(String userProductId) {
        if (StringUtils.isEmpty(userProductId)) {
            return null;
        }

        Document oldUserProductDoc = MongoUtil.getOneById(MongoUtil.USER_PRODUCT_COL, userProductId);
        List<Document> orders = oldUserProductDoc.get("orders", List.class);
        if (orders != null && orders.size() > 0) {
            return orders.get(orders.size() - 1);
        }

        return null;
    }

    public static Document genUserProductOrder(String userProductId, Document orderDoc) {

        int termValue = orderDoc.getInteger("term_long");
        String termUnit = orderDoc.getString("term_unit");
        Date startTime = null, stopTime = null;

        Document lastOrderDoc = getUserProdctLastOrder(userProductId);
        if (lastOrderDoc != null) {
            Date lastStopTime = lastOrderDoc.getDate("stop_time");
            Date now = new Date();
            if (now.compareTo(lastStopTime) < 0) {
                startTime = lastStopTime;
                stopTime = TimeUtil.getStopDate(startTime, termUnit, termValue);
            }
        }
        if (startTime == null) {
            startTime = TimeUtil.convertLocalDate2Date(LocalDate.now());
            stopTime = TimeUtil.getStopDate(
                    TimeUtil.convertLocalDate2Date(
                            LocalDate.now().plusDays(1)), termUnit, termValue);
        }

        String orderId = String.valueOf(orderDoc.getOrDefault("_id", orderDoc.getOrDefault("id", "")));
        String planId = String.valueOf(orderDoc.getOrDefault("plan_id", ""));
        Document planDoc = MongoUtil.getOneById(MongoUtil.PLAN_COL, planId);

        //计算用户产品的起始时间
        Document upOrdersDoc = new Document()
                .append("id", orderId)
                .append("term_long", orderDoc.get("term_long"))
                .append("term_unit", orderDoc.get("term_unit"))
                .append("right", planDoc.get("right"))
                .append("start_time", startTime)
                .append("stop_time", stopTime);

        return upOrdersDoc;
    }

    /**
     * 创建用户产品
     *
     * @return
     */
    public static Document createUserProduct(Document orderDoc, String productId) {

//        String userId = orderDoc.getString("user_id");
//
//        String planId = orderDoc.getString("plan_id");

        //计算用户产品的起始时间
        Document upOrdersDoc = genUserProductOrder(null, orderDoc);
        List<Document> orders = new ArrayList<>();
        orders.add(upOrdersDoc);

        //生成用户看板
        Document productDoc = MongoUtil.getOneById(MongoUtil.PRODUCT_COL, productId);
        if (productDoc == null) {
            return null;
        }
//        String boardId = productDoc.getString("board_id");
//        String newBoardId = DashBoardUtil.copyDashBoard(boardId, userId);

        Document userProductDoc = new Document();
        userProductDoc.append("product_id", productId)
                .append("name", productDoc.getString("name"))
                .append("user_id", orderDoc.get("user_id"))
                .append("image_url", productDoc.getOrDefault("image_url", "/image/default_logo.png"))
                .append("orders", orders)
                .append("start_time", upOrdersDoc.get("start_time"))
                .append("stop_time", upOrdersDoc.get("stop_time"));

        Document newUserProductDoc = MongoUtil.insertOne(MongoUtil.USER_PRODUCT_COL, userProductDoc);

        return newUserProductDoc;
    }

    /**
     * 更新用户产品
     *
     * @return
     */
    public static Document updateUserProduct(String upId, Document orderDoc) {
        Document oldUserProductDoc = MongoUtil.getOneById(MongoUtil.USER_PRODUCT_COL, upId);
        if (oldUserProductDoc == null) {
            return null;
        }

        Document upOrdersDoc = genUserProductOrder(upId, orderDoc);

        List<Document> orders = oldUserProductDoc.get("orders", List.class);
        orders.add(upOrdersDoc);

        Document productDoc = new Document()
                .append("orders", orders)
                .append("stop_time", upOrdersDoc.get("stop_time"));

        Document userProductDoc = MongoUtil.updateOneById(MongoUtil.USER_PRODUCT_COL, upId, productDoc);

        return userProductDoc;
    }

    /**
     * 重设产品起始时间
     *
     * @return
     */
    public static boolean resetUserProductsTerm(String userId) {
        return resetUserProductsTerm(
                userId, getProductIdsMatchUserProducts(userId, ""));
    }

    /**
     * 获取用户产品看板
     *
     * @param upId
     * @param userId
     * @param lang
     * @return
     */
    public static Document getUserProductBoard(String upId, String userId, String lang) {
        List<Bson> conditions = Arrays.asList(
                eq("_id", new ObjectId(upId)),
                eq("user_id", userId)
        );
        Document upDoc = MongoUtil.getOneByConditions(MongoUtil.USER_PRODUCT_COL, conditions);
        int status = getUserProductStatus(upDoc);
//        logger.info("status :  " + status);
        if (status != ProductStatus.Avail.ordinal()) {
            return null;
        }
        String productId = String.valueOf(upDoc.get("product_id"));
        Document productDoc = MongoUtil.getOneById(MongoUtil.PRODUCT_COL, productId);
        if (productDoc == null) {
            return null;
        }
        String boardId = String.valueOf(productDoc.get("board_id" + lang));
        Document board = DashBoardUtil.getDashboard(boardId);
        if (board == null) {
            return null;
        }
        Document resp = new Document()
                .append("id", upId)
                .append("product_id", productId)
                .append("board", board)
                .append("stop_time", TimeUtil.date2String(upDoc.getDate("stop_time")))
                .append("remaining", getUserProductRemaining(upDoc));
        Document order = getUserProductCurrentOrder(upId);
        if (order != null) {
            String planId = String.valueOf(order.get("plan_id"));
            Document planDoc = MongoUtil.getOneById(MongoUtil.PLAN_COL, planId);
            resp.append("plan_name", planDoc.getString("name" + lang))
                    .append("right", planDoc.get("right"));
        }

        return resp;
    }

    public static Document getFreeUserProductBoard(String id, String userId, String lang) {
        if (!isFreeProduct(id)) {
            return null;
        }
        Document upDoc = MongoUtil.getOneById(MongoUtil.PRODUCT_COL, id);
        if (upDoc == null) {
            return null;
        }
        String boardId = String.valueOf(upDoc.get("board_id" + lang));
        Document board = DashBoardUtil.getDashboard(boardId);
        if (board == null) {
            return null;
        }
        Document conf = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "free_product");
        if (conf == null) {
            return null;
        }
        Date stopTime = (Date) conf.getOrDefault("stop_time", new Date());
        Document resp = new Document()
                .append("id", id)
                .append("product_id", id)
                .append("board", board)
                .append("stop_time", stopTime)
                .append("remaining", TimeUtil.getDaysCount(new Date(), stopTime));
//        Document order = getUserProductCurrentOrder(upId);
        String enterprise = PlanUtil.Plan.Enterprise.name();
        Document planDoc = MongoUtil.getOneByField(MongoUtil.PLAN_COL, "name", enterprise);
        if (planDoc == null) {
            logger.error("cannot find plan named: " + enterprise);
            return null;
        }
        resp.append("plan_name", planDoc.getString("name" + lang))
                .append("right", planDoc.get("right"));

        return resp;
    }

    public static boolean isFreeProduct(String id) {
        return getFreeProductsIds().contains(id);
    }

    /**
     * @param upId
     * @param userId
     * @return
     */
    public static Document upgradeUserProduct(String upId, String userId, String planId) {

        Document order = getUserProductCurrentOrder(upId);
        if (order == null) {
            logger.error("");
//            String planId = order.getString("plan_id");
//            Document planDoc = MongoUtil.getOneById(MongoUtil.PLAN_COL, planId);

//            resp.append("plan_name", planDoc.getString("name" + lang))
//                    .append("right", planDoc.get("right"));
        }


        return null;
    }

    /**
     * 获取用户产品看板列表
     *
     * @param userId
     * @return
     */
    public static Document getUserProductBoards(String userId) {
        List<Bson> conds = Arrays.asList(new Document("user_id", userId));
        List<Document> dbList = MongoUtil.getDBList(
                MongoUtil.USER_PRODUCT_COL, conds, "name", "asc");
        List<Document> list = new ArrayList<>();
        Document dataDoc = new Document();
        dbList.forEach((Document productDoc) -> list.add(new Document()
                .append("product_id", productDoc.get("product_id"))
                .append("name", productDoc.getString("name"))
                .append("image_url", productDoc.getString("image_url"))
                .append("start_time", productDoc.getDate("start_time"))
                .append("stop_time", productDoc.getDate("stop_time"))
                .append("create_at", productDoc.getDate("create_at"))
                .append("update_at", productDoc.getDate("update_at"))
                .append("id", productDoc.get("id"))
        ));
        int total = MongoUtil.getDBCount(MongoUtil.USER_PRODUCT_COL, conds);
        dataDoc.append("total", total)
                .append("list", list);
        return dataDoc;

    }

    /**
     * 获取用户看板权限状态
     *
     * @param userId
     * @return
     */
    public static int getExpiryStatus(String userId, String category) {
        long s = System.currentTimeMillis();
        List<String> allProductIds = getAllProductIds(category, true);
        List<String> freeProductsIds = getFreeProductsIds(category);
        List<String> unexpiryPids = getProductIdsMatchUserProducts(userId, category);
        List<String> expiryPids = getUserExpiredProductsIds(userId, category);
        List<String> notPurchasePids = new ArrayList<>(allProductIds);
        List<String> purchasePids = new ArrayList<>(allProductIds);
        unexpiryPids.removeAll(expiryPids);
        freeProductsIds.removeAll(unexpiryPids);
        notPurchasePids.removeAll(freeProductsIds);
        notPurchasePids.removeAll(unexpiryPids);
        purchasePids.removeAll(notPurchasePids);
        int allSize = allProductIds.size();
        int freeSize = freeProductsIds.size();
        int unexSize = unexpiryPids.size();
        int exSize = expiryPids.size();
        int notPurSize = notPurchasePids.size();
        int purSize = purchasePids.size();
//        logger.info(String.format("allSize: %d, freeSize: %d, unexSize: %d," +
//                        " exSize: %d, notPurSize: %d, purSize: %d"
//                , allSize, freeSize, unexSize, exSize, notPurSize, purSize));
        long e = System.currentTimeMillis();
//        logger.info("getExpiryStatus_total_time: " + (e - s) / 1000.0);
        if (freeSize > 0) return ExpiryStatus.Normal.ordinal();                 //有免费看板显示提示
        if (exSize > 0 && unexSize == 0) return ExpiryStatus.Expired.ordinal();//有过购买记录但全部过期
        if (notPurSize == allSize) return ExpiryStatus.NoPurchase.ordinal();    //没有购买记录
        if (notPurSize < allSize) return ExpiryStatus.Normal.ordinal();         //其余
        return ExpiryStatus.Unknown.ordinal();
    }

    private static List<String> getUserExpiredProductsIds(String userId, String category) {
        return getUserExpiredProducts(userId, category)
                .stream()
                .map(document -> document.get("product_id").toString())
                .collect(Collectors.toList());
    }

    private static List<Document> getUserExpiredProducts(String userId, String category) {
        return getUserProducts(userId, category)
                .stream()
                .filter(document -> document.getDate("stop_time").before(new Date()))
                .collect(Collectors.toList());
    }

    private static List<String> getUserNotExpiredProductsIds(String userId, String category) {
        List<String> list = getProductIdsMatchUserProducts(userId, category);
        list.removeAll(getUserExpiredProductsIds(userId, category));
        return list;
    }

    public static List<Document> getUserProducts(String userId, String category) {
        List<Bson> conds = new ArrayList<>();
        List<String> pids = getProductIdsMatchUserProducts(userId, category);
        conds.add(eq("user_id", userId));
        conds.add(in("product_id", pids));
        return MongoUtil.getDBList(MongoUtil.USER_PRODUCT_COL, conds);
    }

    public static List<Document> getAllProducts(String category, boolean containBoardId) {
        List<Bson> conds = new ArrayList<>();
        if (!category.isEmpty()) {
            conds.add(or(
                    eq("stock_category.0", category),
                    eq("stock_category.1", category),
                    eq("stock_category.2", category)
            ));
        }
        if (containBoardId) {
            conds.add(exists("board_id"));
        }
        List<Document> proDoc = MongoUtil.getDBList(MongoUtil.PRODUCT_COL, conds);
        return proDoc;
    }

    public static List<Document> getProducts(String category) {
        return getProducts(Collections.EMPTY_LIST, category);
    }

    /**
     * 获取指定pid的产品列表
     *
     * @param pids
     * @param category
     * @return
     */
    public static List<Document> getProducts(List<String> pids, String category) {
        List<Bson> conds = new ArrayList<>();
        conds.add(in("_id", pids
                .stream()
                .map(ObjectId::new)
                .collect(Collectors.toList())));
        if (!category.isEmpty()) {
            conds.add(or(
                    eq("stock_category.0", category),
                    eq("stock_category.1", category),
                    eq("stock_category.2", category)
            ));
        }
        return MongoUtil.getDBList(COL, conds);
    }

    public static Map<String, String> getMapOfUserProducts(
            String userId,
            Function<? super Document, String> keyMapper,
            Function<? super Document, String> valMapper
    ) {
        List<Document> userProducts = ProductUtil.getUserProducts(userId, "");
        return userProducts.stream().collect(Collectors.toMap(keyMapper, valMapper));
    }

    public static Map<String, Document> getMapOfProductsMatchUserProducts(
            String userId, String category,
            Function<? super Document, String> keyMapper,
            Function<? super Document, Document> valMapper
    ) {
        return getProductsMatchUserProducts(userId, category)
                .stream()
                .collect(Collectors.toMap(keyMapper, valMapper));

    }

    public enum ProductStatus {
        UnOrder,        //未购买
        UnAvail,        //购买, 未生效
        Avail,          //购买, 已生效
        Expired,        //购买, 已过期
        ComingSoon,        //即将推出
    }

    public enum ExpiryStatus {
        NoPurchase(0, "无购买记录"),
        Expired(1, "权限已过期"),
        Normal(2, "权限正常"),
        Unknown(3, "未知权限状态"),
        ;

        public int id;
        public String name;

        ExpiryStatus(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static ExpiryStatus valueOf(int id) {
            for (ExpiryStatus expiryStatus : values()) {
                if (expiryStatus.id == id) {
                    return expiryStatus;
                }
            }
            return Unknown;
        }
    }
}
