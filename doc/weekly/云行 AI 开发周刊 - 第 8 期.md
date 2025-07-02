## 云行 AI

云行 AI 后端使用 SpringBoot 框架开发，其中 SpringBoot 的 IoC 容器管理非常便捷。然而，如果没有合理规划架构和依赖，IoC 也会给系统埋下诸多隐患。IoC 默认采用单例模式实例化并管理对象，这容易导致习惯使用依赖管理的开发者将有状态的对象也放入 IoC 容器中，从而埋下安全隐患。此外，当架构层次不清晰时，复杂项目很容易出现依赖混乱、循环依赖等问题。

因此，我们对云行 AI 后端框架进行了进一步重构优化。所有工具类都重构为静态工具类，不再参与 IoC 管理。对于有状态的实例或需要参数配置的对象，则采用 Builder 模式灵活构建，同样不参与 IoC 管理。这些改进使整体代码更加简洁，结构也更加清晰了。

## 开源推荐

1. **Claude Code**

地址: <https://github.com/anthropics/claude-code>

Claude Code 是一个智能编码工具，它存在于你的终端中，理解你的代码库，并通过执行常规任务、解释复杂代码和处理 git 工作流程，帮助你更快地编码——所有这些都可以通过自然语言命令完成。

2. **Gemini CLI**

地址: <https://github.com/google-gemini/gemini-cli>

Gemini CLI 是基于Google Gemini 的对标 Claude code 和 Cursor 的开源命令行AI工具，主要用于在终端中快速查询、编辑大型代码库，自动化开发和运维任务。它支持多模态输入，能生成代码、处理文档，还能集成搜索和多种扩展工具，提升开发效率。

3. **Tersa**

地址: <https://github.com/haydenbleasel/tersa>

Tersa 是一个开源的画布，用于构建 AI 工作流。拖放、连接并运行节点来构建您自己的由各种行业领先 AI 模型提供支持的工作流。

4. **Pickaxe**

地址: <https://github.com/hatchet-dev/pickaxe>

Pickaxe 是一个简单的 TypeScript 库，用于构建具有容错性和可扩展性的 AI 代理。

5. **Twocast**

地址: <https://github.com/panyanyany/Twocast>

真人对话AI播客生成器，多语言，多音色。

6. **Dual AI Chat**

地址: <https://github.com/yeahhe365/Dual-AI-Chat>

一个先进的聊天应用，演示了一种独特的对话范式：用户的查询首先由两个不同的人工智能角色进行辩论和提炼，然后才提供最终的综合答案。该项目利用 Google Gemini API 驱动一个逻辑型 AI (Cognito) 和一个怀疑型 AI (Muse)，它们协作生成更健壮、准确和经过严格审查的响应。

7. **DPanel**

地址: <https://github.com/donknap/dpanel>

Docker 可视化面板系统，提供完善的 docker 管理功能。

## **云行 AI 开源地址**

Github：<https://github.com/boyazuo/yunxing-ai>

Gitee：<https://gitee.com/yxboot/yunxing-ai>

欢迎关注。