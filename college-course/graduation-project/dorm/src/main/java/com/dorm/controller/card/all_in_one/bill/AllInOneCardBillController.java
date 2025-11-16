package com.dorm.controller.card.all_in_one.bill;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.entity.card.all_in_one.AllInOneCardPO;
import com.dorm.entity.card.all_in_one.bill.AllInOneCardBillPO;
import com.dorm.entity.card.all_in_one.bill.AllInOneCardBillVO;
import com.dorm.entity.card.all_in_one.bill.QueryAllInOneCardBillDTO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.user.UserRoles;
import com.dorm.service.card.all_in_one.AllInOneCardService;
import com.dorm.service.card.all_in_one.bill.AllInOneCardBillService;
import com.dorm.service.user.student.StudentService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AllInOneCardBillController {

    @Resource
    private AllInOneCardBillService allInOneCardBillService;

    @Resource
    private StudentService studentService;

    @Resource
    private AllInOneCardService allInOneCardService;
    @Autowired
    private SecurityUtils securityUtils;

    @RequestMapping("/all-in-one-card-bill/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'CARD_MANAGER')")
    public String showAllInOneCardBillListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        QueryAllInOneCardBillDTO allInOneCardBillDTO,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        List<AllInOneCardBillPO> allInOneCardBillPOList;
        try (Page<AllInOneCardBillPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<AllInOneCardBillPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(allInOneCardBillDTO.getNo())) {
                qw.like("no", allInOneCardBillDTO.getNo());
            }
            allInOneCardBillPOList = allInOneCardBillService.listByCreateTimeDesc(qw);
        }

        // 处理 AllInOneCardBillPO -> AllInOneCardBillVO
        // 返回一卡通数据
        List<AllInOneCardBillVO> allInOneCardBills = new ArrayList<>();
        for (AllInOneCardBillPO allInOneCardBillPO : allInOneCardBillPOList) {
            AllInOneCardPO allInOneCardPO = allInOneCardService.getById(allInOneCardBillPO.getAllInOneCardId());
            StudentPO studentPO = studentService.getById(allInOneCardPO.getStudentId());
            // 构造 AllInOneCardBillVO 信息
            AllInOneCardBillVO allInOneCardBill = AllInOneCardBillVO.valueOf(
                allInOneCardBillPO,
                allInOneCardPO,
                studentPO
            );
            allInOneCardBills.add(allInOneCardBill);
        }

        // 如果是学生，过滤掉非自己的数据
        UserVO userVO = securityUtils.getCurrentUser();
        if (userVO.getRole() == UserRoles.STUDENT) {
            // 获取学生 ID
            StudentPO studentPO = studentService.getOne(new QueryWrapper<>(StudentPO.class).eq("user_id", userVO.getId()));
            // 获取学生的一卡通 ID 列表
            List<AllInOneCardPO> allInOneCardPOS = allInOneCardService.list(
                new QueryWrapper<AllInOneCardPO>()
                    .eq("student_id", studentPO.getId())
            );
            List<Integer> allInOneCardIds = allInOneCardPOS.stream().map(AllInOneCardPO::getId).toList();
            // 过滤掉非自己的数据
            allInOneCardBills = allInOneCardBills.stream().filter(i -> allInOneCardIds.contains(i.getId())).toList();
        }

        PageInfo<AllInOneCardBillVO> pageInfo = new PageInfo<>(allInOneCardBills);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("no", allInOneCardBillDTO.getNo());

        return "all-in-one-card-bill/list";
    }

}
