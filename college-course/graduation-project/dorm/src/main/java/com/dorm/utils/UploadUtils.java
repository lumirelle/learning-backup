package com.dorm.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Component
public class UploadUtils {

    @Value("${location}")
    private String uploadLocation;

    public String uploadFile(MultipartFile uploadFile) throws IOException {
        // 确保上传路径文件夹存在
        File uploadFolder = new File(uploadLocation);
        if (!uploadFolder.exists()) {
            //使用 `mkdirs` 方法创建目录
            uploadFolder.mkdirs();
        }

        // 处理文件名，添加时间戳，避免文件名冲突
        String filename = Objects.requireNonNullElse(uploadFile.getOriginalFilename(), "unnamed-file");
        String suffix = filename.substring(filename.lastIndexOf("."));
        String prefix = System.nanoTime() + "";
        String uploadFilename = prefix + suffix;

        // 上传文件
        File file = new File(uploadFolder, uploadFilename);
        uploadFile.transferTo(file);

        return uploadFilename;
    }
}
