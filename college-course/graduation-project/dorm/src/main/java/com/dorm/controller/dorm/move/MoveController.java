package com.dorm.controller.dorm.move;


import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.entity.QueryParams;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.dorm.DormVO;
import com.dorm.entity.dorm.move.AddMoveDTO;
import com.dorm.entity.dorm.move.MovePO;
import com.dorm.entity.dorm.move.MoveVO;
import com.dorm.entity.dorm.move.QueryMoveDTO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.entity.user.student.StudentVO;
import com.dorm.entity.user.teacher.TeacherPO;
import com.dorm.enums.dorm.DormStatus;
import com.dorm.enums.dorm.move.MoveStatus;
import com.dorm.enums.dorm.move.MoveType;
import com.dorm.enums.user.UserRoles;
import com.dorm.enums.user.teacher.TeacherType;
import com.dorm.service.dorm.DormService;
import com.dorm.service.dorm.move.MoveService;
import com.dorm.service.user.student.StudentService;
import com.dorm.service.user.teacher.TeacherService;
import com.dorm.utils.IdListUtils;
import com.dorm.utils.SecurityUtils;
import com.dorm.utils.UploadUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
public class MoveController {
    @Resource
    private MoveService moveService;

    @Resource
    private StudentService studentService;

    @Resource
    private DormService dormService;

    @Resource
    private UploadUtils uploadUtils;

    @Resource
    private SecurityUtils securityUtils;

    @Resource
    private TeacherService teacherService;

    @RequestMapping("/move/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public String showMoveListPage(
        @RequestParam(name = "pageNum", defaultValue = "1", required = false) Integer pageNum,
        @RequestParam(name = "pageSize", defaultValue = "15", required = false) Integer pageSize,
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

        List<MovePO> movePOList;
        try (Page<MovePO> ignored = PageHelper.startPage(pageNum, pageSize)) {
            if (Strings.isNotBlank(queryParams.getSearchKey())) {
                movePOList = moveService.listMoveByStudentName(queryParams.getSearchKey());
            } else {
                movePOList = moveService.list();
            }
        }

        // 处理 MovePO -> MoveVO
        // 返回搬迁数据
        List<MoveVO> moves = new ArrayList<>();
        for (MovePO movePO : movePOList) {
            // 用 movePO 里的 studentId，从 student service 获取一个 studentPO
            StudentPO studentPO = studentService.getById(movePO.getStudentId());
            DormPO fromDormPO = dormService.getById(movePO.getFromDormId());
            DormPO toDormPO = dormService.getById(movePO.getToDormId());
            // 构造 MoveVO 信息
            MoveVO move = MoveVO.valueOf(movePO, studentPO, fromDormPO, toDormPO);
            moves.add(move);
        }

        // 如果是学生，筛选出自己的搬迁申请
        UserVO user = securityUtils.getCurrentUser();
        StudentVO userStudent = null;
        if (user.getRole() == UserRoles.STUDENT) {
            StudentPO studentPO = studentService.getOne(new QueryWrapper<>(StudentPO.class).eq(
                "user_id", user.getId()));
            if (studentPO != null) {
                DormPO dormPO = dormService.getById(studentPO.getDormId());
                userStudent = StudentVO.valueOf(studentPO, dormPO);
                moves = moves.stream().filter(i -> i.getStudentId().equals(studentPO.getId())).toList();
            } else {
                moves = new ArrayList<>();
            }
        }

        PageInfo<MoveVO> pageInfo = new PageInfo<>(moves);
        model.addAttribute("pageInfo", pageInfo);

        // 回显查询条件
        model.addAttribute("searchKey", queryParams.getSearchKey());

        // FIXME: 额外的学生信息和宿舍信息，用来在添加搬迁时选择
        if (user.getRole() == UserRoles.ADMIN || user.getRole() == UserRoles.TEACHER) {
            List<StudentVO> students = new ArrayList<>();
            List<StudentPO> studentPOList = studentService.list();
            for (StudentPO studentPO1 : studentPOList) {
                // 获取宿舍信息
                DormPO dormPO = dormService.getById(studentPO1.getDormId());
                StudentVO student = StudentVO.valueOf(studentPO1, dormPO);
                students.add(student);
            }
            //变量重命名，返回给前端
            model.addAttribute("students", students);
        } else if (user.getRole() == UserRoles.STUDENT) {
            model.addAttribute("userStudent", userStudent);
        }

        List<DormVO> dorms = DormVO.valuesOf(dormService.list());
        model.addAttribute("dorms", dorms);

        return "move/list";
    }

    @RequestMapping("/api/move/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String addMove(
        //校验
        @ModelAttribute @Validated AddMoveDTO moveDTO,
        BindingResult bindingResult,
        @RequestParam MultipartFile file,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/move/list";

        // 检验搬迁类型不能为空
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // 检验学生ID是否存在
        StudentPO studentPO = studentService.getById(moveDTO.getStudentId());
        if (studentPO == null) {
            redirectAttributes.addFlashAttribute("msg", "学生 ID 不存在");
            return url;
        }

        // 如果学生有未结束的搬迁记录，则不能再提交新的搬迁申请
        //如果学生有搬迁状态不是通过、拒绝和取消，就不能申请搬迁
        List<MovePO> studentMoves = moveService.list(new QueryWrapper<>(MovePO.class).eq(
            "student_id", moveDTO.getStudentId()));
        if (studentMoves.stream()
            .anyMatch(i -> i.getStatus() != MoveStatus.PASS && i.getStatus() != MoveStatus.REJECT && i.getStatus() != MoveStatus.CANCELLED)) {
            redirectAttributes.addFlashAttribute("msg", "学生仍有未处理的搬迁记录");
            return url;
        }

        // 宿舍 ID 检验（来源和迁往宿舍 ID 是否相同）
        if (Objects.equals(studentPO.getDormId(), moveDTO.getToDormId())) {
            redirectAttributes.addFlashAttribute("msg", "来源和迁往宿舍 ID 不能相同");
            return url;
        }

        // 去往的宿舍存不存在，和是不是满人了
        DormPO toDormPO = dormService.getById(moveDTO.getToDormId());
        if (toDormPO == null) {
            redirectAttributes.addFlashAttribute("msg", "迁往的宿舍不存在");
            return url;
        } else if (toDormPO.getStatus() == DormStatus.FULL) {
            redirectAttributes.addFlashAttribute("msg", "迁往的宿舍已满员");
            return url;
        }

        try {
            MovePO movePO = MovePO.valueOf(moveDTO);
            // 学生信息中的宿舍 ID 就是来源宿舍 ID
            movePO.setFromDormId(studentPO.getDormId());
            // 上传证明文件
            movePO.setProve(uploadUtils.uploadFile(file));
            // 添加搬迁记录时默认设置为待辅导员审核状态，同时设置创建时间
            movePO.setStatus(MoveStatus.WAIT_INSTRUCTOR_AUDIT);
            movePO.setCreateTime(new DateTime());
            moveService.save(movePO);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("msg", "上传文件失败");
            return url;
        }

        return url;
    }

    @RequestMapping("/api/move/agree/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String agreeMove(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/move/list";

        // ID 检验
        MovePO movePO = moveService.getById(id);
        if (movePO == null) {
            redirectAttributes.addFlashAttribute("msg", "搬迁不存在");
            return url;
        }

        // 获取搬迁类型和状态
        MoveType moveType = movePO.getType();
        MoveStatus moveStatus = movePO.getStatus();

        // 获取当前用户
        UserVO user = securityUtils.getCurrentUser();
        UserRoles userRole = user.getRole();

        // 如果是教师用户，则获取教师类型
        TeacherType teacherType = null;
        if (userRole == UserRoles.TEACHER) {
            TeacherPO teacherInfo = teacherService.getOne(new QueryWrapper<>(TeacherPO.class).eq(
                "user_id", user.getId()));

            // 检查教师有没有绑定教师信息
            if (teacherInfo == null) {
                redirectAttributes.addFlashAttribute("msg", "请联系管理员绑定教师信息");
                return url;
            }
            teacherType = teacherInfo.getTeacherType();

            // 检查是否有该类型教师不能操作的数据
            // 1. 辅导员
            if (teacherType == TeacherType.INSTRUCTOR && moveStatus != MoveStatus.WAIT_INSTRUCTOR_AUDIT) {
                redirectAttributes.addFlashAttribute("msg", "只能审核待辅导员审核的搬迁记录");
                return url;
            }
            // 2. 学办
            else if (teacherType == TeacherType.OFFICE && moveStatus != MoveStatus.WAIT_OFFICE_AUDIT) {
                redirectAttributes.addFlashAttribute("msg", "只能审核待学办审核的搬迁记录");
                return url;
            }
            // 3. 学工部
            else if (teacherType == TeacherType.AFFAIRS && moveStatus != MoveStatus.WAIT_AFFAIRS_AUDIT) {
                redirectAttributes.addFlashAttribute("msg", "只能审核待学工部审核的搬迁记录");
                return url;
            }
        }

        // 构建更新搬迁信息条件
        UpdateWrapper<MovePO> uw = new UpdateWrapper<>();
        uw.eq("id", id);
        // 设置同意后的新状态
        // 1. 换宿舍类型搬迁只需要一步审核
        MoveStatus newStatus = null;
        if (moveType == MoveType.CHANGE) {
            newStatus = MoveStatus.PASS;
        }
        // 2. 其他类型需要三步审核
        else {
            // 2.1 管理员直接通过
            if (userRole == UserRoles.ADMIN) {
                newStatus = MoveStatus.PASS;
            }
            // 2.2 教师
            else if (userRole == UserRoles.TEACHER) {
                // 2.2.1 辅导员类型，待学办审核
                if (teacherType == TeacherType.INSTRUCTOR) {
                    newStatus = MoveStatus.WAIT_OFFICE_AUDIT;
                }
                // 2.2.2 学办，待学办审核 -> 待学工部审核
                else if (teacherType == TeacherType.OFFICE) {
                    newStatus = MoveStatus.WAIT_AFFAIRS_AUDIT;
                }
                // 2.2.3 学工部，待学工部审核 -> 通过
                else if (teacherType == TeacherType.AFFAIRS) {
                    newStatus = MoveStatus.PASS;
                }
            }
        }
        uw.set("status", newStatus);
        // 设置更新时间
        uw.set("update_time", new DateTime());
        moveService.update(uw);

        // 如果搬迁状态为通过，则更新宿舍和学生状态
        if (newStatus == MoveStatus.PASS) {
            // 更新来源宿舍状态
            DormPO fromDorm = dormService.getById(movePO.getFromDormId());
            if (fromDorm != null) {
                fromDorm.decreaseSetting();
                fromDorm.setStatus(DormStatus.FREE);
                dormService.updateById(fromDorm);
            }

            // 更新迁往宿舍状态
            DormPO toDorm = dormService.getById(movePO.getToDormId());
            if (toDorm != null) {
                toDorm.increaseSetting();
                if (Objects.equals(toDorm.getSetting(), toDorm.getPeople())) {
                    toDorm.setStatus(DormStatus.FULL);
                }
                dormService.updateById(toDorm);
            }

            // 更新学生状态
            UpdateWrapper<StudentPO> uwStudent = new UpdateWrapper<>();
            uwStudent.eq("id", movePO.getStudentId());
            uwStudent.set("dorm_id", movePO.getToDormId());
            studentService.update(uwStudent);
        }

        return url;
    }

    @RequestMapping("/api/move/reject/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String rejectMove(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        String url = "redirect:/move/list";

        // ID 检验
        MovePO movePO = moveService.getById(id);
        if (movePO == null) {
            redirectAttributes.addFlashAttribute("msg", "搬迁记录不存在");
            return url;
        }

        // 获取搬迁状态
        MoveStatus moveStatus = movePO.getStatus();

        // 获取当前用户
        UserVO user = securityUtils.getCurrentUser();
        UserRoles userRole = user.getRole();

        // 如果是教师用户，则获取教师类型
        if (userRole == UserRoles.TEACHER) {
            TeacherPO teacherInfo = teacherService.getOne(new QueryWrapper<>(TeacherPO.class).eq(
                "user_id", user.getId()));

            // 检查教师有没有绑定教师信息
            if (teacherInfo == null) {
                redirectAttributes.addFlashAttribute("msg", "请联系管理员绑定教师信息");
                return url;
            }
            TeacherType teacherType = teacherInfo.getTeacherType();

            // 检查是否有该类型教师不能操作的数据
            // 1. 辅导员
            if (teacherType == TeacherType.INSTRUCTOR && moveStatus != MoveStatus.WAIT_INSTRUCTOR_AUDIT) {
                redirectAttributes.addFlashAttribute("msg", "只能审核待辅导员审核的搬迁记录");
                return url;
            }
            // 2. 学办
            else if (teacherType == TeacherType.OFFICE && moveStatus != MoveStatus.WAIT_OFFICE_AUDIT) {
                redirectAttributes.addFlashAttribute("msg", "只能审核待学办审核的搬迁记录");
                return url;
            }
            // 3. 学工部
            else if (teacherType == TeacherType.AFFAIRS && moveStatus != MoveStatus.WAIT_AFFAIRS_AUDIT) {
                redirectAttributes.addFlashAttribute("msg", "只能审核待学工部审核的搬迁记录");
                return url;
            }
        }

        // 构建更新搬迁信息条件
        UpdateWrapper<MovePO> uw = new UpdateWrapper<>();
        uw.eq("id", id);
        // 设置拒绝后的新状态
        uw.set("status", MoveStatus.REJECT);
        // 设置更新时间
        uw.set("update_time", new DateTime());
        moveService.update(uw);

        return url;
    }

    @RequestMapping("/api/move/cancel/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String cancelMove(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        String url = "redirect:/move/list";

        // ID 检验
        MovePO movePO = moveService.getById(id);
        if (movePO == null) {
            redirectAttributes.addFlashAttribute("msg", "搬迁记录不存在");
            return url;
        }

        // 获取搬迁状态
        MoveStatus moveStatus = movePO.getStatus();

        // 检查是否可以撤销
        if (moveStatus == MoveStatus.PASS || moveStatus == MoveStatus.REJECT || moveStatus == MoveStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute("msg", "搬迁记录已不可撤销");
            return url;
        }

        // 取消搬迁
        movePO.setStatus(MoveStatus.CANCELLED);
        movePO.setUpdateTime(new DateTime());
        moveService.updateById(movePO);

        return url;
    }

    @ResponseBody
    @RequestMapping("/api/move/batchAgree")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String batchAgree(String idList) {
        // "1,2,3,4" -> [1, 2, 3, 4]
        List<Integer> list = IdListUtils.convertToIntegerList(idList);

        if (moveService.isAnyIdNotExist(list)) {
            return "某个 ID 不存在";
        }

        // 获取搬迁数据列表
        List<MovePO> movePOList = moveService.listByIds(list);

        // 获取当前用户
        UserVO user = securityUtils.getCurrentUser();
        UserRoles userRole = user.getRole();
        // 如果是教师用户，则获取教师类型
        TeacherType teacherType = null;
        if (userRole == UserRoles.TEACHER) {
            TeacherPO teacherInfo = teacherService.getOne(new QueryWrapper<>(TeacherPO.class).eq(
                "user_id", user.getId()));

            // 检查教师有没有绑定教师信息
            if (teacherInfo == null) {
                return "请联系管理员绑定教师信息";
            }
            teacherType = teacherInfo.getTeacherType();

            // 检查是否有该类型教师不能操作的数据
            // 1. 辅导员
            if (teacherType == TeacherType.INSTRUCTOR && movePOList.stream().anyMatch(move -> move.getStatus() != MoveStatus.WAIT_INSTRUCTOR_AUDIT)) {
                return "只能审核待辅导员审核的搬迁记录";
            }
            // 2. 学办
            else if (teacherType == TeacherType.OFFICE && movePOList.stream().anyMatch(move -> move.getStatus() != MoveStatus.WAIT_OFFICE_AUDIT)) {
                return "只能审核待学办审核的搬迁记录";
            }
            // 3. 学工部
            else if (teacherType == TeacherType.AFFAIRS && movePOList.stream().anyMatch(move -> move.getStatus() != MoveStatus.WAIT_AFFAIRS_AUDIT)) {
                return "只能审核待学工部审核的搬迁记录";
            }
        }

        boolean flag = true;
        for (MovePO movePO : movePOList) {
            // 获取搬迁类型和状态
            MoveType moveType = movePO.getType();

            // 构建更新搬迁信息条件
            UpdateWrapper<MovePO> uw = new UpdateWrapper<>();
            uw.eq("id", movePO.getId());
            // 设置同意后的新状态
            // 1. 换宿舍类型搬迁只需要一步审核
            MoveStatus newStatus = null;
            if (moveType == MoveType.CHANGE) {
                newStatus = MoveStatus.PASS;
            }
            // 2. 其他类型需要三步审核
            else {
                // 2.1 管理员直接通过
                if (userRole == UserRoles.ADMIN) {
                    newStatus = MoveStatus.PASS;
                }
                // 2.2 教师
                else if (userRole == UserRoles.TEACHER) {
                    // 2.2.1 辅导员类型，待学办审核
                    if (teacherType == TeacherType.INSTRUCTOR) {
                        newStatus = MoveStatus.WAIT_OFFICE_AUDIT;
                    }
                    // 2.2.2 学办，待学办审核 -> 待学工部审核
                    else if (teacherType == TeacherType.OFFICE) {
                        newStatus = MoveStatus.WAIT_AFFAIRS_AUDIT;
                    }
                    // 2.2.3 学工部，待学工部审核 -> 通过
                    else if (teacherType == TeacherType.AFFAIRS) {
                        newStatus = MoveStatus.PASS;
                    }
                }
            }
            uw.set("status", newStatus);
            // 设置更新时间
            uw.set("update_time", new DateTime());
            flag = flag & moveService.update(uw);

            // 如果搬迁状态为通过，则更新宿舍和学生状态
            if (newStatus == MoveStatus.PASS) {
                // 更新来源宿舍状态
                DormPO fromDorm = dormService.getById(movePO.getFromDormId());
                if (fromDorm != null) {
                    fromDorm.decreaseSetting();
                    fromDorm.setStatus(DormStatus.FREE);
                    dormService.updateById(fromDorm);
                    log.info("学生搬迁存在来源宿舍，ID： {}。宿舍人数减少，当前人数：{}", fromDorm.getId(), fromDorm.getSetting());
                }

                // 更新迁往宿舍状态
                DormPO toDorm = dormService.getById(movePO.getToDormId());
                if (toDorm != null) {
                    toDorm.increaseSetting();
                    if (Objects.equals(toDorm.getSetting(), toDorm.getPeople())) {
                        toDorm.setStatus(DormStatus.FULL);
                    }
                    dormService.updateById(toDorm);
                    log.info("学生搬迁存在去往宿舍，ID： {}。宿舍人数增加，当前人数：{}", toDorm.getId(), toDorm.getSetting());
                }

                // 更新学生状态
                UpdateWrapper<StudentPO> uwStudent = new UpdateWrapper<>();
                uwStudent.eq("id", movePO.getStudentId());
                uwStudent.set("dorm_id", movePO.getToDormId());
                studentService.update(uwStudent);

                StudentPO updateResult = studentService.getById(movePO.getStudentId());
                log.info("学生搬迁成功，更新学生宿舍 ID： {} -> {}", movePO.getFromDormId(), updateResult.getDormId());
            }
        }

        if (!flag) {
            return "批量同意部分失败";
        } else {
            return "OK";
        }
    }

    @ResponseBody
    @RequestMapping("/api/move/batchReject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String batchRejectMove(String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);

        if (moveService.isAnyIdNotExist(list)) {
            return "某个 ID 不存在";
        }

        // 获取搬迁数据列表
        List<MovePO> movePOList = moveService.listByIds(list);

        // 获取当前用户
        UserVO user = securityUtils.getCurrentUser();
        UserRoles userRole = user.getRole();

        // 如果是教师用户，则获取教师类型
        if (userRole == UserRoles.TEACHER) {
            TeacherPO teacherInfo = teacherService.getOne(new QueryWrapper<>(TeacherPO.class).eq(
                "user_id", user.getId()));

            // 检查教师有没有绑定教师信息
            if (teacherInfo == null) {
                return "请联系管理员绑定教师信息";
            }
            TeacherType teacherType = teacherInfo.getTeacherType();

            // 检查是否有该类型教师不能操作的数据
            // 1. 辅导员
            if (teacherType == TeacherType.INSTRUCTOR && movePOList.stream().anyMatch(move -> move.getStatus() != MoveStatus.WAIT_INSTRUCTOR_AUDIT)) {
                return "只能审核待辅导员审核的搬迁记录";
            }
            // 2. 学办
            else if (teacherType == TeacherType.OFFICE && movePOList.stream().anyMatch(move -> move.getStatus() != MoveStatus.WAIT_OFFICE_AUDIT)) {
                return "只能审核待学办审核的搬迁记录";
            }
            // 3. 学工部
            else if (teacherType == TeacherType.AFFAIRS && movePOList.stream().anyMatch(move -> move.getStatus() != MoveStatus.WAIT_AFFAIRS_AUDIT)) {
                return "只能审核待学工部审核的搬迁记录";
            }
        }

        boolean flag = true;
        for (MovePO movePO : movePOList) {
            // 拒绝搬迁
            movePO.setStatus(MoveStatus.REJECT);
            movePO.setUpdateTime(new DateTime());
            flag = flag & moveService.updateById(movePO);
        }

        if (!flag) {
            return "批量拒绝部分失败";
        } else {
            return "OK";
        }
    }

    @ResponseBody
    @RequestMapping("/api/move/batchCancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public String batchCancelMove(String idList) {
        List<Integer> list = IdListUtils.convertToIntegerList(idList);

        if (moveService.isAnyIdNotExist(list)) {
            return "某个 ID 不存在";
        }
        if (moveService.isAnyIdNotCancelable(list)) {
            return "某个 ID 的搬迁记录已不可撤销";
        }

        // 获取搬迁数据列表
        List<MovePO> movePOList = moveService.listByIds(list);

        boolean flag = true;
        for (MovePO movePO : movePOList) {
            // 取消搬迁
            movePO.setStatus(MoveStatus.CANCELLED);
            movePO.setUpdateTime(new DateTime());
            flag = flag & moveService.updateById(movePO);
        }

        if (!flag) {
            return "批量撤销部分失败";
        } else {
            return "OK";
        }
    }

}
