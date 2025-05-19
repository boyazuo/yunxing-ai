# 云行 AI - AI 应用构建平台

云行 AI 是一个功能强大的 AI 应用构建平台，旨在简化 AI 应用的开发和部署过程。通过提供丰富的工具和接口，开发者可以快速构建、定制和部署 AI 解决方案。

## 界面展示
![](https://github.com/boyazuo/yunxing-ai/blob/main/web/public/images/login.png?raw=true)
![](https://github.com/boyazuo/yunxing-ai/blob/main/web/public/images/chat.png?raw=true)
![](https://github.com/boyazuo/yunxing-ai/blob/main/web/public/images/apps.png?raw=true)

## 项目架构

项目采用前后端分离架构：

### 前端架构

- **框架**：基于 [Next.js 15.3.1](https://nextjs.org) 构建 + Turbopack
- **语言**：TypeScript
- **UI 组件**：[shadcn/ui](https://ui.shadcn.com/)、[@radix-ui/react-*](https://www.radix-ui.com/)
- **样式**：[TailwindCSS 4](https://tailwindcss.com/)
- **状态管理**：React Hooks
- **表单处理**：react-hook-form + zod 验证
- **国际化**：next-intl 4
- **认证**：next-auth 4
- **HTTP请求**：axios
- **UI动画**：framer-motion
- **Markdown渲染**：react-markdown + remark-gfm
- **图标**：lucide-react + react-icons
- **提示通知**：sonner
- **代码格式化**：Biome
- **包管理工具**：pnpm

### 后端架构

- **语言**：Java 17
- **框架**：SpringBoot 3.4.5
- **安全认证**：Spring Security + JWT (jjwt 0.12.6)
- **数据库**：MySQL 8.3 + MyBatis-Plus 3.5.11
- **连接池**：Druid 1.2.24
- **缓存**：Redis
- **工具库**：Hutool 5.8.36、Lombok
- **API文档**：SpringDoc OpenAPI 2.8.6
- **HTTP客户端**：OkHttp、Retrofit
- **云存储**：阿里云OSS
- **响应式编程**：Webflux、Reactor
- **AI集成**：LLM模块集成多种AI模型
- **邮件服务**：Spring Mail

## 目录结构

```
yunxing-ai/
├── web/                 # 前端代码
│   ├── app/             # Next.js应用目录
│   │   ├── [locale]/    # 国际化路由
│   │   ├── api/         # API路由处理
│   │   └── globals.css  # 全局样式
│   ├── components/      # 可复用的UI组件
│   ├── lib/             # 工具函数和通用逻辑
│   ├── hooks/           # 自定义React hooks
│   ├── i18n/            # 国际化配置
│   ├── types/           # TypeScript类型定义
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
|   |   |    ├── system  # 系统模块
|   |   |    └── ai      # AI模型模块
│   │   ├── util/        # 工具类
│   │   ├── llm/         # LLM模型集成
│   │   └── ApiApplication.java # 应用入口类
│   └── pom.xml          # Maven依赖管理
│
└── doc/                 # 项目文档
    ├── sql/             # 数据库脚本
    ├── dev_log/         # 开发日志
    └── weekly/          # 周报
```

## 数据库结构

系统采用 MySQL 8.3 数据库，主要表结构包括：

- **账户管理**：tenant（租户表）、user（用户表）、tenant_user（租户用户关联表）
- **应用管理**：app（应用表）、app_config（应用配置表）
- **对话管理**：conversation（会话表）、message（消息表）
- **模型管理**：model（模型表）、provider（模型提供商表）

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
- **多语言支持**：基于 next-intl 实现国际化
- **组件驱动设计**：基于 shadcn/ui 和 Radix UI 的可访问性和可定制性
- **安全认证**：Spring Security 结合 JWT 提供可靠的用户认证
- **数据库优化**：MyBatis-Plus 和 Druid 提供高效的数据访问和连接池管理
- **AI 能力**：集成多种大语言模型
- **类型安全**：全栈 TypeScript 和严格类型检查


## 交流协作

云行AI目前处于起步阶段，我们记录了完整的开发过程和技术思考。欢迎有志于AI应用开发的同道者加入我们，一起探索AI应用开发的无限可能。无论您是前端工程师、后端开发者，还是对AI应用感兴趣的任何人，都可以通过以下方式参与:

- **Star & Fork**: 在[GitHub](https://github.com/boyazuo/yunxing-ai)或[Gitee](https://gitee.com/yxboot/yunxing-ai)上关注项目
- **贡献代码**: 提交PR，参与功能开发和问题修复
- **技术讨论**: 分享您的想法和建议
- **加入社区**: 扫描下方二维码添加微信 (备注: 云行AI)

我们相信，通过开源协作的力量，可以共同打造更加强大和易用的AI应用构建平台。期待与您一起，见证云行AI的成长与蜕变！

<img src="https://yxboot-oss.oss-cn-beijing.aliyuncs.com/images/weixin-boya.png" width="160" />
