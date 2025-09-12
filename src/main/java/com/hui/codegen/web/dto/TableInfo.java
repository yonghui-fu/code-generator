package com.hui.codegen.web.dto;

/**
 * 表信息DTO
 */
public class TableInfo {
    private String tableName;
    private String className;
    private String tableComment;
    private Integer columnCount;

    public TableInfo() {
    }

    public TableInfo(String tableName, String className) {
        this.tableName = tableName;
        this.className = className;
    }

    public TableInfo(String tableName, String className, String tableComment, Integer columnCount) {
        this.tableName = tableName;
        this.className = className;
        this.tableComment = tableComment;
        this.columnCount = columnCount;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }
}