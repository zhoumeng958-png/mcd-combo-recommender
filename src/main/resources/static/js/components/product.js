/**
 * 单品浏览页
 */
Vue.component('page-product', {
    template: `
    <div>
        <h2 class="page-title">单品库</h2>
        <p class="page-desc">全部在售单品，套餐就是由这些单品组合而成</p>

        <el-card shadow="never" style="margin-bottom:16px">
            <el-form :inline="true" size="small">
                <el-form-item label="名称">
                    <el-input v-model="query.keyword" placeholder="输入单品名称"
                              clearable style="width:170px" @keyup.enter.native="load"></el-input>
                </el-form-item>
                <el-form-item label="分类">
                    <el-radio-group v-model="query.category" size="mini" @change="load">
                        <el-radio-button label="">全部</el-radio-button>
                        <el-radio-button v-for="c in categories" :key="c.value" :label="c.value">
                            {{ c.label }}
                        </el-radio-button>
                    </el-radio-group>
                </el-form-item>
                <el-form-item>
                    <el-button type="danger" icon="el-icon-search" @click="load">查询</el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <el-card shadow="never" v-loading="loading">
            <el-table :data="list" size="small" border stripe style="width:100%">
                <el-table-column prop="id" label="ID" width="60"></el-table-column>
                <el-table-column prop="name" label="单品名称" min-width="130"></el-table-column>
                <el-table-column label="分类" width="90">
                    <template slot-scope="s">
                        <el-tag size="mini" type="info">
                            {{ McdUtil.productCategoryText(s.row.category) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="单价" width="100">
                    <template slot-scope="s">
                        <span class="price-text">¥{{ McdUtil.money(s.row.price) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="calories" label="热量(kcal)" width="110"></el-table-column>
                <el-table-column label="辣度" width="130">
                    <template slot-scope="s">
                        <el-rate :value="s.row.spicyLevel" :max="3" disabled
                                 style="display:inline-block;vertical-align:middle"></el-rate>
                    </template>
                </el-table-column>
                <el-table-column label="口味标签" min-width="160">
                    <template slot-scope="s">
                        <el-tag v-for="t in McdUtil.tagList(s.row.tasteTags)" :key="t"
                                size="mini" style="margin-right:4px">{{ t }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="描述" min-width="160"
                                 show-overflow-tooltip></el-table-column>
            </el-table>

            <div v-if="!loading && !list.length" class="empty-box">
                <div class="empty-icon"><i class="el-icon-box"></i></div>
                <div>没有符合条件的单品</div>
            </div>

            <el-pagination v-if="total > 0"
                           background
                           layout="total, prev, pager, next, sizes"
                           :total="total"
                           :current-page.sync="query.current"
                           :page-size.sync="query.size"
                           :page-sizes="[10, 20, 50]"
                           style="margin-top:16px"
                           @current-change="load"
                           @size-change="load">
            </el-pagination>
        </el-card>
    </div>
    `,

    data() {
        return {
            McdUtil: McdUtil,
            loading: false,
            list: [],
            total: 0,
            query: {
                keyword: '',
                category: '',
                current: 1,
                size: 10
            },
            categories: [
                {value: 'HAMBURGER', label: '汉堡'},
                {value: 'SIDE', label: '小食'},
                {value: 'DRINK', label: '饮料'},
                {value: 'DESSERT', label: '甜品'},
                {value: 'BREAKFAST', label: '早餐'}
            ]
        };
    },

    created() {
        this.load();
    },

    methods: {
        load() {
            this.loading = true;
            McdApi.productPage(this.query)
                .then(page => {
                    this.list = page.records || [];
                    this.total = page.total || 0;
                })
                .catch(err => this.$message.error(err.message || '加载失败'))
                .finally(() => this.loading = false);
        }
    }
});
