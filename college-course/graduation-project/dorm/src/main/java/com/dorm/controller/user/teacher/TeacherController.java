package com.dorm.controller.user.teacher;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.enums.user.teacher.TeacherType;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.teacher.AddTeacherDTO;
import com.dorm.entity.user.teacher.QueryTeacherDTO;
import com.dorm.entity.user.teacher.TeacherPO;
import com.dorm.entity.user.teacher.TeacherVO;
import com.dorm.entity.user.teacher.UpdateTeacherDTO;
import com.dorm.service.user.UserService;
import com.dorm.utils.IdListUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.dorm.service.user.teacher.TeacherService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
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

@Slf4j
@Controller
public class TeacherController {
    @Resource
    private TeacherService teacherService;

    @Resource
    private UserService userService;

    @RequestMapping("/teacher/list/{teacherType}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showTeacherListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        @PathVariable TeacherType teacherType,
        @ModelAttribute QueryTeacherDTO teacherDTO,
        Model model
    ) {
        //分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        List<TeacherPO> teacherPOList;
        try (Page<TeacherPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<TeacherPO> qw = new QueryWrapper<>();
            qw.eq("teacher_type", teacherType);
            if (Strings.isNotBlank(teacherDTO.getNoOrName())) {
                qw.like("no", teacherDTO.getNoOrName()).or().like("name", teacherDTO.getNoOrName());
            }
            teacherPOList = teacherService.list(qw);
        }

        // 处理 InstructPO -> InstructorVO
        // 返回学生数据
        List<TeacherVO> teachers = new ArrayList<>();
        for (TeacherPO teacherPO : teacherPOList) {
            // 设置个人信息
            UserPO userPO = userService.getById(teacherPO.getUserId());
            // 构造 InstructorVO 信息
            TeacherVO student = TeacherVO.valueOf(teacherPO, userPO);
            teachers.add(student);
        }
        PageInfo<TeacherVO> pageInfo = new PageInfo<>(teachers);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("noOrName", teacherDTO.getNoOrName());

        // 回显教师类型
        model.addAttribute("teacherType", teacherType);

        // FIXME: 额外的用户信息，用来添加教师时选择
        List<UserVO> users = UserVO.valuesOf(userService.listUnboundTeacherUsers());
        model.addAttribute("users", users);

        return "teacher/list";
    }

    @RequestMapping("/api/teacher/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addTeacher(
        @ModelAttribute @Validated AddTeacherDTO teacherDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/teacher/list/" + teacherDTO.getTeacherType();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 工号检验
        if (teacherService.isNoExist(teacherDTO.getNo())) {
            redirectAttributes.addFlashAttribute("msg", "工号已存在");
            return url;
        }
        // 检查新关联的用户是否存在
        UserPO userPO = userService.getById(teacherDTO.getUserId());
        if (userPO == null) {
            redirectAttributes.addFlashAttribute("msg", "用户不存在");
            return url;
        }

        TeacherPO teacherPO = TeacherPO.valueOf(teacherDTO);
        teacherService.save(teacherPO);
        return url;
    }


    @RequestMapping("/teacher/update/{teacherType}/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showTeacherUpdatePage(
        @PathVariable TeacherType teacherType,
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String notExistUrl = "redirect:/teacher/list/" + teacherType;

        // 获取教师信息
        TeacherPO teacherPO = teacherService.getById(id);

        if (teacherPO == null) {
            redirectAttributes.addFlashAttribute("msg", "教师不存在");
            // 默认跳转到辅导员教师列表
            return notExistUrl;
        }

        // 设置个人信息
        UserPO userPO = userService.getById(teacherPO.getUserId());
        TeacherVO teacher = TeacherVO.valueOf(teacherPO, userPO);
        model.addAttribute("teacher", teacher);

        // 回显教师类型
        model.addAttribute("teacherType", teacherType);

        // FIXME: 额外的用户信息，用来更新教师时选择
        List<UserPO> userPOList = userService.listUnboundTeacherUsers();
        // 还要添加当前教师的用户信息
        if (userPO != null) {
            userPOList.add(userPO);
        }
        List<UserVO> users = UserVO.valuesOf(userPOList);
        model.addAttribute("users", users);

        return "teacher/update";
    }

    @RequestMapping("/api/teacher/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateTeacher(
        @ModelAttribute @Validated UpdateTeacherDTO teacherDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/teacher/list/" + teacherDTO.getTeacherType();
        String errorUrl = "redirect:/teacher/update/" + teacherDTO.getTeacherType() + "/" + teacherDTO.getId();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
        }

        // ID 检验
        TeacherPO teacherPO = teacherService.getById(teacherDTO.getId());
        if (teacherPO == null) {
            redirectAttributes.addFlashAttribute("msg", "教师不存在");
            return errorUrl;
        }
        // 检查新关联的用户是否存在
        if (teacherDTO.getUserId() != null) {
            UserPO userPO = userService.getById(teacherDTO.getUserId());
            if (userPO == null) {
                redirectAttributes.addFlashAttribute("msg", "用户不存在");
                return errorUrl;
            }
        }

        // 更新教师信息
        UpdateWrapper<TeacherPO> uw = new UpdateWrapper<>();
        uw.eq("id", teacherDTO.getId());
        uw.set("name", teacherDTO.getName());
        uw.set("sex", teacherDTO.getSex());
        uw.set("age", teacherDTO.getAge());
        uw.set("college", teacherDTO.getCollege());
        uw.set("major", teacherDTO.getMajor());
        uw.set("user_id", teacherDTO.getUserId());
        teacherService.update(uw);

        return successUrl;
    }

    @RequestMapping("/api/teacher/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteInstructor(@PathVariable Integer id) {
        teacherService.removeById(id);
        return "redirect:/teacher/list";
    }

    // NOTE: AJAX 删除
    @ResponseBody
    @RequestMapping("/api/teacher/batchDelete")
    @PreAuthorize("hasRole('ADMIN')")
    public String batchDeleteInstructor(@RequestParam String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);
        boolean isSuccess = teacherService.removeByIds(list);
        if (isSuccess) {
            return "OK";
        } else {
            return "error";
        }
    }
}
