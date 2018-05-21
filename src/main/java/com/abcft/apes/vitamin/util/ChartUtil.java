package com.abcft.apes.vitamin.util;

import com.abcft.apes.vitamin.common.CommonUtil;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import com.udojava.evalex.Expression;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import redis.clients.jedis.Jedis;

import javax.json.JsonObject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.text.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;

/**
 * Created by zhyzhu on 17-4-26.
 */
public class ChartUtil {

    private static Logger logger = Logger.getLogger(ChartUtil.class);
    private static final long ONEDAYINMILLIONSECS = 60 * 60 * 24 * 1000;
    private static final long ONEHOURINMILLIONSECS = 60 * 60 * 1000;
    private static final long ONEMININMILLIONSECS = 60 * 1000;


    public static final int CHART_SHARE_NONE = 0;       //图表未共享
    public static final int CHART_SHARE_PUBLIC = 2;     //图表分享

    public static final int CHART_STATUS_OK = 0;        //图表正常
    public static final int CHART_STATUS_REMOVED = 1;   //图表已被移除


    public static final String CHART_TYPE_DEFAULT = "live_chart";
    public static final String TABLE_TYPE_DEFAULT = "live_table";
    public static final String CHART_TYPE_EXCEL_IMAGE = "excel_image";
    public static final String CHART_TYPE_EXCEL_LIVE = "excel_live_chart";
    public static final String TABLE_TYPE_EXCEL_LIVE = "excel_live_table";
    public static final String CHART_TYPE_PDF_LIVE = "pdf_live_chart";
    public static final String TABLE_TYPE_PDF_LIVE = "pdf_live_table";

    public static String CHART_API_URL = "http://10.12.0.30:11000";

    // 最多返回800条记录，从查询到的结果中等间隔的返回800条记录，理论上应该不会影响
    // 图表的绘制效果，性能可以有较大提升
    public static final int SAMPLE_COUNT = 800;

    /**
     * 插入图表
     * @param chart
     * @param userId
     * @return
     */
    public static long insertChart(Document chart, String userId) {

        MongoUtil.fixKeys(chart, true);

        MongoCollection<Document> collection = MongoUtil.getCollection(MongoUtil.CHART_COL);
        long newId = MongoUtil.getNextSequence("charts");

        chart.put("_id", newId);

        if (chart.containsKey("creator_id")) {
            chart.put("user_id", userId);
        } else {
            chart.put("creator_id", userId);
        }
        chart.put("status", CHART_STATUS_OK);

        if (!chart.containsKey("create_at")) {
            chart.put("create_at", new Date());
        }
        chart.put("update_at", new Date());

        collection.insertOne(chart);

        return getChartId(chart);
    }

    public static long getChartId(Document chart, String key) {
        if (chart == null || !chart.containsKey(key)) {
            return -1;
        }
        Object chartId = chart.get(key);
        if (chartId instanceof String) {
            return Long.parseLong(chart.getString(key));
        } else if (chartId instanceof Number) {
            return  ((Number)chart.get(key)).longValue();
        }

        return -1;
    }

    public static long getChartId(Document chart) {
        return getChartId(chart, "_id");
    }

    /**
     * 图表是否公开
     * @param id
     * @return
     */
    public static boolean isPublic(long id) {
        List<Bson> conditions = Arrays.asList(
            eq("_id", id),
            eq("share", CHART_SHARE_PUBLIC)
        );
        Document docChart = MongoUtil.getOneByConditions(MongoUtil.CHART_COL, conditions);
        if (docChart != null) {
            return true;
        }

        return false;
    }

    /*
     * 图表所在看板是否公开
     */
    public static boolean isBoardPublic(long id) {
        Document docChart = MongoUtil.getOneById(MongoUtil.CHART_COL, id);
        if (docChart == null || docChart.isEmpty() || !docChart.containsKey("board_id")) {
            return false;
        }

        String boardId = docChart.getString("board_id");
//        if (DashBoardUtil.isPublic(boardId)) {
//            return true;
//        }

        return false;
    }

    /**
     *
     * @param id
     * @param uid
     * @return
     */
    public static boolean isCreator(long id, String uid) {
        List<Bson> conditions = Arrays.asList(
            eq("_id", id),
            eq("creator_id", uid)
        );
        Document document = MongoUtil.getOneByConditions(MongoUtil.CHART_COL, conditions);

        return (document != null);
    }

    /**
     * 获取chart
     * @param id
     * @param detail
     * @return
     */
    public static Document getChart(long id, boolean detail) {
        Document chartDoc = MongoUtil.getOneById(MongoUtil.CHART_COL, id);
        if (chartDoc == null || chartDoc.isEmpty()) {
            return null;
        }

        MongoUtil.fixKeys(chartDoc, false);
        if (chartDoc != null && !detail) {
            MongoUtil.normalizeChart(chartDoc);
        }

        if (chartDoc.containsKey("creator_id")) {
            String creatorId = chartDoc.getString("creator_id");
            String creatorName = AccountUtil.getUsernameById(creatorId);
            chartDoc.put("creator_name", creatorName);
        }

        if (chartDoc.containsKey("chartSeries")) {
            Document chartSeries = (Document)chartDoc.get("chartSeries");
            if (chartSeries.containsKey("root_chart")) {
                long rootChartId = Long.parseLong(chartSeries.get("root_chart").toString());
                Document rootChartDoc = MongoUtil.getOneById(MongoUtil.CHART_COL, rootChartId);
                if (chartDoc == null || chartDoc.isEmpty()) {
                    return null;
                }
                chartDoc.put("chartSeries", rootChartDoc.get("chartSeries"));
                logger.info("replace chart series: " + id);
            }
        }

        return chartDoc;
    }

    /**
     * 清空图表缓存
     * @param chartId
     */
    public static void clearChartCache(long chartId) {
        MongoCollection<Document> col = MongoUtil.getCollection(MongoUtil.CHART_DATA_COL);

        col.deleteMany(eq("_id", chartId));
    }

    /**
     *
     * @param chartId
     * @return
     */
    public static boolean isChartDataUpdated(long chartId, String params) {

        List<Bson> conditions = new ArrayList<>();
        conditions.add(eq("chart_id", chartId));
        conditions.add(eq("params", params));

        Document document = MongoUtil.getOneByConditions(MongoUtil.CHART_DATA_COL, conditions);

        if (document == null || !document.containsKey("updated")) {
            return false;
        }

        return document.getBoolean("updated");
    }

    public static String getOriginChartDataUpdateTime(Object chartId, String params) {
        List<Long> chartIds = new ArrayList<>();
        List<Bson> conditions = Arrays.asList(
            eq("origin_chart", chartId)
        );

        MongoUtil.getCollection(MongoUtil.CHART_COL)
            .find(and(conditions))
            .forEach(new Block<Document>() {
                @Override
                public void apply(Document document) {
                    chartIds.add(((Number)document.get("_id")).longValue());
                }
            });

        List<Bson> conditions2 = Arrays.asList(
            in("chart_id", chartIds),
            eq("params", params),
            exists("chart_update_time", true)
        );

        Document chartData = MongoUtil.getOneByConditions(MongoUtil.CHART_DATA_COL, conditions2);
        if (chartData == null || !chartData.containsKey("chart_update_time")) {
            return null;
        }

        return chartData.getString("chart_update_time");
    }

    public static String getChartType(Document chart) {
        if (chart.containsKey("type")) {
            return chart.getString("type");
        } else {
            return CHART_TYPE_DEFAULT;
        }
    }


    /**
     * 更新图表缓存数据
     * @param chart
     * @param params
     * @return
     */
    public static void refreshChartCacheData(Document chart, String params) {
        String chartType = getChartType(chart);
//        if (chartType.equals(CHART_TYPE_EXCEL_LIVE)) {
//            String excelId = chart.getString("excel_id");
//            ExcelUtil.extractExcelChartData(excelId);
//            return;
//        }
//        if (chartType.equals(TABLE_TYPE_EXCEL_LIVE)) {
//            ExcelUtil.extractExcelTableData(chart.getString("excel_id"));
//            return;
//        }

        // 查询图表数据
        Document table = queryChartData(chart, params);
        if (table == null) {
//            logger.warn("get chart data null: " + chart.get("_id"));
            return;
        }

        Object chartId = chart.get("_id");
        long origin_chart = -1;
        if (chart.containsKey("origin_chart")) {
            origin_chart = ChartUtil.getChartId(chart, "origin_chart");
        }

        // 缓存查询数据到chart表中
        Document chartData = new Document();
        int refresh_interval = 600;
        if (chart.containsKey("refresh_interval")) {
            refresh_interval = ((Number)chart.get("refresh_interval")).intValue();
        }
        chartData.append("refresh_time", TimeUtil.getRelateDate(new Date(), Calendar.SECOND, refresh_interval));

        List<Bson> conditions2 = new ArrayList<>();
        conditions2.add(eq("chart_id", chartId));
        conditions2.add(eq("params", params));
        Document chartCache = MongoUtil.getOneByConditions(MongoUtil.CHART_DATA_COL, conditions2);
        if (chartCache == null) {
            chartData.append("chart_id", chartId);
            chartData.append("params", params);
            chartData.append("table", table);

            // 图表更新时间
            String chartUpdateTime = getOriginChartDataUpdateTime(origin_chart, params);
            if (!StringUtils.isEmpty(chartUpdateTime)) {
                chartData.put("chart_update_time", chartUpdateTime);
            } else if (table.containsKey("data_update_time")) {
                chartData.put("chart_update_time", table.get("data_update_time"));
            } else {
                chartData.put("chart_update_time", TimeUtil.getCurTimeStampStr());
            }

            MongoUtil.insertOne(MongoUtil.CHART_DATA_COL, chartData);
        } else {
            int oldHash = ((Document)chartCache.get("table")).hashCode();
            int newHash = table.hashCode();
            // 数据是否有更新
            if (newHash != oldHash) {
//                logger.info("chart data updated: " + chartId + " " + new Date());
                String chartUpdateTime = TimeUtil.getCurTimeStampStr();
                //图表数据最新时间
                chartData.put("chart_update_time", chartUpdateTime);
                chartData.put("table", table);
            }
            MongoUtil.updateOne(MongoUtil.CHART_DATA_COL, conditions2, chartData);
        }
    }

    /**
     * 获取图表数据
     * @param chartId
     * @param parameters
     * @return
     */
    public static Document getChartDataTable(long chartId, String parameters) {
        long startTime = System.currentTimeMillis();
        Document chart = getChart(chartId, true);
        if (chart == null) {
            throw new NotFoundException();
        }

        if (parameters == null) {
            parameters = "";
        }

        String chartType = getChartType(chart);
        //获取缓存
        List<Bson> conditions = new ArrayList<>();
        if (chartType.equals(CHART_TYPE_EXCEL_LIVE) && chart.containsKey("origin_chart")) {
            conditions.add(eq("chart_id", ChartUtil.getChartId(chart, "origin_chart")));
        } else if (chartType.equals(TABLE_TYPE_EXCEL_LIVE) && chart.containsKey("origin_chart")) {
            conditions.add(eq("chart_id", ChartUtil.getChartId(chart, "origin_chart")));
        } else if (chartType.equals(CHART_TYPE_PDF_LIVE) && chart.containsKey("origin_chart")) {
            conditions.add(eq("chart_id", ChartUtil.getChartId(chart, "origin_chart")));
        } else if (chartType.equals(TABLE_TYPE_PDF_LIVE) && chart.containsKey("origin_chart")) {
            conditions.add(eq("chart_id", ChartUtil.getChartId(chart, "origin_chart")));
        } else {
            conditions.add(eq("chart_id", chartId));
        }
        conditions.add(eq("params", parameters));
        long getChartTime = System.currentTimeMillis();

        Document chartData = MongoUtil.getOneByConditions(MongoUtil.CHART_DATA_COL, conditions);
        long getDataTime = System.currentTimeMillis();

        // 缓存数据不存在, 更新缓存数据
        if (chartData == null) {
            refreshChartCacheData(chart, parameters);

            chartData = MongoUtil.getOneByConditions(MongoUtil.CHART_DATA_COL, conditions);
            if (chartData == null) {
                logger.warn("chart data has not table: " + chartId + " " + parameters);
                return null;
            }
        }
        long refreshDataTime = System.currentTimeMillis();

        // 获取图表数据
        if (!chartData.containsKey("table")) {
            logger.warn("chart data has not table: " + chartId + " " + parameters);
            return null;
        }

        MongoUtil.updateOne(MongoUtil.CHART_DATA_COL, conditions, new Document("query_time", TimeUtil.getCurTimeStampStr()));
        long updateDataTime = System.currentTimeMillis();

        Document dataTable = chartData.get("table", Document.class);
        if (dataTable.containsKey("rows")) {
            List<List<Object>> rows = dataTable.get("rows", List.class);

            if (rows.size() < 2) {
                dataTable.put("rows", null);
            } else {
                boolean notEmpty = false;

                for (List<Object> row : rows) {
                    if (row.size() > 0) {
                        notEmpty = true;
                        break;
                    }
                }

                if (!notEmpty) {
                    dataTable.put("rows", null);
                }
            }
        }

        if (chartData.containsKey("data_update_time")) {
            dataTable.put("data_update_time", chartData.get("data_update_time"));
        }
        if (chartData.containsKey("chart_update_time")) {
            dataTable.put("chart_update_time", chartData.get("chart_update_time"));
        } else if (chartData.containsKey("data_update_time")) {
            dataTable.put("chart_update_time", chartData.get("data_update_time"));
        }

        return dataTable;
    }

    /**
     * 查询图表数据
     * @param chart
     * @param parameters
     * @return
     */
    public static Document queryChartData(Document chart, String parameters) {
        return queryDataByChartDefinition(chart, parameters);
    }


    private static Document queryDataByChartDefinition(Document chart, String parameters) {
//        if (chart.containsKey("sheetRef")) {
////            chart = ExcelEngineUtil.getChartDocument(chart.getString("sheetRef"));
//            Integer version = chart.getInteger("version");
//            String refId = chart.getString("sheetRef");
//            if (version != null && version == 3) {
//                return ExcelEngineUtil3.getChartDataTable(refId);
//            } else {
//                // for v2 Excel APIs
//                return ExcelEngineUtil2.getChartDataTable(refId);
//            }
//        }

        Document params = null;
        if (!StringUtils.isEmpty(parameters)) {
            params = Document.parse(parameters);
        }

        Document dataSource = MongoUtil.getDocument(chart, "dataSource");
        Document parametersInfo = MongoUtil.getDocument(dataSource, "parameters");
        MongoUtil.parseParametersFunction(params);
        MongoUtil.parseParametersFunction(parametersInfo);

        int startIndex = 0;
        if (params != null && params.containsKey(":startIndex")) {
            startIndex = MongoUtil.getDocument(params, ":startIndex").getInteger("default");
        } else {
            if (parametersInfo.containsKey(":startIndex")) {
                startIndex = MongoUtil.getDocument(parametersInfo, ":startIndex").getInteger("default");
            }
        }

        int endIndex = 0;
        if (params != null && params.containsKey(":endIndex")) {
            endIndex = MongoUtil.getDocument(params, ":endIndex").getInteger("default");
        } else {
            if (parametersInfo.containsKey(":endIndex")) {
                endIndex = MongoUtil.getDocument(parametersInfo, ":endIndex").getInteger("default");
            }
        }

        List<Document> columns = MongoUtil.getList(dataSource, "columns");
        //替换columns里面的参数
        for (int i = 0; i < columns.size(); i++) {
            Document d = applyQueryParameters(columns.get(i), parametersInfo, params);
            columns.set(i, d);
        }

        //解析aggregates
        List<Document> aggregates = MongoUtil.getList(dataSource, "aggregates");
        if (aggregates == null || aggregates.isEmpty()) {
            logger.info("aggretates is empty");

            return buildHighChartResult(new Object[0][], columns, startIndex, endIndex, true, false, false);
        }
        boolean tableMode = "table".equals(dataSource.getString("dataType"));

        List<List<Document>> queryResults = new ArrayList<>();
        int rowCount = tableMode ? 0 : Integer.MAX_VALUE;

        //查询数据
        for (Document query : aggregates) {
            query = applyQueryParameters(query, parametersInfo, params);
            //获取要查询的collection
            String collectionName = MongoUtil.getString(query, "collection", null);
            MongoCollection<Document> collection = MongoUtil.getCollection(collectionName);
            if (collection == null) {
                throw new NotFoundException("collection " + collectionName + " not found.");
            }

            //替换查询里面的参数
            List<Document> aggregate = MongoUtil.getList(query, "aggregate");
            AggregateIterable<Document> dataIterable = collection.aggregate(aggregate);
            List<Document> datas = dataIterable.into(new ArrayList<>());
            queryResults.add(datas);
            //如果是图表的模式, 数据是按照行的方式合并到一起
            if (tableMode) {
                rowCount += datas.size();
            } else {
                //否则数据就是按照列的方式合并到一起, 这里计算所有查询获取的数据里最小的行数, 方便最后的数据对齐
                if (datas.size() < rowCount) {
                    rowCount = datas.size();
                }
            }
        }

        // 合并查询结果
        int columnCount = columns.size();
        Object[][] dataTable = new Object[rowCount][columnCount];
        if (tableMode) {
            // 按照行合并到一起
            int i = 0;
            for (List<Document> queryResult : queryResults) {
                for (Document document : queryResult) {
                    Object[] row = new Object[columnCount];
                    for (int j = 0; j < columnCount; j++) {
                        Document column = columns.get(j);
                        String id = column.getString("id");
                        if (StringUtils.isEmpty(id)) {
                            row[j] = null;
                        } else {
                            row[j] = document.get(id);
                        }
                    }
                    dataTable[i++] = row;
                }
            }
        } else {
            // 按列合并到一起
            for (int i = 0; i < rowCount; i++) {
                Object[] row = new Object[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    Document column = columns.get(j);
                    String id = column.getString("id");
                    if (StringUtils.isEmpty(id)) {
                        row[j] = null;
                    } else {
                        int query_index = column.getInteger("query_index", 0);
                        List<Document> queryResult = queryResults.get(query_index);
                        Document document = queryResult.get(i);
                        row[j] = document.get(id);
                    }
                }
                dataTable[i] = row;
            }
        }

        boolean sample = dataSource.getBoolean("sample", true);
        boolean retainNull = dataSource.getBoolean("retainNull", false);
        if (!chart.containsKey("chartLib")) {
            return buildResult(dataTable, columns, startIndex, endIndex, sample, retainNull);
        } else {
            boolean transpose = dataSource.getBoolean("transpose", false);
            Document options = MongoUtil.getDocument(chart, "options");
            Document chartOpt = MongoUtil.getDocument(options, "chart");
            if (chartOpt != null && "map".equals(chartOpt.getString("type"))) {
                return buildGeoHighChartResult(dataTable, columns);
            } else {
                return buildHighChartResult(dataTable, columns, startIndex, endIndex, sample, transpose, retainNull);
            }
        }
    }

    private static Object getColumnDefaultValue(String type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case "date":
                return 0;
            case "number":
                return 0;
            case "string":
                return "";
            default:
                return null;
        }
    }


    private static List<List<Object>> transpose(final List<List<Object>> data) {
        if (data == null || data.size() == 0) {
            return data;
        }

        List<List<Object>> result = new ArrayList<>();
        int height = data.size();
        int width = 0;
        if (height > 0) {
            width = data.get(0).size();
        }
        for (int w = 0; w < width; w++) {
            List<Object> col = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                List<Object> row = data.get(i);
                col.add(row.get(w));
            }
            result.add(col);
        }

        return result;
    }

    private static Document buildGeoHighChartResult(final Object[][] dataTable, final List<Document> columns) {
        Document result = new Document();
        result.put("chartLib", "highChart");
        int columnCount = columns.size();
        List<Object> rows = new ArrayList<>();
        for (Object[] line : dataTable) {
            Document row = new Document();
            for (int j = 0; j < columnCount; j++) {
                Document column = columns.get(j);
                boolean hidden = column.getBoolean("hidden", false);
                if (!hidden) {
                    row.put(column.getString("label"), line[j]);
                }
            }
            rows.add(row);
        }

        result.put("data", rows);
        return result;
    }

    private static Document buildHighChartResult(final Object[][] dataTable, final List<Document> columns, final int startIndex, final int endIndex, boolean sample, boolean transpose, boolean retainNull) {
        Document result = new Document();
        result.put("chartLib", "highChart");
        int rowCount = dataTable.length;
        int columnCount = columns.size();
        List<List<Object>> rows = new ArrayList<>(rowCount);
        List<Object> cols = new ArrayList<>(columnCount);
        List<Format> formats = new ArrayList<>(columnCount);
        List<Expression> functions = new ArrayList<>(columnCount);
        for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
            Document column = columns.get(colIndex);
            String type = column.getString("type");
            String dataType = column.getString("dataType");
            String pattern = column.getString("pattern");
            boolean hidden = column.getBoolean("hidden", false);
            Format format = null;
            if (!StringUtils.isEmpty(pattern)) {
                if ("number".equals(type)) {
                    format = new DecimalFormat(pattern);
                } else if ("date".equals(type) || "date".equals(dataType)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
                    format = dateFormat;
                }
            }
            formats.add(format);
            if (!hidden) {
                cols.add(column.get("label"));
            }
            String function = column.getString("function");
            if (StringUtils.isEmpty(function)) {
                functions.add(null);
            } else {
                Expression expression = new Expression(function);
                expression.addFunction(expression.new Function("first", 1) {
                    @Override
                    public BigDecimal eval(List<BigDecimal> parameters) {
                        if (parameters.size() == 0) {
                            throw new RuntimeException("function 'first' requires at least one parameter");
                        }
                        int columnIndex = parameters.get(0).intValue();
                        if (rowCount == 0) {
                            return BigDecimal.ZERO;
                        }
                        Object value = dataTable[0][columnIndex];
                        if (value == null) {
                            value = 1;
                        }
                        return StringUtils.toBigDecimal(value);
                    }
                });
                functions.add(expression);
            }
        }

        long updateTime = 0;
        rows.add(cols);
        int selectParam = rowCount / SAMPLE_COUNT;
        // 应用公式, 格式化dataTable
        for (int i = 0; i < rowCount; i++) {
            if (i < startIndex) {
                continue;
            }
            // self is excluded
            if (i > rowCount - 1 + endIndex) {
                continue;
            }
            if (sample && rowCount > SAMPLE_COUNT && selectParam > 0 && i % selectParam != 0) {
                continue;
            }

            cols = new ArrayList<>();
            final Object[] row = dataTable[i];
            final Integer row_i = i;
            for (int j = 0; j < columnCount; j++) {
                Document column = columns.get(j);
                boolean hidden = column.getBoolean("hidden", false);
                if (hidden) {
                    continue;
                }

                Format format = formats.get(j);
                Object value = row[j];
                Expression function = functions.get(j);
                if (function != null) {
                    function.addFunction(function.new Function("val", 1) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'val' requires at least one parameter");
                            }
                            int columnIndex = parameters.get(0).intValue();
                            Object value = row[columnIndex];
                            if (value == null) {
                                value = 0;
                            }
                            return StringUtils.toBigDecimal(value);
                        }
                    });
//                    get the adjacent previous value
                    function.addFunction(function.new Function("previous", 1) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'previous' requires at least one parameter");
                            }
                            if (row_i == 0) {
                                return BigDecimal.ZERO;
                            }
                            int columnIndex = parameters.get(0).intValue();
                            Object value = dataTable[row_i - 1][columnIndex];
                            return StringUtils.toBigDecimal(value);
                        }
                    });
                    function.addFunction(function.new Function("valIndex", 2) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'valIndex' requires at least two parameters");
                            }
                            if (row_i == 0) {
                                return BigDecimal.ZERO;
                            }
                            int columnIndex = parameters.get(0).intValue();
                            int indexDelta = parameters.get(1).intValue();
                            int pos = row_i + indexDelta;
                            if (pos < 0) {
                                return new BigDecimal(0);
                            } else {
                                Object value = dataTable[pos][columnIndex];
                                return StringUtils.toBigDecimal(value);
                            }
                        }
                    });
                    // logic calculate: notEqual function
                    function.addFunction(function.new Function("ne", 2) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'ne' requires at least two parameters");
                            }
                            if (parameters.get(0).compareTo(parameters.get(1)) == 0) {
                                // means false
                                return BigDecimal.ZERO;
                            } else {
                                // means true
                                return BigDecimal.ONE;
                            }
                        }
                    });
                    function.addFunction(function.new Function("eq", 2) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'eq' requires at least two parameters");
                            }
                            if (parameters.get(0).compareTo(parameters.get(1)) == 0) {
                                // means true
                                return BigDecimal.ONE;
                            } else {
                                // means false
                                return BigDecimal.ZERO;
                            }
                        }
                    });
                    // implement EXCEL IF function.
                    function.addFunction(function.new Function("if", 3) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'if' requires at least three parameters");
                            }

                            // if logic is true
                            if (parameters.get(0).compareTo(BigDecimal.ONE) == 0) {
                                return parameters.get(1);
                            } else {
                                return parameters.get(2);
                            }
                        }
                    });
                    // return the last value which is not equal to the given parameter.
                    // self is included.
                    function.addFunction(function.new Function("lastNe", 2) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'lastNe' requires at least two parameters");
                            }

                            final int columnIndex = parameters.get(0).intValue();
                            final BigDecimal target = parameters.get(1);
                            int pos = row_i;
                            Object value = dataTable[pos][columnIndex];
                            while (pos >= 0 && StringUtils.toBigDecimal(value).equals(target)) {
                                pos--;
                                value = dataTable[pos][columnIndex];
                            }

                            if (pos < 0) {
                                return BigDecimal.ZERO;
                            } else {
                                return StringUtils.toBigDecimal(value);
                            }
                        }
                    });
                    value = function.eval().doubleValue();
                }

                Object v = value;
//                if (value == null && !retainNull) {
//                    // 根据指标类型填充默认值
//                    value = ExcelEngineUtil2.getMeasureDefaultValue(column.getString("id"));
//                    v = value;
//                }
                if (value == null && !retainNull) {
                    // 根据列类型填充默认值
                    value = getColumnDefaultValue(column.getString("type"));
                    v = value;
                }

                Long timeStamp = null;
                if (value != null && (format instanceof DateFormat) && !(value instanceof Date)) {
                    if (value instanceof String && !NumberUtils.isCreatable(value.toString())) {
                        try {
                            Date dt = DateUtils.parseDate((String) value, "yyyy-MM-dd");
                            timeStamp = dt.getTime();
                            value = dt;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        timeStamp = Long.parseLong(value.toString());
                        value = new Date(timeStamp);
                    }
                }
                if (value != null && value instanceof Date) {
                    timeStamp = ((Date) value).getTime();
                    v = String.format("Date(%d)", timeStamp);
                }

                // 获取最新时间
                String dataType = column.getString("dataType");
                if (value != null && timeStamp == null && dataType != null && "date".equals(dataType)) {
                    String str_v = value.toString();
                    if (NumberUtils.isCreatable(str_v)) {
                        timeStamp = Long.parseLong(str_v);
                    } else {
                        try {
                            if (str_v.length() == 10) {
                                Date dt = DateUtils.parseDate(str_v, "yyyy-MM-dd");
                                timeStamp = dt.getTime();
                            } else if(str_v.length() == 19) {
                                Date dt = DateUtils.parseDate(str_v, "yyyy-MM-dd HH:mm:ss");
                                timeStamp = dt.getTime();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (timeStamp != null && timeStamp > updateTime) {
                    updateTime = timeStamp;
                }

                if (value != null && format instanceof NumberFormat && !(value instanceof Number)) {
                    value = NumberUtils.createNumber(value.toString());
                }
                if (format != null && value != null) {
                    Object f = format.format(value);
                    cols.add(f);
                } else {
                    cols.add(v);
                }
            }
            rows.add(cols);
        }
        if (transpose) {
            rows = transpose(rows);
        }
        try {
            if (updateTime > 0) {
                result.put("data_update_time", Long.toString(updateTime));
            }
        } catch (Exception e) {
            logger.error("get chart update time error: " + e.getMessage());
        }
        result.put("data", new Document("rows", rows));
        return result;
    }

    private static Document buildResult(Object[][] dataTable, List<Document> columns, int startIndex, int endIndex, boolean sample, boolean retainNull) {
        Document result = new Document();
        int rowCount = dataTable.length;
        int columnCount = columns.size();
        List<Document> rows = new ArrayList<>(rowCount);
        List<Document> cols = new ArrayList<>(columnCount);
        List<Format> formats = new ArrayList<>(columnCount);
        List<Expression> functions = new ArrayList<>(columnCount);
        for (Document column : columns) {
            String type = column.getString("type");
            String dataType = column.getString("dataType");
            String pattern = column.getString("pattern");
            String role = column.getString("role");
            boolean hidden = column.getBoolean("hidden", false);
            Format format = null;
            if (!StringUtils.isEmpty(pattern)) {
                if ("number".equals(type)) {
                    format = new DecimalFormat(pattern);
                } else if ("date".equals(type) || "date".equals(dataType)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
                    format = dateFormat;
                }
            }
            formats.add(format);
            if (!hidden) {
                Document col = new Document();
                col.put("type", type);
                col.put("label", column.getString("label"));
                col.put("pattern", pattern);
                if (role != null) {
                    col.put("role", role);
                }
                cols.add(col);
            }
            String function = column.getString("function");
            if (StringUtils.isEmpty(function)) {
                functions.add(null);
            } else {
                Expression expression = new Expression(function);
                expression.addFunction(expression.new Function("first", 1) {
                    @Override
                    public BigDecimal eval(List<BigDecimal> parameters) {
                        if (parameters.size() == 0) {
                            throw new RuntimeException("function 'first' requires at least one parameter");
                        }
                        int columnIndex = parameters.get(0).intValue();
                        if (rowCount == 0) {
                            return BigDecimal.ZERO;
                        }
                        Object value = dataTable[0][columnIndex];
                        if (value == null) {
                            value = 1;
                        }
                        return StringUtils.toBigDecimal(value);
                    }
                });
                functions.add(expression);
            }
        }
        result.put("cols", cols);

        long updateTime = 0;
        int selectParam = rowCount / SAMPLE_COUNT;
        // 应用公式, 格式化dataTable
        for (int i = 0; i < rowCount; i++) {
            if (i < startIndex) {
                continue;
            }
            // self is excluded
            if (i > rowCount - 1 + endIndex) {
                continue;
            }
            if (sample && rowCount > SAMPLE_COUNT && selectParam > 0 && i % selectParam != 0) {
                continue;
            }

            final Object[] row = dataTable[i];
            Document rowJson = new Document();
            List<Document> cells = new ArrayList<>(columnCount);
            Integer row_i = i;
            for (int j = 0; j < columnCount; j++) {
                Document column = columns.get(j);
                boolean hidden = column.getBoolean("hidden", false);
                if (hidden) {
                    continue;
                }
                Document cell = new Document();

                Format format = formats.get(j);
                Object value = row[j];
                Expression function = functions.get(j);
                if (function != null) {
                    function.addFunction(function.new Function("val", 1) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'val' requires at least one parameter");
                            }
                            int columnIndex = parameters.get(0).intValue();
                            Object value = row[columnIndex];
                            if (value == null) {
                                value = 0;
                            }
                            return StringUtils.toBigDecimal(value);
                        }
                    });
//                    get the adjacent previous value
                    function.addFunction(function.new Function("previous", 1) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'previous' requires at least one parameter");
                            }
                            if (row_i == 0) {
                                return BigDecimal.ZERO;
                            }
                            int columnIndex = parameters.get(0).intValue();
                            Object value = dataTable[row_i - 1][columnIndex];
                            return StringUtils.toBigDecimal(value);
                        }
                    });
                    function.addFunction(function.new Function("valIndex", 2) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'valIndex' requires at least two parameters");
                            }
                            if (row_i == 0) {
                                return BigDecimal.ZERO;
                            }
                            int columnIndex = parameters.get(0).intValue();
                            int indexDelta = parameters.get(1).intValue();
                            int pos = row_i + indexDelta;
                            if (pos < 0) {
                                return new BigDecimal(0);
                            } else {
                                Object value = dataTable[pos][columnIndex];
                                return StringUtils.toBigDecimal(value);
                            }
                        }
                    });
                    // logic calculate: notEqual function
                    function.addFunction(function.new Function("ne", 2) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'ne' requires at least two parameters");
                            }
                            if (parameters.get(0).compareTo(parameters.get(1)) == 0) {
                                // means false
                                return BigDecimal.ZERO;
                            } else {
                                // means true
                                return BigDecimal.ONE;
                            }
                        }
                    });
                    function.addFunction(function.new Function("eq", 2) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'eq' requires at least two parameters");
                            }
                            if (parameters.get(0).compareTo(parameters.get(1)) == 0) {
                                // means true
                                return BigDecimal.ONE;
                            } else {
                                // means false
                                return BigDecimal.ZERO;
                            }
                        }
                    });
                    // implement EXCEL IF function.
                    function.addFunction(function.new Function("if", 3) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'if' requires at least three parameters");
                            }

                            // if logic is true
                            if (parameters.get(0).compareTo(BigDecimal.ONE) == 0) {
                                return parameters.get(1);
                            } else {
                                return parameters.get(2);
                            }
                        }
                    });
                    // return the last value which is not equal to the given parameter.
                    // self is included.
                    function.addFunction(function.new Function("lastNe", 2) {
                        @Override
                        public BigDecimal eval(List<BigDecimal> parameters) {
                            if (parameters.size() == 0) {
                                throw new RuntimeException("function 'lastNe' requires at least two parameters");
                            }

                            final int columnIndex = parameters.get(0).intValue();
                            final BigDecimal target = parameters.get(1);
                            int pos = row_i;
                            Object value = dataTable[pos][columnIndex];
                            while (pos >= 0 && StringUtils.toBigDecimal(value).equals(target)) {
                                pos--;
                                value = dataTable[pos][columnIndex];
                            }

                            if (pos < 0) {
                                return BigDecimal.ZERO;
                            } else {
                                return StringUtils.toBigDecimal(value);
                            }
                        }
                    });
                    value = function.eval().doubleValue();
                }

                Object v = value;
//                if (value == null && !retainNull) {
//                    // 根据指标类型填充默认值
//                    value = ExcelEngineUtil2.getMeasureDefaultValue(column.getString("id"));
//                    v = value;
//                }
                if (value == null && !retainNull) {
                    // 根据列类型填充默认值
                    value = getColumnDefaultValue(column.getString("type"));
                    v = value;
                }
                String dataType = column.getString("dataType");
                if (dataType != null && dataType.equals("date")) {
//                    dataLatestTimestamp = value.toString();
                    try{
                        long tmp = Long.parseLong(value.toString());
                        if (tmp>updateTime) {
                            updateTime = tmp;
                        }
                    }catch (Exception e) {
                        logger.error("get chart update time error: " + e.getMessage());
                    }
                }
                if (value != null && (format instanceof DateFormat) && !(value instanceof Date)) {
                    if (value instanceof String && !NumberUtils.isCreatable(value.toString())) {
                        try {
                            value = DateUtils.parseDate((String) value, "yyyy-MM-dd");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        value = new Date(Long.parseLong(value.toString()));
                    }
                }
                if (value != null && value instanceof Date) {
                    v = String.format("Date(%d)", ((Date) value).getTime());
                }
                if (value != null && format instanceof NumberFormat && !(value instanceof Number)) {
                    value = NumberUtils.createNumber(value.toString());
                }
                if (format != null && value != null) {
                    Object f = format.format(value);
                    cell.put("v", v);
                    cell.put("f", f);
                } else {
                    cell.put("v", v);
                }
                cells.add(cell);
            }
            rowJson.put("c", cells);
            rows.add(rowJson);
        }
        try {
            if (updateTime > 0) {
                result.put("data_update_time", Long.toString(updateTime));
            }
        } catch (Exception e) {
            logger.error("get chart update time error: " + e.getMessage());
        }
        result.put("rows", rows);
        return result;
    }

    private static Document applyQueryParameters(Document query, Document parametersInfo, Document parameters) {
        String collectionName = MongoUtil.getString(query, "collection", null);
        Document filter = new Document(query);
        for (Map.Entry<String, Object> param : parametersInfo.entrySet()) {
            String name = param.getKey();
            Document info = (Document) param.getValue();
            Object value = info.get("default");
            String type = info.getString("type");
            if (parameters != null && parameters.containsKey(name)) {
                value = parameters.get(name);
            }
            if ("date".equals(type)) {
                value = new Date(Long.parseLong(value.toString()));
            } else if (NumberUtils.isCreatable(value.toString())){
                value = NumberUtils.createNumber(value.toString());
            }
            // 对于分钟数据，endDate加一天，以便查询到endDate当天的数据
            if (collectionName != null && collectionName.equals("wind_wsi") && name.equals(":endDate")
                && NumberUtils.isCreatable(value.toString())) {
                value = NumberUtils.toLong(value.toString()) + 86400000;
            }
            replaceValue(filter, name, value);
        }
        return filter;
    }

    private static void replaceValue(Document document, String name, Object value) {
        List<String> candidateKeys = new ArrayList<>();
        for (Map.Entry<String, Object> item : document.entrySet()) {
            String key = item.getKey();
            if (key.equals(name)) {
                candidateKeys.add(key);
            }
            Object v = item.getValue();
            if (name.equals(v)) {
                item.setValue(value);
            } else if (v instanceof Document) {
                replaceValue((Document) item.getValue(), name, value);
            } else if (v instanceof List) {
                List list = (List) v;
                for (int i = 0; i < list.size(); i++) {
                    Object aitem = list.get(i);
                    if (aitem instanceof Document) {
                        replaceValue((Document) aitem, name, value);
                    } else if (name.equals(aitem)) {
                        list.set(i, value);
                    }
                }
            }
        }

        for (String key : candidateKeys) {
            Object v = document.get(key);
            document.remove(key);
            document.put(String.valueOf(value), v);
        }
    }

    /**
     * 获取符合条件的chart总数
     * @param conditions
     * @return
     */
    public static long getChartCount(List<Bson> conditions) {
        return MongoUtil.getDBCount(MongoUtil.CHART_COL, conditions);
    }

    public static int getChartCountUniq(List<Bson> conditions) {
        int count = 0;
        List<Document> charts = MongoUtil.getDBList(MongoUtil.CHART_COL, conditions);

        Document origin_map = new Document();
        long uniq_id = 0;
        for(Document chart: charts) {
            uniq_id = chart.get("title").hashCode();
//            if (chart.containsKey("origin_chart")) {
//                uniq_id = ((Number) chart.get("origin_chart")).longValue();
//            } else {
//                uniq_id = ((Number) chart.get("_id")).longValue();
//            }
            if (!origin_map.containsKey(Long.toString(uniq_id))) {
                origin_map.put(Long.toString(uniq_id), 1);
                count++;
            }
        }

        return count;
    }

    /**
     * 获取图表详细信息
     * @param chartId
     * @return
     */
    public static Document getChartDetail(Long chartId) {
        List<Bson> conditions = Arrays.asList(
            eq("_id", chartId),
            ne("deleted", true)
        );
        Document chartSketch = MongoUtil.getOneByConditions(MongoUtil.CHART_COL, conditions);
        if (chartSketch == null) {
            logger.warn("chartSketch not exist: " + chartId);
            return null;
        }
        Document chartDetail = new Document();
        chartDetail.put("title", chartSketch.get("title"));
        chartDetail.put("_id", chartSketch.get("_id").toString());
        if (chartSketch.containsKey("toolbar")) {
            Document toolbar = chartSketch.get("toolbar", Document.class);
            chartDetail.put("toolbar", toolbar.get("parameters"));
        }
        if (chartSketch.containsKey("customize")) {
            Document customize = chartSketch.get("customize", Document.class);
            chartDetail.put("customize", true);
            if (customize.containsKey("topn")) {
                chartDetail.put("topn", customize.get("topn"));
            }
        }

        if (chartSketch.containsKey("type")) {
            chartDetail.put("type", chartSketch.get("type"));
        }
        if (chartSketch.containsKey("image_url")) {
            chartDetail.put("image_url", chartSketch.get("image_url"));
            if (chartDetail.containsKey("thumbnail")) {
                chartDetail.remove("thumbnail");
            }
        } else if (chartSketch.containsKey("thumbnail")) {
            chartDetail.put("thumbnail", chartSketch.get("thumbnail"));
        }
        if (chartSketch.containsKey("chartType")){
            chartDetail.put("chartType", chartSketch.get("chartType"));
        }
        if (chartSketch.containsKey("options")) {
            chartDetail.put("options", chartSketch.get("options"));
        }
        if (chartSketch.containsKey("timerange")) {
            chartDetail.put("timerange", chartSketch.get("timerange"));
        }
        if (chartSketch.containsKey("company_id")) {
            chartDetail.put("company_id", chartSketch.get("company_id"));
        }
        if (chartSketch.containsKey("creator_id")) {
            String creatorId = chartSketch.getString("creator_id");
            chartDetail.put("creator_id", creatorId);
            String creatorName = AccountUtil.getUsernameById(creatorId);
            chartDetail.put("creator_name", creatorName);
        }
        if (chartSketch.containsKey("origin_chart")) {
            chartDetail.put("origin_chart", chartSketch.get("origin_chart"));
        }
        if (chartSketch.containsKey("from_chart")) {
            chartDetail.put("from_chart", chartSketch.get("from_chart"));
        }
        if (!chartDetail.containsKey("type")) {
            chartDetail.put("type", CHART_TYPE_DEFAULT);
        }
        if (chartSketch.containsKey("chartSeries")) {
            chartDetail.put("chartSeries", chartSketch.get("chartSeries"));
        }
        chartDetail.put("description", chartSketch.getOrDefault("description", ""));

        Object chartDesp = getDescription(chartId);
        if (chartDesp == null) {
            chartDesp = chartSketch.getOrDefault("description", "");
        }
        chartDetail.put("description", chartDesp);

        MongoUtil.fixKeys(chartDetail, false);
        MongoUtil.normalizeChart(chartDetail);
        if (chartDetail.containsKey("dataTable")) {
            chartDetail.remove("dataTable");
        }

        return chartDetail;
    }

    public static String getChartDataSourceUrl(long chartId, Document params) {
        StringBuilder sb = new StringBuilder();
        try{
            sb.append("/api/v1/chart/query-data?id=")
                .append(Long.toString(chartId))
                .append("&parameters=")
                .append(URLEncoder.encode(JSON.serialize(params), "UTF-8"));

        } catch (Exception e) {
            logger.warn("");
        }

        return sb.toString();
    }

    /**
     * 获取符合条件的chart列表
     * @param conditions
     * @param offset
     * @param limit
     * @param sort
     * @param order
     * @return
     */
    public static List<Document> getChartList(List<Bson> conditions, int offset, int limit, String sort, String order, String boardId) {
        List<Document> results = new ArrayList<>();
        try{
            List<Document> charts = MongoUtil.getDBList(MongoUtil.CHART_COL, conditions, sort, order, 0, 0, null);

            Document origin_map = new Document();
            long uniq_id = 0;
            int offset2 = 0;
            int limit2 = 0;
            for(Document chart: charts) {
                    Document chartNew = new Document();
                    chartNew.put("_id", chart.get("id"));
                    chartNew.put("title", chart.get("title"));
                    chartNew.put("chartType", chart.get("chartType"));
                    chartNew.put("options", chart.get("options"));
                    chartNew.put("timerange", chart.get("timerange"));
//                }

                //
                if (chart.containsKey("title")) {
                    uniq_id = chart.get("title").hashCode();
                }
                if (chart.containsKey("origin_chart")) {
                    chartNew.put("origin_chart", chart.get("origin_chart"));
                }

                //过滤从同一个源图表复制而来的图表
                if (origin_map.containsKey(Long.toString(uniq_id))) {
                    continue;
                } else {
                    origin_map.put(Long.toString(uniq_id), 1);
                }

                offset2 += 1;
                if (offset2 <= offset) {
                    continue;
                }
                limit2 += 1;
                if (limit > 0 && limit2 > limit) {
                    break;
                }

                //
                if (!StringUtils.isEmpty(boardId)) {
                    long chartId = ((Number)chart.get("_id")).longValue();
                    chartNew.put("inBoard", DashBoardUtil.isChartInBoard(boardId, chartId));
                }

                if (chart.containsKey("company_id")) {
                    chartNew.put("company_id", chart.get("company_id"));
                }
                if (chart.containsKey("image_url")) {
                    chartNew.put("image_url", chart.get("image_url"));
                }
                if (!chartNew.containsKey("image_url") && chart.containsKey("thumbnail")) {
                    chartNew.put("thumbnail", chart.get("thumbnail"));
                }
                if (chart.containsKey("type")) {
                    chartNew.put("type", chart.get("type"));
                }

                if (chart.containsKey("creator_id")) {
                    String creatorId = chart.getString("creator_id");
                    chartNew.put("creator_id", creatorId);
                    JsonObject user = AccountUtil.getUserById(creatorId);
                    if (user!=null) {
                        chartNew.put("creator_name", user.getString("username"));
                    }
                }
                if (!chart.containsKey("type") || chart.getString("type") != CHART_TYPE_EXCEL_IMAGE) {
                    MongoUtil.fixKeys(chartNew, false);
                    MongoUtil.normalizeChart(chartNew);
                }
                results.add(chartNew);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get chart list error: ", e);
        }
        return results;
    }

    public static List<Document> getAllCharts() {
        MongoCollection<Document> charts = MongoUtil.getCollection("charts");

        List<Bson> condition = Arrays.asList(
            Filters.exists("sheetRef", false),
            Filters.exists("board_id", false),
            Filters.exists("origin_chart", false),
            Filters.eq("company_id", "system")
        );
        FindIterable<Document> dataIterable = charts.find(Filters.and(condition))
            .sort(ascending("_id"));

        List<Document> documents = dataIterable.into(new ArrayList<>());
        MongoUtil.fixKeys(documents, false);
        return documents;
    }

    public static boolean updateChartImageUrl(long id, String imageUrl) {
        MongoCollection<Document> charts = MongoUtil.getCollection("charts");
        Document document = new Document();
        document.put("image_url", imageUrl);
        Document document1 = MongoUtil.updateOneById(MongoUtil.CHART_COL, id, document);
        if (document1 == null) {
            logger.warn("update chart title failed: " + id);
            return false;
        }

        return true;

    }

    public static boolean updateChartTitle(long id, String title) {
        MongoCollection<Document> charts = MongoUtil.getCollection("charts");
        Document document = new Document();
        document.put("title", title);
        document.put("update_at", new Date());
        Document document1 = MongoUtil.updateOneById(MongoUtil.CHART_COL, id, document);
        if (document1 == null) {
            logger.warn("update chart title failed: " + id);
            return false;
        }

        clearChartCache(id);

        return true;
    }

    public static boolean replaceChartDoc(Document chart, String uid) {
        MongoUtil.fixKeys(chart, true);

        long chartId = ChartUtil.getChartId(chart);

        //更新图表缩略图
//        saveThumbnail(chartId, chart);

        UpdateResult result = MongoUtil.getCollection(MongoUtil.CHART_COL)
            .replaceOne(eq("_id", chartId), chart);

        //更新图表缓存数据
        clearChartCache(chartId);

        return (result.getModifiedCount() > 0);
    }

    public static boolean updateChart(long chartId, Document chart, String uid) {
        MongoUtil.fixKeys(chart, true);

        //更新图表缩略图
//        saveThumbnail(chartId, chart);

        if (chart.containsKey("_id")) {
            chart.remove("_id");
        }

        Document document = MongoUtil.updateOneById(MongoUtil.CHART_COL, chartId, chart);

        //更新图表缓存数据
        clearChartCache(chartId);

        return (document!=null && !document.isEmpty());
    }

    public static List<Document> getDataSet(Document data) {
        Document sourceDef = new Document();
        sourceDef.put("dataSource", data);
        Document response = queryDataByChartDefinition(sourceDef, null);

        List<Document> columns = (List<Document>)response.get("cols");
        List<Document> rows = (List<Document>)response.get("rows");
        String[] colNames = new String[columns.size()];
        for (int i=0; i < columns.size(); i++) {
            colNames[i] = columns.get(i).getString("id");
        }

        List<Document> formatedResp = new ArrayList<Document>();
        for (int i=0; i < rows.size(); i++) {
            Document formatedRowData = new Document();
            List<Document> rowData = (List<Document>)rows.get(i).get("c");
            for (int j=0; j < rowData.size(); j++) {
                if (colNames[j].equals("date")) {
                    Object value = rowData.get(j).get("f");
                    if (value == null) {
                        value = rowData.get(j).get("v");
                    }
                    formatedRowData.put(colNames[j], value);
                } else {
                    formatedRowData.put(colNames[j], rowData.get(j).get("v"));
                }
            }
            formatedResp.add(formatedRowData);
        }

        return formatedResp;
    }

    public static Document queryApiDataById(long _id, String parameter) throws Exception {
        // DO NOT query by 'params' field.
        // each query result is saved to the same chart_id corresponding document
        // therefore, no need to save parameter in dash board record
        // 'params' field is used to check whether update the cache data.
        List<Bson> condition = Arrays.asList(
            Filters.eq("chart_id", _id)
        );
        Document data_record = MongoUtil.getOneByConditions(MongoUtil.CHART_DATA_COL, condition);
        // check if out of date
        if (data_record != null) {
            Date last = data_record.getDate("update_at");
            long delta = new Date().getTime() - last.getTime();
            if (delta >= ONEDAYINMILLIONSECS
                || !StringUtils.isEmpty(parameter) && !parameter.equals(data_record.getString("params"))) {
                data_record = null;
            }
        }

        if (data_record != null) {
            Document table = data_record.get("table", Document.class);
            return table;
        } else {
            Document record = MongoUtil.getOneById(MongoUtil.CHART_COL, _id);
            Document toolbar = record.get("toolbar", Document.class);

            String target = toolbar.getString("target");

            Document params;
            if (StringUtils.isEmpty(parameter)) {
                params = new Document();
            } else {
                params = Document.parse(parameter);
            }
            List<Document> parameters = toolbar.get("parameters", List.class);

            Date start = new Date();
            Document data = queryApiData(parameters, params, target);
            Date stop = new Date();
            logger.info(String.format("query api use time: %d",  TimeUtil.getTimeInterval(start, stop)) );

            data_record = new Document("chartLib", "highChart");
            data_record.put("chart_id", _id);
            data_record.put("target", target);
            data_record.put("params", JSON.serialize(params));
            data_record.put("table", data);

            UpdateOptions opt = new UpdateOptions();
            opt.upsert(true);
            data_record.put("update_at", new Date());
            MongoUtil.getCollection(MongoUtil.CHART_DATA_COL)
                .updateOne(Filters.and(condition), new Document("$set", data_record), opt);

            // update configed value to parameters
            boolean changed = false;
            for (Document p : parameters) {
                String name = p.getString("name");
                if (params.containsKey(name)) {
                    p.put("value", params.get(name));
                    changed = true;
                }
            }
            if (changed) {
                MongoUtil.getCollection(MongoUtil.CHART_COL)
                    .updateOne(Filters.eq("_id", _id),
                        new Document("$set", new Document("toolbar.parameters", parameters)));
            }

            return data;
        }
    }

    private static Document queryApiData(List<Document> parameters, Document params, String targetUrl) throws Exception {
        if (parameters == null) {
            return null;
        }

        Document args = new Document();
        StringBuilder sb = new StringBuilder("?");
        for (Document p : parameters) {
            String name = p.getString("name");
            Object value = params.get(name);
            if (value == null) {
                throw new Exception("缺少参数: " + name);
            }

            sb.append(name)
                .append("=")
                .append(value.toString())
                .append("&");
            args.put(name, value.toString());
        }
        int len = sb.length();
        sb.deleteCharAt(len - 1);

        String url = String.format("%s%s", targetUrl, sb.toString());

        //String data = getApiCacheData(url);
        String data = getApiData(url);

        Document raw = Document.parse(data);
        if (raw.containsKey("args")) {
            sb = new StringBuilder();
            Document raw_args = raw.get("args", Document.class);
            for (Map.Entry<String, Object> item : raw_args.entrySet()) {
                String key = item.getKey();
                Object value = item.getValue();
                sb.append(key)
                    .append(" : ")
                    .append(value)
                    .append("\n");

                if (args.containsKey(key)) {
                    args.put(key, value);
                }
            }
            logger.info(String.format("args = %s", sb.toString()));
        }

        Document result = new Document();
        result.put("chartLib", "highChart");
        result.put("args", args);
        result.put("data", new Document("rows", raw.get("data")));

        return result;
    }

    private static String getApiCacheData(String url) throws IOException {
        String data=null;
        Jedis jedis = RedisUtil.getRedis();
        if (jedis == null) {
            logger.warn("jedis is null");
            return getApiData(url);
        }

        String key = RedisUtil.getRedisKey(url.toLowerCase());

        //find cache
        try {

            data = jedis.get(key);
            if (data != null) {
                logger.info("get cache: " + key);
                return data;
            }

            // cache invalid
            data = getApiData(url);
            if (data.indexOf("unknown error occured") >=0 || data.length() < 10) {
                logger.warn("get data error: " + url);
            } else {
                // set cache expiry
                if (!StringUtils.isEmpty(data)) {
                    jedis.set(key, data);
                }
                if (key.indexOf("/stock/quote") > 0) {
                    jedis.expire(key, 60);
                } else if(key.indexOf("refresh=s")>0){
                    jedis.expire(key, 1);
                } else if(key.indexOf("refresh=m")>0){
                    jedis.expire(key, 60);
                } else if(key.indexOf("refresh=h")>0){
                    jedis.expire(key, 60*60);
                } else {
                    jedis.expire(key, 60*60);
                }
            }
            return data;
        }
        finally {
            jedis.close();
        }
    }

    private static String getApiData(String url) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            HttpResponse resp = client.execute(request);

            BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("Query data error: " + url, e);
        }
        return "{}";
    }

    public static String getCustomizeTargetUrl(Document customize) {
        assert(customize.containsKey("target"));

        String target = customize.getString("target");

        try {
            URL url = new URL(target);
            String target_host = CHART_API_URL;
            if (customize.containsKey("target_host")) {
                target_host = customize.getString("target_host");
            }

            if (target_host != null) {
                target = UriBuilder.fromUri(target_host).path(url.getPath()).toString();
            }
        }catch (Exception e) {
            logger.error("get customize target url error: ", e);
        }

        return target;
    }

    public static Document queryCustomData(long _id, String timepoint) throws Exception {
        Document record = MongoUtil.getOneById(MongoUtil.CHART_COL, _id);
        Document customize = record.get("customize", Document.class);

        String target = getCustomizeTargetUrl(customize);

        List<Document> defaultParams = customize.get("parameters", List.class);
        if (!StringUtils.isEmpty(timepoint)) {
            defaultParams.add(new Document("name", "tp").append("value", timepoint));
        }
        Document table = getCustomApiData(target, defaultParams);

        String dataUpdateTime = TimeUtil.getCurTimeStampStr();
        table.put("chartLib", record.get("chartLib"));
        table.put("chart_update_time", dataUpdateTime);
        return table;
    }

    public static String getMarkerId(long chartId, Object seriesId, Object pointId) {
        String strPointId = null;
        if (pointId instanceof Double) {
            strPointId = String.format("%.0f", pointId);
        } else {
            strPointId = pointId.toString();
        }
        return String.format("%d_%s_%s",chartId, seriesId.toString(), strPointId);
    }

    /**
     * 设置 data Marker
     * @param chartId
     * @return
     */
    public static Object getDescription(long chartId) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("chart_id", chartId)
        );

        Document chartDesp = MongoUtil.getOneByConditions(MongoUtil.CHART_DESP_COL, conditions);
        if (chartDesp == null) {
            return null;
        }

        return chartDesp.get("description");
    }

    /**
     * 设置 data desp
     * @param chartId
     * @return
     */
    public static boolean setDescription(long chartId, Object desp) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("chart_id", chartId)
        );
        Document despData = new Document()
                .append("chart_id", chartId)
                .append("description", desp);

        boolean res = MongoUtil.upsertOne(MongoUtil.CHART_DESP_COL, conditions, despData);
        return res;
    }

    public static Document getMarkers(long chartId, String sort, String order, int offset, int limit) {
        Document data = new Document();

        List<Bson> conditions = Arrays.asList(
                Filters.eq("chart_id", chartId)
        );
        Document chart = MongoUtil.getOneById(MongoUtil.CHART_COL, chartId);
        List<Document> orderList = new ArrayList<>();
        FindIterable<Document> findIterable = MongoUtil.getDBFindIterable(MongoUtil.CHART_DATA_MARKER_COL, conditions, sort, order, offset, limit);
        findIterable.forEach(new Consumer<Document>() {
            @Override
            public void accept(Document doc) {
                Document item = new Document()
                        .append("mid", doc.getString("mid"))
                        .append("chart_id", doc.get("chart_id"))
                        .append("series_id", doc.getOrDefault("series_id", ""))
                        .append("series_name", doc.getOrDefault("series_name", ""))
                        .append("point_id", doc.getOrDefault("point_id", ""))
                        .append("chart_title", chart.get("title"))
                        .append("marker", doc.get("marker") )
                        .append("update_at", doc.get("update_at"));

                orderList.add(item);
            }
        });

        int total = MongoUtil.getDBCount(MongoUtil.CHART_DATA_MARKER_COL, conditions);
        data.append("total", total)
                .append("list", orderList);


        return data;
    }

    /**
     * 设置 data Marker
     * @param chartId
     * @param marker
     * @return
     */
    public static boolean setSeriesDataMarker(String userId, long chartId, Object seriesId, Object seriesName, Object pointId, Document marker) {
        String markerId = ChartUtil.getMarkerId(chartId, seriesId, pointId);
        List<Bson> conditions = Arrays.asList(
                Filters.eq("mid", markerId)
        );
        Document markerData = new Document()
                .append("creator_id", userId)
                .append("chart_id", chartId)
                .append("series_id", seriesId)
                .append("series_name", seriesName)
                .append("point_id", pointId)
                .append("mid", markerId)
                .append("marker", marker);

        return MongoUtil.upsertOne(MongoUtil.CHART_DATA_MARKER_COL, conditions, markerData);
    }

    /**
     * 设置 data Marker
     * @param chartId
     * @param marker
     * @return
     */
    public static boolean setSeriesDataMarker(String userId, long chartId, String markerId, Document marker) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("mid", markerId)
        );
        Document markerData = new Document()
                .append("creator_id", userId)
                .append("chart_id", chartId)
                .append("mid", markerId)
                .append("marker", marker);

        return MongoUtil.upsertOne(MongoUtil.CHART_DATA_MARKER_COL, conditions, markerData);
    }


    /**
     * 取消 data Marker
     * @param markerId
     * @return
     */
    public static boolean unsetSeriesDataMarker(String markerId) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("mid", markerId)
        );

        return MongoUtil.deleteOne(MongoUtil.CHART_DATA_MARKER_COL, conditions);
    }

    private static Document collectChartMarkers(long chartId) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("chart_id", chartId)
        );

        Document markerDoc = new Document();
        List<Document> markerList = MongoUtil.getDBList(MongoUtil.CHART_DATA_MARKER_COL, conditions);
        for(Document doc: markerList) {
            markerDoc.put(doc.getString("mid"), doc.get("marker"));
        }

        return markerDoc;
    }

    private static Document getPointMarker(Document markerDoc, String markerId){
        if (markerDoc.containsKey(markerId)){
            return markerDoc.get(markerId, Document.class);
        }

        return null;
    };

    private static List<Document> dataTableToSeriesData(Document chart, Document dataTable) {
        Document options = null;
        boolean isTable = false;
        boolean switchRC = false;
        String chartType = "line";

        long chartId = getChartId(chart);
        Document markerDoc = collectChartMarkers(chartId);

        if (!chart.containsKey("my_chart")) {
            options = chart.get("options", Document.class);
            chartType = chart.getString("chartType");

            Document dataOption = options.get("data", Document.class);
            if (dataOption != null) {
                switchRC = dataOption.getBoolean("switchRowsAndColumns", false);
            }
        } else {
            Document chartInfo = chart.get("chartInfo", Document.class);
            if (chartInfo != null) {
                Document chartOption = chartInfo.get("chart", Document.class);
                if (chartOption != null) {
                    if (chartOption.containsKey("chartType")) {
                        chartType = chartOption.getString("chartType");
                        isTable = "table".equals(chartType);
                    } else {
                        chartType = chartOption.getString("type");
                        isTable = "table".equals(chartType);
                    }
                    options = chartOption.get("options", Document.class);
                }
            }
        }
        if (options.containsKey("chart")) {
            Document chartOption = options.get("chart", Document.class);
            if (chartOption != null && chartOption.getBoolean("polar", false)) {
                chartType = "polar";
            }
        }

        Object xAxisObj = options.get("xAxis");
        Document xAxis = null;
        if (xAxisObj instanceof List) {
            List<Document> xAxises = (List)xAxisObj;
            if (xAxises.size() > 0) {
                xAxis = xAxises.get(0);
            }
        } else if (xAxisObj instanceof Document) {
            xAxis = (Document)xAxisObj;
        }
        String xType = "line";
        if (xAxis != null && xAxis.containsKey("type")) {
            xType = xAxis.getString("type");
        }

        List<Document> list = new ArrayList<>();
        Document data = dataTable.get("data", Document.class);
        if (data == null) {
            return list;
        }
        List<List<Object>> rows = data.get("rows", List.class);
        if (rows == null) {
            return list;
        }

        if (StringUtils.isEmpty(chartType) && isTable) {
            chartType = "table";
        }

        int startRowIndex = 1;
        if ("pie".equals(chartType) || "polar".equals(chartType)) {
            if ("pie".equals(chartType)) {
                xType = "";
            }
            boolean needTranspose = true;
            if (rows.size() > 0) {
                List<Object> firstLine = rows.get(0);
                boolean isAllStr = true;
                boolean isAllEmpty = true;
                for (Object o : firstLine) {
                    isAllStr = isAllStr && (o instanceof String);
                    isAllEmpty = isAllEmpty && (o instanceof String && StringUtils.isEmpty(o.toString()));
                }

                if (isAllStr) {
                    needTranspose = !isAllEmpty;
                }
            }

            if (chart.containsKey("dataSource") && !switchRC) {
                needTranspose = false;
            }

            if (needTranspose) {
                rows = transpose(rows);
            }
        }

        SimpleDateFormat dtFormatter = null;
        if ("bubble".equals(chartType)) {
            List<Document> seriesData = new ArrayList<>();
            for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
                List<Object> row = rows.get(rowIndex);
                Object x = row.get(0);
                Object y = row.get(1);
                Object z = row.get(2);

                Document dataPoint = new Document("x", x)
                        .append("y", y)
                        .append("z", z);

                seriesData.add(dataPoint);
            }
            list.add(new Document("data", seriesData));
        } else {
            boolean isCategory = true;
            if ("datetime".equals(xType) || "line".equals(xType)) {
                isCategory = false;
            }
            List<Object> firstLine = rows.get(0);
            int width = firstLine.size();
            for (int colIndex = 1; colIndex < width; colIndex++) {
                String seriesName = firstLine.get(colIndex).toString();
                List<Document> seriesData = new ArrayList<>();
                for (int rowIndex = startRowIndex; rowIndex < rows.size(); rowIndex++) {
                    List<Object> row = rows.get(rowIndex);
                    if (colIndex >= row.size()) {
                        continue;
                    }

                    Object x = row.get(0);
                    if (!isCategory && !"table".equals(chartType)) {
                        if (x != null && !(x instanceof Number)) {
                            if (NumberUtils.isCreatable(x.toString())) {
//                                x= NumberUtils.createLong(x.toString());
                                x = NumberUtils.createDouble(x.toString()).longValue();
                            } else {
                                if (dtFormatter == null) {
                                    String format = CommonUtil.determineDateFormat(x.toString());
                                    if (format != null) {
                                        if (dtFormatter == null) {
                                            dtFormatter = new SimpleDateFormat(format);
                                        }
                                    }
                                }

                                try {
                                    x = dtFormatter.parse(x.toString()).getTime();
                                } catch (Exception e) {
                                    logger.warn("failed to format date: " + x);
                                }
                            }
                        }
                    }
                    Object y = row.get(colIndex);
//                    if (y != null && !"table".equals(chartType)) {
//                        y = NumberUtils.createFloat(y.toString());
//                    }
                    if (y != null && !"table".equals(chartType)) {
                        if (y.toString().equals("")) {
                            y = null;
                        }
                        else if (NumberUtils.isCreatable(y.toString())) {
                            y = NumberUtils.createFloat(y.toString());
                        } else {
                            continue;
                        }
                    }
                    Document pointData = new Document();
                    if (isCategory) {
                        pointData.append("name", x);
                    } else {
                        pointData.append("x", x);
                    }
                    if (y != null) {
                        pointData.append("y", y);
                    }

                    String markerId = getMarkerId(chartId, colIndex-1, x);
                    Document marker = getPointMarker(markerDoc, markerId);
                    if (marker != null) {
                        pointData.append("marker", marker);
                    }

                    seriesData.add(pointData);
                }

                if (!isCategory) {
                    seriesData.sort((doc1, doc2) -> {
                        Object oa = doc1.get("x");
                        Object ob = doc2.get("x");

                        if (oa == null && ob == null) {
                            return 0;
                        }
                        if (oa == null) {
                            return -1;
                        } else if (ob == null) {
                            return 1;
                        } else {
                            BigDecimal a = new BigDecimal(oa.toString());
                            BigDecimal b = new BigDecimal(ob.toString());

                            return a.compareTo(b);
                        }
                    });
                }
                Document series = new Document()
                        .append("name", seriesName)
                        .append("data", seriesData);

                list.add(series);
                //list.add(new Document("data", seriesData));
            }
        }

        return list;
    }


    public static boolean isChartTable(Document chart) {
        boolean isTable = false;
        if (chart.containsKey("chartLib")) {
            isTable = "custom".equals(chart.getString("chartLib"));
        }
        if (chart.containsKey("type")) {
            String chartType = chart.getString("type");
            if (chartType.equals("live_chart")) {
                Document optionsDoc = chart.get("options", Document.class);
                if (optionsDoc!=null){
                    if (optionsDoc.containsKey("chart")) {
                        Document optChart = optionsDoc.get("chart", Document.class);
                        if (optChart.containsKey("type")) {
                            String optChartType = optChart.getString("type");
                            //logger.info("chart type: " + optChartType);
                            if (optChartType.contains("map")) {
                                //logger.info("contain map");
                                return true;
                            }
                        }
                    }
                }

            }
        }
        if (chart.containsKey("chartType")) {
            if ("table".equals(chart.getString("chartType"))) {
                return true;
            }
        }
        if (chart.containsKey("my_chart")) {
            Document chartInfo = chart.get("chartInfo", Document.class);
            if (chartInfo != null) {
                Document chartOption = chartInfo.get("chart", Document.class);
                if (chartOption != null) {
                    isTable = "table".equals(chartOption.getString("type"));
                }
            }
        }

        return isTable;
    }

    public static Document queryCustomData2(long chartId, String timepoint) throws Exception {
        Document chart = getChart(chartId, true);
        if (chart == null) {
            throw new NotFoundException();
        }

//        logger.info("query custom data start: " + chartId);
        Date start = new Date();
        Document dataTable = queryCustomData(chartId, timepoint);
//        logger.info("query custom data done: " + chartId);

        boolean isTable = isChartTable(chart);
        if (!isTable) {
            Object series = dataTableToSeriesData(chart, dataTable);
//            if (dataTable.containsKey("data")) {
//                //dataTable.remove("data");
//            }
            dataTable.append("series", series);
        }
        return dataTable;

    }
    private static Document getCustomApiData(String target, List<Document> defaultParams) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Document p : defaultParams) {
            sb.append(p.getString("name"))
                    .append("=")
                    .append(URLEncoder.encode(p.getString("value"), "UTF-8"))
                    .append("&");
        }
        sb.deleteCharAt(sb.length() - 1);

        String url = String.format("%s?%s", target, sb.toString());

        Date start = new Date();
        String data = getApiCacheData(url);
        //String data = getApiData(url);
        Date stop = new Date();
        logger.info(String.format("query chart data use time: %d ms %s",  TimeUtil.getTimeInterval(start, stop), url) );

        return Document.parse(data);
    }

    public static void main(String[] args) {
//        MongoUtil.init("mongodb://10.12.0.30:27017", "apes");
//
//        Document p = new Document();
//        p.put("seat", "A00003");
//        p.put("stock", "0700");

        try {
//            Document data = queryApiDataById(60395L, "{ \"seat\" : \"A00003\" , \"stock\" : \"0700\"}");
//            Document data = queryApiDataById(60395L, "");
//            System.out.println(JSON.serialize(data));

            List<Document> params = Arrays.asList(
                new Document("name", "date").append("value", ""),
                new Document("name", "lang").append("value", ""),
                new Document("name", "market").append("value", "google-play")
            );
            Document data = getCustomApiData("http://10.12.6.6:11000/1/cmcm/game_download_rank_line_chart?date=&lang=", params);
            System.out.println(JSON.serialize(new Document("table", data)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
