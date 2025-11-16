package com.dorm.controller.dorm.bill;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.entity.QueryParams;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.dorm.DormVO;
import com.dorm.entity.dorm.bill.AddBillDTO;
import com.dorm.entity.dorm.bill.BillPO;
import com.dorm.entity.dorm.bill.BillVO;
import com.dorm.entity.dorm.bill.QueryBillDTO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.user.UserRoles;
import com.dorm.service.dorm.DormService;
import com.dorm.service.dorm.bill.BillService;
import com.dorm.service.user.student.StudentService;
import com.dorm.utils.IdListUtils;
import com.dorm.utils.SecurityUtils;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BillController {

    @Resource
    private StudentService studentService;

    @Resource
    private SecurityUtils securityUtils;

    @Resource
    private BillService billService;

    @Resource
    private DormService dormService;

    @RequestMapping("/bill/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'SUPERVISOR')")
    public String showBillListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        QueryParams queryParams,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        List<BillPO> billPOList;
        try (Page<BillPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<BillPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(queryParams.getSearchKey())) {
                qw.like("water_no", queryParams.getSearchKey())
                    .or().like("electricity_no", queryParams.getSearchKey());
            }
            billPOList = billService.list(qw);
        }

        // 处理 BillPO -> BillVO
        // 返回生活缴费信息数据
        List<BillVO> bills = new ArrayList<>();
        for (BillPO billPO : billPOList) {
            DormPO dormPO = dormService.getById(billPO.getDormId());
            // 构造 BillVO 信息
            BillVO billVO = BillVO.valueOf(billPO, dormPO);
            bills.add(billVO);
        }

        // 如果是学生，过滤掉非自己的生活缴费信息
        UserVO userVO = securityUtils.getCurrentUser();
        if (userVO.getRole() == UserRoles.STUDENT) {
            StudentPO studentPO = studentService.getOne(new QueryWrapper<StudentPO>().eq("user_id", userVO.getId()));
            bills = bills.stream().filter(i -> i.getDormId().equals(studentPO.getDormId())).toList();
        }

        //把账单转成pageinfo
        PageInfo<BillVO> pageInfo = new PageInfo<>(bills);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("searchKey", queryParams.getSearchKey());

        // FIXME: 额外的宿舍信息，添加时需要
        if (userVO.getRole() == UserRoles.ADMIN) {
            List<DormVO> dorms = DormVO.valuesOf(dormService.list());
            model.addAttribute("dorms", dorms);
        }

        return "bill/list";
    }

    @RequestMapping("/api/bill/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public String addBill(
        @ModelAttribute @Validated AddBillDTO billDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/bill/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 宿舍 ID 检验
        DormPO dormPO = dormService.getById(billDTO.getDormId());
        if (dormPO == null) {
            redirectAttributes.addFlashAttribute("msg", "宿舍不存在");
            return url;
        }

        BillPO billPO = BillPO.valueOf(billDTO);
        // 添加生活缴费信息时默认设置 0 元和正常状态
        billPO.setAmount(BigDecimal.ZERO);
        billService.save(billPO);

        return url;
    }

    @RequestMapping("/bill/recharge/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public String showBillRechargePage(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String notExistUrl = "redirect:/bill/list";

        // 获取生活缴费信息信息
        BillPO billPO = billService.getById(id);

        if (billPO == null) {
            redirectAttributes.addFlashAttribute("msg", "生活缴费信息不存在");
            return notExistUrl;
        }

        // 获取宿舍信息
        DormPO dormPO = dormService.getById(billPO.getDormId());

        BillVO bill = BillVO.valueOf(billPO, dormPO);
        model.addAttribute("bill", bill);

        return "bill/recharge";
    }

    @RequestMapping("/api/bill/recharge")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public String rechargeBill(
        @RequestParam Integer id,
        @RequestParam BigDecimal changeAmount,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/bill/list";

        BillPO allInOneCardPO = billService.getById(id);

        if (allInOneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "生活缴费信息不存在");
            return url;
        }

        // 充值金额检验
        if (changeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("msg", "充值金额必须大于 0");
            return url;
        }

        // 更改生活缴费信息金额
        allInOneCardPO.setAmount(allInOneCardPO.getAmount().add(changeAmount));
        billService.updateById(allInOneCardPO);

        return url;
    }

    @RequestMapping("/bill/consume/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public String showBillConsumePage(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String notExistUrl = "redirect:/bill/list";

        // 获取生活缴费信息信息
        BillPO billPO = billService.getById(id);

        if (billPO == null) {
            redirectAttributes.addFlashAttribute("msg", "生活缴费信息不存在");
            return notExistUrl;
        }

        // 获取宿舍信息
        DormPO dormPO = dormService.getById(billPO.getDormId());

        BillVO bill = BillVO.valueOf(billPO, dormPO);
        model.addAttribute("bill", bill);

        return "bill/consume";
    }

    @RequestMapping("/api/bill/consume")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public String consumeBill(
        @RequestParam Integer id,
        @RequestParam BigDecimal changeAmount,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/bill/list";

        BillPO allInOneCardPO = billService.getById(id);

        if (allInOneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "生活缴费信息不存在");
            return url;
        }

        // 消费金额检验
        if (changeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("msg", "消费金额必须大于 0");
            return url;
        }
        // 余额检验
        if (allInOneCardPO.getAmount().compareTo(changeAmount) < 0) {
            redirectAttributes.addFlashAttribute("msg", "生活缴费信息余额不足");
            return url;
        }

        // 更改生活缴费信息金额
        allInOneCardPO.setAmount(allInOneCardPO.getAmount().subtract(changeAmount));
        billService.updateById(allInOneCardPO);

        return url;
    }

    @RequestMapping("/api/bill/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public String deleteBill(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        String url = "redirect:/bill/list";

        BillPO allInOneCardPO = billService.getById(id);

        if (allInOneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "生活缴费信息不存在");
            return url;
        }

        billService.removeById(allInOneCardPO);

        return url;
    }

    // NOTE: AJAX 注销
    @ResponseBody
    @RequestMapping("/api/bill/batchDelete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public String batchDeleteBill(@RequestParam String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);
        boolean isSuccess = billService.removeBatchByIds(list);
        if (isSuccess) {
            return "OK";
        } else {
            return "部分删除失败";
        }
    }

}
