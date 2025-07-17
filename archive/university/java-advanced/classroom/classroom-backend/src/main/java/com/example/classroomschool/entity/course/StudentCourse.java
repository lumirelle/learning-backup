package com.example.classroomschool.entity.course;

public class StudentCourse {

  private Integer id;

  private Integer student;

  private Integer course;

  private String pigeonhole;

  private String top;

  private Integer displayNum;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getStudent() {
    return student;
  }

  public void setStudent(Integer student) {
    this.student = student;
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
}
