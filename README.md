# 云行 AI - AI 应用构建平台

云行 AI 是一个功能强大的 AI 应用构建平台，旨在简化 AI 应用的开发和部署过程。通过提供丰富的工具和接口，开发者可以快速构建、定制和部署 AI 解决方案。

## 界面展示

![](https://github.com/boyazuo/yunxing-ai/blob/main/web/public/images/login.png?raw=true)
![](https://github.com/boyazuo/yunxing-ai/blob/main/web/public/images/chat.png?raw=true)
![](https://github.com/boyazuo/yunxing-ai/blob/main/web/public/images/apps.png?raw=true)

## 核心功能

- **账户与权限**：多租户（工作空间）管理、用户认证、团队成员与邀请
- **AI 应用构建**：应用创建与配置、系统提示词、变量、知识库绑定
- **对话能力**：流式对话（SSE）、会话与消息管理
- **知识库（RAG）**：文档上传与解析（PDF / Word）、多种分段策略、向量检索、严格模式问答
- **文件管理**：本地 / 阿里云 OSS 文件存储

## 项目架构

项目采用前后端分离架构：

### 前端架构

- **框架**：[Next.js 15.3](https://nextjs.org)（App Router）+ Turbopack
- **语言**：TypeScript、React 19
- **UI 组件**：[shadcn/ui](https://ui.shadcn.com/)、[Radix UI](https://www.radix-ui.com/)
- **样式**：[TailwindCSS 4](https://tailwindcss.com/)
- **状态管理**：React Hooks
- **表单处理**：react-hook-form + zod 验证
- **国际化**：next-intl 4
- **认证**：next-auth 4
- **HTTP 请求**：axios
- **UI 动画**：framer-motion
- **Markdown 渲染**：react-markdown + remark-gfm
- **图标**：lucide-react + react-icons
- **提示通知**：sonner
- **代码格式化**：Biome
- **包管理工具**：pnpm

### 后端架构

- **语言**：Java 17
- **框架**：Spring Boot 3.5
- **安全认证**：Spring Security + JWT（jjwt 0.12.6）
- **数据库**：MySQL 8.3 + MyBatis-Flex 1.10.9
- **连接池**：Druid 1.2.24
- **缓存**：Redis
- **向量数据库**：Qdrant（Spring AI Vector Store）
- **工具库**：Hutool 5.8.36、Lombok
- **API 文档**：SpringDoc OpenAPI 2.8.6
- **HTTP 客户端**：OkHttp
- **云存储**：阿里云 OSS
- **响应式编程**：WebFlux、Reactor（流式对话）
- **AI 集成**：Spring AI 1.1.6（智谱 AI、Ollama、OpenAI 兼容 / DashScope）
- **文档处理**：Apache PDFBox、Apache POI
- **邮件服务**：Spring Mail

## 目录结构

```
yunxing-ai/
├── web/                          # 前端代码
│   ├── app/                      # Next.js App Router
│   │   ├── [locale]/             # 国际化路由
│   │   │   ├── (auth)/           # 登录、注册
│   │   │   └── (main)/           # 主界面（应用、知识库、设置等）
│   │   └── api/                  # NextAuth 等 API 路由
│   ├── components/               # 可复用 UI 组件
│   ├── lib/                      # 工具函数与 API 客户端
│   ├── hooks/                    # 自定义 React Hooks
│   ├── i18n/                     # 国际化配置
│   ├── types/                    # TypeScript 类型定义
│   ├── api/                      # 后端 API 封装
│   └── public/                   # 静态资源
│
├── api/                          # Java 后端服务
│   ├── src/main/java/com/yxboot/
│   │   ├── common/               # 通用工具、异常、统一响应
│   │   ├── config/               # 安全、Web、上传等配置
│   │   ├── ai/                   # AI 能力（Spring AI、RAG、文档处理、向量存储）
│   │   ├── modules/              # 业务模块
│   │   │   ├── account/          # 账户权限（租户、用户、邀请）
│   │   │   ├── app/              # AI 应用与配置
│   │   │   ├── dataset/          # 知识库、文档、分段
│   │   │   ├── ai/               # 对话、会话、消息
│   │   │   └── system/           # 系统（文件、邮件）
│   │   ├── util/                 # 工具类
│   │   └── ApiApplication.java   # 应用入口
│   └── pom.xml
│
└── doc/                          # 项目文档
    ├── sql/                      # 数据库脚本与迁移
    ├── dev_log/                  # 开发日志
    └── weekly/                   # 开发周刊
```

## 数据库结构

系统采用 MySQL 8.3，主要表结构包括：

- **账户管理**：`tenant`（租户）、`user`（用户）、`tenant_user`（租户成员）、`invitation`（邀请）
- **应用管理**：`app`（应用）、`app_config`（应用配置）
- **对话管理**：`conversation`（会话）、`message`（消息）
- **知识库**：`dataset`（知识库）、`dataset_document`（文档）、`dataset_document_segment`（文档分段）
- **系统**：`sys_file`（附件）

完整建表脚本见 [`doc/sql/db_schema.sql`](doc/sql/db_schema.sql)。已有数据库升级请按 [`doc/sql/`](doc/sql/) 下的迁移脚本顺序执行。

## 开发环境搭建

### 前置依赖

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| Node.js | 20+ | 前端运行环境 |
| pnpm | 最新 | 前端包管理 |
| JDK | 17+ | 后端运行环境 |
| Maven | 3.8+ | 后端构建 |
| MySQL | 8.3+ | 业务数据 |
| Redis | 7.0+ | 缓存 |
| Qdrant | 最新 | 向量存储（默认端口 6334） |

### 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE yxboot DEFAULT CHARACTER SET utf8mb4;"

# 导入表结构
mysql -u root -p yxboot < doc/sql/db_schema.sql
```

### 后端开发环境

1. 复制开发配置模板并填写本地信息：

```bash
cd api/src/main/resources
cp application-dev.template.yml application-dev.yml
```

2. 在 `application-dev.yml` 中配置 MySQL、Redis、邮件，以及 AI 相关密钥：

```yaml
yxboot:
  ai:
    chat:
      provider: zhipuai
      api-key: "your-zhipuai-api-key"
    embedding:
      provider: dashscope      # dashscope | ollama | zhipuai
      api-key: ${DASHSCOPE_API_KEY:}
```

3. 启动 Qdrant（向量检索依赖，可使用 Docker）：

```bash
docker run -p 6333:6333 -p 6334:6334 qdrant/qdrant
```

4. 构建并运行：

```bash
cd api
mvn clean package
java -jar target/yunxing-api-0.0.1-SNAPSHOT.jar
```

后端默认地址：[http://localhost:8080](http://localhost:8080)  
API 文档（Swagger UI）：[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 前端开发环境

1. 安装依赖并启动：

```bash
cd web
pnpm install
pnpm dev
```

2. 可选：在 `web/.env.local` 中配置环境变量：

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/v1/api
NEXTAUTH_SECRET=your-nextauth-secret
```

3. 访问开发服务器：[http://localhost:3000](http://localhost:3000)

## 技术特性

- **现代前端架构**：Next.js 15 + React 19 + Turbopack，Server / Client Components 混合渲染
- **多语言支持**：基于 next-intl 的国际化路由
- **组件驱动设计**：shadcn/ui + Radix UI，可访问性与可定制性兼顾
- **安全认证**：Spring Security + JWT，多租户隔离
- **RAG 知识库**：文档解析、Parent-Child 分段、Qdrant 向量检索、严格模式问答
- **流式对话**：基于 WebFlux / SSE 的实时 AI 回复
- **Spring AI 集成**：智谱 AI、DashScope、Ollama 等多 Provider 支持
- **类型安全**：前端 TypeScript 严格模式，后端强类型 DTO

## 交流协作

云行 AI 目前处于起步阶段，我们记录了完整的开发过程和技术思考。欢迎有志于 AI 应用开发的同道者加入我们，一起探索 AI 应用开发的无限可能。无论您是前端工程师、后端开发者，还是对 AI 应用感兴趣的任何人，都可以通过以下方式参与：

- **Star & Fork**：在 [GitHub](https://github.com/boyazuo/yunxing-ai) 或 [Gitee](https://gitee.com/yxboot/yunxing-ai) 上关注项目
- **贡献代码**：提交 PR，参与功能开发和问题修复
- **技术讨论**：分享您的想法和建议
- **加入社区**：扫描下方二维码添加微信（备注：云行 AI）

我们相信，通过开源协作的力量，可以共同打造更加强大和易用的 AI 应用构建平台。期待与您一起，见证云行 AI 的成长与蜕变！

<img src="https://yxboot-oss.oss-cn-beijing.aliyuncs.com/images/weixin-boya.png" width="160" />
