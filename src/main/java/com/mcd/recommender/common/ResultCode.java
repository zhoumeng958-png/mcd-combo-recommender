package com.mcd.recommender.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),

    /* 业务相关 1xxx */
    COMBO_NOT_FOUND(1001, "套餐不存在"),
    PRODUCT_NOT_FOUND(1002, "单品不存在"),
    PREFERENCE_NOT_FOUND(1003, "用户偏好不存在"),
    RECOMMEND_NO_RESULT(1004, "没有符合条件的推荐结果"),
    RECOMMEND_PARAM_INVALID(1005, "推荐参数不合法"),

    /* 用户相关 2xxx */
    USER_NOT_FOUND(2001, "用户不存在"),
    USERNAME_EXISTS(2002, "用户名已被占用"),
    PASSWORD_ERROR(2003, "用户名或密码错误"),
    USER_DISABLED(2004, "账号已被禁用，请联系管理员"),
    OLD_PASSWORD_ERROR(2005, "原密码不正确");

    private final Integer code;
    private final String message;
}
