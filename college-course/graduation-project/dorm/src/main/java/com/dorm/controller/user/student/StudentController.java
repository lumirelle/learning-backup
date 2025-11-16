package com.dorm.controller.user.student;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.entity.user.teacher.TeacherPO;
import com.dorm.enums.dorm.DormStatus;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.dorm.DormVO;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.AddStudentDTO;
import com.dorm.entity.user.student.QueryStudentDTO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.entity.user.student.StudentVO;
import com.dorm.entity.user.student.UpdateStudentDTO;
import com.dorm.enums.user.UserRoles;
import com.dorm.enums.user.teacher.TeacherType;
import com.dorm.service.dorm.DormService;
import com.dorm.service.user.student.StudentService;
import com.dorm.service.user.UserService;
import com.dorm.service.user.teacher.TeacherService;
import com.dorm.utils.IdListUtils;
import com.dorm.utils.SecurityUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
public class StudentController {

    @Resource
    private StudentService studentService;

    @Resource
    private DormService dormService;

    @Resource
    private UserService userService;
    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private TeacherService teacherService;

    @RequestMapping("/student/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String showStudentListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        QueryStudentDTO queryParams,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        // 使用 PageHelper 的原因是可以提供 PageInfo，方便在前端实现是否有上下页的判断和切换
        // startPage 的分页效果对随后的一个 sql 查询生效
        // 他的原理是通过 ThreadLocal 来保存当前线程的分页参数
        // 通过 MyBatis 的拦截器（Interceptor） 机制，在 SQL 执行前进行拦截。
        List<StudentPO> studentPOList;
        try (Page<StudentPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<StudentPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(queryParams.getNoOrName())) {
                qw.like("no", queryParams.getNoOrName())
                    .or()
                    .like("name", queryParams.getNoOrName());
            }
            studentPOList = studentService.list(qw);
        }

        // 处理 StudentPO -> StudentVO
        // 返回学生数据
        List<StudentVO> students = new ArrayList<>();
        for (StudentPO studentPO : studentPOList) {
            // 设置宿舍 & 个人信息
            DormPO dormPO = dormService.getById(studentPO.getDormId());
            UserPO userPO = userService.getById(studentPO.getUserId());
            // 构造 StudentVO 信息
            StudentVO student = StudentVO.valueOf(studentPO, userPO, dormPO);
            students.add(student);
        }
        PageInfo<StudentVO> pageInfo = new PageInfo<>(students);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("noOrName", queryParams.getNoOrName());

        // FIXME: 额外的学生用户信息和宿舍信息，用来在添加学生时选择
        List<UserVO> users = UserVO.valuesOf(userService.listUnboundStudentUsers());
        model.addAttribute("users", users);
        List<DormVO> dorms = DormVO.valuesOf(dormService.listFreeDorms());
        model.addAttribute("dorms", dorms);

        return "student/list";
    }

    @RequestMapping("/api/student/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String addStudent(
        @ModelAttribute @Validated AddStudentDTO studentDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/student/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 如果是教师，只有学工部可以操作
        if (securityUtils.getCurrentUser().getRole() == UserRoles.TEACHER) {
            QueryWrapper<TeacherPO> qw = new QueryWrapper<>();
            qw.eq("user_id", securityUtils.getCurrentUser().getId());
            TeacherPO teacherPO = teacherService.getOne(qw);
            if (teacherPO == null || teacherPO.getTeacherType() != TeacherType.AFFAIRS) {
                redirectAttributes.addFlashAttribute("msg", "无操作权限！");
                return url;
            }
        }

        // 学号检验
        if (studentService.isNoExist(studentDTO.getNo())) {
            redirectAttributes.addFlashAttribute("msg", "学号已存在");
            return url;
        }
        // 检查新关联的用户是否存在
        UserPO userPO = userService.getById(studentDTO.getUserId());
        if (userPO == null) {
            redirectAttributes.addFlashAttribute("msg", "添加的用户不存在");
            return url;
        }
        // 检查新关联的宿舍是否存在 & 宿舍是否已满
        DormPO newDorm = dormService.getById(studentDTO.getDormId());
        if (newDorm == null) {
            redirectAttributes.addFlashAttribute("msg", "添加的宿舍不存在");
            return url;
        } else if (newDorm.getStatus() != DormStatus.FREE) {
            redirectAttributes.addFlashAttribute("msg", "添加的宿舍已满");
            return url;
        }

        StudentPO studentPO = StudentPO.valueOf(studentDTO);
        studentService.save(studentPO);

        // 更新宿舍状态（已住人数 + 1）
        newDorm.increaseSetting();
        // 如果宿舍已满
        if (Objects.equals(newDorm.getSetting(), newDorm.getPeople())) {
            newDorm.setStatus(DormStatus.FULL);
        }
        dormService.updateById(newDorm);

        return url;
    }

    @RequestMapping("/student/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String showStudentUpdatePage(@PathVariable Integer id, RedirectAttributes redirectAttributes, Model model) {
        String notExistUrl = "redirect:/student/list";

        // 如果是教师，只有学工部可以操作
        if (securityUtils.getCurrentUser().getRole() == UserRoles.TEACHER) {
            QueryWrapper<TeacherPO> qw = new QueryWrapper<>();
            qw.eq("user_id", securityUtils.getCurrentUser().getId());
            TeacherPO teacherPO = teacherService.getOne(qw);
            if (teacherPO == null || teacherPO.getTeacherType() != TeacherType.AFFAIRS) {
                redirectAttributes.addFlashAttribute("msg", "无操作权限！");
                return notExistUrl;
            }
        }

        // 获取学生信息
        StudentPO studentPO = studentService.getById(id);

        if (studentPO == null) {
            redirectAttributes.addFlashAttribute("msg", "学生不存在");
            return notExistUrl;
        }

        // 设置宿舍 & 个人信息
        DormPO dormPO = dormService.getById(studentPO.getDormId());
        UserPO userPO = userService.getById(studentPO.getUserId());
        StudentVO student = StudentVO.valueOf(studentPO, userPO, dormPO);
        model.addAttribute("student", student);

        // FIXME: 额外的学生用户信息和宿舍信息，用来更新学生时选择
        List<UserPO> userPOList = userService.listUnboundStudentUsers();
        // 还要添加当前学生的用户信息
        if (userPO != null) {
            userPOList.add(userPO);
        }
        List<UserVO> users = UserVO.valuesOf(userPOList);
        model.addAttribute("users", users);

        return "student/update";
    }


    @RequestMapping("/api/student/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String updateStudent(
        @ModelAttribute @Validated UpdateStudentDTO studentDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/student/list";
        String errorUrl = "redirect:/student/update/" + studentDTO.getId();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return errorUrl;
        }

        // 如果是教师，只有学工部可以操作
        if (securityUtils.getCurrentUser().getRole() == UserRoles.TEACHER) {
            QueryWrapper<TeacherPO> qw = new QueryWrapper<>();
            qw.eq("user_id", securityUtils.getCurrentUser().getId());
            TeacherPO teacherPO = teacherService.getOne(qw);
            if (teacherPO == null || teacherPO.getTeacherType() != TeacherType.AFFAIRS) {
                redirectAttributes.addFlashAttribute("msg", "无操作权限！");
                return errorUrl;
            }
        }

        // ID 检验
        StudentPO oldStudent = studentService.getById(studentDTO.getId());
        if (oldStudent == null) {
            redirectAttributes.addFlashAttribute("msg", "学生不存在");
            return errorUrl;
        }
        // 检查新关联的用户是否存在
        if (studentDTO.getUserId() != null) {
            UserPO userPO = userService.getById(studentDTO.getUserId());
            if (userPO == null) {
                redirectAttributes.addFlashAttribute("msg", "用户不存在");
                return errorUrl;
            }
        }

        // 更新学生信息
        UpdateWrapper<StudentPO> uw = new UpdateWrapper<>();
        uw.eq("id", studentDTO.getId());
        uw.set("name", studentDTO.getName());
        uw.set("sex", studentDTO.getSex());
        uw.set("age", studentDTO.getAge());
        uw.set("college", studentDTO.getCollege());
        uw.set("major", studentDTO.getMajor());
        uw.set("user_id", studentDTO.getUserId());

        studentService.update(uw);

        return successUrl;
    }

    @RequestMapping("/api/student/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String deleteStudent(@PathVariable Integer id, RedirectAttributes redirectAttributes) {

        // 如果是教师，只有学工部可以操作
        if (securityUtils.getCurrentUser().getRole() == UserRoles.TEACHER) {
            QueryWrapper<TeacherPO> qw = new QueryWrapper<>();
            qw.eq("user_id", securityUtils.getCurrentUser().getId());
            TeacherPO teacherPO = teacherService.getOne(qw);
            if (teacherPO == null || teacherPO.getTeacherType() != TeacherType.AFFAIRS) {
                redirectAttributes.addFlashAttribute("msg", "无操作权限！");
                return "redirect:/student/list";
            }
        }

        StudentPO studentPO = studentService.getById(id);

        if (studentPO == null) {
            return "redirect:/student/list";
        }

        // 如果关联了宿舍，更新宿舍状态
        DormPO dormPO = dormService.getById(studentPO.getDormId());
        if (dormPO != null) {
            dormPO.decreaseSetting();
            dormPO.setStatus(DormStatus.FREE);
            dormService.updateById(dormPO);
        }

        studentService.removeById(id);

        return "redirect:/student/list";
    }

    // NOTE: AJAX 删除
    @ResponseBody
    @RequestMapping("/api/student/batchDelete")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String batchDeleteStudent(@RequestParam String idList) {
        // 如果是教师，只有学工部可以操作
        if (securityUtils.getCurrentUser().getRole() == UserRoles.TEACHER) {
            QueryWrapper<TeacherPO> qw = new QueryWrapper<>();
            qw.eq("user_id", securityUtils.getCurrentUser().getId());
            TeacherPO teacherPO = teacherService.getOne(qw);
            if (teacherPO == null || teacherPO.getTeacherType() != TeacherType.AFFAIRS) {
                return "无操作权限！";
            }
        }

        List<Integer> list = IdListUtils.convertToIntegerList(idList);

        if (studentService.isAnyIdNotExist(list)) {
            return "某个 ID 不存在";
        }

        // 如果关联了宿舍，更新宿舍状态
        List<Integer> dormIdList = studentService.getDormIdsByStudentIds(list);
        for (Integer id : dormIdList) {
            DormPO dormPO = dormService.getById(id);
            if (dormPO != null) {
                dormPO.decreaseSetting();
                dormPO.setStatus(DormStatus.FREE);
                dormService.updateById(dormPO);
            }
        }

        boolean isSuccess = studentService.removeByIds(list);
        if (isSuccess) {
            return "OK";
        } else {
            return "删除失败";
        }
    }

}
