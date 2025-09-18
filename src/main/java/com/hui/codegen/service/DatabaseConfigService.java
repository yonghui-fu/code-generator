package com.hui.codegen.service;

import com.hui.codegen.model.DatabaseConfig;
import com.hui.codegen.util.SQLiteUtil;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 数据库配置服务类
 */
@Service
public class DatabaseConfigService {

    /**
     * 保存数据库配置
     */
    public String saveConfig(DatabaseConfig config) {
        String sql;
        boolean isNew = false;
        
        // 如果没有ID，则为新增操作
        if (config.getId() == null || config.getId().trim().isEmpty()) {
            isNew = true;
            config.setId(UUID.randomUUID().toString());
            sql = "INSERT INTO database_config (id, name, host, port, database_name, username, password, charset, db_type, enabled, created_time, updated_time) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))";
        } else {
            // 更新操作
            sql = "UPDATE database_config " +
                  "SET name=?, host=?, port=?, database_name=?, username=?, password=?, charset=?, db_type=?, enabled=?, updated_time=datetime('now') " +
                  "WHERE id=?";
        }

        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (isNew) {
                // 新增
                pstmt.setString(1, config.getId());
                pstmt.setString(2, config.getName());
                pstmt.setString(3, config.getHost());
                pstmt.setString(4, config.getPort());
                pstmt.setString(5, config.getDatabase());
                pstmt.setString(6, config.getUsername());
                pstmt.setString(7, config.getPassword());
                pstmt.setString(8, config.getCharset() != null ? config.getCharset() : "utf8");
                pstmt.setString(9, config.getDbType() != null ? config.getDbType() : "sqlite");
                pstmt.setInt(10, config.isEnabled() ? 1 : 0);
            } else {
                // 更新
                pstmt.setString(1, config.getName());
                pstmt.setString(2, config.getHost());
                pstmt.setString(3, config.getPort());
                pstmt.setString(4, config.getDatabase());
                pstmt.setString(5, config.getUsername());
                pstmt.setString(6, config.getPassword());
                pstmt.setString(7, config.getCharset() != null ? config.getCharset() : "utf8");
                pstmt.setString(8, config.getDbType() != null ? config.getDbType() : "sqlite");
                pstmt.setInt(9, config.isEnabled() ? 1 : 0);
                pstmt.setString(10, config.getId());
            }
            
            int result = pstmt.executeUpdate();
            return result > 0 ? "success" : "保存失败";
            
        } catch (SQLException e) {
            System.err.println("保存数据库配置失败: " + e.getMessage());
            e.printStackTrace();
            return "保存失败: " + e.getMessage();
        }
    }

    /**
     * 获取所有数据库配置
     */
    public List<DatabaseConfig> getAllConfigs() {
        List<DatabaseConfig> configs = new ArrayList<>();
        String sql = "SELECT * FROM database_config WHERE enabled = 1 ORDER BY created_time DESC";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                DatabaseConfig config = new DatabaseConfig();
                config.setId(rs.getString("id"));
                config.setName(rs.getString("name"));
                config.setHost(rs.getString("host"));
                config.setPort(rs.getString("port"));
                config.setDatabase(rs.getString("database_name"));
                config.setUsername(rs.getString("username"));
                config.setPassword(rs.getString("password"));
                config.setCharset(rs.getString("charset"));
                config.setDbType(rs.getString("db_type"));
                config.setEnabled(rs.getInt("enabled") == 1);
                configs.add(config);
            }
            
        } catch (SQLException e) {
            System.err.println("获取数据库配置列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return configs;
    }

    /**
     * 根据ID获取数据库配置
     */
    public DatabaseConfig getConfigById(String id) {
        String sql = "SELECT * FROM database_config WHERE id = ?";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    DatabaseConfig config = new DatabaseConfig();
                    config.setId(rs.getString("id"));
                    config.setName(rs.getString("name"));
                    config.setHost(rs.getString("host"));
                    config.setPort(rs.getString("port"));
                    config.setDatabase(rs.getString("database_name"));
                    config.setUsername(rs.getString("username"));
                    config.setPassword(rs.getString("password"));
                    config.setCharset(rs.getString("charset"));
                    config.setDbType(rs.getString("db_type"));
                    config.setEnabled(rs.getInt("enabled") == 1);
                    return config;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取数据库配置失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 删除数据库配置
     */
    public String deleteConfig(String id) {
        String sql = "UPDATE database_config SET enabled = 0, updated_time = datetime('now') WHERE id = ?";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            int result = pstmt.executeUpdate();
            
            return result > 0 ? "success" : "删除失败";
            
        } catch (SQLException e) {
            System.err.println("删除数据库配置失败: " + e.getMessage());
            e.printStackTrace();
            return "删除失败: " + e.getMessage();
        }
    }

    /**
     * 测试数据库连接
     */
    public String testConnection(String id) {
        DatabaseConfig config = getConfigById(id);
        if (config == null) {
            return "配置不存在";
        }
        
        // 根据数据库类型测试连接
        try {
            String dbType = config.getDbType();
            if ("sqlite".equalsIgnoreCase(dbType)) {
                try (Connection conn = SQLiteUtil.getConnection()) {
                    return "连接成功 - 使用SQLite数据库";
                }
            } else if ("mysql".equalsIgnoreCase(dbType)) {
                String url = "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase() + 
                            "?useUnicode=true&characterEncoding=" + config.getCharset();
                try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
                    return "连接成功 - 使用MySQL数据库";
                }
            } else if ("postgresql".equalsIgnoreCase(dbType)) {
                String url = "jdbc:postgresql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
                try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
                    return "连接成功 - 使用PostgreSQL数据库";
                }
            } else {
                return "不支持的数据库类型: " + dbType;
            }
        } catch (SQLException e) {
            return "连接失败: " + e.getMessage();
        }
    }
}