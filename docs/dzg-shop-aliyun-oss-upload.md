# 店掌柜阿里云 OSS 商品图片联调

## 假设与范围

- 复用项目已有 `dzg-resource`、`dzg-common-oss`、`ImageUpload` 和 `/resource/oss/upload` 链路。
- 不引入外部 `AliOssUtil.java`，只通过项目已有 `sys_oss_config` 配置 OSS。
- 按当前联调要求，配套 SQL 已写入阿里云真实 AccessKey/SecretKey。
- 商品图片只保存 `dzg_product.image_oss_id`，列表展示时通过 `sys_oss.oss_id` 反查 URL。

成功标准：

1. `dzg-resource` 启动后默认 OSS 配置为 `aliyun`。
2. 商品页选择图片后，前端请求 `POST /resource/oss/upload` 返回 `ossId` 和 `url`。
3. 保存商品后，`dzg_shop.dzg_product.image_oss_id` 有值。
4. 刷新商品列表后，图片可通过 `GET /resource/oss/listByIds/{ossIds}` 回显。

## 现有链路

- 前端商品页：`dzg-ui/src/views/shop/product/index.vue`
- 上传组件：`dzg-ui/src/components/ImageUpload/index.vue`
- 上传接口：`POST /resource/oss/upload`
- 资源控制器：`dzg-modules/dzg-resource/src/main/java/com/dzg/resource/controller/SysOssController.java`
- 上传实现：`dzg-modules/dzg-resource/src/main/java/com/dzg/resource/service/impl/SysOssServiceImpl.java`
- OSS 客户端：`dzg-common/dzg-common-oss`
- 配置表：`dzg_cloud.sys_oss_config`
- 文件表：`dzg_cloud.sys_oss`
- 商品图片字段：`dzg_shop.dzg_product.image_oss_id`

## 配置方式

已提供可直接执行的 SQL：

```text
script/sql/update/dzg-shop-aliyun-oss-config.sql
```

该 SQL 会把 `configKey = aliyun` 的配置更新为：

- `accessKey = YOUR_ACCESS_KEY_ID`
- `secretKey = YOUR_ACCESS_KEY_SECRET`
- `bucketName = ruoyi-yibin-hovoy`
- `endpoint = oss-cn-chengdu.aliyuncs.com`
- `region = cn-chengdu`
- `prefix = images`
- `isHttps = Y`
- `accessPolicy = 1`
- `status = 0`

执行后会把其他默认 OSS 配置切回非默认，确保 `aliyun` 是唯一默认配置。

推荐使用后台页面维护：

1. 启动 `Gateway`、`Auth`、`System`、`Resource`、`Shop`。
2. 登录后台，进入 `系统管理 -> 文件管理 -> 配置管理`。
3. 编辑 `configKey = aliyun` 的配置。
4. 按上面的配置值填入。
5. 将 `aliyun` 切换为默认配置。

如果直接改数据库，`dzg-resource` 已启动时需要重启该服务，或再通过后台配置页面保存/切换一次默认配置，让 Redis 中的 `sys_oss_config` 缓存刷新。

## 联调步骤

1. 检查基础字段：

```sql
select count(*) as image_oss_id_exists
from information_schema.columns
where table_schema = 'dzg_shop'
  and table_name = 'dzg_product'
  and column_name = 'image_oss_id';

select config_key, bucket_name, endpoint, prefix, is_https, access_policy, status
from dzg_cloud.sys_oss_config
where config_key = 'aliyun';
```

2. 启动服务：

```text
Gateway
Auth
System
Resource
Shop
```

3. 在商品管理页新增或编辑商品，上传一张 `png/jpg/jpeg` 图片。
4. 保存商品后检查：

```sql
select product_id, product_name, image_oss_id
from dzg_shop.dzg_product
where image_oss_id is not null and image_oss_id <> ''
order by create_time desc
limit 5;

select oss_id, file_name, original_name, url, service
from dzg_cloud.sys_oss
order by create_time desc
limit 5;
```

5. 刷新商品列表，确认图片可回显。

## 常见问题

- `POST /resource/oss/upload` 404：检查 Gateway 是否有 `/resource/** -> dzg-resource` 路由，且 `dzg-resource` 已注册到 Nacos。
- 上传 401/403：检查登录态和角色是否有 `system:oss:upload`、`system:oss:query` 权限。
- 上传提示找不到默认配置：检查 `sys_oss_config.status = '0'` 是否只有 `aliyun` 一条，重启 `dzg-resource` 刷新缓存。
- 图片上传成功但列表不显示：检查商品保存请求体里的 `imageOssId` 是否有值，以及 `dzg_product.image_oss_id` 是否落库。
- 私有桶图片短时间后不可访问：这是预签名 URL 行为；列表刷新会重新通过 `listByIds` 获取临时 URL。
