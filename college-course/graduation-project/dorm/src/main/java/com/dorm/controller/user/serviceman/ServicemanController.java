package com.dorm.controller.user.serviceman;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.serviceman.AddServicemanDTO;
import com.dorm.entity.user.serviceman.QueryServicemanDTO;
import com.dorm.entity.user.serviceman.ServicemanPO;
import com.dorm.entity.user.serviceman.ServicemanVO;
import com.dorm.entity.user.serviceman.UpdateServicemanDTO;
import com.dorm.service.user.UserService;
import com.dorm.service.user.serviceman.ServicemanService;
import com.dorm.utils.IdListUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
public class ServicemanController {

    @Resource
    private ServicemanService servicemanService;

    @Resource
    private UserService userService;

    @RequestMapping("/serviceman/list")
    @PreAuthorize("hasRole('ADMIN')")
    public String showServicemanListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        QueryServicemanDTO queryServicemanDTO,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        List<ServicemanPO> servicemanPOList;
        try (Page<ServicemanPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<ServicemanPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(queryServicemanDTO.getNoOrName())) {
                qw.like("no", queryServicemanDTO.getNoOrName())
                    .or()
                    .like("name", queryServicemanDTO.getNoOrName());
            }
            servicemanPOList = servicemanService.list(qw);
        }

        // 处理 ServicemanPO -> ServicemanVO
        // 返回维修人员数据
        List<ServicemanVO> servicemans = new ArrayList<>();
        for (ServicemanPO servicemanPO : servicemanPOList) {
            // 设置个人信息
            UserPO userPO = userService.getById(servicemanPO.getUserId());
            // 构造 ServicemanVO 信息
            ServicemanVO serviceman = ServicemanVO.valueOf(servicemanPO, userPO);
            servicemans.add(serviceman);
        }
        PageInfo<ServicemanVO> pageInfo = new PageInfo<>(servicemans);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("noOrName", queryServicemanDTO.getNoOrName());

        // FIXME: 额外的用户信息，用来添加维修人员时选择
        List<UserVO> users = UserVO.valuesOf(userService.listUnboundServicemanUsers());
        model.addAttribute("users", users);

        return "serviceman/list";
    }

    @RequestMapping("/api/serviceman/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addServiceman(
        @ModelAttribute @Validated AddServicemanDTO cardManagerDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/serviceman/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 编号检验
        if (servicemanService.isNoExist(cardManagerDTO.getNo())) {
            redirectAttributes.addFlashAttribute("msg", "编号已存在");
            return url;
        }
        // 检查新关联的用户是否存在
        UserPO userPO = userService.getById(cardManagerDTO.getUserId());
        if (userPO == null) {
            redirectAttributes.addFlashAttribute("msg", "添加的用户不存在");
            return url;
        }

        ServicemanPO cardManagerPO = ServicemanPO.valueOf(cardManagerDTO);
        servicemanService.save(cardManagerPO);

        return url;
    }

    @RequestMapping("/serviceman/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showServicemanUpdatePage(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String notExistUrl = "redirect:/serviceman/list";

        // 获取维修人员信息
        ServicemanPO cardManagerPO = servicemanService.getById(id);

        if (cardManagerPO == null) {
            redirectAttributes.addFlashAttribute("msg", "维修人员不存在");
            return notExistUrl;
        }

        // 设置个人信息
        UserPO userPO = userService.getById(cardManagerPO.getUserId());
        ServicemanVO serviceman = ServicemanVO.valueOf(cardManagerPO, userPO);
        model.addAttribute("serviceman", serviceman);

        // FIXME: 额外的用户信息，用来更新维修人员时选择
        List<UserPO> userPOList = userService.listUnboundServicemanUsers();
        // 还要添加当前维修人员的用户信息
        if (userPO != null) {
            userPOList.add(userPO);
        }
        List<UserVO> users = UserVO.valuesOf(userPOList);
        model.addAttribute("users", users);

        return "serviceman/update";
    }


    @RequestMapping("/api/serviceman/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateServiceman(
        @ModelAttribute @Validated UpdateServicemanDTO cardManagerDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/serviceman/list";
        String errorUrl = "redirect:/serviceman/update/" + cardManagerDTO.getId();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return errorUrl;
        }

        // ID 检验
        if (servicemanService.getById(cardManagerDTO.getId()) == null) {
            redirectAttributes.addFlashAttribute("msg", "维修人员不存在");
            return errorUrl;
        }
        // 检查新关联的用户是否存在
        if (cardManagerDTO.getUserId() != null) {
            UserPO userPO = userService.getById(cardManagerDTO.getUserId());
            if (userPO == null) {
                redirectAttributes.addFlashAttribute("msg", "用户不存在");
                return errorUrl;
            }
        }

        // 更新维修人员信息
        UpdateWrapper<ServicemanPO> uw = new UpdateWrapper<>();
        uw.eq("id", cardManagerDTO.getId());
        uw.set("name", cardManagerDTO.getName());
        uw.set("sex", cardManagerDTO.getSex());
        uw.set("age", cardManagerDTO.getAge());
        uw.set("user_id", cardManagerDTO.getUserId());
        servicemanService.update(uw);

        return successUrl;
    }

    @RequestMapping("/api/serviceman/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteServiceman(@PathVariable Integer id) {
        servicemanService.removeById(id);
        return "redirect:/serviceman/list";
    }

    // NOTE: AJAX 删除
    @ResponseBody
    @RequestMapping("/api/serviceman/batchDelete")
    @PreAuthorize("hasRole('ADMIN')")
    public String batchDeleteServiceman(@RequestParam String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);
        boolean isSuccess = servicemanService.removeByIds(list);
        if (isSuccess) {
            return "OK";
        } else {
            return "error";
        }
    }

}
