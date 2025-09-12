package ${packageName}.service;

import ${packageName}.entity.${className};
import ${packageName}.mapper.${className}Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${tableComment!} Service
 * 
 * @author Code Generator
 * @date ${.now?string("yyyy-MM-dd HH:mm:ss")}
 */
@Service
public class ${className}Service {

    @Autowired
    private ${className}Mapper ${classNameLowerFirst}Mapper;

    /**
     * 查询所有${tableComment!}
     */
    public List<${className}> findAll() {
        return ${classNameLowerFirst}Mapper.selectAll();
    }

    /**
     * 根据ID查询${tableComment!}
     */
    public ${className} findById(${primaryKey.javaType} id) {
        return ${classNameLowerFirst}Mapper.selectById(id);
    }

    /**
     * 新增${tableComment!}
     */
    public int save(${className} ${classNameLowerFirst}) {
        return ${classNameLowerFirst}Mapper.insert(${classNameLowerFirst});
    }

    /**
     * 更新${tableComment!}
     */
    public int update(${className} ${classNameLowerFirst}) {
        return ${classNameLowerFirst}Mapper.update(${classNameLowerFirst});
    }

    /**
     * 根据ID删除${tableComment!}
     */
    public int deleteById(${primaryKey.javaType} id) {
        return ${classNameLowerFirst}Mapper.deleteById(id);
    }

    /**
     * 分页查询${tableComment!}
     */
    public List<${className}> findByPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return ${classNameLowerFirst}Mapper.selectByPage(offset, pageSize);
    }

    /**
     * 根据条件查询${tableComment!}
     */
    public List<${className}> findByCondition(${className} condition) {
        return ${classNameLowerFirst}Mapper.selectByCondition(condition);
    }

    /**
     * 统计总数
     */
    public long count() {
        return ${classNameLowerFirst}Mapper.count();
    }

    /**
     * 根据条件统计数量
     */
    public long countByCondition(${className} condition) {
        return ${classNameLowerFirst}Mapper.countByCondition(condition);
    }
}