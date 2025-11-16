package com.example.classroomschool.controller;

import com.example.classroomschool.entity.course.StudentCourse;
import com.example.classroomschool.entity.homework.StudentSubmitHomework;
import com.example.classroomschool.entity.student.AuthorizeStudent;
import com.example.classroomschool.entity.course.Course;
import com.example.classroomschool.entity.course.CourseWithSC;
import com.example.classroomschool.entity.course.TeacherCourse;
import com.example.classroomschool.entity.homework.Homework;
import com.example.classroomschool.entity.homework.StudentSubmitHomeworkFile;
import com.example.classroomschool.entity.inform.Inform;
import com.example.classroomschool.util.pinyin.Chines2PingUtil;
import com.example.classroomschool.service.QiNiuService;
import com.example.classroomschool.util.token.TokenUtil;
import com.example.classroomschool.util.encryption.MD5Encoder;
import com.example.classroomschool.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.common.QiniuException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

  @Resource
  CourseWithSCService courseWithSCService;

  @Resource
  AuthorizeStudentService authorizeStudentService;

  @Resource
  StudentCourseService studentCourseService;

  @Resource
  TeacherCourseService teacherCourseService;

  @Resource
  CourseService courseService;

  @Resource
  HomeworkService homeworkService;

  @Resource
  StudentSubmitHomeworkService studentSubmitHomeworkService;

  @Resource
  HomeworkCommentsService homeworkCommentsService;

  @Resource
  InformService informService;

  @Resource
  InformCommentsService informCommentsService;

  @Resource
  StudentSubmitHomeworkFileService studentSubmitHomeworkFileService;

  @Resource
  QiNiuService qiNiuService;

  /**
   * 学生获取自己的所有课程
   */
  @RequestMapping(value = "/course")
  public List<CourseWithSC> allCourse(@RequestBody Map<String, String> token) {
    String account = TokenUtil.getAccount(token.get("token"));
    AuthorizeStudent authorizeStudent = authorizeStudentService.findByAccount(account);
    List<CourseWithSC> coursInSCS = courseWithSCService.findByStudentId(authorizeStudent.getId());
    // 查询课程对应的人数
    for (CourseWithSC courseWithSC : coursInSCS) {
      List<StudentCourse> studentCourse = studentCourseService.findByCourseId(courseWithSC.getCourseId());
      courseWithSC.setStudentNum(studentCourse.size());
    }
    return coursInSCS;
  }

  /**
   * 学生进行课程置顶
   */
  @RequestMapping(value = "/changeTop")
  public String changeTheTop(@RequestBody Map<String, String> data) {
    String top;
    int id = Integer.parseInt(data.get("id"));
    int courseId = Integer.parseInt(data.get("courseId"));
    // 如果置顶，就改为不置顶，反之亦然
    if ("0".equals(data.get("top"))) {
      top = "1";
    } else {
      top = "0";
    }
    // 操作
    int result = studentCourseService.topTheCourse(id, courseId, top);
    if (result == 1) {
      if ("1".equals(top)) {
        return "置顶成功";
      } else {
        return "已取消置顶";
      }
    } else {
      return "失败";
    }
  }

  /**
   * 学生加入课程
   */
  @RequestMapping(value = "/courseJoin")
  public String joinCourse(@RequestBody Map<String, String> data) {
    // 加课码
    String courseKey = data.get("key");
    int student = Integer.parseInt(data.get("id"));
    Course course = courseService.findCourseByKey(courseKey);

    // 判断课程是否存在
    if (course == null) {
      return "课程不存在";
    }
    // 判断加课码是否停用
    if ("1".equals(course.getDeleting())) {
      return "加课码已停用";
    }
    // 判断本学生是否已加入该课程
    if (studentCourseService.findStudentCourse(student, course.getId()) != null) {
      return "你已加入此课程";
    }
    // 判断课程是否归档
    TeacherCourse testTeacherCourse = teacherCourseService.findTC(course.getMaster(), course.getId());
    if ("1".equals(testTeacherCourse.getPigeonhole())) {
      return "加课码已失效";
    }

    List<StudentCourse> studentCourses = studentCourseService.findByStudent(student);
    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setStudent(student);
    studentCourse.setCourse(course.getId());
    studentCourse.setPigeonhole("0");
    studentCourse.setTop("1"); // 默认置顶
    if (studentCourses == null || studentCourses.size() == 0) {
      studentCourse.setDisplayNum(1);
    } else {
      studentCourse.setDisplayNum(studentCourses.size() + 1);
    }
    studentCourseService.joinCourse(studentCourse);
    return "加课成功";
  }

  /**
   * 学生退课
   */
  @RequestMapping(value = "/courseOut")
  public String outCourse(@RequestBody Map<String, String> data)
      throws UnsupportedEncodingException, NoSuchAlgorithmException, QiniuException {
    int courseId = Integer.parseInt(data.get("courseId"));
    String password = data.get("password");
    int studentId = Integer.parseInt(data.get("id"));
    String account = TokenUtil.getAccount(data.get("token"));

    AuthorizeStudent authorizeStudent = authorizeStudentService.findByAccount(account);
    MD5Encoder md5Encoder = new MD5Encoder();

    if (authorizeStudent.getPassword().equals(md5Encoder.encode(password))) {
      int result = studentCourseService.deleteCourse(studentId, courseId);
      for (Homework homework : homeworkService.findByCourseId(courseId)) {
        String accounts = authorizeStudentService.findById(studentId).getAccount();
        // 文件删除
        for (StudentSubmitHomeworkFile studentSubmitHomeworkFile : studentSubmitHomeworkFileService.findUserFiles(accounts, homework.getId())) {
          String name = accounts + "-" + homework.getId() + "-" + studentSubmitHomeworkFile.getFilename();
          qiNiuService.delete(Chines2PingUtil.getFullSpell(name));
        }
        // 删除学生课程中的作业
        studentSubmitHomeworkService.deleteUserHomework(accounts, homework.getId());
        studentSubmitHomeworkFileService.deleteUserFiels(accounts, homework.getId());
        // 删除学生课程中的作业评论
        homeworkCommentsService.deleteUserComments(accounts, homework.getId());
      }
      // 删除学生公告中评论
      for (Inform inform : informService.findByCourseId(courseId)) {
        informCommentsService.deleteUserComments(
            authorizeStudentService.findById(studentId).getAccount(), inform.getId());
      }
      if (result == 1) {
        return "退课成功";
      } else {
        return "失败";
      }
    } else {
      return "密码错误";
    }
  }

  /**
   * 学生课程归档
   */
  @RequestMapping(value = "/changePigeonhole")
  public String pigeonholeCourse(@RequestBody Map<String, String> data) {
    int course = Integer.parseInt(data.get("courseId"));
    int student = Integer.parseInt(data.get("id"));
    int pigeonhole = Integer.parseInt(data.get("pigeonhole"));
    int result;

    if (pigeonhole == 0) {
      result = studentCourseService.pigeonholeCourse(student, course, "1");
      if (result == 1) {
        return "归档成功";
      } else {
        return "失败";
      }
    } else {
      result = studentCourseService.pigeonholeCourse(student, course, "0");
      if (result == 1) {
        return "取消归档";
      } else {
        return "失败";
      }
    }
  }


  /**
   * 学生课程排序
   */
  @RequestMapping(value = "/courseSort")
  public String courseSort(@RequestBody Map<String, List<Map<String, Object>>> list) {
    int i = 0;
    List<Map<String, Object>> scCourses = list.get("list");
    for (; i < scCourses.size(); i++) {
      int course = (int) scCourses.get(i).get("courseId");
      int student = (int) scCourses.get(i).get("student");
      int index = studentCourseService.sortNum(course, student, i + 1);
    }
    List<StudentCourse> studentCourses = studentCourseService.selectTop((int) scCourses.get(0).get("student"));
    for (StudentCourse studentCourse : studentCourses) {
      studentCourseService.sortNum(studentCourse.getCourse(), studentCourse.getStudent(), i + 1);
      i++;
    }
    List<StudentCourse> scs1 = studentCourseService.selectPigeonhole((int) scCourses.get(0).get("student"));
    for (StudentCourse studentCourse : scs1) {
      studentCourseService.sortNum(studentCourse.getCourse(), studentCourse.getStudent(), i + 1);
      i++;
    }
    return "成功";
  }

  /**
   * 作业信息
   */
  @RequestMapping(value = "/homework")
  public Homework homework(@RequestBody Map<String, String> data) throws ParseException {
    int course = Integer.parseInt(data.get("homeworkId"));
    return homeworkService.findById(course);
  }


  /**
   * 学生作业信息显示
   */
  @RequestMapping(value = "/homeworkShow")
  public String homeworkShow(@RequestBody Map<String, String> data)
      throws ParseException, JsonProcessingException {
    int homeworkId = Integer.parseInt(data.get("homeworkId"));
    String studentId = data.get("studentId");
    HashMap<String, Object> hs = new HashMap<>();
    hs.put("files", studentSubmitHomeworkFileService.findUserFiles(studentId, homeworkId));
    hs.put("sw", studentSubmitHomeworkService.getHomework(studentId, homeworkId));
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(hs);
  }

  /**
   * 学生交作业作业
   */
  @RequestMapping(value = "/homeworkSubmit")
  public String handInHomework(@RequestBody Map<String, String> data) throws ParseException {
    String account = TokenUtil.getAccount(data.get("token"));
    AuthorizeStudent authorizeStudent = authorizeStudentService.findByAccount(account);
    String studentId = authorizeStudent.getAccount();
    int homework = Integer.parseInt(data.get("homeworkId"));
    Homework homework1 = homeworkService.findById(homework);
    StudentSubmitHomework studentSubmitHomework2 = studentSubmitHomeworkService.getHomework(studentId, homework);
    StudentSubmitHomework studentSubmitHomework = new StudentSubmitHomework();
    studentSubmitHomework.setStudent(studentId);
    studentSubmitHomework.setHomework(homework);
    studentSubmitHomework.setStatus("0");
    studentSubmitHomework.setAnswer(data.get("answer"));
    //设置日期格式
    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    studentSubmitHomework.setTime(df.format(new Date()));
    studentSubmitHomework.setReview("0");

    if ("1".equals(homework1.getOvertime())) {
      ParsePosition pos = new ParsePosition(0);
      if (!new Date().before(df.parse(homework1.getEndTime(), pos))) {
        return "超时不能提交";
      }
    }
    if (studentSubmitHomework2 == null) {
      studentSubmitHomeworkService.handIn(studentSubmitHomework);
      return "提交成功";
    } else {
      studentSubmitHomework.setId(studentSubmitHomework2.getId());
      studentSubmitHomeworkService.update(studentSubmitHomework);
      return "修改成功";
    }
  }
}
