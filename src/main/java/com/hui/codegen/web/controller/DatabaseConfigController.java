package com.hui.codegen.web.controller;

import com.hui.codegen.model.DatabaseConfig;
import com.hui.codegen.service.DatabaseConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据库配置控制器
 */
@Controller
@RequestMapping("/database-config")
public class DatabaseConfigController {

    @Autowired
    private DatabaseConfigService databaseConfigService;

    /**
     * 数据库配置页面
     */
    @GetMapping
    public String index(Model model) {
        List<DatabaseConfig> configs = databaseConfigService.getAllConfigs();
        model.addAttribute("configs", configs);
        return "database-config";
    }

    /**
     * 保存数据库配置
     */
    @PostMapping("/save")
    @ResponseBody
    public String saveConfig(@RequestBody Map<String, Object> configMap) {
        try {
            System.out.println("收到保存配置请求: " + configMap);
            
            // 将Map转换为DatabaseConfig对象
            DatabaseConfig config = new DatabaseConfig();
            config.setId((String) configMap.get("id"));
            config.setName((String) configMap.get("name"));
            config.setHost((String) configMap.get("host"));
            config.setPort(String.valueOf(configMap.get("port")));
            config.setDatabase((String) configMap.get("database"));
            config.setUsername((String) configMap.get("username"));
            config.setPassword((String) configMap.get("password"));
            config.setCharset((String) configMap.get("charset"));
            config.setEnabled(true);
            
            System.out.println("转换后的配置对象: " + config.getName() + ", " + config.getHost() + ":" + config.getPort());
            
            String result = databaseConfigService.saveConfig(config);
            System.out.println("保存结果: " + result);
            
            return result;
        } catch (Exception e) {
            System.err.println("保存数据库配置失败: " + e.getMessage());
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    /**
     * 获取数据库配置
     */
    @GetMapping("/{configId}")
    @ResponseBody
    public DatabaseConfig getConfig(@PathVariable String configId) {
        return databaseConfigService.getConfigById(configId);
    }

    /**
     * 删除数据库配置
     */
    @DeleteMapping("/{configId}")
    @ResponseBody
    public String deleteConfig(@PathVariable String configId) {
        return databaseConfigService.deleteConfig(configId);
    }

    /**
     * 测试数据库连接
     */
    @PostMapping("/test/{configId}")
    @ResponseBody
    public String testConnection(@PathVariable String configId) {
        return databaseConfigService.testConnection(configId);
    }
}