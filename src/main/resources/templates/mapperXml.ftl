<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${packageName}.mapper.${className}Mapper">

    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="${packageName}.entity.${className}">
<#list columns as column>
        <#if column.primaryKey>
        <id column="${column.columnName}" property="${column.javaFieldName}" jdbcType="VARCHAR" />
        <#else>
        <result column="${column.columnName}" property="${column.javaFieldName}" jdbcType="VARCHAR" />
        </#if>
</#list>
    </resultMap>

    <!-- 基础字段列表 -->
    <sql id="Base_Column_List">
        <#list columns as column>${column.columnName}<#if column_has_next>, </#if></#list>
    </sql>

    <!-- 查询所有${tableComment!} -->
    <select id="selectAll" resultMap="BaseResultMap">
        SELECT
        <include refid="Base_Column_List" />
        FROM ${tableName}
    </select>

    <!-- 根据ID查询${tableComment!} -->
    <select id="selectById" parameterType="${primaryKey.javaType?lower_case}" resultMap="BaseResultMap">
        SELECT
        <include refid="Base_Column_List" />
        FROM ${tableName}
        WHERE ${primaryKey.columnName} = <#noparse>#{id}</#noparse>
    </select>

    <!-- 新增${tableComment!} -->
    <insert id="insert" parameterType="${packageName}.entity.${className}"<#if primaryKey.autoIncrement> useGeneratedKeys="true" keyProperty="${primaryKey.javaFieldName}"</#if>>
        INSERT INTO ${tableName}
        (
        <#list columns as column>
            <#if !column.autoIncrement>
            ${column.columnName}<#if column_has_next>,</#if>
            </#if>
        </#list>
        )
        VALUES
        (
        <#list columns as column>
            <#if !column.autoIncrement>
            <#noparse>#{</#noparse>${column.javaFieldName}<#noparse>}</#noparse><#if column_has_next>,</#if>
            </#if>
        </#list>
        )
    </insert>

    <!-- 更新${tableComment!} -->
    <update id="update" parameterType="${packageName}.entity.${className}">
        UPDATE ${tableName}
        SET
<#assign first = true>
<#list columns as column>
    <#if !column.primaryKey>
        <#if !first>,</#if>
        ${column.columnName} = <#noparse>#{</#noparse>${column.javaFieldName}<#noparse>}</#noparse>
        <#assign first = false>
    </#if>
</#list>
        WHERE ${primaryKey.columnName} = <#noparse>#{</#noparse>${primaryKey.javaFieldName}<#noparse>}</#noparse>
    </update>

    <!-- 根据ID删除${tableComment!} -->
    <delete id="deleteById" parameterType="${primaryKey.javaType?lower_case}">
        DELETE FROM ${tableName}
        WHERE ${primaryKey.columnName} = <#noparse>#{id}</#noparse>
    </delete>

    <!-- 分页查询${tableComment!} -->
    <select id="selectByPage" resultMap="BaseResultMap">
        SELECT
        <include refid="Base_Column_List" />
        FROM ${tableName}
        LIMIT <#noparse>#{offset}</#noparse>, <#noparse>#{pageSize}</#noparse>
    </select>

    <!-- 根据条件查询${tableComment!} -->
    <select id="selectByCondition" parameterType="${packageName}.entity.${className}" resultMap="BaseResultMap">
        SELECT
        <include refid="Base_Column_List" />
        FROM ${tableName}
        <!-- 条件查询需要根据具体需求手动修改 -->
    </select>

    <!-- 统计总数 -->
    <select id="count" resultType="long">
        SELECT COUNT(*) FROM ${tableName}
    </select>

    <!-- 根据条件统计数量 -->
    <select id="countByCondition" parameterType="${packageName}.entity.${className}" resultType="long">
        SELECT COUNT(*) FROM ${tableName}
        <!-- 条件统计需要根据具体需求手动修改 -->
    </select>

</mapper>