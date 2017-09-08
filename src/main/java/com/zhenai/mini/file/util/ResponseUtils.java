package com.zhenai.mini.file.util;

import com.zhenai.mini.commons.utils.FileUtils;

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

    public static void setCacheControl(HttpServletResponse response){
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.YEAR,10);
        response.setHeader("Cache-Control","max-age="+(calendar.getTimeInMillis()/1000));
        response.setDateHeader("Expires",calendar.getTimeInMillis());
    }
}
