package com.mcd.recommender.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 套餐分类
 */
@Getter
@AllArgsConstructor
public enum ComboCategory {

    CLASSIC("经典组合"),
    SPICY("香辣组合"),
    VALUE("超值组合"),
    FAMILY("家庭/多人分享"),
    BREAKFAST("早餐组合"),
    TEA("下午茶组合"),
    DESSERT("甜品组合");

    private final String desc;

    public static ComboCategory of(String code) {
        if (code == null) {
            return null;
        }
        for (ComboCategory value : values()) {
            if (value.name().equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
