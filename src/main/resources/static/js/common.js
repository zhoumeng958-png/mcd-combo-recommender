/**
 * 通用工具方法
 */
window.McdUtil = {

    /** localStorage 中存放 token 的键名 */
    TOKEN_KEY: 'mcd_token',
    USER_KEY: 'mcd_user',

    /** 读取 token */
    getToken() {
        return localStorage.getItem(this.TOKEN_KEY) || '';
    },

    /** 保存 token 与用户信息 */
    saveLogin(data) {
        localStorage.setItem(this.TOKEN_KEY, data.token || '');
        localStorage.setItem(this.USER_KEY, JSON.stringify({
            userId: data.userId,
            username: data.username,
            nickname: data.nickname,
            role: data.role,
            avatar: data.avatar
        }));
    },

    /** 读取当前用户信息 */
    getUser() {
        try {
            const raw = localStorage.getItem(this.USER_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch (e) {
            return null;
        }
    },

    /** 清除登录态 */
    clearLogin() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    },

    /** 是否已登录 */
    isLogin() {
        return !!this.getToken();
    },

    /** 是否管理员 */
    isAdmin() {
        const u = this.getUser();
        return !!u && u.role === 'ADMIN';
    },

    /** 未登录时清理本地登录态（登录页由 Vue 的 logged 状态控制，无需跳转） */
    redirectToLogin() {
        this.clearLogin();
    },

    /**
     * 统一的请求封装，直接返回后端 Result.data
     * 后端统一响应格式：{ code, message, data, timestamp }
     */
    request(method, url, data, params) {
        let fullUrl = url;
        if (params && Object.keys(params).length) {
            const qs = Object.keys(params)
                .filter(k => params[k] !== undefined && params[k] !== null && params[k] !== '')
                .map(k => encodeURIComponent(k) + '=' + encodeURIComponent(params[k]))
                .join('&');
            if (qs) {
                fullUrl += (url.indexOf('?') >= 0 ? '&' : '?') + qs;
            }
        }

        const headers = {'Content-Type': 'application/json;charset=UTF-8'};

        // 自动携带 token
        const token = this.getToken();
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        const options = {method: method, headers: headers};
        if (data !== undefined && data !== null && method !== 'GET') {
            options.body = JSON.stringify(data);
        }

        return fetch(fullUrl, options)
            .then(resp => {
                // 401 统一跳登录页
                if (resp.status === 401) {
                    this.redirectToLogin();
                    throw new Error('登录已过期，请重新登录');
                }
                if (!resp.ok) {
                    throw new Error('HTTP ' + resp.status + ' ' + resp.statusText);
                }
                return resp.json();
            })
            .then(json => {
                if (json.code !== 200) {
                    throw new Error(json.message || '请求失败');
                }
                return json.data;
            });
    },

    get(url, params) {
        return this.request('GET', url, null, params);
    },

    post(url, data) {
        return this.request('POST', url, data);
    },

    put(url, data) {
        return this.request('PUT', url, data);
    },

    del(url) {
        return this.request('DELETE', url);
    },

    /**
     * 金额格式化
     */
    money(v) {
        if (v === null || v === undefined || v === '') return '-';
        return Number(v).toFixed(2);
    },

    /** 时段码 -> 中文 */
    timeSlotText(code) {
        const map = {
            BREAKFAST: '早餐', LUNCH: '午餐', TEA: '下午茶',
            DINNER: '晚餐', LATE_NIGHT: '夜宵', ALL_DAY: '全天'
        };
        return map[code] || code || '-';
    },

    /** 套餐分类码 -> 中文 */
    categoryText(code) {
        const map = {
            CLASSIC: '经典组合', SPICY: '香辣组合', VALUE: '超值组合',
            FAMILY: '家庭分享', BREAKFAST: '早餐组合', TEA: '下午茶', DESSERT: '甜品'
        };
        return map[code] || code || '-';
    },

    /** 单品分类码 -> 中文 */
    productCategoryText(code) {
        const map = {
            HAMBURGER: '汉堡', SIDE: '小食', DRINK: '饮料',
            DESSERT: '甜品', BREAKFAST: '早餐'
        };
        return map[code] || code || '-';
    },

    /** 策略名称 -> 中文 */
    strategyText(key) {
        const map = {
            budget: '预算匹配', taste: '口味匹配', people: '人数适配',
            timeSlot: '时段适配', popularity: '人气热度', diversity: '多样性'
        };
        return map[key] || key;
    },

    /** 标签串 -> 数组 */
    tagList(str) {
        if (!str) return [];
        return String(str).split(/[,，]/).map(s => s.trim()).filter(s => s);
    }
};
