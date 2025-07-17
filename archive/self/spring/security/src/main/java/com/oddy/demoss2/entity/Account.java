package com.oddy.demoss2.entity;

import lombok.Data;

@Data
public class Account {

  private Integer id;

  private String username;

  private String password;

  private String email;

  private String role;

}
