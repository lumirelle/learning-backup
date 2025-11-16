package com.dorm.controller.dorm.ranking;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.entity.QueryParams;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.dorm.DormVO;
import com.dorm.entity.dorm.ranking.AddRankingDTO;
import com.dorm.entity.dorm.ranking.QueryRankingDTO;
import com.dorm.entity.dorm.ranking.RankingPO;
import com.dorm.entity.dorm.ranking.RankingVO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.user.UserRoles;
import com.dorm.service.dorm.DormService;
import com.dorm.service.dorm.ranking.RankingService;
import com.dorm.service.user.student.StudentService;
import com.dorm.utils.SecurityUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class RankingController {

    @Resource
    private DormService dormService;

    @Resource
    private RankingService rankingService;

    @Resource
    private SecurityUtils securityUtils;

    @Resource
    private StudentService studentService;

    @RequestMapping("/ranking/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'SUPERVISOR')")
    public String showDormRankingListPage(
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
        List<RankingPO> rankingPOList;
        try (Page<RankingPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<RankingPO> qw = new QueryWrapper<>();
            qw.orderByDesc("create_time");
            rankingPOList = rankingService.list(qw);
        }

        // 处理 RankingPO -> RankingVO
        // 返回宿舍数据
        List<RankingVO> rankings = new ArrayList<>();
        for (RankingPO rankingPO : rankingPOList) {
            DormPO dormPO = dormService.getById(rankingPO.getDormId());
            RankingVO rankingVO = RankingVO.valueOf(rankingPO, dormPO);
            rankings.add(rankingVO);
        }

        // 筛选
        if (Strings.isNotBlank(queryParams.getSearchKey())) {
            rankings = rankings.stream().filter(i -> i.getDorm().contains(queryParams.getSearchKey())).toList();
        }

        // 如果是学生，筛选出自己的宿舍
        UserVO user = securityUtils.getCurrentUser();
        if (user.getRole() == UserRoles.STUDENT) {
            StudentPO studentPO = studentService.getOne(new QueryWrapper<>(StudentPO.class).eq(
                "user_id",
                user.getId()
            ));
            if (studentPO != null) {
                rankings = rankings.stream().filter(i -> i.getDormId().equals(studentPO.getDormId())).toList();
            } else {
                rankings = new ArrayList<>();
            }
        }

        PageInfo<RankingVO> pageInfo = new PageInfo<>(rankings);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("searchKey", queryParams.getSearchKey());

        // FIXME: 额外提供的宿舍信息，添加时使用
        List<DormVO> dorms = DormVO.valuesOf(dormService.list());
        model.addAttribute("dorms", dorms);

        return "ranking/list";
    }

    @RequestMapping("/api/ranking/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public String addDormRanking(
        @ModelAttribute @Validated AddRankingDTO rankingDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/ranking/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 宿舍 ID 检验
        DormPO dormPO = dormService.getById(rankingDTO.getDormId());
        if (dormPO == null) {
            redirectAttributes.addFlashAttribute("msg", "宿舍不存在");
            return url;
        }


        RankingPO rankingPO = RankingPO.valueOf(rankingDTO);
        rankingPO.setCreateTime(new Date());
        rankingService.save(rankingPO);

        return url;
    }

}
