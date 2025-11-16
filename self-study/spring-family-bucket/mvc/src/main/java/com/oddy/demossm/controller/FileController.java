package com.oddy.demossm.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
public class FileController {

  @GetMapping("/file")
  public String file() {
    return "file";
  }

  // 实现文件上传接口
  @PostMapping("/upload")
  @ResponseBody
  public String upload(@RequestParam MultipartFile file) throws IOException {
    String basePath = "C:/Users/Oddy/Downloads/uploads/";
    File fileSaved = new File(
        basePath + System.currentTimeMillis() + "_" + file.getOriginalFilename());
    file.transferTo(fileSaved);
    log.info("文件上传成功，文件名：{}", fileSaved.getName());
    return "file upload success";
  }

  // 实现文件下载接口
  @GetMapping("/download/{filename}")
  @ResponseBody
  public String download(@PathVariable String filename, HttpServletResponse response) {
    String basePath = "C:/Users/Oddy/Downloads/uploads/";
    File file = new File(basePath + filename);
    // 判断文件是否存在
    if (!file.exists() || !file.isFile()) {
      // 不存在或者不是文件，返回错误信息
      log.info("文件不存在，文件名：{}", file.getName());
      return "file not exists";
    } else {
      // 存在，开始下载
      response.setContentType("multipart/form-data");
      try (var outputStream = response.getOutputStream()) {
        InputStream inputStream = new FileInputStream(file);
        IOUtils.copy(inputStream, outputStream);
      } catch (IOException e) {
        e.printStackTrace();
        log.error("文件下载失败，文件名：{}", file.getName());
        return "file download failed";
      }
      log.info("文件下载成功，文件名：{}", file.getName());
      return "file download success";
    }
  }

}
