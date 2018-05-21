package com.abcft.apes.vitamin.util;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class FileUtil {
    private static final Logger logger = Logger.getLogger(FileUtil.class);

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
            return dateFormat;
        }
    };

    public static String getWorkingDirString() {
        return System.getProperty("user.dir");
    }

    public static String getFileName(ContentDisposition contentDisposition) {
        String fileName = contentDisposition.getFileName();
        try {
            byte[] bytes = fileName.getBytes("ISO8859-1");
            fileName = new String(bytes, "utf-8");
            return fileName;
        } catch (UnsupportedEncodingException e) {
            logger.error("getFileName failed", e);
            return fileName;
        }
    }


    public static File getWorkingDirectory() {
        return new File(System.getProperty("user.dir"));
    }

    /**
     *
     * @param prefix
     * @param ext
     * @param dir
     * @return
     */
    public static String generateFileName(String dir, String prefix, String ext){
        StringBuilder stringBuilder = new StringBuilder(dir);
        stringBuilder.append(File.separator);

        if (!StringUtils.isEmpty(prefix)) {
            stringBuilder.append(prefix)
                .append("_");
        }

        stringBuilder.append(TimeUtil.getCurDateTimeString());

        if (!StringUtils.isEmpty(ext)) {
            stringBuilder.append(".")
                .append(ext);
        } else {
            stringBuilder.append(".tmp");
        }

        return stringBuilder.toString();
    }

    public static String getSubDirString(String subName) {
        StringBuilder subBuilder = new StringBuilder();
        return subBuilder.append(getWorkingDirString())
            .append(File.separator)
            .append("out")
            .append(File.separator)
            .append(subName)
            .toString();
    }

    private static File getSubDirectory(String subName) {
        File dir = new File(getWorkingDirectory(), "out" + File.separator + subName);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                logger.error("mkdirs failed: " + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    public static File getImageDirectory() {
        return getSubDirectory("image");
    }

    public static File createTempFile(String prefix, String suffix, File directory) throws IOException {
        if (StringUtils.isEmpty(prefix)) {
            prefix = DATE_FORMAT.get().format(new Date());
        } else {
            int dotIndex = prefix.lastIndexOf('.');
            if (dotIndex != -1) {
                if (StringUtils.isEmpty(suffix)) {
                    suffix = prefix.substring(dotIndex);
                }
                prefix = prefix.substring(0, dotIndex);
            }
        }
        prefix = prefix + "_";
        return File.createTempFile(prefix, suffix, directory);
    }

    public static boolean saveToFile(InputStream inputStream, File toFile) {
        try {
            FileOutputStream outputStream = new FileOutputStream(toFile);
            IOUtils.copy(inputStream, outputStream);
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
            return true;
        } catch (IOException e) {
            logger.error("save to file failed: " + toFile.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * 上传图片
     * @param inputStream
     * @return
     */
    public static String uploadImage(InputStream inputStream, String format) {
        try {
            File file = FileUtil.createTempFile("", "."+format, FileUtil.getImageDirectory());
            FileUtil.saveToFile(inputStream, file);

            return "/api/v1/images/" + file.getName();
        } catch (Exception e) {
            logger.error("save head img failed", e);
        }

        return "";
    }
}
