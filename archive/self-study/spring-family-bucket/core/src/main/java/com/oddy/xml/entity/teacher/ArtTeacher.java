package com.oddy.xml.entity.teacher;

import lombok.Data;

@Data
public class ArtTeacher implements Teacher {

  @Override
  public void teach() {
    System.out.println("ArtTeacher teach...");
  }
}
