# 店掌柜运行故障复盘：多人同时运行 Shop 模块

## 1. 问题现象

前端访问店铺经营页面时出现过以下异常：

```text
Error: 内部服务器错误
loadStocks
loadLogs
loadProducts
```

同时商品分类下拉已经可以显示，但商品列表里的“常用供应商”仍显示为“未绑定”。

## 2. 本次根因

本次 500 问题不是前端页面本身导致的，而是运行环境里同时存在多个 `dzg-shop` 服务实例。

`dzg-shop` 启动后会注册到 Nacos：

```yaml
spring:
  application:
    name: dzg-shop
```

Gateway 访问 `/shop/**` 时，会按服务名从 Nacos 找 `dzg-shop` 实例。如果我和队友同时运行同一个模块，并且注册到同一个 Nacos namespace/group，Gateway 可能把请求分发到任意一个实例。

这会造成几个问题：

- 两个人的代码版本不一致：有的实例有最新字段和供应商关联逻辑，有的没有。
- 两个人连接的数据库配置可能不一致：有的连接 `dzg_shop`，有的连接旧库或未补表的库。
- 同一个页面连续请求多个接口时，可能分别打到不同实例，表现为“刚才正常，刷新又失败”。
- 日志不一定出现在自己的 IDEA 控制台，因为请求可能被 Gateway 转发到了队友电脑上的实例。

## 3. 为什么能从 SQL 日志判断实例不一致

当前代码里的 `ShopProduct` 已经包含字段：

```java
private String imageOssId;
```

按当前实体映射，商品查询 SQL 应该包含：

```sql
image_oss_id
```

但实际日志里出现的是：

```sql
SELECT product_id, category_id, product_name, barcode, unit_name,
       sale_price, purchase_price, warning_qty, image_url, status,
       del_flag, remark, tenant_id, create_dept, create_by,
       create_time, update_by, update_time
FROM dzg_product
```

这条 SQL 没有 `image_oss_id`，说明处理请求的 Shop 实例大概率不是当前最新代码。

## 4. 正确运行方式

开发阶段建议同一套 Nacos 环境中，同一时间只保留一个 `dzg-shop` 实例。

如果多人必须同时开发，建议至少做其中一种隔离：

- 使用不同 Nacos namespace，例如 `dev-joe`、`dev-teammate`。
- 使用不同服务名，例如本地临时改成 `dzg-shop-joe`，并同步改 Gateway 路由。
- 使用不同端口并在 Gateway/Nacos 中只保留自己的实例。
- 调试时直接访问自己的 Shop 端口，例如 `http://localhost:9206`，避免经过 Gateway 负载均衡。

## 5. Nacos 检查步骤

进入 Nacos 控制台：

```text
服务管理 -> 服务列表 -> dzg-shop -> 详情
```

检查实例列表：

- 如果有多个 IP/端口，说明 Gateway 会负载均衡到多个实例。
- 调试时只保留自己的实例，或让队友先停止 Shop 模块。
- 停止后等 Nacos 实例下线，再刷新前端测试。

## 6. 供应商关联不显示的排查顺序

商品列表显示“未绑定”时，按下面顺序查。

### 6.1 确认当前请求打到最新 Shop 实例

如果后端 SQL 日志里没有 `image_oss_id`，说明不是最新实例，需要先处理 Nacos 多实例问题。

### 6.2 确认关系表有数据

在 `dzg_shop` 执行：

```sql
select product_id, supplier_id, tenant_id, status, del_flag
from dzg_product_supplier
order by create_time desc
limit 20;
```

如果没有数据，说明商品保存时没有真正写入供应商关联。需要重新编辑商品，选择供应商后保存。

### 6.3 确认租户和状态字段有效

当前系统租户默认为：

```text
000000
```

关联表应满足：

```sql
tenant_id = '000000'
status = '0'
del_flag = '0'
```

如果手工 SQL 插入时这些字段为空，前端可能显示“未绑定”。可以执行：

```sql
update dzg_product_supplier
set tenant_id = '000000'
where tenant_id is null or tenant_id = '';

update dzg_product_supplier
set status = '0'
where status is null or status = '';

update dzg_product_supplier
set del_flag = '0'
where del_flag is null or del_flag = '';
```

### 6.4 确认供应商没有被逻辑删除

```sql
select supplier_id, supplier_name, tenant_id, status, del_flag
from dzg_supplier
where supplier_id in (
    select supplier_id from dzg_product_supplier
);
```

供应商也应满足：

```sql
tenant_id = '000000'
status = '0'
del_flag = '0'
```

## 7. 本次代码侧处理

后端商品服务已经有供应商回填逻辑：

```text
ShopProductService.fillProductExtras
```

它会在商品列表查询后：

- 查询商品分类名称
- 查询 `dzg_product_supplier`
- 查询 `dzg_supplier`
- 回填 `supplierIds`
- 回填 `supplierNames`

为兼容手工导入或旧脚本产生的空状态数据，关联查询已放宽为：

```text
status = '0' 或 status 为空
del_flag = '0' 或 del_flag 为空
```

同时新增数据保存时会主动补齐默认值：

```text
tenant_id = '000000'
status = '0'
del_flag = '0'
```

前端商品列表也增加了兜底回显：如果后端暂时只返回 `supplierIds`，页面会用已经加载的供应商下拉选项映射出供应商名称，避免关联存在但列表仍显示“未绑定”。

SQL 修复脚本也会把历史数据里的空租户、空状态修正为默认值。

## 8. 推荐执行脚本

如果只修商品供应商绑定：

```text
script/sql/update/dzg-shop-fix-product-supplier.sql
```

如果要补齐整个店铺业务库：

```text
script/sql/update/dzg-shop-extend-menu.sql
```

如果要填充大量演示数据：

```text
script/sql/update/dzg-shop-demo-data.sql
```

## 9. 后续约定

- 联调时先确认 Nacos 中只有一个目标服务实例。
- 每次新增表字段后，必须同步执行对应 SQL 补丁。
- 页面显示异常时，先看 Network 对应接口，再看真正处理请求的后端实例日志。
- 不要只看自己的 IDEA 控制台，因为请求可能被 Gateway 转发到队友实例。
