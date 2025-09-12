package ${packageName}.mapper;

import ${packageName}.entity.${className};
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ${tableComment!} Mapper
 * 
 * @author Code Generator
 * @date ${.now?string("yyyy-MM-dd HH:mm:ss")}
 */
public interface ${className}Mapper {

    /**
     * 查询所有${tableComment!}
     */
    List<${className}> selectAll();

    /**
     * 根据ID查询${tableComment!}
     */
    ${className} selectById(${primaryKey.javaType} id);

    /**
     * 新增${tableComment!}
     */
    int insert(${className} ${classNameLowerFirst});

    /**
     * 更新${tableComment!}
     */
    int update(${className} ${classNameLowerFirst});

    /**
     * 根据ID删除${tableComment!}
     */
    int deleteById(${primaryKey.javaType} id);

    /**
     * 分页查询${tableComment!}
     */
    List<${className}> selectByPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 根据条件查询${tableComment!}
     */
    List<${className}> selectByCondition(${className} condition);

    /**
     * 统计总数
     */
    long count();

    /**
     * 根据条件统计数量
     */
    long countByCondition(${className} condition);
}