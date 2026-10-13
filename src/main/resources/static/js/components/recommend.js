/**
 * 首页：智能推荐
 */
Vue.component('page-recommend', {
    template: `
    <div class="recommend-page">
        <h2 class="page-title">智能套餐推荐</h2>
        <p class="page-desc">填写你的偏好，系统会从全部在售套餐中按 7 个维度打分排序，给出最合适的推荐</p>

        <el-row :gutter="18" class="rec-row">
            <!-- 左侧：条件筛选（卡片撑满高度，标题固定、仅表单区滚动） -->
            <el-col :xs="24" :sm="24" :md="8" :lg="7" class="rec-col rec-col-left">
                <el-card shadow="never">
                    <div slot="header" class="card-title">我的偏好</div>

                    <el-form label-position="top" size="small" class="filter-form">
                        <el-form-item>
                            <template slot="label">
                                <div class="label-line">
                                    <span>预算范围（元）</span>
                                    <b class="label-value">¥{{ form.budget[0] }} ~ ¥{{ form.budget[1] }}</b>
                                </div>
                            </template>
                            <el-slider v-model="form.budget" range :min="0" :max="200" :step="5"
                                       :marks="{0:'0', 50:'50', 100:'100', 200:'200'}"></el-slider>
                            <div class="tag-hint">价格区间外的套餐不会出现在推荐结果里</div>
                        </el-form-item>

                        <el-form-item label="就餐人数">
                            <div class="people-row">
                                <span class="people-label">大人</span>
                                <el-input-number v-model="form.peopleCount" :min="1" :max="20"
                                                 size="mini" controls-position="right"
                                                 style="width:100px"></el-input-number>
                                <span class="people-label">儿童</span>
                                <el-input-number v-model="form.childCount" :min="0" :max="10"
                                                 size="mini" controls-position="right"
                                                 style="width:100px"></el-input-number>
                            </div>
                            <div class="tag-hint">
                                共 {{ form.peopleCount + form.childCount }} 人，按
                                <b>{{ effectivePeople }}</b> 人份计算分量
                                （儿童饭量按 0.6 人计，避免点多了浪费）；
                                带儿童时会优先推荐适合儿童的口味
                            </div>
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
                                             border size="mini"
                                             :disabled="form.avoidTags.indexOf(t) >= 0">{{ t }}</el-checkbox>
                            </el-checkbox-group>
                        </el-form-item>

                        <el-form-item label="忌口（可多选）">
                            <el-checkbox-group v-model="form.avoidTags">
                                <el-checkbox v-for="t in avoidOptions" :key="t" :label="t"
                                             border size="mini"
                                             :disabled="form.tasteTags.indexOf(t) >= 0">{{ t }}</el-checkbox>
                            </el-checkbox-group>
                            <div class="tag-hint">偏好与忌口不能是同一个标签，已被选走的口味会置灰</div>
                        </el-form-item>

                        <el-form-item label="期望辣度">
                            <el-rate v-model="form.spicyRate" :max="3" show-text
                                     :texts="['不辣','微辣','中辣','重辣']"></el-rate>
                        </el-form-item>

                        <el-form-item label="热量偏好（可选）">
                            <el-radio-group v-model="form.calorieLevel" size="mini">
                                <el-radio-button label="">不限</el-radio-button>
                                <el-radio-button label="LOW">热量偏低</el-radio-button>
                                <el-radio-button label="HIGH">热量偏高</el-radio-button>
                            </el-radio-group>
                            <div class="tag-hint">
                                以 {{ calorieThreshold }} kcal 为界：一份套餐不超过它算「偏低」，超过算「偏高」
                            </div>
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

            <!-- 右侧：推荐结果（独立滚动，整页保持不动） -->
            <el-col :xs="24" :sm="24" :md="16" :lg="17" class="rec-col rec-col-right">
                <!-- 结果摘要 -->
                <div v-if="result" class="summary-bar">
                    <i class="el-icon-star-on"></i>
                    <div>
                        <div>{{ result.summary }}</div>
                        <div style="margin-top:4px;font-size:12px;color:#B08A00">
                            候选套餐 {{ result.totalCandidates }} 个 ·
                            实际时段「{{ McdUtil.timeSlotText(result.timeSlot) }}」
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
                                    <el-tag size="mini" style="margin-right:5px"
                                            :class="['cat-tag', McdUtil.categoryClass(item.combo.category)]">
                                        {{ McdUtil.categoryText(item.combo.category) }}
                                    </el-tag>
                                    <el-tag v-for="t in item.combo.tasteTagList" :key="t"
                                            size="mini" style="margin-right:5px">{{ t }}</el-tag>
                                    <el-tag size="mini" type="warning">{{ item.combo.calories }} kcal</el-tag>
                                </div>
                            </div>

                            <!-- 综合得分：所有推荐结果都展示，第一名做强调 -->
                            <div class="score-box" :class="{'score-top': item.rank === 1}">
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
                            <span v-for="p in item.combo.items" :key="p.id" class="item-tag"
                                  :class="McdUtil.productCategoryClass(p.category)">
                                {{ p.name }} ×{{ p.quantity }}
                            </span>
                        </div>

                        <!-- 得分明细（折叠） -->
                        <div style="margin-top:10px">
                            <el-button type="text" size="mini"
                                       @click="toggleDetail(item)">
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
            /**
             * 热量档位分界线（kcal），仅用于界面提示。
             * 必须与后端 application.yml 的 recommender.calorie-threshold 保持一致。
             */
            calorieThreshold: 1000,
            form: {
                budget: [30, 60],
                peopleCount: 1,
                /** 儿童人数：饭量按 0.6 个大人折算 */
                childCount: 0,
                timeSlot: null,
                tasteTags: [],
                avoidTags: [],
                spicyRate: 0,
                /** 热量偏好档位：''=不限 / LOW=热量偏低 / HIGH=热量偏高 */
                calorieLevel: ''
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

    computed: {
        /**
         * 等效就餐人数 = 大人 × 1.0 + 儿童 × 0.6
         * 儿童饭量约为大人的六成，按等效人数匹配套餐分量能减少浪费。
         * 与后端 PeopleFitStrategy 的折算系数保持一致。
         */
        effectivePeople() {
            const a = Number(this.form.peopleCount) || 0;
            const c = Number(this.form.childCount) || 0;
            return Math.round((a + c * 0.6) * 10) / 10;
        }
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
            // 不再传 userId，后端自动从登录态取。
            // 也不再传 topN：返回几条由后端决定——最多取配置值，
            // 符合条件的套餐不足时有多少给多少，不会为凑数把区间外的塞进来。
            const payload = {
                peopleCount: f.peopleCount,
                childCount: f.childCount || 0
            };
            if (f.budget && f.budget.length === 2) {
                payload.budgetMin = f.budget[0];
                payload.budgetMax = f.budget[1];
            }
            if (f.timeSlot) payload.timeSlot = f.timeSlot;
            if (f.tasteTags.length) payload.tasteTags = f.tasteTags;
            if (f.avoidTags.length) payload.avoidTags = f.avoidTags;
            if (f.spicyRate > 0) payload.spicyLevel = f.spicyRate;
            if (f.calorieLevel) payload.calorieLevel = f.calorieLevel;
            return payload;
        },

        // 展开 / 收起得分明细
        // 注意：必须用 this.$set，不能用 item._showDetail = !item._showDetail。
        // _showDetail 是点击时才新增的属性，Vue 2 无法侦测对象属性的「新增」，
        // 直接赋值不会触发视图更新，表现为点击按钮毫无反应。
        toggleDetail(item) {
            this.$set(item, '_showDetail', !item._showDetail);
        },

        doRecommend() {
            // 校验：口味偏好与忌口不能出现同一个标签（逻辑自相矛盾）
            // 界面上已通过置灰阻止，这里是兜底，防止其他途径构造出矛盾条件。
            const conflict = this.form.tasteTags.filter(t => this.form.avoidTags.indexOf(t) >= 0);
            if (conflict.length) {
                this.$message({
                    type: 'error',
                    showClose: true,
                    duration: 5000,
                    message: '「' + conflict.join('、') + '」不能同时出现在口味偏好和忌口中，请去掉其中一个再试'
                });
                return;
            }
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
            this.form.childCount = 0;
            this.form.tasteTags = ['经典', '牛肉'];
            this.form.avoidTags = ['香辣'];
            this.form.spicyRate = 0;
            this.form.calorieLevel = '';
            this.doRecommend();
        },

        resetForm() {
            this.form = {
                budget: [30, 60],
                peopleCount: 1,
                childCount: 0,
                timeSlot: this.currentSlot(),
                tasteTags: [],
                avoidTags: [],
                spicyRate: 0,
                calorieLevel: ''
            };
            this.result = null;
        }
    }
});
