package com.dorm.controller.user;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.entity.user.ProfileUserDTO;
import com.dorm.entity.user.RegisterUserDTO;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.entity.user.student.StudentVO;
import com.dorm.service.dorm.fix.FixService;
import com.dorm.service.dorm.move.MoveService;
import com.dorm.service.user.UserService;
import com.dorm.service.user.student.StudentService;
import com.dorm.service.user.teacher.TeacherService;
import com.dorm.utils.UploadUtils;
// spring3 之后记得，所有 servlet 的东西都要用 jakarta!!
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Controller
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private StudentService studentService;

    @Resource
    private TeacherService teacherService;

    @Resource
    private MoveService moveService;

    @Resource
    private FixService fixService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UploadUtils uploadUtils;

    /**
     * 跳转到注册页面
     *
     * @return register
     */
    @RequestMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    /**
     * 注册方法
     *
     * @return 注册成功：index，注册失败：register
     */
    @RequestMapping("/api/register")
    public String register(
        @ModelAttribute @Validated RegisterUserDTO userDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        String successUrl = "redirect:/login";
        String errorUrl = "redirect:/register";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return errorUrl;
        }

        // 用户名已存在
        if (userService.isUserNameExist(userDTO.getUsername())) {
            redirectAttributes.addFlashAttribute("msg", "用户名已存在");
            return errorUrl;
        }
        // 两次输入密码不一致
        if (!Objects.equals(userDTO.getPassword(), userDTO.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("msg", "两次输入密码不一致");
            return errorUrl;
        }

        // 注册
        UserPO newUser = new UserPO();
        newUser.setUsername(userDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        newUser.setPhone(userDTO.getPhone());
        newUser.setEmail(userDTO.getEmail());
        newUser.setRole(userDTO.getRole());
        newUser.setAvatar("images/avatar/1.png"); // 默认头像
        userService.save(newUser);

        return successUrl;
    }

    /**
     * 跳转到登录页面
     *
     * @return index
     */
    @RequestMapping("/login")
    public String showLoginPage(HttpSession session, Model model) {
        // 如果 session 中来自其他接口的重定向错误信息，则添加到 model 中，显示给前端
        if (session.getAttribute("isRedirectMessage") != null) {
            model.addAttribute("msg", session.getAttribute("msg"));
            session.removeAttribute("msg");
            session.removeAttribute("isRedirectMessage");
        }
        return "index";
    }

    /**
     * 跳转到首页
     *
     * @return home
     */
    @RequestMapping("/home")
    private String showHomePage(Model model) {
        // 首页统计信息
        // 最多显示十个学生
        List<StudentPO> studentPOList = studentService.list(new Page<>(1, 10));
        List<StudentVO> studentList = new ArrayList<>();
        for (StudentPO studentPO : studentPOList) {
            UserPO userPO = userService.getById(studentPO.getUserId());
            StudentVO studentVO = StudentVO.valueOf(studentPO, userPO);
            studentList.add(studentVO);
        }
        model.addAttribute("studentList", studentList);
        model.addAttribute("studentCount", studentList.size());

        // 获取其他统计信息
        Long instructorCount = teacherService.count();
        Long moveCount = moveService.count();
        Long fixCount = fixService.count();
        model.addAttribute("instructorCount", instructorCount);
        model.addAttribute("moveCount", moveCount);
        model.addAttribute("fixCount", fixCount);

        return "home";
    }

    /**
     * 跳转到个人信息页面
     *
     * @return profile
     */
    @RequestMapping("/profile")
    public String showProfilePage() {
        return "profile";
    }

    /**
     * 更新用户信息方法
     *
     * @return redirect:/profile
     */
    @RequestMapping("/api/updateProfile")
    public String updateProfile(
        @ModelAttribute @Validated ProfileUserDTO userDTO,
        BindingResult bindingResult,
        @RequestParam MultipartFile avatar,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        String url = "redirect:/profile";
        String notExistUrl = "redirect:/register";

        // 基本参数检验
        if (bindingResult.hasErrors() && bindingResult.getFieldError() != null) {
            redirectAttributes.addFlashAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            return url;
        }

        // ID 和密码检验
        UserPO oldUser = userService.getById(userDTO.getId());
        // 检查用户是否存在
        if (oldUser == null) {
            redirectAttributes.addFlashAttribute("msg", "用户不存在");
            return notExistUrl;
        }
        // 用户存在则检查密码是否正确
        if (!passwordEncoder.matches(userDTO.getPassword(), oldUser.getPassword())) {
            redirectAttributes.addFlashAttribute("msg", "密码错误");
            return url;
        }
        // 然后检查新密码和原密码是否一致
        if (passwordEncoder.matches(userDTO.getNewPassword(), oldUser.getPassword())) {
            redirectAttributes.addFlashAttribute("msg", "新密码不能和原密码一致");
            return url;
        }

        try {
            // 更新用户信息
            UpdateWrapper<UserPO> uw = new UpdateWrapper<>();
            uw.eq("id", userDTO.getId());
            // 如果新密码不为空，则更新密码
            uw.set(
                Strings.isNotBlank(userDTO.getNewPassword()),
                "password",
                passwordEncoder.encode(userDTO.getNewPassword())
            );
            // 类似
            uw.set("email", userDTO.getEmail());
            uw.set("phone", userDTO.getPhone());
            uw.set(
                avatar != null && !avatar.isEmpty(),
                "avatar",
                uploadUtils.uploadFile(avatar)
            );
            userService.update(uw);

            // 更新 session 中的用户信息
            UserPO newUser = userService.getById(userDTO.getId());
            UserVO user = UserVO.valueOf(newUser);
            session.setAttribute("user", user);

            redirectAttributes.addFlashAttribute("msg", "修改成功！");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("msg", "修改失败！");
            throw new RuntimeException(e);
        }

        return url;
    }
}

