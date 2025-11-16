package com.example.classroomschool.entity.homework;

public class StudentSubmitHomework {

  private Integer id;

  private String student;

  private Integer homework;

  private String status;

  private String answer;

  private String time;

  private String file;

  private Integer score;

  private String review;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getStudent() {
    return student;
  }

  public void setStudent(String student) {
    this.student = student == null ? null : student.trim();
  }

  public Integer getHomework() {
    return homework;
  }

  public void setHomework(Integer homework) {
    this.homework = homework;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status == null ? null : status.trim();
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer == null ? null : answer.trim();
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time == null ? null : time.trim();
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file == null ? null : file.trim();
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  public String getReview() {
    return review;
  }

  public void setReview(String review) {
    this.review = review == null ? null : review.trim();
  }
}
