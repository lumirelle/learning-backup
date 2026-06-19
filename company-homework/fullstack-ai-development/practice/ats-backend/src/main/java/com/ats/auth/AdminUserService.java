package com.ats.auth;

import com.ats.auth.dto.BatchCreateItemVO;
import com.ats.auth.dto.BatchCreateUsersReq;
import com.ats.auth.dto.BatchCreateUsersVO;
import com.ats.auth.dto.AdminUserListItemVO;
import com.ats.auth.dto.CreateUserReq;
import com.ats.auth.dto.MeVO;
import com.ats.auth.dto.UpdateUserReq;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.SubDepartment;
import com.ats.entity.User;
import com.ats.repository.HrSubDepartmentMapper;
import com.ats.repository.SubDepartmentMapper;
import com.ats.repository.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Admin 用户管理服务：单个 / 批量创建 HR / CANDIDATE 账号。
 *
 * 设计要点：
 * - 单个创建：事务内执行，重复邮箱抛 {@link BizException}；
 * - 批量创建：每行独立 try / catch，单行失败不影响其余行 ——
 *   这样运营侧"导入 50 条 HR、其中 3 条邮箱已存在"时不会整批回滚，
 *   前端可逐行展示成功 / 失败状态；
 * - 批量请求内部还需查重 email 重复（如同批 2 行写同一邮箱），
 *   先匹配先成功、后续记为 EMAIL_ALREADY_EXISTS；
 * - ADMIN 账号始终拒绝创建（防权限提升）—— Bean Validation 在 DTO 层
 *   已用 {@code @Pattern(regexp = "HR|CANDIDATE")} 拦下，service 层不再额外判断。
 * - M6：HR 创建时必须绑定至少一个子部门（hr_sub_departments）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SubDepartmentMapper subDepartmentMapper;
    private final HrSubDepartmentMapper hrSubDepartmentMapper;

    public List<AdminUserListItemVO> listUsers(String role, Boolean activeOnly) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreatedAt);
        if (role != null && !role.isBlank()) {
            q.eq(User::getRole, role.toUpperCase());
        }
        if (Boolean.TRUE.equals(activeOnly)) {
            q.eq(User::getIsActive, true);
        }
        return userMapper.selectList(q).stream().map(this::toListItem).toList();
    }

    @Transactional
    public AdminUserListItemVO updateUser(Long id, UpdateUserReq req) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BizException(ErrorCode.FORBIDDEN, "不能修改 ADMIN 账号");
        }
        if (req.getFullName() != null) {
            user.setFullName(req.getFullName().trim());
        }
        if (req.getRole() != null) {
            user.setRole(req.getRole().toUpperCase());
        }
        if (req.getActive() != null) {
            user.setIsActive(req.getActive());
        }
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        }
        userMapper.updateById(user);

        if (req.getSubDepartmentIds() != null) {
            hrSubDepartmentMapper.deleteByUserId(id);
            if ("HR".equalsIgnoreCase(user.getRole())) {
                validateSubDepartmentIds(req.getSubDepartmentIds());
                bindHrSubDepartments(id, user.getRole(), req.getSubDepartmentIds());
            }
        }
        return toListItem(user);
    }

    /** 创建单个 HR / CANDIDATE 账号。 */
    @Transactional
    public MeVO createUser(CreateUserReq req) {
        String email = req.getEmail().toLowerCase();

        long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (count > 0) throw BizException.of(ErrorCode.EMAIL_ALREADY_EXISTS);

        validateRoleBindings(req);

        User user = persist(email, req.getPassword(), req.getFullName(), req.getRole());
        bindHrSubDepartments(user.getId(), req.getRole(), req.getSubDepartmentIds());

        log.info("[ADMIN] create user id={} email={} role={}", user.getId(), user.getEmail(), user.getRole());
        return MeVO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    /**
     * 批量创建。逐行独立提交，单行失败不影响其余行 ——
     * 因此整体方法不加 {@code @Transactional}（每个 mapper.insert 走默认短事务）。
     */
    public BatchCreateUsersVO batchCreate(BatchCreateUsersReq req) {
        List<CreateUserReq> users = req.getUsers();
        List<BatchCreateItemVO> items = new ArrayList<>(users.size());
        Set<String> seenInBatch = new HashSet<>();
        int success = 0;
        int failure = 0;

        for (int i = 0; i < users.size(); i++) {
            CreateUserReq item = users.get(i);
            String email = item.getEmail() == null ? "" : item.getEmail().toLowerCase();

            try {
                if (!seenInBatch.add(email)) {
                    throw new BizException(ErrorCode.EMAIL_ALREADY_EXISTS, "同批次内重复邮箱：" + email);
                }

                long count = userMapper.selectCount(
                        new LambdaQueryWrapper<User>().eq(User::getEmail, email));
                if (count > 0) throw BizException.of(ErrorCode.EMAIL_ALREADY_EXISTS);

                validateRoleBindings(item);

                User user = persist(email, item.getPassword(), item.getFullName(), item.getRole());
                bindHrSubDepartments(user.getId(), item.getRole(), item.getSubDepartmentIds());

                items.add(BatchCreateItemVO.builder()
                        .rowIndex(i)
                        .email(email)
                        .success(true)
                        .userId(user.getId())
                        .role(user.getRole())
                        .build());
                success++;
            }
            catch (BizException e) {
                items.add(BatchCreateItemVO.builder()
                        .rowIndex(i)
                        .email(email)
                        .success(false)
                        .errorCode(e.getErrorCode().getCode())
                        .errorMsg(e.getMessage())
                        .build());
                failure++;
            }
            catch (Exception e) {
                log.warn("[ADMIN] batch create row {} unexpected error: {}", i, e.toString());
                items.add(BatchCreateItemVO.builder()
                        .rowIndex(i)
                        .email(email)
                        .success(false)
                        .errorCode(ErrorCode.INTERNAL_ERROR.getCode())
                        .errorMsg("服务异常：" + e.getMessage())
                        .build());
                failure++;
            }
        }

        log.info("[ADMIN] batch create done · total={} success={} failure={}",
                users.size(), success, failure);

        return BatchCreateUsersVO.builder()
                .successCount(success)
                .failureCount(failure)
                .items(items)
                .build();
    }

    private void validateRoleBindings(CreateUserReq req) {
        if (!"HR".equalsIgnoreCase(req.getRole())) {
            return;
        }
        List<Long> ids = req.getSubDepartmentIds();
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "创建 HR 账号必须绑定至少一个子部门");
        }
        validateSubDepartmentIds(ids);
    }

    private void validateSubDepartmentIds(List<Long> ids) {
        Set<Long> unique = new HashSet<>(ids);
        if (unique.size() != ids.size()) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "子部门 id 列表不能重复");
        }
        for (Long id : unique) {
            SubDepartment sd = subDepartmentMapper.selectById(id);
            if (sd == null) {
                throw BizException.of(ErrorCode.SUB_DEPARTMENT_NOT_FOUND);
            }
        }
    }

    private void bindHrSubDepartments(Long userId, String role, List<Long> subDepartmentIds) {
        if (!"HR".equalsIgnoreCase(role) || subDepartmentIds == null || subDepartmentIds.isEmpty()) {
            return;
        }
        hrSubDepartmentMapper.batchInsert(userId, subDepartmentIds);
    }

    private AdminUserListItemVO toListItem(User user) {
        List<Long> subIds = "HR".equalsIgnoreCase(user.getRole())
                ? hrSubDepartmentMapper.selectSubDepartmentIdsByUserId(user.getId())
                : List.of();
        return AdminUserListItemVO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.getIsActive())
                .subDepartmentIds(subIds)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private User persist(String email, String password, String fullName, String role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role.toUpperCase());
        user.setIsActive(true);
        userMapper.insert(user);
        return user;
    }
}
