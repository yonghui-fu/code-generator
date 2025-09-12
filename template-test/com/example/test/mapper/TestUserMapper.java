package com.example.test.mapper;

import com.example.test.entity.TestUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试用户表 Mapper
 * 
 * @author Code Generator
 * @date 2025-09-12 16:53:27
 */
public interface TestUserMapper {

    /**
     * 查询所有测试用户表
     */
    List<TestUser> selectAll();

    /**
     * 根据ID查询测试用户表
     */
    TestUser selectById(Long id);

    /**
     * 新增测试用户表
     */
    int insert(TestUser testUser);

    /**
     * 更新测试用户表
     */
    int update(TestUser testUser);

    /**
     * 根据ID删除测试用户表
     */
    int deleteById(Long id);

    /**
     * 分页查询测试用户表
     */
    List<TestUser> selectByPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 根据条件查询测试用户表
     */
    List<TestUser> selectByCondition(TestUser condition);

    /**
     * 统计总数
     */
    long count();

    /**
     * 根据条件统计数量
     */
    long countByCondition(TestUser condition);
}