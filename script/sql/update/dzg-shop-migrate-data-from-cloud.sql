-- 店掌柜业务表从 dzg_cloud 迁移到 dzg_shop
-- 使用场景：之前误把 dzg_* 业务表导入了 dzg_cloud，后续改为业务库 dzg_shop。
-- 执行前先执行 dzg-shop-extend-menu.sql，确保 dzg_shop 表结构已经存在。

create database if not exists dzg_shop default character set utf8mb4 collate utf8mb4_general_ci;

insert ignore into dzg_shop.dzg_category select * from dzg_cloud.dzg_category;
insert ignore into dzg_shop.dzg_product select * from dzg_cloud.dzg_product;
insert ignore into dzg_shop.dzg_stock select * from dzg_cloud.dzg_stock;
insert ignore into dzg_shop.dzg_stock_log select * from dzg_cloud.dzg_stock_log;
insert ignore into dzg_shop.dzg_customer select * from dzg_cloud.dzg_customer;
insert ignore into dzg_shop.dzg_order select * from dzg_cloud.dzg_order;
insert ignore into dzg_shop.dzg_order_item select * from dzg_cloud.dzg_order_item;
insert ignore into dzg_shop.dzg_credit_record select * from dzg_cloud.dzg_credit_record;
insert ignore into dzg_shop.dzg_repayment select * from dzg_cloud.dzg_repayment;

select 'dzg_cloud.dzg_category' as table_name, count(*) as row_count from dzg_cloud.dzg_category
union all select 'dzg_shop.dzg_category', count(*) from dzg_shop.dzg_category
union all select 'dzg_cloud.dzg_product', count(*) from dzg_cloud.dzg_product
union all select 'dzg_shop.dzg_product', count(*) from dzg_shop.dzg_product
union all select 'dzg_cloud.dzg_stock', count(*) from dzg_cloud.dzg_stock
union all select 'dzg_shop.dzg_stock', count(*) from dzg_shop.dzg_stock
union all select 'dzg_cloud.dzg_customer', count(*) from dzg_cloud.dzg_customer
union all select 'dzg_shop.dzg_customer', count(*) from dzg_shop.dzg_customer
union all select 'dzg_cloud.dzg_order', count(*) from dzg_cloud.dzg_order
union all select 'dzg_shop.dzg_order', count(*) from dzg_shop.dzg_order;

-- 确认 dzg_shop 数据无误后，如需清理 dzg_cloud 中误导入的业务表，再手动执行下面语句。
-- drop table dzg_cloud.dzg_repayment;
-- drop table dzg_cloud.dzg_credit_record;
-- drop table dzg_cloud.dzg_order_item;
-- drop table dzg_cloud.dzg_order;
-- drop table dzg_cloud.dzg_customer;
-- drop table dzg_cloud.dzg_stock_log;
-- drop table dzg_cloud.dzg_stock;
-- drop table dzg_cloud.dzg_product;
-- drop table dzg_cloud.dzg_category;
