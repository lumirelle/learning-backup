package com.dorm.controller.dorm;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.entity.dorm.AddDormDTO;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.dorm.DormVO;
import com.dorm.entity.dorm.QueryDormDTO;
import com.dorm.entity.dorm.UpdateDormDTO;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.entity.user.student.StudentVO;
import com.dorm.enums.dorm.DormStatus;
import com.dorm.enums.user.UserRoles;
import com.dorm.service.dorm.DormService;
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
import java.util.List;
import java.util.Objects;

@Controller
public class DormController {

    @Resource
    private DormService dormService;

    @Resource
    private StudentService studentService;

    @Resource
    private UserService userService;
    @Autowired
    private SecurityUtils securityUtils;

    @RequestMapping("/dorm/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'STUDENT')")
    public String showDormListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        QueryDormDTO dormDTO,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }

        try (Page<DormPO> page = PageHelper.startPage(pageNum, pageSize)) {
            // 查询宿舍信息
            QueryWrapper<DormPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(dormDTO.getNoOrBuilding())) {
                qw.like("no", dormDTO.getNoOrBuilding()).or().like("building", dormDTO.getNoOrBuilding());
            }
            List<DormPO> dormPOList = dormService.list(qw);

            // 处理 DormPO -> DormVO
            // 返回宿舍数据
            List<DormVO> dorms = DormVO.valuesOf(dormPOList);

            // 如果是学生，筛选出自己的搬迁申请
            UserVO user = securityUtils.getCurrentUser();
            if (user.getRole() == UserRoles.STUDENT) {
                StudentPO studentPO = studentService.getOne(new QueryWrapper<>(StudentPO.class).eq(
                    "user_id", user.getId()));
                if (studentPO != null) {
                    dorms = dorms.stream().filter(i -> i.getId().equals(studentPO.getDormId())).toList();
                } else {
                    dorms = new ArrayList<>();
                }
            }

            PageInfo<DormVO> pageInfo = new PageInfo<>(dorms);
            pageInfo.setTotal(page.getTotal());
            pageInfo.setPages(page.getPages());
            model.addAttribute("pageInfo", pageInfo);

            // 回显查询条件
            model.addAttribute("noOrBuilding", dormDTO.getNoOrBuilding());
        }

        return "dorm/list";
    }

    @RequestMapping("/dorm/detail/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'STUDENT')")
    public String dormDetail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        String notExistUrl = "redirect:/dorm/list";

        // 检查宿舍是否存在
        DormPO dormPO = dormService.getById(id);
        if (dormPO == null) {
            redirectAttributes.addFlashAttribute("msg", "宿舍不存在");
            return notExistUrl;
        }

        // 获取宿舍的学生信息
        QueryWrapper<StudentPO> qw = new QueryWrapper<>();
        qw.eq("dorm_id", id);
        List<StudentPO> studentPOList = studentService.list(qw);

        // 处理 StudentPO -> StudentVO
        // 返回宿舍学生数据
        List<StudentVO> students = new ArrayList<>();
        for (StudentPO studentPO : studentPOList) {
            // 设置个人信息
            UserPO userPO = userService.getById(studentPO.getUserId());
            // 构造 StudentVO 信息
            StudentVO student = StudentVO.valueOf(studentPO, userPO);
            students.add(student);
        }
        model.addAttribute("students", students);

        // 设置宿舍信息
        DormVO dorm = DormVO.valueOf(dormPO);
        model.addAttribute("dorm", dorm);

        return "dorm/detail";
    }

    @RequestMapping("/api/dorm/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addDorm(
        @ModelAttribute @Validated AddDormDTO dormDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/dorm/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 宿舍号检验
        if (dormService.isDormExist(dormDTO.getBuilding(), dormDTO.getNo())) {
            redirectAttributes.addFlashAttribute("msg", "宿舍号已存在");
            return url;
        }

        DormPO dormPO = DormPO.valueOf(dormDTO);
        // 添加宿舍时默认设置为已住 0 人
        dormPO.setSetting(0);
        dormPO.setStatus(DormStatus.FREE);
        dormService.save(dormPO);

        return url;
    }

    @RequestMapping("/dorm/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showDormUpdatePage(@PathVariable Integer id, RedirectAttributes redirectAttributes, Model model) {
        String notExistUrl = "redirect:/dorm/list";

        // 获取宿舍信息
        DormPO dormPO = dormService.getById(id);

        if (dormPO == null) {
            redirectAttributes.addFlashAttribute("msg", "宿舍不存在");
            return notExistUrl;
        }

        DormVO dorm = DormVO.valueOf(dormPO);
        model.addAttribute("dorm", dorm);

        return "dorm/update";
    }


    @RequestMapping("/api/dorm/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStudent(
        @ModelAttribute @Validated UpdateDormDTO dormDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/dorm/list";
        String errorUrl = "redirect:/dorm/update/" + dormDTO.getId();

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return errorUrl;
        }

        // ID 检验
        DormPO oldDorm = dormService.getById(dormDTO.getId());
        if (oldDorm == null) {
            redirectAttributes.addFlashAttribute("msg", "宿舍不存在");
            return errorUrl;
        }
        // 容量检验
        if (dormDTO.getPeople() < oldDorm.getSetting()) {
            redirectAttributes.addFlashAttribute("msg", "宿舍容量必须大于等于已有人数");
            return errorUrl;
        }

        // 更新宿舍信息
        UpdateWrapper<DormPO> uw = new UpdateWrapper<>();
        uw.eq("id", dormDTO.getId());
        uw.set("building", dormDTO.getBuilding());
        uw.set("people", dormDTO.getPeople());
        // 如果宿舍容量和已有人数相等，则设置为满员状态
        uw.set(Objects.equals(dormDTO.getPeople(), oldDorm.getSetting()), "status", DormStatus.FULL);
        // 如果宿舍容量大于已有人数，则设置为空闲状态
        uw.set(dormDTO.getPeople() > oldDorm.getSetting(), "status", DormStatus.FREE);
        dormService.update(uw);

        return successUrl;
    }

    @RequestMapping("/api/dorm/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteDorm(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        String url = "redirect:/dorm/list";

        // 检查是否有学生关联
        // 获取宿舍的学生信息
        QueryWrapper<StudentPO> qw = new QueryWrapper<>();
        qw.eq("dorm_id", id);
        long count = studentService.count(qw);
        if (count > 0) {
            redirectAttributes.addFlashAttribute("msg", "宿舍内有学生，无法删除");
            return url;
        }

        dormService.removeById(id);
        return url;
    }

    // NOTE: AJAX 删除
    @ResponseBody
    @RequestMapping("/api/dorm/batchDelete")
    @PreAuthorize("hasRole('ADMIN')")
    public String batchDeleteDorm(@RequestParam String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);

        // 检查是否有学生关联
        // 获取宿舍的学生信息
        QueryWrapper<StudentPO> qw = new QueryWrapper<>();
        qw.in("dorm_id", list);
        long count = studentService.count(qw);
        if (count > 0) {
            return "选择的部分宿舍内有学生，无法删除";
        }

        boolean isSuccess = dormService.removeByIds(list);
        if (isSuccess) {
            return "OK";
        } else {
            return "error";
        }
    }

}
