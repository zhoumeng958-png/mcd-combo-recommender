# 麦当劳套餐推荐助手（mcd-combo-recommender）

基于 **Spring Boot + 可插拔规则策略引擎** 的麦当劳套餐智能推荐系统。

用户输入「预算 / 口味偏好 / 忌口 / 就餐人数 / 时段」，服务对套餐库中所有候选套餐逐项打分、
加权汇总，输出带**推荐理由**和**得分明细**的套餐榜单。

**架构：前后端不分离**——前端页面由 Spring Boot 内置的静态资源目录直接提供，
依赖库（Vue / Element UI）已本地化，**断网也能完整演示**。

---

## 📁 参赛文件导航

本项目为「麦当劳程序员节创意开发大赛」参赛作品，仓库包含以下文件：

| 文件 | 说明 |
| --- | --- |
| [README.md](README.md) | 项目介绍、安装方法、使用示例、目标用户（本文件） |
| [CONTEST_DECLARATION.md](CONTEST_DECLARATION.md) | 参赛声明（官方原文，内容未做任何改动） |
| [MCP_INTEGRATION.md](MCP_INTEGRATION.md) | 麦当劳 MCP Server / Tool / 调用流程 / 业务价值说明 |
| [ENV.md](ENV.md) | 环境变量说明（数据库、MCP Token 的配置方式） |
| [workbuddy.md](workbuddy.md) | 使用 WorkBuddy 开发的完整对话上下文（WorkBuddy 专项奖励核验用） |
| [SUBMIT_GUIDE.md](SUBMIT_GUIDE.md) | 参赛提交流程（官方要求对照版） |
| `src/` | 项目主体源代码 |

---

## 一、技术栈

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 1.8 | 兼容性优先 |
| Spring Boot | 2.7.18 | 最后一条支持 Java 8 的版本线 |
| MyBatis-Plus | 3.5.5 | 单表 CRUD + 分页插件 + 逻辑删除 |
| MySQL | 8.0 | 数据存储 |
| Knife4j | 4.4.0 | 在线接口文档（基于 OpenAPI3） |
| Vue | 2.7.16 | 前端框架（本地 lib，无需 npm） |
| Element UI | 2.15.14 | 前端组件库（本地 lib） |
| Lombok | - | 简化实体代码 |
| Spring Security Crypto | 5.7.12 | 仅引入 BCrypt 密码加密（不引入整套 Spring Security） |
| JUnit 5 | - | 策略引擎单元测试 |

> 前端走的是「原生引入 + 本地 lib」的轻量路线：没有 Node 构建步骤，改完 JS 刷新浏览器即生效，
> 适合比赛演示，也避免了 npm 环境问题。
>
> 登录态采用 **Token（UUID）+ ThreadLocal 上下文** 的无状态方案，**不引入 Redis**，
> token 保存在应用内存的 `ConcurrentHashMap` 中，服务重启后自然失效。

---

## 二、目录结构

```
MMdang
├── pom.xml
├── README.md                       项目说明（本文件）
├── CONTEST_DECLARATION.md          参赛声明（官方原文）
├── MCP_INTEGRATION.md              麦当劳 MCP 集成说明
├── ENV.md                          环境变量说明
├── workbuddy.md                    WorkBuddy 开发对话上下文
└── src
    ├── main
    │   ├── java/com/mcd/recommender
    │   │   ├── McRecommenderApplication.java   启动类
    │   │   ├── common/                         统一响应、异常处理
    │   │   │   ├── Result.java                 统一返回体
    │   │   │   ├── ResultCode.java             业务状态码
    │   │   │   ├── BusinessException.java      业务异常
    │   │   │   ├── GlobalExceptionHandler.java 全局异常处理
    │   │   │   └── PageResult.java             分页返回体
    │   │   ├── config/                         配置类
    │   │   │   ├── MybatisPlusConfig.java      分页插件 + 字段自动填充
    │   │   │   ├── Knife4jConfig.java          接口文档
    │   │   │   ├── CorsConfig.java             跨域
    │   │   │   ├── WebMvcConfig.java           拦截器注册（放行名单）
    │   │   │   ├── PasswordConfig.java         BCrypt 编码器
    │   │   │   └── RecommenderProperties.java  推荐引擎可调参数
    │   │   ├── security/                       ★ 登录鉴权
    │   │   │   ├── LoginUser.java              登录用户（轻量身份）
    │   │   │   ├── UserContext.java            ThreadLocal 当前用户
    │   │   │   ├── TokenManager.java           token 签发 / 校验 / 注销
    │   │   │   └── AuthInterceptor.java        登录拦截器（401 出口）
    │   │   ├── controller/                     接口层
    │   │   ├── service/ + service/impl/        业务层
    │   │   ├── mapper/                         持久层（MyBatis-Plus）
    │   │   ├── entity/                         数据库实体
    │   │   ├── dto/                            入参对象
    │   │   ├── vo/                             出参对象
    │   │   ├── enums/                          枚举（分类 / 时段 / 热量档位）
    │   │   ├── util/TagUtils.java              标签工具
    │   │   └── strategy/                       ★ 推荐策略引擎
    │   │       ├── RecommendStrategy.java      策略接口
    │   │       ├── RecommendContext.java       推荐上下文
    │   │       ├── ScoredCombo.java            打分结果
    │   │       ├── StrategyEngine.java         引擎（加权汇总）
    │   │       └── impl/                       7 个内置策略
    │   └── resources
    │       ├── application.yml                 主配置
    │       ├── application-dev.yml             开发环境（数据库）
    │       ├── db/schema.sql                   建表脚本
    │       ├── db/data.sql                     初始化数据（32 单品 + 14 套餐）
    │       └── mapper/ComboMapper.xml          统计查询 XML 示例
    │   └── test/java/com/mcd/recommender
    │       └── StrategyEngineTest.java             引擎单元测试
    └── resources
        ├── application.yml                         主配置
        ├── application-dev.yml                     开发环境（数据库，密码走环境变量）
        ├── application-local.yml                   本地覆盖（不提交，见 .gitignore）
        ├── db/schema.sql                           建表脚本
        ├── db/data.sql                             初始化数据（32 单品 + 14 套餐 + 2 用户）
        ├── mapper/ComboMapper.xml                  统计查询 XML 示例
        └── static/                                 ★ 前端页面（前后端不分离）
            ├── index.html                          页面入口
            ├── css/app.css                         全局样式（麦当劳红黄主题）
            ├── js/common.js                        工具方法
            ├── js/api.js                           接口封装
            ├── js/app.js                           Vue 实例与导航
            ├── js/components/
            │   ├── recommend.js                    智能推荐页
            │   ├── combo.js                        套餐列表页
            │   ├── product.js                      单品库页
            │   └── dashboard.js                    数据概览页
            └── lib/                                前端依赖（已本地化，离线可用）
                ├── vue.min.js                      Vue 2.7.16
                ├── element-ui.js                   Element UI 2.15.14
                ├── element-ui.css
                └── fonts/                          图标字体
```


---

## 三、前端页面

前端由 **1 个登录页 + 4 个业务页面** 组成，全部由 Spring Boot 静态资源目录直接提供，
**不需要单独启动前端服务**。

| 页面 | 文件 | 功能 |
| --- | --- | --- |
| 登录 / 注册 | `components/login.js` | 登录 / 注册双 Tab，表单校验，一键填入演示账号；登录成功写入 token 并切换到业务界面 |
| 智能推荐 | `components/recommend.js` | 填写偏好（预算区间 / 人数 / 时段 / 口味 / 忌口 / 辣度 / 热量偏好），得到带推荐理由与得分明细的榜单，Top1 金色高亮 |
| 套餐列表 | `components/combo.js` | 卡片式浏览全部套餐，支持关键词 / 分类 / 人数筛选与多维排序，点击查看详情弹窗 |
| 单品库 | `components/product.js` | 表格展示全部单品，含价格、热量、辣度星级、口味标签，支持分类切换 |
| 数据概览 | `components/dashboard.js` | 指标卡 + 分类统计表 + 销量占比条形 + 最近推荐记录 |

访问方式：启动后打开 **http://localhost:8080/index.html**（或 http://localhost:8080/ 会自动跳转）。
首次访问会看到登录页，用 `admin / 123456` 登录即可进入业务界面。

### 前端目录说明

```
static/
├── index.html              页面入口，未登录显示登录页，已登录显示业务界面
├── css/app.css             全局样式（麦当劳红 #DA291C / 黄 #FFC72C）
├── js/
│   ├── common.js           工具方法：fetch 封装（自动带 token、401 处理）、格式化
│   ├── api.js              接口封装，与后端 REST API 一一对应
│   ├── app.js              Vue 根实例、登录态管理、顶部导航、页面切换
│   └── components/         登录页 + 4 个业务页面组件（Vue.component 全局注册）
└── lib/                    Vue + Element UI + 图标字体（本地化，离线可用）
```

### 技术取舍

- **不用 Vue CLI / webpack**：没有构建步骤，改完 JS 刷新即生效，演示时不担心 `npm install` 出错
- **依赖库本地化**：Vue、Element UI、图标字体全部下载到 `lib/`，比赛现场断网也能跑
- **组件用 `Vue.component` 全局注册**：不需要 `.vue` 单文件组件和编译，纯浏览器可运行
- **登录页与业务界面由 `logged` 状态切换**：不做前端路由，避免多余的复杂度

---

## 四、快速开始

> **当前状态：数据库已建好、数据已灌入、项目已编译通过、接口已实测可用。**
> 直接跳到「### 3. 启动」即可。

### 1. 数据库（已完成）

数据库名 **`Mdangdang`**（连接账号通过环境变量注入，见 [ENV.md](ENV.md)），已建好 6 张表并灌入初始数据：

| 表 | 条数 |
| --- | --- |
| mcd_product（单品） | 32 |
| mcd_combo（套餐） | 14 |
| mcd_combo_item（套餐明细） | 48 |
| mcd_user（用户） | 2（`admin` / `demo`，密码均为 `123456`） |
| mcd_preference（用户偏好） | 2（分别绑定 admin / demo） |
| mcd_recommend_record（推荐记录） | 运行时自动写入 |

如需重建，执行：

```bash
cd src/main/resources/db
# 请将 your_password 替换为本地 MySQL 密码
mysql -uroot -pyour_password --default-character-set=utf8mb4 < schema.sql
mysql -uroot -pyour_password --default-character-set=utf8mb4 < data.sql
```

> 脚本使用 `CREATE TABLE IF NOT EXISTS` + `INSERT IGNORE` / `REPLACE INTO`，可重复执行不会产生脏数据。
> 也可以把 `application-dev.yml` 里 `spring.sql.init.mode` 改成 `always`，让程序启动时自动执行。
> 初始账号的密码以 BCrypt 哈希形式存储（`$2a$10$...`），**无法从库中反推明文**。

### 2. 数据库连接（已配置）

`src/main/resources/application-dev.yml` 中的数据库配置已**环境变量化**，
仓库里不包含任何真实密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${MCD_DB_HOST:localhost}:${MCD_DB_PORT:3306}/${MCD_DB_NAME:Mdangdang}?...
    username: ${MCD_DB_USER:root}
    password: ${MCD_DB_PASSWORD:}      # 由环境变量注入
```

| 环境变量 | 默认值 |
| --- | --- |
| `MCD_DB_HOST` / `MCD_DB_PORT` / `MCD_DB_NAME` | `localhost` / `3306` / `Mdangdang` |
| `MCD_DB_USER` | `root` |
| `MCD_DB_PASSWORD` | 无（必须自行设置） |

设置方式二选一：

```bash
# 方式一：环境变量
export MCD_DB_PASSWORD=你的密码
mvn spring-boot:run
```

```bash
# 方式二：本地覆盖文件（已在 .gitignore 中排除）
# 创建 src/main/resources/application-local.yml 写入真实密码即可
# application.yml 默认激活 dev,local 两个 profile，文件存在即自动生效
```

> ⚠️ 方式二依赖 `spring.profiles.active` 中包含 `local`（本项目已默认配置）。
> **不要用 `spring.config.import` 引入该文件**——它执行时机早于 profile 处理，
> 会导致文件以低优先级加载并被去重跳过，密码永远不生效，详见 [ENV.md](ENV.md)。

详见 **[ENV.md](ENV.md)**。

### 3. 启动

```bash
mvn spring-boot:run
```

启动成功后：

- 接口文档：http://localhost:8080/doc.html
- Swagger：http://localhost:8080/swagger-ui.html

> **注意**：如果所在环境注入了 `SERVER__PORT` 环境变量（Spring Boot 宽松绑定会把它当成
> `server.port`，且优先级高于配置文件），启动端口会被改写。此时显式指定端口即可：
> `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080`


---

## 五、接口清单

> 除标注「公开」的接口外，其余接口均需在请求头携带 `Authorization: Bearer <token>`，
> 否则返回 HTTP 401。推荐记录与用户偏好会自动绑定当前登录用户，**前端无需再传 userId**。

### 5.1 登录认证（`/api/auth`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | **公开**。登录，返回 token（默认账号 `admin` / `123456`） |
| POST | `/api/auth/register` | **公开**。注册，成功后直接返回 token |
| POST | `/api/auth/logout` | 注销当前 token，立即失效 |
| GET | `/api/auth/me` | 用 token 换取当前登录用户（前端刷新页面时校验会话） |
| GET | `/api/auth/profile` | 当前用户完整资料 |
| POST | `/api/auth/profile` | 更新个人资料（强制只改自己） |
| POST | `/api/auth/change-password` | 修改密码（参数 `oldPassword` / `newPassword`） |
| GET | `/api/auth/online` | 当前在线用户数（演示用） |

### 5.2 业务接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/recommend` | **核心推荐接口**，传偏好返回榜单（userId 由登录态取） |
| GET | `/api/recommend/quick` | **公开**。快速推荐（浏览器可直接调试） |
| GET | `/api/recommend/by-preference` | 按「我」已保存偏好一键推荐 |
| GET | `/api/recommend/records` | 推荐历史记录（普通用户仅自己；管理员可传 `all=true` 查全部） |
| GET | `/api/combo/page` | 套餐分页查询（筛选 + 排序） |
| GET | `/api/combo/{id}` | 套餐详情（含单品明细、立省金额、折扣） |
| GET | `/api/combo/list` | 全部上架套餐 |
| GET | `/api/combo/stat` | 套餐分类统计 |
| POST/PUT/DELETE | `/api/combo` | 套餐增改删 |
| GET | `/api/product/page` | 单品分页查询 |
| GET | `/api/product/list?category=` | 按分类查单品 |
| POST/PUT/DELETE | `/api/product` | 单品增改删 |
| GET | `/api/preference` | 查询「我」的偏好 |
| POST | `/api/preference` | 保存/更新「我」的偏好 |
| DELETE | `/api/preference` | 删除「我」的偏好 |

### 请求示例

先登录拿 token：

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' \
  | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
```

再带 token 调用推荐：

```bash
curl -X POST http://localhost:8080/api/recommend \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
        "budgetMin": 30,
        "budgetMax": 60,
        "tasteTags": ["经典", "牛肉"],
        "avoidTags": ["香辣"],
        "peopleCount": 1,
        "timeSlot": "LUNCH"
      }'
```

关于几个参数：

| 参数 | 说明 |
| --- | --- |
| `budgetMin` / `budgetMax` | **硬边界**。价格不在区间内的套餐直接淘汰、不参与打分，绝不会出现在结果里 |
| `calorieLevel` | 可选，`LOW`=热量偏低 / `HIGH`=热量偏高，不传=不限。分界线由 `recommender.calorie-threshold` 配置（默认 **1000 kcal**） |
| `maxCalories` | 可选，精确热量上限（kcal）。一般用 `calorieLevel` 即可，这个留给需要精确控制的场景 |
| `topN` | 可选，语义是「**最多**返回几条」，不是「必须凑够几条」。不传取 `recommender.top-n`（默认 5）；符合条件的套餐不足 5 个时**有多少给多少**，不会为凑数把区间外的补进来 |

> `userId` 字段已改为**只读且隐藏**，由后端从登录态注入，手写也会被覆盖。

### 响应示例（实测结果，节选）

下面是上面那条请求的真实返回（预算 ¥30-60 硬过滤后剩 11 个候选，取前 2 名展示）：

```json
{
  "code": 200,
  "message": "推荐成功",
  "data": {
    "userId": "admin",
    "timeSlot": "LUNCH",
    "totalCandidates": 11,
    "costMs": 7,
    "summary": "当前是「午餐」时段，预算 ¥30-¥60，1 人用餐，为你精选 5 个套餐，最推荐「巨无霸中套餐」，最高综合得分 91.00 分。",
    "items": [
      {
        "rank": 1,
        "score": 91.0,
        "reasons": [
          "性价比不错：每元能买到 25.3 kcal",
          "口味契合：经典、牛肉",
          "分量刚好适合 1 人",
          "人气爆款：累计售出 12000 份，评分 4.8"
        ],
        "scoreDetail": {
          "value": 76.44, "taste": 100.0, "people": 100.0,
          "timeSlot": 90.0, "popularity": 97.01, "diversity": 70.91
        },
        "combo": { "id": 1, "name": "巨无霸中套餐", "price": 43.50, "...": "..." }
      },
      {
        "rank": 2,
        "score": 89.66,
        "reasons": [
          "口味契合：经典、牛肉",
          "分量刚好适合 1 人",
          "人气爆款：累计售出 8000 份，评分 4.6"
        ],
        "scoreDetail": {
          "value": 72.22, "taste": 100.0, "people": 100.0,
          "timeSlot": 90.0, "popularity": 92.88, "diversity": 70.91
        },
        "combo": { "id": 4, "name": "双层吉士中套餐", "price": 40.50, "...": "..." }
      }
    ]
  }
}
```

> 注意请求里带了 `avoidTags: ["香辣"]`，因此香辣口味的套餐被压到榜外——
> 这正是口味策略的忌口扣分在起作用。
>
> 另外 `totalCandidates` 只有 11（套餐库共 14 个），是因为预算区间是硬过滤，
> 价格不在 ¥30-60 的 3 个套餐在打分之前就被淘汰了。


---

## 六、登录与用户体系

### 6.1 为什么需要一张用户表

早期版本用前端硬编码的 `userId: "web_user"` 来区分偏好和推荐记录，等于「谁调接口都是同一个人」。
接入登录后，`userId` 全部改为 **当前登录用户的用户名**，因此确实需要在数据库里建一张用户表
（`mcd_user`）来存账号、密码哈希、角色等信息。

### 6.2 表结构

```sql
CREATE TABLE `mcd_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(64)  NOT NULL COMMENT '登录名',
    `password`    VARCHAR(128) NOT NULL COMMENT '密码（BCrypt 哈希，不可逆）',
    `nickname`    VARCHAR(64)  DEFAULT NULL,
    `phone`       VARCHAR(20)  DEFAULT NULL,
    `email`       VARCHAR(64)  DEFAULT NULL,
    `avatar`      VARCHAR(255) DEFAULT NULL,
    `role`        VARCHAR(32)  DEFAULT 'USER' COMMENT 'USER / ADMIN',
    `status`      TINYINT      DEFAULT 1 COMMENT '1 正常 / 0 停用',
    `last_login`  DATETIME     DEFAULT NULL,
    `deleted`     TINYINT      DEFAULT 0,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) COMMENT ='用户表';
```

### 6.3 默认账号

| 用户名 | 密码 | 角色 | 说明 |
| --- | --- | --- | --- |
| `admin` | `123456` | ADMIN | 可查看全体推荐记录（`all=true`） |
| `demo` | `123456` | USER | 普通用户，只能看自己的数据 |

> 两个账号共用同一段 BCrypt 哈希（同一个明文 `123456`），
> 哈希值形如 `$2a$10$...`，**无法反推明文**。

### 6.4 登录态方案（Token + ThreadLocal）

```
① 前端 POST /api/auth/login  ──► 校验密码（BCrypt.matches）
                                     ↓
                              签发 UUID token，存入 ConcurrentHashMap
                                     ↓
                              返回 { token, expiresIn, userId, username, role }
                                     ↓
② 前端把 token 存 localStorage，之后每次请求带 Authorization: Bearer <token>
                                     ↓
③ AuthInterceptor 校验 token ──► 写入 UserContext（ThreadLocal）──► 放行
                                     ↓
④ 业务层用 UserContext.currentUsername() 取当前用户，无需透传参数
                                     ↓
⑤ 请求结束 afterCompletion 清理 ThreadLocal（防止线程复用串号）
```

关键设计点：

- **无状态**：不依赖 session，token 存在应用内存里，**不引入 Redis**；服务重启后所有 token 自然失效。
- **统一出口**：拦截器直接写出 401 的 JSON（拦截器抛出的异常不会被 `@RestControllerAdvice` 捕获），
  而业务层抛出的 401/403 由全局异常处理器映射成对应的 HTTP 状态码。
- **越权防护**：偏好、资料、密码等接口一律使用当前登录用户的身份，
  **不接受前端传入的 userId/id**，从根源上杜绝改他人数据。

### 6.5 放行名单

以下接口无需登录：`/api/auth/login`、`/api/auth/register`、`/api/recommend/quick`。
其余 `/api/**` 均需携带有效 token。

### 6.6 安全说明

- 密码用 **BCrypt** 单向哈希存储（`spring-security-crypto`），即使数据库泄露也无法还原明文。
- 登录失败统一返回「用户名或密码错误」，**不区分**是账号不存在还是密码错误，避免账号枚举。
- 被停用账号（`status=0`）登录会被拦截，返回 `USER_DISABLED`。


---

## 七、推荐算法说明

### 7.1 整体流程

```
请求参数
   ↓
① 解析时段（显式指定 > 按当前时间推断）
   ↓
② 取候选套餐（status=1 上架）
   ↓
③ 硬性过滤（预算区间 / 热量档位，不满足直接淘汰、不参与打分）
   ↓
④ 策略引擎：7 个策略依次给每个候选打 0-100 的「单项分」
   ↓
⑤ 加权平均 → 综合得分（0-100）
   ↓
⑥ 得分倒序，取 TopN（不足则全给），装配推荐理由与明细
   ↓
⑦ 推荐记录落库（失败不影响主流程）
```

> **为什么预算要做硬过滤而不是只打分**：用户把滑块拖到 ¥15-35，就该只看到 ¥15-35 的套餐。
> 若只靠打分扣分，区间外的套餐仍可能因其他维度得分高而挤进榜单，
> 用户看到的就是「系统没按我选的价格来」。
>
> 代价是：过滤之后所有候选都在预算内，再拿「是否在预算内」打分就恒为满分——
> 既不影响排序，还会把所有分数往 100 拉、压缩其他维度的分差。
> 所以**预算不再是打分维度**（它是门槛），「价格」这一维的打分交给 **性价比（value）** 策略：
> 在预算内比较谁更划算。实测下来分差也从 6.31 拉大到 7.60，其他维度的区分度回来了。

### 7.2 内置策略

| 策略 | 权重 key | 默认权重 | 打分逻辑 |
| --- | --- | --- | --- |
| 性价比 | `value` | 1.0 | 按「每元热量」（kcal/元）打分：≤20 记 50 分、≥30 记 100 分，中间线性插值（区间可在 yml 调）。候选里相对最优的加理由「性价比最高」，达到区间中点的加「性价比不错」 |
| 口味匹配 | `taste` | 1.2 | 基准 50 分 + 命中率 × 50；每个忌口标签扣 45 分 |
| 人数适配 | `people` | 1.0 | 人数差 0/1/2/其他 → 100/70/40/15 分 |
| 时段适配 | `timeSlot` | 0.8 | 精确匹配 100 分，全天供应 90 分，不匹配 25 分 |
| 人气热度 | `popularity` | 0.6 | 销量对数归一化 × 0.6 + 评分归一化 × 0.4 |
| 多样性 | `diversity` | 0.4 | 主料在候选集中占比越低分越高，避免同质化 |
| 儿童友好 | `childFriendly` | 0.9 | **仅当请求带儿童时生效**。基准 62 分；甜/清淡/醇香/芝士 +12，香辣/咸香/厚实 −22；辣度 ≥2 再 −25；套餐含甜品 +15；套餐名含「儿童/开心乐园」+20 |

> **预算不在上表里**——它是第 ③ 步的硬门槛，不是一个打分维度。
>
> **性价比为什么不用「候选集内归一化」**：候选只剩两三个时（比如预算收窄到 ¥50-60），
> 两个套餐每元热量只差 1.6%，归一化却会把它们拉成 100 分和 50 分，凭空造出巨大差距。
> 用固定参考区间则「差多少分多少分」，更可预期、也更好解释。

**加权公式**

```
综合得分 = Σ(策略单项分 × 策略权重) / Σ(生效策略权重)
```

> 若某策略的前置条件不满足（例如用户没填预算），该策略**不生效**，
> 既不打分也不计入权重分母，避免「中性分」稀释结果。

### 7.3 权重调优

所有权重都在 `application.yml` 里，改完重启即可，无需改代码：

```yaml
recommender:
  # 最多返回几条（页面已不提供该选项；符合条件的套餐不足时有多少给多少）
  top-n: 5
  # 热量档位分界线（kcal）：≤ 该值算「热量偏低」，> 该值算「热量偏高」
  calorie-threshold: 1000
  # 性价比策略的「每元热量」参考区间（kcal/元）：≤20 记 50 分，≥30 记 100 分
  value-kcal-per-yuan-min: 20
  value-kcal-per-yuan-max: 30
  weights:
    value: 1.0            # 性价比（预算内每元热量）
    taste: 1.2
    people: 1.0
    time-slot: 0.8
    popularity: 0.6
    diversity: 0.4
    child-friendly: 0.9   # 仅当请求带儿童时生效
```

---

## 八、如何扩展一个新策略

三步即可，引擎会自动发现并执行：

1. 在 `strategy/impl/` 下新建类，实现 `RecommendStrategy`：

```java
@Component
public class HealthyStrategy implements RecommendStrategy {

    @Override
    public String name() {
        return "healthy";           // 与 yml 中权重 key 一致
    }

    @Override
    public int order() {
        return 55;                  // 决定执行顺序
    }

    @Override
    public boolean score(RecommendContext context) {
        for (Combo combo : context.getCandidates()) {
            double score = combo.getCalories() <= 800 ? 100 : 40;
            context.addScore(combo.getId(), name(), score);
            if (score == 100) {
                context.addReason(combo.getId(), "低热量，适合轻食需求");
            }
        }
        return true;                // 返回 false 表示本次不参与
    }
}
```

2. 在 `application.yml` 的 `recommender.weights` 里加一行 `healthy: 0.7`。

3. 重启，完成。

> ⚠️ **权重 key 有个坑**：`weights` 是 `Map<String, Double>`，而 Spring 的**宽松绑定对 Map 的键不生效**。
> 配置里写 `time-slot`（kebab-case）而策略名是 `timeSlot`（camelCase）时，
> 直接 `containsKey` 查不到，权重会被**静默忽略**并回退成默认值 1.0——不报错，只是分数不对。
> `RecommenderProperties.weightOf` 已做归一化匹配（去掉 `-` / `_` 再转小写），两种写法都能生效；
> 但新增策略时建议**让 key 与策略名保持一致**，省得排查。

---

## 九、常见问题

**Q：在 Windows 中打开 README，中文显示乱码？**
A：本文件使用 **UTF-8** 编码，并带有 BOM 编码标记，便于旧版 Windows 编辑器识别。
若仍显示乱码，请在编辑器中选择「以 UTF-8 编码重新打开」，确认中文恢复后再保存。
不要直接保存乱码内容，以免覆盖原文。

在 PowerShell 中查看时，可显式指定 UTF-8：

```powershell
Get-Content -LiteralPath .\README.md -Encoding UTF8
```

**Q：启动后端口不是 8080，报 `Port xxxx is already in use`？**
A：环境里存在 `SERVER__PORT` 环境变量。Spring Boot 的宽松绑定会把它解析成 `server.port`
且优先级高于 `application.yml`。用命令行参数覆盖即可：
`mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080`

**Q：启动正常，但所有接口都返回 `500 系统繁忙，请稍后重试`？**
A：**这是最容易踩的坑。** 表面看是"接口全挂"，实际是数据库连接用了空密码。
排查方法：看控制台日志，若出现
`Access denied for user 'root'@'localhost' (using password: NO)`
（注意结尾是 `password: NO`，说明**密码根本没传**），即为本问题。

原因是本地覆盖文件 `application-local.yml` 没被加载。两种修法：

1. **确认 `local` profile 已激活**（本项目 `application.yml` 已默认 `dev,local`）。
   切勿改用 `spring.config.import` 引入该文件——import 的执行时机**早于** profile 处理，
   文件会以低优先级加载并在 profile 阶段被去重跳过，密码永远不生效。
2. **改用环境变量**：设置 `MCD_DB_PASSWORD` 后重启。

> 典型误判：把页面能打开、但接口全 500 当成"前端服务没启动"。
> 本项目前后端不分离，**不存在独立的前端服务**；页面能渲染恰恰说明后端是通的。

**Q：启动报 `Access denied for user 'root'@'localhost'`？**
A：数据库密码未正确注入。请设置 `MCD_DB_PASSWORD` 环境变量，
或创建本地覆盖文件 `application-local.yml`（需确保 `local` profile 处于激活状态），
详见 [ENV.md](ENV.md)。

**Q：`doc.html` 打不开？**
A：确认端口未被占用；Knife4j 依赖首次需要联网下载。

**Q：想让程序自动建表？**
A：把 `application-dev.yml` 中 `spring.sql.init.mode` 改为 `always`。

**Q：套餐数据在哪里维护？**
A：`src/main/resources/db/data.sql`，或直接调 `/api/combo` 接口。

**Q：curl 测接口报 `upstream connect failed`？**
A：本机开了 HTTP 代理。加 `--noproxy '*'` 绕过：`curl --noproxy '*' http://localhost:8080/...`

**Q：调接口返回 401「登录已过期或未登录」？**
A：除放行名单外，所有 `/api/**` 都需要 token。先 `POST /api/auth/login` 拿 token，
再在请求头带上 `Authorization: Bearer <token>`。

**Q：重启服务后 token 失效了，要重新登录？**
A：这是预期行为。token 存在应用内存里（`ConcurrentHashMap`），未引入 Redis，
重启即全部失效——对比赛演示来说反而更干净。

**Q：默认账号密码是什么？想改成别的怎么办？**
A：默认 `admin / 123456`（管理员）和 `demo / 123456`（普通用户）。
改密码可调 `POST /api/auth/change-password`；新增账号用 `/api/auth/register`。

---

## 十、已验证项

| 项目 | 结果 |
| --- | --- |
| `mvn clean compile` | BUILD SUCCESS（69 个源文件） |
| `mvn test` | Tests run: 4, Failures: 0, Errors: 0（含性价比策略的正反用例） |
| 数据库建库 + 灌数据 | 6 张表，32 单品 / 14 套餐 / 48 明细 / 2 用户 |
| `POST /api/auth/login`（admin/123456） | 正常，返回 token（有效期 7200s，role=ADMIN） |
| `POST /api/auth/login`（错误密码） | 正常，返回 `code=2003 用户名或密码错误` |
| 未带 token 调 `/api/recommend` | 正常，返回 **HTTP 401** |
| 带 token 调 `/api/recommend` | 正常，Top1「巨无霸中套餐」91.00 分，记录 `user_id=admin` |
| `POST /api/auth/logout` 后再用旧 token | 正常，返回 **HTTP 401**（立即失效） |
| `GET /api/auth/me` / `/api/preference` | 正常，正确识别当前用户（admin / ADMIN） |
| `GET /api/recommend/quick` | 正常，时段按当前时间自动推断 |
| `GET /api/combo/page`、`/{id}`、`/stat` | 正常，分页 total=14、详情含 3 个单品与立省/折扣、统计 6 个分类 |
| 预算区间硬过滤（传 ¥15-35） | 正常，候选 3 个、返回 3 条，价格**全部在区间内** |
| 热量档位 `calorieLevel=LOW` | 正常，返回结果热量**全部 ≤1000 kcal**（候选 6 个） |
| 热量档位 `calorieLevel=HIGH` | 正常，返回结果热量**全部 >1000 kcal**（候选 8 个） |
| 非法 `calorieLevel=XX` | 正常，视为「不限」，不报错 |
| 条件组合无结果（¥15-35 + 热量偏高） | 正常，返回空榜单 + 可执行建议，**不是报错** |
| 不传 `topN` | 正常，按 `recommender.top-n` 取前 5；符合条件不足 5 个时全部返回 |
| 性价比打分（同一请求） | 正常，`value` 维度对 5 个候选给出 65.74~76.44 的不同分值，不再是恒定满分 |
| 性价比：去掉口味条件 | 正常，「每元热量」最高的「麦辣鸡腿堡中套餐」升到第 1（90.74 分），说明该维度确实影响排序 |
| 性价比：预算收窄到 ¥50-60 | 正常，只剩 2 个候选时按实际差距给分（23.53 vs 23.15 kcal/元 → 67.65 vs 65.74），未放大微小差距 |
| 性价比：热量数据缺失 | 正常，该候选不出现在 `value` 维度，也不计入权重分母 |

### 前端页面（浏览器实测）

用无头 Chrome 实际渲染并模拟点击验证：

| 页面 / 流程 | 结果 |
| --- | --- |
| 静态资源加载 | 全部 HTTP 200，尺寸正常 |
| 未登录访问首页 | 正常显示**登录页**，业务界面隐藏 |
| 空表单提交 | 正常被前端校验拦截 |
| 错误密码登录 | 正常停留在登录页并提示失败 |
| `admin / 123456` 登录 | 正常进入业务界面，顶部显示「管理员」与「退出」 |
| 刷新页面 | 正常通过 `/api/auth/me` **恢复会话**，无需重新登录 |
| 登录态调用推荐接口 | 正常返回 5 条推荐 |
| 点击「退出」 | 正常回到登录页，本地 token 已清除 |
| 推荐页：预算区间硬过滤 | 默认预算 ¥30-60 下，结果价格**全部落在区间内**（不再出现 ¥28 / ¥98 / ¥158） |
| 推荐页：热量偏好三档 | 选「热量偏低」结果全 ≤1000 kcal；选「热量偏高」全 >1000 kcal；「不限」不限制 |
| 推荐页：已移除「返回数量」 | 表单里不再有该选项，返回条数由后端决定（最多 5，不足则全给） |
| 推荐页：得分明细 | 维度显示为「性价比 / 口味匹配 / 人数适配 / 时段适配 / 人气热度 / 多样性」，不再出现「预算匹配」 |
| 推荐页：性价比推荐理由 | 出现「性价比不错：每元能买到 25.3 kcal」并排在理由首位；相对最优时升级为「性价比最高」 |
| 套餐列表：关键词搜标签 | 搜「牛肉」→ 5 条、搜「经典」→ 8 条、搜「超值」→ 4 条 |
| 套餐列表：关键词搜食物 | 搜「薯条」→ 13 条（含中/大/小薯条的套餐全覆盖，只含薯饼的早餐套餐正确排除） |
| 套餐列表：分页 | 13 条结果正确分页，第 2 页内容正确 |
| 套餐列表：分类标签配色 | 6 个分类 6 种颜色，无灰底 |
| 单品库：序号列 | 跨分页连续（第 1 页 1-10，第 2 页 11-20） |
| 单品库：分类标签配色 | 汉堡/小食/饮料/甜品/早餐 各一色，无灰底 |
| 单品库：切分页 | 无「新旧标签并存」的残留（曾复现并修复，见下） |
| 浏览器控制台 | **零错误** |

> 验证脚本放在 `.verify/` 下（该目录已在 `.gitignore` 中，不随仓库分发），
> 共 7 个无头 Chrome 脚本 + 2 个 Python 脚本，合计 135 条断言，全部通过。
> 脚本支持 `APP_BASE` 环境变量指定目标实例。


---

## 十一、后续可扩展方向

- [x] ~~接入用户登录，把 `userId` 换成真实用户体系~~（已完成，见第六章）
- [x] ~~新增「性价比」策略，解决预算维度恒为满分的问题~~（已完成，见 7.2：预算内按每元热量打分）
- [ ] 引入 Redis 缓存套餐库，降低数据库压力
- [ ] 记录用户点击/下单行为，做协同过滤推荐
- [ ] 接入大模型，把 `reasons` 润色成更自然的推荐话术
- [ ] 增加 A/B 测试能力，对比不同权重组合的效果
