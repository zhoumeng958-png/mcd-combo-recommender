package com.mcd.recommender.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 单品分类
 */
@Getter
@AllArgsConstructor
public enum ProductCategory {

    HAMBURGER("汉堡类"),
    SIDE("小食类"),
    DRINK("饮料类"),
    DESSERT("甜品/冰淇淋"),
    BREAKFAST("早餐类");

    private final String desc;

    public static ProductCategory of(String code) {
        if (code == null) {
            return null;
        }
        for (ProductCategory value : values()) {
            if (value.name().equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
