# 店掌柜店铺经营模块实施记录

## 背景

本次工作围绕「店掌柜」智慧零售管理系统的店铺经营模块展开，目标是把原来较粗的店铺经营实现扩展成可演示的经营闭环，并让代码结构更适合后续二次开发。

本次提交：

- 提交号：`af7ef486`
- 提交信息：`feat: 扩展店掌柜店铺经营模块`
- 远端分支：`origin/master`

## 本次完成内容

### 1. 保留主首页看板

保留系统顶部 `首页` 里的老板看板，把它作为经营者进入系统后最先看到的主看板。

首页增加了常用大按钮：

- 收银台
- 采购入库
- 赊账还款
- 销售记录
- 经营报表
- 提醒中心

这样做的原因是面向乡镇小店和中老年经营者时，最常用操作应尽量放在第一屏，减少找菜单的成本。

### 2. 隐藏店铺经营下的重复看板

店铺经营菜单下原来的 `首页看板` 与顶部首页看板职责重复。本次选择隐藏重复入口，避免用户看到两个相似看板后不知道该点哪个。

相关 SQL：

- `script/sql/dzg-shop.sql`
- `script/sql/update/dzg-shop-extend-menu.sql`

### 3. 扩展店铺经营业务模块

在原有收银、商品、库存、客户、赊账基础上，新增了这些可演示模块：

- 销售记录
- 采购入库
- 供应商管理
- 经营报表
- 提醒中心

本阶段不追求完整 ERP，而是优先打通小店经营中最容易演示、最容易理解的链路：

`新增商品 -> 入库 -> 收银 -> 扣库存 -> 挂账 -> 还款 -> 看板/报表查看`

### 4. 后端模块拆分

原来店铺经营逻辑集中在单个 `ShopController` 和 `ShopServiceImpl` 中，后续继续加功能会很快变成大文件。本次拆分为按业务边界命名的 Controller 和 Service。

后端主要目录：

- `dzg-modules/dzg-shop/src/main/java/com/dzg/shop/controller`
- `dzg-modules/dzg-shop/src/main/java/com/dzg/shop/service`
- `dzg-modules/dzg-shop/src/main/java/com/dzg/shop/domain`
- `dzg-modules/dzg-shop/src/main/java/com/dzg/shop/mapper`

拆分后的 Controller：

- `ShopDashboardController`
- `ShopProductController`
- `ShopStockController`
- `ShopCustomerController`
- `ShopCashierController`
- `ShopCreditController`
- `ShopSupplierController`
- `ShopPurchaseController`
- `ShopReportController`
- `ShopNotificationController`

拆分后的 Service：

- `ShopProductService`
- `ShopStockService`
- `ShopCustomerService`
- `ShopCashierService`
- `ShopCreditService`
- `ShopSupplierService`
- `ShopPurchaseService`
- `ShopReportService`
- `ShopNotificationService`

其中库存扣减、入库、流水记录集中放在 `ShopStockService`，收银和采购都调用库存服务，避免库存规则散落在多个地方。

### 5. 前端模块拆分

原来的 `dzg-ui/src/api/shop/index.ts` 承担了过多接口定义。本次拆分为按业务命名的 API 文件：

- `dashboard.ts`
- `product.ts`
- `stock.ts`
- `customer.ts`
- `cashier.ts`
- `credit.ts`
- `purchase.ts`
- `supplier.ts`
- `report.ts`
- `notification.ts`

页面也按业务目录细分：

- `cashier`
- `product`
- `stock`
- `customer`
- `credit`
- `order`
- `purchase`
- `supplier`
- `report`
- `notification`

这样后续继续增加经营功能时，可以按目录直接找到对应页面和接口，不需要在一个大文件里翻找。

### 6. 数据库与菜单 SQL

本次继续使用 `dzg_` 表前缀，新增业务表：

- `dzg_supplier`
- `dzg_purchase_order`
- `dzg_purchase_item`
- `dzg_notification`

已有核心表继续保留：

- `dzg_category`
- `dzg_product`
- `dzg_stock`
- `dzg_stock_log`
- `dzg_customer`
- `dzg_order`
- `dzg_order_item`
- `dzg_credit_record`
- `dzg_repayment`

补丁 SQL：

- `script/sql/update/dzg-shop-extend-menu.sql`

这个补丁用于已经导入过旧版 `dzg-shop.sql` 的数据库，主要作用是补齐新增表、新增菜单，并隐藏重复的店铺首页看板。

### 7. 配置与敏感信息处理

提交前把 SSH 字样、MySQL 密码、Redis 密码从 Git 提交内容中清理掉。Nacos 运行地址保留为服务器地址，方便 IDEA 启动时通过 Maven 资源过滤写入各模块的 `application.yml`。

已改成变量或占位配置的文件包括：

- `script/config/nacos/datasource.yml`
- `script/config/nacos/application-common.yml`
- `script/config/nacos/seata-server.properties`
- `script/config/nacos/dzg-shop.yml`
- `dzg-visual/dzg-nacos/src/main/resources/application.properties`
- `docs/dzg-server-init-nacos-guide.md`

保留 Nacos 服务器地址的文件：

- `pom.xml`：`nacos.server=8.156.68.212:8848`

真实测试密码可以放在 Nacos 控制台或本机未提交配置里，但不要进入 GitHub。

## 验证情况

### 前端构建

已执行：

```powershell
cd D:\Code\shixun\RuoYi-Cloud-Plus\dzg-ui
npm run build:prod
```

结果：构建通过。

构建过程中有 Vite 大 chunk 警告，这是打包体积提示，不是本次功能错误。

### 后端编译

已执行：

```powershell
mvn "-Dmaven.repo.local=D:/Code/shixun/RuoYi-Cloud-Plus/.m2/repository" -pl dzg-modules/dzg-shop -am -DskipTests compile
```

结果：失败在公共模块 `dzg-common-redis`，错误为：

```text
An unknown compilation problem occurred
致命错误: 无法关闭编译器资源
```

这次编译还没有进入 `dzg-shop` 模块，没有出现店铺模块的具体源码行错误。该问题更像 Windows/Javac 环境层面的编译资源关闭问题，后续如果要严格验证后端，需要在 IDEA 或另一台环境中继续编译确认。

### 敏感信息扫描

提交前后均检查过 SSH 字样和密码关键字，最终提交内容中未发现：

- `Syh511322`
- `Syh511`
- `dzg123`
- `ssh连接`

说明：`8.156.68.212:8848` 是 Nacos 连接地址，属于运行所需配置；不要把服务器 SSH 密码、MySQL 密码、Redis 密码提交到仓库。

## 可能遇到的难点

### 1. 菜单和路由不是只改前端

这个项目的菜单来自后端数据库，前端页面写好了不代表菜单一定能出现。需要同时处理：

- 前端 `views/shop/...`
- 前端 API
- 后端接口权限
- SQL 菜单数据
- 用户角色是否拥有菜单权限

如果页面存在但菜单不显示，优先检查数据库 `sys_menu` 和角色菜单关联，而不是只看 Vue 路由。

### 2. 网关路径和 Controller 路径容易重复

当前网关对 `/shop/**` 做了转发和前缀处理，所以后端 Controller 保持相对路径，例如 `/product/list`、`/cashier/checkout`。

如果后端 Controller 再加一层 `/shop`，就可能变成路径重复或前端请求不到。

### 3. 租户字段仍然保留

业务上暂时隐藏和禁用租户，但数据表中仍保留 `tenant_id`，默认 `000000`。

这是为了兼容 RuoYi-Cloud-Plus 的底层租户、权限和数据隔离机制。现在不建议直接删除租户字段，否则可能影响登录、权限、通用查询、数据权限注入等基础能力。

### 4. 库存一致性会越来越重要

演示阶段可以先用简单同步逻辑完成扣库存和入库。但真实经营场景会遇到：

- 两个收银员同时卖同一个商品
- 库存不足但并发下单
- 采购入库和盘点调整同时发生
- 订单取消后是否回滚库存

后续如果做真实使用，需要给库存扣减增加更严格的并发控制和事务边界。

### 5. 赊账不是单纯订单状态

赊账涉及客户、订单、还款、欠款汇总。它不是把订单状态改成“未支付”这么简单。

本次设计中保留了：

- `dzg_credit_record`：赊账记录
- `dzg_repayment`：还款记录
- 客户当前欠款：由未结清赊账记录汇总

这样做后，后续可以支持部分还款、多次还款和赊账提醒。

### 6. Nacos 配置不能直接提交真实值

测试阶段明文配置可以先放在 Nacos 控制台，但 Git 仓库中应只保留模板、变量或占位值。

否则推送到 GitHub 时不仅有安全风险，也可能被 GitHub secret scanning 拦截。

## 实现中比较有趣的地方

### 1. 把“首页”和“店铺首页”做职责区分

一开始容易想到保留两个看板：系统首页一个，店铺经营下一个。但对实际小店老板来说，两个看板会增加理解成本。

本次选择保留顶部首页作为主经营入口，把常用动作放在首页大按钮里。这个取舍更贴合“少点几下、少找菜单”的目标。

### 2. 提醒中心不是单纯查通知表

提醒中心既可以显示 `dzg_notification` 里的提醒，也可以根据实时业务数据生成提醒，例如：

- 库存低于预警值
- 存在未结清赊账

这比只做一张通知表更灵活，因为有些提醒本质上是业务状态，不一定需要提前写入数据库。

### 3. 库存服务成为核心共享能力

收银会扣库存，采购会加库存，盘点会调整库存。它们看起来是不同页面，但底层都离不开库存流水。

所以把库存变更集中到 `ShopStockService` 是比较关键的模块化点。后续要加退货、报损、调拨，也可以继续复用这一层。

### 4. 适老化不是只把字号调大

本次前端页面按“简单经营工具”方向处理，不只是改大字体，还包括：

- 常用入口前置
- 页面主操作突出
- 表单字段尽量少
- 按钮高度更适合点击
- 金额、欠款、库存预警等关键数字更醒目

这类系统面对的是实际经营者，不是后台管理员，所以交互重点应放在“快”和“看得懂”。

## 后续建议

### 1. 先做一次数据库补丁导入

如果数据库已经导入过旧版店铺 SQL，执行：

```sql
script/sql/update/dzg-shop-extend-menu.sql
```

如果是新数据库，可以直接按初始化顺序导入完整 SQL。

### 2. 用 IDEA 验证后端模块

由于命令行 Maven 当前遇到 Windows/Javac 编译资源关闭问题，建议用 IDEA Reload Maven 后验证：

- Gateway
- Auth
- System
- Shop

重点看 `dzg-shop` 是否有真实源码编译错误。

### 3. 继续补齐真实联调

下一阶段建议按这个顺序联调：

1. 商品新增。
2. 商品入库。
3. 现金收银并扣库存。
4. 赊账收银并生成赊账记录。
5. 客户还款。
6. 首页看板、经营报表、提醒中心刷新。

### 4. 后续功能扩展方向

可继续扩展但不建议一次性全做：

- 退货退款
- 盘点单
- 商品条码
- 小票打印
- 会员积分
- 移动端收银
- AI 补货建议
- 滞销商品提醒
- 每日经营总结

建议优先做小店最常用、演示最直观的功能，避免过早变成复杂 ERP。
