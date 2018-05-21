/**
 * 
 */
package com.abcft.apes.vitamin.passport.util;

import java.util.Random;
import java.util.UUID;

/**
 * <p>
 * 随机字符串。
 * <p>
 *
 * 创建日期 2015年1月8日<br>
 * 
 * @author 陈军营(263832707@qq.com) <br>
 * @version $Revision$ $Date$
 * @since 3.0.0
 */
public class RandomUtil {
    // 字符串种子，数字大小写字母
    public static final String RANDOM_CHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // 字符串种子，数字
    public static final String RANDOM_NUM = "0123456789";

    // 字符串种子，数字大写字母
    public static final String RANDOM_NUM_CAPITAL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 生成指定长度的随机字符串
     * 
     * @param length 生成字符串的长度
     * @return
     */
    public static String getRandomString(int length, String RandomChar) {
        StringBuffer buffer = new StringBuffer(RandomChar);
        StringBuffer randomStringBuf = new StringBuffer();
        Random random = new Random();
        int range = buffer.length();
        for (int i = 0; i < length; i++) {
            randomStringBuf.append(buffer.charAt(random.nextInt(range)));
        }
        return randomStringBuf.toString();
    }
    
    /**
     * 生成32位UUID字符串，去掉小横线
     * 
     * @return
     */
    public static String getUUIDString() {
        UUID uuid  =  UUID.randomUUID();
        return uuid.toString().replaceAll("\\-", "");
    }

    public static void main(String[] args) {
        String randomChar = getRandomString(32, RANDOM_CHAR);
        System.out.println("randomChar:" + randomChar);
        
        String randomNum = getRandomString(4, RANDOM_NUM);
        System.out.println("randomNum:" + randomNum);
        
        String randomNumCapital = getRandomString(1, RANDOM_CHAR);
        System.out.println("randomNumCapital:" + randomNumCapital);
        
        String randomUUID = getUUIDString();
        System.out.println("randomUUID:" + randomUUID);
    }
}
