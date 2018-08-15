package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.AccountUtil;
import com.abcft.apes.vitamin.util.ChartUtil;
import com.abcft.apes.vitamin.util.MongoUtil;
import com.abcft.apes.vitamin.util.StringUtils;
import com.mongodb.util.JSON;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by zhyzhu on 17-4-26.
 */
@Path("v1/chart")
public class ChartController extends BaseController {

    private static Logger logger = Logger.getLogger(ChartController.class);
    public static String CHART_API_URL = "http://10.12.0.30:11000";

    /**
     * 查询图表数据
     *
     * @param id
     * @param parameters
     * @param tqx
     * @return
     */
    @GET
    @Path("query-data")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject queryData(
            @QueryParam("id") long id,
            @QueryParam("parameters") String parameters,
            @QueryParam("tqx") String tqx) {
        try {
            String userId = getCurrentUserId();

            if (StringUtils.isEmpty(userId)) {
                return getResponse(false, 1, "无访问该图表数据权限");
            }

            //登录后, 是否有权限
            Document table = ChartUtil.getChartDataTable(id, parameters);
            if (table == null) {
                logger.error("query chart data error");
                return getResponse(false, 2, "获取图表数据失败");
            }

            Document data = new Document();
            data.put("table", table);
            if (!StringUtils.isEmpty(tqx) && tqx.startsWith("reqId:")) {
                data.put("reqId", tqx.substring("reqId:".length()));
            }

            return MongoUtil.document2Json(getResponseDoc(true, data));
        } catch (Exception e) {
            logger.error("Failed to query data.", e);
        }

        return getResponse(false, 3, "获取图表数据失败");
    }

    @GET
    @Path("/{id: \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject get(
            @PathParam("id") long id,
            @DefaultValue("0") @QueryParam("detail") int detail
    ) {
        String uid = getCurrentUserId();
        /*
        if (!ChartUtil.isCreator(id, uid)) {
            getResponse(false, 2, "无访问该图表的权限");
        }
        */

        Document chart = ChartUtil.getChart(id, detail == 1);
        if (chart == null) {
            throw new NotFoundException();
        }

        return getResponse(true, chart);
    }

    @GET
    @Path("query-param-data")
    @Produces(MediaType.APPLICATION_JSON)
    public String queryAPIData(
            @QueryParam("id") long _id,
            @QueryParam("parameters") @DefaultValue("") String parameters
    ) {
        Document result = new Document();
        try {
            Document data = ChartUtil.queryApiDataById(_id, parameters);

            result.put("success", true);
            result.put("message", "");
            result.put("table", data);
        } catch (Exception e) {
            logger.error("Failed to query data.", e);

            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return JSON.serialize(result);
    }

    @GET
    @Path("query-custom-data")
    @Produces(MediaType.APPLICATION_JSON)
    public String queryCustomData(
            @QueryParam("id") long _id,
            @DefaultValue("") @QueryParam("tp") String timePoint
    ) {
        Document result = new Document();
        try {
            long start = System.currentTimeMillis();
            Document table = ChartUtil.queryCustomData2(_id, timePoint);
            long end = System.currentTimeMillis();
            float costTime = (end - start) / 1000f;
            logger.info(String.format("[QUERY_CUSTOM_DATA] cost %s secs, ID: <%s>, TP: <%s>", costTime, _id, timePoint));
            result.put("success", true);
            result.put("message", "");
            result.put("table", table);
        } catch (Exception e) {
            logger.error("Failed to query custom data. id = " + _id, e);

            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return JSON.serialize(result);
    }

    /**
     * 设置数据点标注
     *
     * @return
     */
    @POST
    @Path("set-desp/{id: \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setDescription(
            @PathParam("id") long chartId,
            JsonObject postData
    ) {
        Document despData = MongoUtil.json2Document(postData);
        String uid = getCurrentUserId();

        Object despObject = despData.getOrDefault("description", "");

        if (ChartUtil.setDescription(chartId, despObject)) {
            return getResponse(true);
        }

        return getResponse(false, "增加描述失败");
    }

    /**
     * 设置数据点标注
     *
     * @return
     */
    @POST
    @Path("set-marker/{id: \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setMarker(
            @PathParam("id") long chartId,
            JsonObject postData
    ) {
        Document markerData = MongoUtil.json2Document(postData);
        String uid = getCurrentUserId();

        String markerId = null;
        Document marker = markerData.get("marker", Document.class);
        if (!markerData.containsKey("mid")) {
            if (!markerData.containsKey("series_id") ||
                    !markerData.containsKey("point_id")) {
                return getResponse(false, 1, "参数错误");
            }
            Object seriesId = markerData.get("series_id");
            Object pointId = markerData.get("point_id");
            Object seriesName = markerData.get("series_name");
            if (ChartUtil.setSeriesDataMarker(uid, chartId, seriesId, seriesName, pointId, marker)) {
                return getResponse(true);
            }
        } else {
            markerId = markerData.getString("mid");
            if (ChartUtil.setSeriesDataMarker(uid, chartId, markerId, marker)) {
                return getResponse(true);
            }
        }


        return getResponse(false, "增加标注失败");
    }

    /**
     * 取消数据点标注
     *
     * @return
     */
    @POST
    @Path("unset-marker/{id: \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject unsetMarker(
            @PathParam("id") long chartId,
            JsonObject postData
    ) {
        Document markerData = MongoUtil.json2Document(postData);
        String uid = getCurrentUserId();

        if (!markerData.containsKey("series_id") ||
                !markerData.containsKey("point_id")) {
            return getResponse(false, 1, "参数错误");
        }

        Object seriesId = markerData.get("series_id");
        Object pointId = markerData.get("point_id");

        String markerId = ChartUtil.getMarkerId(chartId, seriesId, pointId);
        if (ChartUtil.unsetSeriesDataMarker(markerId)) {
            return getResponse(true);
        }

        return getResponse(false, "增加标注失败");
    }

    /**
     * 获取marker列表
     *
     * @return
     */
    @GET
    @Path("markers/{id: \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getMarkers(
            @PathParam("id") long chartId,
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit
    ) {
        String cuid = getCurrentUserId();
        if (!AccountUtil.isSuperAdmin(cuid)) {
            return getResponse(false, 2, "无操作权限");
        }

        try {
            Document data = ChartUtil.getMarkers(chartId, sort, order, offset, limit);
            if (data == null || data.isEmpty()) {
                return getResponse(false, 3, "获取订单失败");
            }

            return getResponse(true, data);
        } catch (Exception e) {
            logger.error(e);
        }

        return getResponse(false, 1, "获取订单列表错误");
    }
}
