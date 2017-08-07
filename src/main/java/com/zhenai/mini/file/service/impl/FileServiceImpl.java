package com.zhenai.mini.file.service.impl;

import com.zhenai.mini.file.constant.FileConstant;
import com.zhenai.mini.file.service.IFileService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * @author chaolinye
 * @since 2017/8/1
 */
@RestController
@RequestMapping("file")
public class FileServiceImpl implements IFileService{
    @Value("${server.port}")
    private String port;
    @Override
    @PostMapping("/upload")
    public String upload(@RequestBody byte[] data, String path) {
        File dest=new File(FileConstant.FILE_PATH_PREFIX+path);
        if(!dest.getParentFile().exists()){
            dest.getParentFile().mkdirs();
        }
        try(
                FileOutputStream fs=new FileOutputStream(dest)
        ){
            fs.write(data);
            fs.flush();
            String ip=InetAddress.getLocalHost().getHostAddress();
            return ip+":"+port;
        }catch (Exception e){
            return "fail";
        }
    }

    @Override
    @GetMapping("/download")
    public void download(HttpServletResponse response,String path) {
        File dest=new File(FileConstant.FILE_PATH_PREFIX+path);
        if(!dest.exists()) return;
        try (
            OutputStream outputStream = response.getOutputStream();
            BufferedInputStream ps = new BufferedInputStream(new FileInputStream(dest))
        ){
            IOUtils.copy(ps,outputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
