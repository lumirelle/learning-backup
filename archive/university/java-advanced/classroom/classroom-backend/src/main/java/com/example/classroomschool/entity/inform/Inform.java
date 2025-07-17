package com.example.classroomschool.entity.inform;

public class Inform {

  private Integer id;

  private Integer course;

  private String name;

  private String intro;

  private String time;

  private String top;

  private Integer user;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getCourse() {
    return course;
  }

  public void setCourse(Integer course) {
    this.course = course;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name == null ? null : name.trim();
  }

  public String getIntro() {
    return intro;
  }

  public void setIntro(String intro) {
    this.intro = intro == null ? null : intro.trim();
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time == null ? null : time.trim();
  }

  public String getTop() {
    return top;
  }

  public void setTop(String top) {
    this.top = top == null ? null : top.trim();
  }

  public Integer getUser() {
    return user;
  }

  public void setUser(Integer user) {
    this.user = user;
  }
}
