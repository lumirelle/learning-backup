package com.example.classroomschool.entity.student;

/**
 * 存储基本学生信息，而不是学生用户的信息，不包含密码
 */
public class BeanStudent {

  private Integer id;

  private String account;

  private String phone;

  private String mailbox;

  private String name;

  private String school;

  private String schoolNum;

  private String address;


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
    this.account = account;
  }


  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMailbox() {
    return mailbox;
  }

  public void setMailbox(String mailbox) {
    this.mailbox = mailbox;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSchool() {
    return school;
  }

  public void setSchool(String school) {
    this.school = school;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getSchoolNum() {
    return schoolNum;
  }

  public void setSchoolNum(String schoolNum) {
    this.schoolNum = schoolNum;
  }
}
