package com.hui.codegen.service;

import com.hui.codegen.model.DatabaseConfig;
import com.hui.codegen.web.dto.CodeGenRequest;
import com.hui.codegen.web.dto.PreviewFileInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        
        log.info("生成预览请求: {}", request);
        
        // 根据请求中的配置ID获取数据库配置
        DatabaseConfig config = null;
        if (request.getConfigId() != null && !request.getConfigId().isEmpty()) {
            config = databaseConfigService.getConfigById(request.getConfigId());
        }
        
        // 如果没有配置ID或找不到配置，则使用默认的SQLite配置
        if (config == null) {
            config = new DatabaseConfig();
            config.setDbType("sqlite");
            config.setDatabase("code-generator.db");
        }

        for (String tableName : request.getSelectedTables()) {
            log.info("处理表: {}", tableName);
            // 获取表信息
            Map<String, Object> tableData = buildTableData(config, tableName, request.getPackageName());
            log.info("表数据: {}", tableData);
            
            for (String templateName : request.getSelectedTemplates()) {
                log.info("处理模板: {}", templateName);
                String content = generateFileContent(templateName, tableData);
                String fileName = templateName;//buildFileName(templateName, tableData);
                String fileType = getFileType(templateName);
                
                files.add(new PreviewFileInfo(fileName, fileType, content));
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
        
        log.info("表基本信息 - 表名: {}, 类名: {}, 包名: {}", tableName, data.get("className"), packageName);
        
        // 获取表详细信息
        Map<String, Object> tableDetail = databaseTableService.getTableDetail(config, tableName);
        data.put("tableComment", tableDetail.getOrDefault("tableComment", ""));
        
        // 获取列信息
        List<Map<String, Object>> columns = databaseTableService.getTableColumns(config, tableName);
        log.info("获取到的列信息: {}", columns);
        log.info("列数量: {}", columns != null ? columns.size() : 0);
        data.put("columns", columns);
        
        // 设置hasDate、hasBigDecimal等变量
        boolean hasDate = false;
        boolean hasBigDecimal = false;
        boolean hasTime = false;
        
        if (columns != null) {
            for (Map<String, Object> column : columns) {
                String javaType = (String) column.get("javaType");
                if ("Date".equals(javaType)) {
                    hasDate = true;
                } else if ("BigDecimal".equals(javaType)) {
                    hasBigDecimal = true;
                } else if ("Time".equals(javaType)) {
                    hasTime = true;
                }
            }
        }
        
        data.put("hasDate", hasDate);
        data.put("hasBigDecimal", hasBigDecimal);
        data.put("hasTime", hasTime);
        
        // 查找主键
        Map<String, Object> primaryKey = null;
        if (columns != null) {
            for (Map<String, Object> column : columns) {
                if (Boolean.TRUE.equals(column.get("primaryKey"))) {
                    primaryKey = column;
                    break;
                }
            }
        }
        data.put("primaryKey", primaryKey);
        
        log.info("完整表数据: {}", data);
        return data;
    }

    /**
     * 生成文件内容
     */
    private String generateFileContent(String templateName, Map<String, Object> data) throws Exception {
        String templateContent = templateConfigService.getTemplateContent(templateName);
        
        log.info("模板名称: {}", templateName);
        log.info("模板内容: {}", templateContent);
        
        if (templateContent == null || templateContent.trim().isEmpty()) {
            return "// 模板内容为空\n";
        }
        
        // 使用FreeMarker处理模板
        return processFreeMarkerTemplate(templateName, templateContent, data);
    }

    /**
     * 使用FreeMarker处理模板
     */
    private String processFreeMarkerTemplate(String templateName, String templateContent, Map<String, Object> data) throws Exception {
        log.info("处理模板: {}", templateName);
        log.info("模板数据: {}", data);
        Template template = new Template(templateName, templateContent, freemarkerConfig);
        StringWriter writer = new StringWriter();
        template.process(data, writer);
        
        String result = writer.toString();
        log.info("生成结果: {}", result);
        return result;
    }

    /**
     * 构建文件名
     */
    private String buildFileName(String templateName, Map<String, Object> data) {
        String className = (String) data.get("className");

        // 根据模板类型确定文件扩展名
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
     * 获取文件类型
     */
    private String getFileType(String templateName) {
        if (templateName.contains("xml") || templateName.endsWith(".xml") || templateName.contains("mapperXml")) {
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