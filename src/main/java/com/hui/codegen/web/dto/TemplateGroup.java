package com.hui.codegen.web.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板组信息
 */
public class TemplateGroup {
    private String id;
    private String name;
    private String description;
    private boolean enabled;
    private List<TemplateInfo> templates;

    public TemplateGroup() {
        this.templates = new ArrayList<>();
    }

    public TemplateGroup(String id, String name, String description, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.templates = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<TemplateInfo> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateInfo> templates) {
        this.templates = templates;
    }

    public void addTemplate(TemplateInfo template) {
        if (this.templates == null) {
            this.templates = new ArrayList<>();
        }
        this.templates.add(template);
    }
}