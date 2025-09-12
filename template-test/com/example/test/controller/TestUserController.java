package com.example.test.controller;

import com.example.test.entity.TestUser;
import com.example.test.service.TestUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试用户表 Controller
 * 
 * @author Code Generator
 * @date 2025-09-12 16:53:27
 */
@RestController
@RequestMapping("/testUser")
public class TestUserController {

    @Autowired
    private TestUserService testUserService;

    /**
     * 查询所有测试用户表
     */
    @GetMapping("/list")
    public List<TestUser> list() {
        return testUserService.findAll();
    }

    /**
     * 根据ID查询测试用户表
     */
    @GetMapping("/{id}")
    public TestUser getById(@PathVariable Long id) {
        return testUserService.findById(id);
    }

    /**
     * 新增测试用户表
     */
    @PostMapping
    public int save(@RequestBody TestUser testUser) {
        return testUserService.save(testUser);
    }

    /**
     * 更新测试用户表
     */
    @PutMapping
    public int update(@RequestBody TestUser testUser) {
        return testUserService.update(testUser);
    }

    /**
     * 根据ID删除测试用户表
     */
    @DeleteMapping("/{id}")
    public int deleteById(@PathVariable Long id) {
        return testUserService.deleteById(id);
    }

    /**
     * 分页查询测试用户表
     */
    @GetMapping("/page")
    public List<TestUser> page(@RequestParam(defaultValue = "1") int pageNum,
                                   @RequestParam(defaultValue = "10") int pageSize) {
        return testUserService.findByPage(pageNum, pageSize);
    }
}