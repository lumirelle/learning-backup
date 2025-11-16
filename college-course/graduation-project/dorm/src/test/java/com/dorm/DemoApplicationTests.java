package com.dorm;

import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.entity.user.student.StudentVO;
import com.dorm.service.dorm.DormService;
import com.dorm.service.user.UserService;
import com.dorm.service.user.student.StudentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@SpringBootTest(classes = DemoApplication.class)
class DemoApplicationTests {

    @Resource
    private StudentService studentService;

    @Resource
    private UserService userService;

    @Resource
    private DormService dormService;

    @Configuration
    static class TestConfig {
    }

//    @Test
//    void testBeanCovert() {
//        List<StudentPO> studentPOList = studentService.list();
//
//        if (studentPOList.isEmpty()) {
//            log.warn("No student found");
//            return;
//        }
//
//        StudentPO testStudentPO = studentPOList.getFirst();
//        UserPO testUserPO = userService.getById(testStudentPO.getUserId());
//        DormPO testDormPO = dormService.getById(testStudentPO.getDormId());
//
//        StudentVO studentVO = StudentVO.valueOf(testStudentPO, testUserPO, testDormPO);
//
//        log.info("StudentVO: {}", studentVO);
//    }

}
