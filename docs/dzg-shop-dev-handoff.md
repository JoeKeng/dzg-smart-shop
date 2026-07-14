# 店掌柜开发交接文档

## 1. 模块现状

店掌柜当前基于 RuoYi-Cloud-Plus 二次开发，技术前缀统一为 `dzg`，业务模块集中在 `dzg-shop`。

当前已推送到远端 `origin/master` 的关键提交：

- `af7ef486 feat: 扩展店掌柜店铺经营模块`
- `da8ef0b6 feat: 完善商品分类供应商和图片上传`

本阶段已完成最小经营闭环的主要骨架：

```text
商品分类 -> 商品档案 -> 供应商绑定 -> 采购入库 -> 库存变化 -> 收银销售 -> 赊账还款 -> 看板/报表/提醒
```

## 2. 后端交接

后端业务模块：

```text
dzg-modules/dzg-shop
```

主要职责：

- 商品、分类、供应商
- 库存和库存流水
- 收银订单和订单明细
- 客户、赊账、还款
- 采购入库
- 首页看板、经营报表、提醒中心

关键接口约定：

```text
GET  /shop/category/options
GET  /shop/product/list
GET  /shop/product/options
POST /shop/product
PUT  /shop/product
GET  /shop/supplier/options
POST /shop/purchase
POST /shop/cashier/checkout
```

`GET /shop/product/options` 支持 `supplierId` 查询参数，用于采购入库按供应商筛选商品。

## 3. 前端交接

前端业务目录：

```text
dzg-ui/src/views/shop
dzg-ui/src/api/shop
```

商品页当前能力：

- 分类下拉加载
- 无分类时提示并可新增分类
- 商品新增/编辑
- 供应商多选绑定
- OSS 图片上传
- 商品列表显示分类、供应商和图片

采购页当前能力：

- 选择供应商
- 商品下拉优先展示该供应商绑定商品
- 支持切回显示全部商品
- 采购入库后增加库存并写库存流水

供应商页当前能力：

- 供应商增删改查
- 手机号 11 位校验
- 列表显示已绑定商品数量

## 4. 数据库交接

业务数据库：

```text
dzg_shop
```

系统菜单数据库：

```text
dzg_cloud.sys_menu
```

核心关系：

- 商品分类：`dzg_product.category_id -> dzg_category.category_id`
- 商品供应商：`dzg_product_supplier.product_id -> dzg_product.product_id`
- 商品供应商：`dzg_product_supplier.supplier_id -> dzg_supplier.supplier_id`
- 商品图片：`dzg_product.image_oss_id -> sys_oss.oss_id`

补丁 SQL：

```text
script/sql/update/dzg-shop-extend-menu.sql
script/sql/update/dzg-shop-fix-product-supplier.sql
script/sql/update/dzg-shop-demo-data.sql
```

该补丁用于已有数据库，包含：

- 创建或补齐店铺业务表
- 新增 `image_oss_id`
- 新增 `dzg_product_supplier`
- 插入常用商品分类
- 补齐店铺经营菜单
- 隐藏重复的店铺首页看板

运行故障复盘和多人联调排查见：

```text
docs/dzg-shop-runtime-troubleshooting.md
```

## 5. OSS 与资源服务

商品图片上传依赖：

- `dzg-resource` 必须启动
- OSS 配置必须在系统文件配置管理或运行环境中维护
- 阿里云 AccessKey 和 Secret 不允许写入 Git

前端商品表单使用：

```text
ImageUpload
```

上传接口：

```text
/resource/oss/upload
```

## 6. Nacos 与服务启动

最小联调服务：

```text
Gateway
Auth
System
Shop
Resource
```

如果只验证登录和菜单，先启动：

```text
Gateway
Auth
System
```

如果验证商品图片，必须追加：

```text
Resource
```

如果验证店铺经营业务，必须追加：

```text
Shop
```

## 7. 提交规范

从下一阶段开始按模块小步提交，不再把多个模块混在一个提交里。

推荐提交顺序：

```text
docs: 整理店掌柜当前实现文档
fix: 修复商品分类和供应商联动问题
feat: 完善商品图片上传和展示
feat: 优化收银和采购入库流程
feat: 完善经营报表和提醒中心
```

规则：

- 一个提交只做一个模块或一个问题。
- SQL 和对应后端字段必须同一次提交。
- 配置模板可以提交，真实密码不能提交。
- 提交前先跑密钥扫描。
- 推送使用普通 `git push origin master`，不强推。

## 8. 验证清单

前端：

```powershell
cd D:\Code\shixun\RuoYi-Cloud-Plus\dzg-ui
npm run build:prod
```

后端：

```powershell
mvn "-Dmaven.repo.local=D:/Code/shixun/RuoYi-Cloud-Plus/.m2/repository" -pl dzg-modules/dzg-shop -am -DskipTests compile
```

密钥扫描：

```powershell
rg -n "Syh511322|dzg123|AccessKeyId|OSSAccessKeyId|ssh连接|8\.137\.169\.2" --glob "!**/target/**" --glob "!**/dist/**" --glob "!**/node_modules/**"
```

数据库验证：

```sql
select count(*) from information_schema.columns
where table_schema = 'dzg_shop'
  and table_name = 'dzg_product'
  and column_name = 'image_oss_id';

select count(*) from information_schema.tables
where table_schema = 'dzg_shop'
  and table_name = 'dzg_product_supplier';

select category_name from dzg_shop.dzg_category
where status = '0' and del_flag = '0'
order by sort_order;
```

## 9. 已知问题

- 命令行 Maven 在 Windows 环境可能出现 `An unknown compilation problem occurred / 无法关闭编译器资源`，且没有源码行。当前按环境问题记录。
- 文档和配置中不能写真实服务器密码，测试阶段明文只允许放在 Nacos 控制台或本机未提交配置。
- 菜单来自数据库，前端页面新增后仍需要确认 `sys_menu` 和角色权限。
- 多人同时运行 `dzg-shop` 并注册到同一个 Nacos namespace/group 时，Gateway 会负载均衡到不同实例，可能出现接口偶发 500、字段不一致、供应商关联不显示等问题。联调前先确认 Nacos 中目标服务只有一个有效实例。

## 10. 下一步建议

优先按以下顺序继续：

1. 联调商品闭环：分类、商品、供应商、图片。
2. 联调采购闭环：供应商筛选商品、入库、库存流水。
3. 联调收银闭环：现金收银、库存扣减、赊账订单。
4. 联调还款闭环：客户欠款、还款记录、未还金额。
5. 强化首页看板、经营报表、提醒中心。
6. 做适老化 UI 统一优化。
