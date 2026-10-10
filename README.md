# Invicta

Invicta 是基于现有 UTC Pay 数据库的独立统计与看板服务。它是一个单独部署、单独发布的代码库，不复制旧平台的钱包登录与链上业务实现。

## 设计边界

- 直接连接旧平台 MySQL，连接池使用只读模式。
- 生产数据库账号必须只授予 `SELECT`；这是防止误写的最终边界。
- 查询走 Invicta 自己的 `/api/v1` 接口。
- 新增、更新、删除必须封装为明确的业务服务，并通过 `LegacyApiClient` 调用旧平台 API；禁止通用透传代理。
- 登录使用邮箱一次性验证码，JWT 为无状态访问令牌，不再依赖 TronLink 或 MetaMask。
- 老用户按 `sys_organ.org_email` 登录，不使用密码；仅状态正常的机构可以签发 JWT。
- 验证码和发送冷却均存储在 Redis：有效期 60 秒、同邮箱同用途 60 秒内不可重复发送，验证成功后原子删除且不可复用。
- 登录验证码与注册验证码按用途隔离。注册通过验证码后转调旧平台注册 API，Invicta 不直接写共享数据库。
- 页面提示支持中英文，通过 `Accept-Language` 请求头或 `lang` 查询参数选择；未指定时默认英文。
- 邮件账号使用与 UTC Pay 一致的 `mail.configs` 配置结构，SMTP 密码只通过环境变量或部署密钥注入。

## 已有骨架接口

| Method | Path | 说明 |
|---|---|---|
| POST | `/api/v1/auth/email/request-code` | 请求登录或注册验证码 |
| POST | `/api/v1/auth/email/verify` | 验证验证码并签发 JWT |
| POST | `/api/v1/auth/register` | 验证邮箱并将机构注册请求转调旧平台 |
| GET | `/api/v1/auth/me` | 当前账号 |
| GET | `/api/v1/organizations` | 机构分页列表 |
| GET | `/api/v1/chains` | 链列表 |
| GET | `/api/v1/currencies` | 币种列表 |
| GET | `/api/v1/payments` | 收单分页列表、详情、统计 |
| GET | `/api/v1/recharges` | 充值分页列表、详情、统计 |
| GET | `/api/v1/summary` | 归集分页列表、详情、统计、钱包币种余额 |
| GET | `/api/v1/balances` | 平衡分页列表、详情、统计、余额占比 |
| GET/POST | `/api/v1/withdraw` | 提现列表、详情、统计；申请/审批/确认转调老平台 |
| GET | `/api/v1/dashboard/summary` | 看板汇总 |

Swagger UI: `http://localhost:8081/docs`

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

## 只读数据库账号

请由 DBA 在对应环境执行，库名和来源网段按实际情况收紧：

```sql
CREATE USER 'invicta_readonly'@'10.%' IDENTIFIED BY '<strong-password>';
GRANT SELECT ON utcpay.* TO 'invicta_readonly'@'10.%';
FLUSH PRIVILEGES;
```

不要给 Invicta 数据库账号授予 `INSERT`、`UPDATE`、`DELETE`、`CREATE` 或 `ALTER`。

## 示例

```bash
curl -X POST http://localhost:8081/api/v1/auth/email/request-code \
  -H 'Accept-Language: zh-CN' \
  -H 'Content-Type: application/json' \
  -d '{"email":"operator@example.com","purpose":"LOGIN"}'

curl -X POST http://localhost:8081/api/v1/auth/email/verify \
  -H 'Content-Type: application/json' \
  -d '{"email":"operator@example.com","code":"123456"}'

# 新用户先请求 REGISTRATION 验证码，再提交机构注册资料。
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"new@example.com","code":"123456","type":"INDIVIDUAL","organizationName":"New Org","registrationCode":"INVITE-CODE","chainId":1,"stakeAccount":false,"details":{"walletAddress":""}}'

curl 'http://localhost:8081/api/v1/payments?orgId=100&page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

也可以使用 `?lang=zh-CN` 或 `?lang=en-US`。错误响应中的 `code` 始终保持不变，只有给页面展示的 `message` 和校验详情会随语言变化。

## 下一步

1. 与旧平台真实库表做集成测试，校准历史字段类型与状态枚举。
2. 为验证码校验增加失败次数限制，并在网关按 IP、邮箱和设备维度限流及记录安全审计。
3. 按 `sys` 控制器清单继续迁移非核心辅助接口；每个写接口建立明确的旧 API 对应关系。
4. 为高频看板查询增加组合索引、只读副本和缓存，不在首版直接改共享生产库。
