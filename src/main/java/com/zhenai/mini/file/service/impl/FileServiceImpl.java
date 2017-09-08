package com.zhenai.mini.file.service.impl;

import com.zhenai.mini.file.constant.FileConstant;
import com.zhenai.mini.file.service.IFileService;
import com.zhenai.mini.file.util.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chaolinye
 * @since 2017/8/1
 */
@RestController
public class FileServiceImpl implements IFileService {
    @Value("${server.port}")
    private String port;

    private final static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload(@RequestBody byte[] data, String path) {
        File dest = new File(FileConstant.FILE_PATH_PREFIX + path);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try (
                FileOutputStream fs = new FileOutputStream(dest)
        ) {
            fs.write(data);
            fs.flush();
            String ip = InetAddress.getLocalHost().getHostAddress();
            return "http://" + ip + ":" + port + "/file/download/" + path;
        } catch (Exception e) {
            return "fail";
        }
    }

    @Override
    public String compress(String srcPath, String destPath, int width, int height) {
        if (width == 0 && height == 0) return "fail";
        List<String> commands = new ArrayList<>();
        commands.add("convert");
        commands.add(FileConstant.FILE_PATH_PREFIX + srcPath);
        commands.add("-resize");
        if (width == 0) {
            commands.add("x" + height);
        } else if (height == 0) {
            commands.add(String.valueOf(width));
        } else {
            commands.add(width + "x" + height + "!");
        }
        commands.add(FileConstant.FILE_PATH_PREFIX + destPath);
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commands);
        try {
            Process process = processBuilder.start();
            process.waitFor();
            int exit = process.exitValue();
            if (exit != 0) {
                return "fail";
            } else {
                String ip = InetAddress.getLocalHost().getHostAddress();
                return "http://" + ip + ":" + port + "/file/download/" + destPath;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response) {
        String path = extractPathFromPattern(request);
        File dest = new File(FileConstant.FILE_PATH_PREFIX + path);
        if (!dest.exists()) return;
        // 文件总字节数
        long fileLength = dest.length();
        logger.info("path={} size={}bytes", path, fileLength);
//        response.reset();
        // 开始字节
        long pastLength = 0;
        // 0：从头开始的全文下载；1：从某字节开始的下载（bytes=27000-）；2：从某字节开始到某字节结束的下载（bytes=27000-39000）
        int rangeSwitch = 0;
        // 记录客户端需要下载的字节段的最后一个字节偏移量（比如bytes=27000-39000，则这个值是为39000）
        long toLength = 0;
        // 客户端请求的字节总量
        long contentLength = 0;
        // 记录客户端传来的形如“bytes=27000-”或者“bytes=27000-39000”的内容
        String rangeBytes = "";

        byte b[] = new byte[1024];
        // 客户端请求的下载的文件块的开始字节
        if (request.getHeader("Range") != null) {
            // 返回状态码206
            response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Connection", "keep-alive");
            rangeBytes = request.getHeader("Range").replaceAll(
                    "bytes=", "").trim();
            logger.info("range Bytes={} ", rangeBytes);
            if (rangeBytes.indexOf('-') == rangeBytes.length() - 1) {
                rangeSwitch = 1;
                rangeBytes = rangeBytes.substring(0,
                        rangeBytes.indexOf('-'));
                pastLength = Long.parseLong(rangeBytes.trim());
                contentLength = fileLength - pastLength;
            } else {
                rangeSwitch = 2;
                String temp1 = rangeBytes.substring(0,
                        rangeBytes.indexOf('-'));
                String temp2 = rangeBytes.substring(
                        rangeBytes.indexOf('-') + 1);
                // 开始字节
                pastLength = Long.parseLong(temp1.trim());
                // 结束字节
                toLength = Long.parseLong(temp2.trim());
                contentLength = toLength - pastLength + 1;
            }
        } else {
            contentLength = fileLength;
        }

        logger.info("开始字节={}", pastLength);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Length", String.valueOf(contentLength));
        ResponseUtils.setContentType(path, response);
        ResponseUtils.setCacheControl(response);
        // 构造Content-Range请求头
        switch (rangeSwitch) {
            case 1: {
                String contentRange = new StringBuffer("bytes ")
                        .append(String.valueOf(pastLength))
                        .append("-")
                        .append(String.valueOf(fileLength - 1))
                        .append("/")
                        .append(String.valueOf(fileLength))
                        .toString();
                response.setHeader("Content-Range", contentRange);
                break;
            }
            case 2: {
                String contentRange = new StringBuffer("bytes ")
                        .append(rangeBytes)
                        .append("/")
                        .append(String.valueOf(fileLength))
                        .toString();
                response.setHeader("Content-Range", contentRange);
                break;
            }
            default: {
                break;
            }
        }
        try (
                BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
                RandomAccessFile raf = new RandomAccessFile(dest, "r")
        ) {

            switch (rangeSwitch) {
                // 普通下载，或者从头开始的下载
                case 0: {
                    // 同1
                }
                case 1: {
                    // 针对 bytes=27000- 这样的请求
                    raf.seek(pastLength);
                    int n = 0;
                    while ((n = raf.read(b, 0, 1024)) != -1) {
                        out.write(b, 0, n);
                    }
                    break;
                }
                case 2: {
                    // 针对 bytes=27000-39000 这样的请求
                    raf.seek(pastLength); // 形如
                    int n = 0;
                    long readLength = 0; // 记录已读字节数
                    // 大部分字节在这里读取
                    while (readLength <= contentLength - 1024) {
                        n = raf.read(b, 0, 1024);
                        readLength += 1024;
                        out.write(b, 0, n);
                    }
                    // 余下的不足 1024个字节在这里读取
                    if (readLength <= contentLength) {
                        n = raf.read(b, 0,
                                (int) (contentLength - readLength));
                        out.write(b, 0, n);
                    }
                    break;
                }
                default: {
                    break;
                }
            }
            out.flush();
        } catch (Exception e) {
//            logger.error("download error exception={}",e);
        }
    }

    /**
     * 把指定URL后的字符串全部截断当成参数
     * 这么做是为了防止URL中包含中文或者特殊字符（/等）时，匹配不了的问题
     *
     * @param request
     * @return
     */
    private static String extractPathFromPattern(
            final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }

}
