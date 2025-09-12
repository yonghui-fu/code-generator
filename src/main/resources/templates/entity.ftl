package ${packageName}.entity;

<#if hasDate>
import java.util.Date;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>
<#if hasTime>
import java.sql.Time;
</#if>

/**
 * ${tableComment!}
 * 对应数据库表: ${tableName}
 * 
 * @author Code Generator
 * @date ${.now?string("yyyy-MM-dd HH:mm:ss")}
 */
public class ${className} {

<#list columns as column>
    /**
     * ${column.columnComment!}
     */
    private ${column.javaType} ${column.javaFieldName};

</#list>

    public ${className}() {}

<#list columns as column>
    public ${column.javaType} get${column.javaFieldName?cap_first}() {
        return ${column.javaFieldName};
    }

    public void set${column.javaFieldName?cap_first}(${column.javaType} ${column.javaFieldName}) {
        this.${column.javaFieldName} = ${column.javaFieldName};
    }

</#list>
    @Override
    public String toString() {
        return "${className}{" +
<#list columns as column>
                "${column.javaFieldName}=" + ${column.javaFieldName} +<#if column_has_next>
<#else>
                '}';
</#if>
</#list>
    }
}