# 店掌柜智慧零售管理系统服务器初始化与 Nacos 配置流程

本文用于把当前项目部署到服务器环境时，按顺序配置 Nacos、MySQL、Redis 和项目配置中心。

参考入口：

- 官方初始化文档：https://plus-doc.dromara.org/#/ruoyi-cloud-plus/quickstart/init
- 当前项目 Nacos 配置模板：`script/config/nacos/`
- 当前项目 SQL 初始化脚本：`script/sql/`

## 1. 前提和假设

本流程按以下部署方式编写：

- Nacos 已部署在服务器上。
- MySQL 和 Redis 部署在同一台服务器上。
- 项目英文技术前缀统一为 `dzg`。
- 项目中文名为「店掌柜」智慧零售管理系统。
- Java 服务通过 Nacos 读取配置，不直接把数据库和 Redis 配置写死在每个服务里。

文档中使用以下占位符，实际操作时替换成真实值：

| 占位符 | 含义 | 示例 |
| --- | --- | --- |
| `SERVER_IP` | 服务器公网或内网 IP | `SERVER_IP` |
| `MYSQL_HOST` | MySQL 地址，和服务器同机时通常就是 `SERVER_IP` | `MYSQL_HOST` |
| `MYSQL_PORT` | MySQL 端口 | `3306` |
| `MYSQL_APP_USER` | 项目访问 MySQL 的账号 | `dzg_app` |
| `MYSQL_APP_PASSWORD` | 项目访问 MySQL 的密码 | 自行设置 |
| `NACOS_HOST` | Nacos 地址，通常就是 `SERVER_IP` | `NACOS_HOST` |
| `NACOS_PORT` | Nacos 端口 | `8848` |
| `NACOS_USERNAME` | Nacos 登录账号 | `nacos` |
| `NACOS_PASSWORD` | Nacos 登录密码 | 自行设置 |
| `REDIS_HOST` | Redis 地址，和服务器同机时通常就是 `SERVER_IP` | `REDIS_HOST` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | 自行设置 |

成功标准：

1. MySQL 中存在项目数据库和对应表。
2. Redis 可以连接，`PING` 返回 `PONG`。
3. Nacos 的 `dev` 命名空间中存在项目所需 Data ID。
4. 后端服务启动日志能连接 Nacos、MySQL、Redis。
5. Nacos 服务列表能看到 `dzg-gateway`、`dzg-auth`、`dzg-system` 等服务。

## 2. 密码隐藏方式

不要把 MySQL、Redis、服务器 SSH 密码直接提交到 Git。

本项目配置模板采用环境变量占位方式隐藏密码：

| 配置项 | Git 中写法 | 部署时真实值来源 |
| --- | --- | --- |
| MySQL 密码 | `${MYSQL_PASSWORD}` | 服务器环境变量 `MYSQL_PASSWORD` |
| Redis 密码 | `${REDIS_PASSWORD}` | 服务器环境变量 `REDIS_PASSWORD` |
| Nacos 数据库密码 | `${MYSQL_PASSWORD}` | Nacos 服务进程环境变量 `MYSQL_PASSWORD` |
| Nacos 登录账号 | `${env.NACOS_USERNAME}` | Maven / IDEA 环境变量 `NACOS_USERNAME` |
| Nacos 登录密码 | `${env.NACOS_PASSWORD}` | Maven / IDEA 环境变量 `NACOS_PASSWORD` |

Linux 服务器临时设置环境变量：

```bash
export MYSQL_PASSWORD='你的MySQL密码'
export REDIS_PASSWORD='你的Redis密码'
export NACOS_USERNAME='nacos'
export NACOS_PASSWORD='你的Nacos登录密码'
```

Docker Compose 推荐写在服务器本地 `.env` 文件里，并把 `.env` 加入 `.gitignore`，不要提交：

```env
MYSQL_PASSWORD=你的MySQL密码
REDIS_PASSWORD=你的Redis密码
NACOS_USERNAME=nacos
NACOS_PASSWORD=你的Nacos登录密码
```

然后在 `docker-compose.yml` 的服务里注入：

```yaml
environment:
  MYSQL_PASSWORD: ${MYSQL_PASSWORD}
  REDIS_PASSWORD: ${REDIS_PASSWORD}
  NACOS_USERNAME: ${NACOS_USERNAME}
  NACOS_PASSWORD: ${NACOS_PASSWORD}
```

如果直接在 Nacos 控制台里维护配置，也可以只在 Nacos 控制台填写真实密码，Git 仓库里的 `script/config/nacos/*.yml` 保持 `${MYSQL_PASSWORD}`、`${REDIS_PASSWORD}`。Nacos 控制台必须限制账号权限，不能公开访问。

如果密码已经提交或推送到 GitHub，即使后面删掉也要视为已经泄露，应立即修改 MySQL、Redis、服务器 SSH 密码。

## 3. 先理解这几个组件

MySQL：保存系统用户、菜单、租户、业务数据、定时任务、工作流等持久化数据。

Redis：保存缓存、登录状态、验证码、分布式锁等临时数据。Redis 还没部署完成时，可以先配置 MySQL 和 Nacos，但完整启动后端服务通常会失败。

Nacos：本项目把它当作“配置中心 + 服务注册中心”使用。每个服务启动时会先连接 Nacos，再从 Nacos 读取 `application-common.yml`、`datasource.yml` 和自己的配置文件。

命名空间：本项目默认用 Maven profile 决定环境。`dev` 环境读取 Nacos 的 `dev` 命名空间，`prod` 环境读取 `prod` 命名空间。

Data ID：Nacos 里的配置文件名，例如 `application-common.yml`、`datasource.yml`、`dzg-system.yml`。

## 4. 开放服务器端口

服务器安全组和防火墙至少需要确认以下端口：

| 组件 | 端口 | 说明 |
| --- | --- | --- |
| Nacos | `8848` | 本地开发机和后端服务需要访问 |
| MySQL | `3306` | 后端服务需要访问；如只允许内网访问更好 |
| Redis | `6379` | 后端服务需要访问；不要无密码暴露到公网 |
| Gateway | `8080` 或项目实际网关端口 | 前端访问后端入口 |

如果 MySQL、Redis、Nacos 和后端服务都在同一台服务器，可以优先使用内网地址或 `127.0.0.1`；如果后端在本地 IDEA 启动，就需要使用服务器 IP。

## 5. 创建项目数据库

推荐把数据库名从原始 `ry-*` 改成项目自己的 `dzg_*`。MySQL 数据库名建议使用下划线，不使用横线，避免 SQL 中每次都要加反引号。

推荐数据库映射：

| 原数据库 | 店掌柜数据库 | 用途 | 导入脚本 |
| --- | --- | --- | --- |
| `ry-cloud` | `dzg_cloud` | 系统、租户、菜单、用户、资源、代码生成等 | `script/sql/ry-cloud.sql` |
| `ry-job` | `dzg_job` | SnailJob / 定时任务 | `script/sql/ry-job.sql` |
| `ry-workflow` | `dzg_workflow` | 工作流 | `script/sql/ry-workflow.sql` |
| `ry-seata` | `dzg_seata` | Seata 分布式事务，默认可先不启用 | `script/sql/ry-seata.sql` |
| `ry-config` | `dzg_config` | Nacos 配置中心数据库，仅 Nacos 需要持久化到 MySQL 时使用 | `script/sql/ry-config.sql` |

在 MySQL 中执行：

```sql
CREATE DATABASE IF NOT EXISTS dzg_cloud
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS dzg_job
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS dzg_workflow
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS dzg_seata
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS dzg_config
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
```

创建项目专用 MySQL 用户，避免直接使用 `root`：

```sql
CREATE USER IF NOT EXISTS 'MYSQL_APP_USER'@'%' IDENTIFIED BY 'MYSQL_APP_PASSWORD';

GRANT ALL PRIVILEGES ON dzg_cloud.* TO 'MYSQL_APP_USER'@'%';
GRANT ALL PRIVILEGES ON dzg_job.* TO 'MYSQL_APP_USER'@'%';
GRANT ALL PRIVILEGES ON dzg_workflow.* TO 'MYSQL_APP_USER'@'%';
GRANT ALL PRIVILEGES ON dzg_seata.* TO 'MYSQL_APP_USER'@'%';

FLUSH PRIVILEGES;
```

如果 Nacos 自己也使用 MySQL 持久化，建议给 Nacos 单独建账号，不要和业务项目共用账号：

```sql
CREATE USER IF NOT EXISTS 'nacos_user'@'%' IDENTIFIED BY 'NACOS_DB_PASSWORD';
GRANT ALL PRIVILEGES ON dzg_config.* TO 'nacos_user'@'%';
FLUSH PRIVILEGES;
```

## 6. 导入 SQL

在本地或服务器上用 MySQL 客户端导入：

```bash
mysql -h MYSQL_HOST -P MYSQL_PORT -u MYSQL_APP_USER -p dzg_cloud < script/sql/ry-cloud.sql
mysql -h MYSQL_HOST -P MYSQL_PORT -u MYSQL_APP_USER -p dzg_cloud < script/sql/dzg-shop.sql
mysql -h MYSQL_HOST -P MYSQL_PORT -u MYSQL_APP_USER -p dzg_job < script/sql/ry-job.sql
mysql -h MYSQL_HOST -P MYSQL_PORT -u MYSQL_APP_USER -p dzg_workflow < script/sql/ry-workflow.sql
mysql -h MYSQL_HOST -P MYSQL_PORT -u MYSQL_APP_USER -p dzg_seata < script/sql/ry-seata.sql
```

`script/sql/ry-config.sql` 是 Nacos 配置中心表结构和初始 Data ID 记录。只有在“服务器上的 Nacos 准备用 MySQL 保存配置，并且 Nacos 数据库还没有初始化”时才导入：

```bash
mysql -h MYSQL_HOST -P MYSQL_PORT -u nacos_user -p dzg_config < script/sql/ry-config.sql
```

如果 Nacos 已经能正常登录、新增配置、重启后配置不丢失，不要重复导入 `ry-config.sql`。

导入完成后检查：

```sql
USE dzg_cloud;
SHOW TABLES;

USE dzg_job;
SHOW TABLES;

USE dzg_workflow;
SHOW TABLES;
```

## 7. 配置 Nacos 自身连接 MySQL

这一步只针对服务器上的 Nacos。

如果 Nacos 是独立部署，并且要使用 MySQL 持久化配置，需要在 Nacos 的 `conf/application.properties` 中配置数据库。示例：

```properties
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://MYSQL_HOST:MYSQL_PORT/dzg_config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai
db.user.0=nacos_user
db.password.0=NACOS_DB_PASSWORD
```

改完后重启 Nacos，然后访问：

```text
http://NACOS_HOST:8848/nacos
```

能登录并能看到配置管理页面，说明 Nacos 自身已正常。

## 8. 创建 Nacos 命名空间

登录 Nacos 控制台：

```text
http://NACOS_HOST:8848/nacos
```

进入“命名空间”，创建：

| 命名空间名称 | 命名空间 ID | 用途 |
| --- | --- | --- |
| `dev` | `dev` | 开发环境 |
| `prod` | `prod` | 生产环境 |

注意：命名空间 ID 必须是 `dev` 和 `prod`。当前项目的服务配置里写的是：

```yaml
namespace: ${spring.profiles.active}
```

也就是说，`dev` profile 只会读取命名空间 ID 为 `dev` 的配置。

## 9. 导入或创建 Nacos 配置

在 Nacos 控制台切换到 `dev` 命名空间，进入“配置管理”，按下面列表创建配置。

Group 统一使用：

```text
DEFAULT_GROUP
```

配置列表：

| Data ID | 类型 | 内容来源 |
| --- | --- | --- |
| `application-common.yml` | YAML | `script/config/nacos/application-common.yml` |
| `datasource.yml` | YAML | `script/config/nacos/datasource.yml` |
| `dzg-auth.yml` | YAML | `script/config/nacos/dzg-auth.yml` |
| `dzg-gateway.yml` | YAML | `script/config/nacos/dzg-gateway.yml` |
| `dzg-gateway-mvc.yml` | YAML | `script/config/nacos/dzg-gateway-mvc.yml` |
| `dzg-system.yml` | YAML | `script/config/nacos/dzg-system.yml` |
| `dzg-shop.yml` | YAML | `script/config/nacos/dzg-shop.yml` |
| `dzg-gen.yml` | YAML | `script/config/nacos/dzg-gen.yml` |
| `dzg-job.yml` | YAML | `script/config/nacos/dzg-job.yml` |
| `dzg-resource.yml` | YAML | `script/config/nacos/dzg-resource.yml` |
| `dzg-workflow.yml` | YAML | `script/config/nacos/dzg-workflow.yml` |
| `dzg-monitor.yml` | YAML | `script/config/nacos/dzg-monitor.yml` |
| `dzg-snailjob-server.yml` | YAML | `script/config/nacos/dzg-snailjob-server.yml` |
| `seata-server.properties` | Properties | `script/config/nacos/seata-server.properties` |

如果先只启动基础后端，至少需要：

- `application-common.yml`
- `datasource.yml`
- `dzg-gateway.yml`
- `dzg-auth.yml`
- `dzg-system.yml`
- `dzg-shop.yml`

如果启动代码生成、文件资源、任务调度、工作流，再补对应 Data ID。

## 10. 修改 `datasource.yml`

Nacos 中的 `datasource.yml` 决定项目连接哪些 MySQL 数据库。把原来的 `localhost` 和 `ry-*` 改成服务器地址和 `dzg_*` 数据库。

示例：

```yaml
datasource:
  system-master:
    url: jdbc:mysql://MYSQL_HOST:3306/dzg_cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true
    username: MYSQL_APP_USER
    password: MYSQL_APP_PASSWORD
  gen:
    url: jdbc:mysql://MYSQL_HOST:3306/dzg_cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true
    username: MYSQL_APP_USER
    password: MYSQL_APP_PASSWORD
  job:
    url: jdbc:mysql://MYSQL_HOST:3306/dzg_job?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true
    username: MYSQL_APP_USER
    password: MYSQL_APP_PASSWORD
  workflow:
    url: jdbc:mysql://MYSQL_HOST:3306/dzg_workflow?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true
    username: MYSQL_APP_USER
    password: MYSQL_APP_PASSWORD
```

下面的 `spring.datasource.dynamic`、连接池、Seata 开关等配置保持模板原样即可。第一次部署不要随意改这些参数。

## 11. 修改 `application-common.yml`

Nacos 中的 `application-common.yml` 是所有服务共享配置。当前最需要改 Redis。

找到：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: ruoyi123
      database: 0
```

改成：

```yaml
spring:
  data:
    redis:
      host: REDIS_HOST
      port: REDIS_PORT
      password: REDIS_PASSWORD
      database: 0
      timeout: 10000
      ssl.enabled: false
```

如果 Redis 还没部署完成：

- 可以先完成 MySQL 和 Nacos 配置。
- 不建议启动完整后端。
- Redis 部署完成后，再回到 Nacos 修改 `application-common.yml`。

同一个文件里还有 RabbitMQ：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: ruoyi
    password: ruoyi123
```

如果当前阶段没有部署 RabbitMQ，先不要启动依赖消息总线的能力；如果已经部署，就把它改成服务器 RabbitMQ 的地址、账号和密码。

## 12. 配置 Seata

当前 `application-common.yml` 中：

```yaml
seata:
  enabled: false
```

首次部署可以保持 `false`，先把基础登录、网关、系统服务跑通。

如果后续启用 Seata，再修改 Nacos 的 `seata-server.properties`：

```properties
store.db.url=jdbc:mysql://MYSQL_HOST:3306/dzg_seata?useUnicode=true&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true
store.db.user=MYSQL_APP_USER
store.db.password=MYSQL_APP_PASSWORD
```

同时确认已导入：

```text
script/sql/ry-seata.sql -> dzg_seata
```

## 13. 本地 IDEA / Maven 指向服务器 Nacos

项目本地 `application.yml` 中使用 Maven 占位符：

```yaml
server-addr: @nacos.server@
username: @nacos.username@
password: @nacos.password@
```

这些值来自根 `pom.xml` 的 profile。

开发环境需要确认 `dev` profile 中类似下面这样：

```xml
<profiles.active>dev</profiles.active>
<nacos.server>SERVER_IP:8848</nacos.server>
<nacos.discovery.group>DEFAULT_GROUP</nacos.discovery.group>
<nacos.config.group>DEFAULT_GROUP</nacos.config.group>
<nacos.username>NACOS_USERNAME</nacos.username>
<nacos.password>NACOS_PASSWORD</nacos.password>
```

修改 `pom.xml` 后，在 IDEA 执行：

```text
Maven -> Reload All Maven Projects
```

否则 IDEA 可能还使用旧的 Nacos 地址。

## 14. 推荐启动顺序

先启动基础设施：

1. MySQL
2. Redis
3. Nacos

再启动后端服务。基础服务建议顺序：

1. `dzg-gateway` 或 `dzg-gateway-mvc`，二选一按当前项目入口使用
2. `dzg-auth`
3. `dzg-system`
4. `dzg-shop`
5. `dzg-resource`
6. `dzg-gen`
7. `dzg-job`
8. `dzg-workflow`
9. `dzg-monitor`
10. `dzg-snailjob-server`

第一次排错时，不要一次启动所有模块。先跑通 `gateway + auth + system`，确认登录链路正常，再启动 `dzg-shop`。

## 15. 验证命令和检查点

检查 MySQL：

```bash
mysql -h MYSQL_HOST -P MYSQL_PORT -u MYSQL_APP_USER -p -e "SHOW DATABASES LIKE 'dzg_%';"
```

检查 Redis：

```bash
redis-cli -h REDIS_HOST -p REDIS_PORT -a REDIS_PASSWORD PING
```

期望输出：

```text
PONG
```

检查 Nacos 配置：

- 命名空间切到 `dev`。
- 配置列表能看到 `application-common.yml`、`datasource.yml`、`dzg-system.yml` 等。
- Group 是 `DEFAULT_GROUP`。
- YAML 配置类型选的是 `YAML`，不是 `TEXT`。

检查服务注册：

- 进入 Nacos “服务管理 -> 服务列表”。
- 命名空间切到 `dev`。
- 能看到 `dzg-gateway`、`dzg-auth`、`dzg-system`。

检查后端日志：

需要看到类似信息：

```text
Located property source: NacosPropertySource
Registering service with nacos
Started Dzg...Application
```

如果日志中出现 `localhost:3306` 或 `localhost:6379`，说明 Nacos 配置还没有改对，或者服务没有读取到正确命名空间。

## 16. 常见问题

### 16.1 Nacos 配置明明存在，服务还是读不到

重点检查：

- 命名空间 ID 是否是 `dev`，不是只把名称写成 `dev`。
- Data ID 是否完全一致，例如 `datasource.yml`，不要写成 `datasource.yaml`。
- Group 是否是 `DEFAULT_GROUP`。
- IDEA 是否 Reload Maven，确保 `@nacos.server@` 已替换成服务器地址。
- 启动时激活的 profile 是否是 `dev`。

### 16.2 仍然连接 `localhost:3306`

说明 `datasource.yml` 里数据库地址没改，或者服务读取的是另一个命名空间。到 Nacos 的 `dev` 命名空间重新检查 `datasource.yml`。

### 16.3 MySQL 连接失败

重点检查：

- MySQL 用户是否允许远程访问：`'MYSQL_APP_USER'@'%'`。
- 服务器安全组是否放行 `3306`。
- MySQL 配置是否监听外部地址。
- 数据库名是否改成 `dzg_cloud`、`dzg_job`、`dzg_workflow`。
- 密码里有特殊字符时，确认 YAML 没有解析错误。复杂密码建议用英文单引号包起来。

示例：

```yaml
password: 'your@complex:password'
```

### 16.4 Redis 连接失败

重点检查：

- Redis 是否已经启动。
- `REDIS_HOST`、`REDIS_PORT` 是否正确。
- Redis 密码是否和 `application-common.yml` 一致。
- 服务器安全组是否放行 `6379`。
- Redis 不建议无密码暴露到公网。

### 16.5 Nacos 可以登录，但服务无法注册

重点检查：

- `pom.xml` 中 `nacos.server` 是否是 `SERVER_IP:8848`。
- Nacos 用户名密码是否正确。
- 服务启动机器是否能访问 `http://SERVER_IP:8848/nacos`。
- Nacos 命名空间是否为 `dev`。

### 16.6 导入 `ry-config.sql` 后配置内容不完整

`ry-config.sql` 主要用于初始化 Nacos 配置表和 Data ID 记录。实际配置内容仍建议以 `script/config/nacos/` 下的文件为准，逐个复制到 Nacos 控制台，并修改 MySQL、Redis、Nacos、Seata 等环境地址。

## 17. 最小上线顺序建议

第一步，只完成基础设施：

- Nacos 可访问
- MySQL 数据库和表已创建
- Redis 可 `PING`

第二步，只配置 `dev` 命名空间：

- `application-common.yml`
- `datasource.yml`
- `dzg-gateway.yml`
- `dzg-auth.yml`
- `dzg-system.yml`

第三步，启动最小服务：

- `dzg-gateway`
- `dzg-auth`
- `dzg-system`

第四步，确认登录和基础菜单正常后，再启动：

- `dzg-resource`
- `dzg-gen`
- `dzg-job`
- `dzg-workflow`
- `dzg-monitor`
- `dzg-snailjob-server`

这样排错范围最小，出问题时能快速判断是 Nacos、MySQL、Redis 还是某个业务模块配置问题。
