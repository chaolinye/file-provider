package com.zhenai.mini.file.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author chaolinye
 * @since 2017/8/1
 */
public class IPUtil {
    public static String getCurrentIPString()throws UnknownHostException{
            return InetAddress.getLocalHost().getHostAddress();
    }
}
