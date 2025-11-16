package com.dorm.controller.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.entity.BarChartColumn;
import com.dorm.entity.dorm.BuildingSettingVO;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.entity.user.student.StudentVO;
import com.dorm.service.dorm.DormService;
import com.dorm.service.user.UserService;
import com.dorm.service.user.student.StudentService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Controller
public class StatisticController {

    @Resource
    private DormService dormService;

    @Resource
    private StudentService studentService;

    @Resource
    private UserService userService;

    @ResponseBody
    @RequestMapping("/api/statistic/setting-per-building")
    @PreAuthorize("!hasAnyRole('SERVICEMAN')")
    public List<BarChartColumn> settingsPerBuilding() {
        // 每栋楼的人数
        List<BuildingSettingVO> peoplePerBuild = dormService.countPeoplePerBuild();

        // 设置到柱状图的列里
        List<BarChartColumn> columns = new ArrayList<>();
        for (BuildingSettingVO peopleNumber : peoplePerBuild) {
            BarChartColumn column = new BarChartColumn();
            //楼栋的名字
            column.setType(peopleNumber.getBuilding());
            column.setMount(Integer.valueOf(peopleNumber.getSetting()));
            columns.add(column);
        }

        return columns;
    }

    @RequestMapping("/statistic/student-in-building/{name}")
    @PreAuthorize("!hasAnyRole('SERVICEMAN')")
    public String showStudentsInBuildingPage(@PathVariable String name, Model model) {
        // 根据楼栋名称筛选
        List<DormPO> dormPOList = dormService.list(new QueryWrapper<DormPO>().eq("building", name));
        List<StudentPO> studentPOList = studentService.list(new QueryWrapper<StudentPO>().in("dorm_id", dormPOList.stream().map(DormPO::getId).toList()));

        // 处理 StudentPO -> StudentVO
        // 返回楼栋学生数据
        List<StudentVO> students = new ArrayList<>();
        for (StudentPO studentPO : studentPOList) {
            // 设置个人信息
            UserPO userPO = userService.getById(studentPO.getUserId());
            // 构造 StudentVO 信息
            StudentVO student = StudentVO.valueOf(studentPO, userPO);
            students.add(student);
        }

        model.addAttribute("students", students);

        return "/statistic/detail";
    }

}
