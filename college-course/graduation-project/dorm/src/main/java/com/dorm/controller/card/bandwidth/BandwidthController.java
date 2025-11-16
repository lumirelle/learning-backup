package com.dorm.controller.card.bandwidth;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.entity.card.bandwidth.AddBandwidthDTO;
import com.dorm.entity.card.bandwidth.BandwidthPO;
import com.dorm.entity.card.bandwidth.BandwidthVO;
import com.dorm.entity.card.bandwidth.QueryBandwidthDTO;
import com.dorm.entity.card.bandwidth.UpdateBandwidthDTO;
import com.dorm.entity.card.telephone.TelephoneCardPO;
import com.dorm.entity.card.telephone.TelephoneCardVO;
import com.dorm.enums.card.telephone.TelephoneCardStatus;
import com.dorm.service.card.bandwidth.BandwidthService;
import com.dorm.service.card.telephone.TelephoneCardService;
import com.dorm.utils.IdListUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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

@Controller
public class BandwidthController {

    @Resource
    private BandwidthService bandwidthService;

    @Resource
    private TelephoneCardService telephoneCardService;

    @RequestMapping("/bandwidth/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'CARD_MANAGER')")
    public String showBandwidthListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        @ModelAttribute QueryBandwidthDTO bandwidthDTO,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        List<BandwidthPO> bandwidthPOList;
        try (Page<BandwidthPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            bandwidthPOList = bandwidthService.list();
        }

        // 处理 BandwidthPO -> BandwidthVO
        // 返回宽带数据
        List<BandwidthVO> bandwidths = new ArrayList<>();
        for (BandwidthPO bandwidthPO : bandwidthPOList) {
            // 设置宽带信息
            TelephoneCardPO telephoneCardPO = telephoneCardService.getById(bandwidthPO.getTelephoneCardId());
            // 构造 BandwidthVO 信息
            BandwidthVO telephoneCard = BandwidthVO.valueOf(bandwidthPO, telephoneCardPO);
            bandwidths.add(telephoneCard);
        }

        // 筛选
        if (Strings.isNotEmpty(bandwidthDTO.getTelephone())) {
            bandwidths = bandwidths.stream()
                .filter(i -> i.getTelephone().contains(bandwidthDTO.getTelephone()))
                .toList();
        }

        PageInfo<BandwidthVO> pageInfo = new PageInfo<>(bandwidths);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("telephone", bandwidthDTO.getTelephone());

        // FIXME：额外的宽带数据，添加时用
        List<TelephoneCardVO> telephoneCards = TelephoneCardVO.valuesOf(telephoneCardService.listNotCanceled());
        model.addAttribute("telephoneCards", telephoneCards);

        return "bandwidth/list";
    }

    @RequestMapping("/api/bandwidth/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'CARD_MANAGER')")
    public String addBandwidth(
        @ModelAttribute @Validated AddBandwidthDTO bandwidthDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/bandwidth/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 电话卡 ID 检验
        TelephoneCardPO telephoneCardPO = telephoneCardService.getById(bandwidthDTO.getTelephoneCardId());
        if (telephoneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "电话卡不存在");
            return url;
        }

        // 电话卡注销检查
        if (telephoneCardPO.getStatus() == TelephoneCardStatus.CANCEL) {
            redirectAttributes.addFlashAttribute("msg", "电话卡已注销");
            return url;
        }

        // 电话卡已有宽带
        BandwidthPO oldBandwidth = bandwidthService.getOne(new UpdateWrapper<BandwidthPO>()
            .eq("telephone_card_id", telephoneCardPO.getId()));
        if (oldBandwidth != null) {
            redirectAttributes.addFlashAttribute("msg", "电话卡已有宽带");
            return url;
        }

        BandwidthPO bandwidthPO = BandwidthPO.valueOf(bandwidthDTO);
        bandwidthService.save(bandwidthPO);

        return url;
    }

    @RequestMapping("/bandwidth/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CARD_MANAGER')")
    public String showBandwidthUpdatePage(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String notExistUrl = "redirect:/bandwidth/list";

        // 获取宽带信息
        BandwidthPO bandwidthPO = bandwidthService.getById(id);

        if (bandwidthPO == null) {
            redirectAttributes.addFlashAttribute("msg", "宽带不存在");
            return notExistUrl;
        }

        // 获取电话卡
        TelephoneCardPO telephoneCardPO = telephoneCardService.getById(bandwidthPO.getTelephoneCardId());

        BandwidthVO bandwidthVO = BandwidthVO.valueOf(bandwidthPO, telephoneCardPO);
        model.addAttribute("bandwidth", bandwidthVO);

        return "bandwidth/update";
    }


    @RequestMapping("/api/bandwidth/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'CARD_MANAGER')")
    public String updateBandwidth(
        @ModelAttribute @Validated UpdateBandwidthDTO bandwidthDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/bandwidth/list";
        String errorUrl = "redirect:/bandwidth/update/" + bandwidthDTO.getId();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return errorUrl;
        }

        // ID 检验
        BandwidthPO oldBandwidth = bandwidthService.getById(bandwidthDTO.getId());
        if (oldBandwidth == null) {
            redirectAttributes.addFlashAttribute("msg", "宽带不存在");
            return errorUrl;
        }

        // 更新宽带信息
        UpdateWrapper<BandwidthPO> uw = new UpdateWrapper<>();
        uw.eq("id", bandwidthDTO.getId());
        uw.set("speed", bandwidthDTO.getSpeed());
        bandwidthService.update(uw);

        return successUrl;
    }

    @RequestMapping("/api/bandwidth/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CARD_MANAGER')")
    public String deleteBandwidth(@PathVariable Integer id) {
        bandwidthService.removeById(id);
        return "redirect:/bandwidth/list";
    }

    // NOTE: AJAX 删除
    @ResponseBody
    @RequestMapping("/api/bandwidth/batchDelete")
    @PreAuthorize("hasAnyRole('ADMIN', 'CARD_MANAGER')")
    public String batchDeleteBandwidth(@RequestParam String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);
        if (bandwidthService.removeBatchByIds(list)) {
            return "OK";
        } else {
            return "ERROR";
        }
    }

}
