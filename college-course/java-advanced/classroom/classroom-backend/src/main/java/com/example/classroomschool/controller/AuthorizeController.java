package com.example.classroomschool.controller;


import com.example.classroomschool.entity.picture.Picture;
import com.example.classroomschool.entity.student.AuthorizeStudent;
import com.example.classroomschool.entity.teacher.AuthorizeTeacher;
import com.example.classroomschool.service.AuthorizeStudentService;
import com.example.classroomschool.service.AuthorizeTeacherService;
import com.example.classroomschool.service.PictureService;
import com.example.classroomschool.util.encryption.MD5Encoder;
import com.example.classroomschool.util.random.RandomUtil;
import com.example.classroomschool.util.token.TokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class AuthorizeController {

  @Resource
  AuthorizeTeacherService teacherService;

  @Resource
  AuthorizeStudentService studentService;

  @Resource
  PictureService pictureService;

  /**
   * 登录
   */
  @PostMapping(value = "/login")
  public String loginStudent(@RequestBody Map<String, String> data)
      throws JsonProcessingException, UnsupportedEncodingException, NoSuchAlgorithmException {
    // 通过邮箱/手机号查找用户
    AuthorizeStudent authorizeStudent = studentService.findByAccountKey(data.get("account"));
    AuthorizeTeacher authorizeTeacher = teacherService.findByAccountKey(data.get("account"));

    // 用户不存在
    if (authorizeStudent == null && authorizeTeacher == null) {
      return "0";
    }

    MD5Encoder md5Encoder = new MD5Encoder();
    // 该账户是学生
    if (authorizeStudent != null) {
      // 密码错误
      if (!authorizeStudent.getPassword().equals(md5Encoder.encode(data.get("password")))) {
        return "1";
      }
      // 用账号生成 jwt token 并验证
      String token = TokenUtil.sign("ROLE_student", authorizeStudent.getAccount());
      HashMap<String, Object> responseMap = new HashMap<>();
      //验证token能不能用，没必要的
      if (TokenUtil.verify(token)) {
        responseMap.put("code", 200);
      } else {
        responseMap.put("code", 500);
      }
      responseMap.put("role", 0); // 学生
      responseMap.put("token", token);
      // 查这个学生的信息
      String account = TokenUtil.getAccount(token);
      AuthorizeStudent stu = studentService.findByAccount(account);
      responseMap.put("user", stu);
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.writeValueAsString(responseMap);
    }
    // 该账户是教师
    else {
      if (!authorizeTeacher.getPassword().equals(md5Encoder.encode(data.get("password")))) {
        return "1";
      }
      String token = TokenUtil.sign("ROLE_teacher", authorizeTeacher.getAccount());
      HashMap<String, Object> responseMap = new HashMap<>();
      if (TokenUtil.verify(token)) {
        responseMap.put("code", 200);
      } else {
        responseMap.put("code", 500);
      }
      responseMap.put("role", 1);
      responseMap.put("token", token);
      String account = TokenUtil.getAccount(token);
      AuthorizeTeacher tea = teacherService.findByAccount(account);
      responseMap.put("user", tea);
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.writeValueAsString(responseMap);
    }
  }

  /**
   * 学生注册
   */
  @RequestMapping(value = "/register/student")
  public String addStudent(@RequestBody Map<String, String> data)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String account; // 账号 account
    String mailbox; // 邮箱 accountKey
    String phone; // 手机号 accountKey
    AuthorizeStudent authorizeStudent = new AuthorizeStudent();

    // 校验验证码
    MD5Encoder md5Encoder = new MD5Encoder();
    //（加密后判断）
    if (!md5Encoder.encode(data.get("answer")).equals(data.get("answerPlus"))) {
      return "验证码错误";
    }

    // 校验账号是否已经存在
    // 验证邮箱/电话是否已经存在
    if (data.get("account").contains("@")) {
      mailbox = data.get("account");
      // 验证邮箱是否已经存在
      AuthorizeStudent authorizeStudentMailbox = studentService.findByMailbox(mailbox);
      AuthorizeTeacher authorizeTeacherMailbox = teacherService.findByMailbox(mailbox);
      if (authorizeStudentMailbox != null || authorizeTeacherMailbox != null) {
        return "邮箱已存在";
      }
      authorizeStudent.setMailbox(mailbox);
    } else {
      phone = data.get("account");
      // 验证电话是否已经存在
      AuthorizeStudent authorizeStudentPhone = studentService.findByPhone(phone);
      AuthorizeTeacher authorizeTeacherPhone = teacherService.findByPhone(phone);
      if (authorizeStudentPhone != null || authorizeTeacherPhone != null) {
        return "电话已存在";
      }
      authorizeStudent.setPhone(phone);
    }

    // 随机生成账号，有可能重复
    while (true) {
      account = RandomUtil.randomAccount(false);
      if (studentService.findByAccount(account) == null) {
        authorizeStudent.setAccount(account);
        break;
      }
    }

    authorizeStudent.setPassword(md5Encoder.encode(data.get("password")));
    authorizeStudent.setName(data.get("name"));
    authorizeStudent.setSchool(data.get("school"));
    authorizeStudent.setSchoolNum(data.get("schoolNum"));
    studentService.createStudent(authorizeStudent);

    // 添加头像图片
    String address = RandomUtil.randomAvatarAddress();
    //头像
    Picture avatar = new Picture();
    avatar.setPicid(account);
    avatar.setAddress(address);
    pictureService.createPicture(avatar);
    return "注册成功";
  }

  /**
   * 老师注册
   */
  @RequestMapping(value = "/register/teacher")
  public String addTeacher(@RequestBody Map<String, String> date)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    String account;
    String mailbox;
    String phone;
    AuthorizeTeacher authorizeTeacher = new AuthorizeTeacher();

    // 校验验证码
    MD5Encoder md5Encoder = new MD5Encoder();
    if (!md5Encoder.encode(date.get("answer")).equals(date.get("answerPlus"))) {
      return "验证码错误";
    }

    // 校验账号是否已经存在
    if (date.get("account").contains("@")) {
      mailbox = date.get("account");
      AuthorizeStudent authorizeStudentMailbox = studentService.findByMailbox(mailbox);
      AuthorizeTeacher authorizeTeacherMailbox = teacherService.findByMailbox(mailbox);
      if (authorizeStudentMailbox != null || authorizeTeacherMailbox != null) {
        return "邮箱已存在";
      }
      authorizeTeacher.setMailbox(mailbox);
    } else {
      phone = date.get("account");
      AuthorizeStudent authorizeStudentPhone = studentService.findByPhone(phone);
      AuthorizeTeacher authorizeTeacherPhone = teacherService.findByPhone(phone);
      if (authorizeStudentPhone != null || authorizeTeacherPhone != null) {
        return "电话已存在";
      }
      authorizeTeacher.setPhone(phone);
    }

    // 随机生成账号
    while (true) {
      account = RandomUtil.randomAccount(true);
      if (teacherService.findByAccount(account) == null) {
        authorizeTeacher.setAccount(account);
        break;
      }
    }

    authorizeTeacher.setName(date.get("name"));
    authorizeTeacher.setSchool(date.get("school"));
    authorizeTeacher.setPassword(md5Encoder.encode(date.get("password")));
    teacherService.createTeacher(authorizeTeacher);

    // 添加头像图片
    String address = RandomUtil.randomAvatarAddress();
    Picture picture = new Picture();
    picture.setPicid(account);
    picture.setAddress(address);
    pictureService.createPicture(picture);
    return "注册成功";
  }

  /**
   * 随机生成验证码
   */
  @RequestMapping(value = "/validCode")
  public Object checkNumber()
      throws UnsupportedEncodingException, NoSuchAlgorithmException, JsonProcessingException {
    String opNames = "加减乘";
    String ops = "+-*";
    String equation;
    int result;

    // 生成两个 1-20 的随机数
    int num1 = (int) (Math.random() * 20 + 1);
    int num2 = (int) (Math.random() * 20 + 1);
    // 加减乘三选一
    int k = (int) (Math.random() * 3);
    char opName = opNames.charAt(k);
    char op = ops.charAt(k);
    // 如果是减法，被减数需要大于减数，否则交换位置
    if (op == '-') {
      if (num1 < num2) {
        int temp = num1;
        num1 = num2;
        num2 = temp;
      }
    }

    // 生成等式和结果
    equation = String.format("%d%c%d=", num1, opName, num2);
    if (op == '+') {
      result = num1 + num2;
    } else if (op == '-') {
      result = num1 - num2;
    } else {
      result = num1 * num2;
    }

    HashMap<String, String> responseMap = new HashMap<>();
    responseMap.put("code", equation);
    MD5Encoder md5Encoder = new MD5Encoder();
    // 将结果加密后返回
    responseMap.put("answerPlus", md5Encoder.encode(String.valueOf(result)));
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(responseMap);
  }
}
