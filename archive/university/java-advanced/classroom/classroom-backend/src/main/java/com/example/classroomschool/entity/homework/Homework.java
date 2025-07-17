package com.example.classroomschool.entity.homework;

public class Homework {

  private Integer id;

  private Integer course;

  private String name;

  private String intro;

  private String startTime;

  private String endTime;

  private String maxScore;

  private String file;

  private String type;

  private String overtime;

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

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime == null ? null : startTime.trim();
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime == null ? null : endTime.trim();
  }

  public String getMaxScore() {
    return maxScore;
  }

  public void setMaxScore(String maxScore) {
    this.maxScore = maxScore == null ? null : maxScore.trim();
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file == null ? null : file.trim();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type == null ? null : type.trim();
  }

  public String getOvertime() {
    return overtime;
  }

  public void setOvertime(String overtime) {
    this.overtime = overtime == null ? null : overtime.trim();
  }
}
