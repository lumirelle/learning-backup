package com.example.classroomschool.entity.course;

public class Course {

  private Integer id;

  private String name;

  private String className;

  private Integer master;

  private String courseKey;

  private String deleting;

  private String year;

  private String term;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name == null ? null : name.trim();
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className == null ? null : className.trim();
  }

  public Integer getMaster() {
    return master;
  }

  public void setMaster(Integer master) {
    this.master = master;
  }

  public String getCourseKey() {
    return courseKey;
  }

  public void setCourseKey(String courseKey) {
    this.courseKey = courseKey == null ? null : courseKey.trim();
  }

  public String getDeleting() {
    return deleting;
  }

  public void setDeleting(String deleting) {
    this.deleting = deleting == null ? null : deleting.trim();
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year == null ? null : year.trim();
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term == null ? null : term.trim();
  }
}
