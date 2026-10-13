package com.mcd.recommender.enums;

/**
 * 热量偏好档位。
 * <p>
 * 多数人对「热量多少 kcal」没有具体概念，因此前端只给「偏低 / 偏高」两档，
 * 由后端按 {@code recommender.calorie-threshold}（默认 1000 kcal）换算成硬性条件：
 * <ul>
 *     <li>{@link #LOW} —— 只保留 ≤ 阈值的套餐</li>
 *     <li>{@link #HIGH} —— 只保留 &gt; 阈值的套餐</li>
 * </ul>
 */
public enum CalorieLevel {

    /** 热量偏低 */
    LOW("热量偏低"),

    /** 热量偏高 */
    HIGH("热量偏高");

    private final String desc;

    CalorieLevel(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 宽松解析：忽略大小写与首尾空格，无法识别时返回 {@code null}（视为不限）。
     */
    public static CalorieLevel of(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        String value = code.trim().toUpperCase();
        for (CalorieLevel level : values()) {
            if (level.name().equals(value)) {
                return level;
            }
        }
        return null;
    }
}
