# 部署说明

## 方式一：Docker Compose（推荐）

```bash
cd deploy
cp .env.example .env
# 修改 .env 中的密码与 JWT_SECRET
docker compose up -d --build
```

访问：

- 前端：<http://服务器IP:8088>
- 后端健康检查：<http://服务器IP:8080/api/v1/health>
- Swagger：<http://服务器IP:8080/swagger-ui.html>

首次启动会自动执行 Flyway 迁移并创建演示数据。

### 生产前必须改

| 变量 | 说明 |
|---|---|
| `JWT_SECRET` | 至少 32 字节随机字符串 |
| `DB_PASSWORD` | MySQL 业务账号密码 |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 |
| `ADMIN_PASSWORD` | 平台管理员密码 |
| `FRONTEND_PORT` / `BACKEND_PORT` | 对外端口 |

### 备份

```bash
docker exec btr-mysql mysqldump -ubtr -p"$DB_PASSWORD" btr > btr-$(date +%F).sql
```

上传目录/对象存储另行备份。

## 方式二：传统部署

1. MySQL 8 + Redis（可选）
2. 构建后端：`cd backend && ./mvnw -DskipTests package`
3. 启动：`SPRING_PROFILES_ACTIVE=prod DB_HOST=... DB_PASSWORD=... JWT_SECRET=... java -jar target/btr-platform-0.1.0-SNAPSHOT.jar`
4. 构建前端：`cd frontend && npm ci && npm run build`
5. 把 `frontend/dist` 交给 Nginx，`/api/` 反代到后端 8080。

## 手机 App

- PWA：Android Chrome / iOS Safari 打开站点后「添加到主屏幕」。
- 原生壳：`frontend/capacitor.config.ts`，安装 Android Studio/Xcode 后按注释执行 Capacitor 打包。
