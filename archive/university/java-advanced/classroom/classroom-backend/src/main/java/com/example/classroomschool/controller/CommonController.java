package com.example.classroomschool.controller;

import com.example.classroomschool.entity.course.Course;
import com.example.classroomschool.entity.course.StudentCourse;
import com.example.classroomschool.entity.course.TeacherCourse;
import com.example.classroomschool.entity.homework.Homework;
import com.example.classroomschool.entity.homework.HomeworkComments;
import com.example.classroomschool.entity.inform.Inform;
import com.example.classroomschool.entity.inform.InformComments;
import com.example.classroomschool.entity.picture.Picture;
import com.example.classroomschool.entity.student.AuthorizeStudent;
import com.example.classroomschool.entity.teacher.AuthorizeTeacher;
import com.example.classroomschool.entity.teacher.BeanTeacher;
import com.example.classroomschool.service.AuthorizeStudentService;
import com.example.classroomschool.service.AuthorizeTeacherService;
import com.example.classroomschool.service.BeanStudentService;
import com.example.classroomschool.service.BeanTeacherService;
import com.example.classroomschool.service.CourseService;
import com.example.classroomschool.service.HomeworkCommentsService;
import com.example.classroomschool.service.HomeworkService;
import com.example.classroomschool.service.InformCommentsService;
import com.example.classroomschool.service.InformService;
import com.example.classroomschool.service.PictureService;
import com.example.classroomschool.service.StudentCourseService;
import com.example.classroomschool.service.TeacherCourseService;
import com.example.classroomschool.util.token.TokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通用接口，提供请求数据，返回对应结果
 */
@RestController
@RequestMapping("/api/start")
public class CommonController {

  @Resource
  StudentCourseService studentCourseService;

  @Resource
  PictureService pictureService;

  @Resource
  TeacherCourseService teacherCourseService;

  @Resource
  CourseService courseService;

  @Resource
  HomeworkService homeworkService;

  @Resource
  BeanTeacherService beanTeacherService;

  @Resource
  BeanStudentService beanStudentService;

  @Resource
  HomeworkCommentsService homeworkCommentsService;

  @Resource
  InformCommentsService informCommentsService;

  @Resource
  InformService informService;

  @Resource
  AuthorizeTeacherService authorizeTeacherService;

  @Resource
  AuthorizeStudentService authorizeStudentService;

  /**
   * 获取当前登录的账号信息
   */
  @RequestMapping(value = "/user")
  public Object getUser(@RequestBody Map<String, String> data) throws JsonProcessingException {
    String account = TokenUtil.getAccount(data.get("token"));
    AuthorizeStudent authorizeStudent = authorizeStudentService.findByAccount(account);
    AuthorizeTeacher authorizeTeacher = authorizeTeacherService.findByAccount(account);
    HashMap<String, Object> responseMap = new HashMap<>();
    if (authorizeStudent == null) {
      responseMap.put("user", authorizeTeacher);
    } else {
      responseMap.put("user", authorizeStudent);
    }
    // 获取这个 id 相关的图片（头像）
    Picture picture = pictureService.findById(account);
    responseMap.put("address", picture.getAddress());
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(responseMap);
  }

  /**
   * 根据教师 id 获取教师名
   */
  @RequestMapping(value = "/teacherName")
  public String name(@RequestBody Map<String, String> data) {
    int id = Integer.parseInt(data.get("id"));
    AuthorizeTeacher authorizeTeacher = authorizeTeacherService.findById(id);
    return authorizeTeacher.getName();
  }

  /**
   * 根据课程 id 获取课程详情（本课程及本课程的所有作业、老师、学生）
   */
  @RequestMapping(value = "/courseInfo")
  public Object courseInfo(@RequestBody Map<String, String> data) throws JsonProcessingException {
    int courseId = Integer.parseInt(data.get("courseId"));

    // 获取课程、作业信息
    Course course = courseService.findById(courseId);
    List<Homework> homework = homeworkService.findByCourseId(courseId);

    // 获取老师和助教信息
    List<TeacherCourse> teacherCourses = teacherCourseService.findByCourseId(courseId);
    Collections.reverse(teacherCourses);
    List<BeanTeacher> teachers = new ArrayList<>();
    List<BeanTeacher> assistants = new ArrayList<>();
    for (TeacherCourse teacherCourse : teacherCourses) {
      if (!"学生".equals(teacherCourse.getRole())) {
        teachers.add(
            beanTeacherService.getTeacher(teacherCourse.getTeacher(), teacherCourse.getCourse()));
      } else {
        assistants.add(
            beanTeacherService.getTeacher(teacherCourse.getTeacher(), teacherCourse.getCourse()));
      }
    }

    // 获取学生信息
    List<StudentCourse> studentCourses = studentCourseService.findByCourseId(courseId);
    List<Object> students = new ArrayList<>();
    for (StudentCourse studentCourse : studentCourses) {
      students.add(beanStudentService.getStudent(studentCourse.getStudent()));
    }
    students.addAll(assistants); // 助教也是学生

    HashMap<String, Object> responseMap = new HashMap<>();
    responseMap.put("course", course);
    responseMap.put("homework", homework);
    responseMap.put("teacher", teachers);
    responseMap.put("tc", teacherCourses);
    responseMap.put("student", students);
    responseMap.put("address",
        pictureService.findById(String.valueOf(courseId)).getAddress().replace("min", "big"));
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(responseMap);
  }

  /**
   * 根据课程 id 获取本课程的学生人数
   */
  @RequestMapping(value = "/studentNum")
  public int number(@RequestBody Map<String, String> data) {
    int course = Integer.parseInt(data.get("courseId"));
    List<StudentCourse> studentCourse = studentCourseService.findByCourseId(course);
    System.out.println(course + "  " + studentCourse.size());
    return studentCourse.size();
  }

  /**
   * 根据课程负责人 id 查找负责人的账号
   */
  @RequestMapping(value = "/master")
  public String master(@RequestBody Map<String, String> data) throws ParseException {
    int masterId = Integer.parseInt(data.get("master"));
    return authorizeTeacherService.findById(masterId).getAccount();
  }

  /**
   * 根据作业 id 获取作业信息
   */
  @RequestMapping(value = "/homework")
  public Homework homework(@RequestBody Map<String, String> data) throws ParseException {
    int homeworkId = Integer.parseInt(data.get("homeworkId"));
    return homeworkService.findById(homeworkId);
  }

  /**
   * 根据课程 id 获取所有公告
   */
  @RequestMapping(value = "/inform")
  public List<Inform> inform(@RequestBody Map<String, String> data) throws ParseException {
    int courseId = Integer.parseInt(data.get("courseId"));
    return informService.findByCourseId(courseId);
  }

  /**
   * 根据公告 id 获取公告信息
   */
  @RequestMapping(value = "/informId")
  public Inform informId(@RequestBody Map<String, String> data) throws ParseException {
    int informId = Integer.parseInt(data.get("InformId"));
    return informService.findById(informId);
  }

  /**
   * 根据公告发布人 id 获取发布人信息
   */
  @RequestMapping(value = "/issuer")
  public Object getUserTeacher(@RequestBody Map<String, String> data)
      throws JsonProcessingException {
    // 只有老师才能发布公告
    int teacherId = Integer.parseInt(data.get("userId"));
    AuthorizeTeacher authorizeTeacher = authorizeTeacherService.findById(teacherId);
    HashMap<String, Object> responseMap = new HashMap<>();
    responseMap.put("user", authorizeTeacher);
    Picture picture = pictureService.findById(authorizeTeacher.getAccount());
    responseMap.put("address", picture.getAddress());
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(responseMap);
  }

  /**
   * 根据作业 id 获取所有作业评论
   */
  @RequestMapping(value = "/comments")
  public List<HomeworkComments> comment(@RequestBody Map<String, String> data)
      throws ParseException {
    int homeworkId = Integer.parseInt(data.get("homeworkId"));
    return homeworkCommentsService.findByHomeworkId(homeworkId);
  }

  /**
   * 根据公告 id 获取全部公告评论
   */
  @RequestMapping(value = "/InformComments")
  public List<InformComments> informComment(@RequestBody Map<String, String> data)
      throws ParseException {
    int informId = Integer.parseInt(data.get("informId"));
    return informCommentsService.findByInformId(informId);
  }

  /**
   * 发表作业评论
   */
  @RequestMapping(value = "/discuss")
  public String discuss(@RequestBody Map<String, String> data) throws ParseException {
    HomeworkComments homeworkComments = new HomeworkComments();
    homeworkComments.setAccount(data.get("account"));
    homeworkComments.setHomeworkid(Integer.parseInt(data.get("homeworkid")));
    homeworkComments.setImage(data.get("image"));
    homeworkComments.setWord(data.get("word"));
    homeworkComments.setTime(data.get("time"));
    homeworkComments.setName(data.get("name"));
    homeworkCommentsService.create(homeworkComments);
    return "发表成功";
  }

  /**
   * 发表公告评论
   */
  @RequestMapping(value = "/InformDiscuss")
  public String informDiscuss(@RequestBody Map<String, String> data) throws ParseException {
    InformComments comments = new InformComments();
    comments.setAccount(data.get("account"));
    comments.setInformid(Integer.parseInt(data.get("informId")));
    comments.setImage(data.get("image"));
    comments.setWord(data.get("word"));
    comments.setTime(data.get("time"));
    comments.setName(data.get("name"));
    informCommentsService.createInformComments(comments);
    return "发表成功";
  }

  /**
   * 删除作业评论
   */
  @RequestMapping(value = "/deleteComments")
  public String deleteComments(@RequestBody Map<String, String> data) throws ParseException {
    homeworkCommentsService.deleteById(Integer.parseInt(data.get("id")));
    return "删除成功";
  }

  /**
   * 删除公告评论
   */
  @RequestMapping(value = "/deleteInformComments")
  public String deleteInformComments(@RequestBody Map<String, String> data) throws ParseException {
    informCommentsService.deleteById(Integer.parseInt(data.get("id")));
    return "删除成功";
  }
}
