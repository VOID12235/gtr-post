# P1 开发状态

> 更新：2026-11-08
> 定位：可直接部署的社会赛事平台（桌面 Web + 手机 PWA）

## 已完成

| 模块 | 内容 | 状态 |
|---|---|---|
| 工程 | Maven Wrapper、Spring Boot 4.1.2、Flyway、H2/MySQL 双迁移 | ✅ |
| 多租户 | `tenant_id` 行级隔离、TenantContext、MyBatis-Plus 拦截器 | ✅ |
| 认证 | 登录、邮箱注册、找回密码、BCrypt、JWT（2h）、滑动验证、邮箱验证码 | ✅ |
| 租户 | 平台开通、列表、当前租户资料 | ✅ |
| 赛事 | 创建、列表、状态流转、公开详情 | ✅ |
| 报名 | 选手报名、我的报名、管理端名单 | ✅ |
| 作品 | 提交/修改、我的作品、公开作品 | ✅ |
| 评分 | 评分表、维度权重、评委任务、打分、平均分 | ✅ |
| 赞助 | 赞助商、活动、库存位、审核、公开曝光位 | ✅ |
| 证书 | 批量生成、编号、公开验真 | ✅ |
| 通知 | 站内信、未读、已读 | ✅ |
| 设置 | 站点/邮件/上传/报名费/安全/奖金保险/订单 | ✅ |
| 文件 | 本地上传、类型/大小校验、公开下载 | ✅ |
| 报名费 | 待支付订单、管理员确认、退款 | ✅ |
| 风险 | 奖金池、保险保单、线下安全方案 | ✅ |
| 成员 | 创建评委/运营、改角色、启停 | ✅ |
| 商城 | 商品、库存、下单、支付确认、发货、退款 | ✅ |
| 仪表盘 | 赛事/报名/作品/证书统计 | ✅ |
| 公开站 | 首页、赛事列表、赛事详情、证书验真 | ✅ |
| 手机 | PWA manifest、Service Worker、可安装、移动端适配 | ✅ |
| 部署 | Docker Compose、Nginx、CI 工作流 | ✅ |
| 测试 | 上下文 + Flyway + 登录 + 未授权访问 | ✅ |

## 演示账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 平台管理员 / 租户所有者 | `admin` | `admin123` |
| 选手 | `player` | `player123` |
| 选手二 | `player2` | `player123` |
| 评委 | `judge` | `judge123` |

租户编码统一为 `demo`。

## 演示数据

首次启动会自动创建：

| 数据 | 内容 |
|---|---|
| 租户 | 示例科技（demo） |
| 赛事 | 2027 企业创新赛（SUBMISSION） |
| 作品 | 智能巡检机器人（已评分 91.5）、园区能耗优化方案（待评分） |
| 评分 | 91.5（演示评委） |
| 证书 | `BTR-1-DEMO` · 一等奖 |
| 赞助商 | 星云科技 |
| 投放 | 2027 企业创新赛冠名（已通过） |
| 库存位 | 赛事冠名、首页 Banner |

> 用 `judge` 登录评审工作台，可以看到「园区能耗优化方案」待评分。

证书验真：<http://localhost:5173/verify/BTR-1-DEMO>

## 主要接口

### 认证 / 租户

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/send-register-code`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
- `GET /api/v1/auth/captcha/slider`
- `POST /api/v1/auth/captcha/slider/verify`
- `GET /api/v1/auth/me`
- `PUT /api/v1/auth/password`
- `GET /api/v1/platform/tenants`
- `POST /api/v1/platform/tenants`
- `GET /api/v1/tenant/profile`

### 赛事 / 报名 / 作品

- `GET/POST /api/v1/admin/contests`
- `GET /api/v1/admin/contests/{id}`
- `PUT /api/v1/admin/contests/{id}/status`
- `GET /api/v1/public/contests?tenantCode=demo`
- `GET /api/v1/public/contests/{id}?tenantCode=demo`
- `POST /api/v1/signups`
- `GET /api/v1/signups/mine`
- `GET /api/v1/admin/signups?contestId=`
- `POST /api/v1/works`
- `GET /api/v1/works/mine`
- `GET /api/v1/admin/works?contestId=`
- `GET /api/v1/public/works?contestId=&tenantCode=demo`

### 评分

- `GET/POST /api/v1/admin/rubrics`
- `GET /api/v1/public/rubrics?contestId=&tenantCode=demo`
- `GET /api/v1/judge/tasks?contestId=`
- `POST /api/v1/judge/scores`
- `GET /api/v1/admin/scores?contestId=`

### 赞助

- `GET/POST /api/v1/platform/sponsors`
- `GET/POST /api/v1/platform/campaigns`
- `POST /api/v1/platform/campaigns/{id}/approve`
- `GET /api/v1/public/placements?slot=&contestId=&tenantCode=demo`
- `POST /api/v1/public/ad/impression`
- `POST /api/v1/public/ad/click`

### 证书 / 通知 / 仪表盘

- `GET /api/v1/admin/certificates?contestId=`
- `POST /api/v1/admin/certificates/generate`
- `GET /api/v1/public/certificates/{code}`
- `GET /api/v1/notifications`
- `PUT /api/v1/notifications/{id}/read`
- `GET /api/v1/admin/dashboard`

### 设置 / 上传 / 报名费 / 风险

- `GET/PUT /api/v1/admin/config/{site|mail|upload|payment|security}`
- `POST /api/v1/admin/config/mail/test`
- `GET /api/v1/public/site?tenantCode=demo`
- `POST /api/v1/files` / `GET /api/v1/files/{id}`
- `GET /api/v1/payments/mine`
- `GET /api/v1/admin/payments?contestId=`
- `POST /api/v1/admin/payments/{id}/paid`
- `POST /api/v1/admin/payments/{id}/refund`
- `GET /api/v1/admin/risk/{contestId}`
- `PUT /api/v1/admin/risk/{contestId}/prize|insurance|safety`

### 成员 / 商城

- `GET/POST /api/v1/admin/members`
- `PUT /api/v1/admin/members/{id}/role|status`
- `GET /api/v1/public/shop/products?tenantCode=demo`
- `GET /api/v1/public/shop/products/{id}?tenantCode=demo`
- `POST /api/v1/shop/orders`
- `GET /api/v1/shop/orders/mine`
- `GET/POST /api/v1/admin/shop/products`
- `PUT /api/v1/admin/shop/products/{id}`
- `PUT /api/v1/admin/shop/products/{id}/status`
- `GET /api/v1/admin/shop/orders`
- `POST /api/v1/admin/shop/orders/{id}/paid|ship|complete|refund`

## 手机 PWA

- `public/manifest.webmanifest`
- `public/sw.js`
- `npm run icons` 生成 192/512/maskable/apple-touch 图标

安装方式：
- Android Chrome：打开站点 → 菜单 → 安装应用
- iOS Safari：分享 → 添加到主屏幕

原生壳：`frontend/capacitor.config.ts`，安装 Android Studio / Xcode 后可执行 Capacitor 打包。

## 下一步

1. 在线支付（微信/支付宝服务商）
2. 商品批量发货 / 物流 API / 售后
3. 模板 / 克隆
4. 多轮次 / 多赛道 / 团队赛 UI
5. 评审申诉 / 仲裁 / 归一化
6. 原生 APK / IPA 签名打包

## 已知待办

- 生产环境默认管理员密码必须覆盖。
- 文件上传已落本地 `/root/BRT/uploads`，尚未接对象存储。
- 邮件需在后台配置真实 SMTP 授权码后才能发送。
- 报名费为「线下支付 + 管理员确认」，在线支付需接持牌机构。
- 商城为「线下支付 + 人工发货」，未接在线支付与物流 API。
- 赞助曝光/点击为事件表，尚未做日聚合与报表导出。
- 证书当前为数据记录，PDF 模板渲染待接 PDFBox。
- 安全策略中的登录失败限流只做了配置记录，尚未接入登录逻辑。
- 邮箱验证码/滑动票据存在进程内存，多实例部署需迁移到 Redis。
