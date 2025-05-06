# 云行 AI 前端项目

这是云行 AI 平台的前端项目，使用 [Next.js 15](https://nextjs.org) 框架构建。

## 技术栈

- **核心框架**：TypeScript, React 19, Next.js 15.3 (App Router) + Turbopack
- **UI组件**：shadcn/ui + Radix UI组件库
- **样式**：TailwindCSS 4
- **状态管理**：React Hooks
- **表单处理**：react-hook-form + zod验证
- **国际化**：next-intl
- **认证**：next-auth
- **HTTP请求**：axios
- **代码格式化**：Biome
- **包管理器**：pnpm

## 开发环境

首先，运行开发服务器:

```bash
# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev
```

在浏览器中打开 [http://localhost:3000](http://localhost:3000) 查看应用。

修改 `app/page.tsx` 文件后，页面会自动更新。

## 项目结构

```
web/
├── app/               # Next.js App Router
│   ├── api/           # API 路由
│   ├── (auth)/        # 认证相关页面
│   ├── (dashboard)/   # 应用主界面
│   └── ...
├── components/        # 共享组件
│   ├── ui/            # UI 基础组件 (shadcn/ui)
│   └── ...
├── lib/               # 工具函数和共享逻辑
├── types/             # TypeScript 类型定义
├── styles/            # 全局样式
└── public/            # 静态资源
```

## 编码规范

- 使用 TypeScript 进行类型安全开发
- 优先使用 React Server Components
- 遵循声明式编程范式
- 使用 Tailwind CSS 实现响应式设计
- 通过 Biome 进行代码格式化和检查

## 构建与部署

```bash
# 构建生产版本
pnpm build

# 本地启动生产版本
pnpm start
```

## 更多资源

- [Next.js 文档](https://nextjs.org/docs)
- [React 文档](https://react.dev)
- [TailwindCSS 文档](https://tailwindcss.com/docs)
- [shadcn/ui 组件](https://ui.shadcn.com)
