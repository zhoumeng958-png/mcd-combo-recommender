# 环境变量说明

本项目遵循「**代码与凭据分离**」原则：仓库中不包含任何真实的 Token、密码或密钥，
所有敏感信息一律通过**环境变量**注入。

---

## 一、数据库配置

`application-dev.yml` 中的数据库配置全部使用环境变量占位符，
并提供了本地开发的默认值（见下表）。如需覆盖，设置对应环境变量即可。

| 环境变量 | 说明 | 默认值 |
| --- | --- | --- |
| `MCD_DB_HOST` | 数据库主机 | `localhost` |
| `MCD_DB_PORT` | 数据库端口 | `3306` |
| `MCD_DB_NAME` | 数据库名 | `Mdangdang` |
| `MCD_DB_USER` | 数据库用户名 | `root` |
| `MCD_DB_PASSWORD` | 数据库密码 | *（空，必须自行设置）* |

### 配置示例

**方式一：环境变量（推荐）**

```bash
export MCD_DB_USER=root
export MCD_DB_PASSWORD=你的密码
mvn spring-boot:run
```

**方式二：本地覆盖文件（不提交到 Git）**

在 `src/main/resources/` 下创建 `application-local.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/Mdangdang?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true
    username: root
    password: 你的密码
```

该文件已在 `.gitignore` 中排除，不会被提交。

> ⚠️ **必须让 `local` profile 处于激活状态，否则该文件不会生效。**
>
> `application.yml` 中已默认设置 `spring.profiles.active: dev,local`，
> 所以只要文件存在就会被自动加载，**无需任何额外参数**。
>
> **不要改用 `spring.config.import` 引入该文件。** 这是一个容易踩的坑：
> `spring.config.import` 的执行时机**早于** profile 处理，因此被导入的文件
> 会以**低优先级**加载，随后在 profile 阶段又被**去重跳过**。
> 结果是它永远被 `application-dev.yml` 里的 `${MCD_DB_PASSWORD:}`（空值）覆盖，
> 表现为启动正常、但所有接口都报 `500 系统繁忙`，
> 日志里是 `Access denied for user 'root'@'localhost' (using password: NO)`。

---

## 二、麦当劳 MCP 配置

麦当劳 MCP Server 的 Token **不写入任何配置文件**，
请在 WorkBuddy 的 MCP 配置中使用环境变量占位符：

```json
{
  "mcpServers": {
    "mcd-mcp": {
      "type": "streamablehttp",
      "url": "https://mcp.mcd.cn",
      "headers": {
        "Authorization": "Bearer ${MCD_MCP_TOKEN}"
      }
    }
  }
}
```

| 环境变量 | 说明 |
| --- | --- |
| `MCD_MCP_TOKEN` | 从麦当劳 MCP 平台申请到的个人访问令牌 |

> ⚠️ 请勿将真实 Token 提交到公开仓库。若 Token 曾经泄漏，请立即在平台重置。

---

## 三、安全自查清单

提交到公开仓库前，建议确认：

- [ ] 未在代码、配置、文档中硬编码任何真实 Token / 密码 / 密钥
- [ ] `application-local.yml` 未被提交（已在 `.gitignore` 中排除）
- [ ] `git log` 中未包含含敏感信息的提交（如有，需重写历史）
- [ ] 数据库中的用户密码以 BCrypt 哈希形式存储，非明文

### 自动化扫描脚本

本项目提供了一个敏感信息自查脚本，提交前跑一次即可：

```bash
python .verify/check_secrets.py
```

它会扫描仓库内所有文本文件，检查已知口令与常见 Token 模式，输出通过 / 问题报告。

> ⚠️ **为什么不用 grep 验证？**
> 实测在本环境下，`grep` 对含中文的 UTF-8 文件会**漏报**——同一串密码，
> `sed -n 'Np'` 能正常显示，而 `grep -c` / `grep -F -c` 却返回 0。
> 因此本项目统一改用 Python 逐字节扫描，避免误判。

### 对话记录导出时的脱敏

`workbuddy.md` 由 `.verify/export_workbuddy.py` 自动导出，脚本内置两重脱敏：

1. **已知口令**：历史上出现过的所有本地口令 → `******`
2. **通用模式**：`Bearer xxx`、`password: xxx`、`密码：xxx` 等 → 掩码

导出完成后脚本会**自动复扫**生成的文件，若发现残留会直接报错退出（退出码 1）。
