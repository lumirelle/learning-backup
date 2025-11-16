package com.dorm.controller.dorm.fix;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.dorm.DormVO;
import com.dorm.entity.dorm.fix.AddFixDTO;
import com.dorm.entity.dorm.fix.FixPO;
import com.dorm.entity.dorm.fix.FixVO;
import com.dorm.entity.QueryParams;
import com.dorm.entity.dorm.fix.UpdateFixDTO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.dorm.fix.FixStatus;
import com.dorm.enums.user.UserRoles;
import com.dorm.service.dorm.DormService;
import com.dorm.service.dorm.fix.FixService;
import com.dorm.service.user.student.StudentService;
import com.dorm.utils.IdListUtils;
import com.dorm.utils.SecurityUtils;
import com.dorm.utils.UploadUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FixController {
    @Resource
    private FixService fixService;

    @Resource
    private DormService dormService;

    @Resource
    private UploadUtils uploadUtils;

    @Resource
    private SecurityUtils securityUtils;

    @Resource
    private StudentService studentService;

    @RequestMapping("/fix/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'SERVICEMAN', 'SUPERVISOR')")
    public String showFixListPage(
        @RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
        @RequestParam(value = "pageSize", defaultValue = "15", required = false) Integer pageSize,
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
        List<FixPO> fixPOList;
        try (Page<FixPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<FixPO> qw = new QueryWrapper<>();
            fixPOList = fixService.list(qw);
        }

        // 处理 FixPO -> FixVO
        // 返回报修数据
        List<FixVO> fixes = new ArrayList<>();
        for (FixPO fixPO : fixPOList) {
            // 设置报修信息
            DormPO dormPO = dormService.getById(fixPO.getDormId());
            // 构造 FixVO 信息
            FixVO fix = FixVO.valueOf(fixPO, dormPO);
            fixes.add(fix);
        }

        // 筛选
        if (Strings.isNotBlank(queryParams.getSearchKey())) {
            fixes = fixes.stream().filter(i ->
                i.getDorm().contains(queryParams.getSearchKey())
                    || i.getDescription().contains(queryParams.getSearchKey())
            ).toList();
        }

        UserVO user = securityUtils.getCurrentUser();
        DormPO userDorm = null;
        // 如果是学生，筛选出自己宿舍的报修记录
        if (user.getRole() == UserRoles.STUDENT) {
            StudentPO studentPO = studentService.getOne(new QueryWrapper<>(StudentPO.class).eq(
                "user_id",
                user.getId()
            ));
            if (studentPO != null) {
                userDorm = dormService.getById(studentPO.getDormId());
                fixes = fixes.stream().filter(i -> i.getDormId().equals(studentPO.getDormId())).toList();
            } else {
                fixes = new ArrayList<>();
            }
        }

        PageInfo<FixVO> pageInfo = new PageInfo<>(fixes);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("searchKey", queryParams.getSearchKey());

        // FIXME: 额外的宿舍信息，用来在添加保修时使用
        if (user.getRole() == UserRoles.ADMIN || user.getRole() == UserRoles.SERVICEMAN) {
            List<DormVO> dorms = DormVO.valuesOf(dormService.list());
            model.addAttribute("dorms", dorms);
        } else if (user.getRole() == UserRoles.STUDENT) {
            model.addAttribute("userDorm", userDorm);
        }

        return "fix/list";
    }

    @RequestMapping("/api/fix/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String addFix(
        @ModelAttribute @Validated AddFixDTO fixDTO,
        BindingResult bindingResult,
        @RequestParam MultipartFile file,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/fix/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        try {
            FixPO fixPO = FixPO.valueOf(fixDTO);
            // 上传图片
            fixPO.setImage(uploadUtils.uploadFile(file));
            // 添加报修记录时默认设置为待处理状态，同时设置创建时间
            fixPO.setStatus(FixStatus.WAIT_FOR_RECEIVE);
            fixPO.setCreateTime(new DateTime());
            fixService.save(fixPO);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("msg", "上传图片失败");
            return url;
        }

        return url;
    }

    @RequestMapping("/fix/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICEMAN')")
    public String showFixUpdatePage(@PathVariable Integer id, RedirectAttributes redirectAttributes, Model model) {
        String notExistUrl = "redirect:/fix/list";

        // 获取报修信息
        FixPO fixPO = fixService.getById(id);

        if (fixPO == null) {
            redirectAttributes.addFlashAttribute("msg", "报修记录不存在");
            return notExistUrl;
        }

        // 获取宿舍信息
        DormPO dormPO = dormService.getById(fixPO.getDormId());
        FixVO fix = FixVO.valueOf(fixPO, dormPO);
        model.addAttribute("fix", fix);

        return "fix/update";
    }

    @RequestMapping("/api/fix/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICEMAN')")
    public String updateFix(
        @ModelAttribute @Validated UpdateFixDTO fixDTO,
        BindingResult bindingResult,
        @RequestParam(required = false) MultipartFile file,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/fix/list";
        String errorUrl = "redirect:/fix/update/" + fixDTO.getId();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return errorUrl;
        }

        // ID 检验
        FixPO oldFix = fixService.getById(fixDTO.getId());
        if (oldFix == null) {
            redirectAttributes.addFlashAttribute("msg", "报修不存在");
            return errorUrl;
        }

        try {
            // 更新报修信息
            UpdateWrapper<FixPO> uw = new UpdateWrapper<>();
            uw.eq("id", fixDTO.getId());
            uw.set("description", fixDTO.getDescription());
            if (securityUtils.getCurrentUser().getRole() == UserRoles.STUDENT) {
                uw.set("image", uploadUtils.uploadFile(file));
            }
            uw.set("status", fixDTO.getStatus());
            // 设置更新时间
            uw.set("update_time", new DateTime());
            fixService.update(uw);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("msg", "上传图片失败");
            return errorUrl;
        }

        return successUrl;
    }

    @RequestMapping("/api/fix/cancel/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String cancelFix(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        FixPO fixPO = fixService.getById(id);

        if (fixPO == null) {
            redirectAttributes.addFlashAttribute("msg", "报修记录不存在");
            return "redirect:/fix/list";
        }
        if (fixPO.getStatus() != FixStatus.WAIT_FOR_RECEIVE) {
            redirectAttributes.addFlashAttribute("msg", "报修记录已不可撤销");
            return "redirect:/fix/list";
        }

        // 取消报修
        fixPO.setStatus(FixStatus.CANCELLED);
        fixPO.setUpdateTime(new DateTime());
        fixService.updateById(fixPO);

        return "redirect:/fix/list";
    }

    @ResponseBody
    @RequestMapping("/api/fix/batchCancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String batchCancelFix(String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);

        if (fixService.isAnyIdNotExist(list)) {
            return "某个 ID 不存在";
        }
        if (fixService.isAnyIdNotCancelable(list)) {
            return "某个 ID 的报修记录已不可撤销";
        }

        boolean flag = true;
        for (Integer id : list) {
            // 取消报修
            FixPO fixPO = fixService.getById(id);
            fixPO.setStatus(FixStatus.CANCELLED);
            fixPO.setUpdateTime(new DateTime());
            flag = flag & fixService.updateById(fixPO);
        }

        if (!flag) {
            return "批量撤销失败";
        } else {
            return "OK";
        }
    }
}
