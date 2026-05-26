# 云行 AI Docker 部署说明

本文档介绍如何使用 Docker 部署云行 AI 全栈项目，包含 Qdrant、后端 API 与前端 Web 服务。业务数据库与缓存均使用外部托管服务（阿里云 RDS MySQL、外部 Redis），不在 Docker 内自建 MySQL / Redis 容器。

生产环境推荐使用**宿主机 Nginx + Docker**：Docker 只跑应用容器，由服务器上已有的 Nginx 统一对外提供 80/443 入口。

## 目录

- [架构概览](#架构概览)
- [前置要求](#前置要求)
- [文件说明](#文件说明)
- [快速开始](#快速开始)
- [环境变量配置](#环境变量配置)
- [部署模式](#部署模式)
- [常用运维命令](#常用运维命令)
- [数据持久化与备份](#数据持久化与备份)
- [数据库迁移](#数据库迁移)
- [故障排查](#故障排查)
- [生产环境建议](#生产环境建议)

## 架构概览

```
┌─────────────────────────────────────────────────────────┐
│                      浏览器 / 用户                        │
└───────────────────────────┬─────────────────────────────┘
                            │
                   ┌────────▼────────┐
                   │  宿主机 Nginx    │  :80 / :443
                   │  (生产环境)      │
                   └────────┬────────┘
                            │ 127.0.0.1
              ┌─────────────┴─────────────┐
              │                           │
       ┌──────▼──────┐             ┌──────▼──────┐
       │  web:3000   │             │  api:8080   │
       │  (Docker)   │             │  (Docker)   │
       └─────────────┘             └──────┬──────┘
                                          │
                                   ┌──────▼──────┐
                                   │   qdrant    │
                                   │  (Docker)   │
                                   └─────────────┘
                            ├──────────────────────────► 阿里云 RDS (MySQL)
                            └──────────────────────────► 外部 Redis
```

| 服务 | 说明 | 端口 |
|------|------|------|
| 宿主机 Nginx | 生产环境统一入口，反向代理到本机 Docker 容器 | 80 / 443 |
| 阿里云 RDS | 业务数据库（外部托管，需自行创建实例与库表） | 3306 |
| 外部 Redis | 缓存（外部托管，如阿里云 Redis） | 6379 |
| qdrant | 向量数据库（RAG 知识库），生产环境不对外暴露 | 6333 / 6334 |
| api | Java 后端（Spring Boot），生产环境仅监听 127.0.0.1 | 8080 |
| web | 前端（Next.js），生产环境仅监听 127.0.0.1 | 3000 |

## 前置要求

| 组件 | 版本要求 |
|------|----------|
| Docker | 24.0+ |
| Docker Compose | v2.20+（支持 `docker compose` 命令） |
| 宿主机 Nginx | 1.18+（生产环境） |
| 阿里云 RDS MySQL | 8.0+，已创建数据库实例与业务库 |
| 外部 Redis | 6.0+，部署服务器可访问 |
| 磁盘空间 | 建议 ≥ 10 GB |
| 内存 | 建议 ≥ 4 GB |

首次构建需要下载基础镜像并编译前后端，耗时约 5–15 分钟（视网络与机器性能而定）。

**RDS 准备事项：**

1. 在阿里云 RDS 控制台创建 MySQL 实例，并创建业务数据库（如 `yxboot`）。
2. 在 RDS 白名单中放行部署服务器的公网 IP（或内网 IP，若 ECS 与 RDS 同 VPC）。
3. 执行 `doc/sql/db_schema.sql` 初始化表结构（见 [数据库迁移](#数据库迁移)）。

**Redis 准备事项：**

1. 准备可访问的外部 Redis 实例（如阿里云 Redis）。
2. 在 Redis 白名单 / 安全组中放行部署服务器 IP。
3. 在 `.env` 中配置 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`。

## 文件说明

```
yunxing-ai/
├── docker-compose.yml          # 标准部署编排
├── docker-compose.prod.yml     # 生产模式叠加（本机端口 + 隐藏 Qdrant 对外端口）
├── .env.docker.example         # 环境变量模板
├── api/
│   ├── Dockerfile
│   └── src/main/resources/application-docker.yml
├── web/
│   └── Dockerfile
└── docker/
    ├── nginx/yunxing-ai.conf   # 宿主机 Nginx 站点配置
    └── scripts/
        ├── start.sh            # 启动服务
        ├── stop.sh             # 停止服务
        ├── build.sh            # 构建镜像并启动
        ├── logs.sh             # 查看日志
        └── clean.sh            # 清理容器与数据卷
```

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/boyazuo/yunxing-ai.git
cd yunxing-ai
```

### 2. 配置环境变量

```bash
cp .env.docker.example .env
```

编辑 `.env`，**至少**填写以下必填项：

```bash
# 阿里云 RDS
MYSQL_HOST=rm-xxxxxxxx.mysql.rds.aliyuncs.com
MYSQL_USER=your_rds_user
MYSQL_PASSWORD=your_rds_password

# 外部 Redis
REDIS_HOST=r-xxxxxxxx.redis.rds.aliyuncs.com
REDIS_PASSWORD=your_redis_password

AI_CHAT_API_KEY=你的智谱AI密钥
AI_EMBEDDING_API_KEY=你的DashScope密钥
JWT_SECRET=至少32位的随机字符串
NEXTAUTH_SECRET=至少32位的随机字符串
```

### 3. 初始化 RDS 数据库

若 RDS 中尚未建表，在本地执行：

```bash
mysql -h rm-xxxxxxxx.mysql.rds.aliyuncs.com -P 3306 -u your_rds_user -p yxboot < doc/sql/db_schema.sql
```

### 4. 启动服务

**方式一：使用脚本（推荐）**

```bash
chmod +x docker/scripts/*.sh
./docker/scripts/build.sh
```

**方式二：直接使用 Docker Compose**

```bash
docker compose up -d --build
```

### 5. 访问应用（本地调试）

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端页面 |
| http://localhost:8080 | 后端 API |
| http://localhost:8080/swagger-ui.html | API 文档 |

后端启动后整体就绪约需 30 秒–1 分钟。

## 环境变量配置

完整配置项见 `.env.docker.example`，以下为关键变量说明。

### 数据库（阿里云 RDS）

| 变量 | 示例 | 说明 |
|------|------|------|
| `MYSQL_HOST` | `rm-xxx.mysql.rds.aliyuncs.com` | RDS 连接地址（**必填**） |
| `MYSQL_PORT` | `3306` | RDS 端口，默认 3306 |
| `MYSQL_DATABASE` | `yxboot` | 数据库名 |
| `MYSQL_USER` | `yxboot` | RDS 数据库账号 |
| `MYSQL_PASSWORD` | — | RDS 数据库密码 |

> 部署服务器需能访问 RDS 网络（公网或 VPC 内网），并在 RDS 白名单中放行对应 IP。

### 缓存（外部 Redis）

| 变量 | 示例 | 说明 |
|------|------|------|
| `REDIS_HOST` | `r-xxx.redis.rds.aliyuncs.com` | Redis 连接地址（**必填**） |
| `REDIS_PORT` | `6379` | Redis 端口，默认 6379 |
| `REDIS_PASSWORD` | — | Redis 访问密码 |

> 部署服务器需能访问 Redis 网络（公网或 VPC 内网），并在 Redis 白名单中放行对应 IP。

### AI 服务（必填）

| 变量 | 说明 |
|------|------|
| `AI_CHAT_API_KEY` | 对话模型 API Key（默认 Provider：智谱 AI） |
| `AI_EMBEDDING_API_KEY` | 向量嵌入 API Key（默认 Provider：DashScope） |
| `AI_CHAT_PROVIDER` | 对话 Provider，默认 `zhipuai` |
| `AI_CHAT_MODEL` | 对话模型，默认 `glm-4-flash` |
| `AI_EMBEDDING_PROVIDER` | 嵌入 Provider，默认 `dashscope` |
| `AI_EMBEDDING_MODEL` | 嵌入模型，默认 `text-embedding-v4` |

### 前端与认证

| 变量 | 本地调试示例 | 生产环境示例 |
|------|-------------|-------------|
| `NEXT_PUBLIC_API_BASE_URL` | `http://localhost:8080/v1/api` | `https://your-domain.com/v1/api` |
| `NEXTAUTH_URL` | `http://localhost:3000` | `https://your-domain.com` |
| `NEXTAUTH_SECRET` | 随机字符串 | 随机字符串 |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | `https://your-domain.com` |
| `UPLOAD_URL_PREFIX` | `http://localhost:8080/` | `https://your-domain.com/` |

> **注意**：`NEXT_PUBLIC_API_BASE_URL` 在构建 Web 镜像时写入，修改后需重新构建：
>
> ```bash
> docker compose build web && docker compose up -d web
> ```

### 邮件（可选）

团队邀请功能需要配置 SMTP：

```bash
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=your-email@example.com
MAIL_PASSWORD=your-password
```

## 部署模式

### 本地调试模式

各服务直接暴露端口，适合本地开发、测试与调试。

```bash
./docker/scripts/start.sh
# 或
docker compose up -d
```

访问 http://localhost:3000 与 http://localhost:8080。

### 生产模式（宿主机 Nginx + Docker）

Docker 只运行 `qdrant`、`api`、`web` 三个容器。`api` / `web` 仅绑定 `127.0.0.1`，由服务器上已有的 Nginx 对外提供统一入口。

#### 第一步：修改 `.env` 中的对外地址

```bash
NEXT_PUBLIC_API_BASE_URL=https://your-domain.com/v1/api
NEXTAUTH_URL=https://your-domain.com
CORS_ALLOWED_ORIGINS=https://your-domain.com
UPLOAD_URL_PREFIX=https://your-domain.com/
```

#### 第二步：构建并启动 Docker 容器

```bash
./docker/scripts/build.sh --prod
# 或
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

生产模式下：
- `api` 监听 `127.0.0.1:8080`
- `web` 监听 `127.0.0.1:3000`
- `qdrant` 不对外暴露端口

#### 第三步：配置宿主机 Nginx

```bash
# 复制站点配置
sudo cp docker/nginx/yunxing-ai.conf /etc/nginx/sites-available/yunxing-ai

# 编辑 server_name 为你的域名
sudo nano /etc/nginx/sites-available/yunxing-ai

# 启用站点
sudo ln -sf /etc/nginx/sites-available/yunxing-ai /etc/nginx/sites-enabled/yunxing-ai

# 检查并重载
sudo nginx -t && sudo systemctl reload nginx
```

站点配置已包含以下路由规则：
- `/v1/api/` → `127.0.0.1:8080`（含 SSE 流式对话，已关闭缓冲）
- `/api/` → `127.0.0.1:3000`（NextAuth 等前端 API 路由）
- `/` → `127.0.0.1:3000`（前端页面）
- `/swagger-ui.html` 等 → `127.0.0.1:8080`（API 文档）

#### 第四步：配置 HTTPS（推荐）

```bash
sudo certbot --nginx -d your-domain.com
```

Certbot 会自动修改 Nginx 配置并启用 HTTPS。完成后确认 `.env` 中相关地址均为 `https://` 前缀，并重新构建 web 镜像。

#### 访问应用

| 地址 | 说明 |
|------|------|
| https://your-domain.com | 前端页面 |
| https://your-domain.com/v1/api | 后端 API |
| https://your-domain.com/swagger-ui.html | API 文档 |

## 常用运维命令

项目提供了便捷脚本，均位于 `docker/scripts/` 目录。

| 操作 | 命令 |
|------|------|
| 启动（本地调试） | `./docker/scripts/start.sh` |
| 启动（生产） | `./docker/scripts/start.sh --prod` |
| 构建并启动（本地） | `./docker/scripts/build.sh` |
| 构建并启动（生产） | `./docker/scripts/build.sh --prod` |
| 停止 | `./docker/scripts/stop.sh` |
| 停止（生产） | `./docker/scripts/stop.sh --prod` |
| 查看全部日志 | `./docker/scripts/logs.sh` |
| 查看单个服务日志 | `./docker/scripts/logs.sh api` |
| 清理容器与数据卷 | `./docker/scripts/clean.sh` |

也可直接使用 Docker Compose：

```bash
# 查看服务状态
docker compose ps

# 重启单个服务
docker compose restart api

# 仅重新构建后端
docker compose build api && docker compose up -d api

# 连接 RDS 数据库（需本地安装 mysql 客户端）
mysql -h ${MYSQL_HOST} -P ${MYSQL_PORT:-3306} -u ${MYSQL_USER} -p ${MYSQL_DATABASE}

# 查看后端健康状态（生产模式经 Nginx 访问）
curl http://127.0.0.1:8080/api-docs
```

**Nginx 相关：**

```bash
# 修改站点配置后重载
sudo nginx -t && sudo systemctl reload nginx

# 查看 Nginx 状态
sudo systemctl status nginx
```

## 数据持久化与备份

Docker Compose 使用命名数据卷持久化以下数据：

| 数据卷 | 内容 |
|--------|------|
| `qdrant_data` | Qdrant 向量索引 |
| `upload_data` | 后端本地上传文件 |

业务数据库由阿里云 RDS 托管，缓存由外部 Redis 托管，备份与恢复请使用对应云控制台或官方工具。

**备份 RDS 示例：**

```bash
mysqldump -h ${MYSQL_HOST} -P ${MYSQL_PORT:-3306} -u ${MYSQL_USER} -p ${MYSQL_DATABASE} > backup.sql
```

**恢复 RDS 示例：**

```bash
mysql -h ${MYSQL_HOST} -P ${MYSQL_PORT:-3306} -u ${MYSQL_USER} -p ${MYSQL_DATABASE} < backup.sql
```

> 执行 `clean.sh` 或 `docker compose down -v` 会**永久删除** Docker 数据卷（Qdrant、上传文件），**不会**影响 RDS 与外部 Redis 数据，但操作前仍建议确认已备份。

## 数据库迁移

首次部署需在 RDS 上执行 `doc/sql/db_schema.sql` 初始化表结构。若项目已有数据库版本升级，请按 `doc/sql/` 目录下迁移脚本的编号顺序手动执行：

```bash
# 示例：执行单个迁移脚本
mysql -h ${MYSQL_HOST} -u ${MYSQL_USER} -p ${MYSQL_DATABASE} < doc/sql/migration_19_reset_embedding_dashscope.sql
```

## 故障排查

### 后端启动失败 / 连接数据库超时

1. 确认 `.env` 中 `MYSQL_HOST`、`MYSQL_USER`、`MYSQL_PASSWORD` 配置正确
2. 确认 RDS 白名单已放行部署服务器 IP
3. 确认 RDS 实例与部署服务器网络互通（公网或同 VPC 内网）
4. 在部署服务器上测试连通性：`mysql -h ${MYSQL_HOST} -u ${MYSQL_USER} -p`
5. 查看后端日志：`./docker/scripts/logs.sh api`

### 后端启动失败 / 连接 Redis 超时

1. 确认 `.env` 中 `REDIS_HOST`、`REDIS_PASSWORD` 配置正确
2. 确认 Redis 白名单已放行部署服务器 IP
3. 在部署服务器上测试连通性：`redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT:-6379} -a ${REDIS_PASSWORD} ping`
4. 查看后端日志：`./docker/scripts/logs.sh api`

### 502 Bad Gateway（Nginx）

1. 确认 Docker 容器正常运行：`docker compose ps`
2. 确认 api/web 在本机可访问：
   ```bash
   curl http://127.0.0.1:8080/api-docs
   curl http://127.0.0.1:3000
   ```
3. 确认使用了 `--prod` 模式启动（端口绑定在 127.0.0.1）
4. 检查 Nginx 配置：`sudo nginx -t`
5. 查看 Nginx 错误日志：`sudo tail -f /var/log/nginx/error.log`

### 前端无法登录 / API 请求跨域

1. 确认 `NEXT_PUBLIC_API_BASE_URL` 指向浏览器可访问的后端地址（经 Nginx 的 `/v1/api` 路径）
2. 确认 `CORS_ALLOWED_ORIGINS` 包含前端实际访问地址
3. 确认 `NEXTAUTH_URL` 与浏览器地址栏一致
4. 修改 `NEXT_PUBLIC_API_BASE_URL` 后需重新构建 web 镜像

### AI 对话或知识库不可用

1. 确认 `.env` 中已填写 `AI_CHAT_API_KEY` 与 `AI_EMBEDDING_API_KEY`
2. 查看后端日志：`./docker/scripts/logs.sh api`
3. 确认 Qdrant 容器正常运行：`docker compose ps qdrant`

### 端口冲突

本地调试模式下，若 8080、3000 等端口已被占用，可在 `docker-compose.yml` 中修改 `ports` 映射。生产模式下 api/web 绑定本机端口，一般不与 Nginx 的 80/443 冲突。

### 查看容器资源占用

```bash
docker stats yunxing-qdrant yunxing-api yunxing-web
```

## 生产环境建议

1. **修改默认密码**：务必更换 `.env` 中 JWT、NextAuth 的默认密钥，并使用强密码的 RDS / Redis 账号。
2. **RDS / Redis 安全**：优先使用 VPC 内网连接；若走公网，务必配置白名单并启用自动备份。
3. **使用 HTTPS**：通过 Certbot 在宿主机 Nginx 上配置 TLS，并相应更新 `.env` 中所有对外地址为 `https://`。
4. **限制端口暴露**：生产模式使用 `--prod`，api/web 仅监听 127.0.0.1，Qdrant 不映射对外端口。
5. **文件存储**：默认使用容器内本地存储（`upload_data` 卷）。如需 OSS，修改 `application-docker.yml` 中的 `yxboot.upload` 配置。
6. **监控与日志**：建议接入 Docker 日志驱动或 ELK/Loki 等日志系统，并启用 RDS / Redis 自动备份与 `upload_data` 数据卷备份。
7. **资源规划**：知识库向量写入与 AI 对话对内存和 CPU 有一定要求，生产环境建议分配 ≥ 8 GB 内存。

---

如有问题，欢迎在 [GitHub Issues](https://github.com/boyazuo/yunxing-ai/issues) 反馈。
