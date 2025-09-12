# 代码生成器配置说明

## 1. 数据库配置
```java
DatabaseConfig databaseConfig = new DatabaseConfig();
databaseConfig.setUrl("jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8");
databaseConfig.setUsername("root");
databaseConfig.setPassword("password");
databaseConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
```

## 2. 生成器配置
```java
GeneratorConfig config = new GeneratorConfig();
config.setDatabase(databaseConfig);           // 数据库配置
config.setPackageName("com.example.demo");    // 生成代码的包名
config.setOutputPath("./generated-code");     // 输出路径
```

## 3. 表配置
```java
// 指定要生成的表（可选，不指定则生成所有表）
config.setTables(Arrays.asList("user", "order", "product"));
```

## 4. 生成选项配置（MyBatis XML方式）
```java
config.setGenerateEntity(true);        // 生成实体类
config.setGenerateController(true);    // 生成Controller
config.setGenerateService(true);       // 生成Service
config.setGenerateMapper(true);        // 生成Mapper接口（纯接口，无注解）
config.setGenerateMapperXml(true);     // 生成Mapper XML文件
```

## 5. 生成的文件结构
```
generated-code/
└── com/
    └── example/
        └── demo/
            ├── entity/
            │   ├── User.java
            │   ├── Order.java
            │   └── Product.java
            ├── controller/
            │   ├── UserController.java
            │   ├── OrderController.java
            │   └── ProductController.java
            ├── service/
            │   ├── UserService.java
            │   ├── OrderService.java
            │   └── ProductService.java
            └── mapper/
                ├── UserMapper.java         # Mapper接口
                ├── UserMapper.xml          # MyBatis XML配置
                ├── OrderMapper.java
                ├── OrderMapper.xml
                ├── ProductMapper.java
                └── ProductMapper.xml
```

## 6. MyBatis XML特性
- 完整的CRUD操作
- 结果映射（resultMap）
- 基础字段列表（sql片段）
- 分页查询支持
- 条件查询支持
- 自动处理主键和自增列
- 使用#{param}参数占位符