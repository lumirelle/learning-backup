package com.oddy.xml.entity.teacher;

import lombok.Data;

@Data
public class ProgramTeacher implements Teacher {

  @Override
  public void teach() {
    System.out.println("ProgramTeacher teach...");
  }
}
