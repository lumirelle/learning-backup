package com.dorm.utils;

import com.dorm.entity.user.UserVO;
import com.dorm.enums.user.teacher.TeacherType;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SecurityUtils {

    private HttpSession session;

    public UserVO getCurrentUser() {
        if (session == null) {
            return null;
        }
        return (UserVO) session.getAttribute("user");
    }

    public TeacherType getTeacherType() {
        return (TeacherType) session.getAttribute("teacherType");
    }

}
