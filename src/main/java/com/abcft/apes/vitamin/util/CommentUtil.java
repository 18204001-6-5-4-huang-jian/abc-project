package com.abcft.apes.vitamin.util;

import com.mongodb.client.model.Filters;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.*;

/**
 * Created by zhyzhu on 17-7-31.
 */
public class CommentUtil {
    private static Logger logger = Logger.getLogger(CommentUtil.class);

    public enum Langs {
        ZH_CN("zh_CN");

        private String name;

        Langs(String name) {
            this.name = name;
        }
    }
    public static int getNewCommentCount(String boardId, String userId) {
        List<Bson> conds1 = Arrays.asList(
                new Document("board_id", boardId),
                new Document("user_id", userId)
        );
        Document latestDoc = MongoUtil.getOneByConditions(MongoUtil.COMMENT_READ_COL, conds1);
        Date latestTime = null;
        if (latestDoc != null && latestDoc.containsKey("hot")) {
            latestTime = latestDoc.getDate("hot");
        }

        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("common", 0));
        conds.add(Filters.exists("comment_user_id", false));
        conds.add(Filters.eq("board_id", boardId));
        if (latestTime != null) {
            conds.add(Filters.gt("create_at", latestTime));
        }
        return MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conds);
    }

    public static int getNewCommonCommentCount(String boardId, String userId) {
        List<Bson> conds1 = Arrays.asList(
                new Document("board_id", boardId),
                new Document("user_id", userId)
        );
        Document latestDoc = MongoUtil.getOneByConditions(MongoUtil.COMMENT_READ_COL, conds1);
        Date latestTime = null;
        if (latestDoc != null && latestDoc.containsKey("hot")) {
            latestTime = latestDoc.getDate("hot");
        }

        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("common", 1));
        conds.add(Filters.eq("board_id", boardId));
        conds.add(Filters.exists("comment_user_id", false));
        if (latestTime != null) {
            conds.add(Filters.gt("create_at", latestTime));
        }
        return MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conds);
    }

    public static int getToMeCommentCount(String boardId, String userId) {
        List<Bson> conds1 = Arrays.asList(
                Filters.eq("board_id", boardId),
                Filters.eq("user_id", userId)
        );
        Document latestDoc = MongoUtil.getOneByConditions(MongoUtil.COMMENT_READ_COL, conds1);
        Date latestTime = null;
        if (latestDoc != null && latestDoc.containsKey("to_me")) {
            latestTime = latestDoc.getDate("to_me");
        }

        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("board_id", boardId));
        conds.add(Filters.eq("comment_user_id", userId));
        if (latestTime != null) {
            conds.add(Filters.gt("create_at", latestTime));
        }
        return MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conds);
    }

    /**
     * 获取未读取的赞
     *
     * @param boardId
     * @param userId
     * @return
     */
    public static int getLikeCommentCount(String boardId, String userId) {
        List<Bson> conds1 = Arrays.asList(
                new Document("board_id", boardId),
                new Document("user_id", userId)
        );
        Document latestDoc = MongoUtil.getOneByConditions(MongoUtil.COMMENT_READ_COL, conds1);
        Date latestTime = null;
        if (latestDoc != null && latestDoc.containsKey("like")) {
            latestTime = latestDoc.getDate("like");
        }

        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("board_id", boardId));
        conds.add(Filters.eq("comment_user_id", userId));
        if (latestTime != null) {
            conds.add(Filters.gt("create_at", latestTime));
        }
        return MongoUtil.getDBCount(MongoUtil.COMMENT_LIKE_COL, conds);
    }

    /**
     * 获取未读取的评论数
     *
     * @param boardId
     * @param userId
     * @return
     */
    public static Document getCommentCount(String boardId, String userId) {

        int newLatestCommentCount = getNewCommentCount(boardId, userId);
        int newCommonCommentCount = getNewCommonCommentCount(boardId, userId);
        int newAllCommentCount = newLatestCommentCount + newCommonCommentCount;
        int newToMeCommentCount = getToMeCommentCount(boardId, userId);
        int newLikeCommentCount = getLikeCommentCount(boardId, userId);
        int newMeCommentCount = (newToMeCommentCount + newLikeCommentCount);
        int newTotalCommentCount = (newAllCommentCount + newMeCommentCount);

        Document res = new Document()
                .append("total", newTotalCommentCount)
                .append("all", newAllCommentCount)
                .append("hot", newLatestCommentCount)
                .append("latest", newLatestCommentCount)
                .append("common", newCommonCommentCount)
                .append("to_me", newToMeCommentCount)
                .append("like", newLikeCommentCount);

        return res;
    }

    /**
     * 判断中文
     * @param lang
     * @return
     */
    public static boolean iszh_CN(String lang){return lang.equalsIgnoreCase(Langs.ZH_CN.name);}
    public static String getAnnonName(String userId, String boardId, String lang) {
        boolean iszhCN = iszh_CN(lang);
        if (iszhCN){lang = Langs.ZH_CN.name;}
        List<Bson> cond = Arrays.asList(
                Filters.eq("user_id", userId),
                Filters.eq("board_id", boardId)
        );
        //获取用户匿名昵称记录
        Document AnonDoc = MongoUtil.getOneByConditions(MongoUtil.USER_ANON_NICKNAME_COL, cond);
        //若记录存在
        if (AnonDoc != null) {
            //若记录中不存在该语种的昵称,返回英文昵称
            if (StringUtils.isEmpty(AnonDoc.getString("anon_name_" + lang))) {
                return AnonDoc.getString("anon_name");
            } else {
                return AnonDoc.getString("anon_name_" + lang);
            }
        } else {
            int count = MongoUtil.getDBCount(MongoUtil.FAMOUS_PERSON_COL);
            int id = (int)(Math.random() * count);
            Document doc = MongoUtil.getOneByField(MongoUtil.FAMOUS_PERSON_COL, "id", id);
            if (doc==null) {
                return "";
            }
            String anonName = doc.getString("name");
            String anonNameZh = doc.getString("name_zh");
            Document newDoc = new Document()
                    .append("user_id", userId)
                    .append("board_id", boardId)
                    .append("anon_name", anonName)
                    .append("anon_name_" + Langs.ZH_CN.name, anonNameZh);
            MongoUtil.insertOne(MongoUtil.USER_ANON_NICKNAME_COL, newDoc);

            if (iszhCN) {
                return anonNameZh;
            }
            return anonName;
        }
    }

    /**
     * 通过主评论id
     * 获取主评论的用户id
     * @param commentId
     * @return
     */
    public static String getUserIdByCommentId(String commentId) {
        //通过主评论id找到对应评论文档
        Document userDoc = MongoUtil.getOneByField(MongoUtil.COMMENT_COL, "_id", new ObjectId(commentId));
        if (userDoc == null) {
            return null;
        }

        return userDoc.getString("user_id");
    }

    /**
     * 创建评论
     *
     * @param boardId
     * @param userId
     * @param commentId
     * @param lang
     * @param anon
     * @param comment
     * @return
     */
    public static Document createComment(String boardId, String userId, String commentId, String sonId, String lang, int anon, Document comment) {
        //处理lang
        boolean iszhCN = iszh_CN(lang);
        if (iszhCN){lang = Langs.ZH_CN.name;}
        String nickName = getCommenterNickName(userId, boardId, anon, lang);
        //处理评论换行
        StringUtils.processCommentESC(comment);
        Document commentDoc = new Document()
                .append("board_id", boardId)
                .append("user_id", userId)
                .append("anon", anon)
                .append("nickname", nickName)
                .append("common", 0)
                .append("hot", 0)
                .append("like_count", 0)
                .append("comment_count", 0)
                .append("comment", comment.get("comment"));
        try {

            if (!StringUtils.isEmpty(commentId)) {
                commentDoc.append("comment_id", commentId);
                //若有son_id字段，则创建二级子评论
                if (!StringUtils.isEmpty(sonId)) {
                    commentDoc.append("son_id", sonId);
                    incCommentCount(sonId, 1);
                    updateCommentHot(sonId);
                }
                String commentUserId = getUserIdByCommentId(commentId);
                if (commentUserId != null) {
                    commentDoc.append("comment_user_id", commentUserId);
                }
                //更新评论书
                incCommentCount(commentId, 1);

                //更新评论热度
                updateCommentHot(commentId);
            } else {
                //若有son_id字段，则创建二级子评论
                if (!StringUtils.isEmpty(sonId)) {
                    Document sonDoc = MongoUtil.getOneById(MongoUtil.COMMENT_COL, sonId);
                    if (sonDoc != null) {
                        commentId = sonDoc.getString("comment_id");
                        commentDoc.append("comment_id", commentId);
                        commentDoc.append("son_id", sonId);
                        String commentUserId = getUserIdByCommentId(commentId);
                        if (commentUserId != null) {
                            commentDoc.append("comment_user_id", commentUserId);
                        }
                        incCommentCount(sonId, 1);
                        updateCommentHot(sonId);
                        //更新评论书
                        incCommentCount(commentId, 1);

                        //更新评论热度
                        updateCommentHot(commentId);
                    } else {
                        throw new Exception("创建二级子评论失败， 一级子评论不存在");
                    }
                }

            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        Document doc = MongoUtil.insertOne(MongoUtil.COMMENT_COL, commentDoc);
        //若发表子评论，立即返回更新之后的子评论列表
        if (!StringUtils.isEmpty(commentId)) {
            return getSubComments(boardId, commentId, 0, 999, "", lang);
        }

        return doc;
    }

    /**
     * 删除评论权限
     *
     * @param userId
     * @return
     */
    public static boolean authDeleteComment(String userId, String commentId) {
        if (AccountUtil.isSuperAdmin(userId)) {
            return true;
        }
        try {

            List<Bson> conditions = Arrays.asList(
                    Filters.eq("user_id", userId),
                    Filters.eq("_id", new ObjectId(commentId))
            );
            Document doc = MongoUtil.getOneByConditions(MongoUtil.COMMENT_COL, conditions);
            if (doc != null) {
                logger.info(" doc " + doc.toString());
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return false;
    }

    /**
     * 设置常用问题权限
     * @param userId
     * @return
     */
    public static boolean authSetCommon(String userId) {
        if (AccountUtil.isSuperAdmin(userId)) {
            return true;
        }
        try {

            List<Bson> conditions = Arrays.asList(
                    Filters.eq("_id", new ObjectId(userId)),
                    Filters.eq("right.common", 1)
            );
            Document doc = MongoUtil.getOneByConditions(MongoUtil.ACCOUNT_COL, conditions);
            if (doc != null) {
                logger.info(" doc " + doc.toString());
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return false;
    }

    /**
     * 删除评论
     *
     * @param commentId
     * @return
     */
    public static Document deleteComment(String commentId) {
        Document doc = MongoUtil.getOneById(MongoUtil.COMMENT_COL, commentId);
        if (doc != null && doc.containsKey("comment_id")) {
            String super_comment_id = doc.getString("comment_id");
            incCommentCount(super_comment_id, -1);
            //若该评论为二级子评论
            if (doc.containsKey("son_id")) {
                String sonId = doc.getString("son_id");
                //一级子评论的评论量减一
                incCommentCount(sonId, -1);
            }
        }

//        boolean res = MongoUtil.deleteOne(MongoUtil.COMMENT_COL, commentId);
//        MongoUtil.deleteMany(MongoUtil.COMMENT_LIKE_COL, "comment_id", commentId);
        boolean res = MongoUtil.pseudoDeleteOne(MongoUtil.COMMENT_COL, commentId);
        MongoUtil.pseudoDeleteMany(MongoUtil.COMMENT_LIKE_COL, "comment_id", commentId);

        Document data = new Document()
                .append("res", res)
                .append("comment_id", doc.getString("comment_id"));
        return data;
    }

    /**
     * 更新评论数
     *
     * @param commentId
     * @return
     */
    public static void incCommentCount(String commentId, int inc) {
        Document updateDoc = new Document("$inc", new Document("comment_count", inc));
        MongoUtil.updateOneById(MongoUtil.COMMENT_COL, commentId, updateDoc, false);
    }

    /**
     * 更新评论热度
     *
     * @param commentId
     * @return
     */
    public static int updateCommentHot(String commentId) {
        Document commentDoc = MongoUtil.getOneById(MongoUtil.COMMENT_COL, commentId);
        if (commentDoc == null) {
            return 0;
        }
        int likeCount = commentDoc.getInteger("like_count");
        int commentCount = commentDoc.getInteger("comment_count");

        Date today = TimeUtil.getCurDate();
        List<Bson> conditions = Arrays.asList(
                Filters.eq("comment_id", commentId),
                Filters.gte("create_at", today)
        );

        int todayCommentCount = MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conditions);
        int preDayCommentCount = commentCount - todayCommentCount;

        int hot = (likeCount * 1 + preDayCommentCount * 3 + todayCommentCount * 6);

        Document updateDoc = new Document("hot", hot);
        MongoUtil.updateOneById(MongoUtil.COMMENT_COL, commentId, updateDoc);

        return hot;
    }

    /**
     * 更新评论读取时间
     *
     * @param boardId
     * @param userId
     * @param category
     */
    public static void updateCommentReadTime(String boardId, String userId, String category) {
        List<Bson> conds = Arrays.asList(
                Filters.eq("user_id", userId),
                Filters.eq("board_id", boardId)
        );
        Document readComments = new Document()
                .append("user_id", userId)
                .append("board_id", boardId)
                .append(category, new Date());

        MongoUtil.upsertOne(MongoUtil.COMMENT_READ_COL, conds, readComments);

    }

    /**
     * 获取评论者昵称
     *
     * @param userId
     * @return
     */
    public static String getCommenterNickName(String userId, String boardId, int anon, String lang) {
        if (anon == 1) {
            return getAnnonName(userId, boardId, lang);
        }
        return AccountUtil.getNicknameById(userId);
    }

    /**
     * @param commentDoc
     * @return
     */
    public static String getCommentNickName(Document commentDoc, String lang) {
        String userId = commentDoc.getString("user_id");
        String boardId = commentDoc.getString("board_id");
        int anon = commentDoc.getInteger("anon");
        String nickName = getCommenterNickName(userId, boardId, anon, lang);
        return nickName;
    }

    /**
     * 获取子评论
     *
     * @param boardId
     * @param offset
     * @param limit
     * @return
     */
    public static Document getSubComments(String boardId, String commentId, int offset, int limit, String query, String lang) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("board_id", boardId),
                Filters.eq("comment_id", commentId),
                Filters.ne("deleted", true)
        );
        List<Document> data = MongoUtil.getDBList(MongoUtil.COMMENT_COL, "create_at", "desc", offset, limit, conditions);

        List<Document> commentList = new ArrayList<>();
        for (Document d : data) {
            String id = d.getString("id");
            String nickname = getCommentNickName(d, lang);
            Document comment = new Document()
                    .append("id", id)
                    .append("anon", d.get("anon"))
                    .append("comment", d.get("comment"))
                    .append("create_time", TimeUtil.date2CommentDate(d.getDate("create_at"), lang))
                    .append("user_id", d.getString("user_id"))
                    .append("comment_id", d.getString("comment_id"));

            String sonId = d.getString("son_id");
            if (!StringUtils.isEmpty(sonId)) {
                Document sonDoc = MongoUtil.getOneById(MongoUtil.COMMENT_COL, sonId);
                String sonNickname;
                String reply = iszh_CN(lang) ? " 回复 " : " replied ";
                if (sonDoc != null)
                    sonNickname = getCommentNickName(sonDoc, lang);
                else
                    sonNickname = "";
                //二级子评论回复了一级子评论
                comment.append("nickname", nickname + reply + sonNickname);
            } else {
                comment.append("nickname", nickname);
            }
            commentList.add(comment);
        }

        int total = MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conditions);
        Document comments = new Document()
                .append("total", total)
                .append("list", commentList)
                .append("comment_id", commentId);

        return comments;
    }

    /**
     * 最热评论
     *
     * @param boardId
     * @param offset
     * @param limit
     * @return
     */
    public static Document getHotComments(String boardId, int offset, int limit, String query, String lang) {

        List<Bson> conditions = new ArrayList<>(Arrays.asList(
                Filters.eq("board_id", boardId),
                Filters.eq("common", 0),
                Filters.exists("comment_id", false),
                Filters.ne("deleted", true)
        ));

        if (!StringUtils.isEmpty(query)) {
                    conditions.add(Filters.regex("comment", query, "i"));
        }
        List<String> sort = Arrays.asList(
                "hot",
                "create_at"
        );
        List<Document> data = MongoUtil.getDBList(MongoUtil.COMMENT_COL, sort, "desc", offset, limit, conditions);

        List<Document> commentList = new ArrayList<>();
        for (Document d : data) {
            String id = d.getString("id");
            String commentString;
            if (!StringUtils.isEmpty(query)) {
                commentString = d.getString("comment").replaceAll(query, "<span style=\"color:white\">" + query + "</span>");
            } else {
                commentString = d.getString("comment");
            }
            //String nickName = getCommenterNickName(userId, anon);
            Document comment = new Document()
                    .append("id", id)
                    .append("anon", d.get("anon"))
                    .append("comment", commentString)
                    .append("nickname", getCommentNickName(d, lang))
                    .append("create_time", TimeUtil.date2CommentDate(d.getDate("create_at"), lang))
                    .append("like_count", d.get("like_count"))
                    .append("comment_count", d.get("comment_count"))
                    .append("user_id", d.getString("user_id"));

            commentList.add(comment);
        }
        //logger.info("data " + commentList.toString());

        int total = MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conditions);
        Document comments = new Document()
                .append("total", total)
                .append("list", commentList);

        return comments;
    }

    /**
     * 最新评论
     *
     * @param boardId
     * @param offset
     * @param limit
     * @return
     */
    public static Document getLatestComments(String boardId, int offset, int limit, String query, String lang) {

        List<Bson> conditions = new ArrayList<>(Arrays.asList(
                Filters.eq("board_id", boardId),
                Filters.exists("comment_id", false),
                Filters.eq("common", 0),
                Filters.ne("deleted", true)
        ));
        if (!StringUtils.isEmpty(query)) {
            conditions.add(Filters.regex("comment", query, "i"));
        }

        List<Document> data = MongoUtil.getDBList(MongoUtil.COMMENT_COL, "create_at", "desc", offset, limit, conditions);
        List<Document> commentList = new ArrayList<>();
        for (Document d : data) {
            String id = d.getString("id");
            String commentString;
            if (!StringUtils.isEmpty(query)) {
                commentString = d.getString("comment").replaceAll(query, "<span style=\"color:white\">" + query + "</span>");
            } else {
                commentString = d.getString("comment");
            }
            Document comment = new Document()
                    .append("id", id)
                    .append("anon", d.get("anon"))
                    .append("nickname", getCommentNickName(d, lang))
                    .append("comment", commentString)
                    .append("create_time", TimeUtil.date2CommentDate(d.getDate("create_at"), lang))
                    .append("like_count", d.get("like_count"))
                    .append("comment_count", d.get("comment_count"))
                    .append("user_id", d.getString("user_id"));

            commentList.add(comment);
        }

        int total = MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conditions);
        Document comments = new Document()
                .append("total", total)
                .append("list", commentList);

        return comments;
    }

    /**
     * 常见问题
     *
     * @param boardId
     * @param offset
     * @param limit
     * @return
     */
    public static Document getCommonComments(String boardId, int offset, int limit, String query, String lang) {
        List<Bson> conditions = new ArrayList<>(Arrays.asList(
                Filters.eq("board_id", boardId),
                Filters.eq("common", 1),
                Filters.ne("deleted", true)
        ));
        if (!StringUtils.isEmpty(query)) {
            conditions.add(Filters.regex("comment", query, "i"));
        }

        List<Document> data = MongoUtil.getDBList(MongoUtil.COMMENT_COL, "create_at", "desc", offset, limit, conditions);
        List<Document> commentList = new ArrayList<>();
        for (Document d : data) {
            String id = d.getString("id");
            String commentString;
            if (!StringUtils.isEmpty(query)) {
                commentString = d.getString("comment").replaceAll(query, "<span style=\"color:white\">" + query + "</span>");
            } else {
                commentString = d.getString("comment");
            }
            Document comment = new Document()
                    .append("id", id)
                    .append("anon", d.get("anon"))
                    .append("nickname", getCommentNickName(d, lang))
                    .append("comment", commentString)
                    .append("create_time", TimeUtil.date2CommentDate(d.getDate("create_at"), lang))
                    .append("like_count", d.get("like_count"))
                    .append("comment_count", d.get("comment_count"))
                    .append("user_id", d.getString("user_id"));
            if (d.containsKey("nickname")) {
                comment.put("nickname", d.get("nickname"));
            } else {
                String userId = d.getString("user_id");
                int anon = d.getInteger("anon");
                String nickName = getCommenterNickName(userId, boardId, anon, lang);
                comment.put("nickname", nickName);
            }

            commentList.add(comment);
        }

        int total = MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conditions);
        Document comments = new Document()
                .append("total", total)
                .append("list", commentList);

        return comments;
    }

    /**
     * 我发表的评论
     *
     * @param boardId
     * @param offset
     * @param limit
     * @return
     */
    public static Document getCommentsCreateByMe(String boardId, String userId, int offset, int limit, String query, String lang) {
        List<Bson> conditions = new ArrayList<>(Arrays.asList(
                Filters.eq("board_id", boardId),
                Filters.eq("user_id", userId),
                Filters.exists("comment_id", false),
                Filters.ne("deleted", true)
        ));

        if (!StringUtils.isEmpty(query)) {
            conditions.add(Filters.regex("comment", query, "i"));
        }

        List<Document> data = MongoUtil.getDBList(MongoUtil.COMMENT_COL, "create_at", "desc", offset, limit, conditions);
        List<Document> commentList = new ArrayList<>();
        for (Document d : data) {
            String id = d.getString("id");
            int anon = d.getInteger("anon");
            String nickName = getCommenterNickName(userId, boardId, anon, lang);
            Document comment = new Document()
                    .append("id", id)
                    .append("anon", anon)
                    .append("nickname", getCommentNickName(d, lang))
                    .append("comment", d.get("comment"))
                    .append("create_time", TimeUtil.date2CommentDate(d.getDate("create_at"), lang))
                    .append("like_count", d.get("like_count"))
                    .append("comment_count", d.get("comment_count"))
                    .append("user_id", d.getString("user_id"));

            commentList.add(comment);
        }

        int total = MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conditions);
        Document comments = new Document()
                .append("total", total)
                .append("list", commentList);

        return comments;
    }

    /**
     * 我跟的评论
     *
     * @param boardId
     * @param offset
     * @param limit
     * @return
     */
    public static Document getCommentsAppendByMe(String boardId, String userId, int offset, int limit, String query, String lang) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("board_id", boardId),
                Filters.exists("comment_id", true),
                Filters.eq("user_id", userId),
                Filters.ne("deleted", true)

        );

        List<Document> data = MongoUtil.getDBList(MongoUtil.COMMENT_COL, "create_at", "desc", offset, limit, conditions);
        List<Document> commentList = new ArrayList<>();
        for (Document d : data) {
            String commentId = d.getString("comment_id");
            Document commentDoc = MongoUtil.getOneById(MongoUtil.COMMENT_COL, commentId);
            Document superComment = new Document();
            //如果主评论已被删除
            if (commentDoc == null) {
                superComment
                        .append("id", "")
                        .append("anon", "")
                        .append("nickname", "")
                        .append("comment", iszh_CN(lang) ? "原评论已被删除。" : "The original comment has been deleted。");

            } else {

                superComment
                        .append("id", commentDoc.get("_id").toString())
                        .append("anon", commentDoc.get("anon"))
                        .append("nickname", getCommentNickName(commentDoc, lang))
                        .append("comment", commentDoc.get("comment"));
            }
            String id = d.getString("id");
            int anon = d.getInteger("anon");
//            String nickName = getCommenterNickName(userId, boardId, anon, lang);
            Document comment = new Document()
                    .append("id", id)
                    .append("anon", anon)
                    .append("nickname", getCommentNickName(d, lang))
                    .append("comment", d.get("comment"))
                    .append("create_time", TimeUtil.date2CommentDate(d.getDate("create_at"), lang))
                    .append("super_comment", superComment)
                    .append("user_id", d.getString("user_id"));

            commentList.add(comment);
        }

        int total = MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conditions);
        Document comments = new Document()
                .append("total", total)
                .append("list", commentList);

        return comments;
    }

    /**
     * 评论我的评论
     *
     * @param boardId
     * @param offset
     * @param limit
     * @return
     */
    public static Document getCommentsToMe(String boardId, String userId, int offset, int limit, String query, String lang) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("board_id", boardId),
                Filters.exists("comment_id", true),
                Filters.eq("comment_user_id", userId),
                Filters.ne("deleted", true)

        );

        List<Document> commentToMeList = MongoUtil.getDBList(MongoUtil.COMMENT_COL, "create_time", "desc", offset, limit, conditions);
        List<Document> commentList = new ArrayList<>();
        for (Document likeDoc : commentToMeList) {

            String commentId = likeDoc.getString("comment_id");
            Document commentDoc = MongoUtil.getOneById(MongoUtil.COMMENT_COL, commentId);
//            int anon1 = commentDoc.getInteger("anon");
//            String nickName1 = getCommenterNickName(userId, boardId, anon1);
            Document superComment = new Document();

            if (commentDoc == null) {
                superComment
                        .append("id", "")
                        .append("anon", "")
                        .append("nickname", "")
                        .append("comment", iszh_CN(lang) ? "原评论已被删除。" : "The original comment has been deleted。");

            } else {

                superComment
                        .append("id", commentDoc.get("_id").toString())
                        .append("anon", commentDoc.get("anon"))
                        .append("nickname", getCommentNickName(commentDoc, lang))
                        .append("comment", commentDoc.get("comment"));
            }

//            String likeUserName = likeDoc.getString("nickname");
//            Document comDoc = MongoUtil.getOneById(MongoUtil.COMMENT_COL, commentId);
            Document comment = new Document()
                    .append("nickname", getCommentNickName(likeDoc, lang))
                    .append("id", likeDoc.getString("id"))
                    .append("comment", likeDoc.get("comment"))
                    .append("create_time", TimeUtil.date2CommentDate(likeDoc.getDate("create_at"), lang))
                    .append("super_comment", superComment)
                    .append("user_id", likeDoc.getString("user_id"));
            commentList.add(comment);
        }

        int total = MongoUtil.getDBCount(MongoUtil.COMMENT_COL, conditions);
        Document comments = new Document()
                .append("list", commentList)
                .append("total", total);

        return comments;
    }

    /**
     * 赞我的评论
     *
     * @param boardId
     * @param offset
     * @param limit
     * @return
     */
    public static Document getCommentsLikeToMe(String boardId, String userId, int offset, int limit, String query, String lang) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("board_id", boardId),
                Filters.exists("comment_id", true),
                Filters.eq("comment_user_id", userId),
                Filters.ne("deleted", true)

        );

        List<Document> likeList = MongoUtil.getDBList(MongoUtil.COMMENT_LIKE_COL, "create_at", "desc", offset, limit, conditions);
        List<Document> commentList = new ArrayList<>();
        for (Document likeDoc : likeList) {
            String likeUserId = likeDoc.getString("user_id");
            String likeUserName = getAnnonName(likeUserId, boardId, lang);    //获取点赞的用户昵称
            String userNickName = AccountUtil.getNicknameById(userId);  //获取被赞的用户昵称
            String commentId = likeDoc.getString("comment_id");
            Document comDoc = MongoUtil.getOneById(MongoUtil.COMMENT_COL, commentId);
            Document comment = new Document()
                    .append("user_id", likeDoc.getString("user_id"));

            if (comDoc == null) {
                comment
                        .append("like_nickname", likeUserName)              //点赞用户昵称
                        .append("id", commentId)      //被赞评论id
                        .append("nickname", userNickName)                   //被赞用户昵称
                        .append("comment", iszh_CN(lang) ? "原评论已被删除。" : "The original comment has been deleted。")                  //被赞用户评论内容
                        .append("create_time", TimeUtil.date2CommentDate(likeDoc.getDate("create_at"), lang));
            } else {
                comment
                        .append("like_nickname", likeUserName)
                        .append("id", commentId)
                        .append("nickname", userNickName)
                        .append("comment", comDoc.get("comment"))
                        .append("create_time", TimeUtil.date2CommentDate(likeDoc.getDate("create_at"), lang));
            }
            commentList.add(comment);
        }

        int total = MongoUtil.getDBCount(MongoUtil.COMMENT_LIKE_COL, conditions);
        Document comments = new Document()
                .append("total", total)
                .append("list", commentList);

        return comments;
    }

    /**
     * 获取评论列表
     *
     * @param boardId
     * @param userId
     * @param category
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    public static Document getComments(String boardId, String commentId, String userId, String category, String sort, String order, String lang, int offset, int limit, String query) {
        //处理lang
        boolean iszhCN = iszh_CN(lang);
        if (iszhCN){lang = Langs.ZH_CN.name;}
        if (!StringUtils.isEmpty(commentId)) {
            return getSubComments(boardId, commentId, offset, limit, query, lang);
        } else {
            //更新评论读取时间
            updateCommentReadTime(boardId, userId, category);
            if (category.equals("hot")) {
                return getHotComments(boardId, offset, limit, query, lang);
            }
            if (category.equals("time")) {
                return getLatestComments(boardId, offset, limit, query, lang);
            }
            if (category.equals("common")) {
                return getCommonComments(boardId, offset, limit, query, lang);
            }
            if (category.equals("create")) {
                return getCommentsCreateByMe(boardId, userId, offset, limit, query, lang);
            }
            if (category.equals("append")) {
                return getCommentsAppendByMe(boardId, userId, offset, limit, query, lang);
            }
            if (category.equals("to_me")) {
                return getCommentsToMe(boardId, userId, offset, limit, query, lang);
            }
            if (category.equals("like")) {
                return getCommentsLikeToMe(boardId, userId, offset, limit, query, lang);
            }
        }

        return null;
    }

    /**
     * 搜索
     *
     * @param boardId
     * @param query
     * @param category
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @return
     */
    public static Document search(String boardId, String query, String category, String sort, String order, int offset, int limit) {
        return null;
    }

    /**
     * 点赞
     *
     * @param userId
     * @param commentId
     * @return
     */
    public static boolean updateLike(String userId, String commentId) {
        List<Bson> conds = Arrays.asList(
                Filters.eq("user_id", userId),
                Filters.eq("comment_id", commentId)
        );
        logger.info("user_id: " + userId + " comment_id: " + commentId);
        Document likeDoc = MongoUtil.getOneByConditions(MongoUtil.COMMENT_LIKE_COL, conds);
        if (likeDoc == null) {
            Document commentDoc = MongoUtil.getOneById(MongoUtil.COMMENT_COL, commentId);
            String commentUserId = commentDoc.getString("user_id");
            String boardId = commentDoc.getString("board_id");
            Document commentLikeDoc = new Document()
                    .append("user_id", userId)
                    .append("comment_id", commentId)
                    .append("board_id", boardId)
                    .append("comment_user_id", commentUserId);
            MongoUtil.insertOne(MongoUtil.COMMENT_LIKE_COL, commentLikeDoc);

            Document updateDoc = new Document("$inc", new Document("like_count", 1));
            MongoUtil.updateOneById(MongoUtil.COMMENT_COL, commentId, updateDoc, false);
        } else {
            Document updateDoc = new Document("$inc", new Document("like_count", -1));
            MongoUtil.updateOneById(MongoUtil.COMMENT_COL, commentId, updateDoc, false);

            MongoUtil.deleteOne(MongoUtil.COMMENT_LIKE_COL, conds);
        }

        //更新评论热度
        updateCommentHot(commentId);

        return true;
    }

    /**
     * 设为常用
     *
     * @param userId
     * @param commentId
     * @return
     */
    public static boolean updateCommon(String userId, String commentId) {
        if (!authSetCommon(userId)) {
            return false;
        }
        List<Bson> conds = Arrays.asList(
                Filters.eq("_id", new ObjectId(commentId)),
                Filters.eq("common", 1)
        );
        Document doc = MongoUtil.getOneByConditions(MongoUtil.COMMENT_COL, conds);
        if (doc == null) {
            Document updateDoc = new Document("common", 1);
            MongoUtil.updateOneById(MongoUtil.COMMENT_COL, commentId, updateDoc);
        } else {
            Document updateDoc = new Document("common", 0);
            MongoUtil.updateOneById(MongoUtil.COMMENT_COL, commentId, updateDoc);
        }

        return true;
    }

    /**
     * 更新评论
     * @param commentId
     * @param comment
     * @return
     */
    public static Document updateComment(String commentId, Document comment) {
        Document newComment = new Document();
        Document res = MongoUtil.updateOneById(MongoUtil.COMMENT_COL, commentId, newComment);
        if (res != null)
            return newComment;
        else
            return null;
    }

    /**
     * 获取消息通知
     *
     * @param userId
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param lang
     * @return
     */
    public static Document getNotices(String userId, String sort, String order, int offset, int limit, String lang) {
        try {
            List<Document> boards = ProductUtil.getUserProductBoards(userId).get("list", ArrayList.class);
            List<Document> noticesList = new ArrayList<>();
            Map<String, Integer> counter = new HashMap<>();
            counter.put("total", 0);
            boards.forEach((Document document) -> {
                        String productId = document.getString("product_id");
                        String boardName = document.getString("name");
                        String userProductId = document.getString("id");
                        List<Document> commentlist = getNoticesList(productId, userId, sort, order, offset, limit, 0);
                        List<Document> likelist = getNoticesList(productId, userId, sort, order, offset, limit, 1);
                        String prefix = "<a style='color:blue; text-decoration:none' href='index.html#/dashboard/"
                                + userProductId + "'>";
                        String suffix = "</a>";
                        commentlist.forEach((Document doc) ->
                                processNotice(noticesList, doc, boardName, prefix, suffix, lang, 0));
                        likelist.forEach((Document doc) ->
                                processNotice(noticesList, doc, boardName, prefix, suffix, lang, 1));
                        int commentCount = getToMeCommentCount(productId, userId);
                        int likeCount = getLikeCommentCount(productId, userId);
                        counter.put(
                                "total", counter.get("total")
                                        + commentCount
                                        + likeCount
                        );
                    }
            );
            if (0 == counter.get("total")) {
                return null;
            }
            noticesList.sort((a, b) -> b.getDate("create_at")
                    .compareTo(a.getDate("create_at")));
            noticesList.forEach(document -> document.replace("create_at",
                    TimeUtil.date2CommentDate(document.getDate("create_at"), lang)));
            Document noticesDoc = new Document()
                    .append("total", counter.get("total"))
                    .append("list", noticesList);
            return noticesDoc;

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 处理消息
     *
     * @param noticesList
     * @param doc
     * @param boardName
     * @param prefix
     * @param suffix
     * @param lang
     * @param responseType
     */
    public static void processNotice(
            List<Document> noticesList, Document doc, String boardName,
            String prefix, String suffix, String lang, int responseType
    ) {
        StringBuilder responseNotice = new StringBuilder();
        String nickname;
        //responseType: 0 代表回复评论， 1 代表赞
        if (0 == responseType) {
            nickname = getCommentNickName(doc, lang);
        } else {
            nickname = getCommenterNickName(
                    doc.getString("user_id"),
                    doc.getString("board_id"),
                    1, lang);
        }
        Date createTime = doc.getDate("create_at");
        responseNotice
                .append(prefix).append(nickname).append(suffix);
        if (iszh_CN(lang)) {
            responseNotice
                    .append(" 在 ")
                    .append(prefix).append(boardName).append(suffix)
                    .append((0 == responseType ? " 回复" : " 赞") + "了你的评论");
        } else {
            responseNotice
                    .append((0 == responseType ? " replied to" : " liked") + " your comment at ")
                    .append(prefix).append(boardName).append(suffix);
        }
        noticesList.add(new Document()
                .append("notice", responseNotice.toString())
                .append("type", responseType)
                .append("create_at", createTime));
    }

    /**
     * 提供消息列表
     *
     * @param boardId
     * @param userId
     * @param sort
     * @param order
     * @param offset
     * @param limit
     * @param responseType
     * @return
     */
    public static List<Document> getNoticesList(
            String boardId, String userId, String sort,
            String order, int offset, int limit, int responseType
    ) {
        String key = (0 == responseType) ? "to_me" : "like";
        String database = (0 == responseType) ? MongoUtil.COMMENT_COL : MongoUtil.COMMENT_LIKE_COL;
        List<Bson> conds1 = Arrays.asList(
                new Document("board_id", boardId),
                new Document("user_id", userId)
        );
        Document latestDoc = MongoUtil.getOneByConditions(MongoUtil.COMMENT_READ_COL, conds1);
        Date latestTime = null;
        if (latestDoc != null && latestDoc.containsKey(key)) {
            latestTime = latestDoc.getDate(key);
        }

        List<Bson> conds = new ArrayList<>();
        conds.add(Filters.eq("comment_user_id", userId));
        conds.add(Filters.eq("board_id", boardId));
        if (latestTime != null) {
            conds.add(Filters.gt("create_at", latestTime));
        }
        List<Document> list = MongoUtil.getDBList(database, conds, sort, order, offset, limit, new String[]{});
        return list;
    }

}
