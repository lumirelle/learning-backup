package com.dorm.controller.card.telephone;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.entity.card.bandwidth.BandwidthPO;
import com.dorm.entity.card.telephone.AddTelephoneCardDTO;
import com.dorm.entity.card.telephone.QueryTelephoneCardDTO;
import com.dorm.entity.card.telephone.TelephoneCardPO;
import com.dorm.entity.card.telephone.TelephoneCardVO;
import com.dorm.entity.card.telephone.UpdateTelephoneCardDTO;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.dorm.move.MoveVO;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.entity.user.student.StudentVO;
import com.dorm.enums.card.telephone.TelephoneCardStatus;
import com.dorm.enums.user.UserRoles;
import com.dorm.service.card.bandwidth.BandwidthService;
import com.dorm.service.card.telephone.TelephoneCardService;
import com.dorm.service.user.UserService;
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
public class TelephoneCardController {

    @Resource
    private TelephoneCardService telephoneCardService;

    @Resource
    private StudentService studentService;

    @Resource
    private UserService userService;

    @Resource
    private SecurityUtils securityUtils;

    @Autowired
    private BandwidthService bandwidthService;

    @RequestMapping("/telephone-card/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'CARD_MANAGER')")
    public String showTelephoneCardListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        QueryTelephoneCardDTO telephoneCardDTO,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        List<TelephoneCardPO> telephoneCardPOList;
        try (Page<TelephoneCardPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<TelephoneCardPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(telephoneCardDTO.getTelephone())) {
                qw.like("telephone", telephoneCardDTO.getTelephone());
            }
            telephoneCardPOList = telephoneCardService.list(qw);
        }

        // 处理 TelephoneCardPO -> TelephoneCardVO
        // 返回电话卡数据
        List<TelephoneCardVO> telephoneCards = new ArrayList<>();
        for (TelephoneCardPO telephoneCardPO : telephoneCardPOList) {
            // 设置学生信息 & 用户信息
            StudentPO studentPO = studentService.getById(telephoneCardPO.getStudentId());
            UserPO userPO = userService.getById(studentPO.getUserId());
            // 构造 TelephoneCardVO 信息
            TelephoneCardVO telephoneCard = TelephoneCardVO.valueOf(telephoneCardPO, studentPO, userPO);
            telephoneCards.add(telephoneCard);
        }

        // 如果是学生，筛选出自己的
        UserVO userVO = securityUtils.getCurrentUser();
        if (userVO.getRole() == UserRoles.STUDENT) {
            StudentPO studentPO = studentService.getOne(new QueryWrapper<StudentPO>().eq("user_id", userVO.getId()));
            if (studentPO != null) {
                telephoneCards = telephoneCards.stream().filter(i -> i.getStudentId().equals(studentPO.getId())).toList();
            } else {
                telephoneCards = new ArrayList<>();
            }
        }

        PageInfo<TelephoneCardVO> pageInfo = new PageInfo<>(telephoneCards);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("telephone", telephoneCardDTO.getTelephone());

        // FIXME：额外的学生数据，添加时用
        List<StudentVO> students = StudentVO.valuesOf(studentService.list());
        model.addAttribute("students", students);

        return "telephone-card/list";
    }

    @RequestMapping("/api/telephone-card/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'CARD_MANAGER')")
    public String addTelephoneCard(
        @ModelAttribute @Validated AddTelephoneCardDTO telephoneCardDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/telephone-card/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 学生 ID 检验
        StudentPO studentPO = studentService.getById(telephoneCardDTO.getStudentId());
        if (studentPO == null) {
            redirectAttributes.addFlashAttribute("msg", "学生不存在");
            return url;
        }

        TelephoneCardPO telephoneCardPO = TelephoneCardPO.valueOf(telephoneCardDTO);
        telephoneCardPO.setStatus(TelephoneCardStatus.NORMAL);
        telephoneCardPO.setCreateTime(new Date());
        telephoneCardService.save(telephoneCardPO);

        if (telephoneCardDTO.getIsGiveBandwidth()) {
            if (telephoneCardDTO.getBandwidthSpeed() == null) {
                redirectAttributes.addFlashAttribute("msg", "宽带速度不能为空");
                return url;
            }

            BandwidthPO bandwidthPO = new BandwidthPO();
            bandwidthPO.setTelephoneCardId(telephoneCardPO.getId());
            bandwidthPO.setSpeed(telephoneCardDTO.getBandwidthSpeed());
            bandwidthService.save(bandwidthPO);
        }

        return url;
    }

    @RequestMapping("/telephone-card/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CARD_MANAGER')")
    public String showTelephoneCardUpdatePage(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String notExistUrl = "redirect:/telephone-card/list";

        // 获取电话卡信息
        TelephoneCardPO telephoneCardPO = telephoneCardService.getById(id);

        if (telephoneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "电话卡不存在");
            return notExistUrl;
        }

        // 获取学生信息 & 用户信息
        StudentPO studentPO = studentService.getById(telephoneCardPO.getStudentId());
        UserPO userPO = userService.getById(studentPO.getUserId());

        TelephoneCardVO telephoneCardVO = TelephoneCardVO.valueOf(telephoneCardPO, studentPO, userPO);
        model.addAttribute("telephoneCard", telephoneCardVO);

        return "telephone-card/update";
    }


    @RequestMapping("/api/telephone-card/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'CARD_MANAGER')")
    public String updateStudent(
        @ModelAttribute @Validated UpdateTelephoneCardDTO telephoneCardDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/telephone-card/list";
        String errorUrl = "redirect:/telephone-card/update/" + telephoneCardDTO.getId();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return errorUrl;
        }

        // ID 检验
        TelephoneCardPO oldTelephoneCard = telephoneCardService.getById(telephoneCardDTO.getId());
        if (oldTelephoneCard == null) {
            redirectAttributes.addFlashAttribute("msg", "电话卡不存在");
            return errorUrl;
        }

        // 检查是否有宽带关联
        // 获取电话卡的宽带信息
        QueryWrapper<BandwidthPO> qw = new QueryWrapper<>();
        qw.eq("telephone_card_id", oldTelephoneCard.getId());
        long count = bandwidthService.count(qw);
        if (count > 0) {
            redirectAttributes.addFlashAttribute("msg", "电话卡内有宽带，无法注销");
            return errorUrl;
        }

        // 更新电话卡信息
        UpdateWrapper<TelephoneCardPO> uw = new UpdateWrapper<>();
        uw.eq("id", telephoneCardDTO.getId());
        uw.set("status", telephoneCardDTO.getStatus());
        telephoneCardService.update(uw);

        return successUrl;
    }

    @RequestMapping("/api/telephone-card/cancel/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CARD_MANAGER')")
    public String cancelTelephoneCard(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        String url = "redirect:/telephone-card/list";

        TelephoneCardPO telephoneCardPO = telephoneCardService.getById(id);
        if (telephoneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "电话卡不存在");
            return url;
        }
        telephoneCardPO.setStatus(TelephoneCardStatus.CANCEL);

        // 检查是否有宽带关联
        // 获取电话卡的宽带信息
        QueryWrapper<BandwidthPO> qw = new QueryWrapper<>();
        qw.eq("telephone_card_id", id);
        long count = bandwidthService.count(qw);
        if (count > 0) {
            redirectAttributes.addFlashAttribute("msg", "电话卡内有宽带，无法注销");
            return url;
        }

        telephoneCardService.updateById(telephoneCardPO);
        return url;
    }

    // NOTE: AJAX 注销
    @ResponseBody
    @RequestMapping("/api/telephone-card/batchCancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CARD_MANAGER')")
    public String batchCancelTelephoneCard(@RequestParam String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);

        for (Integer id : list) {
            TelephoneCardPO telephoneCardPO = telephoneCardService.getById(id);

            if (telephoneCardPO == null) {
                return "部分电话卡不存在，注销失败";
            }

            // 检查是否有宽带关联
            // 获取电话卡的宽带信息
            QueryWrapper<BandwidthPO> qw = new QueryWrapper<>();
            qw.eq("telephone_card_id", id);
            long count = bandwidthService.count(qw);
            if (count > 0) {
                return "部分电话卡内有宽带，无法注销";
            }

            telephoneCardPO.setStatus(TelephoneCardStatus.CANCEL);
            telephoneCardService.updateById(telephoneCardPO);
        }

        return "OK";
    }

}
