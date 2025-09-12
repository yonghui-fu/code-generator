package com.hui.codegen.web.dto;

import java.util.List;

/**
 * 代码预览结果
 */
public class CodePreviewResult {
    private boolean success;
    private String message;
    private List<PreviewFileInfo> files;

    public CodePreviewResult() {
    }

    public CodePreviewResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public CodePreviewResult(boolean success, String message, List<PreviewFileInfo> files) {
        this.success = success;
        this.message = message;
        this.files = files;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PreviewFileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<PreviewFileInfo> files) {
        this.files = files;
    }
}