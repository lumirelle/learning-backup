package com.example.classroomschool.entity.homework;

public class StudentSubmitHomeworkFile {

  private Integer id;

  private String student;

  private Integer homework;

  private String fileurl;

  private String filename;

  private Double fileSize;

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

  public String getFileurl() {
    return fileurl;
  }

  public void setFileurl(String fileurl) {
    this.fileurl = fileurl == null ? null : fileurl.trim();
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename == null ? null : filename.trim();
  }

  public Double getFileSize() {
    return fileSize;
  }

  public void setFileSize(Double fileSize) {
    this.fileSize = fileSize;
  }
}
