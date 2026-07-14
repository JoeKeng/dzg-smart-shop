# 「店掌柜」智慧零售管理系统

「店掌柜」智慧零售管理系统是基于 Spring Cloud、Spring Boot、MyBatis-Plus、Nacos、Dubbo、Sa-Token 等技术栈构建的智慧零售微服务后台。

## 项目说明

- 技术标识统一使用 `dzg`。
- Java 根包名统一为 `com.dzg`。
- Maven 模块和服务名统一使用 `dzg-*`。
- 系统中文名统一为「店掌柜」智慧零售管理系统。

## 模块结构

- `dzg-auth`：认证中心。
- `dzg-gateway` / `dzg-gateway-mvc`：网关服务。
- `dzg-api`：远程接口定义。
- `dzg-common`：公共能力模块。
- `dzg-modules`：业务服务模块。
- `dzg-visual`：监控、Nacos、任务调度等可视化服务。
- `dzg-example`：示例模块。

## 本地开发

1. 使用 JDK 17。
2. 在 IDEA 中打开根目录并重新加载 Maven 项目。
3. 使用项目内 Maven 仓库验证：

```powershell
mvn "-Dmaven.repo.local=D:/Code/shixun/RuoYi-Cloud-Plus/.m2/repository" -DskipTests compile
```

## 来源说明

本项目基于 Dromara RuoYi-Cloud-Plus 进行二次开发和品牌化改造，保留原项目许可证要求。
