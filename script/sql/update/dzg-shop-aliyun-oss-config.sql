-- 店掌柜阿里云 OSS 配置
-- 用途：将已有 sys_oss_config.aliyun 配置改为当前阿里云 OSS，并切换为默认 OSS。
-- 注意：本文件按当前联调要求包含明文 AccessKey/SecretKey。
-- 执行库：dzg_cloud

use dzg_cloud;

-- 1. 先确认 aliyun 配置行存在。
select oss_config_id, config_key, bucket_name, endpoint, prefix, is_https, access_policy, status
from sys_oss_config
where config_key = 'aliyun';

-- 2. 写入阿里云 OSS 配置。
update sys_oss_config
set access_key = 'YOUR_ACCESS_KEY_ID',
    secret_key = 'YOUR_ACCESS_KEY_SECRET',
    bucket_name = 'ruoyi-yibin-hovoy',
    prefix = 'images',
    endpoint = 'oss-cn-chengdu.aliyuncs.com',
    domain = '',
    is_https = 'Y',
    region = 'cn-chengdu',
    access_policy = '1',
    status = '0',
    update_time = sysdate(),
    remark = '店掌柜商品图片阿里云 OSS 配置'
where config_key = 'aliyun';

-- 3. 确保只有 aliyun 是默认配置。status = '0' 表示默认。
update sys_oss_config
set status = '1',
    update_time = sysdate()
where config_key <> 'aliyun'
  and status = '0';

-- 4. 执行后确认。
select config_key, bucket_name, endpoint, prefix, is_https, access_policy, status
from sys_oss_config
where config_key = 'aliyun';

-- 5. 如果 dzg-resource 已经启动，执行后需要重启 dzg-resource，
--    或在后台文件配置管理中保存/切换一次默认配置，刷新 Redis 缓存。
