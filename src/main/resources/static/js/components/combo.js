/**
 * 套餐浏览页
 */
Vue.component('page-combo', {
    template: `
    <div>
        <h2 class="page-title">套餐列表</h2>
        <p class="page-desc">浏览全部在售套餐，可按分类、价格区间筛选与排序</p>

        <el-card shadow="never" style="margin-bottom:16px">
            <el-form :inline="true" size="small">
                <el-form-item label="关键词">
                    <el-input v-model="query.keyword" placeholder="套餐名称 / 编码"
                              clearable style="width:170px" @keyup.enter.native="load"></el-input>
                </el-form-item>
                <el-form-item label="分类">
                    <el-select v-model="query.category" placeholder="全部" clearable
                               style="width:130px">
                        <el-option v-for="c in categories" :key="c.value"
                                   :label="c.label" :value="c.value"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="人数">
                    <el-select v-model="query.peopleCount" placeholder="不限" clearable
                               style="width:100px">
                        <el-option v-for="n in [1,2,3,4]" :key="n" :label="n + '人'"
                                   :value="n"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="排序">
                    <el-select v-model="query.sortField" style="width:120px" @change="load">
                        <el-option label="按销量" value="salesCount"></el-option>
                        <el-option label="按价格" value="price"></el-option>
                        <el-option label="按评分" value="rating"></el-option>
                        <el-option label="按热量" value="calories"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-radio-group v-model="query.sortOrder" size="mini" @change="load">
                        <el-radio-button label="desc">降序</el-radio-button>
                        <el-radio-button label="asc">升序</el-radio-button>
                    </el-radio-group>
                </el-form-item>
                <el-form-item>
                    <el-button type="danger" icon="el-icon-search" @click="load">查询</el-button>
                    <el-button icon="el-icon-refresh-left" @click="reset">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <div v-loading="loading">
            <el-row :gutter="16">
                <el-col :xs="24" :sm="12" :md="8" :lg="6"
                        v-for="c in list" :key="c.id" style="margin-bottom:16px">
                    <el-card shadow="hover" body-style="padding:0">
                        <div style="padding:16px 16px 12px">
                            <div style="display:flex;justify-content:space-between;align-items:flex-start">
                                <div style="flex:1;min-width:0;padding-right:8px">
                                    <div class="rec-name" style="font-size:15px">{{ c.name }}</div>
                                    <div style="font-size:11px;color:#C0C4CC;margin-top:2px">{{ c.code }}</div>
                                </div>
                                <el-tag size="mini" type="info">
                                    {{ McdUtil.categoryText(c.category) }}
                                </el-tag>
                            </div>

                            <p class="rec-desc" style="margin-top:8px;min-height:38px">
                                {{ c.description }}
                            </p>

                            <div style="margin:8px 0">
                                <el-tag v-for="t in c.tasteTagList" :key="t" size="mini"
                                        style="margin:0 4px 4px 0">{{ t }}</el-tag>
                            </div>

                            <div style="display:flex;justify-content:space-between;font-size:12px;color:#909399">
                                <span><i class="el-icon-user"></i> {{ c.peopleCount }}人份</span>
                                <span><i class="el-icon-s-data"></i> {{ c.calories }} kcal</span>
                                <span><i class="el-icon-star-on"></i> {{ c.rating }}</span>
                            </div>
                        </div>

                        <div style="border-top:1px solid #EBEEF5;padding:10px 16px;
                                    display:flex;justify-content:space-between;align-items:center">
                            <div>
                                <span class="price-text" style="font-size:19px">¥{{ McdUtil.money(c.price) }}</span>
                                <span style="font-size:12px;color:#C0C4CC;text-decoration:line-through;
                                             margin-left:6px" v-if="c.originalPrice">
                                    ¥{{ McdUtil.money(c.originalPrice) }}
                                </span>
                                <div style="font-size:11px;color:#67C23A" v-if="c.saveAmount">
                                    立省 ¥{{ McdUtil.money(c.saveAmount) }}
                                </div>
                            </div>
                            <el-button type="danger" size="mini" plain
                                       @click="showDetail(c.id)">详情</el-button>
                        </div>
                    </el-card>
                </el-col>
            </el-row>

            <div v-if="!loading && !list.length" class="empty-box">
                <div class="empty-icon"><i class="el-icon-box"></i></div>
                <div>没有符合条件的套餐</div>
            </div>

            <el-pagination v-if="total > 0"
                           background
                           layout="total, prev, pager, next, sizes"
                           :total="total"
                           :current-page.sync="query.current"
                           :page-size.sync="query.size"
                           :page-sizes="[8, 12, 20, 40]"
                           style="text-align:center;margin-top:10px"
                           @current-change="load"
                           @size-change="load">
            </el-pagination>
        </div>

        <!-- 详情弹窗 -->
        <el-dialog :title="detail ? detail.name : '套餐详情'" :visible.sync="dialogVisible"
                   width="600px" top="7vh">
            <div v-if="detail" v-loading="detailLoading">
                <el-descriptions :column="2" border size="small">
                    <el-descriptions-item label="套餐编码">{{ detail.code }}</el-descriptions-item>
                    <el-descriptions-item label="分类">
                        {{ McdUtil.categoryText(detail.category) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="套餐价">
                        <span class="price-text">¥{{ McdUtil.money(detail.price) }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="单点原价">
                        ¥{{ McdUtil.money(detail.originalPrice) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="建议人数">{{ detail.peopleCount }} 人</el-descriptions-item>
                    <el-descriptions-item label="总热量">{{ detail.calories }} kcal</el-descriptions-item>
                    <el-descriptions-item label="辣度">{{ detail.spicyLevel }}</el-descriptions-item>
                    <el-descriptions-item label="适用时段">{{ detail.timeSlots }}</el-descriptions-item>
                    <el-descriptions-item label="销量">{{ detail.salesCount }} 份</el-descriptions-item>
                    <el-descriptions-item label="评分">{{ detail.rating }}</el-descriptions-item>
                    <el-descriptions-item label="立省" :span="2" v-if="detail.saveAmount">
                        <span style="color:#67C23A;font-weight:600">
                            ¥{{ McdUtil.money(detail.saveAmount) }}（{{ detail.discount }} 折）
                        </span>
                    </el-descriptions-item>
                </el-descriptions>

                <div style="margin-top:16px">
                    <div class="card-title" style="margin-bottom:10px">套餐包含</div>
                    <el-table :data="detail.items" size="mini" border style="width:100%">
                        <el-table-column prop="name" label="单品"></el-table-column>
                        <el-table-column label="分类" width="80">
                            <template slot-scope="s">
                                {{ McdUtil.productCategoryText(s.row.category) }}
                            </template>
                        </el-table-column>
                        <el-table-column label="单价" width="90">
                            <template slot-scope="s">¥{{ McdUtil.money(s.row.price) }}</template>
                        </el-table-column>
                        <el-table-column prop="calories" label="热量" width="80"></el-table-column>
                        <el-table-column prop="quantity" label="数量" width="60"></el-table-column>
                    </el-table>
                </div>
            </div>
        </el-dialog>
    </div>
    `,

    data() {
        return {
            McdUtil: McdUtil,
            loading: false,
            detailLoading: false,
            list: [],
            total: 0,
            dialogVisible: false,
            detail: null,
            query: {
                keyword: '',
                category: '',
                peopleCount: '',
                sortField: 'salesCount',
                sortOrder: 'desc',
                current: 1,
                size: 8
            },
            categories: [
                {value: 'CLASSIC', label: '经典组合'},
                {value: 'SPICY', label: '香辣组合'},
                {value: 'VALUE', label: '超值组合'},
                {value: 'FAMILY', label: '家庭分享'},
                {value: 'BREAKFAST', label: '早餐组合'},
                {value: 'TEA', label: '下午茶'},
                {value: 'DESSERT', label: '甜品'}
            ]
        };
    },

    created() {
        this.load();
    },

    methods: {
        load() {
            this.loading = true;
            McdApi.comboPage(this.query)
                .then(page => {
                    this.list = page.records || [];
                    this.total = page.total || 0;
                })
                .catch(err => this.$message.error(err.message || '加载失败'))
                .finally(() => this.loading = false);
        },

        reset() {
            this.query = {
                keyword: '', category: '', peopleCount: '',
                sortField: 'salesCount', sortOrder: 'desc',
                current: 1, size: 8
            };
            this.load();
        },

        showDetail(id) {
            this.dialogVisible = true;
            this.detailLoading = true;
            this.detail = null;
            McdApi.comboDetail(id)
                .then(d => this.detail = d)
                .catch(err => this.$message.error(err.message || '加载详情失败'))
                .finally(() => this.detailLoading = false);
        }
    }
});
