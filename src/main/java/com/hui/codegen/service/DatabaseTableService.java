package com.hui.codegen.service;

import com.hui.codegen.model.DatabaseConfig;
import com.hui.codegen.util.SQLiteUtil;
import com.hui.codegen.web.dto.SimpleTableInfo;
import com.hui.codegen.web.dto.SimpleTablePageResult;
import com.hui.codegen.web.dto.TableInfo;
import com.hui.codegen.web.dto.TablePageResult;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库表信息服务类
 */
@Service
public class DatabaseTableService {

    /**
     * 获取简单表列表（只包含表名，用于快速返回）
     */
    public List<SimpleTableInfo> getTableNamesOnly(DatabaseConfig config, int page, int size) {
        List<SimpleTableInfo> tables = new ArrayList<>();
        
        try (Connection conn = getConnection(config)) {
            String sql = "";
            if ("mysql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? ORDER BY table_name LIMIT ? OFFSET ?";
            } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT tablename as table_name FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename LIMIT ? OFFSET ?";
            } else {
                // SQLite默认查询
                sql = "SELECT name as table_name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name LIMIT ? OFFSET ?";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if ("mysql".equalsIgnoreCase(config.getDbType())) {
                    pstmt.setString(1, config.getDatabase());
                    pstmt.setInt(2, size);
                    pstmt.setInt(3, (page - 1) * size);
                } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
                    pstmt.setInt(1, size);
                    pstmt.setInt(2, (page - 1) * size);
                } else {
                    // SQLite
                    pstmt.setInt(1, size);
                    pstmt.setInt(2, (page - 1) * size);
                }
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String tableName = rs.getString("table_name");
                        tables.add(new SimpleTableInfo(tableName));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取表名列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tables;
    }

    /**
     * 获取所有表名列表（不分页，用于快速返回）
     */
    public List<SimpleTableInfo> getAllTableNames(DatabaseConfig config) {
        List<SimpleTableInfo> tables = new ArrayList<>();
        
        try (Connection conn = getConnection(config)) {
            String sql = "";
            if ("mysql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? ORDER BY table_name";
            } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT tablename as table_name FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename";
            } else {
                // SQLite默认查询
                sql = "SELECT name as table_name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if ("mysql".equalsIgnoreCase(config.getDbType())) {
                    pstmt.setString(1, config.getDatabase());
                }
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String tableName = rs.getString("table_name");
                        tables.add(new SimpleTableInfo(tableName));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取所有表名列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tables;
    }

    /**
     * 获取简单表列表（分页，只包含表名，用于快速返回）
     */
    public SimpleTablePageResult getTableNamesByPage(DatabaseConfig config, int page, int size) {
        List<SimpleTableInfo> tables = new ArrayList<>();
        int total = 0;
        
        try (Connection conn = getConnection(config)) {
            
            // 获取总数（系统表查询）
            total = getTotalTableCount(conn, config);
            
            // 分页查询表信息
            String sql = "";
            if ("mysql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? ORDER BY table_name LIMIT ? OFFSET ?";
            } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT tablename as table_name FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename LIMIT ? OFFSET ?";
            } else {
                // SQLite默认查询
                sql = "SELECT name as table_name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name LIMIT ? OFFSET ?";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if ("mysql".equalsIgnoreCase(config.getDbType())) {
                    pstmt.setString(1, config.getDatabase());
                    pstmt.setInt(2, size);
                    pstmt.setInt(3, (page - 1) * size);
                } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
                    pstmt.setInt(1, size);
                    pstmt.setInt(2, (page - 1) * size);
                } else {
                    // SQLite
                    pstmt.setInt(1, size);
                    pstmt.setInt(2, (page - 1) * size);
                }
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String tableName = rs.getString("table_name");
                        tables.add(new SimpleTableInfo(tableName));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取表名列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        int totalPages = (int) Math.ceil((double) total / size);
        return new SimpleTablePageResult(tables, totalPages, total, page, size);
    }

    /**
     * 获取表列表（分页）
     */
    public TablePageResult getTablesByPage(DatabaseConfig config, int page, int size) {
        List<TableInfo> tables = new ArrayList<>();
        int total = 0;
        
        try (Connection conn = getConnection(config)) {
            
            // 获取总数（系统表查询）
            total = getTotalTableCount(conn, config);
            
            // 分页查询表信息
            String sql = "";
            if ("mysql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? ORDER BY table_name LIMIT ? OFFSET ?";
            } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT tablename as table_name FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename LIMIT ? OFFSET ?";
            } else {
                // SQLite默认查询
                sql = "SELECT name as table_name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name LIMIT ? OFFSET ?";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if ("mysql".equalsIgnoreCase(config.getDbType())) {
                    pstmt.setString(1, config.getDatabase());
                    pstmt.setInt(2, size);
                    pstmt.setInt(3, (page - 1) * size);
                } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
                    pstmt.setInt(1, size);
                    pstmt.setInt(2, (page - 1) * size);
                } else {
                    // SQLite
                    pstmt.setInt(1, size);
                    pstmt.setInt(2, (page - 1) * size);
                }
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String tableName = rs.getString("table_name");
                        String tableComment = getTableComment(conn, tableName, config);
                        
                        // 转换为类名
                        String className = convertToClassName(tableName);
                        
                        // 获取列数量
                        int columnCount = getColumnCount(conn, tableName, config);
                        
                        TableInfo tableInfo = new TableInfo(tableName, className, tableComment, columnCount);
                        tables.add(tableInfo);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取表列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        int totalPages = (int) Math.ceil((double) total / size);
        return new TablePageResult(tables, totalPages, total, page, size);
    }

    /**
     * 获取简单表列表（用于全选）
     */
    public List<TableInfo> getSimpleTables(DatabaseConfig config) {
        List<TableInfo> tables = new ArrayList<>();
        
        try (Connection conn = getConnection(config)) {
            String sql = "";
            if ("mysql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?";
            } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT tablename as table_name FROM pg_tables WHERE schemaname = 'public'";
            } else {
                // SQLite默认查询
                sql = "SELECT name as table_name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if ("mysql".equalsIgnoreCase(config.getDbType())) {
                    pstmt.setString(1, config.getDatabase());
                }
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String tableName = rs.getString("table_name");
                        String className = convertToClassName(tableName);
                        tables.add(new TableInfo(tableName, className));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取简单表列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tables;
    }

    /**
     * 获取表详细信息
     */
    public Map<String, Object> getTableDetail(DatabaseConfig config, String tableName) {
        Map<String, Object> detail = new HashMap<>();
        
        try (Connection conn = getConnection(config)) {
            
            // 获取表注释
            String tableComment = getTableComment(conn, tableName, config);
            detail.put("tableComment", tableComment);
            
            // 获取列数量
            int columnCount = getColumnCount(conn, tableName, config);
            detail.put("columnCount", columnCount);
            
        } catch (SQLException e) {
            System.err.println("获取表详细信息失败: " + e.getMessage());
            e.printStackTrace();
            detail.put("tableComment", "获取失败");
            detail.put("columnCount", 0);
        }
        
        return detail;
    }

    /**
     * 获取表的列信息
     */
    public List<Map<String, Object>> getTableColumns(DatabaseConfig config, String tableName) {
        List<Map<String, Object>> columns = new ArrayList<>();
        
        try (Connection conn = getConnection(config)) {
            String sql = "";
            if ("mysql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT column_name, data_type, is_nullable, column_default, column_comment " +
                      "FROM information_schema.columns " +
                      "WHERE table_schema = ? AND table_name = ? " +
                      "ORDER BY ordinal_position";
            } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
                sql = "SELECT column_name, data_type, is_nullable, column_default " +
                      "FROM information_schema.columns " +
                      "WHERE table_schema = 'public' AND table_name = ? " +
                      "ORDER BY ordinal_position";
            } else {
                // SQLite
                sql = "PRAGMA table_info(" + tableName + ")";
            }
            
            System.out.println("执行SQL查询列信息: " + sql);
            System.out.println("表名: " + tableName);
            
            if ("mysql".equalsIgnoreCase(config.getDbType()) || "postgresql".equalsIgnoreCase(config.getDbType())) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    if ("mysql".equalsIgnoreCase(config.getDbType())) {
                        pstmt.setString(1, config.getDatabase());
                        pstmt.setString(2, tableName);
                    } else {
                        pstmt.setString(1, tableName);
                    }
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> column = new HashMap<>();
                            String columnName = rs.getString("column_name");
                            String dataType = rs.getString("data_type");
                            boolean nullable = "YES".equalsIgnoreCase(rs.getString("is_nullable"));
                            String defaultValue = rs.getString("column_default");
                            String columnComment = "mysql".equalsIgnoreCase(config.getDbType()) ? rs.getString("column_comment") : "";
                            
                            column.put("columnName", columnName);
                            column.put("dataType", dataType);
                            column.put("nullable", nullable);
                            column.put("defaultValue", defaultValue);
                            column.put("columnComment", columnComment);
                            column.put("primaryKey", false); // 简化处理，实际需要查询主键信息
                            column.put("autoIncrement", false); // 简化处理，实际需要查询自增信息
                            
                            // 转换为Java字段名和类型
                            String javaFieldName = convertToFieldName(columnName);
                            String javaType = convertToJavaType(dataType, config.getDbType());
                            column.put("javaFieldName", javaFieldName);
                            column.put("javaType", javaType);
                            
                            columns.add(column);
                        }
                    }
                }
            } else {
                // SQLite
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    System.out.println("SQLite查询结果:");
                    while (rs.next()) {
                        Map<String, Object> column = new HashMap<>();
                        String columnName = rs.getString("name");
                        String dataType = rs.getString("type");
                        boolean nullable = rs.getInt("notnull") == 0;
                        String defaultValue = rs.getString("dflt_value");
                        boolean primaryKey = rs.getInt("pk") > 0;
                        
                        System.out.println("列名: " + columnName + ", 类型: " + dataType + ", 主键: " + primaryKey);
                        
                        column.put("columnName", columnName);
                        column.put("dataType", dataType);
                        column.put("nullable", nullable);
                        column.put("defaultValue", defaultValue);
                        column.put("columnComment", ""); // SQLite不支持列注释
                        column.put("primaryKey", primaryKey);
                        column.put("autoIncrement", primaryKey && dataType.toUpperCase().contains("INTEGER"));
                        
                        // 转换为Java字段名和类型
                        String javaFieldName = convertToFieldName(columnName);
                        String javaType = convertToJavaType(dataType, "sqlite");
                        column.put("javaFieldName", javaFieldName);
                        column.put("javaType", javaType);
                        
                        columns.add(column);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取表列信息失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("返回的列信息: " + columns);
        System.out.println("列数量: " + columns.size());
        return columns;
    }

    /**
     * 根据数据库配置获取连接
     */
    private Connection getConnection(DatabaseConfig config) throws SQLException {
        String dbType = config.getDbType();
        if (dbType == null || dbType.isEmpty()) {
            dbType = "sqlite"; // 默认为SQLite
        }
        
        if ("mysql".equalsIgnoreCase(dbType)) {
            String url = "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase() + 
                        "?useUnicode=true&characterEncoding=" + config.getCharset();
            return DriverManager.getConnection(url, config.getUsername(), config.getPassword());
        } else if ("postgresql".equalsIgnoreCase(dbType)) {
            String url = "jdbc:postgresql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
            return DriverManager.getConnection(url, config.getUsername(), config.getPassword());
        } else {
            // SQLite
            return SQLiteUtil.getConnection();
        }
    }

    /**
     * 获取表总数
     */
    private int getTotalTableCount(Connection conn, DatabaseConfig config) throws SQLException {
        String sql = "";
        if ("mysql".equalsIgnoreCase(config.getDbType())) {
            sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ?";
        } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
            sql = "SELECT COUNT(*) FROM pg_tables WHERE schemaname = 'public'";
        } else {
            // SQLite
            sql = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'";
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if ("mysql".equalsIgnoreCase(config.getDbType())) {
                pstmt.setString(1, config.getDatabase());
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * 获取表的列数量
     */
    private int getColumnCount(Connection conn, String tableName, DatabaseConfig config) throws SQLException {
        if ("mysql".equalsIgnoreCase(config.getDbType())) {
            String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = ? AND table_name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, config.getDatabase());
                pstmt.setString(2, tableName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
            String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tableName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } else {
            // SQLite
            String sql = "PRAGMA table_info(" + tableName + ")";
            int count = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    count++;
                }
            }
            return count;
        }
        return 0;
    }
    
    /**
     * 获取表注释
     */
    private String getTableComment(Connection conn, String tableName, DatabaseConfig config) throws SQLException {
        if ("mysql".equalsIgnoreCase(config.getDbType())) {
            String sql = "SELECT table_comment FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, config.getDatabase());
                pstmt.setString(2, tableName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("table_comment");
                    }
                }
            }
        } else if ("postgresql".equalsIgnoreCase(config.getDbType())) {
            String sql = "SELECT obj_description((SELECT oid FROM pg_class WHERE relname = ?), 'pg_class') as table_comment";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tableName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("table_comment");
                    }
                }
            }
        } else {
            // SQLite没有表注释，返回默认值
            switch (tableName.toLowerCase()) {
                case "users":
                case "user":
                    return "用户表";
                case "orders":
                case "order":
                    return "订单表";
                case "products":
                case "product":
                    return "商品表";
                case "categories":
                case "category":
                    return "分类表";
                default:
                    return tableName + "表";
            }
        }
        return tableName + "表";
    }

    /**
     * 转换表名为类名
     */
    private String convertToClassName(String tableName) {
        if (tableName == null) return null;
        
        // 移除表前缀（如果有）
        if (tableName.startsWith("t_") || tableName.startsWith("tb_")) {
            tableName = tableName.substring(tableName.indexOf("_") + 1);
        }
        
        // 转换为驼峰命名
        StringBuilder className = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : tableName.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                className.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                className.append(Character.toLowerCase(c));
            }
        }
        
        return className.toString();
    }

    /**
     * 转换列名为Java字段名
     */
    private String convertToFieldName(String columnName) {
        if (columnName == null) return null;
        
        StringBuilder fieldName = new StringBuilder();
        boolean capitalizeNext = false;
        
        for (char c : columnName.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                fieldName.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                fieldName.append(Character.toLowerCase(c));
            }
        }
        
        return fieldName.toString();
    }

    /**
     * 转换数据库类型为Java类型
     */
    private String convertToJavaType(String dbType, String dbTypeName) {
        if (dbType == null) return "Object";
        
        dbType = dbType.toUpperCase();
        
        if ("mysql".equalsIgnoreCase(dbTypeName) || "postgresql".equalsIgnoreCase(dbTypeName)) {
            if (dbType.contains("INT") || dbType.contains("TINYINT") || dbType.contains("SMALLINT") || dbType.contains("BIGINT")) {
                return "Long";
            } else if (dbType.contains("DECIMAL") || dbType.contains("NUMERIC")) {
                return "BigDecimal";
            } else if (dbType.contains("FLOAT") || dbType.contains("DOUBLE")) {
                return "Double";
            } else if (dbType.contains("DATE") || dbType.contains("TIME") || dbType.contains("YEAR")) {
                return "Date";
            } else if (dbType.contains("TEXT") || dbType.contains("CHAR") || dbType.contains("VARCHAR") || dbType.contains("CLOB")) {
                return "String";
            } else if (dbType.contains("BLOB")) {
                return "byte[]";
            } else {
                return "String"; // 默认为String
            }
        } else {
            // SQLite
            if (dbType.contains("INT")) {
                return "Long";
            } else if (dbType.contains("REAL") || dbType.contains("FLOAT") || dbType.contains("DOUBLE")) {
                return "Double";
            } else if (dbType.contains("TEXT") || dbType.contains("CHAR")) {
                return "String";
            } else if (dbType.contains("BLOB")) {
                return "byte[]";
            } else if (dbType.contains("NUMERIC") || dbType.contains("DECIMAL")) {
                return "BigDecimal";
            } else if (dbType.contains("DATE")) {
                return "Date";
            } else {
                return "String"; // 默认为String
            }
        }
    }
}