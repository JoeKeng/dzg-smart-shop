-- 店掌柜收款码 OSS 文件登记
-- 用途：把已上传到阿里云 OSS 的收银台收款码补登记到 sys_oss，便于文件管理列表查看。
-- 执行库：dzg_cloud

use dzg_cloud;

insert into sys_oss (
    oss_id, tenant_id, file_name, original_name, file_suffix, url, ext1,
    create_dept, create_time, create_by, update_time, update_by, service
)
select
    1900000000000001001, '000000',
    'images/dzg-pay/pay-alipay-cashier.jpg',
    '店掌柜支付宝收款码.jpg',
    '.jpg',
    'https://ruoyi-yibin-hovoy.oss-cn-chengdu.aliyuncs.com/images/dzg-pay/pay-alipay-cashier.jpg',
    '{"fileSize":81667,"contentType":"image/jpeg"}',
    103, sysdate(), 1, sysdate(), 1, 'aliyun'
where not exists (
    select 1 from sys_oss where file_name = 'images/dzg-pay/pay-alipay-cashier.jpg'
);

insert into sys_oss (
    oss_id, tenant_id, file_name, original_name, file_suffix, url, ext1,
    create_dept, create_time, create_by, update_time, update_by, service
)
select
    1900000000000001002, '000000',
    'images/dzg-pay/pay-wechat-cashier.png',
    '店掌柜微信收款码.png',
    '.png',
    'https://ruoyi-yibin-hovoy.oss-cn-chengdu.aliyuncs.com/images/dzg-pay/pay-wechat-cashier.png',
    '{"fileSize":84020,"contentType":"image/png"}',
    103, sysdate(), 1, sysdate(), 1, 'aliyun'
where not exists (
    select 1 from sys_oss where file_name = 'images/dzg-pay/pay-wechat-cashier.png'
);

select oss_id, file_name, original_name, file_suffix, service, url
from sys_oss
where file_name in (
    'images/dzg-pay/pay-alipay-cashier.jpg',
    'images/dzg-pay/pay-wechat-cashier.png'
)
order by file_name;
