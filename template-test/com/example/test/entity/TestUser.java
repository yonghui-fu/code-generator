package com.example.test.entity;


/**
 * 测试用户表
 * 对应数据库表: test_user
 * 
 * @author Code Generator
 * @date 2025-09-12 16:53:27
 */
public class TestUser {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;


    public TestUser() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "TestUser{" +
                "id=" + id +
                "username=" + username +
                "email=" + email +                '}';
    }
}