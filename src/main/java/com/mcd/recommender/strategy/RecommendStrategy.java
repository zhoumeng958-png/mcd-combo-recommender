package com.mcd.recommender.strategy;

/**
 * 推荐策略接口
 * <p>
 * 实现类交给 Spring 管理，{@link StrategyEngine} 会自动收集并按 order 升序执行。
 * 新增一个推荐维度 = 新增一个实现类，无需改动引擎与业务代码。
 */
public interface RecommendStrategy {

    /**
     * 策略名称，需与 application.yml 中 recommender.weights 的 key 对应
     */
    String name();

    /**
     * 执行顺序，数值越小越先执行
     */
    default int order() {
        return 100;
    }

    /**
     * 执行打分
     *
     * @param context 推荐上下文
     * @return true 表示该策略本次生效（计入权重分母）；false 表示条件不满足，跳过
     */
    boolean score(RecommendContext context);
}
