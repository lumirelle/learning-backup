package entity;

import com.alibaba.fastjson2.annotation.JSONField;
import entity.annotation.Column;
import entity.annotation.Table;
import java.sql.Date;

@Table(name = "headteacher")
public class HeadteacherBean implements Bean {

  @JSONField(name = "headteacher_no")
  @Column(name = "headteacher_no", length = 10, minLength = 10, primaryKey = true)
  private String no;

  @JSONField(name = "headteacher_name")
  @Column(name = "headteacher_name", length = 20, notNull = true)
  private String name;

  @JSONField(name = "class_no")
  @Column(name = "class_no", length = 9, minLength = 9, notNull = true)
  private String classNo;

  @JSONField(name = "headteacher_sex")
  @Column(name = "headteacher_sex", type = "boolean", notNull = true)
  private Boolean sex;

  @JSONField(name = "headteacher_birthday")
  @Column(name = "headteacher_birthday", type = "date", notNull = true)
  private Date birthday;

  @JSONField(name = "headteacher_telephone")
  @Column(name = "headteacher_telephone", length = 11, minLength = 11, notNull = true)
  private String telephone;

  @JSONField(name = "headteacher_note")
  @Column(name = "headteacher_note", type = "text")
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
