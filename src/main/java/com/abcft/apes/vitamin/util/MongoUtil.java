package com.abcft.apes.vitamin.util;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import com.udojava.evalex.Expression;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

public class MongoUtil {
    private static Logger logger = Logger.getLogger(MongoUtil.class);

    private static MongoClient mongo = null;
    private static MongoDatabase database = null;

    public static String ACCOUNT_COL = "account";               //用户帐号
    public static String WX_USER_COL = "wx_user";               //微信用户帐号
    public static String DASHBOARD_COL = "dashboard";           //看板
    public static String DASHBOARD_TMPL_COL = "dashboard_tmpl"; //看板
    public static String CHART_COL = "charts";                  //图表
    public static String CHART_DESP_COL = "chart_desp";         //图表描述
    public static String CHART_DATA_COL = "charts_data";           //看板
    public static String ORDER_COL = "order";                   //订单
    public static String PRODUCT_COL = "product";               //产品
    public static String USER_PRODUCT_COL = "user_product";   //已订购的产品
    public static String PLAN_COL = "plan";                     //方案
    public static String INVITE_CODE_COL = "invite_code";       //邀请码
    public static final String CHART_DATA_MARKER_COL = "charts_data_marker"; //数据点标记
    public static String COMMENT_COL = "comment";               //评论
    public static String COMMENT_LIKE_COL = "comment_like";     //评论点赞
    public static String COMMENT_READ_COL = "comment_consume_time";  //评论读取时间
    public static String FAMOUS_PERSON_COL = "famous_person";  //评论读取时间
    public static String USER_ANON_NICKNAME_COL = "user_anon_nickname";  //用户匿名昵称
    public static String REPORT_COL = "report";                     //日报
    public static String USER_REPORT_COL = "user_report";           //用户日报
    public static String FREE_REPORT_COL = "free_report";           //免费日报
    public static String FREE_EXPIRY_COL = "free_expiry";           //免费期间
    public static String CONFIG_COL = "config";                     //官方配置
    public static String TRANSFER_NOTICE_COL = "transfer_notice";     //汇款通知
    public static final String PREDICTION_COL = "prediction";       //首页预测成果展示
    //    public static final String QUARTERLY_REPORT = "quarterly_report_temp"; //季报
    public static final String QUARTERLY_REPORT = "earning_day"; //季报

//    public static void init(String connStr, String dbName) {
//        MongoClientURI connectionString = new MongoClientURI(connStr);
//        mongo = new MongoClient(connectionString);
//        database = mongo.getDatabase(dbName);
//
//        initDb();
//    }

    public static void init(String host, String portStr, String db, String user, String password) {
        MongoCredential credential = MongoCredential.createCredential(user, db, password.toCharArray());
        int port = Integer.parseInt(portStr);
        mongo = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
        database = mongo.getDatabase(db);//获取数据库
        initDb();
    }

    private static void initDb() {
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public static MongoCollection<Document> getCollection(String col) {
        return database.getCollection(col);
    }

    public static Document json2Document(JsonObject object) {
        if (object.containsKey("id")) {
            String oid = object.getString("id");
            Document doc = Document.parse(object.toString());
            doc.remove("id");
            return doc.append("_id", new ObjectId(oid));
        } else {
            return Document.parse(object.toString());
        }
    }

    public static JsonObject document2Json(Document doc) {
        if (doc == null) {
            return null;
        }
        if (doc.containsKey("_id")) {
            Object oid = doc.remove("_id");
            doc.append("id", oid.toString());
        }
        return StringUtils.parseJson(JSON.serialize(doc));
    }

    /*
    * */
    public static String saveOne(Document doc, String table) {
        try {
            MongoCollection<Document> collection = database.getCollection(table);
            collection.insertOne(doc);

            return doc.get("_id", ObjectId.class).toString();
        } catch (Exception e) {
            logger.error("save one to db error: " + e.getMessage());
        }
        return null;
    }

    public static boolean deleteOne(String col, long id) {
        MongoCollection<Document> collection = database.getCollection(col);

        DeleteResult dr = collection.deleteMany(eq("_id", id));
        if (dr != null) {
            return dr.getDeletedCount() > 0;
        }

        return false;
    }


    public static boolean deleteOne(String col, List<Bson> conditions) {
        MongoCollection<Document> collection = database.getCollection(col);

        DeleteResult result = collection.deleteOne(and(conditions));
        if (result == null) {
            logger.error(String.format("delete one from col %s erorr: ", col));
            return false;
        }

        return (result.getDeletedCount() > 0);
    }

    public static boolean deleteMany(String col, String field, Object value) {
        MongoCollection<Document> collection = database.getCollection(col);

        DeleteResult dr = collection.deleteMany(eq(field, value));

        if (dr != null) {
            return dr.getDeletedCount() >= 0;
        }

        return false;
    }

    public static boolean deleteMany(String col, List<Bson> conds) {
        MongoCollection<Document> collection = database.getCollection(col);

        DeleteResult result = collection.deleteMany(and(conds));
        if (result == null) {
            logger.error(String.format("delete many from col %s erorr: ", col));
            return false;
        }

        return (result.getDeletedCount() > 0);
    }

    public static boolean deleteOne(String col, String id) {
        MongoCollection<Document> collection = database.getCollection(col);

        DeleteResult dr = collection.deleteMany(eq("_id", new ObjectId(id)));

        if (dr != null) {
            return dr.getDeletedCount() >= 0;
        }

        return false;
    }

    public static JsonObject getDbJsonList(
            String col,
            String sort,
            String order,
            int offset,
            int limit
    ) {

        return getDbJsonList(col, null, sort, order, offset, limit);
    }

    /**
     * @param col
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param conditions
     * @return
     */
    public static JsonObject getDbJsonList(
            String col,
            List<Bson> conditions,
            String sort,
            String order,
            int offset,
            int limit
    ) {

        return getDbJsonList(col, sort, order, offset, limit, conditions, null);
    }

    /**
     * @param col
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param conditions
     * @param ignoreFields
     * @return
     */
    public static JsonObject getDbJsonList(
            String col,
            String sort,
            String order,
            int offset,
            int limit,
            List<Bson> conditions,
            String[] ignoreFields
    ) {
        Document document = getDBDocList(col, sort, order, offset, limit, conditions, ignoreFields);

        return MongoUtil.document2Json(document);
    }


    /**
     *
     **/
    public static Document getDBDocList(
            String col,
            String sort,
            String order,
            int offset,
            int limit
    ) {
        return getDBDocList(col, sort, order, offset, limit, null, null);
    }

    /**
     *
     **/
    public static Document getDBDocList(
            String col,
            String sort,
            String order,
            int offset,
            int limit,
            List<Bson> conditions
    ) {
        return getDBDocList(col, sort, order, offset, limit, conditions, null);
    }

    /**
     *
     **/
    public static Document getDBDocList(
            String col,
            String sort,
            String order,
            int offset,
            int limit,
            List<Bson> conditions,
            String[] ignoreFields
    ) {
        Document document = new Document();

        List<Document> docList = getDBList(col, conditions, sort, order, offset, limit, ignoreFields);

        document.append("total", getDBCount(col, conditions));
        document.append("list", docList);

        return document;
    }

    public static List<Document> getDBList(
            String col
    ) {
        return getDBList(col, null, "", "");
    }

    public static List<Document> getDBList(
            String col,
            List<Bson> conditions
    ) {
        return getDBList(col, conditions, "", "");
    }

    public static List<Document> getDBList(
            String col,
            List<Bson> conditions,
            String sort,
            String order
    ) {
        return getDBList(col, conditions, sort, order, 0, 0, null);
    }

    public static List<Document> getDBList(
            String col,
            String sort,
            String order,
            String sort2,
            String order2,
            int offset,
            int limit,
            List<Bson> conditions
    ) {
        return getDBList(col, conditions, sort, order, sort2, order2, offset, limit, null);
    }

    public static List<Document> getDBList(
            String col,
            List<String> sort,
            String order,
            int offset,
            int limit,
            List<Bson> conditions
    ) {
        return getDBList(col, conditions, sort, order, offset, limit, null);
    }

    public static List<Document> getDBList(
            String col,
            String sort,
            String order,
            int offset,
            int limit,
            List<Bson> conditions
    ) {
        return getDBList(col, conditions, sort, order, offset, limit, null);
    }

    public static List<Document> getDBList(
            String col,
            String sort,
            String order,
            String sort2,
            String order2,
            int offset,
            int limit
    ) {
        return getDBList(col, null, sort, order, sort2, order2, offset, limit, null);
    }

    public static List<Document> getDBList(
            String col,
            String sort,
            String order,
            int offset,
            int limit
    ) {
        return getDBList(col, null, sort, order, offset, limit, null);
    }

    /**
     * @param col
     * @param conditions
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    public static JsonArray getDBJsonArray(
            String col,
            List<Bson> conditions,
            String sort,
            String order,
            int offset,
            int limit
    ) {
        return getDBJsonArray(col, conditions, sort, order, offset, limit, null);
    }

    /**
     * @param col
     * @param conditions
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param ignoreFields
     * @return
     */
    public static JsonArray getDBJsonArray(
            String col,
            List<Bson> conditions,
            String sort,
            String order,
            int offset,
            int limit,
            String[] ignoreFields
    ) {

        JsonArrayBuilder builder = Json.createArrayBuilder();

        FindIterable<Document> fi = getDBFindIterable(col, conditions, sort, order, offset, limit);
        MongoCursor<Document> cursor = fi.iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                if (ignoreFields != null) {
                    for (String field : ignoreFields) {
                        doc.remove(field);
                    }
                }
                builder.add(MongoUtil.document2Json(doc));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(" get db list 2 error: " + e.getMessage());
        } finally {
            cursor.close();
        }

        return builder.build();
    }

    public static FindIterable<Document> getDBFindIterable(
            String col,
            List<Bson> conditions
    ) {
        return getDBFindIterable(col, conditions, new ArrayList<>(), "", 0, Integer.MAX_VALUE, null);
    }

    public static FindIterable<Document> getDBFindIterable(
            String col,
            List<Bson> conditions,
            List<String> sort,
            String order
    ) {
        return getDBFindIterable(col, conditions, sort, order, 0, Integer.MAX_VALUE, null);
    }

    public static FindIterable<Document> getDBFindIterable(
            String col,
            List<Bson> conditions,
            String sort,
            String order
    ) {
        return getDBFindIterable(col, conditions, Arrays.asList(sort), order, 0, Integer.MAX_VALUE, null);
    }


    public static FindIterable<Document> getDBFindIterable(
            String col,
            List<Bson> conditions,
            List<String> sort,
            String order,
            int offset,
            int limit
    ) {
        return getDBFindIterable(col, conditions, sort, order, offset, limit, null);
    }

    public static FindIterable<Document> getDBFindIterable(
            String col,
            List<Bson> conditions,
            String sort,
            String order,
            int offset,
            int limit
    ) {
        return getDBFindIterable(col, conditions, sort, order, null, null, offset, limit, null);
    }

    public static FindIterable<Document> getDBFindIterable(
            String col,
            List<Bson> conditions,
            String sort,
            String order,
            String sort2,
            String order2,
            int offset,
            int limit
    ) {
        return getDBFindIterable(col, conditions, sort, order, sort2, order2, offset, limit, null);
    }

    public static FindIterable<Document> getDBFindIterable(
            String col,
            List<Bson> conditions,
            List<String> sort,
            String order,
            int offset,
            int limit,
            List<String> fields
    ) {
        MongoCollection<Document> collection = getCollection(col);

        FindIterable<Document> fi = collection.find();
        if (conditions != null && conditions.size() > 0) {
            fi = fi.filter(Filters.and(conditions));
        }
        if (sort != null) {
            fi = fi.sort(getSortOrder(sort, order));
        } else {
            fi = fi.sort(getSortOrder("_id", "asc"));
        }
        if (fields != null) {
            fi = fi.projection(Projections.include(fields));
        }

        fi = fi.skip(offset).limit(limit);
        return fi;
    }

    public static FindIterable<Document> getDBFindIterable(
            String col,
            List<Bson> conditions,
            String sort1,
            String order1,
            String sort2,
            String order2,
            int offset,
            int limit,
            List<String> fields
    ) {
        MongoCollection<Document> collection = getCollection(col);

        FindIterable<Document> fi = collection.find();
        if (conditions != null && conditions.size() > 0) {
            fi = fi.filter(Filters.and(conditions));
        }
        if (!StringUtils.isEmpty(sort1)) {
            fi = fi.sort(getSortOrder(sort1, order1));
        }
        if (!StringUtils.isEmpty(sort2)) {
            fi = fi.sort(getSortOrder(sort2, order2));
        } else if (StringUtils.isEmpty(sort1)) {
            fi = fi.sort(getSortOrder("_id", "asc"));
        }
        if (fields != null) {
            fi = fi.projection(Projections.include(fields));
        }

        fi = fi.skip(offset).limit(limit);
        return fi;
    }

    public static Bson getSortOrder(List<String> sort, String order) {
        return order.equals("asc") ? ascending(sort) : descending(sort);
    }

    public static Bson getSortOrder(String sort, String order) {
        return order.equals("asc") ? ascending(sort) : descending(sort);
    }

    public static List<Document> getDBList(
            String col,
            List<Bson> conditions,
            String sort,
            String order,
            int offset,
            int limit,
            String[] ignoreFields
    ) {

        return getDBList(col, conditions, sort, order, null, null, offset, limit, ignoreFields);
    }

    public static List<Document> getDBList(
            String col,
            List<Bson> conditions,
            List<String> sort,
            String order,
            int offset,
            int limit,
            String[] ignoreFields
    ) {
        FindIterable<Document> fi = getDBFindIterable(col, conditions, sort, order, offset, limit);

        MongoCursor<Document> cursor = fi.iterator();
        List<Document> docList = new ArrayList<>();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                if (ignoreFields != null) {
                    for (String field : ignoreFields) {
                        doc.remove(field);
                    }
                }
                if (doc.containsKey("_id")) {
                    String oid = doc.get("_id").toString();
                    doc.put("id", oid);
                    doc.remove("_id");
                }
                docList.add(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(" get db list 2 error: " + e.getMessage());
        } finally {
            cursor.close();
        }

        return docList;
    }

    public static List<Document> getDBList(
            String col,
            List<Bson> conditions,
            String sort,
            String order,
            String sort2,
            String order2,
            int offset,
            int limit,
            String[] ignoreFields
    ) {
        FindIterable<Document> fi = getDBFindIterable(col, conditions, sort, order, sort2, order2, offset, limit);

        MongoCursor<Document> cursor = fi.iterator();
        List<Document> docList = new ArrayList<>();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                if (ignoreFields != null) {
                    for (String field : ignoreFields) {
                        doc.remove(field);
                    }
                }
                if (doc.containsKey("_id")) {
                    String oid = doc.get("_id").toString();
                    doc.put("id", oid);
                    doc.remove("_id");
                }
                docList.add(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(" get db list 2 error: " + e.getMessage());
        } finally {
            cursor.close();
        }

        return docList;
    }

    public static int getDBCount(String col, List<Bson> conditions) {
        MongoCollection<Document> collection = database.getCollection(col);
        if (conditions != null && conditions.size() > 0) {
            return (int) collection.count(Filters.and(conditions));
        }
        return (int) collection.count();
    }

    public static int getDBCount(String col) {
        return getDBCount(col, null);
    }

    public static JsonObject getJsonById(String col, String id) {
        MongoCollection<Document> collection = database.getCollection(col);
        Document doc = collection.find(eq("_id", new ObjectId(id))).first();
        return MongoUtil.document2Json(doc);
    }

    public static JsonObject getJsonByField(String col, String field, String value) {
        Document doc = getOneByField(col, field, value);
        return MongoUtil.document2Json(doc);
    }

    public static JsonObject getJsonByCondtions(String col, List<Bson> conditions) {
        return getJsonByCondtions(col, conditions, null);

    }

    public static JsonObject getJsonByCondtions(String col, List<Bson> conditions, List<String> fields) {
        Document doc = getOneByConditions(col, conditions, fields);
        return MongoUtil.document2Json(doc);
    }

    public static Document getOneById(String col, long id) {
        return getOneByField(col, "_id", id);
    }

    public static Document getOneById(String col, long id, List<String> fields) {
        return getOneByField(col, "_id", id, fields);
    }

    public static Document getOneById(String col, String id, List<String> fields) {
        if (id == null) {
            return null;
        }

        return getOneByField(col, "_id", new ObjectId(id), fields);
    }

    public static Document getOneById(String col, String id) {
        if (id == null) {
            return null;
        }
        try {
            ObjectId objectId = new ObjectId(id);
            return getOneByField(col, "_id", new ObjectId(id));
        } catch (Exception e) {

            logger.error("dashboard id error: " + id);
        }
        return null;
    }

    public static Document getOneByField(String col, String field, Object value) {
        return getOneByField(col, field, value, null);
    }

    public static Document getOneByField(String col, String field, Object value, List<String> fields) {
        List<Bson> conditions = new ArrayList<>();
        conditions.add(eq(field, value));

        return getOneByConditions(col, conditions, fields);
    }

    public static Document getOneByConditions(String col, List<Bson> conditions) {
        return getOneByConditions(col, conditions, null);

    }

    public static Document getOneByConditions(String col, List<Bson> conditions, List<String> fields) {
        Document document = null;
        try {
            MongoCollection<Document> collection = database.getCollection(col);
            if (fields != null) {
                document = collection.find(and(conditions)).projection(Projections.include(fields)).limit(1).first();
            } else {
                document = collection.find(and(conditions)).limit(1).first();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return document;
    }

    public static Document insertOne(String col, Document doc) {
        doc.put("create_at", new Date());
        doc.put("update_at", new Date());

        MongoUtil.getCollection(col).insertOne(doc);

        if (!doc.containsKey("_id")) {
            logger.error("insert into collection failed :  " + col);
            return null;
        }

        return doc;
    }

    public static boolean upsertOne(String col, List<Bson> conditions, Document doc) {
        return upsertOne(col, conditions, doc, doc);
    }

    public static boolean upsertOne(String col, List<Bson> conditions, Document doc1, Document doc2) {
        MongoCollection<Document> collection = database.getCollection(col);

        Document document1 = null;
        Document document = MongoUtil.getOneByConditions(col, conditions);
        if (document == null) {
            document1 = insertOne(col, doc1);
        } else {
            document1 = updateOne(col, conditions, doc2);
        }

        if (document1 == null) {
            return false;
        }
        return true;
    }

    public static Document insertOne(String col, JsonObject entryJson) {
        Document doc = MongoUtil.json2Document(entryJson);
        return insertOne(col, doc);
    }

    public static void normalizeChart(Document item) {
        if (item.containsKey("_id")) {
            Object id = item.get("_id");
            item.remove("_id");
            item.put("id", id);
        }
        item.remove("dataSource");
        String source;
        if (item.containsKey("toolbar")) {
            source = String.format("/api/v1/chart/query-param-data?id=%s", item.get("id"));
        } else if (item.containsKey("customize")) {
            source = String.format("/api/v1/chart/query-custom-data?id=%s", item.get("id"));
        } else {
            source = String.format("/api/v1/chart/query-data?id=%s", item.get("id"));
        }
        item.put("dataSourceUrl", source);
    }

    public static long getNextSequence(String name) {
        MongoCollection<Document> collection = database.getCollection("counters");
        Document obj = collection.findOneAndUpdate(eq("_id", name),
                new Document("$inc", new Document("seq", 1)));
        return ((Number) obj.get("seq")).longValue();
    }

    /**
     * @param document
     * @param escape
     */
    // Mongodb不允许bson key里面包含.$符号, 这里把符号替换为对应的全角符号
    // escape为true 则半角->全角, 否则全角->半角
    public static void fixKeys(Document document, boolean escape) {
        Map<String, String> keyMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Document) {
                fixKeys((Document) value, escape);
            } else if (value instanceof List) {
                fixKeys((List) value, escape);
            }
            if (escape) {
                if (key.contains(".") || key.contains("$")) {
                    keyMap.put(key, key.replace(".", "．").replace("$", "＄"));
                }
            } else {
                if (key.contains("．") || key.contains("＄")) {
                    keyMap.put(key, key.replace("．", ".").replace("＄", "$"));
                }
            }
        }
        for (Map.Entry<String, String> entry : keyMap.entrySet()) {
            Object value = document.get(entry.getKey());
            document.remove(entry.getKey());
            document.put(entry.getValue(), value);
        }
    }

    public static void fixKeys(List list, boolean escape) {
        for (Object value : list) {
            if (value instanceof Document) {
                fixKeys((Document) value, escape);
            } else if (value instanceof List) {
                fixKeys((List) value, escape);
            }
        }
    }


    public static String getString(Document document, String key, String defaultValue) {
        String value = document.getString(key);
        if (StringUtils.isEmpty(value)) {
            value = defaultValue;
        }
        return value;
    }

    public static Document getDocument(Document document, String key, Document defaultValue) {
        Object value = document.get(key);
        if (value instanceof Document) {
            return (Document) value;
        } else {
            return defaultValue;
        }
    }

    public static Document getDocument(Document document, String key) {
        return getDocument(document, key, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(Document document, String key, List<T> defaultValue) {
        Object value = document.get(key);
        if (value instanceof List) {
            return (List<T>) value;
        } else {
            return defaultValue;
        }
    }

    public static <T> List<T> getList(Document document, String key) {
        return getList(document, key, null);
    }


    public static String getParaName(Document query, String key, Document parameters) {
        Iterator it = query.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry item = (Map.Entry) it.next();
            Object value = item.getValue();
            if (value instanceof String) {
                if (parameters.containsKey(value)) {
                    return (String) value;
                }
            } else if (value != null) {
                List<Document> items = (List<Document>) value;
                for (int i = 0; i < items.size(); i++) {
                    String name = getParaName(items.get(i), key, parameters);
                    if ("".equals(name)) {
                        continue;
                    }

                    return name;
                }
            }
        }

        return "";
    }

    public static void updateParaName(Document query, String oldName, String newName, Document parameters) {
        Iterator it = query.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry item = (Map.Entry) it.next();
            Object value = item.getValue();
            if (value instanceof String) {
                if (oldName.equals(value) && parameters.containsKey(newName)) {
                    item.setValue(newName);
                    continue;
                }
            } else if (value != null) {
                if (value instanceof Document) {
                    updateParaName((Document) value, oldName, newName, parameters);
                } else {
                    List<Document> items = (List<Document>) value;
                    for (int i = 0; i < items.size(); i++) {
                        updateParaName(items.get(i), oldName, newName, parameters);
                    }
                }
            }
        }
    }


    public static List<Document> getMeasurements() {
        MongoCollection<Document> collection = database.getCollection("measurements");
        FindIterable<Document> dataIterable = collection.find(eq("enabled", true));
        List<Document> datas = new ArrayList<Document>();
        dataIterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                document.remove("_id");
                String measureId = document.getString("measure_id");
                String path = document.getString("path");
                String type = document.getString("type");
                String table = document.getString("table");
                String[] paths = path.split("/");
                List<Document> temp = datas;
                for (int i = 0; i < paths.length; i++) {
                    String p = paths[i];
                    boolean found = false;
                    Document doc = null;
                    for (int j = 0; j < temp.size(); j++) {
                        if (temp.get(j).getString("text").equals(p)) {
                            doc = temp.get(j);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        doc = new Document();
                        doc.put("text", p);
                        if (i == paths.length - 1) {
                            doc.put("id", measureId);
                            Document attr = new Document();
                            attr.put("type", type);
                            attr.put("dataName", table);
                            doc.put("attribute", attr);
                        } else {
                            doc.put("id", p);
                            doc.put("state", "closed");
                            doc.put("children", new ArrayList<Document>());
                        }
                        temp.add(doc);
                    }
                    if (i != paths.length - 1) {
                        temp = doc.get("children", ArrayList.class);
                    }
                }
            }
        });
        return datas;
    }

    public static List<Document> getSecurities(int sectorId) {
        MongoCollection<Document> collection = database.getCollection("wind_wset");

        Document projection = new Document();
        projection.put("_id", false);
        projection.put("sec_name", true);
        projection.put("windcode", true);
        FindIterable<Document> dataIterable = collection.find(eq("sectors", sectorId)).projection(projection);
        List<Document> datas = new ArrayList<Document>();
        dataIterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                datas.add(document);
            }
        });
        return datas;
    }

    public static Document updateOneById(String col, Object id, Document doc) {
        return updateOneById(col, id, doc, true);
    }

    public static Document updateOneById(String col, Object id, Document doc, boolean set) {
        if (id instanceof String) {
            id = new ObjectId((String) id);
        }
        List<Bson> conditions = new ArrayList<>();
        conditions.add(eq("_id", id));

        return updateOne(col, conditions, doc, set);
    }

    public static Document updateOne(String col, List<Bson> conditions, Document data) {
        return updateOne(col, conditions, data, true);
    }

    public static Document updateOne(String col, List<Bson> conditions, Document data, boolean set) {
        if (set) {
            data.put("update_at", new Date());
            return updateOneWithDoc(col, conditions, new Document("$set", data));
        } else {
            return updateOneWithDoc(col, conditions, data);
        }
    }

    public static Document updateOneWithDoc(String col, List<Bson> conditions, Document data) {
        MongoCollection<Document> collection = database.getCollection(col);

        Document doc = collection.findOneAndUpdate(and(conditions), data);

        return doc;
    }


    private static List<Document> searchByField(String field, String search, String collectionName) {
        MongoCollection<Document> indexes = database.getCollection(collectionName);
        FindIterable<Document> dataIterable;
        if (StringUtils.isEmpty(search)) {
            dataIterable = indexes.find();
        } else {
            dataIterable = indexes.find(regex(field, search));
        }
        List<Document> results = new ArrayList<>();
        dataIterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                results.add(document);
            }
        });
        return results;
    }

    private static boolean replaceStringWithObjId(JsonObject source, Document doc, String field) {
        if (source.containsKey("creator_id")) {
            doc.remove("creator_id");
            doc.append("creator_id", new ObjectId(source.getString("creator_id")));
            return true;
        }
        return false;
    }

    public static void parseParametersFunction(Document parameters) {
        if (parameters == null) {
            return;
        }
        Pattern p = Pattern.compile("(:\\w+)");
        for (Object o : parameters.entrySet()) {
            Map.Entry item = (Map.Entry) o;
            Object value = item.getValue();
            if (value instanceof Document) {
                Document doc = (Document) value;
                if (doc.containsKey("function")) {
                    String function = doc.getString("function");
                    Matcher matcher = p.matcher(function);
                    while (matcher.find()) {
                        String g = matcher.group(1);
                        if (parameters.containsKey(g)) {
                            function = function.replace(g, ((Document) parameters.get(g)).get("default").toString());
                        }
                    }

                    Expression expression = new Expression(function);
                    expression.addFunction(expression.new Function("curTimestamp", 0) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            return new BigDecimal(new Date().getTime());
                        }
                    });
                    BigDecimal result = expression.eval();
                    if (result != null) {
                        doc.put("default", result);
                        doc.remove("function");
                    }
                }
            }
        }
    }

    /**
     * 伪删除，添加deleted=true
     *
     * @param col
     * @param id
     * @return
     */
    public static boolean pseudoDeleteOne(String col, String id) {
        try {

            Document doc = new Document("deleted", true);
            List<Bson> conds = Arrays.asList(
                    Filters.eq("_id", new ObjectId(id))
            );
            Document doc1 = MongoUtil.updateOne(col, conds, doc);
            if (doc1 == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    /**
     * 伪删除，添加deleted=true
     *
     * @param col
     * @param field
     * @param value
     * @return
     */
    public static boolean pseudoDeleteMany(String col, String field, Object value) {
        try {

            Document doc = new Document("$set", new Document("deleted", true));
            List<Bson> conds = Arrays.asList(
                    Filters.eq(field, value),
                    Filters.ne("deleted", true)
            );
            return MongoUtil.updateMany(col, conds, doc);

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新多个文档
     *
     * @param col
     * @param conds
     * @param doc
     * @return
     */
    public static boolean updateMany(String col, List<Bson> conds, Document doc) {
        MongoCollection<Document> collection = database.getCollection(col);
        if (doc.containsKey("$set"))
            doc = doc.get("$set", Document.class);
        doc.put("update_at", new Date());
        doc = new Document("$set", doc);
        UpdateResult res = collection.updateMany(Filters.and(conds), doc);
        return res.wasAcknowledged();
    }

    public static Document updateOneByField(String col, String fieldName, String fieldValue, Document updateDoc) {
        return updateOneByField(col, fieldName, fieldValue, updateDoc, true);
    }

    public static List<Document> getDBList(String col, List<Bson> conditions, String sort, String order, int offset, int limit) {
        return getDBList(col, conditions, sort, order, offset, limit, new String[]{});
    }

    public static Document updateOneByField(String col, String fieldName, String fieldValue, Document updateDoc, boolean set) {
        List<Bson> conditions = new ArrayList<>();
        conditions.add(eq(fieldName, fieldValue));
        return updateOne(col, conditions, updateDoc, set);
    }
}
