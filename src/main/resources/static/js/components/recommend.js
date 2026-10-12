/**
 * 首页：智能推荐
 */
Vue.component('page-recommend', {
    template: `
    <div>
        <h2 class="page-title">智能套餐推荐</h2>
        <p class="page-desc">填写你的偏好，系统会从全部在售套餐中按 6 个维度打分排序，给出最合适的推荐</p>

        <el-row :gutter="18">
            <!-- 左侧：条件筛选 -->
            <el-col :xs="24" :sm="24" :md="8" :lg="7">
                <el-card shadow="never">
                    <div slot="header" class="card-title">我的偏好</div>

                    <el-form label-position="top" size="small" class="filter-form">
                        <el-form-item label="预算范围（元）">
                            <el-slider v-model="form.budget" range :min="0" :max="200" :step="5"
                                       :marks="{0:'0', 50:'50', 100:'100', 200:'200'}"></el-slider>
                        </el-form-item>

                        <el-form-item label="就餐人数">
                            <el-radio-group v-model="form.peopleCount" size="mini">
                                <el-radio-button :label="1">1人</el-radio-button>
                                <el-radio-button :label="2">2人</el-radio-button>
                                <el-radio-button :label="3">3人</el-radio-button>
                                <el-radio-button :label="4">4人</el-radio-button>
                            </el-radio-group>
                        </el-form-item>

                        <el-form-item label="就餐时段">
                            <el-select v-model="form.timeSlot" placeholder="自动识别当前时段"
                                       clearable style="width:100%">
                                <el-option v-for="s in timeSlots" :key="s.value"
                                           :label="s.label" :value="s.value"></el-option>
                            </el-select>
                        </el-form-item>

                        <el-form-item label="口味偏好（可多选）">
                            <el-checkbox-group v-model="form.tasteTags">
                                <el-checkbox v-for="t in tasteOptions" :key="t" :label="t"
                                             border size="mini">{{ t }}</el-checkbox>
                            </el-checkbox-group>
                        </el-form-item>

                        <el-form-item label="忌口（可多选）">
                            <el-checkbox-group v-model="form.avoidTags">
                                <el-checkbox v-for="t in avoidOptions" :key="t" :label="t"
                                             border size="mini">{{ t }}</el-checkbox>
                            </el-checkbox-group>
                        </el-form-item>

                        <el-form-item label="期望辣度">
                            <el-rate v-model="form.spicyRate" :max="3" show-text
                                     :texts="['不辣','微辣','中辣','重辣']"></el-rate>
                        </el-form-item>

                        <el-form-item label="热量上限（可选，减脂用）">
                            <el-input-number v-model="form.maxCalories" :min="0" :max="5000"
                                             :step="100" controls-position="right"
                                             placeholder="不限" style="width:100%"></el-input-number>
                        </el-form-item>

                        <el-form-item label="返回数量">
                            <el-radio-group v-model="form.topN" size="mini">
                                <el-radio-button :label="3">3</el-radio-button>
                                <el-radio-button :label="5">5</el-radio-button>
                                <el-radio-button :label="10">10</el-radio-button>
                            </el-radio-group>
                        </el-form-item>

                        <el-button type="danger" icon="el-icon-magic-stick"
                                   style="width:100%" :loading="loading" @click="doRecommend">
                            {{ loading ? '正在计算…' : '开始推荐' }}
                        </el-button>
                        <el-button size="mini" type="text" style="width:100%;margin:8px 0 0"
                                   @click="resetForm">重置条件</el-button>
                    </el-form>
                </el-card>
            </el-col>

            <!-- 右侧：推荐结果 -->
            <el-col :xs="24" :sm="24" :md="16" :lg="17">
                <!-- 结果摘要 -->
                <div v-if="result" class="summary-bar">
                    <i class="el-icon-star-on"></i>
                    <div>
                        <div>{{ result.summary }}</div>
                        <div style="margin-top:4px;font-size:12px;color:#B08A00">
                            候选套餐 {{ result.totalCandidates }} 个 ·
                            实际时段「{{ McdUtil.timeSlotText(result.timeSlot) }}」·
                            计算耗时 {{ result.costMs }} ms
                        </div>
                    </div>
                </div>

                <!-- 加载中 -->
                <div v-if="loading" class="empty-box">
                    <div class="empty-icon"><i class="el-icon-loading"></i></div>
                    <div>正在为你的偏好打分排序…</div>
                </div>

                <!-- 未推荐时的引导 -->
                <div v-else-if="!result" class="empty-box">
                    <div class="empty-icon"><i class="el-icon-dish"></i></div>
                    <div>左侧填好偏好后点击「开始推荐」</div>
                    <div style="font-size:12px;margin-top:6px">
                        也可以直接试试
                        <el-button type="text" @click="quickDemo">一键示例</el-button>
                    </div>
                </div>

                <!-- 推荐结果列表 -->
                <div v-else>
                    <div v-for="item in result.items" :key="item.rank"
                         class="rec-card" :class="{'top-1': item.rank === 1}">
                        <div class="rec-head">
                            <span class="rank-badge" :class="'rank-' + item.rank">{{ item.rank }}</span>

                            <div style="flex:1;min-width:0">
                                <div class="rec-name">
                                    {{ item.combo.name }}
                                    <el-tag v-if="item.rank === 1" type="danger" size="mini"
                                            effect="dark" style="margin-left:6px">最佳推荐</el-tag>
                                </div>
                                <p class="rec-desc">{{ item.combo.description }}</p>
                                <div style="margin-top:6px">
                                    <el-tag size="mini" type="info" style="margin-right:5px">
                                        {{ McdUtil.categoryText(item.combo.category) }}
                                    </el-tag>
                                    <el-tag v-for="t in item.combo.tasteTagList" :key="t"
                                            size="mini" style="margin-right:5px">{{ t }}</el-tag>
                                    <el-tag size="mini" type="warning">{{ item.combo.calories }} kcal</el-tag>
                                </div>
                            </div>

                            <div class="score-box" v-if="item.rank === 1">
                                <div class="score-num">{{ item.score }}</div>
                                <div class="score-label">综合得分</div>
                            </div>

                            <div class="rec-price">
                                <div class="now"><small>¥</small>{{ McdUtil.money(item.combo.price) }}</div>
                                <span class="old" v-if="item.combo.originalPrice">
                                    ¥{{ McdUtil.money(item.combo.originalPrice) }}
                                </span>
                                <el-tag v-if="item.combo.discount" type="danger" size="mini"
                                        effect="plain">{{ item.combo.discount }}折</el-tag>
                            </div>
                        </div>

                        <!-- 推荐理由 -->
                        <ul class="reason-list" v-if="item.reasons && item.reasons.length">
                            <li v-for="(r, i) in item.reasons" :key="i">{{ r }}</li>
                        </ul>

                        <!-- 套餐内容 -->
                        <div class="combo-items">
                            <i class="el-icon-food"></i>
                            <span style="margin-right:8px">套餐包含：</span>
                            <span v-for="p in item.combo.items" :key="p.id" class="item-tag">
                                {{ p.name }} ×{{ p.quantity }}
                            </span>
                        </div>

                        <!-- 得分明细（折叠） -->
                        <div style="margin-top:10px">
                            <el-button type="text" size="mini"
                                       @click="item._showDetail = !item._showDetail">
                                {{ item._showDetail ? '收起得分明细' : '查看得分明细' }}
                                <i :class="item._showDetail ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
                            </el-button>
                            <div v-show="item._showDetail"
                                 style="margin-top:10px;padding:12px 16px;background:#FAFAFA;border-radius:8px">
                                <el-row :gutter="20">
                                    <el-col :span="12" v-for="(v, k) in item.scoreDetail" :key="k">
                                        <div class="detail-row">
                                            <span>{{ McdUtil.strategyText(k) }}</span>
                                            <span class="score-val">{{ v }} 分</span>
                                        </div>
                                        <el-progress :percentage="v" :show-text="false"
                                                     :stroke-width="5"
                                                     :color="v >= 85 ? '#67C23A' : (v >= 60 ? '#E6A23C' : '#F56C6C')">
                                        </el-progress>
                                    </el-col>
                                </el-row>
                            </div>
                        </div>
                    </div>
                </div>
            </el-col>
        </el-row>
    </div>
    `,

    data() {
        return {
            McdUtil: McdUtil,
            loading: false,
            result: null,
            form: {
                budget: [30, 60],
                peopleCount: 1,
                timeSlot: null,
                tasteTags: [],
                avoidTags: [],
                spicyRate: 0,
                maxCalories: undefined,
                topN: 5
            },
            tasteOptions: ['经典', '香辣', '牛肉', '鸡肉', '清淡', '芝士', '甜', '醇香', '冰爽', '咸香'],
            avoidOptions: ['香辣', '牛肉', '鸡肉', '鱼', '猪肉', '芝士', '甜'],
            timeSlots: [
                {value: 'BREAKFAST', label: '早餐（5:00-10:30）'},
                {value: 'LUNCH', label: '午餐（10:30-14:00）'},
                {value: 'TEA', label: '下午茶（14:00-17:00）'},
                {value: 'DINNER', label: '晚餐（17:00-21:00）'},
                {value: 'LATE_NIGHT', label: '夜宵（21:00 以后）'}
            ]
        };
    },

    created() {
        // 自动读取当前时段，方便用户知道默认值
        this.form.timeSlot = this.currentSlot();
    },

    methods: {
        currentSlot() {
            const h = new Date().getHours();
            if (h >= 5 && h < 10.5) return 'BREAKFAST';
            if (h >= 10.5 && h < 14) return 'LUNCH';
            if (h >= 14 && h < 17) return 'TEA';
            if (h >= 17 && h < 21) return 'DINNER';
            return 'LATE_NIGHT';
        },

        buildPayload() {
            const f = this.form;
            // 不再传 userId，后端自动从登录态取
            const payload = {
                peopleCount: f.peopleCount,
                topN: f.topN
            };
            if (f.budget && f.budget.length === 2) {
                payload.budgetMin = f.budget[0];
                payload.budgetMax = f.budget[1];
            }
            if (f.timeSlot) payload.timeSlot = f.timeSlot;
            if (f.tasteTags.length) payload.tasteTags = f.tasteTags;
            if (f.avoidTags.length) payload.avoidTags = f.avoidTags;
            if (f.spicyRate > 0) payload.spicyLevel = f.spicyRate;
            if (f.maxCalories) payload.maxCalories = f.maxCalories;
            return payload;
        },

        doRecommend() {
            this.loading = true;
            this.result = null;
            McdApi.recommend(this.buildPayload())
                .then(data => {
                    this.result = data;
                })
                .catch(err => {
                    this.$message.error(err.message || '推荐失败，请检查后端服务');
                })
                .finally(() => {
                    this.loading = false;
                });
        },

        quickDemo() {
            this.form.budget = [30, 60];
            this.form.peopleCount = 1;
            this.form.tasteTags = ['经典', '牛肉'];
            this.form.avoidTags = ['香辣'];
            this.form.spicyRate = 0;
            this.form.maxCalories = undefined;
            this.form.topN = 5;
            this.doRecommend();
        },

        resetForm() {
            this.form = {
                budget: [30, 60],
                peopleCount: 1,
                timeSlot: this.currentSlot(),
                tasteTags: [],
                avoidTags: [],
                spicyRate: 0,
                maxCalories: undefined,
                topN: 5
            };
            this.result = null;
        }
    }
});
