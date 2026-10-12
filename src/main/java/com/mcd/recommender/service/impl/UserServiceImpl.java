package com.mcd.recommender.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mcd.recommender.common.BusinessException;
import com.mcd.recommender.common.PageResult;
import com.mcd.recommender.common.ResultCode;
import com.mcd.recommender.dto.LoginRequest;
import com.mcd.recommender.dto.RegisterRequest;
import com.mcd.recommender.entity.User;
import com.mcd.recommender.mapper.UserMapper;
import com.mcd.recommender.security.LoginUser;
import com.mcd.recommender.security.TokenManager;
import com.mcd.recommender.service.UserService;
import com.mcd.recommender.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenManager tokenManager;

    @Override
    public LoginVO login(LoginRequest request) {
        User user = getByUsername(request.getUsername());

        // 用户不存在与密码错误返回同一提示，避免暴露"哪些用户名存在"
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 更新最后登录时间
        User update = new User();
        update.setId(user.getId());
        update.setLastLogin(LocalDateTime.now());
        userMapper.updateById(update);

        return buildLoginVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterRequest request) {
        if (getByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // 存哈希，绝不存明文
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname())
                ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        log.info("新用户注册成功: {}", user.getUsername());
        return buildLoginVO(user);
    }

    @Override
    public void logout(String token) {
        tokenManager.removeToken(token);
    }

    @Override
    public User getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("limit 1"));
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "新密码长度不能少于 6 位");
        }

        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(update);

        log.info("用户修改密码: {}", user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateProfile(User user) {
        User exist = getById(user.getId());
        // 只允许改这几个字段，防止越权修改密码与角色
        User update = new User();
        update.setId(exist.getId());
        update.setNickname(user.getNickname());
        update.setEmail(user.getEmail());
        update.setPhone(user.getPhone());
        update.setAvatar(user.getAvatar());
        userMapper.updateById(update);
        return userMapper.selectById(exist.getId());
    }

    @Override
    public PageResult<User> page(String keyword, Long current, Long size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, kw).or().like(User::getNickname, kw));
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = userMapper.selectPage(
                new Page<>(current == null || current < 1 ? 1L : current,
                        size == null || size < 1 ? 10L : size),
                wrapper);
        return PageResult.of(page);
    }

    /* ------------------------------------------------------------------ */

    private LoginVO buildLoginVO(User user) {
        LoginUser loginUser = new LoginUser(
                user.getId(), user.getUsername(), user.getNickname(), user.getRole());
        String token = tokenManager.createToken(loginUser);
        return toVO(user, token);
    }

    private LoginVO toVO(User user, String token) {
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(tokenManager.getExpireSeconds());
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        return vo;
    }
}
