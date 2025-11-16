package com.example.classroomschool.entity.course;

public class TeacherCourse {

  private Integer id;

  private Integer teacher;

  private Integer course;

  private String pigeonhole;

  private String top;

  private Integer displayNum;

  private String role;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getTeacher() {
    return teacher;
  }

  public void setTeacher(Integer teacher) {
    this.teacher = teacher;
  }

  public Integer getCourse() {
    return course;
  }

  public void setCourse(Integer course) {
    this.course = course;
  }

  public String getPigeonhole() {
    return pigeonhole;
  }

  public void setPigeonhole(String pigeonhole) {
    this.pigeonhole = pigeonhole == null ? null : pigeonhole.trim();
  }

  public String getTop() {
    return top;
  }

  public void setTop(String top) {
    this.top = top == null ? null : top.trim();
  }

  public Integer getDisplayNum() {
    return displayNum;
  }

  public void setDisplayNum(Integer displayNum) {
    this.displayNum = displayNum;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role == null ? null : role.trim();
  }
}
