# 云行 AI Docker 部署说明

本文档介绍如何使用 Docker 一键部署云行 AI 全栈项目，包含 MySQL、Redis、Qdrant、后端 API 与前端 Web 服务。

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
          ┌─────────────────┴─────────────────┐
          │  标准模式                            │  生产模式（--prod）
          │  web:3000  +  api:8080             │  nginx:80
          └─────────────────┬─────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
   ┌────▼────┐        ┌─────▼─────┐       ┌─────▼─────┐
   │   web   │        │    api    │       │  mysql    │
   │ Next.js │        │Spring Boot│       │  redis    │
   └─────────┘        └─────┬─────┘       │  qdrant   │
                            │             └───────────┘
                            └──────────────────────────►
```

| 服务 | 说明 | 默认端口 |
|------|------|----------|
| mysql | 业务数据库，首次启动自动执行建表脚本 | 3306 |
| redis | 缓存 | 6379 |
| qdrant | 向量数据库（RAG 知识库） | 6333 / 6334 |
| api | Java 后端（Spring Boot） | 8080 |
| web | 前端（Next.js） | 3000 |
| nginx | 反向代理（仅生产模式） | 80 |

## 前置要求

| 组件 | 版本要求 |
|------|----------|
| Docker | 24.0+ |
| Docker Compose | v2.20+（支持 `docker compose` 命令） |
| 磁盘空间 | 建议 ≥ 10 GB |
| 内存 | 建议 ≥ 4 GB |

首次构建需要下载基础镜像并编译前后端，耗时约 5–15 分钟（视网络与机器性能而定）。

## 文件说明

```
yunxing-ai/
├── docker-compose.yml          # 标准部署编排
├── docker-compose.prod.yml     # 生产模式叠加（Nginx 统一入口）
├── .env.docker.example         # 环境变量模板
├── api/
│   ├── Dockerfile
│   └── src/main/resources/application-docker.yml
├── web/
│   └── Dockerfile
└── docker/
    ├── nginx/nginx.conf        # Nginx 反向代理配置
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
AI_CHAT_API_KEY=你的智谱AI密钥
AI_EMBEDDING_API_KEY=你的DashScope密钥
JWT_SECRET=至少32位的随机字符串
NEXTAUTH_SECRET=至少32位的随机字符串
```

### 3. 启动服务

**方式一：使用脚本（推荐）**

```bash
chmod +x docker/scripts/*.sh
./docker/scripts/build.sh
```

**方式二：直接使用 Docker Compose**

```bash
docker compose up -d --build
```

### 4. 访问应用

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端页面 |
| http://localhost:8080 | 后端 API |
| http://localhost:8080/swagger-ui.html | API 文档 |

首次启动时，MySQL 容器会自动执行 `doc/sql/db_schema.sql` 初始化数据库表结构。后端需等待 MySQL 健康检查通过后才启动，整体就绪约需 1–2 分钟。

## 环境变量配置

完整配置项见 `.env.docker.example`，以下为关键变量说明。

### 数据库

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MYSQL_ROOT_PASSWORD` | root123456 | MySQL root 密码 |
| `MYSQL_DATABASE` | yxboot | 数据库名 |
| `MYSQL_USER` | root | 应用连接用户名 |
| `MYSQL_PASSWORD` | root123456 | 应用连接密码 |

### 缓存

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `REDIS_PASSWORD` | redis123456 | Redis 访问密码 |

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

| 变量 | 标准模式示例 | 说明 |
|------|-------------|------|
| `NEXT_PUBLIC_API_BASE_URL` | `http://localhost:8080/v1/api` | 浏览器访问的后端 API 地址（**构建时写入前端镜像**） |
| `NEXTAUTH_URL` | `http://localhost:3000` | NextAuth 回调地址 |
| `NEXTAUTH_SECRET` | 随机字符串 | NextAuth 加密密钥 |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | 后端允许的跨域来源，多个用英文逗号分隔 |
| `UPLOAD_URL_PREFIX` | `http://localhost:8080/` | 本地上传文件的访问 URL 前缀 |

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

### 标准模式

各服务独立暴露端口，适合本地开发、测试与调试。

```bash
./docker/scripts/start.sh
# 或
docker compose up -d
```

### 生产模式（Nginx 统一入口）

通过 Nginx 在 80 端口统一代理前端与后端，适合服务器部署。

**1. 修改 `.env` 中的对外地址：**

```bash
NEXT_PUBLIC_API_BASE_URL=http://你的域名或IP/v1/api
NEXTAUTH_URL=http://你的域名或IP
CORS_ALLOWED_ORIGINS=http://你的域名或IP
UPLOAD_URL_PREFIX=http://你的域名或IP/
```

**2. 重新构建并启动：**

```bash
./docker/scripts/build.sh --prod
# 或
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

**3. 访问：**

| 地址 | 说明 |
|------|------|
| http://你的域名或IP | 前端页面 |
| http://你的域名或IP/v1/api | 后端 API |
| http://你的域名或IP/swagger-ui.html | API 文档 |

生产模式下，api 与 web 不再直接对外暴露端口，所有流量经 Nginx 转发。Nginx 已针对 SSE 流式对话关闭缓冲，支持长连接。

## 常用运维命令

项目提供了便捷脚本，均位于 `docker/scripts/` 目录。

| 操作 | 命令 |
|------|------|
| 启动 | `./docker/scripts/start.sh` |
| 启动（生产） | `./docker/scripts/start.sh --prod` |
| 构建并启动 | `./docker/scripts/build.sh` |
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

# 进入 MySQL 容器
docker exec -it yunxing-mysql mysql -uroot -p

# 查看后端健康状态
curl http://localhost:8080/api-docs
```

## 数据持久化与备份

Docker Compose 使用命名数据卷持久化以下数据：

| 数据卷 | 内容 |
|--------|------|
| `mysql_data` | MySQL 数据库文件 |
| `redis_data` | Redis 持久化数据 |
| `qdrant_data` | Qdrant 向量索引 |
| `upload_data` | 后端本地上传文件 |

**备份 MySQL 示例：**

```bash
docker exec yunxing-mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} yxboot > backup.sql
```

**恢复 MySQL 示例：**

```bash
docker exec -i yunxing-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} yxboot < backup.sql
```

> 执行 `clean.sh` 或 `docker compose down -v` 会**永久删除**所有数据卷，操作前请确认已备份。

## 数据库迁移

首次部署由 `doc/sql/db_schema.sql` 自动初始化。若项目已有数据库版本升级，请按 `doc/sql/` 目录下迁移脚本的编号顺序手动执行：

```bash
# 示例：执行单个迁移脚本
docker exec -i yunxing-mysql mysql -uroot -p yxboot < doc/sql/migration_19_reset_embedding_dashscope.sql
```

## 故障排查

### 后端启动失败 / 连接数据库超时

1. 确认 MySQL 容器健康：`docker compose ps`
2. 查看 MySQL 日志：`./docker/scripts/logs.sh mysql`
3. 检查 `.env` 中 `MYSQL_PASSWORD` 是否与 `MYSQL_ROOT_PASSWORD` 一致（默认配置下两者相同）

### 前端无法登录 / API 请求跨域

1. 确认 `NEXT_PUBLIC_API_BASE_URL` 指向浏览器可访问的后端地址
2. 确认 `CORS_ALLOWED_ORIGINS` 包含前端实际访问地址
3. 修改 `NEXT_PUBLIC_API_BASE_URL` 后需重新构建 web 镜像

### AI 对话或知识库不可用

1. 确认 `.env` 中已填写 `AI_CHAT_API_KEY` 与 `AI_EMBEDDING_API_KEY`
2. 查看后端日志：`./docker/scripts/logs.sh api`
3. 确认 Qdrant 容器正常运行：`docker compose ps qdrant`

### 端口冲突

若本机 3306、6379、8080、3000 等端口已被占用，可在 `docker-compose.yml` 中修改 `ports` 映射，例如：

```yaml
ports:
  - "8081:8080"   # 将后端映射到 8081
```

同时更新 `.env` 中对应的 `NEXT_PUBLIC_API_BASE_URL`、`UPLOAD_URL_PREFIX`、`CORS_ALLOWED_ORIGINS`。

### 查看容器资源占用

```bash
docker stats yunxing-mysql yunxing-redis yunxing-qdrant yunxing-api yunxing-web
```

## 生产环境建议

1. **修改默认密码**：务必更换 `.env` 中 MySQL、Redis、JWT、NextAuth 的默认密钥。
2. **使用 HTTPS**：在 Nginx 前增加 TLS 终止（如 Let's Encrypt + Certbot），并相应更新 `NEXTAUTH_URL`、`CORS_ALLOWED_ORIGINS` 等地址为 `https://`。
3. **限制端口暴露**：生产模式下 api/web 已通过 compose 隐藏对外端口；MySQL、Redis、Qdrant 建议去掉 `ports` 映射，仅保留容器内网络访问。
4. **文件存储**：默认使用容器内本地存储（`upload_data` 卷）。如需 OSS，修改 `application-docker.yml` 中的 `yxboot.upload` 配置。
5. **监控与日志**：建议接入 Docker 日志驱动或 ELK/Loki 等日志系统，定期备份 `mysql_data` 与 `upload_data` 数据卷。
6. **资源规划**：知识库向量写入与 AI 对话对内存和 CPU 有一定要求，生产环境建议分配 ≥ 8 GB 内存。

---

如有问题，欢迎在 [GitHub Issues](https://github.com/boyazuo/yunxing-ai/issues) 反馈。
