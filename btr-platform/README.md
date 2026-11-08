# BTR 社会赛事平台

> 主办方免费，赞助商付费。企业、行业协会、政府/园区、品牌、执行公司的社会赛事基础设施。
> 桌面 Web + 手机 PWA（可安装），核心闭环已跑通。

---

## 当前状态

| 模块 | 状态 |
|---|---|
| 多租户 / 登录 / 注册 | ✅ |
| 赛事创建 / 状态流转 / 公开页 | ✅ |
| 报名 / 作品提交 | ✅ |
| 评分表 / 评委打分 / 平均分 | ✅ |
| 赞助商 / 投放 / 库存位 / 曝光位 | ✅ |
| 证书生成 / 编号 / 公开验真 | ✅ |
| 站内通知 / 仪表盘 | ✅ |
| 公开站 / 移动端 / PWA | ✅ |
| 系统设置 / 邮件 / 上传 / 报名费 / 奖金保险 | ✅ |
| 商品商城 / 成员管理 | ✅ |
| Docker Compose / Nginx / CI | ✅ |

详细接口与状态见 [`docs/P1-STATUS.md`](docs/P1-STATUS.md)。

---

## 演示账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 平台管理员 / 租户所有者 | `admin` | 本地 `admin123`；生产为部署时设置的密码 |
| 选手 | `player` | `player123` |
| 选手二 | `player2` | `player123` |
| 评委 | `judge` | `judge123` |

登录：邮箱/账号 + 密码 + 滑动验证。
注册：邮箱 + 邮箱验证码 + 昵称 + 密码，账号=邮箱。
找回密码：邮箱 + 邮箱验证码 + 新密码。

> 注册/找回密码需要先在「系统设置 → 邮件」配置 SMTP。

租户编码：`demo`

首次启动自动创建演示赛事、作品、评分、证书（`BTR-1-DEMO`）和赞助投放。

证书验真：<http://localhost:5173/verify/BTR-1-DEMO>

---

## 本地启动

### 后端

要求 JDK 21+。

```bash
cd backend
./mvnw spring-boot:run
```

Windows：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

- API：<http://localhost:8080/api/v1>
- 健康检查：<http://localhost:8080/api/v1/health>
- Swagger：<http://localhost:8080/swagger-ui.html>
- H2 控制台：<http://localhost:8080/h2-console>（URL `jdbc:h2:file:./data/btr`，用户 `sa`，密码空）

### 前端

要求 Node 20+。

```bash
cd frontend
npm install
npm run dev
```

打开 <http://localhost:5173>。

---

## 主要页面

| 路径 | 说明 |
|---|---|
| `/` | 公开首页 |
| `/contests` | 赛事列表 |
| `/contests/:id` | 赛事详情、报名、作品、赞助位 |
| `/verify/:code` | 证书验真 |
| `/login` | 登录 / 注册 |
| `/dashboard` | 管理仪表盘 |
| `/contests-admin` | 赛事管理 |
| `/tenants` | 租户管理 |
| `/sponsors` | 赞助商与投放 |
| `/rubrics` | 评分表 |
| `/certificates` | 证书生成 |
| `/settings` | 系统设置（站点/邮件/上传/报名费/安全/奖金保险/订单） |
| `/members` | 成员管理（评委/运营账号） |
| `/shop` | 公开商城 |
| `/shop-admin` | 商品与订单管理 |
| `/judge` | 评委工作台 |
| `/submit/:contestId` | 选手提交作品（含文件上传） |
| `/me` | 选手个人中心 |

---

## 核心流程

### 管理员

1. 开通租户。
2. 创建赛事（DRAFT）。
3. 推进到 SIGNUP / SUBMISSION / JUDGING / PUBLISHED / FINISHED。
4. 配置评分表。
5. 创建赞助商与投放，审核上线。
6. 生成证书。

### 选手

1. 注册 / 登录。
2. 浏览赛事并报名（收费赛事生成待支付订单）。
3. 管理员确认支付后，进入「提交作品」上传文件。
4. 个人中心查看报名、作品、支付、通知。

### 评委

1. 登录后进入评审工作台。
2. 选择赛事，查看待评作品。
3. 打分 + 评语。
4. 系统计算作品平均分。

### 证书验真

公开访问 `/verify`，输入编号即可验证赛事、奖项、持证人。

---

## 多租户

- 所有业务表带 `tenant_id`。
- MyBatis-Plus `TenantLineInnerInterceptor` 自动注入租户条件。
- `tenant`、`sys_user`、`sponsor`、`campaign`、`campaign_placement`、`ad_event` 例外。
- JWT 携带 `tenantId`，请求结束清理。
- 平台管理员通过 `PLATFORM_ADMIN` 访问跨租户接口。

新增业务表：

1. 加 `tenant_id BIGINT NOT NULL`。
2. 索引带 `tenant_id`。
3. 不要随意加入 `BtrTenantLineHandler.IGNORED_TABLES`。

---

## 数据库迁移

- 开发/测试：`db/migration/h2`
- 生产：`db/migration/mysql`
- 新增变更：`V3__xxx.sql`，禁止修改已执行脚本。

生产启动：

```bash
SPRING_PROFILES_ACTIVE=prod \
DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=btr DB_USER=btr DB_PASSWORD=*** \
JWT_SECRET=*** \
java -jar backend/target/btr-platform-0.1.0-SNAPSHOT.jar
```

---

## 部署

```bash
cd deploy
cp .env.example .env
# 修改密码与 JWT_SECRET
docker compose up -d --build
```

- 前端：<http://服务器IP:8088>
- 后端：<http://服务器IP:8080>
- MySQL：`localhost:3306`

详见 [`deploy/README.md`](deploy/README.md)。

---

## 手机 App

### PWA（已就绪）

- Android Chrome：打开站点 → 菜单 → 安装应用
- iOS Safari：分享 → 添加到主屏幕
- 离线缓存：`public/sw.js`
- 图标：`npm run icons`

### 原生壳（可选）

`frontend/capacitor.config.ts` 已配置。安装 Android Studio / Xcode 后：

```bash
npm i -D @capacitor/cli @capacitor/core
npm i @capacitor/android @capacitor/ios
npx cap add android
npx cap add ios
npm run build
npx cap sync
```

---

## 测试

```bash
cd backend
./mvnw test

cd ../frontend
npm run build
```

已覆盖：

- Spring 上下文 + Flyway 迁移
- 登录返回 JWT
- 未登录访问受保护接口被拒绝

---

## 目录

```text
btr-platform/
├── backend/                 Spring Boot 4.1.2 + MyBatis-Plus + Flyway
│   └── src/main/java/com/btr/platform/
│       ├── common/          响应 / 错误码 / 异常
│       ├── config/          Security / MyBatis-Plus / OpenAPI
│       ├── controller/      Auth / Tenant / Contest / Signup / Work /
│       │                    Rubric / Judge / Sponsor / Certificate /
│       │                    Notification / Dashboard / Health
│       ├── dto/             请求与视图
│       ├── entity/          14 个实体
│       ├── mapper/          MyBatis-Plus Mapper
│       ├── security/        JWT / 过滤器 / 当前用户
│       ├── service/         业务层
│       └── tenant/          租户上下文 / 拦截器
├── frontend/                Vue 3 + Vite + TypeScript
│   ├── src/views/           公开站 / 管理端 / 评委端 / 个人中心
│   └── public/brand/        Logo / PWA 图标
├── deploy/                  Docker Compose + Nginx
├── docs/                    P1 状态
└── .github/workflows/       CI
```

---

## 下一步

1. 按 [`../上线后操作清单.md`](../上线后操作清单.md) 完成改密码、站点、邮件、上传、报名费配置。
2. 在线支付（微信/支付宝服务商）
3. 商品商城（P2）
4. 多轮次 / 多赛道 / 团队赛 UI
5. 评审申诉 / 仲裁 / 归一化
6. 对象存储 / PDF 证书 / 邮件模板 / 原生 APK

---

## Logo

- 方形：`frontend/public/brand/logo.svg`
- 横版深色：`logo-horizontal.svg`
- 横版浅色：`logo-horizontal-light.svg`
- favicon：`favicon.svg`

> 按提供的 BTR 标识重绘为 SVG，可替换为原始设计源文件。
