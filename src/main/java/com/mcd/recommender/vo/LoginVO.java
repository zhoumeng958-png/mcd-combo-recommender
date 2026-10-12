package com.mcd.recommender.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录成功返回
 */
@Data
@Schema(description = "登录结果")
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "访问令牌，后续请求需放入请求头 Authorization")
    private String token;

    @Schema(description = "令牌有效期（秒）")
    private Long expiresIn;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "角色：ADMIN / USER")
    private String role;

    @Schema(description = "头像")
    private String avatar;
}
