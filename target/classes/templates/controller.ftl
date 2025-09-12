package ${packageName}.controller;

import ${packageName}.entity.${className};
import ${packageName}.service.${className}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ${tableComment!} Controller
 * 
 * @author Code Generator
 * @date ${.now?string("yyyy-MM-dd HH:mm:ss")}
 */
@RestController
@RequestMapping("/${classNameLowerFirst}")
public class ${className}Controller {

    @Autowired
    private ${className}Service ${classNameLowerFirst}Service;

    /**
     * 查询所有${tableComment!}
     */
    @GetMapping("/list")
    public List<${className}> list() {
        return ${classNameLowerFirst}Service.findAll();
    }

    /**
     * 根据ID查询${tableComment!}
     */
    @GetMapping("/{id}")
    public ${className} getById(@PathVariable ${primaryKey.javaType} id) {
        return ${classNameLowerFirst}Service.findById(id);
    }

    /**
     * 新增${tableComment!}
     */
    @PostMapping
    public int save(@RequestBody ${className} ${classNameLowerFirst}) {
        return ${classNameLowerFirst}Service.save(${classNameLowerFirst});
    }

    /**
     * 更新${tableComment!}
     */
    @PutMapping
    public int update(@RequestBody ${className} ${classNameLowerFirst}) {
        return ${classNameLowerFirst}Service.update(${classNameLowerFirst});
    }

    /**
     * 根据ID删除${tableComment!}
     */
    @DeleteMapping("/{id}")
    public int deleteById(@PathVariable ${primaryKey.javaType} id) {
        return ${classNameLowerFirst}Service.deleteById(id);
    }

    /**
     * 分页查询${tableComment!}
     */
    @GetMapping("/page")
    public List<${className}> page(@RequestParam(defaultValue = "1") int pageNum,
                                   @RequestParam(defaultValue = "10") int pageSize) {
        return ${classNameLowerFirst}Service.findByPage(pageNum, pageSize);
    }
}