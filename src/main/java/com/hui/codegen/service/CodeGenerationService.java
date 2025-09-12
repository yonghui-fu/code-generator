package com.hui.codegen.service;

import com.hui.codegen.model.DatabaseConfig;
import com.hui.codegen.web.dto.CodeGenRequest;
import com.hui.codegen.web.dto.PreviewFileInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成核心服务类
 */
@Service
public class CodeGenerationService {

    @Autowired
    private DatabaseConfigService databaseConfigService;
    
    @Autowired
    private DatabaseTableService databaseTableService;
    
    @Autowired
    private TemplateConfigService templateConfigService;

    private Configuration freemarkerConfig;

    public CodeGenerationService() {
        // 初始化FreeMarker配置
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        freemarkerConfig.setDefaultEncoding("UTF-8");
    }

    /**
     * 生成代码预览
     */
    public List<PreviewFileInfo> generatePreview(CodeGenRequest request) throws Exception {
        List<PreviewFileInfo> files = new ArrayList<>();
        
        // 使用SQLite，不需要实际的数据库配置
        DatabaseConfig config = new DatabaseConfig();
        config.setDatabase("sqlite");

        for (String tableName : request.getSelectedTables()) {
            // 获取表信息
            Map<String, Object> tableData = buildTableData(config, tableName, request.getPackageName());
            
            for (String templateName : request.getSelectedTemplates()) {
                String content = generateFileContent(templateName, tableData);
                String fileName = buildFileName(templateName, tableData);
                String filePath = buildFilePath(templateName, tableData, request.getPackageName());
                String fileType = getFileType(templateName);
                
                files.add(new PreviewFileInfo(fileName, filePath, fileType, content));
            }
        }
        
        return files;
    }

    /**
     * 构建表数据模型
     */
    private Map<String, Object> buildTableData(DatabaseConfig config, String tableName, String packageName) {
        Map<String, Object> data = new HashMap<>();
        
        // 表基本信息
        data.put("tableName", tableName);
        data.put("className", convertToClassName(tableName));
        data.put("classNameLowerFirst", convertToFieldName(convertToClassName(tableName)));
        data.put("packageName", packageName);
        
        // 获取表详细信息
        Map<String, Object> tableDetail = databaseTableService.getTableDetail(config, tableName);
        data.put("tableComment", tableDetail.getOrDefault("tableComment", ""));
        
        // 获取列信息
        List<Map<String, Object>> columns = databaseTableService.getTableColumns(config, tableName);
        data.put("columns", columns);
        
        // 查找主键
        Map<String, Object> primaryKey = null;
        for (Map<String, Object> column : columns) {
            if (Boolean.TRUE.equals(column.get("primaryKey"))) {
                primaryKey = column;
                break;
            }
        }
        data.put("primaryKey", primaryKey);
        
        return data;
    }

    /**
     * 生成文件内容
     */
    private String generateFileContent(String templateName, Map<String, Object> data) throws Exception {
        String templateContent = templateConfigService.getTemplateContent(templateName);
        
        if (templateContent == null || templateContent.trim().isEmpty()) {
            return "// 模板内容为空\n";
        }
        
        // 使用FreeMarker处理模板
        Template template = new Template(templateName, templateContent, freemarkerConfig);
        StringWriter writer = new StringWriter();
        template.process(data, writer);
        
        return writer.toString();
    }

    /**
     * 构建文件名
     */
    private String buildFileName(String templateName, Map<String, Object> data) {
        String className = (String) data.get("className");
        
        switch (templateName) {
            case "entity.ftl":
                return className + ".java";
            case "controller.ftl":
                return className + "Controller.java";
            case "service.ftl":
                return className + "Service.java";
            case "mapper.ftl":
                return className + "Mapper.java";
            case "mapperXml.ftl":
                return className + "Mapper.xml";
            default:
                return className + ".java";
        }
    }

    /**
     * 构建文件路径
     */
    private String buildFilePath(String templateName, Map<String, Object> data, String packageName) {
        String basePath = packageName.replace('.', '/');
        String fileName = buildFileName(templateName, data);
        
        switch (templateName) {
            case "entity.ftl":
                return basePath + "/entity/" + fileName;
            case "controller.ftl":
                return basePath + "/controller/" + fileName;
            case "service.ftl":
                return basePath + "/service/" + fileName;
            case "mapper.ftl":
                return basePath + "/mapper/" + fileName;
            case "mapperXml.ftl":
                return basePath + "/mapper/" + fileName;
            default:
                return basePath + "/" + fileName;
        }
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String templateName) {
        if (templateName.contains("xml") || templateName.endsWith(".xml")) {
            return "xml";
        }
        return "java";
    }
    
    /**
     * 转换表名为类名
     */
    private String convertToClassName(String tableName) {
        if (tableName == null) return null;
        
        // 移除表前缀
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
     * 转换类名为字段名
     */
    private String convertToFieldName(String className) {
        if (className == null || className.isEmpty()) return className;
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }
}