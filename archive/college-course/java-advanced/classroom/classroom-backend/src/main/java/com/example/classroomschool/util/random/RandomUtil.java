package com.example.classroomschool.util.random;

public class RandomUtil {

  /**
   * 随机生成账号，学生账号以s开头，教师账号以t开头
   *
   * @param isTeacher 是否是教师
   * @return 账号
   */
  public static String randomAccount(boolean isTeacher) {
    return (isTeacher ? "t" : "s") + String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
  }

  public static String randomAvatarAddress() {
    int value = (int) (Math.random() * 31 + 10);
    return "https://www.ketangpai.com/Public/Common/img/40/" + value + ".png";
  }

  public static String randomCourseSkinAddress() {
    int value = (int) (Math.random() * 44 + 1);
    if (value < 10) {
      return "https://assets.ketangpai.com/theme/teacher/min/0" + value + ".png";
    } else {
      return "https://assets.ketangpai.com/theme/teacher/min/" + value + ".png";
    }
  }

  public static String randomCourseKey() {
    StringBuilder newCourseKey = new StringBuilder();
    String chars = "ABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789";
    for (int i = 0; i < 6; i++) {
      int value = (int) (Math.random() * 36 + 0);
      newCourseKey.append(chars.charAt(value));
    }
    return newCourseKey.toString();
  }
}
