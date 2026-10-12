package com.mcd.recommender.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面跳转控制器
 * <p>
 * 让根路径直接进入前端首页，避免用户需要手动输入 index.html
 */
@Controller
public class IndexController {

    @GetMapping({"/", "/index"})
    public String index() {
        return "forward:/index.html";
    }
}
