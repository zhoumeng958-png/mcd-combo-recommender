# 参赛提交流程（官方要求对照版）

本文档把「麦当劳程序员节创意开发大赛」的官方提交要求整理成可执行的步骤清单。
本地仓库已初始化并完成首次提交，你只需要完成 **推送到 GitHub → 设为公开 → 发 Issue 报名** 三步。

---

## 一、时间节点（北京时间）

| 阶段 | 时间 |
| --- | --- |
| 报名及排名 | 2026-10-09 10:30 — **2026-10-25 23:59** |
| 定榜 | 2026-10-26 00:00 采集的排行榜数据 |
| 奖品兑换及信息提交截止 | 2026-11-14 |

---

## 二、报名条件（官方原文对照）

| # | 条件 | 本项目状态 |
| --- | --- | --- |
| 1 | 项目中包含规定的文件，且指定文件名符合要求 | ✅ 五个文件齐备 |
| 2 | 按照标准格式发布报名 Issue | ⬜ 待你提交 |
| 3 | GitHub 项目为公开可访问的 **Public** 仓库 | ⬜ 待你设置 |
| 4 | 项目名称及内容不含违法违规或敏感内容 | ✅ 已做凭据脱敏 |
| 5 | 项目创建时间为 2025-12-25 00:00 至 2026-10-25 23:59 | ✅ 现在创建符合 |

---

## 三、必需的五个文件

| # | 文件名 | 是否必须 | 本项目 |
| --- | --- | --- | --- |
| 1 | `README.md` | 必须，**文件名不可改动** | ✅ |
| 2 | `CONTEST_DECLARATION.md` | 必须，**文件名及内容均不可改动** | ✅ 官方原文，未改一字 |
| 3 | `MCP_INTEGRATION.md` | 必须，**文件名不可改动** | ✅ |
| 4 | 源代码 | 必须，形式不限 | ✅ 97 个文件 |
| 5 | `workbuddy.md` | 非必须；**参加 WorkBuddy 专项奖励时必须提交** | ✅ |

> ⚠️ `CONTEST_DECLARATION.md` 是本项目的红线文件，任何情况下都不要修改其内容。

---

## 四、操作步骤

### 步骤 1：在 GitHub 创建空仓库

1. 打开 https://github.com/new
2. **Repository name** 填：`mcd-combo-recommender`（或你喜欢的名字）
3. **Description** 填：麦当劳套餐智能推荐助手 · Spring Boot + 可插拔策略引擎
4. **Visibility** 选择 **Public**（⚠️ 必须公开，否则报名失败）
5. **不要**勾选 "Add a README file"、"Add .gitignore"、"Choose a license"
   （因为本地已有这些文件，勾选会导致推送冲突）
6. 点击 **Create repository**

### 步骤 2：关联远程仓库并推送

创建后 GitHub 会显示仓库地址，把下面的 `你的用户名` 替换成你的 GitHub 用户名：

```bash
cd /d/ZM/MMdang

git remote add origin https://github.com/你的用户名/mcd-combo-recommender.git

git push -u origin main
```

> 推送时若要求登录，推荐用 **Personal Access Token** 代替密码：
> GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) →
> Generate new token，勾选 `repo` 权限，用生成的 token 当密码填写。

**验证推送成功：**

```bash
git remote -v          # 应显示 origin 指向你的仓库
git status             # 应显示 "Your branch is up to date with 'origin/main'"
```

然后浏览器打开 `https://github.com/你的用户名/mcd-combo-recommender`，
确认能看到五个文件且仓库标记为 **Public**。

### 步骤 3：发布报名 Issue

访问活动官方仓库的 Issue 页面：

> **https://github.com/MCD-qifei/MCP-Skill-Competition/issues**

点击 **New issue**，按下面的格式填写。

> ⚠️ **官方补充的严格校验规则（非常重要）**
> 1. **【标题】和【正文】之间不要使用换行符**
> 2. **`项目简介` 的标签和内容之间不要使用换行符**
> 3. **项目地址链接不要使用 Markdown 格式**（即不要写成 `[文字](链接)`，直接写裸链接）

**标题**（自定义即可）：

```
【参赛申请】麦当劳套餐推荐助手
```

**正文**（⚠️ 不要超过 1000 字，⚠️ 不要携带图片，⚠️ 标签与内容同行）：

```
【参赛申请】
项目名称：麦当劳套餐推荐助手
项目地址：https://github.com/你的用户名/mcd-combo-recommender
项目简介：基于 Spring Boot 与可插拔规则策略引擎的麦当劳套餐智能推荐系统。用户输入预算、口味偏好、忌口、就餐人数与用餐时段，系统对候选套餐逐项打分并加权汇总，输出带推荐理由与得分明细的榜单。内置预算匹配、口味匹配、人数适配、时段适配、人气热度、多样性共 6 个策略，权重可配置、策略可插拔。项目接入麦当劳 MCP Server，实现了门店查询、菜单查询、营养数据、优惠券、价格计算等 6 个 Tool 的调用链路。前后端不分离，Element UI 前端由 Spring Boot 静态资源直接提供，内置 Token 登录体系与 BCrypt 密码加密，依赖本地化、断网可完整演示。
```

**核实地址的两个来源**：
- 活动主仓库：`https://github.com/MCD-qifei/MCP-Skill-Competition`
- 报名 Issue 列表：`https://github.com/MCD-qifei/MCP-Skill-Competition/issues`

### 步骤 4：等待结果

- **报名成功**：系统会在你的 Issue 下回复成功通知
- **报名失败**：系统会回复失败原因，修改后**可以重新提交 Issue**

---

## 五、排名规则（重要，早点拉 Star）

- 麦当劳会定时采集报名成功项目的**公开 Star 数**
- **Star 数大于 0** 才进入排行榜
- 只有排名**前 100** 才进入获奖范围
- 定榜以 **2026-10-26 00:00** 采集的数据为准

### 奖励一览

| 奖励 | 条件 |
| --- | --- |
| 巨无霸 50/30/20 次免费兑换券 | 第 1 / 2 / 3 名 |
| WorkBuddy 3,000 积分 | 前 100 名且提交了 `workbuddy.md` |
| WorkBuddy 10,240 积分 | 前 3 名 |
| 实物周边 | 进入排行榜的项目 |

> 💡 **策略建议**：报名成功后尽早把项目发到技术社区（掘金、V2EX、即刻等）拉 Star，
> 因为排名只认 Star 数，越早开始积累越好。

---

## 六、提交前自查清单

- [x] 五个文件齐备，文件名完全一致
- [x] `CONTEST_DECLARATION.md` 为官方原文，内容未改动
- [x] 仓库不含任何真实 Token / 密码（已扫描确认）
- [x] `application-local.yml`（含本地密码）已被 `.gitignore` 排除
- [x] 本地 Git 仓库已初始化并完成首次提交
- [ ] 推送到 GitHub 并设为 **Public**
- [ ] 按标准格式发布报名 Issue（≤1000 字、无图片）
- [ ] 收到报名成功通知
- [ ] 开始积累 Star

---

## 七、常见问题

**Q：仓库必须是 Public 吗？**
A：是的，这是硬性报名条件。Private 仓库会直接报名失败。

**Q：`workbuddy.md` 需要更新吗？**
A：建议在最终完成后再导出一次，确保对话记录覆盖完整开发过程。
导出脚本：`.verify/export_workbuddy.py`（已内置敏感信息脱敏）。

**Q：Issue 里可以放图片吗？**
A：**不可以**。官方明确要求"请勿携带图片等内容，以免报名失败"。

**Q：Issue 超过 1000 字会怎样？**
A：可能报名失败。上面的模板已控制在要求范围内。

**Q：项目名可以用中文吗？**
A：可以。仓库名建议用英文（如 `mcd-combo-recommender`），Issue 里的项目名称用中文即可。
