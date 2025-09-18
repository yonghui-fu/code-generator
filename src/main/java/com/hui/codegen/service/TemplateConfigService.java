package com.hui.codegen.service;

import com.hui.codegen.util.SQLiteUtil;
import com.hui.codegen.web.dto.TemplateGroup;
import com.hui.codegen.web.dto.TemplateInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
        // 自动为模板名称添加.ftl后缀（如果还没有的话）
        String templateName = templateInfo.getName();
        if (!templateName.endsWith(".ftl")) {
            templateName = templateName + ".ftl";
            templateInfo.setName(templateName);
        }
        
        // 检查模板名称是否已存在，如果存在则生成新的唯一名称
        templateName = generateUniqueTemplateName(templateName, templateInfo.getGroupId());
        templateInfo.setName(templateName);
        
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
     * 更新模板组
     */
    public String updateTemplateGroup(TemplateGroup templateGroup) {
        // 检查是否为系统默认组
        if ("default".equals(templateGroup.getId()) || "java".equals(templateGroup.getId()) || "config".equals(templateGroup.getId())) {
            return "系统默认组不能修改";
        }
        
        String sql = "UPDATE template_group SET name = ?, description = ?, enabled = ?, updated_time = datetime('now') WHERE id = ?";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, templateGroup.getName());
            pstmt.setString(2, templateGroup.getDescription());
            pstmt.setInt(3, templateGroup.isEnabled() ? 1 : 0);
            pstmt.setString(4, templateGroup.getId());
            
            int result = pstmt.executeUpdate();
            return result > 0 ? "success" : "更新失败";
            
        } catch (SQLException e) {
            System.err.println("更新模板组失败: " + e.getMessage());
            e.printStackTrace();
            return "更新失败: " + e.getMessage();
        }
    }

    /**
     * 删除模板组
     */
    public String deleteTemplateGroup(String groupId) {
        // 检查是否为系统默认组
//        if ("default".equals(groupId) || "java".equals(groupId) || "config".equals(groupId)) {
//            return "系统默认组不能删除";
//        }
        
        String deleteTemplatesSql = "DELETE FROM template_info WHERE group_id = ?";
        String deleteTemplateContentsSql = "DELETE FROM template_content WHERE template_name IN (SELECT name FROM template_info WHERE group_id = ?)";
        String deleteGroupSql = "DELETE FROM template_group WHERE id = ?";
        
        try (Connection conn = SQLiteUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // 删除组内所有模板内容
                try (PreparedStatement pstmt = conn.prepareStatement(deleteTemplateContentsSql)) {
                    pstmt.setString(1, groupId);
                    pstmt.executeUpdate();
                }
                
                // 删除组内所有模板信息
                try (PreparedStatement pstmt = conn.prepareStatement(deleteTemplatesSql)) {
                    pstmt.setString(1, groupId);
                    pstmt.executeUpdate();
                }
                
                // 删除模板组
                try (PreparedStatement pstmt = conn.prepareStatement(deleteGroupSql)) {
                    pstmt.setString(1, groupId);
                    int result = pstmt.executeUpdate();
                    if (result <= 0) {
                        throw new SQLException("删除模板组失败");
                    }
                }
                
                conn.commit();
                return "success";
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("删除模板组失败: " + e.getMessage());
            e.printStackTrace();
            return "删除失败: " + e.getMessage();
        }
    }

    /**
     * 初始化单个模板的默认内容
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
    
    /**
     * 生成唯一的模板名称
     */
    private String generateUniqueTemplateName(String originalName, String groupId) {
        try (Connection conn = SQLiteUtil.getConnection()) {
            // 检查原始名称是否已存在
            String checkSql = "SELECT COUNT(*) FROM template_info WHERE name = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, originalName);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // 原始名称不存在，直接使用
                        return originalName;
                    }
                }
            }
            
            // 原始名称已存在，生成新名称
            String baseName = originalName;
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = originalName.substring(0, dotIndex);
                extension = originalName.substring(dotIndex);
            }
            
            String newName = originalName;
            int counter = 1;
            while (true) {
                newName = baseName + "_" + counter + extension;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, newName);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            // 新名称不存在，可以使用
                            break;
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("检查模板名称唯一性时出错: " + e.getMessage());
                    throw e;
                }
                counter++;
                
                // 防止无限循环
                if (counter > 1000) {
                    throw new SQLException("无法为模板生成唯一名称，已尝试1000次");
                }
            }
            
            return newName;
        } catch (SQLException e) {
            System.err.println("生成唯一模板名称失败: " + e.getMessage());
            // 如果出现错误，返回原始名称，让数据库约束来处理
            return originalName;
        }
    }
    
    /**
     * 导出所有模板
     */
    public void exportTemplates(HttpServletResponse response) throws IOException {
        exportTemplatesByGroup(response, null);
    }
    
    /**
     * 导出指定模板组的模板
     */
    public void exportTemplatesByGroup(HttpServletResponse response, String groupId) throws IOException {
        response.setContentType("application/zip");
        if (groupId != null) {
            response.setHeader("Content-Disposition", "attachment; filename=\"templates_" + groupId + ".zip\"");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=\"templates.zip\"");
        }
        
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            if (groupId != null) {
                // 导出指定模板组的模板
                TemplateGroup group = getTemplateGroupById(groupId);
                if (group != null) {
                    // 导出模板组信息
                    String groupInfo = String.format("name=%s\ndescription=%s\nenabled=%s\n", 
                        group.getName(), group.getDescription(), group.isEnabled());
                    ZipEntry groupEntry = new ZipEntry(group.getId() + "/group.info");
                    zipOut.putNextEntry(groupEntry);
                    zipOut.write(groupInfo.getBytes("UTF-8"));
                    zipOut.closeEntry();
                    
                    // 导出该组的所有模板
                    for (TemplateInfo template : group.getTemplates()) {
                        // 获取模板内容
                        String content = getTemplateContent(template.getName());
                        
                        // 创建模板信息文件
                        String templateInfo = String.format("name=%s\ndisplayName=%s\ndescription=%s\ntemplateType=%s\nenabled=%s\ngroupId=%s\n", 
                            template.getName(), template.getDisplayName(), template.getDescription(), 
                            template.getTemplateType(), template.isEnabled(), template.getGroupId());
                        ZipEntry infoEntry = new ZipEntry(group.getId() + "/" + template.getName() + ".info");
                        zipOut.putNextEntry(infoEntry);
                        zipOut.write(templateInfo.getBytes("UTF-8"));
                        zipOut.closeEntry();
                        
                        // 创建模板内容文件
                        ZipEntry contentEntry = new ZipEntry(group.getId() + "/" + template.getName());
                        zipOut.putNextEntry(contentEntry);
                        zipOut.write(content.getBytes("UTF-8"));
                        zipOut.closeEntry();
                    }
                }
            } else {
                // 获取所有模板组
                List<TemplateGroup> groups = getAllTemplateGroups();
                
                // 导出每个模板组和模板
                for (TemplateGroup group : groups) {
                    // 导出模板组信息
                    String groupInfo = String.format("name=%s\ndescription=%s\nenabled=%s\n", 
                        group.getName(), group.getDescription(), group.isEnabled());
                    ZipEntry groupEntry = new ZipEntry(group.getId() + "/group.info");
                    zipOut.putNextEntry(groupEntry);
                    zipOut.write(groupInfo.getBytes("UTF-8"));
                    zipOut.closeEntry();
                    
                    // 导出该组的所有模板
                    for (TemplateInfo template : group.getTemplates()) {
                        // 获取模板内容
                        String content = getTemplateContent(template.getName());
                        
                        // 创建模板信息文件
                        String templateInfo = String.format("name=%s\ndisplayName=%s\ndescription=%s\ntemplateType=%s\nenabled=%s\ngroupId=%s\n", 
                            template.getName(), template.getDisplayName(), template.getDescription(), 
                            template.getTemplateType(), template.isEnabled(), template.getGroupId());
                        ZipEntry infoEntry = new ZipEntry(group.getId() + "/" + template.getName() + ".info");
                        zipOut.putNextEntry(infoEntry);
                        zipOut.write(templateInfo.getBytes("UTF-8"));
                        zipOut.closeEntry();
                        
                        // 创建模板内容文件
                        ZipEntry contentEntry = new ZipEntry(group.getId() + "/" + template.getName());
                        zipOut.putNextEntry(contentEntry);
                        zipOut.write(content.getBytes("UTF-8"));
                        zipOut.closeEntry();
                    }
                }
            }
        }
    }
    
    /**
     * 根据ID获取模板组
     */
    public TemplateGroup getTemplateGroupById(String groupId) {
        String sql = "SELECT id, name, description, enabled, sort_order " +
                     "FROM template_group " +
                     "WHERE id = ? AND enabled = 1";
        
        try (Connection conn = SQLiteUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, groupId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    TemplateGroup group = new TemplateGroup();
                    group.setId(rs.getString("id"));
                    group.setName(rs.getString("name"));
                    group.setDescription(rs.getString("description"));
                    group.setEnabled(rs.getInt("enabled") == 1);
                    
                    // 加载该组的模板列表
                    group.setTemplates(getTemplatesByGroupId(group.getId()));
                    return group;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取模板组失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 导入模板
     */
    public String importTemplates(MultipartFile file) {
        return importTemplatesToGroup(file, null);
    }
    
    /**
     * 导入模板到指定模板组
     */
    public String importTemplatesToGroup(MultipartFile file, String targetGroupId) {
        System.out.println("开始导入模板到组: " + targetGroupId);
        
        // 添加空值检查
        if (file == null) {
            System.err.println("文件对象为空");
            return "导入失败: 文件对象为空";
        }
        
        if (targetGroupId == null || targetGroupId.trim().isEmpty()) {
            System.err.println("目标组ID为空");
            return "导入失败: 目标组ID不能为空";
        }
        
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                System.err.println("上传的文件为空");
                return "导入失败: 上传的文件为空";
            }
            
            System.out.println("文件大小: " + file.getSize() + " 字节");
            System.out.println("文件名称: " + file.getOriginalFilename());
            
            ZipInputStream zipIn = new ZipInputStream(file.getInputStream());
            ZipEntry entry;
            
            // 用于存储组信息和模板信息
            List<TemplateGroup> groupsToImport = new ArrayList<>();
            List<TemplateImportInfo> templatesToImport = new ArrayList<>();
            
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                System.out.println("处理ZIP条目: " + entryName);
                
                // 处理组信息文件
                if (entryName.endsWith("/group.info")) {
                    String groupInfo = readZipEntry(zipIn);
                    System.out.println("读取组信息: " + groupInfo);
                    TemplateGroup group = parseGroupInfo(groupInfo, entryName);
                    if (group != null) {
                        System.out.println("解析组信息成功: " + group.getId());
                        groupsToImport.add(group);
                    } else {
                        System.out.println("解析组信息失败");
                    }
                } 
                // 处理模板信息文件
                else if (entryName.endsWith(".info") && !entryName.endsWith("/group.info")) {
                    String templateInfo = readZipEntry(zipIn);
                    System.out.println("读取模板信息: " + templateInfo);
                    TemplateImportInfo importInfo = parseTemplateInfo(templateInfo, entryName);
                    if (importInfo != null) {
                        System.out.println("解析模板信息成功: " + importInfo.getTemplateName());
                        // 如果指定了目标组ID，则将模板导入到该组
                        // 否则使用模板文件中指定的组ID
                        if (targetGroupId != null) {
                            System.out.println("设置目标组ID: " + targetGroupId + " (原组ID: " + importInfo.getGroupId() + ")");
                            importInfo.setGroupId(targetGroupId);
                        }
                        System.out.println("添加模板信息到导入列表: " + importInfo.getTemplateName() + ", 组ID: " + importInfo.getGroupId());
                        templatesToImport.add(importInfo);
                    } else {
                        System.out.println("解析模板信息失败");
                    }
                }
                // 处理模板内容文件
                else if (!entryName.endsWith(".info") && !entryName.endsWith("/")) {
                    String content = readZipEntry(zipIn);
                    System.out.println("读取模板内容，大小: " + (content != null ? content.length() : 0));
                    // 找到对应的模板信息并设置内容
                    for (TemplateImportInfo importInfo : templatesToImport) {
                        String templateFileName = entryName.substring(entryName.lastIndexOf("/") + 1);
                        if (importInfo.getTemplateName().equals(templateFileName)) {
                            System.out.println("设置模板内容: " + templateFileName);
                            importInfo.setContent(content);
                            break;
                        }
                    }
                }
                
                zipIn.closeEntry();
            }
            
            // 检查是否有要导入的模板
            if (templatesToImport.isEmpty()) {
                System.out.println("没有找到要导入的模板");
                return "导入失败: 没有找到要导入的模板";
            }
            
            System.out.println("ZIP处理完成，准备导入组和模板");
            System.out.println("待导入模板数量: " + templatesToImport.size());
            for (TemplateImportInfo info : templatesToImport) {
                System.out.println("  - " + info.getTemplateName() + " -> 组 " + info.getGroupId());
            }
            
            // 导入组和模板
            importGroupsAndTemplates(groupsToImport, templatesToImport, targetGroupId);
            
            // 记录导入统计信息
            System.out.println("导入完成，导入了 " + groupsToImport.size() + " 个组和 " + templatesToImport.size() + " 个模板到组 " + targetGroupId);
            
            return "success";
        } catch (Exception e) {
            System.err.println("导入模板失败: " + e.getMessage());
            e.printStackTrace();
            return "导入失败: " + e.getMessage();
        }
    }
    
    /**
     * 读取ZIP条目内容
     */
    private String readZipEntry(ZipInputStream zipIn) throws IOException {
        StringBuilder content = new StringBuilder();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = zipIn.read(buffer)) != -1) {
            content.append(new String(buffer, 0, read, "UTF-8"));
        }
        return content.toString();
    }
    
    /**
     * 解析组信息
     */
    private TemplateGroup parseGroupInfo(String groupInfo, String entryName) {
        try {
            TemplateGroup group = new TemplateGroup();
            String groupId = entryName.substring(0, entryName.indexOf("/"));
            group.setId(groupId);
            
            String[] lines = groupInfo.split("\n");
            for (String line : lines) {
                if (line.startsWith("name=")) {
                    group.setName(line.substring(5));
                } else if (line.startsWith("description=")) {
                    group.setDescription(line.substring(12));
                } else if (line.startsWith("enabled=")) {
                    group.setEnabled("true".equals(line.substring(8)));
                }
            }
            
            return group;
        } catch (Exception e) {
            System.err.println("解析组信息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析模板信息
     */
    private TemplateImportInfo parseTemplateInfo(String templateInfo, String entryName) {
        try {
            TemplateImportInfo importInfo = new TemplateImportInfo();
            String[] lines = templateInfo.split("\n");
            for (String line : lines) {
                if (line.startsWith("name=")) {
                    importInfo.setTemplateName(line.substring(5));
                } else if (line.startsWith("displayName=")) {
                    importInfo.setDisplayName(line.substring(12));
                } else if (line.startsWith("description=")) {
                    importInfo.setDescription(line.substring(12));
                } else if (line.startsWith("templateType=")) {
                    importInfo.setTemplateType(line.substring(13));
                } else if (line.startsWith("enabled=")) {
                    importInfo.setEnabled("true".equals(line.substring(8)));
                } else if (line.startsWith("groupId=")) {
                    importInfo.setGroupId(line.substring(8));
                }
            }
            
            return importInfo;
        } catch (Exception e) {
            System.err.println("解析模板信息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 导入组和模板
     */
    private void importGroupsAndTemplates(List<TemplateGroup> groupsToImport, List<TemplateImportInfo> templatesToImport, String targetGroupId) {
        System.out.println("开始导入组和模板，目标组ID: " + targetGroupId);
        System.out.println("待导入组数量: " + groupsToImport.size() + ", 待导入模板数量: " + templatesToImport.size());
        
        try (Connection conn = SQLiteUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // 如果指定了目标组ID，确保该组存在
                if (targetGroupId != null) {
                    System.out.println("确保目标组存在: " + targetGroupId);
                    ensureGroupExists(conn, targetGroupId);
                }
                
                // 导入模板 - 作为新模板导入到目标组，完全独立于原模板组
                System.out.println("开始导入模板到目标组...");
                for (TemplateImportInfo templateInfo : templatesToImport) {
                    System.out.println("作为新模板导入: " + templateInfo.getTemplateName() + " 到目标组: " + targetGroupId);
                    // 作为新模板导入到目标组，与原模板组完全无关
                    importTemplateAsNewToTargetGroup(conn, templateInfo, targetGroupId);
                }
                
                // 验证导入结果
                System.out.println("验证导入结果...");
                verifyImportedTemplates(conn, targetGroupId, templatesToImport);
                
                conn.commit();
                System.out.println("导入完成，事务已提交");
            } catch (Exception e) {
                System.err.println("导入过程中发生异常，回滚事务: " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
                throw new SQLException("导入失败", e);
            }
        } catch (Exception e) {
            System.err.println("导入组和模板失败: " + e.getMessage());
            e.printStackTrace();
            // 将检查异常包装为运行时异常，以便控制器能正确处理
            throw new RuntimeException("导入失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 作为新模板导入到目标组，确保与原模板组完全无关
     */
    private void importTemplateAsNewToTargetGroup(Connection conn, TemplateImportInfo templateInfo, String targetGroupId) throws SQLException {
        System.out.println("作为新模板导入到目标组: " + templateInfo.getTemplateName() + " -> " + targetGroupId);
        
        // 生成新的模板名称，确保在目标组中唯一
        String newTemplateName = generateUniqueTemplateNameForTargetGroup(conn, templateInfo.getTemplateName(), targetGroupId);
        System.out.println("新模板名称: " + newTemplateName);
        
        String insertTemplateSql = "INSERT INTO template_info (name, display_name, description, template_type, enabled, sort_order, group_id, created_time, updated_time) " +
                                  "VALUES (?, ?, ?, ?, ?, 0, ?, datetime('now'), datetime('now'))";
        String insertContentSql = "INSERT INTO template_content (template_name, content, created_time, updated_time) " +
                                 "VALUES (?, ?, datetime('now'), datetime('now'))";
        
        // 插入模板信息到目标组
        try (PreparedStatement templateStmt = conn.prepareStatement(insertTemplateSql)) {
            templateStmt.setString(1, newTemplateName);
            templateStmt.setString(2, templateInfo.getDisplayName());
            templateStmt.setString(3, templateInfo.getDescription());
            templateStmt.setString(4, templateInfo.getTemplateType());
            templateStmt.setInt(5, templateInfo.isEnabled() ? 1 : 0);
            templateStmt.setString(6, targetGroupId);
            int result = templateStmt.executeUpdate();
            System.out.println("插入新模板信息到目标组结果: " + result);
            
            if (result <= 0) {
                throw new SQLException("插入模板信息失败");
            }
        } catch (SQLException e) {
            System.err.println("插入新模板信息到目标组失败: " + e.getMessage());
            // 检查是否是唯一性约束冲突
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("唯一性约束冲突，尝试生成新的模板名称");
                // 尝试重新生成名称并再次插入
                String alternativeName = generateUniqueTemplateNameForTargetGroup(conn, newTemplateName + "_copy", targetGroupId);
                try (PreparedStatement templateStmt = conn.prepareStatement(insertTemplateSql)) {
                    templateStmt.setString(1, alternativeName);
                    templateStmt.setString(2, templateInfo.getDisplayName());
                    templateStmt.setString(3, templateInfo.getDescription());
                    templateStmt.setString(4, templateInfo.getTemplateType());
                    templateStmt.setInt(5, templateInfo.isEnabled() ? 1 : 0);
                    templateStmt.setString(6, targetGroupId);
                    int result = templateStmt.executeUpdate();
                    System.out.println("使用替代名称插入新模板信息到目标组结果: " + result);
                    
                    if (result <= 0) {
                        throw new SQLException("使用替代名称插入模板信息失败");
                    }
                    // 更新要使用的模板名称
                    newTemplateName = alternativeName;
                }
            } else {
                throw e;
            }
        }
        
        // 插入模板内容
        if (templateInfo.getContent() != null && !templateInfo.getContent().isEmpty()) {
            try (PreparedStatement contentStmt = conn.prepareStatement(insertContentSql)) {
                contentStmt.setString(1, newTemplateName);
                contentStmt.setString(2, templateInfo.getContent());
                int result = contentStmt.executeUpdate();
                System.out.println("插入新模板内容结果: " + result);
                
                if (result <= 0) {
                    throw new SQLException("插入模板内容失败");
                }
            } catch (SQLException e) {
                System.err.println("插入新模板内容失败: " + e.getMessage());
                throw e;
            }
        } else {
            System.out.println("模板内容为空，跳过插入内容");
        }
        
        System.out.println("新模板 " + newTemplateName + " 成功导入到目标组 " + targetGroupId + "，与原模板组无关");
    }
    
    /**
     * 为目标组生成唯一的模板名称，确保与原模板组无关
     */
    private String generateUniqueTemplateNameForTargetGroup(Connection conn, String originalName, String targetGroupId) throws SQLException {
        // 检查原始名称是否在目标组中已存在
        String checkSql = "SELECT COUNT(*) FROM template_info WHERE name = ? AND group_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, originalName);
            checkStmt.setString(2, targetGroupId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // 原始名称在目标组中不存在，直接使用
                    System.out.println("模板名称 " + originalName + " 在目标组 " + targetGroupId + " 中不存在，可以直接使用");
                    return originalName;
                } else {
                    System.out.println("模板名称 " + originalName + " 在目标组 " + targetGroupId + " 中已存在，需要生成新名称");
                }
            }
        }
        
        // 原始名称在目标组中已存在，生成新名称
        String baseName = originalName;
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = originalName.substring(0, dotIndex);
            extension = originalName.substring(dotIndex);
        }
        
        String newName = originalName;
        int counter = 1;
        while (true) {
            newName = baseName + "_" + counter + extension;
            System.out.println("尝试使用名称: " + newName);
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, newName);
                checkStmt.setString(2, targetGroupId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // 新名称在目标组中不存在，可以使用
                        System.out.println("找到唯一名称: " + newName);
                        break;
                    } else {
                        System.out.println("名称 " + newName + " 在目标组 " + targetGroupId + " 中已存在，继续尝试");
                    }
                }
            } catch (SQLException e) {
                System.err.println("检查模板名称唯一性时出错: " + e.getMessage());
                throw e;
            }
            counter++;
            
            // 防止无限循环
            if (counter > 1000) {
                throw new SQLException("无法为模板生成唯一名称，已尝试1000次");
            }
        }
        
        return newName;
    }
    
    /**
     * 验证导入的模板
     */
    private void verifyImportedTemplates(Connection conn, String targetGroupId, List<TemplateImportInfo> templatesToImport) throws SQLException {
        System.out.println("验证目标组中的模板数量...");
        String countSql = "SELECT COUNT(*) FROM template_info WHERE group_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
            pstmt.setString(1, targetGroupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("目标组 " + targetGroupId + " 中的模板数量: " + count);
                    System.out.println("预期导入模板数量: " + templatesToImport.size());
                }
            }
        }
        
        // 列出目标组中的所有模板
        System.out.println("目标组中的所有模板:");
        String listSql = "SELECT name, display_name FROM template_info WHERE group_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(listSql)) {
            pstmt.setString(1, targetGroupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("  模板: " + rs.getString("name") + " (" + rs.getString("display_name") + ")");
                }
            }
        }
    }
    
    /**
     * 确保模板组存在
     */
    private void ensureGroupExists(Connection conn, String groupId) throws SQLException {
        // 检查groupId是否为空或null
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new SQLException("模板组ID不能为空");
        }
        
        String checkSql = "SELECT COUNT(*) FROM template_group WHERE id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, groupId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // 组不存在，创建一个默认组
                    System.out.println("模板组不存在，创建默认组: " + groupId);
                    String insertDefaultGroupSql = "INSERT INTO template_group (id, name, description, enabled, sort_order, created_time, updated_time) VALUES (?, ?, ?, 1, 0, datetime('now'), datetime('now'))";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertDefaultGroupSql)) {
                        insertStmt.setString(1, groupId);
                        insertStmt.setString(2, "导入组-" + groupId);
                        insertStmt.setString(3, "通过模板导入创建的组");
                        int result = insertStmt.executeUpdate();
                        System.out.println("创建模板组结果: " + result);
                        if (result <= 0) {
                            throw new SQLException("创建模板组失败");
                        }
                    }
                } else {
                    System.out.println("模板组已存在: " + groupId);
                }
            }
        }
    }
    
    /**
     * 导入组
     */
    private void importGroup(Connection conn, TemplateGroup group) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM template_group WHERE id = ?";
        String insertSql = "INSERT OR IGNORE INTO template_group (id, name, description, enabled, sort_order, created_time, updated_time) " +
                          "VALUES (?, ?, ?, ?, 0, datetime('now'), datetime('now'))";
        
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, group.getId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // 组不存在，插入新组
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, group.getId());
                        insertStmt.setString(2, group.getName());
                        insertStmt.setString(3, group.getDescription());
                        insertStmt.setInt(4, group.isEnabled() ? 1 : 0);
                        int result = insertStmt.executeUpdate();
                        System.out.println("插入模板组结果: " + result);
                    }
                } else {
                    System.out.println("模板组已存在，更新信息: " + group.getId());
                    String updateSql = "UPDATE template_group SET name = ?, description = ?, enabled = ?, updated_time = datetime('now') WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, group.getName());
                        updateStmt.setString(2, group.getDescription());
                        updateStmt.setInt(3, group.isEnabled() ? 1 : 0);
                        updateStmt.setString(4, group.getId());
                        int result = updateStmt.executeUpdate();
                        System.out.println("更新模板组结果: " + result);
                    }
                }
            }
        }
    }
    
    /**
     * 导入模板
     */
    private void importTemplate(Connection conn, TemplateImportInfo templateInfo) throws SQLException {
        System.out.println("开始导入模板: " + templateInfo.getTemplateName() + ", 组ID: " + templateInfo.getGroupId());
        
        String insertTemplateSql = "INSERT OR IGNORE INTO template_info (name, display_name, description, template_type, enabled, sort_order, group_id, created_time, updated_time) " +
                                  "VALUES (?, ?, ?, ?, ?, 0, ?, datetime('now'), datetime('now'))";
        String insertContentSql = "INSERT OR IGNORE INTO template_content (template_name, content, created_time, updated_time) " +
                                 "VALUES (?, ?, datetime('now'), datetime('now'))";
        
        // 插入模板信息
        try (PreparedStatement templateStmt = conn.prepareStatement(insertTemplateSql)) {
            templateStmt.setString(1, templateInfo.getTemplateName());
            templateStmt.setString(2, templateInfo.getDisplayName());
            templateStmt.setString(3, templateInfo.getDescription());
            templateStmt.setString(4, templateInfo.getTemplateType());
            templateStmt.setInt(5, templateInfo.isEnabled() ? 1 : 0);
            templateStmt.setString(6, templateInfo.getGroupId());
            int result = templateStmt.executeUpdate();
            System.out.println("插入模板信息结果: " + result);
            
            // 如果没有插入成功，检查是否因为违反唯一约束
            if (result == 0) {
                System.out.println("模板信息未插入，可能已存在或违反约束");
                // 检查模板是否已存在
                String checkSql = "SELECT COUNT(*) FROM template_info WHERE name = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, templateInfo.getTemplateName());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            System.out.println("模板 " + templateInfo.getTemplateName() + " 已存在，尝试更新");
                            String updateSql = "UPDATE template_info SET display_name = ?, description = ?, template_type = ?, enabled = ?, group_id = ?, updated_time = datetime('now') WHERE name = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                updateStmt.setString(1, templateInfo.getDisplayName());
                                updateStmt.setString(2, templateInfo.getDescription());
                                updateStmt.setString(3, templateInfo.getTemplateType());
                                updateStmt.setInt(4, templateInfo.isEnabled() ? 1 : 0);
                                updateStmt.setString(5, templateInfo.getGroupId());
                                updateStmt.setString(6, templateInfo.getTemplateName());
                                int updateResult = updateStmt.executeUpdate();
                                System.out.println("更新模板信息结果: " + updateResult);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("插入模板信息失败: " + e.getMessage());
            throw e;
        }
        
        // 插入模板内容
        if (templateInfo.getContent() != null) {
            try (PreparedStatement contentStmt = conn.prepareStatement(insertContentSql)) {
                contentStmt.setString(1, templateInfo.getTemplateName());
                contentStmt.setString(2, templateInfo.getContent());
                int result = contentStmt.executeUpdate();
                System.out.println("插入模板内容结果: " + result);
                
                // 如果没有插入成功，尝试更新
                if (result == 0) {
                    System.out.println("模板内容未插入，尝试更新");
                    String updateSql = "UPDATE template_content SET content = ?, updated_time = datetime('now') WHERE template_name = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, templateInfo.getContent());
                        updateStmt.setString(2, templateInfo.getTemplateName());
                        int updateResult = updateStmt.executeUpdate();
                        System.out.println("更新模板内容结果: " + updateResult);
                    }
                }
            } catch (SQLException e) {
                System.err.println("插入模板内容失败: " + e.getMessage());
                throw e;
            }
        }
        
        // 验证模板是否成功插入
        String verifySql = "SELECT COUNT(*) FROM template_info WHERE name = ? AND group_id = ?";
        try (PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
            verifyStmt.setString(1, templateInfo.getTemplateName());
            verifyStmt.setString(2, templateInfo.getGroupId());
            try (ResultSet rs = verifyStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("模板 " + templateInfo.getTemplateName() + " 成功插入到组 " + templateInfo.getGroupId());
                } else {
                    System.out.println("警告：模板 " + templateInfo.getTemplateName() + " 未成功插入到组 " + templateInfo.getGroupId());
                    // 检查模板是否存在于其他组中
                    String checkOtherGroupSql = "SELECT group_id FROM template_info WHERE name = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkOtherGroupSql)) {
                        checkStmt.setString(1, templateInfo.getTemplateName());
                        try (ResultSet rs2 = checkStmt.executeQuery()) {
                            if (rs2.next()) {
                                String otherGroupId = rs2.getString("group_id");
                                System.out.println("模板 " + templateInfo.getTemplateName() + " 已存在于组 " + otherGroupId);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 模板导入信息类
     */
    private static class TemplateImportInfo {
        private String templateName;
        private String displayName;
        private String description;
        private String templateType;
        private boolean enabled;
        private String groupId;
        private String content;
        
        // Getters and Setters
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getTemplateType() { return templateType; }
        public void setTemplateType(String templateType) { this.templateType = templateType; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}