package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.AccountUtil;
import com.abcft.apes.vitamin.util.InviteCodeUtil;
import com.abcft.apes.vitamin.util.MongoUtil;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by zhyzhu on 17-4-26.
 */
@Path("/")
public class InviteCodeController extends BaseController {

    private static Logger logger = Logger.getLogger(InviteCodeController.class);

    /**
     * 生成邀请码
     * @return
     */
    @GET
    @Path("v1/invite-code/create")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject createInviteCode(
        @DefaultValue("0") @QueryParam("discount") int discount,       //折扣
        @DefaultValue("0") @QueryParam("abate") int abate

    ) {
        String userId = getCurrentUserId();
        if (!AccountUtil.isSuperAdmin(userId)) {
            return getResponse(false, 1, "无操作权限");
        }

        String code = InviteCodeUtil.createInviteCode(discount, abate);
        Document data = new Document("code", code);

        return getResponse(true, data);
    }

    /**
     * 验证邀请码
     * @return
     */
    @POST
    @Path("v1/invite-code/verify")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject verifyInviteCode(
        @FormParam("price") int price,
        @FormParam("email") String email,
        @FormParam("code") String code
    ) {
        Document data = InviteCodeUtil.verifyInviteCode(email, code, price);
        if (data == null) {
            return getResponse(false, 1, "邀请码错误");
        }

        return getResponse(true, data);
    }

    /**
     * 邀请码列表
     * @return
     */
    @GET
    @Path("v1/invite-codes")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getInviteCodes(
        @DefaultValue("") @QueryParam("sort") String sort,
        @DefaultValue("asc") @QueryParam("order") String order,
        @DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("50") @QueryParam("limit") int limit
    ) {
        try {
            Document document = MongoUtil.getDBDocList(MongoUtil.INVITE_CODE_COL, sort, order, offset, limit);
            if (document==null || document.isEmpty()) {
                return getResponse(false, 2, "获取订单信息失败");
            }

            return getResponse(true, document);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return getResponse(false, 1, "获取产品列表错误");
    }
}
