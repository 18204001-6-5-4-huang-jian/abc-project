package com.abcft.apes.vitamin.controllers;

import com.abcft.apes.vitamin.util.CommentUtil;
import com.abcft.apes.vitamin.util.MongoUtil;
import com.abcft.apes.vitamin.util.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.annotation.security.PermitAll;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by zhyzhu on 17-7-31.
 */
@PermitAll()
@Path("/")
public class CommentController extends BaseController {

    private static Logger logger = Logger.getLogger(CommentController.class);

    /**
     * 获取看板未读取数
     * @return
     */
    @GET
    @Path("v1/comment/count/{bid: [\\d\\w]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getCommentCount(
            @PathParam("bid") String boardId
    ) {
        try {
            String userId = getCurrentUserId();
            if (StringUtils.isEmpty(boardId)) {
                return getResponse(false, 3, "参数错错误");
            }

            Document res = CommentUtil.getCommentCount(boardId, userId);
            if (res == null) {
                return getResponse(false, 2, "获取未读取评论数失败");
            }

            return getResponse(true, res);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return getResponse(false, 1, "获取未读消息失败");
    }

    /**
     * 获取评论列表
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    @GET
    @Path("v1/comments/{bid: [\\d\\w]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getComments(
            @PathParam("bid") String boardId,
            @DefaultValue("") @QueryParam("cid") String commentId,
            @DefaultValue("") @QueryParam("c") String category,
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit
    ) {
        try {
            String userId = getCurrentUserId();
            if (StringUtils.isEmpty(boardId)) {
                return getResponse(false, 3, "参数错错误");
            }

            Document res = CommentUtil.getComments(boardId, commentId, userId, category, sort, order, lang, offset, limit, query);
            if (res == null) {
                return getResponse(false, 2, "获取未读取评论数失败");
            }

            return getResponse(true, res);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return getResponse(false, 1, "获取产品列表失败");
    }

    /**
     * 搜索评论
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    @GET
    @Path("v1/search/{bid: [\\d\\w]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject search(
            @PathParam("bid") String boardId,
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("") @QueryParam("c") String category,
            @DefaultValue("") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit
    ) {
        try {
            String userId = getCurrentUserId();
            if (StringUtils.isEmpty(boardId)) {
                return getResponse(false, 3, "参数错错误");
            }

            Document res = CommentUtil.search(boardId, query, category, sort, order, offset, limit);
            if (res == null) {
                return getResponse(false, 2, "获取未读取评论数失败");
            }

            return getResponse(true, res);

        } catch (Exception e) {
        }

        return getResponse(false, 1, "获取产品列表失败");
    }

    /**
     * 创建评论
     * 若包含主评论id，则创建的为一级子评论
     * 若还包含一级子评论id，则创建的为二级子评论
     * @param commentId 主评论id
     * @param sonId     一级子评论id
     * @return
     */
    @Path("v1/comment/create/{bid: [\\d\\w]+}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject createComment(
            @PathParam("bid") String boardId,
            @DefaultValue("") @QueryParam("cid") String commentId,
            @DefaultValue("") @QueryParam("sid") String sonId,  //一级子评论ID
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("0") @QueryParam("anon") int anon,
            JsonObject commentJson
    ){
        try {
            String userId = getCurrentUserId();
            if (StringUtils.isEmpty(boardId)) {
                return getResponse(false, 2, "参数错错误");
            }

            Document comment = MongoUtil.json2Document(commentJson);
            if (commentJson.containsKey("anon")) {
                anon = commentJson.getInt("anon");
            }
            if (commentJson.containsKey("cid")) {
                commentId = commentJson.getString("cid");
            }

            Document res = CommentUtil.createComment(boardId, userId, commentId, sonId, lang, anon, comment);
            if (res == null) {
                return getResponse(false, 2, "创建评论失败");
            }

            return getResponse(true, res);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return getResponse(false, 1, "创建评论失败");
    }

    /**
     * 删除评论
     * @return
     */
    @Path("v1/comment/delete/{cid: [\\d\\w]+}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject deleteComment(
            @PathParam("cid") String commentId
    ){
        String userId = getCurrentUserId();

        if (!CommentUtil.authDeleteComment(userId, commentId)) {
            return getResponse(false, 2, "删除评论失败, 无操作权限");
        }

        Document data = CommentUtil.deleteComment(commentId);
        if (!data.getBoolean("res")) {
            return getResponse(false, 3, "删除评论失败");
        }

        return getResponse(true, data);
    }

    /**
     * 判断是否拥有设置常见问题权限
     * @return
     */
    @Path("v1/comment/have-setcommon")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject haveSetCommonRight() {
        String userId = getCurrentUserId();
        boolean res = CommentUtil.authSetCommon(userId);
        return getResponse(res, res ? "当前用户可以设置常见问题" : "当前用户不能设置常见问题");
    }

    /**
     * 点赞
     * @return
     */
    @Path("v1/comment/like/{cid: [\\d\\w]+}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject likeComment(
            @PathParam("cid") String commentId
    ){
        try {
            String userId = getCurrentUserId();

            boolean res = CommentUtil.updateLike(userId, commentId);
            if (!res) {
                return getResponse(false, 2, "点赞失败");
            }

            return getResponse(true);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return getResponse(false, 1, "创建评论失败");
    }

    /**
     * 设置常见问题
     * @return
     */
    @Path("v1/comment/set-common/{cid: [\\d\\w]+}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject setCommon(
            @PathParam("cid") String commentId
    ){
        try {
            String userId = getCurrentUserId();

            boolean res = CommentUtil.updateCommon(userId, commentId);
            if (!res) {
                return getResponse(false, 2, "设置常见问题失败");
            }

            return getResponse(true);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return getResponse(false, 1, "设置常见问题失败");
    }

    /**
     * 更新评论
     * @param commentId
     * @param commentJson
     * @return
     */
    @Path("v1/comment/update/{cid: [\\d\\w]+}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject updateComment(
            @PathParam("cid") String commentId,
            JsonObject commentJson
    ) {
        try{
            Document comment = MongoUtil.json2Document(commentJson);
            Document res = CommentUtil.updateComment(commentId, comment);
            if (res != null)
                return getResponse(true, res);
            return getResponse(false, 2, "更新评论失败");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return getResponse(false, 1, "更新评论失败");
    }

    /**
     * 获取消息通知
     * @return
     */
    @Path("v1/comment/notices")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getNotices(
            @DefaultValue("create_at") @QueryParam("sort") String sort,
            @DefaultValue("desc") @QueryParam("order") String order,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("20") @QueryParam("limit") int limit,
            @DefaultValue("") @QueryParam("lang") String lang
    ) {
        String userId = getCurrentUserId();
        Document data = CommentUtil.getNotices(userId, sort, order, offset, limit, lang);
        if (data != null) {
            return getResponse(true, data);
        }
        return getResponse(false, "没有新的消息");
    }
}
