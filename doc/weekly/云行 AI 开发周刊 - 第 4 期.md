## 云行 AI

对模型调用的封装是云行 AI 项目的第一个挑战。本周我们继续优化了框架的封装，目前架构已经相当完善。整体 AI 大模型调用架构分为四层：底层是各大模型提供商的原生 API 封装；上层是对这些原生 API 的通用封装，包含 ChatModel、EmbeddingModel 等核心接口。由于业务需要协调和配置各个模型，我们在 ChatModel 之上又封装了一层 ChatClient，负责模型的协调与配置。这样，业务层只需提供基本参数调用 ChatClient 即可。总的来说，这个架构符合预期设想，结构清晰，易于扩展，调用简单。

详细内容将在后续的开发日记中介绍。

我比较关注 AI 领域的项目和产品，因此本周刊的开源推荐以 AI 项目为主。这些都是近期看到的的优质项目，欢迎感兴趣的读者关注，我会持续更新推荐。

## 开源推荐

1. **AgenticSeek**

地址: <https://github.com/Fosowl/agenticSeek>

一个 **100% 本地替代 Manus AI** 的方案，这款支持语音的 AI 助理能够自主浏览网页、编写代码和规划任务，同时将所有数据保留在您的设备上。专为本地推理模型量身打造，完全在您自己的硬件上运行，确保完全的隐私保护和零云端依赖。

2. **n8n**

地址: <https://github.com/n8n-io/n8n>

`n8n` 是一款带有 `AI` 能力的工作流程自动化工具，提供了多步骤 AI 代理集成和超过 500 个集成工具的能力，开发者可以自定义想要使用的功能和应用程序。

3. **AingDesk**

地址: <https://github.com/aingdesk/AingDesk>

AingDesk是一款简单好用的AI助手，支持知识库、模型API、分享、联网搜索、智能体，它还在飞快成长中。

4. **Magentic-UI**

地址: <https://github.com/microsoft/magentic-ui>

微软出品，一个由多智能体系统驱动的、以人为中心的界面研究原型，能够浏览和执行网页操作、生成和执行代码，以及生成和分析文件。

5. **IDEA 系列激活**

地址: <https://github.com/saxpjexck/lsix>

只需点击一次，自动激活。

6. **PDF2MD**

地址: <https://github.com/ItusiAI/Open-PDF2MD>

PDF2MD是一个高效的PDF到Markdown转换工具，旨在帮助用户轻松将PDF文档转换为Markdown格式，便于编辑、分享和发布。通过简洁易用的界面和强大的转换功能，PDF2MD成为内容创作者、研究人员和开发者的得力助手。

7. **Gemini Image App**

地址: <https://github.com/0xsline/GeminiImageApp>

一个现代化的全栈 AI 图像处理平台，集成了 Google Gemini、OpenCV 和 YOLO 等先进技术，提供图像问答、生成、编辑、目标检测、图像分割和视频生成等功能。

8. **Vanna**

地址: <https://github.com/vanna-ai/vanna>

Vanna 是一个基于 MIT 许可的开源 Python RAG（检索增强生成）框架，用于 SQL 生成及相关功能。

9. **Opik**

地址: <https://github.com/comet-ml/opik>

Opik 帮助您构建、评估和优化 LLM 系统，使其运行得更好、更快、更经济。从 RAG 聊天机器人到代码助手，再到复杂的智能体管道，Opik 提供全面的跟踪、评估、仪表板，以及诸如 **Opik Agent Optimizer** 和 **Opik Guardrails** 等强大功能，以改进并保护生产环境中的 LLM 驱动应用。

10. **Second Me**

地址: <https://github.com/mindverse/Second-Me>

轻松定制你的本地 AI 数字分身。该项目是专注于用个人数据训练 AI 的平台，致力于帮助每个人构建、训练并拥有属于自己的本地 AI 数字分身。它采用分层记忆建模（HMM）和 Me-Alignment 算法，将你的知识、兴趣和偏好融入 AI，打造更懂你的 AI 助手。

11. **Chatterbox TTS**

地址: <https://github.com/resemble-ai/chatterbox>

顶尖开源语音合成

12. **Smart Mermaid**

地址: <https://github.com/liujuntao123/smart-mermaid>

Smart Mermaid 是一款利用人工智能技术，将您的文本描述智能转换为 Mermaid 格式图表代码，并实时渲染成可视化图表的 Web 应用。无论是流程图、序列图、甘特图还是状态图，只需输入文本，AI 即可为您生成相应的图表。

13. Ainee

地址: <https://github.com/luyu0279/Ainee>

AI 开源笔记工具，将音频、文字、文件以及 YouTube 视频等转换为笔记。



## 云行 AI 开源地址

Github：<https://github.com/boyazuo/yunxing-ai>

Gitee：<https://gitee.com/yxboot/yunxing-ai>