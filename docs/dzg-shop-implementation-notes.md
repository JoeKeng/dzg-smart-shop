# 店掌柜店铺经营模块实现记录

## 当前状态

本记录整理截至当前远端 `master` 的店掌柜店铺经营模块实现情况。最近两次核心提交为：

- `af7ef486 feat: 扩展店掌柜店铺经营模块`
- `da8ef0b6 feat: 完善商品分类供应商和图片上传`

当前仓库已完成从基础店铺经营模块到“商品分类、供应商绑定、采购筛选、OSS 图片上传”的第一轮可演示闭环扩展。

## 已完成内容

### 1. 店铺经营模块

后端新增 `dzg-modules/dzg-shop`，用于承载店掌柜业务逻辑。模块内按业务边界拆分 Controller、Service、Domain 和 Mapper，避免后续功能继续堆在单个大类里。

当前主要业务能力：

- 首页经营看板
- 收银台
- 商品管理
- 库存管理
- 客户管理
- 赊账管理
- 销售记录
- 采购入库
- 供应商管理
- 经营报表
- 提醒中心

业务接口继续保持 `/shop` 统一入口，避免扩大网关和前端代理变更。

### 2. 前端业务页面

前端新增 `dzg-ui/src/views/shop` 和 `dzg-ui/src/api/shop`，按业务目录拆分页面和接口。

当前页面已覆盖：

- `cashier`：收银台
- `product`：商品管理
- `stock`：库存管理
- `customer`：客户管理
- `credit`：赊账管理
- `order`：销售记录
- `purchase`：采购入库
- `supplier`：供应商管理
- `report`：经营报表
- `notification`：提醒中心

界面按适老化方向处理，重点是大字号、大按钮、高对比、少字段、少步骤和明确反馈。

### 3. 商品分类

商品分类接口保持：

```text
GET /shop/category/options
```

商品表单通过该接口加载分类下拉选项。远程测试库已补入常用分类种子数据：

- 饮料酒水
- 休闲零食
- 粮油调味
- 日用百货
- 清洁纸品

如果分类为空，前端商品页会提示“先新增分类”，并提供新增分类入口。

### 4. 商品、分类、供应商关系

当前业务关系定义为：

- 一个商品属于一个分类，使用 `dzg_product.category_id`
- 一个商品可以绑定多个供应商，使用 `dzg_product_supplier`
- 分类与供应商之间不直接绑定，二者通过商品产生业务关系

这样处理的原因是：同一个分类下可能有多个供应商，同一个供应商也可能供应多个分类的商品。直接让分类绑定供应商会让业务关系变复杂，也不符合小店实际进货场景。

### 5. 图片上传与 OSS

商品图片不引入外部 `AliOssUtil.java`，而是复用项目现有能力：

- 后端资源服务：`dzg-resource`
- 通用 OSS 能力：`dzg-common-oss`
- 前端上传组件：`ImageUpload`
- 上传接口：`/resource/oss/upload`

商品表新增字段：

```text
dzg_product.image_oss_id
```

同时保留：

```text
dzg_product.image_url
```

`image_url` 用于兼容旧数据或直接展示地址，`image_oss_id` 用于框架 OSS 文件记录回显。

## 数据库与 SQL

店铺经营业务表放在 `dzg_shop`，系统菜单仍放在 `dzg_cloud.sys_menu`。

核心新增或扩展：

- `dzg_product.image_oss_id`
- `dzg_product_supplier`
- `dzg_supplier`
- `dzg_purchase_order`
- `dzg_purchase_item`
- `dzg_notification`

SQL 文件：

- `script/sql/dzg-shop.sql`：新库初始化使用
- `script/sql/update/dzg-shop-extend-menu.sql`：已有库补丁使用
- `script/sql/update/dzg-shop-migrate-data-from-cloud.sql`：历史数据迁移辅助使用

补丁 SQL 已按幂等方式处理，适合对已初始化过的数据库重复执行。

## 已验证内容

### 前端

已执行：

```powershell
cd D:\Code\shixun\RuoYi-Cloud-Plus\dzg-ui
npm run build:prod
```

结果：构建通过。Vite 存在大 chunk 提示，这是体积提示，不是功能错误。

### 后端

已执行：

```powershell
mvn "-Dmaven.repo.local=D:/Code/shixun/RuoYi-Cloud-Plus/.m2/repository" -pl dzg-modules/dzg-shop -am -DskipTests compile
```

当前 Maven 在公共模块编译阶段出现过无源码行错误：

```text
An unknown compilation problem occurred
致命错误: 无法关闭编译器资源
```

该错误未指向本次 `dzg-shop` 代码的具体源码行，暂按 Windows/Javac 环境层问题记录。后续如果出现明确源码文件和行号，再按源码问题修复。

### 数据库

远程测试 MySQL 已同步补丁，确认结果：

- `dzg_shop.dzg_product.image_oss_id` 存在
- `dzg_shop.dzg_product_supplier` 存在
- `dzg_shop.dzg_category` 有可用分类数据

## 开发难点

### 1. 菜单不是只改前端

本项目菜单来自后端数据库 `sys_menu`。前端页面存在，不代表菜单一定显示。新增页面时需要同时处理：

- 前端页面
- 前端 API
- 后端接口
- 数据库菜单
- 角色菜单权限

### 2. 网关路径不能重复加前缀

前端请求 `/shop/...`，网关会转发到店铺服务。后端 Controller 保持相对业务路径即可，不能随意再加一层重复前缀，否则容易出现接口 404。

### 3. 租户字段暂时保留

业务上不展示租户，但表结构保留 `tenant_id`，默认值为 `000000`。这是为了兼容 RuoYi-Cloud-Plus 的多租户、权限和通用查询机制。

### 4. 库存一致性后续要重点加强

当前已具备入库、扣库存和库存流水基础能力。后续真实使用时，需要继续考虑并发收银、订单取消、退货、盘点调整等场景。

## 有趣的实现点

- 首页看板保留在主首页，店铺经营下隐藏重复看板，减少中老年经营者找入口的成本。
- 商品和供应商采用多对多关系，贴近小店“同一商品可能多个进货渠道”的实际情况。
- 采购入库选择供应商后优先展示绑定商品，同时保留“显示全部商品”，兼顾效率和容错。
- 图片上传没有另写阿里云工具类，而是接入框架已有 OSS 能力，避免密钥进入代码。

## 后续开发原则

- 从现在开始按模块小步提交。
- 每次提交只做一个明确目标。
- SQL 和对应后端字段必须同一次提交。
- 提交前必须做密钥扫描。
- 前端改动至少跑 `npm run build:prod`。
- 后端改动至少跑店铺模块 Maven 编译；若仍是无源码行 Javac 问题，记录但不误判为业务代码错误。
