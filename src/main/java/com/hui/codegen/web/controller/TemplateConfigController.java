package com.hui.codegen.web.controller;

import com.hui.codegen.service.TemplateConfigService;
import com.hui.codegen.web.dto.TemplateInfo;
import com.hui.codegen.web.dto.TemplateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 模板配置控制器
 */
@Controller
@RequestMapping("/template-config")
public class TemplateConfigController {

    @Autowired
    private TemplateConfigService templateConfigService;

    /**
     * 模板配置页面
     */
    @GetMapping
    public String index(Model model) {
        List<TemplateGroup> templateGroups = templateConfigService.getAllTemplateGroups();
        model.addAttribute("templateGroups", templateGroups);
        return "template-config";
    }

    /**
     * 获取模板内容
     */
    @GetMapping("/content/{templateName}")
    @ResponseBody
    public String getTemplateContent(@PathVariable String templateName) {
        try {
            return templateConfigService.getTemplateContent(templateName);
        } catch (Exception e) {
            return "获取模板内容失败: " + e.getMessage();
        }
    }

    /**
     * 保存模板内容
     */
    @PostMapping("/save")
    @ResponseBody
    public String saveTemplate(@RequestParam String templateName, @RequestParam String content) {
        try {
            return templateConfigService.saveTemplateContent(templateName, content);
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 添加新模板
     */
    @PostMapping("/add")
    @ResponseBody
    public String addTemplate(@RequestBody TemplateInfo templateInfo) {
        try {
            return templateConfigService.addTemplate(templateInfo);
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 删除模板
     */
    @DeleteMapping("/delete/{templateName}")
    @ResponseBody
    public String deleteTemplate(@PathVariable String templateName) {
        try {
            return templateConfigService.deleteTemplate(templateName);
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 获取所有模板组
     */
    @GetMapping("/groups")
    @ResponseBody
    public List<TemplateGroup> getAllTemplateGroups() {
        return templateConfigService.getAllTemplateGroups();
    }
    
    /**
     * 添加新模板组
     */
    @PostMapping("/groups/add")
    @ResponseBody
    public String addTemplateGroup(@RequestBody TemplateGroup templateGroup) {
        try {
            return templateConfigService.addTemplateGroup(templateGroup);
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 更新模板组
     */
    @PostMapping("/groups/update")
    @ResponseBody
    public String updateTemplateGroup(@RequestBody TemplateGroup templateGroup) {
        try {
            return templateConfigService.updateTemplateGroup(templateGroup);
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 删除模板组
     */
    @DeleteMapping("/groups/delete/{groupId}")
    @ResponseBody
    public String deleteTemplateGroup(@PathVariable String groupId) {
        try {
            return templateConfigService.deleteTemplateGroup(groupId);
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 更新模板的组关联
     */
    @PostMapping("/update-group")
    @ResponseBody
    public String updateTemplateGroup(@RequestParam String templateName, @RequestParam String groupId) {
        try {
            // 这里可以实现更新模板组关联的逻辑
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 导出所有模板
     */
    @GetMapping("/export")
    public void exportTemplates(HttpServletResponse response) {
        try {
            templateConfigService.exportTemplates(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 导出指定模板组的模板
     */
    @GetMapping("/export/{groupId}")
    public void exportTemplatesByGroup(@PathVariable String groupId, HttpServletResponse response) {
        try {
            templateConfigService.exportTemplatesByGroup(response, groupId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 导入模板
     */
    @PostMapping("/import")
    @ResponseBody
    public String importTemplates(@RequestParam("file") MultipartFile file) {
        try {
            return templateConfigService.importTemplates(file);
        } catch (Exception e) {
            return "导入失败: " + e.getMessage();
        }
    }
    
    /**
     * 导入模板到指定模板组
     */
    @PostMapping("/import/{groupId}")
    @ResponseBody
    public String importTemplatesToGroup(@PathVariable String groupId, @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("收到导入模板请求，目标组ID: " + groupId);
            // 检查文件是否为空
            if (file.isEmpty()) {
                System.err.println("上传的文件为空");
                return "导入失败: 上传的文件为空";
            }
            
            // 检查groupId是否为空
            if (groupId == null || groupId.trim().isEmpty()) {
                System.err.println("目标组ID为空");
                return "导入失败: 目标组ID不能为空";
            }
            
            System.out.println("文件大小: " + file.getSize() + " 字节");
            System.out.println("文件名称: " + file.getOriginalFilename());
            
            String result = templateConfigService.importTemplatesToGroup(file, groupId);
            System.out.println("导入模板完成，结果: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("导入模板到组时发生异常: " + e.getMessage());
            e.printStackTrace();
            // 返回更详细的错误信息
            String errorMsg = "导入失败: " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += " (原因: " + e.getCause().getMessage() + ")";
            }
            return errorMsg;
        }
    }
}