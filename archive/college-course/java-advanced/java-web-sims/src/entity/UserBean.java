package entity;

import com.alibaba.fastjson2.annotation.JSONField;
import entity.annotation.Column;
import entity.annotation.Table;

@Table(name = "user")
public class UserBean implements Bean {

  @JSONField(name = "username")
  @Column(name = "username", length = 20, primaryKey = true)
  private String username;

  @JSONField(name = "password")
  @Column(name = "password", length = 20, notNull = true)
  private String password;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
