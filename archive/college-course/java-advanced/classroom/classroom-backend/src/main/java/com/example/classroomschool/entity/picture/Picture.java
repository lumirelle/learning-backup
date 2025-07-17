package com.example.classroomschool.entity.picture;

public class Picture {

  private Integer id;

  private String picid;

  private String address;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getPicid() {
    return picid;
  }

  public void setPicid(String picid) {
    this.picid = picid == null ? null : picid.trim();
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address == null ? null : address.trim();
  }
}
