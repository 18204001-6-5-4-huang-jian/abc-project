package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.*;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by zhyzhu on 17-9-12.
 */
@Path("/")
public class ReportController extends BaseController {

    private static Logger logger = Logger.getLogger(ReportController.class);

    /**
     * 获取报告列表
     *
     * @param lang
     * @param category
     * @param query
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    @GET
    @Path("v1/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getReports(
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("") @QueryParam("category") String category,
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("date") @QueryParam("sort") String sort,
            @DefaultValue("desc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("minute") @QueryParam("cache_time_unit") String cacheTimeUnit,
            @DefaultValue("5") @QueryParam("cache_expiry") int cacheExpiry
    ) {
        try {
            String cuid = getCurrentUserId();
            long s = System.currentTimeMillis();
            Document data = ReportUtil.getCachedReports(
                    cuid, lang, category, query, sort, order, offset, limit,
                    cacheTimeUnit, cacheExpiry);
            long e = System.currentTimeMillis();
//            logger.info("get report time: " + (e - s) / 1000.0);
            if (data == null) {
                return getResponse(false, 2, "获取日报失败");
            }
            return getResponse(true, data);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "获取日报失败" + StringUtils.showError(e));
        }

    }

    /**
     * 设置免费周
     *
     * @param start
     * @param stop
     * @return
     */
    @Path("v1/report/free-date")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setFreeExpiry(
            @QueryParam("start") String start,
            @QueryParam("stop") String stop
    ) {
        try {

            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 1, "无操作权限");
            }
            boolean res = ReportUtil.setFreeExpiry(start, stop);
            if (res) {
                return getResponse(true, "设置免费周成功");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return getResponse(false, "设置免费周失败");
    }

    /**
     * 设置免费日报
     *
     * @param category
     * @param type
     * @param start
     * @param stop
     * @return
     */
    @Path("v1/report/free-report")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setFreeReports(
            @DefaultValue("") @QueryParam("name") String name,
            @DefaultValue("") @QueryParam("category") String category,
            @DefaultValue("") @QueryParam("type") String type,
            @QueryParam("start") String start,
            @QueryParam("stop") String stop
    ) {
        try {

            String cuid = getCurrentUserId();
            if (!AccountUtil.isSuperAdmin(cuid)) {
                return getResponse(false, 1, "no operation authority");
            }
            if (name.isEmpty() && category.isEmpty() && type.isEmpty()) {
                return getResponse(false, 2, "params error");
            }
            List<String> names = null, categorys = null;
            if (!name.isEmpty()) {
                names = StringUtils.CSV2List(name);
            }
            if (!category.isEmpty()) {
                categorys = StringUtils.CSV2List(category);
            }
            boolean res = ReportUtil.setFreeReports(names, categorys, type, start, stop);
            if (res) {
                return getResponse(true, "设置免费日报成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return getResponse(false, 1, "设置免费日报失败");
    }

    /**
     * 获取季报日历
     *
     * @param date
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    @Path("v1/reports/calendar")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getQuarterlyReportCalendar(
            @DefaultValue("") @QueryParam("date") String date,
            @DefaultValue("") @QueryParam("pid") String pid,
            @DefaultValue("pub_date") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("10") @QueryParam("limit") int limit,
            @DefaultValue("minute") @QueryParam("cache_time_unit") String cacheTimeUnit,
            @DefaultValue("5") @QueryParam("cache_expiry") int cacheExpiry,
            @DefaultValue("true") @QueryParam("cached") boolean cached,
            @DefaultValue("") @QueryParam("lang") String lang
    ) {
        try {
            Date dateDate = getQRDateParam(date, lang);

            pid = pid.trim();
            if (pid.isEmpty()) {
                pid = null;
            }
            String key = RedisUtil.getRedisKey("getQuarterlyReportCalendar" +
                    String.valueOf(date) + String.valueOf(pid) + sort + order + offset + limit +
                    cacheTimeUnit + cacheExpiry + lang
            );
            Document document = null;
            if (cached) {
                document = RedisUtil.getDocument(key);
            }
            if (document == null) {
                document = ReportUtil.getQuarterlyReportCalendar(
                        dateDate, pid, sort, order, offset, limit, lang);
            }

            if (document == null) {
                return getResponse(false, 1, "get session report calendar failed");
            }

            if (document.containsKey("error")) {
                return getResponse(false, 2, document.getString("error"));
            }

            RedisUtil.set(key, document, TimeUtil.strToTimeUnit(cacheTimeUnit), cacheExpiry);
            return getResponse(true, document);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    private Date getQRDateParam(String date, String lang) {
        if (!date.isEmpty()) {
            try {
                date = StringUtils.isCN(lang) ?
                        date :
                        date.substring(0, 3) + " " + date.replaceAll("\\w+\\s", "");
//                    logger.info(date);
                return TimeUtil.
                        strToDate(date, StringUtils.isCN(lang), "yyyy-MM", "yyyy年MM月", "MMM yyyy");
            } catch (Exception e) {
                logger.error(e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 获取季报日历的通知
     *
     * @return
     */
    @Path("v1/reports/calendar-notify")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getQuarterlyReportCalendarNotify(
            @DefaultValue("0") @QueryParam("quarterly") int flag,   //用于判断用户登录期间消息数的显示
            @DefaultValue("update_date") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("minute") @QueryParam("cache_time_unit") String cacheTimeUnit,
            @DefaultValue("5") @QueryParam("cache_expiry") int cacheExpire,
            @DefaultValue("true") @QueryParam("cached") boolean cached,
            @DefaultValue("") @QueryParam("lang") String lang
    ) {
        try {

            String cuid = getCurrentUserId();
            boolean isAnalyst = AccountUtil.isAnalyst(cuid);
            Document resDoc = null;
            String key = RedisUtil.getRedisKey("getQuarterlyReportCalendarNotify" +
                    cuid + isAnalyst + flag + sort + order + offset + limit +
                    cacheTimeUnit + cacheExpire + cached + lang);
//            logger.info("cached" + cached);
            if (cached) {
                resDoc = RedisUtil.getDocument(key);
            }
            if (resDoc == null) {
                resDoc = ReportUtil.getQuarterlyReportCalendarNotify(
                        cuid, isAnalyst, flag,
                        sort, order, offset, limit,
                        lang
                );
            }
            if (resDoc == null) {
                return getResponse(false, 1, "get session report calendar notify failed");
            }

            if (resDoc.containsKey("error")) {
                return getResponse(false, 2,
                        resDoc.getOrDefault("error", "").toString());
            }

            RedisUtil.set(key, resDoc, TimeUtil.strToTimeUnit(cacheTimeUnit), cacheExpire);
            return getResponse(true, resDoc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }

    @Path("v1/reports/calendar-head")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getQuarterlyReportCalenderHead(
            @DefaultValue("minute") @QueryParam("cache_time_unit") String cacheTimeUnit,
            @DefaultValue("5") @QueryParam("cache_expiry") int cacheExpiry,
            @DefaultValue("true") @QueryParam("cached") boolean cached,
            @DefaultValue("") @QueryParam("lang") String lang
    ) {
        try {
            Document resDoc = null;
            String key = RedisUtil.getRedisKey("getQuarterlyReportCalenderHead"
                    + cacheTimeUnit + cacheExpiry + lang);
            if (cached) {
                resDoc = RedisUtil.getDocument(key);
            }
            if (resDoc == null) {
                resDoc = ReportUtil.getQuarterlyReportCalendarHead(lang);
            }
            if (resDoc == null) {
                return getResponse(false, 1, "get calendar head failed");
            }
            RedisUtil.set(key, resDoc, TimeUtil.strToTimeUnit(cacheTimeUnit), cacheExpiry);
            return getResponse(true, resDoc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 0, StringUtils.showError(e));
        }
    }
}
