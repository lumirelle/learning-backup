package com.example.classroomschool.service;


import com.example.classroomschool.config.QiNiuConfig;
import com.example.classroomschool.util.pinyin.Chines2PingUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class QiNiuService {

  /**
   * 最大尝试次数
   */
  public static final Integer MAX_RE_TRY = 3;
  /**
   * 七牛云操作成功状态码
   */
  public static final Integer SUCCESS_CODE = 200;
  @Resource
  private UploadManager uploadManager;
  @Resource
  private BucketManager bucketManager;
  @Resource
  private Auth auth;

  /**
   * 删除本地临时文件
   *
   * @param file
   */
  public static void deleteTempFile(File file) {
    if (file != null) {
      File del = new File(file.toURI());
      del.delete();
    }
  }

  /**
   * 以文件的形式上传 自定获取文件名转换为拼音形式 存储到七牛云
   *
   * @param multipartFile
   * @return
   * @throws QiniuException
   */
  public String uploadFile(String homeworkId, String studentId, MultipartFile multipartFile)
      throws Exception {
    File file = multipartFileToFile(homeworkId, studentId, multipartFile);
    String name = file.getName();
    String pinyinName = Chines2PingUtil.getFullSpell(name);
    Response response = this.uploadManager.put(file, pinyinName, getUploadToken());
    int retry = 0;
    while (response.needRetry() && retry < MAX_RE_TRY) {
      response = this.uploadManager.put(file, pinyinName, getUploadToken());
      retry++;
    }
    deleteTempFile(file);
    if (response.statusCode == SUCCESS_CODE) {
      return QiNiuConfig.PROTOCOL + QiNiuConfig.CDN_PROFILE + "/" + pinyinName;
    }
    return "上传失败!";
  }

  /**
   * 删除七牛云上的相关文件
   *
   * @param key
   * @return
   * @throws QiniuException
   */
  public String delete(String key) throws QiniuException {
    Response response = bucketManager.delete(QiNiuConfig.BUCKET, key);
    int retry = 0;
    //判断是否需要 重试 删除 且重试次数为3
    while (response.needRetry() && retry++ < MAX_RE_TRY) {
      response = bucketManager.delete(QiNiuConfig.BUCKET, key);
    }
    return response.statusCode == SUCCESS_CODE ? "删除成功!" : "删除失败!";
  }

  /**
   * 获取上传凭证
   *
   * @return
   */
  private String getUploadToken() {
    return this.auth.uploadToken(QiNiuConfig.BUCKET);
  }

  /**
   * 定义七牛云上传的相关策略
   */
  public StringMap getPutPolicy() {
    StringMap stringMap = new StringMap();
    stringMap.put("returnBody",
        "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"width\":$(imageInfo.width), \"height\":${imageInfo.height}}");
    return stringMap;
  }

  /**
   * 获取公共空间文件
   *
   * @param fileKey 要下载的文件名
   * @return
   */
  public String getPublicFile(String fileKey) throws Exception {
    String encodedFileName = URLEncoder.encode(fileKey, "utf-8").replace("+", "%20");
    return String.format("%s%s/%s", QiNiuConfig.PROTOCOL, QiNiuConfig.CDN_PROFILE, encodedFileName);
  }

  /**
   * MultipartFile 转file
   *
   * @param file
   * @return
   * @throws Exception
   */
  public File multipartFileToFile(String homeworkId, String studentId, MultipartFile file)
      throws Exception {
    File toFile = null;
    if (file.getSize() <= 0) {
      file = null;
    } else {
      InputStream ins = null;
      ins = file.getInputStream();
      toFile = new File(studentId + "-" + homeworkId + "-" + file.getOriginalFilename());
      inputStreamToFile(ins, toFile);
      ins.close();
    }
    return toFile;
  }

  //获取流文件
  private void inputStreamToFile(InputStream ins, File file) {
    try {
      OutputStream os = Files.newOutputStream(file.toPath());
      int bytesRead = 0;
      byte[] buffer = new byte[8192];
      while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
        os.write(buffer, 0, bytesRead);
      }
      os.close();
      ins.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
