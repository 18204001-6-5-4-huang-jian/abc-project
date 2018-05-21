package com.abcft.apes.vitamin.util;


import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.MultivaluedMap;
import java.io.StringReader;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Long.toHexString;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class StringUtils {
    private static Logger logger = Logger.getLogger(StringUtils.class);

    private static String host_url = null;

    public static void init(String value) {
        StringUtils.host_url = value;
    }

    private static Pattern pattern = Pattern.compile("[\\[\\]]+");

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static String tsToStr(Timestamp ts) {
        return tsToStr(ts, "yyyy-MM-dd HH:mm:ss");
    }

    public static String tsToStr(Timestamp ts, String format) {
        if (ts == null) {
            return "";
        }
        DateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(ts);
    }

    public static Timestamp strToTs(String str, String format) throws ParseException {
        try {
            Timestamp ts = Timestamp.valueOf(str);
            return ts;
        } catch (Exception e) {
            DateFormat formatter = new SimpleDateFormat(format);
            Date date = formatter.parse(str);
            Timestamp ts = new Timestamp(date.getTime());
            return ts;
        }
    }

    public static Timestamp strToTs(String str) throws ParseException {
        return strToTs(str, "yyyy-MM-dd");
    }

    public static JsonObject parseJson(String string) {
        if (StringUtils.isEmpty(string)) {
            return Json.createObjectBuilder().build();
        }
        JsonReader jsonReader = Json.createReader(new StringReader(string));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return new BigDecimal(0);
        }
        String stringValue = value.toString();
        if (NumberUtils.isCreatable(stringValue)) {
            return new BigDecimal(NumberUtils.createNumber(stringValue).doubleValue());
        } else {
            return new BigDecimal(0);
        }
    }

    public static String getHostUrl() {
        return StringUtils.host_url;
    }

    public static String getBaseUri() {
        return "http://localhost:9080";
    }

    public static String toHexStr(Object obj) {
        return toHexString(obj.hashCode());
    }

    private static List<Object> processList(Document doc) {
        Iterator<String> it = doc.keySet().iterator();
        boolean allNum = true;
        Object firstVal = null;
        int max = 0;
        while (it.hasNext()) {
            String key = it.next();
            if (firstVal == null) {
                firstVal = doc.get(key);
            }
            if (!NumberUtils.isCreatable(key)) {
                allNum = false;
                break;
            }
            int n = NumberUtils.toInt(key);
            if (n > max) {
                max = n;
            }
        }
        List list = null;
        if (allNum) {
            if (firstVal instanceof String) {
                list = new ArrayList<String>(max + 1);
            } else {
                list = new ArrayList<Document>(max + 1);
            }
            it = doc.keySet().iterator();
            for (int i = 0; i < max + 1; i++) {
                Document d = (Document) doc.get(Integer.toString(i));
                // rules: list can not inside list
                processList(d);
                list.add(d);
            }
        } else {
            it = doc.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                Object d = doc.get(key);
                if (d instanceof Document) {
                    List<Object> childList = processList((Document) d);
                    if (childList != null) {
                        doc.put(key, childList);
                    }
                }
            }
        }

        return list;
    }

    private static void convertNumberValue(Document doc) {
        Iterator<String> it = doc.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            Object value = doc.get(key);
            if (value instanceof String) {
                if (NumberUtils.isCreatable((String) value)) {
                    Long longVal = NumberUtils.toLong((String) value);
                    if (longVal >= 0 && longVal <= Integer.MAX_VALUE || longVal < 0 && longVal >= Integer.MIN_VALUE) {
                        doc.put(key, longVal.intValue());
                    } else {
                        doc.put(key, NumberUtils.createNumber((String) value));
                    }
                }
            } else if (value instanceof Document) {
                convertNumberValue((Document) value);
            } else if (value instanceof List) {
                List values = (List) value;
                for (int i = 0; i < values.size(); i++) {
                    convertNumberValue((Document) values.get(i));
                }
            }
        }
    }

    public static Document formToDocument(MultivaluedMap<String, String> formData) {
        Document root = new Document();
        for (Map.Entry<String, List<String>> param : formData.entrySet()) {
            String name = param.getKey();
            String value = param.getValue().get(0);
            String[] segs = pattern.split(name);
            Document parent = root;
            for (int i = 0; i < segs.length - 1; i++) {
                if (!parent.containsKey(segs[i])) {
                    Document child = new Document();
                    parent.put(segs[i], child);
                    parent = child;
                } else {
                    parent = (Document) parent.get(segs[i]);
                }
            }
            parent.put(segs[segs.length - 1], value);
        }

        processList(root);

        convertNumberValue(root);

        return root;
    }

    public static String covertBytesToHexString(byte[] byteArray) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; ++i) {
            stringBuffer.append(Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuffer.toString();
    }

    public static String hashString(String input, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hashedBytes = md.digest(input.getBytes());

            return covertBytesToHexString(hashedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get md5 instance error!");
            return null;
        }
    }

    public static String generateMD5(String input) {
        return hashString(input, "MD5");
    }

    public static String generateSHA1(String input) {
        return hashString(input, "SHA-1");
    }

    public static String generateSHA256(String input) {
        return hashString(input, "SHA-256");
    }

    public static String combine(String... str) {
        StringBuilder sb = new StringBuilder();
        for (String s : str) {
            sb.append(s);
        }

        return sb.toString();
    }

    public static Document processCommentESC(Document comment) {
        if (comment.containsKey("comment")) {
            String commentString = comment.getString("comment");
            commentString = commentString
                    .replaceAll("(\n|\r\n|\r|\n\r)", "<br/>")
                    .replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")
                    .replaceAll(" ", "&nbsp;");
            comment.replace("comment", commentString);
        }
        return comment;
    }

    public static String formatList(List list, String head, String sep, String tail) {
        return formatList(list, null, head, sep, tail, 0);
    }

    /**
     * 格式化列表
     *
     * @param list
     * @param key  列表元素中的关键字
     * @param head
     * @param sep
     * @param tail
     * @return
     */
    public static String formatList(List list, String key, String head, String sep, String tail) {
        return formatList(list, key, head, sep, tail, 0);
    }

    /**
     * @param list
     * @param key
     * @param head
     * @param sep
     * @param tail
     * @param abbr 缩略显示的最大数, 0为无限制
     * @return
     */
    public static String formatList(List list, String key, String head, String sep, String tail, final int abbr) {
        try {
            if (list.isEmpty()) {
                return "";
            }
            List<String> stringList;
            if (list.get(0) instanceof Document && !key.isEmpty()) {
                stringList = ((List<Document>) list)
                        .stream()
                        .map((Document doc) -> doc.getString(key))
                        .collect(Collectors.toList());
            } else {
                stringList = list;
            }
            if (abbr != 0) {
                stringList = stringList.subList(0, abbr);
                stringList.add("...");
            }
            return stringList.stream().collect(Collectors.joining(sep, head, tail));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将字符串列表格式化成字符分割值
     *
     * @param stringList
     * @param sep
     * @return
     */

    public static String list2CSV(List<String> stringList, String sep) {
        return formatList(stringList, "", sep, "");
    }

    public static String list2CSV(List<String> stringList) {
        return list2CSV(stringList, ",");
    }

    /**
     * 将CSV字符串转化成字符串列表
     *
     * @param str
     * @param seps
     * @return
     */
    public static List<String> CSV2List(String str, String... seps) {
        str = str.replaceAll("[\t\n\r\\s]+", " ");
        for (String sep : seps) {
            str = str.replaceAll(sep, ",");
        }
        String[] split = str.split(",");
        List<String> strings = new ArrayList<>(Arrays.asList(split));
        return strings;
    }

    public static List<String> CSV2List(String str) {
        return CSV2List(str, ",", "，");
    }

    public static String showError(Exception e) {
        return String.format(
                "\n%s\n%s",
                e.getMessage(),
                Arrays.toString(
                        e.getStackTrace())
                        .replaceAll("[\\[\\]]", "")
                        .replaceAll(",", "\n"));
    }

    public static List<String> parseCSVQueryParam(String names) {
        if (isEmpty(names)) return Collections.EMPTY_LIST;
        names = names
                .replaceAll("\\s+", " ")
                .replaceAll("%\\d+", "")
                .replaceAll("\\s*,\\s*", ",");
        return StringUtils.CSV2List(names);
    }

    public static boolean isCN(String lang) {
        lang = lang.trim().replaceAll("_", "").toLowerCase();
        return lang.matches(".*zh.*cn.*");
    }

    public static boolean isDev(String host) {
        return !host.matches("(.*www\\.eversight\\.ai.*|.*data\\.modeling\\.ai.*)");
    }
}
