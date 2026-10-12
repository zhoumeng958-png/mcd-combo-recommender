/**
 * 登录 / 注册页
 */
Vue.component('page-login', {
    template: `
    <div class="login-wrap">
        <div class="login-box">
            <div class="login-brand">
                <div class="brand-icon">M</div>
                <h1>麦当劳套餐推荐助手</h1>
                <p>登录后即可保存偏好、查看你的专属推荐</p>
            </div>

            <el-tabs v-model="activeTab" stretch>
                <!-- 登录 -->
                <el-tab-pane label="登录" name="login">
                    <el-form :model="loginForm" :rules="loginRules" ref="loginForm"
                             size="medium" @keyup.enter.native="doLogin">
                        <el-form-item prop="username">
                            <el-input v-model="loginForm.username"
                                      prefix-icon="el-icon-user"
                                      placeholder="用户名" clearable></el-input>
                        </el-form-item>
                        <el-form-item prop="password">
                            <el-input v-model="loginForm.password"
                                      prefix-icon="el-icon-lock"
                                      type="password" placeholder="密码"
                                      show-password></el-input>
                        </el-form-item>
                        <el-button type="danger" style="width:100%"
                                   :loading="loading" @click="doLogin">
                            {{ loading ? '登录中…' : '登 录' }}
                        </el-button>
                    </el-form>

                    <div class="login-tip">
                        <i class="el-icon-info"></i>
                        演示账号：<b>admin</b> / <b>123456</b>
                        <el-button type="text" size="mini" @click="fillDemo">一键填入</el-button>
                    </div>
                </el-tab-pane>

                <!-- 注册 -->
                <el-tab-pane label="注册" name="register">
                    <el-form :model="regForm" :rules="regRules" ref="regForm"
                             size="medium" @keyup.enter.native="doRegister">
                        <el-form-item prop="username">
                            <el-input v-model="regForm.username"
                                      prefix-icon="el-icon-user"
                                      placeholder="用户名（3-20位）" clearable></el-input>
                        </el-form-item>
                        <el-form-item prop="nickname">
                            <el-input v-model="regForm.nickname"
                                      prefix-icon="el-icon-postcard"
                                      placeholder="昵称（可选）" clearable></el-input>
                        </el-form-item>
                        <el-form-item prop="password">
                            <el-input v-model="regForm.password"
                                      prefix-icon="el-icon-lock"
                                      type="password" placeholder="密码（6-32位）"
                                      show-password></el-input>
                        </el-form-item>
                        <el-form-item prop="confirm">
                            <el-input v-model="regForm.confirm"
                                      prefix-icon="el-icon-lock"
                                      type="password" placeholder="确认密码"
                                      show-password></el-input>
                        </el-form-item>
                        <el-button type="danger" style="width:100%"
                                   :loading="loading" @click="doRegister">
                            {{ loading ? '注册中…' : '注 册' }}
                        </el-button>
                    </el-form>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
    `,

    data() {
        return {
            activeTab: 'login',
            loading: false,
            loginForm: {username: '', password: ''},
            regForm: {username: '', nickname: '', password: '', confirm: ''},
            loginRules: {
                username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
                password: [
                    {required: true, message: '请输入密码', trigger: 'blur'},
                    {min: 6, max: 32, message: '密码长度需为 6-32 位', trigger: 'blur'}
                ]
            },
            regRules: {
                username: [
                    {required: true, message: '请输入用户名', trigger: 'blur'},
                    {min: 3, max: 20, message: '用户名长度需为 3-20 位', trigger: 'blur'}
                ],
                password: [
                    {required: true, message: '请输入密码', trigger: 'blur'},
                    {min: 6, max: 32, message: '密码长度需为 6-32 位', trigger: 'blur'}
                ],
                confirm: [
                    {required: true, message: '请再次输入密码', trigger: 'blur'},
                    {validator: (rule, value, cb) => {
                        if (value !== this.regForm.password) {
                            cb(new Error('两次输入的密码不一致'));
                        } else {
                            cb();
                        }
                    }, trigger: 'blur'}
                ]
            }
        };
    },

    methods: {
        fillDemo() {
            this.loginForm.username = 'admin';
            this.loginForm.password = '123456';
        },

        doLogin() {
            this.$refs.loginForm.validate(valid => {
                if (!valid) return;
                this.loading = true;
                McdApi.login(this.loginForm.username, this.loginForm.password)
                    .then(data => {
                        McdUtil.saveLogin(data);
                        this.$message.success('欢迎回来，' + (data.nickname || data.username));
                        this.$emit('login-success', data);
                    })
                    .catch(err => this.$message.error(err.message || '登录失败'))
                    .finally(() => this.loading = false);
            });
        },

        doRegister() {
            this.$refs.regForm.validate(valid => {
                if (!valid) return;
                this.loading = true;
                McdApi.register({
                    username: this.regForm.username,
                    password: this.regForm.password,
                    nickname: this.regForm.nickname
                })
                    .then(data => {
                        McdUtil.saveLogin(data);
                        this.$message.success('注册成功，已自动登录');
                        this.$emit('login-success', data);
                    })
                    .catch(err => this.$message.error(err.message || '注册失败'))
                    .finally(() => this.loading = false);
            });
        }
    }
});
