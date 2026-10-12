package com.mcd.recommender.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

/**
 * 就餐时段，并给出每个时段的默认时间窗口，用于「按时段推荐」策略
 */
@Getter
@AllArgsConstructor
public enum TimeSlot {

    BREAKFAST("早餐", LocalTime.of(5, 0), LocalTime.of(10, 30)),
    LUNCH("午餐", LocalTime.of(10, 30), LocalTime.of(14, 0)),
    TEA("下午茶", LocalTime.of(14, 0), LocalTime.of(17, 0)),
    DINNER("晚餐", LocalTime.of(17, 0), LocalTime.of(21, 0)),
    LATE_NIGHT("夜宵", LocalTime.of(21, 0), LocalTime.of(23, 59, 59)),
    ALL_DAY("全天", LocalTime.MIN, LocalTime.MAX);

    private final String desc;
    private final LocalTime start;
    private final LocalTime end;

    /**
     * 根据当前时间推断所处时段
     */
    public static TimeSlot current() {
        LocalTime now = LocalTime.now();
        for (TimeSlot slot : values()) {
            if (slot == ALL_DAY) {
                continue;
            }
            if (!now.isBefore(slot.start) && !now.isAfter(slot.end)) {
                return slot;
            }
        }
        // 0:00 - 5:00 视为夜宵
        return LATE_NIGHT;
    }

    public static TimeSlot of(String code) {
        if (code == null) {
            return null;
        }
        for (TimeSlot value : values()) {
            if (value.name().equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
