package entity;

import com.alibaba.fastjson2.annotation.JSONField;
import entity.annotation.Column;
import entity.annotation.Table;
import java.sql.Date;

@Table(name = "student")
public class StudentBean implements Bean {

  @JSONField(name = "student_no")
  @Column(name = "student_no", length = 11, minLength = 11, primaryKey = true)
  private String no;

  @JSONField(name = "student_name")
  @Column(name = "student_name", length = 20, notNull = true)
  private String name;

  @JSONField(name = "class_no")
  @Column(name = "class_no", length = 9, minLength = 9, notNull = true)
  private String classNo;

  @JSONField(name = "student_sex")
  @Column(name = "student_sex", type = "boolean", notNull = true)
  private Boolean sex;

  @JSONField(name = "student_birthday")
  @Column(name = "student_birthday", type = "date", notNull = true)
  private Date birthday;

  @JSONField(name = "student_telephone")
  @Column(name = "student_telephone", length = 11, minLength = 11, notNull = true)
  private String telephone;

  @JSONField(name = "student_note")
  @Column(name = "student_note", type = "text")
  private String note;

  public String getNo() {
    return no;
  }

  public void setNo(String no) {
    this.no = no;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClassNo() {
    return classNo;
  }

  public void setClassNo(String classNo) {
    this.classNo = classNo;
  }

  public Boolean getSex() {
    return sex;
  }

  public void setSex(Boolean sex) {
    this.sex = sex;
  }

  public Date getBirthday() {
    return birthday;
  }

  public void setBirthday(Date birthday) {
    this.birthday = birthday;
  }

  public String getTelephone() {
    return telephone;
  }

  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
