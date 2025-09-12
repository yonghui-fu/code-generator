package com.hui.codegen.service;

import com.hui.codegen.util.SQLiteUtil;
import com.hui.codegen.web.dto.TemplateGroup;
import com.hui.codegen.web.dto.TemplateInfo;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 模板配置服务类
 */
@Service
public class TemplateConfigService {

    /**
     * 获取所有模板组及其模板
     */
    public List<TemplateGroup> getAllTemplateGroups() {
        List<TemplateGroup> groups = new ArrayList<>();
        String sql = "SELECT id, name, description, enabled, sort_order " +
                     "FROM template_group " +
                     "WHERE enabled = 1 " +
                     "ORDER BY sort_order, created_time";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                TemplateGroup group = new TemplateGroup();
                group.setId(rs.getString("id"));
                group.setName(rs.getString("name"));
                group.setDescription(rs.getString("description"));
                group.setEnabled(rs.getInt("enabled") == 1);
                
                // 加载该组的模板列表
                group.setTemplates(getTemplatesByGroupId(group.getId()));
                groups.add(group);
            }
            
        } catch (SQLException e) {
            System.err.println("获取模板组列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return groups;
    }

    /**
     * 根据组ID获取模板列表
     */
    public List<TemplateInfo> getTemplatesByGroupId(String groupId) {
        List<TemplateInfo> templates = new ArrayList<>();
        String sql = "SELECT name, display_name, description, template_type, enabled, sort_order, group_id " +
                     "FROM template_info " +
                     "WHERE group_id = ? AND enabled = 1 " +
                     "ORDER BY sort_order, created_time";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, groupId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TemplateInfo template = new TemplateInfo();
                    template.setName(rs.getString("name"));
                    template.setDisplayName(rs.getString("display_name"));
                    template.setDescription(rs.getString("description"));
                    template.setTemplateType(rs.getString("template_type"));
                    template.setEnabled(rs.getInt("enabled") == 1);
                    template.setGroupId(rs.getString("group_id"));
                    templates.add(template);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取模板列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return templates;
    }

    /**
     * 获取模板内容
     */
    public String getTemplateContent(String templateName) {
        String sql = "SELECT content FROM template_content WHERE template_name = ?";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, templateName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("content");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取模板内容失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "";
    }

    /**
     * 保存模板内容
     */
    public String saveTemplateContent(String templateName, String content) {
        String sql = "INSERT OR REPLACE INTO template_content (template_name, content, updated_time) " +
                     "VALUES (?, ?, datetime('now'))";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, templateName);
            pstmt.setString(2, content);
            
            int result = pstmt.executeUpdate();
            return result > 0 ? "success" : "保存失败";
            
        } catch (SQLException e) {
            System.err.println("保存模板内容失败: " + e.getMessage());
            e.printStackTrace();
            return "保存失败: " + e.getMessage();
        }
    }

    /**
     * 添加新模板
     */
    public String addTemplate(TemplateInfo templateInfo) {
        String sql = "INSERT INTO template_info (name, display_name, description, template_type, enabled, sort_order, group_id, created_time, updated_time) " +
                     "VALUES (?, ?, ?, ?, ?, 0, ?, datetime('now'), datetime('now'))";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, templateInfo.getName());
            pstmt.setString(2, templateInfo.getDisplayName());
            pstmt.setString(3, templateInfo.getDescription());
            pstmt.setString(4, templateInfo.getTemplateType());
            pstmt.setInt(5, templateInfo.isEnabled() ? 1 : 0);
            pstmt.setString(6, templateInfo.getGroupId());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 为新模板创建默认内容
                saveTemplateContent(templateInfo.getName(), "// 新模板内容\npackage ${packageName};\n\npublic class ${className} {\n    // TODO: 实现类内容\n}");
                return "success";
            } else {
                return "添加失败";
            }
            
        } catch (SQLException e) {
            System.err.println("添加模板失败: " + e.getMessage());
            e.printStackTrace();
            return "添加失败: " + e.getMessage();
        }
    }

    /**
     * 删除模板
     */
    public String deleteTemplate(String templateName) {
        // 检查是否为系统默认模板
        List<String> systemTemplates = Arrays.asList("entity.ftl", "controller.ftl", "service.ftl", "mapper.ftl", "mapperXml.ftl");
        if (systemTemplates.contains(templateName)) {
            return "系统默认模板不能删除";
        }
        
        String deleteTemplateInfoSql = "DELETE FROM template_info WHERE name = ?";
        String deleteTemplateContentSql = "DELETE FROM template_content WHERE template_name = ?";
        
        try (Connection conn = SQLiteUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteTemplateInfoSql);
                 PreparedStatement pstmt2 = conn.prepareStatement(deleteTemplateContentSql)) {
                
                pstmt1.setString(1, templateName);
                pstmt2.setString(1, templateName);
                
                pstmt1.executeUpdate();
                pstmt2.executeUpdate();
                
                conn.commit();
                return "success";
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("删除模板失败: " + e.getMessage());
            e.printStackTrace();
            return "删除失败: " + e.getMessage();
        }
    }

    /**
     * 添加模板组
     */
    public String addTemplateGroup(TemplateGroup templateGroup) {
        String sql = "INSERT INTO template_group (id, name, description, enabled, sort_order, created_time, updated_time) " +
                     "VALUES (?, ?, ?, ?, 0, datetime('now'), datetime('now'))";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String groupId = UUID.randomUUID().toString().substring(0, 8);
            pstmt.setString(1, groupId);
            pstmt.setString(2, templateGroup.getName());
            pstmt.setString(3, templateGroup.getDescription());
            pstmt.setInt(4, templateGroup.isEnabled() ? 1 : 0);
            
            int result = pstmt.executeUpdate();
            return result > 0 ? "success" : "添加失败";
            
        } catch (SQLException e) {
            System.err.println("添加模板组失败: " + e.getMessage());
            e.printStackTrace();
            return "添加失败: " + e.getMessage();
        }
    }

    /**
     * 删除模板组
     */
    public String deleteTemplateGroup(String groupId) {
        // 检查是否为系统默认组
        if ("default".equals(groupId) || "java".equals(groupId) || "config".equals(groupId)) {
            return "系统默认组不能删除";
        }
        
        // 检查组内是否还有模板
        String checkTemplatesSql = "SELECT COUNT(*) FROM template_info WHERE group_id = ? AND enabled = 1";
        String deleteGroupSql = "UPDATE template_group SET enabled = 0, updated_time = datetime('now') WHERE id = ?";
        
        try (Connection conn = SQLiteUtil.getConnection()) {
            
            // 检查组内模板数量
            try (PreparedStatement checkStmt = conn.prepareStatement(checkTemplatesSql)) {
                checkStmt.setString(1, groupId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return "该组内还有模板，请先删除组内所有模板";
                    }
                }
            }
            
            // 删除组
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteGroupSql)) {
                deleteStmt.setString(1, groupId);
                int result = deleteStmt.executeUpdate();
                return result > 0 ? "success" : "删除失败";
            }
            
        } catch (SQLException e) {
            System.err.println("删除模板组失败: " + e.getMessage());
            e.printStackTrace();
            return "删除失败: " + e.getMessage();
        }
    }

    /**
     * 重置模板为默认内容
     */
    public String resetTemplate(String templateName) {
        // 只能重置系统默认模板
        List<String> systemTemplates = Arrays.asList("entity.ftl", "controller.ftl", "service.ftl", "mapper.ftl", "mapperXml.ftl");
        if (!systemTemplates.contains(templateName)) {
            return "只能重置系统默认模板";
        }
        
        try (Connection conn = SQLiteUtil.getConnection()) {
            // 重新初始化该模板的默认内容
            initSingleTemplateContent(conn, templateName);
            return "success";
        } catch (SQLException e) {
            System.err.println("重置模板失败: " + e.getMessage());
            e.printStackTrace();
            return "重置失败: " + e.getMessage();
        }
    }

    /**
     * 初始化单个模板的默认内容
     */
    private void initSingleTemplateContent(Connection conn, String templateName) throws SQLException {
        String content = getDefaultTemplateContent(templateName);
        if (content != null) {
            String sql = "UPDATE template_content SET content = ?, updated_time = datetime('now') WHERE template_name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, content);
                pstmt.setString(2, templateName);
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * 获取默认模板内容
     */
    private String getDefaultTemplateContent(String templateName) {
        switch (templateName) {
            case "entity.ftl":
                return "package ${packageName}.entity;\n\n" +
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
            case "controller.ftl":
                return "package ${packageName}.controller;\n\n" +
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
            // 其他模板内容可以类似添加
            default:
                return null;
        }
    }
}