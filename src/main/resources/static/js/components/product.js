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
                              clearable style="width:170px" @keyup.enter.native="onSearch"></el-input>
                </el-form-item>
                <el-form-item label="分类">
                    <el-radio-group v-model="query.category" size="mini" @change="onSearch">
                        <el-radio-button label="">全部</el-radio-button>
                        <el-radio-button v-for="c in categories" :key="c.value" :label="c.value">
                            {{ c.label }}
                        </el-radio-button>
                    </el-radio-group>
                </el-form-item>
                <el-form-item>
                    <el-button type="danger" icon="el-icon-search" @click="onSearch">查询</el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <el-card shadow="never" v-loading="loading">
            <el-table :data="list" row-key="id" size="small" border stripe style="width:100%">
                <el-table-column type="index" label="序号" width="60"
                                 :index="rowIndex"></el-table-column>
                <el-table-column prop="name" label="单品名称" min-width="130"></el-table-column>
                <el-table-column label="分类" width="90">
                    <template slot-scope="s">
                        <el-tag size="mini"
                                :class="['cat-tag', McdUtil.productCategoryClass(s.row.category)]">
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
        /** 序号：跨分页连续（第 2 页从 11 开始），而不是数据库主键 */
        rowIndex(i) {
            const cur = Number(this.query.current) || 1;
            const size = Number(this.query.size) || 10;
            return (cur - 1) * size + i + 1;
        },

        /** 筛选条件变化：必须先回到第 1 页再查。
         *  否则在第 2 页切到结果更少的分类时，请求的是「第 2 页」，
         *  分页器会跳回第 1 页但表格不会重新加载，页面一直是空的。 */
        onSearch() {
            this.query.current = 1;
            this.load();
        },

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
