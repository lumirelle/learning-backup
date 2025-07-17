package com.oddy.demossm.entity;

import lombok.Data;

@Data
public class User {

  private String username;

  private String password;

  public String toSuperString() {
    return super.toString();
  }

}
