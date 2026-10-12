package com.mcd.recommender.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签工具类：库中标签统一用逗号分隔的字符串存储，这里做拆分与比对
 */
public final class TagUtils {

    /** 主料标签，用于多样性策略判断 */
    public static final List<String> MAIN_INGREDIENTS = Arrays.asList("牛肉", "鸡肉", "鱼", "猪肉");

    /** 主料缺失时的兜底值 */
    public static final String OTHER_INGREDIENT = "其他";

    private TagUtils() {
    }

    /**
     * 逗号分隔字符串 -> 标签列表（去空、去重、保持顺序）
     */
    public static List<String> split(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String tag : tags.split("[,，]")) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * 两个集合的交集
     */
    public static List<String> intersect(Collection<String> a, Collection<String> b) {
        if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
            return Collections.emptyList();
        }
        return a.stream().filter(b::contains).collect(Collectors.toList());
    }

    /**
     * 判断逗号分隔的标签串是否与目标集合有交集
     */
    public static boolean hit(String tags, Collection<String> targets) {
        if (targets == null || targets.isEmpty()) {
            return false;
        }
        return !intersect(split(tags), targets).isEmpty();
    }

    /**
     * 提取主料标签（牛肉 / 鸡肉 / 鱼 / 猪肉），识别不出返回「其他」
     */
    public static String mainIngredient(String tasteTags) {
        for (String tag : split(tasteTags)) {
            if (MAIN_INGREDIENTS.contains(tag)) {
                return tag;
            }
        }
        return OTHER_INGREDIENT;
    }

    /**
     * 把标签列表拼回逗号分隔字符串
     */
    public static String join(Collection<String> tags) {
        return tags == null ? "" : String.join(",", tags);
    }
}
