package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.PlanUtil;
import com.abcft.apes.vitamin.util.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by zhyzhu on 17-4-23.
 */
@Path("/")
public class PlanController extends BaseController {

    private static Logger logger = Logger.getLogger(PlanController.class);

    /**
     * 方案列表
     *
     * @param sort
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param query
     * @return
     */
    @GET
    @Path("v1/plans")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getPlans(
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("") @QueryParam("q") String query
    ) {
        try {
            if (!StringUtils.isEmpty(lang)) {
                lang = "_" + lang;
            }
            Document document = PlanUtil.getPlans(sort, order, offset, limit, query, lang);
            if (document == null || document.isEmpty()) {
                return getResponse(false, 1, "获取订单信息失败");
            }

            return getResponse(true, document);
        } catch (Exception e) {
            logger.error(e);
        }

        return getResponse(false, 1, "获取订单列表失败");
    }


    /**
     * 获取方案
     *
     * @param lang
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param query
     * @param pids   产品id列表
     * @return
     */
    @GET
    @Path("v1/plans2")
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getPlans2(
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("") @QueryParam("q") String query,
            @NotNull @QueryParam("pid") List<String> pids
    ) {
        try {
            if (!StringUtils.isEmpty(lang)) {
                lang = "_" + lang;
            }
//            logger.info(Arrays.toString(pids.toArray()));
            Document document = PlanUtil.getPlans2(sort, order, offset, limit, query, lang, pids);
            if (document == null || document.isEmpty()) {
                return getResponse(false, 1, "获取订单信息失败");
            }

            return getResponse(true, document);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(false, 1, "获取订单列表失败" + StringUtils.showError(e));
        }
    }
}
