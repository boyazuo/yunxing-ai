# 云行 AI 后端 API

这是云行 AI 平台的后端 API 项目，基于 Spring Boot 开发。

## 技术栈

- JDK 17
- Spring Boot 3.4.5
- Spring Security
- Spring AI (预置集成)
- MyBatis-Plus 3.5.11
- MySQL 8.3
- Druid 1.2.24
- Redis
- JWT (jjwt 0.12.6)
- Hutool 5.8.36
- Lombok

## 项目结构

```
api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── yxboot/
│   │   │           ├── common/       # 通用工具和基础类
│   │   │           ├── config/       # 配置类
│   │   │           ├── modules/      # 业务模块
|   |   |           |   ├── account   # 账户权限模块
|   |   |           |   ├── app       # 应用模块
|   |   |           |   ├── chat      # 会话模块
|   |   |           |   ├── dataset   # 知识库模块
|   |   |           |   └── model     # 模型管理模块
│   │   │           ├── util/         # 工具类
│   │   │           └── ApiApplication.java # 主应用程序
│   │   └── resources/
│   │       ├── mapper/     # MyBatis XML映射文件
│   │       └── application.yml # 应用配置文件
│   └── test/              # 测试代码
└── pom.xml                # Maven配置文件
```

## 运行环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.3+
- Redis 7.0+

## 快速开始

1. 克隆项目
2. 配置数据库
   - 在MySQL中创建数据库：`CREATE DATABASE yunxing DEFAULT CHARACTER SET utf8mb4;`
   - 导入 SQL 脚本: doc/sql/db_schema.sql
   - 修改`application.yml`中的数据库连接信息
3. 配置Redis
   - 修改`application.yml`中的Redis连接信息
4. 构建和运行项目
   ```bash
   mvn clean package
   java -jar target/yunxing-api-0.0.1-SNAPSHOT.jar
   ```

## API文档

启动应用后，可以通过以下地址访问API文档：
- http://localhost:8080/swagger-ui.html
