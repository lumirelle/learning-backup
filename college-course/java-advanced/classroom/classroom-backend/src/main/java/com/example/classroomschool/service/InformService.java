package com.example.classroomschool.service;


import com.example.classroomschool.entity.inform.Inform;
import com.example.classroomschool.mapper.InformMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class InformService {

  @Resource
  InformMapper informMapper;

  public int AddInform(Inform inform) {
    return informMapper.insert(inform);
  }

  public int updateInform(Inform inform) {
    return informMapper.updateByPrimaryKey(inform);
  }

  public List<Inform> findByCourseId(int course) {
    return informMapper.selectInform(course);
  }


  public int STop(int id, String top, int course) {
    return informMapper.setTheTop(id, top, course);
  }

  public int deleteByInformId(int informId) {
    return informMapper.deleteByPrimaryKey(informId);
  }

  public Inform findById(int InformId) {
    return informMapper.selectByPrimaryKey(InformId);
  }
}
