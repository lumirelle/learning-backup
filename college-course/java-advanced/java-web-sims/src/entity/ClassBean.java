package entity;

import com.alibaba.fastjson2.annotation.JSONField;
import entity.annotation.Column;
import entity.annotation.Table;

@Table(name = "class")
public class ClassBean implements Bean {

  @JSONField(name = "class_no")
  @Column(name = "class_no", length = 9, minLength = 9, primaryKey = true)
  private String no;

  @JSONField(name = "class_name")
  @Column(name = "class_name", length = 20, notNull = true)
  private String name;

  @JSONField(name = "student_count")
  @Column(name = "student_count", type = "tinyint", unsigned = true, notNull = true)
  private Byte studentCount;

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

  public Byte getStudentCount() {
    return studentCount;
  }

  public void setStudentCount(Byte studentCount) {
    this.studentCount = studentCount;
  }
}
