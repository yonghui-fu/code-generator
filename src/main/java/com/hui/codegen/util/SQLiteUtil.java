package com.hui.codegen.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

/**
 * SQLite数据库工具类
 */
public class SQLiteUtil {
    
    private static final String DB_PATH = "code-generator.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    static {
        // 初始化数据库
        initDatabase();
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * 初始化数据库，创建必要的表
     */
    private static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建数据库配置表
            String createConfigTable = "CREATE TABLE IF NOT EXISTS database_config (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "host TEXT NOT NULL," +
                    "port TEXT NOT NULL," +
                    "database_name TEXT NOT NULL," +
                    "username TEXT NOT NULL," +
                    "password TEXT," +
                    "charset TEXT DEFAULT 'utf8'," +
                    "enabled INTEGER DEFAULT 1," +
                    "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            stmt.execute(createConfigTable);
            
            // 创建模板组表
            String createTemplateGroupTable = "CREATE TABLE IF NOT EXISTS template_group (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "enabled INTEGER DEFAULT 1," +
                    "sort_order INTEGER DEFAULT 0," +
                    "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            stmt.execute(createTemplateGroupTable);
            
            // 创建模板信息表
            String createTemplateInfoTable = "CREATE TABLE IF NOT EXISTS template_info (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL UNIQUE," +
                    "display_name TEXT NOT NULL," +
                    "description TEXT," +
                    "template_type TEXT DEFAULT 'java'," +
                    "enabled INTEGER DEFAULT 1," +
                    "sort_order INTEGER DEFAULT 0," +
                    "group_id TEXT," +
                    "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (group_id) REFERENCES template_group(id)" +
                    ")";
            
            stmt.execute(createTemplateInfoTable);
            
            // 创建模板内容表
            String createTemplateContentTable = "CREATE TABLE IF NOT EXISTS template_content (" +
                    "template_name TEXT PRIMARY KEY," +
                    "content TEXT NOT NULL," +
                    "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (template_name) REFERENCES template_info(name)" +
                    ")";
            
            stmt.execute(createTemplateContentTable);
            
            // 初始化默认数据
            initDefaultData(conn);
            
            System.out.println("SQLite数据库初始化完成，数据库文件：" + new File(DB_PATH).getAbsolutePath());
            
        } catch (SQLException e) {
            System.err.println("初始化SQLite数据库失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 初始化默认数据
     */
    private static void initDefaultData(Connection conn) throws SQLException {
        // 插入默认模板组数据（如果不存在）
        String insertDefaultGroups = "INSERT OR IGNORE INTO template_group (id, name, description, enabled, sort_order) VALUES " +
                "('default', '默认组', '系统默认的模板组', 1, 0), " +
                "('java', 'Java类', 'Java相关的模板', 1, 1), " +
                "('config', '配置文件', '配置文件相关的模板', 1, 2)";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(insertDefaultGroups);
        }
        
        // 插入默认模板数据（如果不存在）
        String insertDefaultTemplates = "INSERT OR IGNORE INTO template_info (name, display_name, description, template_type, enabled, sort_order, group_id) VALUES " +
                "('entity.ftl', '实体类模板', '生成JPA实体类', 'java', 1, 0, 'java'), " +
                "('controller.ftl', '控制器模板', '生成Spring MVC控制器', 'java', 1, 1, 'java'), " +
                "('service.ftl', '服务层模板', '生成业务服务层', 'java', 1, 2, 'java'), " +
                "('mapper.ftl', 'Mapper接口模板', '生成MyBatis Mapper接口', 'java', 1, 3, 'java'), " +
                "('mapperXml.ftl', 'Mapper XML模板', '生成MyBatis XML映射文件', 'xml', 1, 0, 'config')";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(insertDefaultTemplates);
        }
        
        // 创建示例表结构用于代码生成演示
        createDemoTables(conn);
        
        // 插入默认模板内容
        initDefaultTemplateContent(conn);
    }
    
    /**
     * 创建示例表结构
     */
    private static void createDemoTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 用户表
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL UNIQUE," +
                    "password TEXT NOT NULL," +
                    "email TEXT," +
                    "phone TEXT," +
                    "status INTEGER DEFAULT 1," +
                    "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            // 商品表
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "price REAL NOT NULL," +
                    "stock INTEGER DEFAULT 0," +
                    "category_id INTEGER," +
                    "status INTEGER DEFAULT 1," +
                    "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            // 分类表
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "parent_id INTEGER DEFAULT 0," +
                    "sort_order INTEGER DEFAULT 0," +
                    "status INTEGER DEFAULT 1," +
                    "created_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            // 订单表
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "order_no TEXT NOT NULL UNIQUE," +
                    "user_id INTEGER NOT NULL," +
                    "total_amount REAL NOT NULL," +
                    "status INTEGER DEFAULT 1," +
                    "payment_status INTEGER DEFAULT 0," +
                    "shipping_address TEXT," +
                    "remark TEXT," +
                    "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            // 订单明细表
            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "order_id INTEGER NOT NULL," +
                    "product_id INTEGER NOT NULL," +
                    "product_name TEXT NOT NULL," +
                    "price REAL NOT NULL," +
                    "quantity INTEGER NOT NULL," +
                    "total_amount REAL NOT NULL" +
                    ")");
        }
    }
    
    /**
     * 初始化默认模板内容
     */
    private static void initDefaultTemplateContent(Connection conn) throws SQLException {
        // Entity模板内容
        String entityTemplate = "package ${packageName}.entity;\n\n" +
                "import javax.persistence.*;\n\n" +
                "/**\n" +
                " * ${tableComment}\n" +
                " */\n" +
                "@Entity\n" +
                "@Table(name = \"${tableName}\")\n" +
                "public class ${className} {\n\n" +
                "<#list columns as column>\n" +
                "    /**\n" +
                "     * ${column.columnComment}\n" +
                "     */\n" +
                "    <#if column.primaryKey>\n" +
                "    @Id\n" +
                "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n" +
                "    </#if>\n" +
                "    @Column(name = \"${column.columnName}\")\n" +
                "    private ${column.javaType} ${column.javaFieldName};\n\n" +
                "</#list>\n" +
                "    // Getters and Setters\n" +
                "<#list columns as column>\n" +
                "    public ${column.javaType} get${column.javaFieldName?cap_first}() {\n" +
                "        return ${column.javaFieldName};\n" +
                "    }\n\n" +
                "    public void set${column.javaFieldName?cap_first}(${column.javaType} ${column.javaFieldName}) {\n" +
                "        this.${column.javaFieldName} = ${column.javaFieldName};\n" +
                "    }\n\n" +
                "</#list>\n" +
                "}";
        
        // Controller模板内容
        String controllerTemplate = "package ${packageName}.controller;\n\n" +
                "import ${packageName}.entity.${className};\n" +
                "import ${packageName}.service.${className}Service;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.web.bind.annotation.*;\n\n" +
                "/**\n" +
                " * ${tableComment} 控制器\n" +
                " */\n" +
                "@RestController\n" +
                "@RequestMapping(\"/${classNameLowerFirst}\")\n" +
                "public class ${className}Controller {\n\n" +
                "    @Autowired\n" +
                "    private ${className}Service ${classNameLowerFirst}Service;\n\n" +
                "    @GetMapping\n" +
                "    public String list() {\n" +
                "        return \"${className} list\";\n" +
                "    }\n\n" +
                "    @PostMapping\n" +
                "    public String create(@RequestBody ${className} ${classNameLowerFirst}) {\n" +
                "        return \"${className} created\";\n" +
                "    }\n" +
                "}";
        
        // Service模板内容
        String serviceTemplate = "package ${packageName}.service;\n\n" +
                "import ${packageName}.entity.${className};\n" +
                "import ${packageName}.mapper.${className}Mapper;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.stereotype.Service;\n\n" +
                "/**\n" +
                " * ${tableComment} 服务类\n" +
                " */\n" +
                "@Service\n" +
                "public class ${className}Service {\n\n" +
                "    @Autowired\n" +
                "    private ${className}Mapper ${classNameLowerFirst}Mapper;\n\n" +
                "    public ${className} findById(<#if primaryKey??>${primaryKey.javaType}<#else>Long</#if> id) {\n" +
                "        return ${classNameLowerFirst}Mapper.selectById(id);\n" +
                "    }\n\n" +
                "    public int save(${className} ${classNameLowerFirst}) {\n" +
                "        return ${classNameLowerFirst}Mapper.insert(${classNameLowerFirst});\n" +
                "    }\n" +
                "}";
        
        // Mapper接口模板内容
        String mapperTemplate = "package ${packageName}.mapper;\n\n" +
                "import ${packageName}.entity.${className};\n" +
                "import org.apache.ibatis.annotations.Mapper;\n\n" +
                "/**\n" +
                " * ${tableComment} Mapper接口\n" +
                " */\n" +
                "@Mapper\n" +
                "public interface ${className}Mapper {\n\n" +
                "    ${className} selectById(<#if primaryKey??>${primaryKey.javaType}<#else>Long</#if> id);\n\n" +
                "    int insert(${className} ${classNameLowerFirst});\n\n" +
                "    int updateById(${className} ${classNameLowerFirst});\n\n" +
                "    int deleteById(<#if primaryKey??>${primaryKey.javaType}<#else>Long</#if> id);\n" +
                "}";
        
        // Mapper XML模板内容
        String mapperXmlTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"${packageName}.mapper.${className}Mapper\">\n\n" +
                "    <resultMap id=\"BaseResultMap\" type=\"${packageName}.entity.${className}\">\n" +
                "<#list columns as column>\n" +
                "        <#if column.primaryKey>\n" +
                "        <id column=\"${column.columnName}\" property=\"${column.javaFieldName}\" />\n" +
                "        <#else>\n" +
                "        <result column=\"${column.columnName}\" property=\"${column.javaFieldName}\" />\n" +
                "        </#if>\n" +
                "</#list>\n" +
                "    </resultMap>\n\n" +
                "    <sql id=\"Base_Column_List\">\n" +
                "        <#list columns as column>${column.columnName}<#if column_has_next>, </#if></#list>\n" +
                "    </sql>\n\n" +
                "    <select id=\"selectById\" resultMap=\"BaseResultMap\">\n" +
                "        SELECT\n" +
                "        <include refid=\"Base_Column_List\" />\n" +
                "        FROM ${tableName}\n" +
                "        WHERE <#if primaryKey??>${primaryKey.columnName} = <#noparse>#{${primaryKey.javaFieldName}}</#noparse><#else>id = <#noparse>#{id}</#noparse></#if>\n" +
                "    </select>\n\n" +
                "    <insert id=\"insert\">\n" +
                "        INSERT INTO ${tableName} (\n" +
                "            <#list columns as column><#if !column.primaryKey || !column.autoIncrement>${column.columnName}<#if column_has_next>, </#if></#if></#list>\n" +
                "        ) VALUES (\n" +
                "            <#list columns as column><#if !column.primaryKey || !column.autoIncrement><#noparse>#{${column.javaFieldName}}</#noparse><#if column_has_next>, </#if></#if></#list>\n" +
                "        )\n" +
                "    </insert>\n\n" +
                "    <update id=\"updateById\">\n" +
                "        UPDATE ${tableName}\n" +
                "        <set>\n" +
                "<#list columns as column>\n" +
                "            <#if !column.primaryKey>\n" +
                "            <if test=\"${column.javaFieldName} != null\">\n" +
                "                ${column.columnName} = <#noparse>#{${column.javaFieldName}}</#noparse>,\n" +
                "            </if>\n" +
                "            </#if>\n" +
                "</#list>\n" +
                "        </set>\n" +
                "        WHERE <#if primaryKey??>${primaryKey.columnName} = <#noparse>#{${primaryKey.javaFieldName}}</#noparse><#else>id = <#noparse>#{id}</#noparse></#if>\n" +
                "    </update>\n\n" +
                "    <delete id=\"deleteById\">\n" +
                "        DELETE FROM ${tableName}\n" +
                "        WHERE <#if primaryKey??>${primaryKey.columnName} = <#noparse>#{${primaryKey.javaFieldName}}</#noparse><#else>id = <#noparse>#{id}</#noparse></#if>\n" +
                "    </delete>\n\n" +
                "</mapper>";
        
        // 插入模板内容
        String insertTemplateContent = "INSERT OR REPLACE INTO template_content (template_name, content) VALUES " +
                "('entity.ftl', ?), " +
                "('controller.ftl', ?), " +
                "('service.ftl', ?), " +
                "('mapper.ftl', ?), " +
                "('mapperXml.ftl', ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertTemplateContent)) {
            pstmt.setString(1, entityTemplate);
            pstmt.setString(2, controllerTemplate);
            pstmt.setString(3, serviceTemplate);
            pstmt.setString(4, mapperTemplate);
            pstmt.setString(5, mapperXmlTemplate);
            pstmt.execute();
        }
    }

    /**
     * 测试数据库连接
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}