package com.dorm.controller.user.supervisor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.supervisor.AddSupervisorDTO;
import com.dorm.entity.user.supervisor.QuerySupervisorDTO;
import com.dorm.entity.user.supervisor.SupervisorVO;
import com.dorm.entity.user.supervisor.UpdateSupervisorDTO;
import com.dorm.service.user.UserService;
import com.dorm.utils.IdListUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.dorm.entity.user.supervisor.SupervisorPO;
import com.dorm.service.dorm.DormService;
import com.dorm.service.user.supervisor.SupervisorServiceImpl;
import jakarta.annotation.Resource;
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
import java.util.Set;

@Controller
public class SupervisorController {
    @Resource
    private SupervisorServiceImpl supervisorService;

    @Resource
    private DormService dormService;

    @Resource
    private UserService userService;

    @RequestMapping("/supervisor/list")
    @PreAuthorize("hasRole('ADMIN')")
    public String showSupervisorListPage(
        @RequestParam(value = "PageNum", defaultValue = "1", required = false) Integer pageNum,
        @RequestParam(value = "PageSize", defaultValue = "15", required = false) Integer pageSize,
        QuerySupervisorDTO supervisorDTO,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        List<SupervisorPO> supervisorPOList;
        try (Page<SupervisorPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<SupervisorPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(supervisorDTO.getNoOrName())) {
                qw.like("no", supervisorDTO.getNoOrName())
                    .or()
                    .like("name", supervisorDTO.getNoOrName());
            }
            supervisorPOList = supervisorService.list(qw);
        }

        // 处理 SupervisorPO -> SupervisorVO
        // 返回宿管数据
        List<SupervisorVO> supervisors = new ArrayList<>();
        for (SupervisorPO supervisorPO : supervisorPOList) {
            // 设置个人信息
            UserPO userPO = userService.getById(supervisorPO.getUserId());
            // 构造 StudentVO 信息
            SupervisorVO supervisor = SupervisorVO.valueOf(supervisorPO, userPO);
            supervisors.add(supervisor);
        }
        PageInfo<SupervisorVO> pageInfo = new PageInfo<>(supervisors);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("noOrName", supervisorDTO.getNoOrName());

        // FIXME: 额外的用户信息和楼栋信息，用来添加时选择
        List<UserVO> users = UserVO.valuesOf(userService.listUnboundSupervisorUsers());
        model.addAttribute("users", users);

        Set<String> buildings = dormService.listUniqueBuildings();
        model.addAttribute("buildings", buildings);

        return "supervisor/list";
    }

    @RequestMapping("/api/supervisor/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addSupervisor(
        @ModelAttribute @Validated AddSupervisorDTO supervisorDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/supervisor/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 宿管编号检验
        if (supervisorService.isNoExist(supervisorDTO.getNo())) {
            redirectAttributes.addFlashAttribute("msg", "编号已存在");
            return url;
        }
        // 检查新关联的用户是否存在
        UserPO userPO = userService.getById(supervisorDTO.getUserId());
        if (userPO == null) {
            redirectAttributes.addFlashAttribute("msg", "添加的用户不存在");
            return url;
        }

        SupervisorPO supervisorPO = SupervisorPO.valueOf(supervisorDTO);
        supervisorService.save(supervisorPO);

        return url;
    }

    @RequestMapping("supervisor/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showSupervisorUpdatePage(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String notExistUrl = "redirect:/supervisor/list";

        // 获取宿管信息
        SupervisorPO supervisorPO = supervisorService.getById(id);

        if (supervisorPO == null) {
            redirectAttributes.addFlashAttribute("msg", "宿管不存在");
            return notExistUrl;
        }

        // 设置个人信息
        UserPO userPO = userService.getById(supervisorPO.getUserId());
        SupervisorVO supervisor = SupervisorVO.valueOf(supervisorPO, userPO);
        model.addAttribute("supervisor", supervisor);

        // FIXME: 额外的用户信息和楼栋信息，用来更新时选择
        List<UserPO> userPoList = userService.listUnboundSupervisorUsers();
        if (userPO != null) {
            userPoList.add(userPO);
        }
        List<UserVO> users = UserVO.valuesOf(userPoList);
        model.addAttribute("users", users);

        Set<String> buildings = dormService.listUniqueBuildings();
        model.addAttribute("buildings", buildings);

        return "supervisor/update";
    }

    @RequestMapping("/api/supervisor/update")
    public String updateSupervisor(
        @ModelAttribute @Validated UpdateSupervisorDTO supervisorDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/supervisor/list";
        String errorUrl = "redirect:/supervisor/update/" + supervisorDTO.getId();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return errorUrl;
        }

        // ID 检验
        if (supervisorService.isNoExist(supervisorDTO.getNo())) {
            redirectAttributes.addFlashAttribute("msg", "学生不存在");
            return errorUrl;
        }
        // 检查新关联的用户是否存在
        if (supervisorDTO.getUserId() != null) {
            UserPO userPO = userService.getById(supervisorDTO.getUserId());
            if (userPO == null) {
                redirectAttributes.addFlashAttribute("msg", "用户不存在");
                return errorUrl;
            }
        }

        UpdateWrapper<SupervisorPO> uw = new UpdateWrapper<>();
        uw.eq("id", supervisorDTO.getId());
        uw.set("name", supervisorDTO.getName());
        uw.set("sex", supervisorDTO.getSex());
        uw.set("age", supervisorDTO.getAge());
        uw.set("building", supervisorDTO.getBuilding());
        uw.set("user_id", supervisorDTO.getUserId());
        supervisorService.update(uw);

        return successUrl;

    }

    @RequestMapping("/api/supervisor/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSupervisor(@PathVariable Integer id) {
        supervisorService.removeById(id);
        return "redirect:/supervisor/list";
    }

    // NOTE: AJAX 删除
    @ResponseBody
    @RequestMapping("/api/supervisor/batchDelete")
    @PreAuthorize("hasRole('ADMIN')")
    public String batchDeleteSupervisor(String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);
        boolean isSuccess = supervisorService.removeByIds(list);
        if (isSuccess) {
            return "OK";
        } else {
            return "error";
        }
    }
}
