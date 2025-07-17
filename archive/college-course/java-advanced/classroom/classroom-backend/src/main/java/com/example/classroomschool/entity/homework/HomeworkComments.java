package com.example.classroomschool.entity.homework;

public class HomeworkComments {

  private Integer id;

  private String account;

  private String name;

  private String time;

  private String image;

  private String word;

  private Integer homeworkid;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account == null ? null : account.trim();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name == null ? null : name.trim();
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time == null ? null : time.trim();
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image == null ? null : image.trim();
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word == null ? null : word.trim();
  }

  public Integer getHomeworkid() {
    return homeworkid;
  }

  public void setHomeworkid(Integer homeworkid) {
    this.homeworkid = homeworkid;
  }
}
