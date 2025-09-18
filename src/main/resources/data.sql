-- 模板组表
CREATE TABLE IF NOT EXISTS template_group (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT 1,
    sort_order INTEGER DEFAULT 0
);

-- 模板信息表
CREATE TABLE IF NOT EXISTS template_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    template_type VARCHAR(20) DEFAULT 'java',
    enabled BOOLEAN NOT NULL DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    group_id VARCHAR(50),
    file_name_pattern VARCHAR(200), -- 生成文件名的模板模式
    FOREIGN KEY (group_id) REFERENCES template_group(id)
);

-- 插入默认模板组数据（如果不存在）
INSERT OR IGNORE INTO template_group (id, name, description, enabled, sort_order) VALUES
('default', '默认组', '系统默认的模板组', 1, 0),
('java', 'Java类', 'Java相关的模板', 1, 1),
('config', '配置文件', '配置文件相关的模板', 1, 2);

-- 插入默认模板数据（如果不存在）
INSERT OR IGNORE INTO template_info (name, display_name, description, template_type, enabled, sort_order, group_id, file_name_pattern) VALUES
('entity.ftl', '实体类模板', '生成JPA实体类', 'java', 1, 0, 'java', '${className}.java'),
('controller.ftl', '控制器模板', '生成Spring MVC控制器', 'java', 1, 1, 'java', '${className}Controller.java'),
('service.ftl', '服务层模板', '生成业务服务层', 'java', 1, 2, 'java', '${className}Service.java'),
('mapper.ftl', 'Mapper接口模板', '生成MyBatis Mapper接口', 'java', 1, 3, 'java', '${className}Mapper.java'),
('mapperXml.ftl', 'Mapper XML模板', '生成MyBatis XML映射文件', 'xml', 1, 0, 'config', '${className}Mapper.xml');