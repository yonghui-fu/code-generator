package com.hui.codegen.service;

import com.hui.codegen.model.DatabaseConfig;
import com.hui.codegen.util.SQLiteUtil;
import com.hui.codegen.web.dto.TableInfo;
import com.hui.codegen.web.dto.TablePageResult;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite数据库表信息服务类
 */
@Service
public class DatabaseTableService {

    /**
     * 获取表列表（分页）
     */
    public TablePageResult getTablesByPage(DatabaseConfig config, int page, int size) {
        List<TableInfo> tables = new ArrayList<>();
        int total = 0;
        
        try (Connection conn = SQLiteUtil.getConnection()) {
            
            // 获取总数（SQLite系统表查询）
            total = getTotalTableCount(conn);
            
            // 分页查询表信息
            String sql = "SELECT name as table_name " +
                     "FROM sqlite_master " +
                     "WHERE type = 'table' AND name NOT LIKE 'sqlite_%' " +
                     "ORDER BY name " +
                     "LIMIT ? OFFSET ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, size);
                pstmt.setInt(2, (page - 1) * size);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String tableName = rs.getString("table_name");
                        String tableComment = getTableComment(conn, tableName);
                        
                        // 转换为类名
                        String className = convertToClassName(tableName);
                        
                        // 获取列数量
                        int columnCount = getColumnCount(conn, tableName);
                        
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
        
        try (Connection conn = SQLiteUtil.getConnection()) {
            
            String sql = "SELECT name as table_name " +
                     "FROM sqlite_master " +
                     "WHERE type = 'table' AND name NOT LIKE 'sqlite_%' " +
                     "ORDER BY name";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        
        try (Connection conn = SQLiteUtil.getConnection()) {
            
            // 获取表注释
            String tableComment = getTableComment(conn, tableName);
            detail.put("tableComment", tableComment);
            
            // 获取列数量
            int columnCount = getColumnCount(conn, tableName);
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
        
        try (Connection conn = SQLiteUtil.getConnection()) {
            
            String sql = "PRAGMA table_info(" + tableName + ")";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    String columnName = rs.getString("name");
                    String dataType = rs.getString("type");
                    boolean nullable = rs.getInt("notnull") == 0;
                    String defaultValue = rs.getString("dflt_value");
                    boolean primaryKey = rs.getInt("pk") > 0;
                    
                    column.put("columnName", columnName);
                    column.put("dataType", dataType);
                    column.put("nullable", nullable);
                    column.put("defaultValue", defaultValue);
                    column.put("columnComment", ""); // SQLite不支持列注释
                    column.put("primaryKey", primaryKey);
                    column.put("autoIncrement", primaryKey && dataType.toUpperCase().contains("INTEGER"));
                    
                    // 转换为Java字段名和类型
                    String javaFieldName = convertToFieldName(columnName);
                    String javaType = convertSQLiteToJavaType(dataType);
                    column.put("javaFieldName", javaFieldName);
                    column.put("javaType", javaType);
                    
                    columns.add(column);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取表列信息失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return columns;
    }

    /**
     * 获取表总数
     */
    private int getTotalTableCount(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) " +
                     "FROM sqlite_master " +
                     "WHERE type = 'table' AND name NOT LIKE 'sqlite_%'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * 获取表的列数量
     */
    private int getColumnCount(Connection conn, String tableName) throws SQLException {
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
    
    /**
     * 获取表注释（SQLite模拟）
     */
    private String getTableComment(Connection conn, String tableName) {
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
     * 转换SQLite数据类型为Java类型
     */
    private String convertSQLiteToJavaType(String sqliteType) {
        if (sqliteType == null) return "Object";
        
        sqliteType = sqliteType.toUpperCase();
        
        if (sqliteType.contains("INT")) {
            return "Long";
        } else if (sqliteType.contains("REAL") || sqliteType.contains("FLOAT") || sqliteType.contains("DOUBLE")) {
            return "Double";
        } else if (sqliteType.contains("TEXT") || sqliteType.contains("CHAR")) {
            return "String";
        } else if (sqliteType.contains("BLOB")) {
            return "byte[]";
        } else if (sqliteType.contains("NUMERIC") || sqliteType.contains("DECIMAL")) {
            return "BigDecimal";
        } else if (sqliteType.contains("DATE")) {
            return "Date";
        } else {
            return "String"; // 默认为String
        }
    }
}