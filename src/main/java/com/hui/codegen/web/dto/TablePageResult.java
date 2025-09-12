package com.hui.codegen.web.dto;

import java.util.List;

/**
 * 表分页结果
 */
public class TablePageResult {
    private List<TableInfo> tables;
    private int totalPages;
    private long total;
    private int currentPage;
    private int pageSize;

    public TablePageResult() {
    }

    public TablePageResult(List<TableInfo> tables, int totalPages, long total, int currentPage, int pageSize) {
        this.tables = tables;
        this.totalPages = totalPages;
        this.total = total;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public List<TableInfo> getTables() {
        return tables;
    }

    public void setTables(List<TableInfo> tables) {
        this.tables = tables;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}