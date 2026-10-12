/**
 * 数据概览页
 */
Vue.component('page-dashboard', {
    template: `
    <div v-loading="loading">
        <h2 class="page-title">数据概览</h2>
        <p class="page-desc">套餐库整体情况与各分类统计</p>

        <!-- 顶部指标 -->
        <el-row :gutter="16" style="margin-bottom:18px">
            <el-col :xs="12" :sm="6" v-for="s in summaryCards" :key="s.lbl">
                <div class="stat-block" style="margin-bottom:12px">
                    <div class="num">{{ s.num }}</div>
                    <div class="lbl">{{ s.lbl }}</div>
                </div>
            </el-col>
        </el-row>

        <el-row :gutter="18">
            <!-- 分类统计表 -->
            <el-col :xs="24" :md="14">
                <el-card shadow="never">
                    <div slot="header" class="card-title">套餐分类统计</div>
                    <el-table :data="stat" size="small" border stripe>
                        <el-table-column label="分类" min-width="110">
                            <template slot-scope="s">
                                {{ McdUtil.categoryText(s.row.category) }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="comboCount" label="套餐数" width="80"></el-table-column>
                        <el-table-column label="均价" width="90">
                            <template slot-scope="s">
                                <span class="price-text">¥{{ McdUtil.money(s.row.avgPrice) }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="avgCalories" label="平均热量" width="95"></el-table-column>
                        <el-table-column prop="totalSales" label="总销量" width="90"></el-table-column>
                        <el-table-column prop="avgRating" label="平均评分" width="90"></el-table-column>
                    </el-table>
                </el-card>
            </el-col>

            <!-- 销量占比 -->
            <el-col :xs="24" :md="10">
                <el-card shadow="never">
                    <div slot="header" class="card-title">各分类销量占比</div>
                    <div v-for="s in stat" :key="s.category" style="margin-bottom:14px">
                        <div class="detail-row">
                            <span>{{ McdUtil.categoryText(s.category) }}</span>
                            <span class="score-val">{{ s.totalSales }} 份</span>
                        </div>
                        <el-progress :percentage="pct(s.totalSales)" :show-text="false"
                                     :stroke-width="10" color="#DA291C"></el-progress>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 最近推荐记录 -->
        <el-card shadow="never" style="margin-top:18px">
            <div slot="header" class="card-title">最近推荐记录</div>
            <el-table :data="records" size="small" border stripe>
                <el-table-column prop="id" label="记录ID" width="80"></el-table-column>
                <el-table-column prop="userId" label="用户" width="110"></el-table-column>
                <el-table-column label="Top1 套餐" min-width="160">
                    <template slot-scope="s">
                        {{ comboName(s.row.topComboId) }}
                    </template>
                </el-table-column>
                <el-table-column label="得分" width="90">
                    <template slot-scope="s">
                        <span class="price-text">{{ s.row.topScore }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="costMs" label="耗时(ms)" width="100"></el-table-column>
                <el-table-column prop="createTime" label="时间" min-width="160"></el-table-column>
            </el-table>
            <div v-if="!records.length" class="empty-box" style="padding:26px">
                <div>还没有推荐记录，去「智能推荐」页试一次吧</div>
            </div>
        </el-card>
    </div>
    `,

    data() {
        return {
            McdUtil: McdUtil,
            loading: false,
            stat: [],
            records: [],
            comboMap: {},
            summary: {productCount: 0, comboCount: 0, recordCount: 0, avgPrice: 0}
        };
    },

    computed: {
        summaryCards() {
            return [
                {num: this.summary.comboCount, lbl: '在售套餐'},
                {num: this.summary.productCount, lbl: '在售单品'},
                {num: '¥' + McdUtil.money(this.summary.avgPrice), lbl: '套餐均价'},
                {num: this.summary.recordCount, lbl: '推荐记录'}
            ];
        },
        maxSales() {
            return this.stat.reduce((m, s) => Math.max(m, Number(s.totalSales) || 0), 1);
        }
    },

    created() {
        this.load();
    },

    methods: {
        pct(v) {
            const p = (Number(v) || 0) / this.maxSales * 100;
            return Math.round(p);
        },

        comboName(id) {
            if (!id) return '-';
            return this.comboMap[id] || ('套餐 #' + id);
        },

        load() {
            this.loading = true;
            Promise.all([
                McdApi.comboStat(),
                McdApi.comboList(),
                McdApi.productPage({current: 1, size: 1}),
                McdApi.records({limit: 10})
            ]).then(([stat, combos, productPage, records]) => {
                this.stat = stat || [];

                const map = {};
                (combos || []).forEach(c => map[c.id] = c.name);
                this.comboMap = map;

                this.records = records || [];

                const total = this.stat.reduce((a, s) => a + Number(s.comboCount || 0), 0);
                const priceSum = this.stat.reduce(
                    (a, s) => a + Number(s.avgPrice || 0) * Number(s.comboCount || 0), 0);

                this.summary = {
                    comboCount: total,
                    productCount: productPage.total || 0,
                    recordCount: this.records.length,
                    avgPrice: total ? priceSum / total : 0
                };
            }).catch(err => {
                this.$message.error(err.message || '加载失败');
            }).finally(() => {
                this.loading = false;
            });
        }
    }
});
