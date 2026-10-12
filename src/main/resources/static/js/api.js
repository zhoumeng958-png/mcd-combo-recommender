/**
 * 后端接口封装
 */
window.McdApi = {

    /* ==================== 认证相关 ==================== */

    login(username, password) {
        return McdUtil.post('/api/auth/login', {username: username, password: password});
    },

    register(payload) {
        return McdUtil.post('/api/auth/register', payload);
    },

    logout() {
        return McdUtil.post('/api/auth/logout');
    },

    /** 用 token 换取当前用户信息（刷新页面时用） */
    me() {
        return McdUtil.get('/api/auth/me');
    },

    profile() {
        return McdUtil.get('/api/auth/profile');
    },

    updateProfile(payload) {
        return McdUtil.post('/api/auth/profile', payload);
    },

    changePassword(oldPassword, newPassword) {
        return McdUtil.request('POST',
            '/api/auth/change-password?oldPassword=' + encodeURIComponent(oldPassword)
            + '&newPassword=' + encodeURIComponent(newPassword));
    },

    onlineCount() {
        return McdUtil.get('/api/auth/online');
    },

    /* ==================== 推荐相关 ==================== */

    /** 智能推荐（核心接口，userId 由后端从登录态取） */
    recommend(payload) {
        return McdUtil.post('/api/recommend', payload);
    },

    /** 快速推荐（无需登录） */
    quickRecommend(params) {
        return McdUtil.get('/api/recommend/quick', params);
    },

    /** 按我保存的偏好一键推荐 */
    recommendByMyPreference() {
        return McdUtil.get('/api/recommend/by-preference');
    },

    /** 推荐历史记录（普通用户仅自己，管理员可传 all） */
    records(params) {
        return McdUtil.get('/api/recommend/records', params);
    },

    /* ==================== 套餐相关 ==================== */

    comboPage(params) {
        return McdUtil.get('/api/combo/page', params);
    },

    comboDetail(id) {
        return McdUtil.get('/api/combo/' + id);
    },

    comboList() {
        return McdUtil.get('/api/combo/list');
    },

    comboStat() {
        return McdUtil.get('/api/combo/stat');
    },

    /* ==================== 单品相关 ==================== */

    productPage(params) {
        return McdUtil.get('/api/product/page', params);
    },

    productList(category) {
        return McdUtil.get('/api/product/list', category ? {category: category} : {});
    },

    /* ==================== 用户偏好（自动绑定当前登录用户） ==================== */

    getMyPreference() {
        return McdUtil.get('/api/preference');
    },

    saveMyPreference(preference) {
        return McdUtil.post('/api/preference', preference);
    },

    removeMyPreference() {
        return McdUtil.del('/api/preference');
    }
};
