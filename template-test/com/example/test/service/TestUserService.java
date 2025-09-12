package com.example.test.service;

import com.example.test.entity.TestUser;
import com.example.test.mapper.TestUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测试用户表 Service
 * 
 * @author Code Generator
 * @date 2025-09-12 16:53:27
 */
@Service
public class TestUserService {

    @Autowired
    private TestUserMapper testUserMapper;

    /**
     * 查询所有测试用户表
     */
    public List<TestUser> findAll() {
        return testUserMapper.selectAll();
    }

    /**
     * 根据ID查询测试用户表
     */
    public TestUser findById(Long id) {
        return testUserMapper.selectById(id);
    }

    /**
     * 新增测试用户表
     */
    public int save(TestUser testUser) {
        return testUserMapper.insert(testUser);
    }

    /**
     * 更新测试用户表
     */
    public int update(TestUser testUser) {
        return testUserMapper.update(testUser);
    }

    /**
     * 根据ID删除测试用户表
     */
    public int deleteById(Long id) {
        return testUserMapper.deleteById(id);
    }

    /**
     * 分页查询测试用户表
     */
    public List<TestUser> findByPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return testUserMapper.selectByPage(offset, pageSize);
    }

    /**
     * 根据条件查询测试用户表
     */
    public List<TestUser> findByCondition(TestUser condition) {
        return testUserMapper.selectByCondition(condition);
    }

    /**
     * 统计总数
     */
    public long count() {
        return testUserMapper.count();
    }

    /**
     * 根据条件统计数量
     */
    public long countByCondition(TestUser condition) {
        return testUserMapper.countByCondition(condition);
    }
}