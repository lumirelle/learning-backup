package com.dorm.controller.dorm.access;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.dorm.access.AccessPO;
import com.dorm.entity.dorm.access.AccessVO;
import com.dorm.entity.dorm.access.AddAccessDTO;
import com.dorm.entity.dorm.access.QueryAccessDTO;
import com.dorm.entity.dorm.access.UpdateAccessDTO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.entity.user.student.StudentVO;
import com.dorm.enums.dorm.access.AccessStatus;
import com.dorm.enums.dorm.access.AccessType;
import com.dorm.enums.user.UserRoles;
import com.dorm.enums.user.teacher.TeacherType;
import com.dorm.service.dorm.DormService;
import com.dorm.service.dorm.access.AccessService;
import com.dorm.service.user.student.StudentService;
import com.dorm.utils.IdListUtils;
import com.dorm.utils.SecurityUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
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
import java.util.Date;
import java.util.List;

@Controller
public class AccessController {

    @Resource
    private AccessService accessService;

    @Resource
    private StudentService studentService;

    @Resource
    private DormService dormService;
    @Autowired
    private SecurityUtils securityUtils;

    @RequestMapping("/access/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'TEACHER', 'SUPERVISOR')")
    public String listAccess(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        QueryAccessDTO accessDTO,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        List<AccessPO> accessPOList;
        try (Page<AccessPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<AccessPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(accessDTO.getReason())) {
                qw.like("reason", accessDTO.getReason());
            }
            accessPOList = accessService.list(qw);
        }

        // 处理 DormPO -> DormVO
        List<AccessVO> accesses = new ArrayList<>();
        for (AccessPO accessPO : accessPOList) {
            StudentPO studentPO = studentService.getById(accessPO.getStudentId());
            DormPO dormPO = dormService.getById(studentPO.getDormId());
            AccessVO accessVO = AccessVO.valueOf(accessPO, studentPO, dormPO);
            accesses.add(accessVO);
        }

        // 如果是学生
        UserVO userVO = securityUtils.getCurrentUser();
        if (userVO.getRole() == UserRoles.STUDENT) {
            StudentPO studentPO = studentService.getOne(new QueryWrapper<>(StudentPO.class)
                .eq("user_id", userVO.getId()));
            accesses = accesses.stream()
                .filter(access -> access.getStudentId().equals(studentPO.getId()))
                .toList();
        }

        PageInfo<AccessVO> pageInfo = new PageInfo<>(accesses);
        model.addAttribute("pageInfo", pageInfo);

        // FIXME: 额外的学生信息，用来在添加时选择
        if (userVO.getRole() == UserRoles.ADMIN || userVO.getRole() == UserRoles.TEACHER) {
            List<StudentVO> students = StudentVO.valuesOf(studentService.list());
            model.addAttribute("students", students);
        } else if (userVO.getRole() == UserRoles.STUDENT) {
            StudentPO studentPO = studentService.getOne(new QueryWrapper<>(StudentPO.class)
                .eq("user_id", userVO.getId()));
            StudentVO studentVO = StudentVO.valueOf(studentPO);
            model.addAttribute("userStudent", studentVO);
        }

        return "access/list";
    }

    @RequestMapping("/api/access/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String addAccess(
        @ModelAttribute @Validated AddAccessDTO accessDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/access/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 校验学生是否存在
        StudentPO studentPO = studentService.getById(accessDTO.getStudentId());
        if (studentPO == null) {
            redirectAttributes.addFlashAttribute("msg", "用户不存在");
            return url;
        }

        // 如果指定了类型是出校，校验出校的目的地是否为空
        if (accessDTO.getType() == AccessType.OUT && Strings.isBlank(accessDTO.getDestination())) {
            redirectAttributes.addFlashAttribute("msg", "出入校类型不能为空");
            return url;
        }
        // 如果指定了类型是入校，校验入校的来源地是否为空
        if (accessDTO.getType() == AccessType.IN && Strings.isBlank(accessDTO.getSource())) {
            redirectAttributes.addFlashAttribute("msg", "出入校类型不能为空");
            return url;
        }

        AccessPO accessPO = AccessPO.valueOf(accessDTO);
        accessPO.setCreateTime(new Date());
        accessPO.setStatus(AccessStatus.WAIT_ADULT);
        accessService.save(accessPO);

        return url;
    }

    @RequestMapping("/access/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String showAccessUpdatePage(@PathVariable Integer id, RedirectAttributes redirectAttributes, Model model) {
        String notExistUrl = "redirect:/access/list";

        // 获取出入信息
        AccessPO accessPO = accessService.getById(id);

        // 如果是教师，则检验是否辅导员
        UserVO user = securityUtils.getCurrentUser();
        if (user.getRole() == UserRoles.TEACHER) {
            if (securityUtils.getTeacherType() != TeacherType.INSTRUCTOR) {
                redirectAttributes.addFlashAttribute("msg", "只有辅导员可以审核");
                return notExistUrl;
            }
        }

        if (accessPO == null) {
            redirectAttributes.addFlashAttribute("msg", "出入记录不存在");
            return notExistUrl;
        }

        StudentPO studentPO = studentService.getById(accessPO.getStudentId());
        DormPO dormPO = dormService.getById(studentPO.getDormId());
        AccessVO access = AccessVO.valueOf(accessPO, studentPO, dormPO);
        model.addAttribute("access", access);

        return "access/update";
    }

    @RequestMapping("/api/access/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String updateAccess(
        @ModelAttribute @Validated UpdateAccessDTO updateAccessDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/access/list";
        String errorUrl = "redirect:/access/update/" + updateAccessDTO.getId();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return errorUrl;
        }

        // ID 检验
        AccessPO oldAccess = accessService.getById(updateAccessDTO.getId());
        if (oldAccess == null) {
            redirectAttributes.addFlashAttribute("msg", "出入记录不存在");
            return errorUrl;
        }

        // 学生 ID 检验
        StudentPO studentPO = studentService.getById(oldAccess.getStudentId());
        if (studentPO == null) {
            redirectAttributes.addFlashAttribute("msg", "学生 ID 不存在");
            return errorUrl;
        }

        // 如果是教师，则检验是否辅导员
        UserVO user = securityUtils.getCurrentUser();
        if (user.getRole() == UserRoles.TEACHER) {
            if (securityUtils.getTeacherType() != TeacherType.INSTRUCTOR) {
                redirectAttributes.addFlashAttribute("msg", "只有辅导员可以审核");
                return errorUrl;
            }
        }

        // 更新信息
        UpdateWrapper<AccessPO> uw = new UpdateWrapper<>();
        uw.eq("id", updateAccessDTO.getId());
        uw.set("status", updateAccessDTO.getStatus());
        // 设置更新时间
        uw.set("update_time", new DateTime());
        accessService.update(uw);

        return successUrl;
    }

    @RequestMapping("/api/access/cancel/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String cancelAccess(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        // 获取出入信息
        AccessPO accessPO = accessService.getById(id);

        if (accessPO == null) {
            redirectAttributes.addFlashAttribute("msg", "出入记录不存在");
            return "redirect:/access/list";
        }

        if (accessPO.getStatus() != AccessStatus.WAIT_ADULT) {
            redirectAttributes.addFlashAttribute("msg", "出入记录已不可撤销");
            return "redirect:/access/list";
        }

        accessPO.setStatus(AccessStatus.CANCEL);
        accessPO.setUpdateTime(new DateTime());
        accessService.updateById(accessPO);
        return "redirect:/access/list";
    }

    // NOTE: AJAX 删除
    @ResponseBody
    @RequestMapping("/api/access/batchCancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String batchCancelAccess(@RequestParam String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);

        if (accessService.isAnyIdNotExist(list)) {
            return "某个 ID 不存在";
        }
        if (accessService.isAnyIdNotCancelable(list)) {
            return "某个 ID 的搬迁记录已不可撤销";
        }

        boolean flag = true;
        for (Integer id : list) {
            AccessPO accessPO = accessService.getById(id);
            accessPO.setStatus(AccessStatus.CANCEL);
            accessPO.setUpdateTime(new DateTime());
            flag = flag & accessService.updateById(accessPO);
        }

        if (!flag) {
            return "批量撤销部分失败";
        } else {
            return "OK";
        }
    }

}
