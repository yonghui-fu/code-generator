# 代码生成器 (Code Generator)

一个基于 Spring Boot 的智能代码生成器，支持从数据库表结构自动生成常用的 Java 代码文件。

## ✨ 功能特性

- 🎯 **智能代码生成**：基于数据库表结构自动生成 Entity、Controller、Service、Mapper 等代码文件
- 🗄️ **SQLite 数据库**：内置 SQLite 数据库，无需额外配置，开箱即用
- 🎨 **可视化界面**：现代化的 Bootstrap 5 响应式 Web 界面
- 📝 **模板管理**：支持自定义和管理代码生成模板
- 🔧 **配置管理**：灵活的数据库配置管理系统
- 📦 **演示数据**：内置完整的演示表结构和模板

## 🚀 快速开始

### 环境要求

- Java 8 或更高版本
- Maven 3.6 或更高版本

### 运行项目

1. **克隆项目**
   ```bash
   git clone https://github.com/yonghui-fu/code-generator.git
   cd code-generator
   ```

2. **编译项目**
   ```bash
   mvn clean compile
   ```

3. **运行应用**
   ```bash
   mvn spring-boot:run
   ```

4. **访问应用**
   打开浏览器访问：http://localhost:19999

## 📖 功能说明

### 主要模块

1. **首页** - 项目介绍和导航
2. **数据库配置** - 管理数据库连接配置
3. **模板配置** - 管理代码生成模板
4. **代码生成** - 选择表和模板生成代码

### 内置演示表

项目包含以下演示表结构：
- `users` - 用户表
- `products` - 商品表
- `categories` - 分类表
- `orders` - 订单表
- `order_items` - 订单明细表

### 支持的模板类型

- **Entity 模板** - JPA 实体类
- **Controller 模板** - Spring MVC 控制器
- **Service 模板** - 业务服务层
- **Mapper 模板** - MyBatis Mapper 接口
- **Mapper XML 模板** - MyBatis XML 映射文件

## 🛠️ 技术栈

- **后端框架**：Spring Boot 2.7.18
- **模板引擎**：Thymeleaf + FreeMarker 2.3.32
- **数据库**：SQLite 3.36.0.3
- **前端框架**：Bootstrap 5.1.3
- **代码编辑器**：CodeMirror
- **构建工具**：Maven 3.8+
- **Java 版本**：JDK 8+

## 📁 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/hui/codegen/
│   │       ├── CodeGeneratorApplication.java    # 启动类
│   │       ├── model/                          # 数据模型
│   │       ├── service/                        # 业务服务
│   │       ├── util/                          # 工具类
│   │       └── web/                           # Web层
│   │           ├── controller/                 # 控制器
│   │           └── dto/                       # 数据传输对象
│   └── resources/
│       ├── templates/                         # Thymeleaf模板
│       └── static/                           # 静态资源
└── test/                                     # 测试代码
```

## 🔧 配置说明

### 应用配置

项目使用 `application.properties` 进行配置：

```properties
# 服务器配置
server.port=19999

# 数据库配置（SQLite）
spring.datasource.url=jdbc:sqlite:code-generator.db
spring.datasource.driver-class-name=org.xerial.sqlite.JDBC

# 模板配置
spring.freemarker.suffix=.ftl
spring.freemarker.template-loader-path=classpath:/templates/
```

### 数据库初始化

项目启动时会自动：
1. 创建 SQLite 数据库文件
2. 初始化必要的表结构
3. 插入默认的模板组和模板
4. 创建演示表结构

## 🎯 使用指南

### 1. 添加数据库配置

1. 访问「数据库配置」页面
2. 点击「添加配置」按钮
3. 填写数据库连接信息
4. 点击「测试」验证连接
5. 保存配置

### 2. 管理代码模板

1. 访问「模板配置」页面
2. 选择要编辑的模板
3. 使用 CodeMirror 编辑器修改模板内容
4. 支持 FreeMarker 语法
5. 保存或重置模板

### 3. 生成代码

1. 访问「代码生成」页面
2. 选择数据库配置
3. 选择要生成的表
4. 选择代码模板
5. 设置包名等参数
6. 预览或生成代码

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📝 更新日志

### v1.0.0 (2025-09-12)
- ✅ 完成项目基础架构
- ✅ 实现 SQLite 数据库支持
- ✅ 完成模板管理系统
- ✅ 实现代码生成核心功能
- ✅ 完成 Web 界面开发
- ✅ 添加演示数据和模板

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 👥 作者

- **fuyonghui** - *初始开发* - [GitHub](https://github.com/fuyonghui)

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [FreeMarker](https://freemarker.apache.org/)
- [Bootstrap](https://getbootstrap.com/)
- [CodeMirror](https://codemirror.net/)
- [SQLite](https://www.sqlite.org/)

---

如果这个项目对您有帮助，请给它一个 ⭐ Star！