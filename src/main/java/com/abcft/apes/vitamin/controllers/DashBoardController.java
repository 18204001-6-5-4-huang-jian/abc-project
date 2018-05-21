package com.abcft.apes.vitamin.controllers;


import com.abcft.apes.vitamin.util.StringUtils;
import org.apache.log4j.Logger;

import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by zhyzhu on 17-4-23.
 */
@Path("/")
public class DashBoardController extends BaseController {

    private static Logger logger = Logger.getLogger(DashBoardController.class);

    /**
     * 获取看板数据
     * @param id
     * @return
     */
    @GET
    @Path("v1/dash-boards/{id: [\\d\\w]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getDashboard(
        @PathParam("id") String id
    ) {
        String userId = getCurrentUserId();
        if (StringUtils.isEmpty(id)) {
            return getResponse(false, 3, "参数错错误");
        }

        /*
        Document board = DashBoardUtil.getDashboard(id);
        if (board == null) {
            Document resp = new Document();
            resp.put("id", id);
            return getResponse(false, 1, "访问看板错误", MongoUtil.document2Json(resp));
        }

        if (!DashBoardUtil.isAuth(id, userId)) {
            return getResponse(false, 2, "");
        }

        return getResponse(true, MongoUtil.document2Json(board));
        */
        return getResponse(true);
    }
}
