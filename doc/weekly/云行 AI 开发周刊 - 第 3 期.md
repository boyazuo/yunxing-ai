# 云行AI

本周主要进行知识库的开发工作。之前项目的知识库是使用 Python 语言开发的，在云行 AI 项目中，我们选择用 Java 重写了这部分功能。考虑到平台建设规划，在大模型和向量模型的调用上，我们有更多细节性的构想。为了确保最大的灵活性，我们没有采用 SpringAI 等框架，而是选择自行封装实现。目前，对话模型和嵌入模型的基本架构已经封装完毕，我们将在后续功能实现过程中持续优化这个架构，架构的实现主要借鉴了 SpringAI 和 LangChain4j 。

# 开源推荐

1. **MongoDB's GenAI Showcase**

地址: <https://github.com/mongodb-developer/GenAI-Showcase>

MongoDB 发布了一个包含 100 多个关于 AI Agents 和 RAG 的分步笔记本的仓库。涵盖从如何构建聊天机器人到 AI Agents 的各个方面。

2. **DeepNote**

地址: <https://github.com/thunlp/DeepNote>

以笔记为中心的深度检索增强型生成框架，让知识探索更深入、更精准。通过笔记精炼知识，提升检索效率；支持多种检索方法，适配不同数据集；自动评估知识增长，动态选择最佳笔记生成答案。

3. **Multimodal & Large Language Models**

地址: <https://github.com/Yangyi-Chen/Multimodal-AND-Large-Language-Models>

一个 UIUC 的计算机博士生整理的自己从24年到现在在阅读的一些不错观点的多模态和大规模语言模型的论文列表。

4. **MoneyPrinterTurbo**

地址: <https://github.com/harry0703/MoneyPrinterTurbo>

只需提供一个视频主题或关键词 ，就可以全自动生成视频文案、视频素材、视频字幕、视频背景音乐，然后合成一个高清的短视频。

5. **Mage AI**

地址: <https://github.com/mage-ai/mage-ai>

Mage 是一个用于数据转换和集成的混合框架。它结合了两个世界的最佳之处：笔记本的灵活性以及模块化代码的严谨性。

6. **Higress:** <https://github.com/alibaba/higress>

Higress 是一款云原生 API 网关，内核基于 Istio 和 Envoy，可以用 Go/Rust/JS 等编写 Wasm 插件，提供了数十个现成的通用插件，以及开箱即用的控制台。

Higress 的 AI 网关能力支持国内外所有[主流模型供应商](https://github.com/alibaba/higress/tree/main/plugins/wasm-go/extensions/ai-proxy/provider)和基于 vllm/ollama 等自建的 DeepSeek 模型。同时，Higress 支持通过插件方式托管 MCP (Model Context Protocol) 服务器，使 AI Agent 能够更容易地调用各种工具和服务。借助 [openapi-to-mcp 工具](https://github.com/higress-group/openapi-to-mcpserver)，您可以快速将 OpenAPI 规范转换为远程 MCP 服务器进行托管。Higress 提供了对 LLM API 和 MCP API 的统一管理。

7. **Langflow**

地址: <https://github.com/langflow-ai/langflow>

Langflow 是一个强大的工具，用于构建和部署 AI 驱动的代理和工作流。它为开发者提供了可视化的创作体验和内置的 API 服务器，将每个代理转换为一个 API 端点，可以集成到基于任何框架或技术栈构建的应用程序中。Langflow 一应俱全，并支持所有主要的 LLM、向量数据库以及不断增长的 AI 工具库。

8. **II Agent**

地址: <https://github.com/Intelligent-Internet/ii-agent>

这是一个构建和部署智能体的全新开源框架。它基于大型语言模型，可支持多领域的自动化任务和智能交互。该框架具备多模态处理、网络搜索、代码生成和数据分析等强大功能，能帮助用户高效完成科研、内容创作和软件开发等工作。

9. **MCP Server Chart**

地址: <https://github.com/antvis/mcp-server-chart>

这是一个基于 TypeScript 的强大 MCP 服务器，专门用于可视化图表生成。通过集成 AntV 技术，用户可以轻松创建多种类型的图表。目前已支持超过 15 种不同的图表类型，让数据可视化变得简单直观。

# 云行 AI 开源地址
- Github：<https://github.com/boyazuo/yunxing-ai>
- Gitee：<https://gitee.com/yxboot/yunxing-ai>