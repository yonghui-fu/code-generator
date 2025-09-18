package com.hui.codegen.web.dto;

/**
 * 模板信息
 */
public class TemplateInfo {
    private String name;
    private String displayName;
    private String description;
    private String templateType;
    private String groupId;
    private boolean enabled;
    private String fileNamePattern; // 生成文件名的模板模式

    public TemplateInfo() {
    }

    public TemplateInfo(String name, String displayName, String description, String templateType, String groupId, boolean enabled) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.templateType = templateType;
        this.groupId = groupId;
        this.enabled = enabled;
    }

    public TemplateInfo(String name, String displayName, String description, String templateType, String groupId, boolean enabled, String fileNamePattern) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.templateType = templateType;
        this.groupId = groupId;
        this.enabled = enabled;
        this.fileNamePattern = fileNamePattern;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFileNamePattern() {
        return fileNamePattern;
    }

    public void setFileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
    }
}