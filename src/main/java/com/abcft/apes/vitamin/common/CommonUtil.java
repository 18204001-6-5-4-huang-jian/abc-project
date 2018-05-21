package com.abcft.apes.vitamin.common;

import com.abcft.apes.vitamin.util.FileUtil;
import com.abcft.apes.vitamin.util.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by jrxia on 5/3/17.
 */
public final class CommonUtil {
    private static final Logger logger = Logger.getLogger(CommonUtil.class);
    private static final UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
    private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
        put("^\\d{8}$", "yyyyMMdd");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
        put("^\\d{4}-\\d{1,2}$", "yyyy-MM");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
    }};

    public static String env = null;
    // 保存正文索引url
    public static String saveIndexUrl = null;
    // 保存表格的索引url
    public static String saveTableIndexUrl = null;
    // 保存图表的索引url
    public static String saveChartIndexUrl = null;
    // 删除正文索引url
    public static String deleteIndexUrl = null;
    // 删除表格索引url
    public static String deleteTableIndexUrl = null;
    // 删除图标索引url
    public static String deleteChartIndexUrl = null;
    // 搜索关键字url
    public static String searchUrl = null;
    public static String toWuhanSearchMain = null;
    // pdf extraction rpc address
    public static String pdfExtractorAddress = null;
    // own cloud email folder name
    public static String emailFolderName = "mail600b0ee1d3c240398493f80f81df64b4";
    // own cloud wechat folder name
    public static String wechatFolderName = null;
    // host url
    public static String hostUrl = null;
    // aqlicai chart edit call hostUrl
    public static String aqlicaiHostUrl = null;
    // aqlicai user email
    public static String aqlicaiEmail = null;
    // 图片的云存储前缀
    public static String imageOssPrefix = null;

    public static boolean syncStop = false;
    // 绑定邮箱的路径
    public static String bindMailUrl = null;
    // 重新绑定邮箱的路径
    public static String rebindMailUrl = null;
    // 删除邮箱的路径
    public static String unBindMailUrl = null;
    // 获取已绑定的邮箱
    public static String bindedMailUrl = null;
    // 手动同步邮箱的路径
    public static String syncMailUrl = null;
    /**获取邮箱同步状态的路径*/
    public static String syncStatus = null;
    // 获取绑定邮箱的配置信息
    public static String emailConfig = null;
    // 设置邮箱在线状态
    public static String onlineStatus = null;
    /**调用的key*/
    public static String WYZS_KEY = null;
    /**登录微友助手,增加微信*/
    public static String WYZS_ADD_WECHAT = null;
    /**获取当前用用户的微信帐号信息*/
    public static String WYZS_USERINFO_URL = null;
    /**获取当前用户的群信息*/
    public static String WYZS_GROUPINFO_URL = null;
    /**微友助手，删除机器人*/
    public static String WYZS_DEL_ROBOT_URL = null;
    /**微友助手，添加群组*/
    public static String WYZS_ADD_GROUP_URL = null;
    /**微友助手，删除群组*/
    public static String WYZS_DEL_GROUP_URL = null;
    /**微友助手，获取机器人状态*/
    public static String WYZS_ROBOT_STATUS_URL = null;
    /**微友助手，退出登录*/
    public static String WYZS_LOGOUT = null;
    /**微友助手，消息通知地址*/
    public static String WYZS_NOTIFY_URL = null;

    public static String getUploadFileName(ContentDisposition disposition) {
        return FileUtil.getFileName(disposition);
    }

    public static File getTempDirectory(String group, String owner) {
        File pool = getFilePoolDirectory(group, owner);
        File dir = new File(pool.getAbsolutePath(), "temp");
        if (!dir.exists() && !dir.mkdir()) {
            logger.error("mkdir failed: " + dir.getAbsolutePath());
        }

        return dir;
    }

    public static File getFilePoolDirectory(String group, String owner) {
        File workingDir = FileUtil.getWorkingDirectory();
        File dir = new File(workingDir.getAbsolutePath(), "file_pool");
        if (!dir.exists()) {
            if (dir.mkdir()) {
                setGroupAndOwner(dir, group, owner);
            } else {
                logger.error("mkdirs failed: " + dir.getAbsolutePath());
            }
        }
        return dir;
    }
    public static File getUserFilePoolDirectory(String uid, String group, String owner) {
        File pool = getFilePoolDirectory(group, owner);
        File dir = new File(pool.getAbsolutePath(), uid);
        if (!dir.exists()) {
            if (dir.mkdir()) {
                setGroupAndOwner(dir, group, owner);
            } else {
                logger.error("mkdir failed: " + dir.getAbsolutePath());
            }
        }

        return dir;
    }

    public static void setGroupAndOwner(File file, String smbGroup, String smbOwner) {
        try {
            if (StringUtils.isEmpty(smbGroup)) {
                return;
            }
            if (StringUtils.isEmpty(smbOwner)) {
                return;
            }

            PosixFileAttributeView view = Files.getFileAttributeView(file.toPath(),
                    PosixFileAttributeView.class,
                    LinkOption.NOFOLLOW_LINKS);

            GroupPrincipal group = lookupService.lookupPrincipalByGroupName(smbGroup);
            view.setGroup(group);
            logger.info("set group " + group.getName() + " to " + file.getAbsolutePath());

            UserPrincipal owner = lookupService.lookupPrincipalByName(smbOwner);
            view.setOwner(owner);
            logger.info("set owner " + owner.getName() + " to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();

            logger.error("Failed to set group and owner to " + file.getAbsolutePath(), e);
        }
    }

    public static String getHash(String file) throws IOException, NoSuchAlgorithmException {
        Path path = new File(file).toPath();
        byte[] data = MessageDigest.getInstance("SHA-512")
                .digest(Files.readAllBytes(path));
        return DatatypeConverter.printHexBinary(data);
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getUniqueTitle(Set<String> titles, String title) {
        String title2 = title;
        int i = 0;
        for(; i < 1000; ++i) {
            if (i > 0) {
                title2 = String.format("%s_%d", title, i);
            }
            if (!titles.contains(title2)) {
                return title2;
            }
        }
        title2 = String.format("%s_%d", title, i);

        return title2;
    }

    public static String b64Encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(Charset.forName("UTF-8")));
    }

    public static String b64Decode(String str) {
        return new String(Base64.getDecoder().decode(str), Charset.forName("UTF-8"));
    }



    /**
     * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
     * format is unknown. You can simply extend DateUtil with more formats if needed.
     * @param dateString The date string to determine the SimpleDateFormat pattern for.
     * @return The matching SimpleDateFormat pattern, or null if format is unknown.
     */
    public static String determineDateFormat(String dateString) {
        for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase().matches(regexp)) {
                return DATE_FORMAT_REGEXPS.get(regexp);
            }
        }
        return null; // Unknown format.
    }
    public static boolean isDate(String dateString) {
        for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase().matches(regexp)) {
                return true;
            }
        }
        return false;
    }

    public static String getCodeType(Object o) {
        if (o == null) {
            return "text";
        }

        Class co = o.getClass();
        if (co == Integer.class || co == Long.class || co == Double.class || co == Float.class) {
            return "number";
        } else if (co == String.class) {
            if (CommonUtil.isDate(o.toString())) {
                return "date";
            } else if (NumberUtils.isCreatable(o.toString())) {
                return "number";
            }

            return "text";
        } else if (co == Date.class) {
            return "date";
        }

        return "text";
    }
}

