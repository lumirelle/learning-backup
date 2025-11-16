package com.example.classroomschool.controller;


import com.example.classroomschool.entity.course.Course;
import com.example.classroomschool.entity.course.StudentCourse;
import com.example.classroomschool.entity.course.TeacherCourse;
import com.example.classroomschool.entity.course.CourseWithTC;
import com.example.classroomschool.entity.homework.Homework;
import com.example.classroomschool.entity.homework.StudentHomework;
import com.example.classroomschool.entity.homework.StudentSubmitHomework;
import com.example.classroomschool.entity.homework.StudentSubmitHomeworkFile;
import com.example.classroomschool.entity.inform.Inform;
import com.example.classroomschool.entity.picture.Picture;
import com.example.classroomschool.entity.student.AuthorizeStudent;
import com.example.classroomschool.entity.teacher.AuthorizeTeacher;
import com.example.classroomschool.util.random.RandomUtil;
import com.example.classroomschool.util.pinyin.Chines2PingUtil;
import com.example.classroomschool.service.QiNiuService;
import com.example.classroomschool.util.token.TokenUtil;
import com.example.classroomschool.util.encryption.MD5Encoder;
import com.example.classroomschool.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.common.QiniuException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

  @Resource
  CourseWithTCService courseWithTCService;
  @Resource
  AuthorizeTeacherService authorizeTeacherService;
  @Resource
  TeacherCourseService teacherCourseService;
  @Resource
  CourseService courseService;
  @Resource
  StudentCourseService studentCourseService;
  @Resource
  PictureService pictureService;
  @Resource
  HomeworkService homeworkService;
  @Resource
  StudentHomeWorkService studentHomeWorkService;
  @Resource
  AuthorizeStudentService authorizeStudentService;
  @Resource
  StudentSubmitHomeworkService studentSubmitHomeworkService;
  @Resource
  InformService informService;
  @Resource
  HomeworkCommentsService homeworkCommentsService;
  @Resource
  InformCommentsService informCommentsService;
  @Resource
  StudentSubmitHomeworkFileService studentSubmitHomeworkFileService;
  @Resource
  QiNiuService qiNiuService;


  /**
   * 老师获取自己的所有课程
   */
  @RequestMapping(value = "/course")
  public List<CourseWithTC> allCourse(@RequestBody Map<String, String> token) {
    String account = TokenUtil.getAccount(token.get("token"));
    AuthorizeTeacher authorizeTeacher = authorizeTeacherService.findByAccount(account);
    List<CourseWithTC> courseWithTCS = courseWithTCService.findByTeacherId(authorizeTeacher.getId());
    for (CourseWithTC courseWithTC : courseWithTCS) {
      List<StudentCourse> studentCourse = studentCourseService.findByCourseId(courseWithTC.getCourseId());
      courseWithTC.setStudentNum(studentCourse.size());
    }
    return courseWithTCS;
  }

  /**
   * 老师进行课程置顶
   */
  @RequestMapping(value = "/changeTop")
  public String changeTheTop(@RequestBody Map<String, String> data) {
    String top = "0";
    int id = Integer.parseInt(data.get("id"));
    int courseId = Integer.parseInt(data.get("courseId"));
    if ("0".equals(data.get("top"))) {
      top = "1";
    } else {
      top = "0";
    }
    int index = teacherCourseService.topTheCourse(id, courseId, top);
    if (index == 1) {
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
   * 加课码停用与恢复
   */
  @RequestMapping(value = "/changeCourseKey")
  public String changeCourseKeyStatus(@RequestBody Map<String, String> data) {
    int id = Integer.parseInt(data.get("id"));

    // 0为停用，1为恢复
    if ("0".equals(data.get("deleting"))) {
      if (courseService.stopTheKey(id, "1") == 1) {
        return "加课码已停用";
      } else {
        return "失败";
      }
    } else {
      if (courseService.stopTheKey(id, "0") == 1) {
        return "加课码已恢复";
      } else {
        return "失败";
      }
    }
  }

  /**
   * 加课码重置
   */
  @RequestMapping(value = "/resetCourseKey")
  public String resetCourseKey(@RequestBody Map<String, String> data) {
    int id = Integer.parseInt(data.get("id"));

    // 生成6位随机加课码，重复则重新生成
    String newCourseKey = "";
    int result = 0;
    while (true) {
      newCourseKey = RandomUtil.randomCourseKey();
      Course testCourse = courseService.findCourseByKey(newCourseKey);
      if (testCourse == null) {
        result = courseService.resetTheKey(id, newCourseKey);
        break;
      }
    }
    if (result == 1) {
      return "加课码已重置";
    } else {
      return "失败";
    }
  }

  /**
   * 教师加入课程
   */
  @RequestMapping(value = "/courseJoin")
  public String joinCourse(@RequestBody Map<String, String> data) {
    // 课程加课码
    String courseKey = data.get("key");
    int teacher = Integer.parseInt(data.get("id"));
    Course course = courseService.findCourseByKey(courseKey);

    if (course == null) {
      return "加课码不存在";
    }
    if ("1".equals(course.getDeleting())) {
      return "加课码已停用";
    }
    // 判断该老师是否已加入课程
    TeacherCourse testTeacherCourse = teacherCourseService.findTC(teacher, course.getId());
    if (testTeacherCourse != null) {
      if ("1".equals(testTeacherCourse.getPigeonhole())) {
        return "加课码已失效";
      }
      if ("0".equals(testTeacherCourse.getPigeonhole())) {
        return "此课程已加入";
      }
    }

    // 获取教师所有课程
    List<TeacherCourse> teacherCourses = teacherCourseService.findByTeacherId(teacher);
    TeacherCourse teacherCourse = new TeacherCourse();
    teacherCourse.setTeacher(teacher);
    teacherCourse.setCourse(course.getId());
    teacherCourse.setPigeonhole("0");
    teacherCourse.setTop("1"); // 默认置顶
    teacherCourse.setRole("学生");
    if (teacherCourses == null || teacherCourses.size() == 0) {
      teacherCourse.setDisplayNum(1);
    } else {
      teacherCourse.setDisplayNum(teacherCourses.size() + 1);
    }
    teacherCourseService.joinCourse(teacherCourse);
    return "加课成功";
  }

  /**
   * 老师课程创建与修改
   */
  @RequestMapping(value = "/courseEdit")
  public String establish(@RequestBody Map<String, String> data) {
    String info = data.get("new");
    // 创建
    if ("1".equals(info)) {
      // 初始化一个课程
      Course course = new Course();
      course.setName(data.get("name"));
      course.setClassName(data.get("className"));
      course.setMaster(Integer.parseInt(data.get("master")));
      // 生成 6 位随机加课码，重复则重新生成
      String courseKey = "";
      while (true) {
        courseKey = RandomUtil.randomCourseKey();
        Course testCourse = courseService.findCourseByKey(courseKey);
        if (testCourse == null) {
          course.setCourseKey(courseKey);
          break;
        }
      }
      course.setDeleting("0");
      course.setYear(data.get("year"));
      course.setTerm(data.get("term"));
      int result = courseService.addCourse(course);

      // 创建成功，让老师自动加入课程
      if (result == 1) {
        course = courseService.findCourseByKey(courseKey);
        int teacherId = Integer.parseInt(data.get("master"));
        List<TeacherCourse> teacherCourses = teacherCourseService.findByTeacherId(teacherId);
        TeacherCourse teacherCourse = new TeacherCourse();
        teacherCourse.setTeacher(teacherId);
        teacherCourse.setCourse(course.getId());
        teacherCourse.setPigeonhole("0");
        teacherCourse.setTop("1");
        teacherCourse.setRole("管理员");
        if (teacherCourses == null || teacherCourses.size() == 0) {
          teacherCourse.setDisplayNum(1);
        } else {
          teacherCourse.setDisplayNum(teacherCourses.size() + 1);
        }
        result = teacherCourseService.joinCourse(teacherCourse);

        int value = (int) (Math.random() * 44 + 1);
        String address = RandomUtil.randomCourseSkinAddress();
        Picture picture = new Picture();
        picture.setPicid(String.valueOf(course.getId()));
        picture.setAddress(address);
        int i = pictureService.createPicture(picture);

        if (result == 1) {
          return "创建课程成功";
        } else {
          return "失败";
        }
      }
      // 创建失败
      else {
        return "创建失败";
      }
    }
    // 编辑
    else {
      int id = Integer.parseInt(data.get("id"));
      int master = Integer.parseInt(data.get("master"));
      String name = data.get("name");
      String classname = data.get("className");
      String year = data.get("year");
      String term = data.get("term");
      courseService.alterCourse(id, master, name, classname, year, term);
      return "课程已编辑";
    }
  }

  /**
   * 管理员删除课程/老师退课
   */
  @RequestMapping(value = "/courseOut")
  public String outCourse(@RequestBody Map<String, String> data)
      throws UnsupportedEncodingException, NoSuchAlgorithmException, QiniuException {
    int courseId = Integer.parseInt(data.get("courseId"));
    String password = data.get("password");
    int teacherId = Integer.parseInt(data.get("id"));
    String account = TokenUtil.getAccount(data.get("token"));

    AuthorizeTeacher authorizeTeacher = authorizeTeacherService.findByAccount(account);
    MD5Encoder md5Encoder = new MD5Encoder();

    // 判断密码是否正确
    if (authorizeTeacher.getPassword().equals(md5Encoder.encode(password))) {
      // 判断是否为管理员，是则完全删除，否则只是退课
      if ("管理员".equals(teacherCourseService.findTC(authorizeTeacher.getId(), courseId).getRole())) {
        // 该课程的所有作业
        for (Homework homework : homeworkService.findByCourseId(courseId)) {
          // 文件删除
          for (StudentSubmitHomeworkFile studentSubmitHomeworkFile : studentSubmitHomeworkFileService.findByHomeworkId(homework.getId())) {
            String fileName =
                studentSubmitHomeworkFile.getStudent() + "-" + studentSubmitHomeworkFile.getHomework() + "-" + studentSubmitHomeworkFile.getFilename();
            qiNiuService.delete(Chines2PingUtil.getFullSpell(fileName));
          }
          studentSubmitHomeworkFileService.deleteByHomeworkId(homework.getId());
          studentSubmitHomeworkService.deleteByHomeworkId(homework.getId());
          homeworkCommentsService.deleteByHomeworkId(homework.getId());
          homeworkService.deleteByHomeworkId(homework.getId());
        }
        // 所有公告
        for (Inform inform : informService.findByCourseId(courseId)) {
          informService.deleteByInformId(inform.getId());
          informCommentsService.deleteByInformId(inform.getId());
        }
        // 所有拥有该课程的关系
        teacherCourseService.deleteByCourseId(courseId);
        studentCourseService.deleteByCourseId(courseId);
        courseService.deleteById(courseId);
        return "删除成功";
      }
      // 不是管理员
      else {
        // 老师在课程中的作业
        for (Homework homework : homeworkService.findByCourseId(courseId)) {
          // 文件删除
          for (StudentSubmitHomeworkFile studentSubmitHomeworkFile : studentSubmitHomeworkFileService.findUserFiles(account, homework.getId())) {
            String fileName = account + "-" + homework.getId() + "-" + studentSubmitHomeworkFile.getFilename();
            qiNiuService.delete(Chines2PingUtil.getFullSpell(fileName));
          }
          studentSubmitHomeworkFileService.deleteUserFiels(account, homework.getId());
          // 删除老师在课程中的作业评论
          homeworkCommentsService.deleteUserComments(account, homework.getId());
          // 删除老师再课程中的作业
          studentSubmitHomeworkService.deleteUserHomework(account, homework.getId());
        }
        // 老师在公告中的评论
        for (Inform inform : informService.findByCourseId(courseId)) {
          informCommentsService.deleteUserComments(
              authorizeTeacherService.findById(teacherId).getAccount(), inform.getId());
        }
        teacherCourseService.deleteTC(teacherId, courseId);
        return "退课成功";
      }
    }
    // 密码错误
    else {
      return "密码错误";
    }
  }

  /**
   * 老师归档课程
   */
  @RequestMapping(value = "/changePigeonhole")
  public String pigeonholeCourse(@RequestBody Map<String, String> data) {
    int course = Integer.parseInt(data.get("courseId"));
    int teacher = Integer.parseInt(data.get("id"));
    int pigeonhole = Integer.parseInt(data.get("pigeonhole"));
    int all = Integer.parseInt(data.get("all")); // 0为归档自己，1为归档所有人
    int result;

    if (all == 0) {
      if (pigeonhole == 0) {
        result = teacherCourseService.pigeonholeCourse(teacher, course, "1");
        if (result == 1) {
          return "归档成功";
        } else {
          return "失败";
        }
      } else {
        result = teacherCourseService.pigeonholeCourse(teacher, course, "0");
        if (result == 1) {
          return "取消归档";
        } else {
          return "失败";
        }
      }
    } else {
      if (pigeonhole == 0) {
        result = teacherCourseService.pigeonholeCourse(teacher, course, "1");
        result |= studentCourseService.pigeonholeByCourse(course, "1");
        if (result == 1) {
          return "归档成功";
        } else {
          return "失败";
        }
      } else {
        result = teacherCourseService.pigeonholeCourse(teacher, course, "0");
        result |= studentCourseService.pigeonholeByCourse(course, "0");
        if (result == 1) {
          return "取消归档";
        } else {
          return "失败";
        }
      }
    }
  }

  /**
   * 添加助教
   */
  @RequestMapping(value = "/addTeacher")
  public String addTeacher(@RequestBody Map<String, String> data) {
    AuthorizeTeacher authorizeTeacher = authorizeTeacherService.findByAccountKey(data.get("teacherInfo"));
    AuthorizeStudent authorizeStudent = authorizeStudentService.findByAccountKey(data.get("teacherInfo"));
    if (authorizeStudent != null) {
      return "邀请人不能为学生";
    } else {
      if (authorizeTeacher == null) {
        return "邀请人不存在";
      } else {
        if (teacherCourseService.findTC(authorizeTeacher.getId(), Integer.parseInt(data.get("courseId"))) != null) {
          return "该教师已在这门课中";
        } else {
          List<TeacherCourse> teacherCourseTeacher = teacherCourseService.findByTeacherId(authorizeTeacher.getId());
          TeacherCourse teacherCourse = new TeacherCourse();
          teacherCourse.setTeacher(authorizeTeacher.getId());
          teacherCourse.setCourse(Integer.parseInt(data.get("courseId")));
          teacherCourse.setPigeonhole("0");
          teacherCourse.setTop("1");
          teacherCourse.setRole("助教");
          if (teacherCourseTeacher == null || teacherCourseTeacher.size() == 0) {
            teacherCourse.setDisplayNum(1);
          } else {
            teacherCourse.setDisplayNum(teacherCourseTeacher.size() + 1);
          }
          int index = teacherCourseService.joinCourse(teacherCourse);
          return "邀请成功";
        }
      }
    }
  }

  /**
   * 老师课程排序
   */
  @RequestMapping(value = "/courseSort")
  public String courseSort(@RequestBody Map<String, List<Map<String, Object>>> list) {
    List<Map<String, Object>> tcCourses = list.get("list");
    for (int i = 0; i < tcCourses.size(); i++) {
      int course = (int) tcCourses.get(i).get("courseId");
      int teacher = (int) tcCourses.get(i).get("teacher");
      int index = teacherCourseService.sortNum(course, teacher, i + 1);
    }
    return "成功";
  }

  /**
   * 老师发布作业与修改
   */
  @RequestMapping(value = "/homeworkEdit")
  public String PublishHomework(@RequestBody Map<String, String> data) throws ParseException {
    Homework homework = new Homework();
    String overtime = "0";
    homework.setCourse(Integer.parseInt(data.get("courseId")));
    homework.setName(data.get("name"));
    homework.setIntro(data.get("intro"));
    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    homework.setStartTime(df.format(new Date()));
    homework.setEndTime(data.get("endTime"));
    homework.setMaxScore(data.get("maxScore"));
    homework.setType("0");
    if (data.get("overtime").equals("true")) {
      overtime = "1";
    } else {
      overtime = "0";
    }
    homework.setOvertime(overtime);
    if (data.get("new").equals("1")) {
      int index = homeworkService.addHomework(homework);
      return "作业发布成功";
    } else {
      int homeworkId = Integer.parseInt(data.get("homeworkId"));
      homework.setId(homeworkId);
      int index = homeworkService.updateHomework(homework);
      return "修改成功";
    }

  }

  /**
   * 老师发布公告与修改
   */
  @RequestMapping(value = "/informEdit")
  public String PublishInform(@RequestBody Map<String, String> data) throws ParseException {
    Inform inform = new Inform();
    inform.setCourse(Integer.parseInt(data.get("courseId")));
    inform.setName(data.get("name"));
    inform.setIntro(data.get("intro"));
    inform.setUser(Integer.parseInt(data.get("user")));
    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    inform.setTime(df.format(new Date()));

    if (data.get("new").equals("1")) {
      inform.setTop("0");
      int index = informService.AddInform(inform);
      return "公告发布成功";
    } else {
      inform.setTop(data.get("top"));
      int informId = Integer.parseInt(data.get("informId"));
      inform.setId(informId);
      int index = informService.updateInform(inform);
      return "编辑成功";
    }

  }

  /**
   * 置顶与取消公告
   */
  @RequestMapping(value = "/setTop")
  public String SetTop(@RequestBody Map<String, String> data) throws ParseException {
    String top = "0";
    int id = Integer.parseInt(data.get("id"));
    int course = Integer.parseInt(data.get("course"));
    if (data.get("top").equals("0")) {
      top = "1";
    } else {
      top = "0";
    }
    int index = informService.STop(id, top, course);
    if (index == 1) {
      if (top.equals("1")) {
        return "置顶成功";
      } else {
        return "已取消置顶";
      }
    } else {
      return "失败";
    }

  }

  /**
   * 删除公告
   */
  @RequestMapping(value = "/informDelete")
  public String InformDelete(@RequestBody Map<String, String> data) throws ParseException {
    int informId = Integer.parseInt(data.get("informId"));
    informService.deleteByInformId(informId);
    informCommentsService.deleteByInformId(informId);
    return "删除成功";

  }


  /**
   * 作业删除
   */
  @RequestMapping(value = "/homeworkDelete")
  public String HomeworkDelete(@RequestBody Map<String, String> data) throws ParseException {
    int homeworkId = Integer.parseInt(data.get("homeworkId"));
    homeworkService.deleteByHomeworkId(homeworkId);
    studentSubmitHomeworkService.deleteByHomeworkId(homeworkId);
    homeworkCommentsService.deleteByHomeworkId(homeworkId);
    return "删除成功";
  }

  /**
   * 老师交作业作业
   */
  @RequestMapping(value = "/homeworkSubmit")
  public String HandInHomework(@RequestBody Map<String, String> data) throws ParseException {
    String account = TokenUtil.getAccount(data.get("token"));
    AuthorizeTeacher student = authorizeTeacherService.findByAccount(account);
    String studentId = student.getAccount();
    int homework = Integer.parseInt(data.get("homeworkId"));
    Homework stu_homework = homeworkService.findById(homework);
    StudentSubmitHomework studentSubmitHomework2 = studentSubmitHomeworkService.getHomework(studentId, homework);
    StudentSubmitHomework studentSubmitHomework = new StudentSubmitHomework();
    studentSubmitHomework.setStudent(studentId);
    studentSubmitHomework.setHomework(homework);
    studentSubmitHomework.setStatus("0");
    studentSubmitHomework.setAnswer(data.get("answer"));
    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    studentSubmitHomework.setTime(df.format(new Date()));
    studentSubmitHomework.setReview("0");

    if (stu_homework.getOvertime().equals("1")) {
      ParsePosition pos = new ParsePosition(0);
      if (!new Date().before(df.parse(stu_homework.getEndTime(), pos))) {
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

  /**
   * 老师查看学生作业
   */
  @RequestMapping(value = "/homeworkShow")
  public Object ShowHomework(@RequestBody Map<String, String> data)
      throws ParseException, JsonProcessingException {
    int homeworkId = Integer.parseInt(data.get("homeworkId"));
    //已批人数
    int people = 0;
    //已交的作业
    List<StudentHomework> studentHomeworks = studentHomeWorkService.getHomeWork(homeworkId);
    HashMap<String, Object> hs = new HashMap<>();
    hs.put("studentHomework", studentHomeworks);
    for (StudentHomework studentHomeWork : studentHomeworks) {
      if (studentHomeWork.getReview().equals("1")) {
        people++;
      }
    }
    hs.put("number", people);
    hs.put("homework", homeworkService.findById(homeworkId));
    ObjectMapper objectMapper = new ObjectMapper();
    Homework homework = homeworkService.findById(homeworkId);

    Course course = courseService.findById(homework.getCourse());
    //课程信息
    hs.put("course", course);

    //学生未交作业
    List<StudentCourse> studentCourse = studentCourseService.findByCourseId(homework.getCourse());

    for (StudentHomework studentHomeWork : studentHomeworks) {
      for (int h = 0; h < studentCourse.size(); h++) {
        if ((authorizeStudentService.findByAccount(studentHomeWork.getAccount())) != null) {
          if (studentCourse.get(h)
              .getStudent()
              .equals((authorizeStudentService.findByAccount(studentHomeWork.getAccount())).getId())) {
            studentCourse.remove(h);
            break;
          }
        }
      }
    }
    List<Object> students = new ArrayList<Object>();
    for (StudentCourse value : studentCourse) {
      //未交作业的学生
      students.add(authorizeStudentService.findById(value.getStudent()));
    }

    //老师中学生角色未交作业
    List<TeacherCourse> teacherCourse = teacherCourseService.getNumber(homework.getCourse());
    for (StudentHomework studentHomeWork : studentHomeworks) {
      for (int h = 0; h < teacherCourse.size(); h++) {
        if ((authorizeTeacherService.findByAccount(studentHomeWork.getAccount())) != null) {
          if (teacherCourse.get(h)
              .getTeacher()
              .equals((authorizeTeacherService.findByAccount(studentHomeWork.getAccount())).getId())) {
            teacherCourse.remove(h);
            break;
          }
        }
      }
    }
//        List<Teacher> teachers = new ArrayList<Teacher>();
    for (int i = 0; i < teacherCourse.size(); i++) {
      //未交作业的老师
      students.add(authorizeTeacherService.findById(teacherCourse.get(i).getTeacher()));
    }
    //未交作业学生
    hs.put("unHandIn", students);

    return objectMapper.writeValueAsString(hs);
  }


  /**
   * 老师批改学生作业
   */
  @RequestMapping(value = "/homeworkCheck")
  public String CorrectionHomework(@RequestBody Map<String, String> data) {
    String student = data.get("studentId");
    int homeworkId = Integer.parseInt(data.get("homeworkId"));
    if (!data.get("score").equals("")) {
      int score = Integer.parseInt(data.get("score"));
      //阅读并打分
      studentSubmitHomeworkService.scoringHomework(student, homeworkId, "1", score);
      if (Integer.parseInt(homeworkService.findById(homeworkId).getMaxScore()) < score) {
        return "超过成绩最大值";
      }
      return "批改成功";
    } else {
      studentSubmitHomeworkService.readHomework(student, homeworkId, "1");
      return "请输入成绩";
    }

  }

  /**
   * 老师转让课程
   */
  @RequestMapping(value = "/courseSend")
  public String CourseSend(@RequestBody Map<String, String> data)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    int teacherId = Integer.parseInt(data.get("id"));
    int courseId = Integer.parseInt(data.get("courseId"));
    AuthorizeTeacher authorizeTeacherPlus = authorizeTeacherService.findById(Integer.parseInt(data.get("id")));
    AuthorizeTeacher authorizeTeacher = authorizeTeacherService.findByAccountKey(data.get("account"));
    AuthorizeStudent authorizeStudent = authorizeStudentService.findByAccountKey(data.get("account"));
    MD5Encoder md5Encoder = new MD5Encoder();
    if (authorizeTeacherPlus.getPassword().equals(md5Encoder.encode(data.get("password")))) {
      if (authorizeStudent != null) {
        return "转让人不能为学生";
      } else {
        if (authorizeTeacher == null) {
          return "邀请人不存在";
        } else {
          if (teacherId == authorizeTeacher.getId()) {
            return "不能转让给自己";
          } else {
            teacherCourseService.deleteTC(teacherId, courseId);
            //查看转让的人是否在此课程组
            TeacherCourse teacherCourse = teacherCourseService.findTC(authorizeTeacher.getId(), courseId);
            if (teacherCourse == null) {
              List<TeacherCourse> teacherCourseTeacher = teacherCourseService.findByTeacherId(authorizeTeacher.getId());
              TeacherCourse teacherCourse1 = new TeacherCourse();
              teacherCourse1.setTeacher(authorizeTeacher.getId());
              teacherCourse1.setCourse(courseId);
              teacherCourse1.setPigeonhole("0");
              teacherCourse1.setTop("1");
              teacherCourse1.setRole("管理员");
              if (teacherCourseTeacher == null || teacherCourseTeacher.size() == 0) {
                teacherCourse1.setDisplayNum(1);
              } else {
                teacherCourse1.setDisplayNum(teacherCourseTeacher.size() + 1);
              }
              teacherCourseService.joinCourse(teacherCourse1);
            } else {
              teacherCourse.setRole("管理员");
              teacherCourseService.update(teacherCourse);
            }
            courseService.update(courseId, teacherId, authorizeTeacher.getId());
          }
        }
        return "转让成功";
      }
    } else {
      return "密码错误";
    }

  }

  /**
   * 老师删除成员
   */
  @RequestMapping(value = "/deleteUser")
  public String DeleteUser(@RequestBody Map<String, String> data) throws QiniuException {
    int courseId = Integer.parseInt(data.get("courseId"));
    AuthorizeTeacher authorizeTeacher = authorizeTeacherService.findByAccountKey(data.get("account"));
    AuthorizeStudent authorizeStudent = authorizeStudentService.findByAccountKey(data.get("account"));
    Course course = courseService.findById(courseId);

    if (authorizeStudent != null) {
      studentCourseService.deleteCourse(authorizeStudent.getId(), courseId);
      for (Homework homework : homeworkService.findByCourseId(courseId)) {
        String accounts = authorizeStudentService.findById(authorizeStudent.getId()).getAccount();
        int homeId = homework.getId();

        //文件删除
        for (StudentSubmitHomeworkFile studentSubmitHomeworkFile : studentSubmitHomeworkFileService.findUserFiles(accounts, homeId)) {
          String name = accounts + "-" + homeId + "-" + studentSubmitHomeworkFile.getFilename();
          qiNiuService.delete(Chines2PingUtil.getFullSpell(name));
        }
        //删除学生课程中的作业
        int index2 = studentSubmitHomeworkService.deleteUserHomework(accounts, homeId);
        int index5 = studentSubmitHomeworkFileService.deleteUserFiels(accounts, homeId);
        //删除学生课程中的作业评论
        int index3 = homeworkCommentsService.deleteUserComments(accounts, homeId);


      }

      for (Inform inform : informService.findByCourseId(courseId)) {
        //删除学生公告中评论
        int index4 = informCommentsService.deleteUserComments(
            authorizeStudentService.findById(authorizeStudent.getId()).getAccount(), inform.getId());
      }
      return "删除成功";
    } else {
      if (course.getMaster().equals(authorizeTeacher.getId())) {
        return "不能删除管理员";
      } else {
        teacherCourseService.deleteTC(authorizeTeacher.getId(), courseId);
        for (Homework homework : homeworkService.findByCourseId(courseId)) {
          String accounts = authorizeTeacherService.findById(authorizeTeacher.getId()).getAccount();
          int homeId = homework.getId();
          //文件删除
          for (StudentSubmitHomeworkFile studentSubmitHomeworkFile : studentSubmitHomeworkFileService.findUserFiles(accounts, homeId)) {
            String name = accounts + "-" + homeId + "-" + studentSubmitHomeworkFile.getFilename();
            qiNiuService.delete(Chines2PingUtil.getFullSpell(name));
          }

          //删除老师课程中的作业
          int index2 = studentSubmitHomeworkService.deleteUserHomework(accounts, homeId);
          int index5 = studentSubmitHomeworkFileService.deleteUserFiels(accounts, homeId);
          //删除老师课程中的作业评论
          int index3 = homeworkCommentsService.deleteUserComments(accounts, homeId);
        }

        for (Inform inform : informService.findByCourseId(courseId)) {
          //删除老师公告中评论
          int index4 = informCommentsService.deleteUserComments(
              authorizeTeacherService.findById(authorizeTeacher.getId()).getAccount(), inform.getId());
        }
        return "删除成功";
      }
    }


  }
}
