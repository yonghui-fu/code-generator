package com.hui.codegen.web.controller;

import com.hui.codegen.service.TemplateConfigService;
import com.hui.codegen.web.dto.TemplateInfo;
import com.hui.codegen.web.dto.TemplateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
     * 重置模板为默认内容
     */
    @PostMapping("/reset/{templateName}")
    @ResponseBody
    public String resetTemplate(@PathVariable String templateName) {
        try {
            return templateConfigService.resetTemplate(templateName);
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
}