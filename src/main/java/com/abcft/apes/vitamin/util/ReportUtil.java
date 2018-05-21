package com.abcft.apes.vitamin.util;

import com.mongodb.client.model.Filters;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Create by myliu on 17-9-12
 */
public class ReportUtil {
    private static Logger logger = Logger.getLogger(ReportUtil.class);

    /**
     * 获取日报
     *
     * @param cuid
     * @param lang
     * @param category
     * @param query
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    public static Document getReports(
            String cuid, String lang, String category, String query,
            String sort, String order, int offset, int limit
    ) {
        try {
            long startTime = new Date().getTime();
            if (!StringUtils.isEmpty(lang)) {
                lang = "_" + lang;
            }
            //获取用户拥有的产品id
            List<String> upids = new ArrayList<>();
            List<Bson> conds = new ArrayList<>();
            if (!StringUtils.isEmpty(category)) {
                conds.add(Filters.eq("category.0", category.toUpperCase()));
            }
            Document userDoc = MongoUtil.json2Document(AccountUtil.getUserById(cuid));
            //获取用户订阅的日报ID
            List<Document> userReports = getUserReports(cuid, true);
            upids.addAll(userReports.stream().map(document -> document.getString("product_id")).collect(Collectors.toList()));
            //用户注册不满一周,可免费收看 或 官方设定的免费期内可以免费收看
            if (checkUserExpiry(userDoc) || isInFreeExpiry(new Date())) {
                List<Document> freeUserReports = getFreeUserReports(category, true);
                upids.addAll(freeUserReports.stream().map(document -> document.getString("product_id")).collect(Collectors.toList()));

            }
            //若用户收看权限到期, 返回过期日报
            boolean expired = false;
            if (upids.isEmpty()) {
                expired = true;
                List<Document> expiredDoc = getUserReports(cuid, false);
                if (expiredDoc == null || expiredDoc.isEmpty()) {
                    expiredDoc = getFreeUserReports(category, false);
                }
                for (Document ed : expiredDoc) {
                    upids.add(ed.getString("product_id"));
                }
                Document firstDoc = expiredDoc.stream()
                        .sorted(Comparator
                                .comparing((Document a) -> a.getDate("stop_time"))
                                .reversed())
                        .findFirst().orElse(new Document());

                //过期日报截止时间,若获取不到则默认为一周以前
                Date stopTime = firstDoc.isEmpty()
                        ? TimeUtil.getRelateDate(Calendar.DAY_OF_MONTH, -7)
                        : firstDoc.getDate("stop_time");
                conds.add(Filters.lt("date", stopTime));
            }
            conds.add(Filters.in("product_id", upids));
            LocalDate queryDate = null;
            boolean isDate = false;
            if (!StringUtils.isEmpty(query)) {
                query = query.trim();
                try {
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                            .parseStrict()
                            .appendPattern("uuuuMMdd")
                            .toFormatter()
                            .withResolverStyle(ResolverStyle.STRICT);
                    queryDate = LocalDate.parse(query, formatter);
                    isDate = true;
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                if (!isDate) {
//                    boolean isAlpha = org.apache.commons.lang3.StringUtils.isAsciiPrintable(query);
//                    String suffix = isAlpha ? "" : "_zh_CN";
                    conds.add(Filters.or(
                            Filters.regex("product_name_zh_CN", query, "i"),
                            Filters.regex("product_name_en", query, "i"),
                            Filters.regex("report_zh_CN", query, "i"),
                            Filters.regex("report_en", query, "i"),
                            Filters.regex("report", query, "i"),
                            Filters.regex("product_code_zh_CN", query, "i"),
                            Filters.regex("product_code", query, "i")
                            )
                    );
                } else if (queryDate != null) {
//                    queryDate = TimeUtil.changeTimeZone(queryDate, ZoneId.of("UTC"), ZoneId.systemDefault());
                    conds.add(Filters.lte("date", Date.from(queryDate.atStartOfDay().toInstant(ZoneOffset.UTC))));
                }
            }
            List<String> freePids = ProductUtil.getFreeProductsIds();
            List<Document> repos = MongoUtil.getDBList(MongoUtil.REPORT_COL, conds, sort, order, offset, limit, new String[]{});
            if (repos.isEmpty()) {
                //获取公共日报（所有用户都可以查看，日期自定）
                List<Document> commonRepos = getCommonRepos(query);
                if (commonRepos != null) {
                    repos.addAll(commonRepos);
                }
                repos = repos.stream().distinct()
                        .sorted(Comparator.comparing(document -> document.getDate("date"),
                                Comparator.reverseOrder()))
                        .collect(Collectors.toList());
            }
            List<Document> repoList = new ArrayList<>();
            List<Bson> conds1 = new ArrayList<>();
            conds1.add(Filters.eq("user_id", cuid));
            Function<? super Document, String> keyMapper = document ->
                    String.valueOf(document.getOrDefault("product_id", ""));
            Function<? super Document, String> valMapper = document ->
                    String.valueOf(document.getOrDefault("id",
                            document.getOrDefault("_id", "")));
            Map<String, String> pidUidMap = ProductUtil.getMapOfUserProducts(cuid, keyMapper, valMapper);

            Function<? super Document, String> keyMapper1 = document ->
                    String.valueOf(document.getOrDefault("id",
                            document.getOrDefault("_id", "")));
            Function<? super Document, String> valMapper1 = document -> {
                List<String> stockCode = document.get("stock_category", List.class);
                if (stockCode != null) {
                    stockCode.removeIf(String::isEmpty);
                    if (!stockCode.isEmpty()) {
                        return stockCode.get(0);
                    }
                }
                return "A";
            };
            Map<String, String> pidStockMap = ProductUtil.getMapOfAllProducts(keyMapper1, valMapper1);
            boolean ispidUidMapEmpty = pidUidMap.isEmpty();

            String timeFilter = "";
//            String reportFilter = "";

            boolean isCN = StringUtils.isCN(lang);
            String defaultReports = isCN ? "今日无异动" : "NO NEWS TODAY";
//            boolean isReportDefault;
            for (Document doc : repos) {
                //调整mongo中的时间时区
                Date rawTime = doc.getDate("date");
                Date time = TimeUtil.changeTimeZone(rawTime, ZoneId.of("UTC"), ZoneId.systemDefault());
                String updateTime = TimeUtil.date2String(time, "yyyy-M-d");
                //时间去重
                if (updateTime.equals(timeFilter)) {
                    updateTime = "";
                } else {
                    timeFilter = updateTime;
                }
                //日报去重
                String report = String.valueOf(doc.getOrDefault("report" + lang,
                        doc.getOrDefault("report", defaultReports)));
//                if (report.equals(reportFilter)) {
//                    report = "";
//                } else {
//                    reportFilter = report;
//                }
//                isReportDefault = report.equals(defaultReports);
//                if (report.isEmpty() || (updateTime.isEmpty() && isReportDefault)) continue;
                String pid = doc.get("product_id").toString();
                String productName = String.valueOf(doc.getOrDefault(
                        "product_name" + lang,
                        doc.getOrDefault("product_name", "")))
                        .toUpperCase();

                Document repo =
//                        isReportDefault ?
//                        new Document("report", report)
//                                .append("update_time", updateTime) :
                        new Document("product_name", productName)
                                .append("product_id", pid)
                                .append("product_code", doc.get("product_code"))
                                .append("report", report)
                                .append("update_time", updateTime)
                                .append("chart_id", doc.getOrDefault("chart_id", ""));
                repo.put("up_id", freePids.contains(pid) ? pid : pidUidMap.getOrDefault(pid, pid));
                repo.put("type", pidStockMap.get(pid));
                repoList.add(repo);
            }
            int total = MongoUtil.getDBCount(MongoUtil.REPORT_COL, conds);
            Document res = new Document("total", total)
                    .append("list", repoList)
                    .append("expired", expired);
            long endTime = new Date().getTime();
//            logger.info("retrieve_report_total_time: " + (endTime - startTime) / 1000.0);
            return res;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取公共日报
     *
     * @return
     */
    public static List<Document> getCommonRepos(String query) {
        try {
            Document config = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "common_report_expiry");
            Date stopTime = config.getDate("stop_time");
            List<Bson> conds = new ArrayList<>(Arrays.asList(
                    Filters.lte("date", stopTime)
            ));
            if (!StringUtils.isEmpty(query)) {
                boolean isAlpha = org.apache.commons.lang3.StringUtils.isAsciiPrintable(query);
                String suffix = isAlpha ? "" : "_zh_CN";
                conds.add(Filters.or(
                        Filters.regex("product_name" + suffix, query, "i"),
                        Filters.regex("report" + suffix, query, "i")
                        )
                );
            }
            return MongoUtil.getDBList(MongoUtil.REPORT_COL, conds);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    private static Document getQuery(final String query) {
        Pattern ymd = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})");
        Pattern wordCN = Pattern.compile("[\\\\u4e00-\\\\u9fa5]+");
        Pattern word = Pattern.compile("\\w+");
        Matcher m1 = ymd.matcher(query);
        Matcher m2 = word.matcher(query);
        Matcher m3 = wordCN.matcher(query);
        String date = "";
        String index = "";
        String content = "";
        if (m1.matches()) {
            date = m1.group();
        }
        if (m2.matches()) {
            index = m2.group();
        }
        if (m1.matches()) {
            content = m3.group();
        }
        Document res = new Document("date", date)
                .append("index", index)
                .append("content", content);
        return res;
    }


    /**
     * 获取免费日报
     *
     * @param category
     * @param active   true:未过期 false:过期
     * @return
     */
    public static List<Document> getFreeUserReports(String category, boolean active) {
        List<Bson> conds = new ArrayList<>();
//        String CATEGORY = category.toUpperCase();
//        if (!StringUtils.isEmpty(category)) {
//            conds.add(Filters.or(
//                    Filters.eq("category.0", CATEGORY),
//                    Filters.eq("category.1", CATEGORY),
//                    Filters.eq("category.2", CATEGORY)
//            ));
//        }
        if (active) {  //返回未过期日报id
            conds.add(Filters.gt("stop_time", new Date()));
        } else {        //返回过期日报id
            conds.add(Filters.lt("stop_time", new Date()));
        }
        conds.add(Filters.eq("active", true));
        List<Document> res = MongoUtil.getDBList(MongoUtil.FREE_REPORT_COL, conds);
        return res;
    }

    /**
     * 获取用户订阅的日报
     *
     * @param active true:未过期 false:过期
     * @param cuid
     * @return
     */
    private static List<Document> getUserReports(String cuid, boolean active) {
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                Filters.eq("user_id", cuid)
        ));
        if (active) {  //返回未过期日报id
            conds.add(Filters.gt("stop_time", new Date()));
        } else {        //返回过期日报id
            conds.add(Filters.lt("stop_time", new Date()));
        }
        List<Document> res = MongoUtil.getDBList(MongoUtil.USER_REPORT_COL, conds);
        return res;
    }

    /**
     * 免费期间
     *
     * @param date
     * @return
     */
    public static boolean isInFreeExpiry(Date date) {
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                Filters.lt("start_time", date),
                Filters.gt("stop_time", date),
                Filters.eq("active", true)
        ));
        Document expiryDoc = MongoUtil.getOneByConditions(MongoUtil.FREE_EXPIRY_COL, conds);
        if (expiryDoc != null) {
            return true;
        }
        return false;
    }

    public static boolean checkUserExpiry(Document userDoc) {
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                Filters.eq("name", "new_user")
        ));
        Document config = MongoUtil.getOneByConditions(MongoUtil.CONFIG_COL, conds);
        int type = config.getInteger("term_unit");
        int amount = config.getInteger("term_long");
        return checkUserExpiry(userDoc, type, amount);
    }

    /**
     * 检查是否为规定期限内的新用户
     *
     * @param userDoc
     * @param type
     * @param amount
     * @return
     */
    private static boolean checkUserExpiry(Document userDoc, int type, int amount) {
        Date createDate = userDoc.getDate("create_at");
        Date relateDate = TimeUtil.getRelateDate(createDate, type, amount);
        return new Date().getTime() - relateDate.getTime() < 0;
    }

    public static boolean upsertUserReport(String userId, Document orderDoc, List<Document> products) {
        for (Document product : products) {
            String productId = product.getString("id");
            List<Bson> conditions = Arrays.asList(
                    Filters.eq("product_id", productId),
                    Filters.eq("user_id", userId)
            );
            Document userReportDoc = MongoUtil.getOneByConditions(MongoUtil.USER_REPORT_COL, conditions);
            //产品是否购买过
            if (userReportDoc == null) {
                createUserReport(orderDoc, productId);
            } else {
                String upId = userReportDoc.get("_id").toString();
                updateUserReport(upId, orderDoc);
            }
        }
        return true;
    }

    private static Document updateUserReport(String upId, Document orderDoc) {
        Document oldUserProductDoc = MongoUtil.getOneById(MongoUtil.USER_REPORT_COL, upId);
        if (oldUserProductDoc == null) {
            return null;
        }

        Document upOrdersDoc = genUserReportOrder(upId, orderDoc);

        List<Document> orders = oldUserProductDoc.get("orders", List.class);
        orders.add(upOrdersDoc);

        Document productDoc = new Document()
                .append("orders", orders)
                .append("stop_time", upOrdersDoc.get("stop_time"));

        Document userProductDoc = MongoUtil.updateOneById(MongoUtil.USER_REPORT_COL, upId, productDoc);

        return userProductDoc;
    }

    private static Document createUserReport(Document orderDoc, String productId) {
        String userId = String.valueOf(orderDoc.get("user_id"));

        String planId = String.valueOf(orderDoc.get("plan_id"));

        //计算用户产品的起始时间
        Document upOrdersDoc = ProductUtil.genUserProductOrder(null, orderDoc);
        List<Document> orders = new ArrayList<>();
        orders.add(upOrdersDoc);

        //生成用户看板
        Document productDoc = MongoUtil.getOneById(MongoUtil.PRODUCT_COL, productId);
        if (productDoc == null) {
            return null;
        }
        Document userProductDoc = new Document();
        userProductDoc.append("product_id", productId)
                .append("name", productDoc.getString("name"))
                .append("user_id", orderDoc.get("user_id"))
                .append("image_url", productDoc.getOrDefault("image_url", "/image/default_logo.png"))
                .append("orders", orders)
                .append("start_time", upOrdersDoc.get("start_time"))
                .append("stop_time", upOrdersDoc.get("stop_time"));

        Document newUserProductDoc = MongoUtil.insertOne(MongoUtil.USER_REPORT_COL, userProductDoc);

        return newUserProductDoc;
    }

    private static Document getUserReportLastOrder(String userProductId) {
        if (StringUtils.isEmpty(userProductId)) {
            return null;
        }

        Document oldUserProductDoc = MongoUtil.getOneById(MongoUtil.USER_REPORT_COL, userProductId);
        List<Document> orders = oldUserProductDoc.get("orders", List.class);
        if (orders != null && orders.size() > 0) {
            return orders.get(orders.size() - 1);
        }

        return null;
    }

    private static Document genUserReportOrder(String userProductId, Document orderDoc) {

        int termValue = orderDoc.getInteger("term_long");
        String termUnit = orderDoc.getString("term_unit");
        Date startTime = null, stopTime = null;

        Document lastOrderDoc = getUserReportLastOrder(userProductId);
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
            stopTime = TimeUtil.getStopDate(TimeUtil.convertLocalDate2Date(LocalDate.now().plusDays(1)), termUnit, termValue);
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
     * 设置免费周
     *
     * @param start
     * @param stop
     * @return
     * @throws ParseException
     */
    public static boolean setFreeExpiry(String start, String stop) throws ParseException {
        String pattern = "yyyy-M-d";
        Date startTime;
        Date stopTime;
        startTime = TimeUtil.strToDate(start, pattern);
        stopTime = TimeUtil.strToDate(stop, pattern);
        Document updateDoc = new Document("start_time", startTime)
                .append("stop_time", stopTime)
                .append("active", true);
        Document unactiveDoc = new Document("$set", new Document("active", false));
        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.exists("start_time"));
        conds.add(Filters.exists("stop_time"));
        boolean res = MongoUtil.updateMany(MongoUtil.FREE_EXPIRY_COL, conds, unactiveDoc);
        if (!res) {
            logger.info("setting free date failed");
            return false;
        }
        Document resDoc = MongoUtil.insertOne(MongoUtil.FREE_EXPIRY_COL, updateDoc);
        if (resDoc == null) {
            logger.info("setting free date failed");
            return false;
        }
        logger.info("setting free date successed");
        return true;
    }

    /**
     * 设置免费日报
     *
     * @param type
     * @param start
     * @param stop
     * @return
     * @throws ParseException
     */
    public static boolean setFreeReports(List<String> names, List<String> categorys, String type, String start, String stop) throws ParseException {
        List<String> pids;
        if (type.equalsIgnoreCase("all")) {
            logger.info("set all reports free");
            pids = ProductUtil.getAllProductIds();
        } else {
            pids = ProductUtil.getProductIdsByNames(names);
            pids.addAll(ProductUtil.getAllProductsIds(categorys, true));
            pids = new ArrayList<>(new HashSet<>(pids));
        }
        //作废之前的免费日报
        Document unactiveDoc = new Document("$set", new Document("active", false));
        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.exists("start_time"));
        conds.add(Filters.exists("stop_time"));
        boolean res = MongoUtil.updateMany(MongoUtil.FREE_REPORT_COL, conds, unactiveDoc);
        if (!res) {
            logger.info("setting free date failed");
            return false;
        }
        //插入新的免费日报
        String pattern = "yyyy-M-d";
        Date startTime;
        Date stopTime;
        startTime = TimeUtil.strToDate(start, pattern);
        stopTime = TimeUtil.strToDate(stop, pattern);
        for (String id : pids) {
            Document updateDoc = new Document("start_time", startTime)
                    .append("stop_time", stopTime)
                    .append("active", true)
                    .append("product_id", id);
            Document resDoc = MongoUtil.insertOne(MongoUtil.FREE_REPORT_COL, updateDoc);
            if (resDoc == null) {
                logger.info("setting free date failed");
                return false;
            }
        }

        logger.info("setting free date successed");
        return true;
    }

    public static List<String> getFreeReportsProductsIds() {
        return getFreeUserReports("", true)
                .stream()
                .map(document -> document.getString("product_id"))
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<ObjectId> getFreeReportsProductsOIds() {
        return getFreeReportsProductsIds()
                .stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());
    }

    /**
     * 获取季报日历
     *
     * @return
     */
    public static Document getQuarterlyReportCalendar(
            Date date, String pid,
            String sort, String order, int offset, int limit,
            String lang
    ) {
        boolean isCN = StringUtils.isCN(lang);
        List<Bson> conds = new ArrayList<>();
        Date stopDate;
        if (date == null) {
            date = TimeUtil.getCurDate();
            stopDate = getQRCalendarStopDate(date);
        } else {
            stopDate = TimeUtil.getRelateDate(date, Calendar.MONTH, 1);
        }
        conds.add(Filters.gte("pub_date", date));
        conds.add(Filters.lt("pub_date", stopDate));

        if (pid != null) {
            conds.add(Filters.regex("stock_code",
                    ProductUtil.getStockCodes(pid), "i"));
//            logger.info(pid);
        }

        List<String> stockCodes = ProductUtil.getAllProductStockCodes();
        conds.add(Filters.in("stock_code", stockCodes));

        List<Document> calendarList = MongoUtil.getDBList(
                MongoUtil.QUARTERLY_REPORT, conds, "pub_date", order, offset, limit);
        if (calendarList == null || calendarList.isEmpty()) {
            return null;
        }

        Function<? super Document, ? extends Document> mapper = document -> {
            Date rawPubDate = document.getDate("pub_date");
            String pubDate = TimeUtil.date2String(rawPubDate, "yyyy-M-d");
            String companyName = String.valueOf(document.
                    getOrDefault("stock_code", ""))
                    .toUpperCase();
            String ect = getECTString(String.valueOf(document.
                    getOrDefault("pub_type", "")), isCN);

            return new Document("date", pubDate)
                    .append("company", companyName)
                    .append("type", ect)
                    .append("update_time1",
                            document.getOrDefault("update_time",
                                    Date.from(Instant.from(Instant.EPOCH.atZone(ZoneId.systemDefault())))));
        };

        List<Document> list = calendarList.stream().map(mapper).collect(Collectors.toList());
        //季报去重
        list = calendarListDateFilter(list);

        return new Document("total", list.size())
                .append("list", list);
    }

    private static List<Document> calendarListDateFilter(List<Document> originList) {
        try {
            List<Document> list = new ArrayList<>(originList);
            List<Document> list2 = new ArrayList<>(originList);
            list.sort(Comparator.comparing(document -> document.getString("company")));
            String stockFilter = "";
            Date dateFilter = Date.from(Instant.from(Instant.EPOCH.atZone(ZoneId.systemDefault())));
            String stockCode;
            Date date;
            Document docFilter = null;
            for (Document doc : list) {
                stockCode = doc.getString("company");
                date = doc.getDate("update_time1");
                if (!stockFilter.equalsIgnoreCase(stockCode)) {
                    docFilter = doc;
                    stockFilter = stockCode;
                    dateFilter = date;
                } else {
                    if (TimeUtil.getDaysCount(date, dateFilter) <= 30) {
                        if (date.before(dateFilter)) {
                            list2.remove(doc);
                        } else {
                            list2.remove(docFilter);
                        }
                    }
                }
            }
            list2.forEach(document -> document.remove("update_time1"));
            return list2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            originList.forEach(document -> document.remove("update_time1"));
            return originList;
        }
    }

    private static Date getQRCalendarStopDate(Date date) {
        Document config = MongoUtil.getOneByField(MongoUtil.CONFIG_COL, "name", "q_r");
        int stopDateCount;
        int stopDateUnit;
        if (config != null) {
            stopDateCount = Integer.parseInt(String.valueOf(config.getOrDefault("stop_date_count", 30)));
            stopDateUnit = TimeUtil.strToCalendarUnit(String.valueOf(config.getOrDefault("stop_date_unit", "day")));
        } else {
            stopDateCount = 30;
            stopDateUnit = Calendar.DAY_OF_YEAR;
        }
        return TimeUtil.getRelateDate(date, stopDateUnit, stopDateCount);
    }

    private static String getECTString(String earnings_call_time, boolean isCN) {
        if (earnings_call_time.matches(".*(?i)before.*")) {
            return isCN ? "盘前" : "Before market";
        } else if (earnings_call_time.matches(".*(?i)after.*")) {
            return isCN ? "盘后" : "After market";
        } else {
            //盘中
            return isCN ? "盘后" : "After market";
        }
    }

    /**
     * 获取季报更新提醒
     *
     * @param userId
     * @param isAnalyst
     * @param lang      @return
     */
    public static Document getQuarterlyReportCalendarNotify(
            String userId, boolean isAnalyst, int flag,
            String sort, String order, int offset, int limit,
            String lang) {
        boolean isCN = StringUtils.isCN(lang);

        //获取更新日期在..内的未检测过的季报信息
        Date startDate = TimeUtil.getCurDate();
        Date stopDate = getQRCalendarStopDate(startDate);
//        logger.info(startDate);
//        logger.info(stopDate);
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                Filters.gte("pub_date", startDate),
                Filters.lt("pub_date", stopDate),
                Filters.in("stock_code", ProductUtil.getAllProductStockCodes())
        ));
        List<Document> list = MongoUtil.getDBList(
                MongoUtil.QUARTERLY_REPORT, conds, sort, order, offset, limit);
//        logger.info(Arrays.toString(list.stream().map(document -> document.get("quarterly_report", Document.class)).collect(Collectors.toList()).toArray()));
        //获取 product_id ：user_product Document 散列表
        Function<? super Object, Object> keyMapper = document ->
                String.valueOf(((Document) document).getOrDefault("product_id", ""));
        Function<? super Object, Object> valMapper = document -> document;
        Map<Object, Object> upMap = ProductUtil.getMapOfUserProducts(userId, keyMapper, valMapper, o -> true);
        //获取 stock_code : product_id 散列表
        Function<? super Document, String> keyMapper1 = document ->
                String.valueOf(document.getOrDefault("stock_code", ""));
        Function<? super Document, String> valMapper1 = document ->
                String.valueOf(document.getOrDefault("id", ""));
        Predicate<? super Document> filter = document ->
                !String.valueOf(document.getOrDefault("stock_code", "")).isEmpty();
        Map<String, String> stockCodeIdMap = ProductUtil.getMapOfAllProducts(keyMapper1, valMapper1, filter);
        //获取常量
        String analystNotify = isCN ?
                "今日已公布，请分析师及时更新季报数据并调整模型" :
                "Earnings report is released today please update " +
                        "the quarterly data and adjust your model input in time";
        String pubDateFormat = isCN ? "yyyy-M-d" : "yyyy/M/d";
        String updateTimePattern = "yyyy-M-d HH:mm";
        List<Document> analystList = new ArrayList<>();
        Function<? super Document, Document> mapper = document -> {
            //季报发布日期，格式
            Date rawPubDate = document.getDate("pub_date");

            String pubDate = TimeUtil.date2String(rawPubDate, pubDateFormat, ZoneId.systemDefault());

            //获取公司名, name优先级最高，能直接匹配product表中数据
            String stockCode = String.valueOf(document.getOrDefault("stock_code", ""));

            //获取季报发布时间在开盘前后：盘前/盘后
            String openMarket = String.valueOf(document.
                    getOrDefault("pub_type", ""));
            String ect = getECTString(openMarket, isCN);

            //获取季报名称，如:2017Q1季报
            String qrName = getQuarterlyReportSimpleName(document, isCN);
            //获取用户产品id，供前端获取并跳转至相应看板
            String pid = stockCodeIdMap.get(stockCode);
//            logger.info(upMap.containsKey(pid));
            Document upDoc = (Document) upMap.get(pid);
            String upId = upDoc == null ? null : String.valueOf(upDoc.getOrDefault("id", ""));
            //获取季报更新时间
            Date rawUpdateTime = document.getDate("update_date");
            String updateTime = TimeUtil.date2String(
                    rawUpdateTime, updateTimePattern, ZoneId.systemDefault());
//            logger.info(upDoc);
            int status = ProductUtil.getUserProductStatus(upDoc);
            Document resDoc = new Document("date", pubDate)
                    .append("title", qrName)
                    .append("company", stockCode)
                    .append("type", ect)
                    .append("up_id", upId)
                    .append("remaining", upId == null ? 0 : 1)
                    .append("product_id", pid)
                    .append("status", status)
                    .append("update_time", updateTime)
                    .append("update_time1", rawUpdateTime)
                    .append("analyst_notify", "");

            //若当前用户是分析师，则返回以下信息
//            logger.info(isAnalyst + " " + rawPubDate);
            if (isAnalyst && TimeUtil.isToday(rawPubDate)) {
//                logger.info("is today");
                Document analystDoc = Document.parse(resDoc.toJson());
                analystDoc.append("analyst_notify", analystNotify);
                analystList.add(analystDoc);
            }
            return resDoc;
        };


        list = list.stream().map(mapper).filter(Objects::nonNull).collect(Collectors.toList());
        list.addAll(analystList);
        list = calendarListDateFilter(list);    //去除重复的季报
        return flag == 0 ?
                new Document("total", list.size())
                        .append("list", list) :
                new Document("total", 0)
                        .append("list", list);
    }


    /**
     * 获取季报的名称: yyyyQMxx
     * 例如：2017Q3 季报
     * 2018Q1 Quarterly Report
     *
     * @param qr
     * @param isCN
     * @return
     */
    private static String getQuarterlyReportSimpleName(Document qr, boolean isCN) {
        return isCN ? "最新一期季报" : "the latest earnings report";
    }

    public static Document getQuarterlyReportCalendarHead(String lang) {
        boolean isCN = StringUtils.isCN(lang);
        String thirdListHead = isCN ? "盘前/盘后" : "Before/After market";
        //获取月份表
        Document param = getStartAndStopParam(false);
        List<String> dateList = getMonthList(
                param.getDate("start_date"),
                param.getDate("stop_date"),
                isCN);

//        logger.info(Arrays.toString(dateList.toArray()));

        Function<? super Document, Document> mapper = document -> new Document()
                .append("id", String.valueOf(document
                        .getOrDefault("id", document
                                .getOrDefault("_id", null))))
                .append("name", String.valueOf(document
                        .getOrDefault("stock_code", null)));

        //获取产品表
        List<Bson> conds = new ArrayList<>(Arrays.asList(
                Filters.exists("board_id"),
                Filters.exists("stock_code"),
                Filters.ne("stock_code", "")
        ));
        List<Document> nameList = MongoUtil
                .getDBList(MongoUtil.PRODUCT_COL, conds)
                .stream().map(mapper).collect(Collectors.toList());
//        nameList.add(0, new Document("id", "").append("name", nameListHead));
//        logger.error(companyList.size());
        return new Document("dates", dateList)
                .append("names", nameList)
                .append("pan", thirdListHead);
    }

    private static Document getStartAndStopParam(boolean cached) {
        String key = RedisUtil.getRedisKey(Thread.currentThread().getStackTrace()[1].getMethodName());
        if (cached) {
            Document cache = RedisUtil.getDocument(key);
            if (cache != null) {
                return cache;
            }
        }
        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.in("stock_code", ProductUtil.getAllProductStockCodes()));
        String sort = "pub_date";
        String order = "asc";
        List<Document> qrs = MongoUtil.getDBList(MongoUtil.QUARTERLY_REPORT, conds, sort, order);
        if (qrs == null || qrs.isEmpty()) {
//            logger.info("qrs null");
            return new Document("start_date", null)
                    .append("stop_date", null);
        }
        String PB = "pub_date";
        Document startDoc = qrs.get(0);
        Document stopDoc = qrs.get(qrs.size() - 1);
        Date startDate = (Date) startDoc.getOrDefault(PB, null);
        Date stopDate = (Date) stopDoc.getOrDefault(PB, null);
        Document result = new Document("start_date", startDate)
                .append("stop_date", stopDate);
        RedisUtil.set(key, result, TimeUnit.MINUTES, 5);
        return result;
    }

    private static List<String> getMonthList(Date startDate, Date stopDate, boolean isCN) {
        List<String> dateList = new ArrayList<>();
        if (startDate == null || stopDate == null) {
//            logger.info("param null");
            startDate = new Date();
            stopDate = TimeUtil.getRelateDate(Calendar.YEAR, 1);
        }
        Date now = new Date();
        LocalDate start;
        if (startDate.after(now)) {
            startDate = now;
        }
        start = LocalDate.from(startDate.toInstant().atZone(ZoneId.systemDefault()));
        LocalDate stop = LocalDate.from(stopDate.toInstant().atZone(ZoneId.systemDefault()));
        start = LocalDate.of(start.getYear(), start.getMonthValue(), 1);
        stop = LocalDate.of(stop.getYear(), stop.getMonthValue(), stop.lengthOfMonth());
//        logger.info(start.format(DateTimeFormatter.ofPattern("yyyy MM")));
//        logger.info(stop.format(DateTimeFormatter.ofPattern("yyyy MM")));
//        LocalDate test = stop.plusMonths(1);
        String pattern = isCN ? "yyyy年M月" : "MMM yyyy";
        Locale locale = isCN ? Locale.CHINA : Locale.ENGLISH;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, locale);
        for (LocalDate localDate = start; localDate.compareTo(stop) <= 0; localDate = localDate.plusMonths(1)) {
            dateList.add(localDate.format(dateTimeFormatter));
        }
        return dateList;
    }

    public static Document getCachedReports(
            String cuid, String lang, String category,
            String query, String sort, String order,
            int offset, int limit,
            String cacheTimeUnit, int cacheExpire
    ) {
        try {

            String key = RedisUtil.getRedisKey(Thread.currentThread().getStackTrace()[1].getMethodName()
                    + cuid + lang + category + query + sort + order + offset + limit);
            Document data = RedisUtil.getDocument(key);
            if (data == null) {
                data = getReports(cuid, lang, category, query, sort, order, offset, limit);
                RedisUtil.set(key, data, TimeUtil.strToTimeUnit(cacheTimeUnit), cacheExpire);
            }
            return data;
        } catch (Exception e) {
            logger.error(e);
            return getReports(cuid, lang, category, query, sort, order, offset, limit);
        }
    }

}
