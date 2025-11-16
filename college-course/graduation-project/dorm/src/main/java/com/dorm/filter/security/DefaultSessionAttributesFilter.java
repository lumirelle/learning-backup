package com.dorm.filter.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.UserVO;
import com.dorm.entity.user.teacher.TeacherPO;
import com.dorm.entity.user.teacher.TeacherVO;
import com.dorm.enums.user.UserRoles;
import com.dorm.enums.user.teacher.TeacherType;
import com.dorm.service.user.UserService;
import com.dorm.service.user.teacher.TeacherService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class DefaultSessionAttributesFilter extends OncePerRequestFilter {

    private final UserService userService;

    private final TeacherService teacherService;

    @Override
    protected void doFilterInternal(
        //从安全上下文中获取当前认证信息
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //检查认证信息不为空且通过
        //如果它的Principal是UserDetail类型，就把他改名字成userDetails
        if (authentication != null && authentication.isAuthenticated() &&
            authentication.getPrincipal() instanceof UserDetails userDetails) {

            //获取当前请求的会话
            HttpSession session = request.getSession();

            if (session.getAttribute("baseUrl") == null) {
                // 设置baseUrl，如http://localhost:8088
                String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                request.getSession().setAttribute("baseUrl", baseUrl);
            }

            if (session.getAttribute("user") == null) {
                log.info("User is null, update it.");

                // 设置用户信息
                QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("username", userDetails.getUsername());
                UserPO userPO = userService.getOne(queryWrapper);
                UserVO user = UserVO.valueOf(userPO);
                request.getSession().setAttribute("user", user);

                // 如果是教师，设置教师类型信息
                if (user.getRole().equals(UserRoles.TEACHER)) {
                    TeacherPO teacherPO = teacherService.getOne(new QueryWrapper<TeacherPO>().eq("user_id", user.getId()));
                    if (teacherPO != null) {
                        request.getSession().setAttribute("teacherType", teacherPO.getTeacherType());
                    } else {
                        request.getSession().setAttribute("teacherType", TeacherType.UNSET);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
