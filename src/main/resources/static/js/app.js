/**
 * 应用入口
 */
new Vue({
    el: '#app',

    data: {
        /** 是否已登录 */
        logged: false,
        /** 当前用户信息 */
        user: null,
        /** 当前页 */
        currentPage: 'recommend',
        pages: [
            {key: 'recommend', label: '智能推荐', icon: 'el-icon-magic-stick'},
            {key: 'combo', label: '套餐列表', icon: 'el-icon-food'},
            {key: 'product', label: '单品库', icon: 'el-icon-dish'},
            {key: 'dashboard', label: '数据概览', icon: 'el-icon-data-analysis'}
        ]
    },

    computed: {
        currentComponent() {
            return 'page-' + this.currentPage;
        },
        displayName() {
            if (!this.user) return '';
            return this.user.nickname || this.user.username;
        },
        isAdmin() {
            return !!this.user && this.user.role === 'ADMIN';
        }
    },

    created() {
        // 页面刷新后，若本地有 token 则向后端确认登录态是否仍然有效
        this.restoreSession();
    },

    methods: {
        /**
         * 恢复登录态：
         * 本地有 token 不代表一定有效（可能已过期或服务重启），
         * 因此必须调 /api/auth/me 校验一次。
         */
        restoreSession() {
            if (!McdUtil.isLogin()) {
                this.logged = false;
                return;
            }
            McdApi.me()
                .then(data => {
                    this.user = data;
                    this.logged = true;
                })
                .catch(() => {
                    this.logged = false;
                    McdUtil.clearLogin();
                });
        },

        onLoginSuccess(data) {
            this.user = {
                userId: data.userId,
                username: data.username,
                nickname: data.nickname,
                role: data.role
            };
            this.logged = true;
            this.currentPage = 'recommend';
        },

        doLogout() {
            this.$confirm('确定要退出登录吗？', '提示', {
                confirmButtonText: '退出',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                McdApi.logout()
                    .catch(() => {
                        // 即使后端调用失败也照常清理本地登录态
                    })
                    .finally(() => {
                        McdUtil.clearLogin();
                        this.logged = false;
                        this.user = null;
                        this.$message.success('已退出登录');
                    });
            }).catch(() => {
            });
        },

        switchPage(key) {
            this.currentPage = key;
            window.scrollTo({top: 0, behavior: 'smooth'});
        }
    },

    mounted() {
        // 未登录时不探测后端，避免无意义的 401 提示
        if (McdUtil.isLogin()) {
            McdUtil.get('/api/combo/stat')
                .then(() => console.log('%c后端连接正常', 'color:#67C23A;font-weight:bold'))
                .catch(err => console.warn('后端连接检查:', err.message));
        }
    }
});
