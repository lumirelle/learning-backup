package com.dorm.controller.dorm.objects;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.dorm.DormVO;
import com.dorm.entity.dorm.objects.AddObjectsDTO;
import com.dorm.entity.dorm.objects.ObjectsPO;
import com.dorm.entity.dorm.objects.ObjectsVO;
import com.dorm.entity.dorm.objects.QueryObjectsDTO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.entity.user.student.StudentVO;
import com.dorm.service.dorm.DormService;
import com.dorm.service.dorm.objects.ObjectsService;
import com.dorm.service.user.student.StudentService;
import com.dorm.utils.NoGenerateUtils;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class ObjectsController {

    @Resource
    private DormService dormService;

    @Resource
    private ObjectsService objectsService;

    @Resource
    private StudentService studentService;

    @Resource
    private NoGenerateUtils noGenerateUtils;

    @RequestMapping("/objects/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public String showDormObjectsListPage(
        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
        QueryObjectsDTO objectsDTO,
        Model model
    ) {
        // 分页
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 15;
        }
        List<ObjectsPO> objectsPOList;
        try (Page<ObjectsPO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            QueryWrapper<ObjectsPO> qw = new QueryWrapper<>();
            if (Strings.isNotBlank(objectsDTO.getDescription())) {
                qw.like("description", objectsDTO.getDescription());
            }
            qw.orderByDesc("create_time");
            objectsPOList = objectsService.list(qw);
        }

        // 处理 ObjectsPO -> ObjectsVO
        // 返回宿舍数据
        List<ObjectsVO> objects = new ArrayList<>();
        for (ObjectsPO objectsPO : objectsPOList) {
            StudentPO studentPO = studentService.getById(objectsPO.getStudentId());
            DormPO dormPO = dormService.getById(studentPO.getDormId());
            ObjectsVO objectsVO = ObjectsVO.valueOf(objectsPO, studentPO, dormPO);
            objects.add(objectsVO);
        }

        PageInfo<ObjectsVO> pageInfo = new PageInfo<>(objects);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("description", objectsDTO.getDescription());

        // FIXME: 额外提供的学生信息，添加时使用
        List<StudentVO> students = StudentVO.valuesOf(studentService.list());
        model.addAttribute("students", students);

        return "objects/list";
    }

    @RequestMapping("/api/objects/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public String addDormObjects(
        @ModelAttribute @Validated AddObjectsDTO objectsDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/objects/list";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 学生 ID 检验
        StudentPO studentPO = studentService.getById(objectsDTO.getStudentId());
        if (studentPO == null) {
            redirectAttributes.addFlashAttribute("msg", "学生不存在");
            return url;
        }

        ObjectsPO objectsPO = ObjectsPO.valueOf(objectsDTO);
        objectsPO.setNo(noGenerateUtils.generateUniqueNo("OB", 20));
        objectsPO.setCreateTime(new Date());
        objectsService.save(objectsPO);

        return url;
    }

}
