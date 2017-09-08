package com.zhenai.mini.file.util;

import com.zhenai.mini.commons.utils.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chaolinye
 * @since 2017/8/25
 */
public class ResponseUtils {
    private static Map<String, String> contentTypeMap = new HashMap<>();

    static {
        contentTypeMap.put("m4a", "audio/x-m4a");
    }

    public static void setContentType(String filePath, HttpServletResponse response) {
        String type = contentTypeMap.get(FileUtils.getSuffix(filePath));
        if (type != null) {
            response.setHeader("Content-Type", type);
        }
    }

//    public static void setCacheControl(HttpServletResponse response){
//        Calendar calendar=Calendar.getInstance();
//        calendar.add(Calendar.YEAR,10);
//        response.setHeader("Cache-Control","max-age="+(calendar.getTimeInMillis()/1000));
//        response.setDateHeader("Expires",calendar.getTimeInMillis());
//        response.addDateHeader("Last-Modified", System.currentTimeMillis());
//    }
    public static void setCacheControl(HttpServletRequest request, HttpServletResponse response, long lastModified){
//        long headerTime = request.getDateHeader("If-Modified-Since");
        Calendar calendar=Calendar.getInstance();
        long nowTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR,10);
        long expireTime = calendar.getTimeInMillis()-nowTime;
//        if(headerTime > 0 && lastModified > headerTime){
//            response.setStatus(HttpServletResponse.SC_OK);
//            return true;
//        }
//        if(headerTime + expireTime > nowTime){
//            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
//            return false;
//        }
//        String previousToken = request.getHeader("If-None-Match");
//        if (previousToken != null && previousToken.equals(Long.toString(lastModified))) {
//            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
//            return false;
//        }
        response.setHeader("ETag", Long.toString(lastModified)); // 添加ETag
//        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Cache-Control","max-age="+(expireTime/1000));
        response.addDateHeader("Last-Modified", nowTime);
        response.addDateHeader("Expires", nowTime + expireTime);
    }
}
