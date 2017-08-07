package com.zhenai.mini.file.service;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author chaolinye
 * @since 2017/8/1
 */
public interface IFileService {

    String upload(byte[] data,String path);

    void download(HttpServletResponse response,String path);

}
