package com.hui.codegen.web.dto;

/**
 * 预览文件信息
 */
public class PreviewFileInfo {
    private String fileName;
    private String filePath;
    private String fileType;
    private String content;

    public PreviewFileInfo() {
    }

    public PreviewFileInfo(String fileName, String filePath, String fileType, String content) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}