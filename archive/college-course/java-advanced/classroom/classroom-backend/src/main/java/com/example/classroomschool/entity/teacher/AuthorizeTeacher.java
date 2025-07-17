package com.example.classroomschool.entity.teacher;

public class AuthorizeTeacher {

  private Integer id;

  private String account;

  private String password;

  private String phone;

  private String mailbox;

  private String name;

  private String school;

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password == null ? null : password.trim();
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone == null ? null : phone.trim();
  }

  public String getMailbox() {
    return mailbox;
  }

  public void setMailbox(String mailbox) {
    this.mailbox = mailbox == null ? null : mailbox.trim();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name == null ? null : name.trim();
  }

  public String getSchool() {
    return school;
  }

  public void setSchool(String school) {
    this.school = school == null ? null : school.trim();
  }
}
