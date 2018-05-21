package com.abcft.apes.vitamin.util;

import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class DashBoardUtil {
    private static Logger logger = Logger.getLogger(DashBoardUtil.class);
    public static String COL = MongoUtil.DASHBOARD_COL;

    /**
     * 复制看板
     * @param bid
     * @return
     */
    public static String copyDashBoard(String bid, String userId) {
        try{

            Document dashDoc = MongoUtil.getOneById(MongoUtil.DASHBOARD_TMPL_COL, bid);
            if (dashDoc ==null) {
                logger.warn("board not exist: " + bid);
                return null;
            }
            dashDoc.put("origin_id", bid);
            if (!StringUtils.isEmpty(userId)) {
                dashDoc.put("creator_id", userId);
            }
            if (dashDoc.containsKey("_id")) {
                dashDoc.remove("_id");
            }
            if (dashDoc.containsKey("demo")) {
                dashDoc.remove("demo");
            }
            Document document = MongoUtil.insertOne(MongoUtil.DASHBOARD_COL, dashDoc);
            if (document == null) {
                return null;
            }
            String newBid = document.getObjectId("_id").toString();

            return newBid;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("bid: " + bid);

        }
        return null;
    }

    /**
     * chart 是否已在 board 中
     * 判断 board 中 的 chart 是否有 title 和 指定的 chart title 相同
     * @param boardId
     * @param chartId
     * @return
     */
    public static boolean isChartInBoard(String boardId, long chartId) {

        Document chartDoc = MongoUtil.getOneById(MongoUtil.CHART_COL, chartId);
        String chartTitle = chartDoc.getString("title");

        Document board = MongoUtil.getOneById(MongoUtil.DASHBOARD_COL, boardId);
        if (!board.containsKey("charts")) {
            return false;
        }
        List<Document> boardCharts = board.get("charts", List.class);
        for (Document boardChart: boardCharts) {
            long cId = ChartUtil.getChartId(boardChart, "id");
            Document c = MongoUtil.getOneById(MongoUtil.CHART_COL, cId);
            if (c==null) {
                logger.info(String.format("board: %s miss chart: %s", boardId, cId));
                continue;
            }
            if (c.containsKey("title")) {
                String title = c.getString("title");
                if (chartTitle.equals(title)) {
                    return true;
                }
            }
        }

        return false;
    }


    public static Document getDashboard(String boardId) {
        return getDashboard(boardId, true);
    }

    /**
     * 获取看板
     * @param boardId
     * @return
     */
    public static Document getDashboard(String boardId, boolean hasCharts) {
        Document docBoard = MongoUtil.getOneById(MongoUtil.DASHBOARD_TMPL_COL, boardId);
        if (docBoard == null) {
            logger.warn("docBoard is null " + boardId);
            return null;
        }

        Document boardDoc = new Document()
                .append("id", docBoard.get("_id").toString())
                .append("title", docBoard.get("title"));

        if (hasCharts) {
            List<Document> chartList = getCharts(docBoard);
            boardDoc.put("charts", chartList);
        }

        return boardDoc;
    }

    /**
     * 获取看板中的chart列表
     * @param boardId
     * @return
     */
    public static List<Document> getCharts(final String boardId) {
        Document board = MongoUtil.getOneById(MongoUtil.DASHBOARD_COL, boardId);
        if (board == null) {
            logger.warn("dashboard not found: " + boardId);
            return null;
        }

        return getCharts(board);
    }

    public static List<Document> getCharts(Document board) {
        List<Document> chartList = new ArrayList<>();
        if (board.containsKey("charts")) {
            List<Document> boardCharts = board.get("charts", List.class);
            for(Document boardChart : boardCharts) {
                Long chartId = ChartUtil.getChartId(boardChart, "id");
                Document chartDetail = ChartUtil.getChartDetail(chartId);
                if (chartDetail == null) {
                    logger.warn(String.format("dashboard: %s boardChart: miss", board.get("_id"), chartId));
                    continue;
                }

                Document chartDoc = new Document()
                        .append("id", chartDetail.get("id"))
                        .append("title", chartDetail.get("title"))
                        .append("description", chartDetail.getOrDefault("description", ""))
                        .append("type", chartDetail.get("type"))
                        .append("options", chartDetail.getOrDefault("options", ""))
                        .append("dataSourceUrl", chartDetail.getOrDefault("dataSourceUrl", ""))
                        .append("position", boardChart.get("position"));
                if (chartDetail.containsKey("customize")) {
                    chartDoc.append("customize", true);
                }
                if (chartDetail.containsKey("chartSeries")) {
                    chartDoc.append("chartSeries", chartDetail.get("chartSeries"));
                }
                if (chartDetail.containsKey("chartType")) {
                    chartDoc.append("chartType", chartDetail.get("chartType"));
                }
                if (chartDetail.containsKey("tag")) {
                    chartDoc.append("tag", chartDetail.get("tag"));
                }

                chartList.add(chartDoc);
            }
        }

        return chartList;
    }

    public static boolean isAuth(String boardId, String uid) {
        return true;
    }
}
