# Invicta

Invicta 是基于现有 UTC Pay 数据库的独立统计与看板服务。它是一个单独部署、单独发布的代码库，不复制旧平台的钱包登录与链上业务实现。

## 设计边界

- 直接连接旧平台 MySQL，连接池使用只读模式。
- 生产数据库账号必须只授予 `SELECT`；这是防止误写的最终边界。
- 查询走 Invicta 自己的 `/api/v1` 接口。
- 新增、更新、删除必须封装为明确的业务服务，并通过 `LegacyApiClient` 调用旧平台 API；禁止通用透传代理。
- 登录使用唯一的 `sys_organ.email`，支持邮箱验证码或密码，JWT 为无状态访问令牌，不再依赖 TronLink 或 MetaMask。
- 密码使用 BCrypt 存储；Invicta 只读数据库，注册和资料更新均通过旧平台 API 完成。
- 历史 `org_email` 不迁移到新 `email`。已有商户选择“已有账户登录”，使用商户名定位账号，验证码仍发送到原 `org_email`。
- 已有账号验证码的 Redis 身份键包含 `orgId`；即使多个商户共用一个历史邮箱，验证码也不会在商户之间复用。
- 验证码和发送冷却均存储在 Redis：有效期 60 秒、同邮箱同用途 60 秒内不可重复发送，验证成功后原子删除且不可复用。
- 登录验证码与注册验证码按用途隔离。注册通过验证码后转调旧平台注册 API，并返回仅可完善资料的 JWT。
- 新账号完成商户名和商户类型（3 专业版、4 轻量版、5 标准版）前，只能访问 `/auth/me` 和 `/auth/profile`。
- 注册和资料完善调用 UTC Pay 的 `/api/invicta/**` 外部接口，沿用 `X-PAY-KEY`、`X-PAY-SIGN`、`X-PAY-TIMESTAMP` 签名规则。
- 新用户注册成功时由 UTC Pay 自动生成各链预付费充值地址和预付费账户；老用户首次成功登录时也会幂等补齐。Invicta 不直接写这两张表。
- 用户向预付费地址充值后，仍由 UTC Pay 原有链上监听和入账流程更新账户余额及流水；Invicta 负责查询、展示和导出。
- UTC Pay 只允许签名验证后解析出的 `merId=1000` 调用上述账号接口；生产环境建议同时配置 Invicta 服务器 IP 白名单。
- 页面提示支持中英文，通过 `Accept-Language` 请求头或 `lang` 查询参数选择；未指定时默认英文。
- 邮件账号使用与 UTC Pay 一致的 `mail.configs` 配置结构，SMTP 密码只通过环境变量或部署密钥注入。

## 已有骨架接口

| Method | Path | 说明 |
|---|---|---|
| POST | `/api/v1/auth/email/code` | 请求登录或注册验证码 |
| POST | `/api/v1/auth/email/login` | 使用邮箱验证码登录 |
| POST | `/api/v1/auth/existing/code` | 已有账户按商户名请求验证码 |
| POST | `/api/v1/auth/existing/login` | 已有账户按商户名验证并登录 |
| POST | `/api/v1/auth/password/login` | 使用邮箱和密码登录 |
| POST | `/api/v1/auth/register` | 验证邮箱、设置密码并创建待完善账号 |
| PUT | `/api/v1/auth/profile` | 填写商户名和商户类型，返回完整权限 JWT |
| GET | `/api/v1/auth/me` | 当前账号 |
| GET | `/api/v1/organization` | 商户分页列表 |
| GET | `/api/v1/chain` | 链列表 |
| GET | `/api/v1/currency` | 币种列表 |
| GET | `/api/v1/payment` | 收单分页列表、详情、统计 |
| GET | `/api/v1/recharge` | 充值分页列表、详情、统计 |
| GET/POST | `/api/v1/summary` | 归集分页列表、详情、统计、钱包币种余额及写操作 |
| GET/POST | `/api/v1/balance` | 平衡分页列表、详情、统计、余额占比及写操作 |
| GET/POST | `/api/v1/withdraw` | 提现列表、详情、统计；申请、审批和确认转调旧平台 |
| GET | `/api/v1/dashboard/summary` | 看板汇总 |
| GET/POST | `/api/v1/prepaid` | 预付费账户、充值地址、地址更新、流水列表和 XLSX 导出 |
| GET/PUT | `/api/v1/strategy` | 查询及配置归集、提现、平衡策略 |

Swagger UI: `http://localhost:8081/docs`

接口路径遵循以下约定：使用小写、直观且与业务一致的功能名称，例如 `payment`、`recharge`、`summary`、`balance` 和 `withdraw`；必要的操作使用 `login`、`approve`、`confirm`、`check-status` 等明确词汇。查询参数统一使用 `camelCase`。

每个响应都会返回 `X-Request-Id` 响应头。服务日志包含相同的请求 ID、HTTP 方法、路径、状态码和耗时；日志禁止记录密码、验证码、JWT、API Secret 和完整请求体。错误响应也包含 `requestId`，可直接用于日志检索。

## 代码结构

- `controller`：按充值、收单、归集、平衡、提现等功能拆分控制器。
- `service`：业务接口；`service/impl`：业务实现、权限校验，以及写操作转调旧平台 API。
- `entity`：与共享数据库表对应的实体；`mapper`：MyBatis-Plus Mapper 接口。
- 简单单表查询使用 `LambdaQueryWrapper`；统计、聚合和联表等复杂 SQL 放在 `resources/mapper/*.xml`。
- `model`：接口返回模型；`common` 放分页和统一异常等通用对象。
- `config`、`security`、`client`：环境配置、安全认证和旧平台客户端。
- `application-dev.yml`、`application-test.yml`、`application-prod.yml`：开发、测试、生产环境配置。

分页统一使用从 0 开始的 `page`、`size`，最大每页 200 条。时间筛选使用 ISO-8601 本地时间，例如 `2026-10-01T00:00:00`，区间为 `[from, to)`。

## 本地启动

要求 Java 17、Maven 3.9+ 和 MySQL 8：

```bash
cp .env.example .env
set -a && source .env && set +a
mvn spring-boot:run
```

也可以运行：

```bash
docker compose up --build
```

开发环境 `INVICTA_MAIL_ENABLED=false` 时，验证码会写入应用日志（邮箱会脱敏）。生产环境必须启用 SMTP，并设置强随机 `JWT_SECRET`。

生产邮件配置沿用 UTC Pay 的账号结构。当前验证码邮件使用配置列表中的第一个发送账号：

```yaml
mail:
  configs:
    - host: ${MAIL_HOST:smtp.gmail.com}
      port: ${MAIL_PORT:465}
      username: ${MAIL_USERNAME}
      password: ${MAIL_PASSWORD}
      default-encoding: UTF-8
      properties:
        mail.smtp.auth: true
        mail.smtp.socketFactory.port: 465
        mail.smtp.socketFactory.class: javax.net.ssl.SSLSocketFactory
        mail.smtp.socketFactory.fallback: false
```

不要把 UTC Pay 现有邮箱密码复制到 Git；部署时为 Invicta 设置相同的邮箱环境变量即可。

调用 UTC Pay 时使用数据库中 `merId=1000` 对应的通用 API Key/Secret：

```bash
LEGACY_API_KEY=<merId=1000对应的API Key>
LEGACY_API_SECRET=<merId=1000对应的Secret Key>
```

UTC Pay 不需要额外配置 `INVICTA_API_KEY`。请求通过原有签名校验后，账号接口会检查凭证对应的
`merId` 必须为 `1000`。Secret 只配置在 Invicta，不能提交到代码库。

发布前需要由 DBA 为 `sys_organ` 增加唯一登录邮箱、密码哈希和资料完成状态字段；仓库中不保存数据库变更 SQL。

## 只读数据库账号

请由 DBA 按环境创建独立的 Invicta 数据库账号，并只授予业务库的 `SELECT` 权限；来源网段也应按部署环境收紧。不要授予 `INSERT`、`UPDATE`、`DELETE`、`CREATE` 或 `ALTER`。

## 示例

```bash
curl -X POST http://localhost:8081/api/v1/auth/email/code \
  -H 'Accept-Language: zh-CN' \
  -H 'Content-Type: application/json' \
  -d '{"email":"operator@example.com","purpose":"LOGIN"}'

curl -X POST http://localhost:8081/api/v1/auth/email/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"operator@example.com","code":"123456"}'

# 历史商户的新 email 字段保持为空，通过商户名进入已有账户登录。
curl -X POST http://localhost:8081/api/v1/auth/existing/code \
  -H 'Content-Type: application/json' \
  -d '{"orgName":"Existing Org"}'

curl -X POST http://localhost:8081/api/v1/auth/existing/login \
  -H 'Content-Type: application/json' \
  -d '{"orgName":"Existing Org","code":"123456"}'

# 新用户先请求 REGISTRATION 验证码，再设置密码创建账号。
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"new@example.com","code":"123456","password":"StrongPass123"}'

# 使用注册响应中的临时 JWT 完善商户资料。
curl -X PUT http://localhost:8081/api/v1/auth/profile \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{"orgName":"New Org","orgType":3}'

curl -X POST http://localhost:8081/api/v1/auth/password/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"new@example.com","password":"StrongPass123"}'

curl 'http://localhost:8081/api/v1/payment?orgId=100&page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

也可以使用 `?lang=zh-CN` 或 `?lang=en-US`。错误响应中的 `code` 始终保持不变，只有给页面展示的 `message` 和校验详情会随语言变化。

## 下一步

1. 与旧平台真实库表做集成测试，校准历史字段类型与状态枚举。
2. 为验证码校验增加失败次数限制，并在网关按 IP、邮箱和设备维度限流及记录安全审计。
3. 按 `sys` 控制器清单继续迁移非核心辅助接口；每个写接口建立明确的旧 API 对应关系。
4. 为高频看板查询增加组合索引、只读副本和缓存，不在首版直接改共享生产库。
