package com.hui.codegen.web.controller;

import com.hui.codegen.model.DatabaseConfig;
import com.hui.codegen.service.DatabaseConfigService;
import com.hui.codegen.service.DatabaseTableService;
import com.hui.codegen.service.TemplateConfigService;
import com.hui.codegen.service.CodeGenerationService;
import com.hui.codegen.web.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成控制器
 */
@Controller
@RequestMapping("/code-gen")
public class CodeGenController {

    @Autowired
    private DatabaseConfigService databaseConfigService;
    
    @Autowired
    private DatabaseTableService databaseTableService;
    
    @Autowired
    private TemplateConfigService templateConfigService;
    
    @Autowired
    private CodeGenerationService codeGenerationService;

    /**
     * 代码生成页面
     */
    @GetMapping
    public String index(Model model) {
        // 获取所有数据库配置
        List<DatabaseConfig> configs = databaseConfigService.getAllConfigs();
        model.addAttribute("configs", configs);
        return "code-gen";
    }

    /**
     * 获取所有表名列表（不分页，用于快速返回）
     */
    @GetMapping("/tables/{configId}/all-names")
    @ResponseBody
    public List<SimpleTableInfo> getAllTableNames(@PathVariable String configId) {
        try {
            // 根据配置ID获取数据库配置
            DatabaseConfig config = databaseConfigService.getConfigById(configId);
            if (config == null) {
                System.err.println("未找到ID为 " + configId + " 的数据库配置");
                return new ArrayList<>();
            }
            return databaseTableService.getAllTableNames(config);
        } catch (Exception e) {
            System.err.println("获取所有表名列表失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 获取表列表（分页，只包含表名，用于快速返回）
     */
    @GetMapping("/tables/{configId}/names-only")
    @ResponseBody
    public SimpleTablePageResult getTableNamesByPage(@PathVariable String configId, 
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        try {
            // 根据配置ID获取数据库配置
            DatabaseConfig config = databaseConfigService.getConfigById(configId);
            if (config == null) {
                System.err.println("未找到ID为 " + configId + " 的数据库配置");
                // 返回空结果
                return new SimpleTablePageResult(new ArrayList<>(), 0, 0, page, size);
            }
            return databaseTableService.getTableNamesByPage(config, page, size);
        } catch (Exception e) {
            System.err.println("获取表名列表失败: " + e.getMessage());
            e.printStackTrace();
            // 返回空结果
            return new SimpleTablePageResult(new ArrayList<>(), 0, 0, page, size);
        }
    }

    /**
     * 获取表列表（分页）
     */
    @GetMapping("/tables/{configId}/page")
    @ResponseBody
    public TablePageResult getTablesByPage(@PathVariable String configId, 
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        try {
            // 根据配置ID获取数据库配置
            DatabaseConfig config = databaseConfigService.getConfigById(configId);
            if (config == null) {
                System.err.println("未找到ID为 " + configId + " 的数据库配置");
                // 返回空结果
                return new TablePageResult(new ArrayList<>(), 0, 0, page, size);
            }
            return databaseTableService.getTablesByPage(config, page, size);
        } catch (Exception e) {
            System.err.println("获取表列表失败: " + e.getMessage());
            e.printStackTrace();
            // 返回空结果
            return new TablePageResult(new ArrayList<>(), 0, 0, page, size);
        }
    }

    /**
     * 获取简单表列表（用于全选）
     */
    @GetMapping("/tables/{configId}/simple")
    @ResponseBody
    public List<TableInfo> getSimpleTables(@PathVariable String configId) {
        try {
            // 根据配置ID获取数据库配置
            DatabaseConfig config = databaseConfigService.getConfigById(configId);
            if (config == null) {
                System.err.println("未找到ID为 " + configId + " 的数据库配置");
                return new ArrayList<>();
            }
            return databaseTableService.getSimpleTables(config);
        } catch (Exception e) {
            System.err.println("获取简单表列表失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 获取表详细信息
     */
    @GetMapping("/table-detail/{configId}/{tableName}")
    @ResponseBody
    public Map<String, Object> getTableDetail(@PathVariable String configId, @PathVariable String tableName) {
        try {
            // 根据配置ID获取数据库配置
            DatabaseConfig config = databaseConfigService.getConfigById(configId);
            if (config == null) {
                System.err.println("未找到ID为 " + configId + " 的数据库配置");
                Map<String, Object> result = new HashMap<>();
                result.put("tableComment", "获取失败：未找到数据库配置");
                result.put("columnCount", 0);
                return result;
            }
            return databaseTableService.getTableDetail(config, tableName);
        } catch (Exception e) {
            System.err.println("获取表详细信息失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("tableComment", "获取失败: " + e.getMessage());
            result.put("columnCount", 0);
            return result;
        }
    }

    /**
     * 生成代码
     */
    @PostMapping("/generate")
    @ResponseBody
    public String generateCode(@RequestBody CodeGenRequest request) {
        try {
            System.out.println("生成代码请求: " + request.getPackageName());
            System.out.println("选中表: " + request.getSelectedTables());
            System.out.println("选中模板: " + request.getSelectedTemplates());
            
            // 验证请求参数
            if (request.getSelectedTables() == null || request.getSelectedTables().isEmpty()) {
                return "请选择要生成的表";
            }
            
            if (request.getSelectedTemplates() == null || request.getSelectedTemplates().isEmpty()) {
                return "请选择要生成的模板";
            }
            
            // 模拟代码生成（后续将由CodeGenerationService实现）
            return "代码生成成功！共生成 " + request.getSelectedTables().size() + " 个表的代码文件。";
        } catch (Exception e) {
            return "代码生成失败: " + e.getMessage();
        }
    }

    /**
     * 预览代码
     */
    @PostMapping("/preview")
    @ResponseBody
    public CodePreviewResult previewCode(@RequestBody CodeGenRequest request) {
        try {
            // 验证请求参数
            if (request.getSelectedTables() == null || request.getSelectedTables().isEmpty()) {
                return new CodePreviewResult(false, "请选择要生成的表");
            }
            
            if (request.getSelectedTemplates() == null || request.getSelectedTemplates().isEmpty()) {
                return new CodePreviewResult(false, "请选择要生成的模板");
            }
            
            // 使用代码生成服务生成预览
            List<PreviewFileInfo> files = codeGenerationService.generatePreview(request);
            return new CodePreviewResult(true, "预览生成成功", files);
        } catch (Exception e) {
            System.err.println("预览生成失败: " + e.getMessage());
            e.printStackTrace();
            return new CodePreviewResult(false, "预览生成失败: " + e.getMessage());
        }
    }
}