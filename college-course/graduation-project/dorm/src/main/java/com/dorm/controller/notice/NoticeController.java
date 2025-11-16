package com.dorm.controller.notice;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.entity.notice.AddNoticeDTO;
import com.dorm.entity.notice.NoticePO;
import com.dorm.entity.notice.NoticeVO;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.teacher.TeacherPO;
import com.dorm.enums.user.UserRoles;
import com.dorm.enums.user.teacher.TeacherType;
import com.dorm.service.notice.NoticeService;
import com.dorm.service.user.UserService;
import com.dorm.service.user.teacher.TeacherService;
import com.dorm.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
public class NoticeController {
    @Resource
    private NoticeService noticeService;

    @Resource
    private UserService userService;

    @Resource
    private SecurityUtils securityUtils;
    
    @Resource
    private TeacherService teacherService;

    @RequestMapping("/notice/list")
    public String listNotice(Model model) {
        List<NoticePO> noticePOList = noticeService.listByCreateTimeDesc();
        List<NoticeVO> notices = new ArrayList<>();
        for (NoticePO noticePO : noticePOList) {
            UserPO userPO = userService.getById(noticePO.getUserId());
            NoticeVO noticeVO = NoticeVO.valueOf(noticePO, userPO);
            notices.add(noticeVO);
        }
        model.addAttribute("notices", notices);
        return "notice/list";
    }

    @RequestMapping("/api/notice/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String addNotice(
        @ModelAttribute @Validated AddNoticeDTO noticeDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/notice/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 获取当前用户
        UserVO currentUser = securityUtils.getCurrentUser();

        // 如果是教师，判断是不是学办和学工部
        if (currentUser.getRole() == UserRoles.TEACHER) {
            TeacherPO teacherPO = teacherService.getOne(new QueryWrapper<TeacherPO>().eq("user_id", currentUser.getId()));
            if (teacherPO.getTeacherType() != TeacherType.OFFICE && teacherPO.getTeacherType() != TeacherType.AFFAIRS) {
                redirectAttributes.addFlashAttribute("msg", "只有管理员、学办和学工部可以发布公告");
                return url;
            }
        }

        NoticePO noticePO = NoticePO.valueOf(noticeDTO);
        noticePO.setUserId(currentUser.getId());
        noticePO.setCreateTime(new Date());
        noticeService.save(noticePO);

        return url;
    }

    @RequestMapping("/api/notice/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String deleteNotice(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        String url = "redirect:/notice/list";

        NoticePO noticePO = noticeService.getById(id);

        // 获取当前用户
        UserVO currentUser = securityUtils.getCurrentUser();

        // 如果是教师，判断是不是他本人发布的
        if (currentUser.getRole() == UserRoles.TEACHER && !Objects.equals(noticePO.getUserId(), currentUser.getId())) {
            redirectAttributes.addFlashAttribute("msg", "学办和学工部只可以删除自己发布的公告");
            return url;
        }

        noticeService.removeById(id);

        return url;
    }
}
