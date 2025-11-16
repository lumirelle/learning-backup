package com.dorm.controller.card.all_in_one;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.entity.card.all_in_one.AddAllInOneCardDTO;
import com.dorm.entity.card.all_in_one.AllInOneCardPO;
import com.dorm.entity.card.all_in_one.AllInOneCardVO;
import com.dorm.entity.card.all_in_one.QueryAllInOneCardDTO;
import com.dorm.entity.card.all_in_one.bill.AllInOneCardBillPO;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.entity.user.student.StudentVO;
import com.dorm.enums.card.all_in_one.AllInOneCardStatus;
import com.dorm.enums.card.all_in_one.bill.AllInOneCardBillUseCase;
import com.dorm.enums.user.UserRoles;
import com.dorm.service.card.all_in_one.AllInOneCardService;
import com.dorm.service.card.all_in_one.bill.AllInOneCardBillService;
import com.dorm.service.user.UserService;
import com.dorm.service.user.student.StudentService;
import com.dorm.utils.IdListUtils;
import com.dorm.utils.NoGenerateUtils;
import com.dorm.utils.SecurityUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class AllInOneCardController {

    @Resource
    private AllInOneCardService allInOneCardService;

    @Resource
    private StudentService studentService;

    @Resource
    private UserService userService;

    @Resource
    private AllInOneCardBillService allInOneCardBillService;

    @Resource
    private NoGenerateUtils noGenerateUtils;

    @Resource
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SecurityUtils securityUtils;

    @RequestMapping("/all-in-one-card/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'CARD_MANAGER')")
    public String showAllInOneCardListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        QueryAllInOneCardDTO allInOneCardDTO,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        List<AllInOneCardPO> allInOneCardPOList;
        try (Page<AllInOneCardPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<AllInOneCardPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(allInOneCardDTO.getNo())) {
                qw.like("no", allInOneCardDTO.getNo());
            }
            allInOneCardPOList = allInOneCardService.list(qw);
        }

        // 处理 AllInOneCardPO -> AllInOneCardVO
        // 返回一卡通数据
        List<AllInOneCardVO> allInOneCards = new ArrayList<>();
        for (AllInOneCardPO allInOneCardPO : allInOneCardPOList) {
            StudentPO studentPO = studentService.getById(allInOneCardPO.getStudentId());
            UserPO userPO = userService.getById(studentPO.getUserId());
            // 构造 AllInOneCardVO 信息
            AllInOneCardVO allInOneCard = AllInOneCardVO.valueOf(allInOneCardPO, studentPO, userPO);
            allInOneCards.add(allInOneCard);
        }

        // 如果是学生，过滤掉非自己的一卡通
        UserVO userVO = securityUtils.getCurrentUser();
        if (userVO.getRole() == UserRoles.STUDENT) {
            StudentPO studentPO = studentService.getOne(new QueryWrapper<StudentPO>().eq("user_id", userVO.getId()));
            if (studentPO != null) {
                allInOneCards = allInOneCards.stream().filter(i -> i.getStudentId().equals(studentPO.getId())).toList();
            } else {
                allInOneCards = new ArrayList<>();
            }
        }

        PageInfo<AllInOneCardVO> pageInfo = new PageInfo<>(allInOneCards);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("no", allInOneCardDTO.getNo());

        // FIXME: 额外的学生信息，添加时需要
        if (userVO.getRole() == UserRoles.ADMIN) {
            List<StudentVO> students = StudentVO.valuesOf(studentService.list());
            model.addAttribute("students", students);
        }

        return "all-in-one-card/list";
    }

    @RequestMapping("/api/all-in-one-card/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addAllInOneCard(
        @ModelAttribute @Validated AddAllInOneCardDTO allInOneCardDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/all-in-one-card/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 学生 ID 检验
        StudentPO studentPO = studentService.getById(allInOneCardDTO.getStudentId());
        if (studentPO == null) {
            redirectAttributes.addFlashAttribute("msg", "学生不存在");
            return url;
        }

        // 密码检验
        if (!allInOneCardDTO.getPassword().equals(allInOneCardDTO.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("msg", "两次密码不一致");
            return url;
        }

        AllInOneCardPO allInOneCardPO = AllInOneCardPO.valueOf(allInOneCardDTO);
        // 生成唯一的一卡通号
        allInOneCardPO.setNo(noGenerateUtils.generateUniqueNo("CD", 20));
        // 添加一卡通时默认设置 0 元和正常状态
        allInOneCardPO.setAmount(BigDecimal.ZERO);
        allInOneCardPO.setStatus(AllInOneCardStatus.NORMAL);
        // 密码加密
        allInOneCardPO.setPassword(passwordEncoder.encode(allInOneCardDTO.getPassword()));
        allInOneCardService.save(allInOneCardPO);

        return url;
    }

    @RequestMapping("/all-in-one-card/recharge/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showAllInOneCardRechargePage(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String notExistUrl = "redirect:/all-in-one-card/list";

        // 获取一卡通信息
        AllInOneCardPO allInOneCardPO = allInOneCardService.getById(id);

        if (allInOneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "一卡通不存在");
            return notExistUrl;
        }

        // 获取学生信息
        StudentPO studentPO = studentService.getById(allInOneCardPO.getStudentId());
        UserPO userPO = userService.getById(studentPO.getUserId());

        AllInOneCardVO allInOneCard = AllInOneCardVO.valueOf(allInOneCardPO, studentPO, userPO);
        model.addAttribute("allInOneCard", allInOneCard);

        return "all-in-one-card/recharge";
    }

    @RequestMapping("/api/all-in-one-card/recharge")
    @PreAuthorize("hasRole('ADMIN')")
    public String rechargeAllInOneCard(
        @RequestParam Integer id,
        @RequestParam BigDecimal amount,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/all-in-one-card/list";

        AllInOneCardPO allInOneCardPO = allInOneCardService.getById(id);

        if (allInOneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "一卡通不存在");
            return url;
        }

        // 充值金额检验
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("msg", "充值金额必须大于 0");
            return url;
        }

        // 更改一卡通金额
        allInOneCardPO.setAmount(allInOneCardPO.getAmount().add(amount));
        allInOneCardService.updateById(allInOneCardPO);

        // 生成充值记录
        AllInOneCardBillPO allInOneCardBillPO = new AllInOneCardBillPO();
        allInOneCardBillPO.setAllInOneCardId(allInOneCardPO.getId());
        allInOneCardBillPO.setNo(noGenerateUtils.generateUniqueNo("CB", 20));
        allInOneCardBillPO.setChangeAmount(amount);
        allInOneCardBillPO.setUseCase(AllInOneCardBillUseCase.OTHER);
        allInOneCardBillPO.setCreateTime(new Date());
        allInOneCardBillService.save(allInOneCardBillPO);

        return url;
    }

    @RequestMapping("/all-in-one-card/consume/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAllInOneCardConsumePage(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String notExistUrl = "redirect:/all-in-one-card/list";

        // 获取一卡通信息
        AllInOneCardPO allInOneCardPO = allInOneCardService.getById(id);

        if (allInOneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "一卡通不存在");
            return notExistUrl;
        }

        // 获取学生信息
        StudentPO studentPO = studentService.getById(allInOneCardPO.getStudentId());
        UserPO userPO = userService.getById(studentPO.getUserId());

        AllInOneCardVO allInOneCard = AllInOneCardVO.valueOf(allInOneCardPO, studentPO, userPO);
        model.addAttribute("allInOneCard", allInOneCard);

        return "all-in-one-card/consume";
    }

    @RequestMapping("/api/all-in-one-card/consume")
    @PreAuthorize("hasRole('ADMIN')")
    public String consumeAllInOneCard(
        @RequestParam Integer id,
        @RequestParam BigDecimal amount,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/all-in-one-card/list";

        AllInOneCardPO allInOneCardPO = allInOneCardService.getById(id);

        if (allInOneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "一卡通不存在");
            return url;
        }

        // 消费金额检验
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("msg", "消费金额必须大于 0");
            return url;
        }
        // 余额检验
        if (allInOneCardPO.getAmount().compareTo(amount) < 0) {
            redirectAttributes.addFlashAttribute("msg", "一卡通余额不足");
            return url;
        }

        // 更改一卡通金额
        allInOneCardPO.setAmount(allInOneCardPO.getAmount().subtract(amount));
        allInOneCardService.updateById(allInOneCardPO);

        // 生成充值记录
        AllInOneCardBillPO allInOneCardBillPO = new AllInOneCardBillPO();
        allInOneCardBillPO.setAllInOneCardId(allInOneCardPO.getId());
        allInOneCardBillPO.setNo(noGenerateUtils.generateUniqueNo("CB", 20));
        allInOneCardBillPO.setChangeAmount(amount.negate());
        allInOneCardBillPO.setUseCase(AllInOneCardBillUseCase.OTHER);
        allInOneCardBillPO.setCreateTime(new Date());
        allInOneCardBillService.save(allInOneCardBillPO);

        return url;
    }

    @RequestMapping("/api/all-in-one-card/consume-self")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public String consumeAllInOneCardSelf(
        @RequestParam Integer id,
        @RequestParam BigDecimal amount,
        @RequestParam String password,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/all-in-one-card/list";

        AllInOneCardPO allInOneCardPO = allInOneCardService.getById(id);

        if (allInOneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "一卡通不存在");
            return url;
        }

        // 密码检验
        if (!passwordEncoder.matches(password, allInOneCardPO.getPassword())) {
            redirectAttributes.addFlashAttribute("msg", "一卡通密码错误");
            return url;
        }

        // 消费金额检验
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("msg", "消费金额必须大于 0");
            return url;
        }
        // 余额检验
        if (allInOneCardPO.getAmount().compareTo(amount) < 0) {
            redirectAttributes.addFlashAttribute("msg", "一卡通余额不足");
            return url;
        }

        // 更改一卡通金额
        allInOneCardPO.setAmount(allInOneCardPO.getAmount().subtract(amount));
        allInOneCardService.updateById(allInOneCardPO);

        // 生成充值记录
        AllInOneCardBillPO allInOneCardBillPO = new AllInOneCardBillPO();
        allInOneCardBillPO.setAllInOneCardId(allInOneCardPO.getId());
        allInOneCardBillPO.setNo(noGenerateUtils.generateUniqueNo("CB", 20));
        allInOneCardBillPO.setChangeAmount(amount.negate());
        allInOneCardBillPO.setUseCase(AllInOneCardBillUseCase.OTHER);
        allInOneCardBillPO.setCreateTime(new Date());
        allInOneCardBillService.save(allInOneCardBillPO);

        return url;
    }

    @RequestMapping("/api/all-in-one-card/withdraw/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String withdrawAllInOneCard(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        String url = "redirect:/all-in-one-card/list";

        AllInOneCardPO allInOneCardPO = allInOneCardService.getById(id);

        if (allInOneCardPO == null) {
            redirectAttributes.addFlashAttribute("msg", "一卡通不存在");
            return url;
        }

        allInOneCardPO.setStatus(AllInOneCardStatus.WITHDRAW);
        allInOneCardService.updateById(allInOneCardPO);

        return url;
    }

    // NOTE: AJAX 注销
    @ResponseBody
    @RequestMapping("/api/all-in-one-card/batchWithdraw")
    @PreAuthorize("hasRole('ADMIN')")
    public String batchWithdrawAllInOneCard(@RequestParam String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);

        for (Integer id : list) {
            AllInOneCardPO allInOneCardPO = allInOneCardService.getById(id);

            if (allInOneCardPO == null) {
                return "一卡通不存在";
            }

            allInOneCardPO.setStatus(AllInOneCardStatus.WITHDRAW);
            allInOneCardService.updateById(allInOneCardPO);
        }

        return "OK";
    }

}
