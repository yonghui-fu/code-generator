package com.hui.codegen.web.dto;

import java.util.List;

/**
 * 代码生成请求
 */
public class CodeGenRequest {
    private String configId;
    private String packageName;
    private String outputPath;
    private List<String> selectedTables;
    private List<String> selectedTemplates;

    public CodeGenRequest() {
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public List<String> getSelectedTables() {
        return selectedTables;
    }

    public void setSelectedTables(List<String> selectedTables) {
        this.selectedTables = selectedTables;
    }

    public List<String> getSelectedTemplates() {
        return selectedTemplates;
    }

    public void setSelectedTemplates(List<String> selectedTemplates) {
        this.selectedTemplates = selectedTemplates;
    }
}