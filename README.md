# 云行 AI - AI 应用构建平台

云行 AI 是一个功能强大的 AI 应用构建平台，旨在简化 AI 应用的开发和部署过程。通过提供丰富的工具和接口，开发者可以快速构建、定制和部署 AI 解决方案。

## 项目架构

项目采用前后端分离架构：

### 前端架构

- **框架**：基于 [Next.js 15.3](https://nextjs.org) 构建 + Turbopack
- **语言**：TypeScript
- **UI 组件**：[shadcn/ui](https://ui.shadcn.com/)、[@radix-ui/react-*](https://www.radix-ui.com/)
- **样式**：[TailwindCSS 4](https://tailwindcss.com/)
- **状态管理**：React Hooks
- **表单处理**：react-hook-form + zod
- **国际化**：next-intl
- **认证**：next-auth
- **HTTP请求**：axios
- **代码格式化**：Biome
- **包管理工具**：pnpm

### 后端架构

- **语言**：Java 17
- **框架**：SpringBoot 3.4.5
- **安全认证**：Spring Security + JWT
- **数据库**：MySQL 8.3 + MyBatis-Plus 3.5.11
- **连接池**：Druid 1.2.24
- **缓存**：Redis
- **工具库**：Hutool 5.8.36、Lombok
- **API文档**：SpringDoc OpenAPI
- **AI集成**：Spring AI (预置集成)
- **邮件服务**：Spring Mail

## 目录结构

```
yunxing-ai/
├── web/                 # 前端代码
│   ├── app/             # Next.js应用目录
│   ├── components/      # 可复用的UI组件
│   ├── lib/             # 工具函数和通用逻辑
│   ├── hooks/           # 自定义React hooks
│   ├── i18n/            # 国际化文件
│   ├── types/           # TypeScript类型定义
│   ├── auth/            # 认证相关代码
│   ├── api/             # API客户端代码
│   └── public/          # 静态资源
│
├── api/                 # Java后端服务
│   ├── src/main/java/com/yxboot/
│   │   ├── common/      # 通用工具和基础类
│   │   ├── config/      # 配置类
│   │   ├── modules/     # 业务模块
|   |   |    ├── account # 账户权限模块
|   |   |    ├── app     # 应用模块
|   |   |    ├── chat    # 会话模块
|   |   |    ├── dataset # 知识库模块
|   |   |    └── model   # 模型管理模块
│   │   └── util/        # 工具类
│   └── pom.xml          # Maven依赖管理
│
└── doc/                 # 项目文档
```

## 开发环境搭建

### 前端开发环境

1. 安装依赖

```bash
cd web
pnpm install
```

2. 启动开发服务器

```bash
pnpm dev
```

3. 访问开发服务器
   打开浏览器访问 [http://localhost:3000](http://localhost:3000)

### 后端开发环境

1. 安装 JDK 17 或更高版本
2. 准备 MySQL 8.3+ 和 Redis 7.0+ 环境
3. 使用 Maven 构建项目

```bash
cd api
mvn clean package
```

4. 运行 SpringBoot 应用

```bash
java -jar target/yunxing-api-0.0.1-SNAPSHOT.jar
```

## 技术特性

- **现代前端架构**：使用 Next.js 15 和 Turbopack 提供卓越的开发体验和性能
- **组件驱动设计**：基于 shadcn/ui 和 Radix UI 的可访问性和可定制性
- **安全认证**：Spring Security 结合 JWT 提供可靠的用户认证
- **数据库优化**：MyBatis-Plus 和 Druid 提供高效的数据访问和连接池管理
- **AI 能力**：集成 Spring AI 便于对接各种大语言模型
- **国际化支持**：使用 next-intl 实现多语言本地化
- **类型安全**：全栈 TypeScript 和严格类型检查

## 贡献指南

欢迎贡献代码、报告问题或提出新功能建议。请遵循项目的编码规范和贡献流程。