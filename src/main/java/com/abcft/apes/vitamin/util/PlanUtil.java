package com.abcft.apes.vitamin.util;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class PlanUtil {

    public static Logger logger = Logger.getLogger(PlanUtil.class);
    public static String COL = MongoUtil.PLAN_COL;

    public enum Plan {
        Basic(0, "Basic"),
        Standard(1, "Standard"),
        Enterprise(2, "Enterprise"),;

        private int id;
        private String name;

        Plan(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static Plan valueOf(int i) {
            for (Plan plan : Plan.values()) {
                if (plan.ordinal() == i)
                    return plan;
            }
            return Basic;
        }

    }

    public enum PriceType {
        PerCompany,
        AllCompany,
    }

    public enum Year {
        Half,
        One,
        Two,
    }

    public static List<Document> getList() {
        return getList(null, null, 0, 0, null, "");
    }

    public static List<Document> getList(String sort, String order, int offset, int limit, String query, String lang) {
        ArrayList<Document> productList = new ArrayList<>();

        List<Bson> conditions = new ArrayList<>();
        if (StringUtils.isEmpty(query)) {
            conditions.add(Filters.regex("name", query, "i"));
        }

        FindIterable<Document> findIterable = MongoUtil.getDBFindIterable(COL, conditions, sort, order, offset, limit);

        findIterable.forEach(new Consumer<Document>() {
            @Override
            public void accept(Document document) {
                Document product = new Document();
                product.append("id", document.get("_id").toString())
                        .append("name", document.get("name" + lang))
                        .append("type", document.get("type"))
                        .append("levels", document.get("levels" + lang))
                        .append("note", document.get("note" + lang));
                productList.add(product);
            }
        });

        return productList;
    }

    /**
     * 获取产品列表
     *
     * @return
     */
    public static Document getPlans(String sort, String order, int offset, int limit, String query, String lang) {
        Document document = new Document();

        List<Document> productList = getList(sort, order, offset, limit, query, lang);
        int total = MongoUtil.getDBCount(COL);
        document.put("total", total);
        document.put("list", productList);

        return document;
    }

    public static Document getPlan(String id) {
        Document document = new Document();

        Document productDoc = MongoUtil.getOneById(COL, id);
        document.put("id", productDoc.getObjectId("_id").toString());
        document.put("name", productDoc.getString("name"));
        document.put("name_zh_CN", productDoc.getString("name_zh_CN"));

        return document;
    }

    public static Document getPlans2(String sort, String order, int offset, int limit, String query, String lang, List<String> pids) {

        Document document = new Document();
        List<Document> productList = new ArrayList<>();
        for (Plan plan : Plan.values()) {
            productList.add(getPlanLevels(pids, plan.name, lang));
        }
        int total = MongoUtil.getDBCount(COL);
        document.put("total", total);
        document.put("list", productList);
        return document;
    }

    public static Document getPlanLevels(List<String> pids, String name, String lang) {
        if (pids == null) {
            pids = new ArrayList<>();
        }
        Document priceRule = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "price_rule");
        int priceType = Plan.valueOf(name).ordinal();
        double totalPrice = getTotalPrice(pids, priceType);
        double discountEnterprise = priceRule.getDouble("discount_enterprise");
        boolean isEnterprise = priceType == 2;
        double temporPrice = totalPrice * (isEnterprise ? discountEnterprise : 1);
        List<Document> levels = new ArrayList<>();
        for (Year year : Year.values()) {
            int i = year.ordinal();
            double yearTimes = Math.pow(2, i);
            double discountYear = Double.valueOf(priceRule.get("discount_year_" + i).toString());
            double price1 = temporPrice * yearTimes;
            double price = price1 * discountYear;
            Document document = new Document("id", i)
                    .append("price", (int) Math.round(price))
                    .append("long", i)
                    .append("long_unit", "year");
            if (i > 0) {
                document.append("price1", (int) Math.round(price1));
            }
            levels.add(document);
        }
        Document planDoc = getPlanByName(name);
        if (planDoc == null) {
            logger.info("plan doc is null");
        }
        Document levelDoc = new Document("id", planDoc.getOrDefault("_id", planDoc.getOrDefault("id", "")))
                .append("name", planDoc.getString("name" + lang))
                .append("type", isEnterprise ? 1 : 0)
                .append("levels", levels)
                .append("note", planDoc.getString("note" + lang));
        return levelDoc;
    }

    public static Document getPlanByName(String name) {
//        logger.info("name: " + name);
        return MongoUtil.getOneByField(COL, "name", name);
    }

    public static String getPlanIdByName(String name) {
        return getPlanByName(name).getObjectId("_id").toString();
    }

    public static double getTotalPrice(List<String> pids, int priceType) {
        if (pids == null || pids.isEmpty()) return 0.0;
        double totalPrice0 = 0.0;
        double totalPrice1 = 0.0;
        List<String> pids0 = new ArrayList<>(pids);
        List<String> pids1 = new ArrayList<>(pids);
        List<String> freeProductsIds = ProductUtil.getFreeProductsIds();
        List<String> freeReportsProductsIds = ReportUtil.getFreeReportsProductsIds();
        pids0.removeAll(freeReportsProductsIds);
        pids1.removeAll(freeProductsIds);
        List<Document> pList0 = ProductUtil.getProducts(pids0);
        List<Document> pList1 = ProductUtil.getProducts(pids1);
        Document priceRule = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "price_rule");
        String defaultDailyPrice = priceRule.get("default_price_daily").toString();
        String defaultBoardPrice = priceRule.get("default_price_board").toString();
        for (Document document : pList0) {
            totalPrice0 += Double.valueOf(document.containsKey("price_0") ? document.get("price_0").toString() : defaultDailyPrice);
        }
        for (Document document : pList1) {
            totalPrice1 += Double.valueOf(document.containsKey("price_1") ? document.get("price_1").toString() : defaultBoardPrice);
        }
        double totalPrice2 = totalPrice0 + totalPrice1;
        return priceType == 0 ? totalPrice0 : priceType == 1 ? totalPrice1 : totalPrice2;
    }
}
