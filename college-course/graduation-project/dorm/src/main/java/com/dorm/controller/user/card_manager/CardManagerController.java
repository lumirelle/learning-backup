package com.dorm.controller.user.card_manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.card_manager.AddCardManagerDTO;
import com.dorm.entity.user.card_manager.CardManagerPO;
import com.dorm.entity.user.card_manager.CardManagerVO;
import com.dorm.entity.user.card_manager.QueryCardManagerDTO;
import com.dorm.entity.user.card_manager.UpdateCardManagerDTO;
import com.dorm.service.user.UserService;
import com.dorm.service.user.card_manager.CardManagerService;
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
public class CardManagerController {

    @Resource
    private CardManagerService cardManagerService;

    @Resource
    private UserService userService;

    @RequestMapping("/card-manager/list")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCardManagerListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        QueryCardManagerDTO cardManagerDTO,
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
        List<CardManagerPO> cardManagerPOList;
        try (Page<CardManagerPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<CardManagerPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(cardManagerDTO.getNoOrName())) {
                qw.like("no", cardManagerDTO.getNoOrName())
                    .or().like("name", cardManagerDTO.getNoOrName());
            }
            cardManagerPOList = cardManagerService.list(qw);
        }

        // 处理 CardManagerPO -> CardManagerVO
        // 返回校园卡管理员数据
        List<CardManagerVO> cardManagers = new ArrayList<>();
        for (CardManagerPO cardManagerPO : cardManagerPOList) {
            // 设置个人信息
            UserPO userPO = userService.getById(cardManagerPO.getUserId());
            // 构造 CardManagerVO 信息
            CardManagerVO cardManager = CardManagerVO.valueOf(cardManagerPO, userPO);
            cardManagers.add(cardManager);
        }
        PageInfo<CardManagerVO> pageInfo = new PageInfo<>(cardManagers);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("noOrName", cardManagerDTO.getNoOrName());

        // FIXME: 额外的用户信息，用来添加校园卡管理员时选择
        List<UserVO> users = UserVO.valuesOf(userService.listUnboundCardManagerUsers());
        model.addAttribute("users", users);

        return "card-manager/list";
    }

    @RequestMapping("/api/card-manager/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addCardManager(
        @ModelAttribute @Validated AddCardManagerDTO cardManagerDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/card-manager/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 编号检验
        if (cardManagerService.isNoExist(cardManagerDTO.getNo())) {
            redirectAttributes.addFlashAttribute("msg", "编号已存在");
            return url;
        }
        // 检查新关联的用户是否存在
        UserPO userPO = userService.getById(cardManagerDTO.getUserId());
        if (userPO == null) {
            redirectAttributes.addFlashAttribute("msg", "添加的用户不存在");
            return url;
        }

        CardManagerPO cardManagerPO = CardManagerPO.valueOf(cardManagerDTO);
        cardManagerService.save(cardManagerPO);

        return url;
    }

    @RequestMapping("/card-manager/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCardManagerUpdatePage(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String notExistUrl = "redirect:/card-manager/list";

        // 获取校园卡管理员信息
        CardManagerPO cardManagerPO = cardManagerService.getById(id);

        if (cardManagerPO == null) {
            redirectAttributes.addFlashAttribute("msg", "校园卡管理员不存在");
            return notExistUrl;
        }

        // 设置个人信息
        UserPO userPO = userService.getById(cardManagerPO.getUserId());
        CardManagerVO cardManager = CardManagerVO.valueOf(cardManagerPO, userPO);
        model.addAttribute("cardManager", cardManager);

        // FIXME: 额外的用户信息，用来更新校园卡管理员时选择
        List<UserPO> userPOList = userService.listUnboundCardManagerUsers();
        // 还要添加当前校园卡管理员的用户信息
        if (userPO != null) {
            userPOList.add(userPO);
        }
        List<UserVO> users = UserVO.valuesOf(userPOList);
        model.addAttribute("users", users);

        return "card-manager/update";
    }


    @RequestMapping("/api/card-manager/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCardManager(
        @ModelAttribute @Validated UpdateCardManagerDTO cardManagerDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/card-manager/list";
        String errorUrl = "redirect:/card-manager/update/" + cardManagerDTO.getId();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return errorUrl;
        }

        // ID 检验
        if (cardManagerService.getById(cardManagerDTO.getId()) == null) {
            redirectAttributes.addFlashAttribute("msg", "校园卡管理员不存在");
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

        // 更新校园卡管理员信息
        UpdateWrapper<CardManagerPO> uw = new UpdateWrapper<>();
        uw.eq("id", cardManagerDTO.getId());
        uw.set("name", cardManagerDTO.getName());
        uw.set("sex", cardManagerDTO.getSex());
        uw.set("age", cardManagerDTO.getAge());
        uw.set("user_id", cardManagerDTO.getUserId());
        cardManagerService.update(uw);

        return successUrl;
    }

    @RequestMapping("/api/card-manager/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCardManager(@PathVariable Integer id) {
        cardManagerService.removeById(id);
        return "redirect:/card-manager/list";
    }

    // NOTE: AJAX 删除
    @ResponseBody
    @RequestMapping("/api/card-manager/batchDelete")
    @PreAuthorize("hasRole('ADMIN')")
    public String batchDeleteCardManager(@RequestParam String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);
        boolean isSuccess = cardManagerService.removeByIds(list);
        if (isSuccess) {
            return "OK";
        } else {
            return "error";
        }
    }

}
