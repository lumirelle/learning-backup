package com.dorm.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.entity.user.serviceman.ServicemanPO;
import com.dorm.enums.user.UserRoles;
import com.dorm.mapper.user.UserMapper;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.card_manager.CardManagerPO;
import com.dorm.entity.user.supervisor.SupervisorPO;
import com.dorm.entity.user.teacher.TeacherPO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.service.user.card_manager.CardManagerService;
import com.dorm.service.user.serviceman.ServicemanService;
import com.dorm.service.user.supervisor.SupervisorService;
import com.dorm.service.user.teacher.TeacherService;
import com.dorm.service.user.student.StudentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements UserService {

    @Resource
    private StudentService studentService;

    @Resource
    private TeacherService teacherService;

    @Resource
    private SupervisorService supervisorService;

    @Resource
    private CardManagerService cardManagerService;

    @Resource
    private ServicemanService servicemanService;

    @Override
    public List<UserPO> listUnboundStudentUsers() {
        return listUnboundRoleUsers(
            studentService.list().stream().map(StudentPO::getUserId).filter(Objects::nonNull).toList(),
            UserRoles.STUDENT
        );
    }

    @Override
    public List<UserPO> listUnboundTeacherUsers() {
        return listUnboundRoleUsers(
            teacherService.list().stream().map(TeacherPO::getUserId).filter(Objects::nonNull).toList(),
            UserRoles.TEACHER
        );
    }

    @Override
    public List<UserPO> listUnboundSupervisorUsers() {
        return listUnboundRoleUsers(
            supervisorService.list().stream().map(SupervisorPO::getUserId).filter(Objects::nonNull).toList(),
            UserRoles.SUPERVISOR
        );
    }

    @Override
    public List<UserPO> listUnboundCardManagerUsers() {
        return listUnboundRoleUsers(
            cardManagerService.list().stream().map(CardManagerPO::getUserId).filter(Objects::nonNull).toList(),
            UserRoles.CARD_MANAGER
        );
    }

    @Override
    public List<UserPO> listUnboundServicemanUsers() {
        return listUnboundRoleUsers(
            servicemanService.list().stream().map(ServicemanPO::getUserId).filter(Objects::nonNull).toList(),
            UserRoles.SERVICEMAN
        );
    }

    private List<UserPO> listUnboundRoleUsers(List<Integer> boundUserIds, UserRoles role) {
        // 查询未绑定的角色用户
        LambdaQueryWrapper<UserPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPO::getRole, role);
        if (!boundUserIds.isEmpty()) {
            log.info("Bound user IDs: {}", boundUserIds);
            queryWrapper.notIn(UserPO::getId, boundUserIds);
        }

        return list(queryWrapper);
    }

    @Override
    public boolean isUserNameExist(String username) {
        QueryWrapper<UserPO> qw = new QueryWrapper<>();
        qw.eq("username", username);
        return count(qw) > 0;
    }
}
