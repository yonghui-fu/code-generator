package com.hui.codegen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成器Spring Boot应用程序
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@RestController
public class CodeGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeGeneratorApplication.class, args);
    }
    
    @GetMapping("/check-db-structure")
    public Map<String, Object> checkDatabaseStructure() {
        Map<String, Object> result = new HashMap<>();
        String dbPath = "D:/workspace/IdeaProjects/code-generator/code-generator.db";
        String url = "jdbc:sqlite:" + dbPath;
        
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            
            // 检查数据库中所有表
            ResultSet tablesRs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
            List<String> tables = new ArrayList<>();
            while (tablesRs.next()) {
                tables.add(tablesRs.getString("name"));
            }
            
            result.put("tables", tables);
            
            // 如果template_info表存在，检查其结构
            if (tables.contains("template_info")) {
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(template_info)");
                List<Map<String, Object>> columns = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("name", rs.getString("name"));
                    column.put("type", rs.getString("type"));
                    column.put("notnull", rs.getBoolean("notnull"));
                    column.put("dflt_value", rs.getString("dflt_value"));
                    column.put("pk", rs.getBoolean("pk"));
                    columns.add(column);
                }
                
                result.put("table", "template_info");
                result.put("columns", columns);
                
                // 检查是否有file_name_pattern字段
                boolean hasFileNamePattern = false;
                for (Map<String, Object> column : columns) {
                    if ("file_name_pattern".equals(column.get("name"))) {
                        hasFileNamePattern = true;
                        break;
                    }
                }
                
                result.put("hasFileNamePattern", hasFileNamePattern);
                
                // 如果没有file_name_pattern字段，则添加它
                if (!hasFileNamePattern) {
                    try {
                        stmt.execute("ALTER TABLE template_info ADD COLUMN file_name_pattern VARCHAR(200)");
                        result.put("addColumnResult", "成功添加file_name_pattern字段");
                    } catch (Exception e) {
                        result.put("addColumnResult", "添加file_name_pattern字段失败: " + e.getMessage());
                    }
                }
            } else {
                result.put("message", "template_info表不存在");
            }
            
            result.put("status", "success");
            result.put("dbPath", dbPath);
            
            conn.close();
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("exception", e.getClass().getName());
        }
        
        return result;
    }
}