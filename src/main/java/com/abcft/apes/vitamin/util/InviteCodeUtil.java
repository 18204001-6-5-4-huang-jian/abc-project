package com.abcft.apes.vitamin.util;

import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by zhyzhu on 17-4-26.
 */
public class InviteCodeUtil {
    private static Logger logger = Logger.getLogger(InviteCodeUtil.class);

    public static String COL = MongoUtil.INVITE_CODE_COL;
    public static int INVITE_CODE_LENGTH = 8;

    public enum Status {
        Avail,          //有效
        Used            //
    }

    public static boolean isInviteCodeExist(String inviteCode) {

        Document document = MongoUtil.getOneByField(COL, "invite_code", inviteCode);

        return (document!=null);
    }

    /**
     * 生成邀请码
     * @return
     */
    public static String createInviteCode(int discount, int abate) {
        String inviteCode = RandomStringUtils.randomAlphanumeric(INVITE_CODE_LENGTH);
        while (isInviteCodeExist(inviteCode)) {
            inviteCode = RandomStringUtils.randomAlphanumeric(INVITE_CODE_LENGTH);
        }
        LocalDate stopDate = LocalDate.now().plusDays(30);

        Document document = new Document("invite_code", inviteCode)
                .append("status", Status.Avail.ordinal())
            .append("stop_time", TimeUtil.convertLocalDate2Date(stopDate));
        if (discount > 100) {
           discount = 100;
        } else if(discount < 0) {
            discount = 0;
        }
        if (abate < 0) {
            abate = 0;
        }
        document.append("discount", discount);
        document.append("abate", abate);

        MongoUtil.insertOne(COL, document);

        return inviteCode;
    }

    /**
     * 验证邀请码
     * @param email
     * @param code
     * @param price
     * @return
     */
    public static Document verifyInviteCode(String email, String code, int price) {
        List<Bson> conditions = Arrays.asList(
                Filters.eq("invite_code", code),
                Filters.eq("status", Status.Avail.ordinal()),
                Filters.gt("stop_time", new Date())
        );
        logger.info("email: " + email + " code: " + code + "  price: " + price );
        Document document = MongoUtil.getOneByConditions(MongoUtil.INVITE_CODE_COL, conditions);
        if (document == null) {
            return null;
        }

        int newPrice = price;

        if (document.containsKey("discount")) {
            int discount = document.getInteger("discount");
            newPrice = price * discount / 100;
        }

        if (document.containsKey("abate")) {
            int abate = document.getInteger("abate");
            if (abate <= newPrice) {
                newPrice -= abate;
            } else {
                newPrice = 0;
            }
        }
        Document document1 = new Document()
                .append("status", Status.Used.ordinal())
                .append("email", email)
                .append("price_origin", price)
                .append("price_pay", newPrice)
                .append("used_at", new Date());

        MongoUtil.updateOneById(MongoUtil.INVITE_CODE_COL,  document.get("_id").toString(), document1 );

        Document resp = new Document("id", document.get("_id").toString())
                .append("price", newPrice);

        return resp;
    }
}
