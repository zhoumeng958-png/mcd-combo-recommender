package com.mcd.recommender.service;

import com.mcd.recommender.common.PageResult;
import com.mcd.recommender.dto.LoginRequest;
import com.mcd.recommender.dto.RegisterRequest;
import com.mcd.recommender.entity.User;
import com.mcd.recommender.vo.LoginVO;

/**
 * 用户服务
 */
public interface UserService {

    /** 登录，成功后返回 token 与用户信息 */
    LoginVO login(LoginRequest request);

    /** 注册 */
    LoginVO register(RegisterRequest request);

    /** 退出登录 */
    void logout(String token);

    /** 按用户名查询 */
    User getByUsername(String username);

    /** 按 ID 查询 */
    User getById(Long id);

    /** 修改密码 */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /** 更新个人资料 */
    User updateProfile(User user);

    /** 用户分页列表（管理员） */
    PageResult<User> page(String keyword, Long current, Long size);
}
