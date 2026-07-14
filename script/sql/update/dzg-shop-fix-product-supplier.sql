-- 店掌柜商品供应商绑定修复脚本
-- 用途：修复商品保存时选择供应商失败，尤其是 dzg_product_supplier 关联表未创建的问题。
-- 执行库：dzg_shop
-- 安全性：幂等脚本，可重复执行；不会删除已有业务数据。

create database if not exists dzg_shop default character set utf8mb4 collate utf8mb4_general_ci;
use dzg_shop;

-- 商品图片字段：商品表如果是旧结构，补上 OSS 图片字段。
set @add_image_oss_id_sql = if(
    exists (
        select 1
        from information_schema.tables
        where table_schema = database()
          and table_name = 'dzg_product'
    )
    and not exists (
        select 1
        from information_schema.columns
        where table_schema = database()
          and table_name = 'dzg_product'
          and column_name = 'image_oss_id'
    ),
    'alter table dzg_product add column image_oss_id varchar(255) default null comment ''图片OSS ID'' after image_url',
    'select ''dzg_product.image_oss_id already exists or dzg_product missing'' as message'
);
prepare add_image_oss_id_stmt from @add_image_oss_id_sql;
execute add_image_oss_id_stmt;
deallocate prepare add_image_oss_id_stmt;

-- 供应商表：商品绑定供应商、采购入库供应商下拉依赖此表。
create table if not exists dzg_supplier (
    supplier_id   bigint(20)   not null comment '供应商ID',
    tenant_id     varchar(20)  default '000000' comment '租户编号',
    supplier_name varchar(120) not null comment '供应商名称',
    contact_name  varchar(60)  default null comment '联系人',
    phone         varchar(30)  default null comment '联系电话',
    address       varchar(255) default null comment '地址',
    status        char(1)      default '0' comment '状态',
    del_flag      char(1)      default '0' comment '删除标志',
    create_dept   bigint(20)   default null comment '创建部门',
    create_by     bigint(20)   default null comment '创建者',
    create_time   datetime     default null comment '创建时间',
    update_by     bigint(20)   default null comment '更新者',
    update_time   datetime     default null comment '更新时间',
    remark        varchar(500) default null comment '备注',
    primary key (supplier_id),
    key idx_dzg_supplier_name (supplier_name)
) engine=innodb comment='店掌柜供应商';

-- 商品供应商关联表：商品可绑定多个供应商，采购按供应商筛选商品依赖此表。
create table if not exists dzg_product_supplier (
    id          bigint(20)  not null comment '主键ID',
    tenant_id   varchar(20) default '000000' comment '租户编号',
    product_id  bigint(20)  not null comment '商品ID',
    supplier_id bigint(20)  not null comment '供应商ID',
    sort_order  int         default 0 comment '排序',
    status      char(1)     default '0' comment '状态',
    del_flag    char(1)     default '0' comment '删除标志',
    create_dept bigint(20)  default null comment '创建部门',
    create_by   bigint(20)  default null comment '创建者',
    create_time datetime    default null comment '创建时间',
    update_by   bigint(20)  default null comment '更新者',
    update_time datetime    default null comment '更新时间',
    primary key (id),
    unique key uk_dzg_product_supplier (tenant_id, product_id, supplier_id),
    key idx_dzg_product_supplier_product (product_id),
    key idx_dzg_product_supplier_supplier (supplier_id)
) engine=innodb comment='店掌柜商品供应商关联';

-- 兼容手工导入或旧脚本产生的空状态数据，否则租户/状态过滤后前端会显示“未绑定”。
update dzg_product_supplier
set tenant_id = '000000'
where tenant_id is null or tenant_id = '';

update dzg_product_supplier
set status = '0'
where status is null or status = '';

update dzg_product_supplier
set del_flag = '0'
where del_flag is null or del_flag = '';

update dzg_supplier
set tenant_id = '000000'
where tenant_id is null or tenant_id = '';

update dzg_supplier
set status = '0'
where status is null or status = '';

update dzg_supplier
set del_flag = '0'
where del_flag is null or del_flag = '';

-- 如果供应商为空，补 3 个基础供应商，便于立刻测试商品绑定。
insert into dzg_supplier (
    supplier_id, tenant_id, supplier_name, contact_name, phone, address, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
)
select 23001001, '000000', '县城综合批发部', '王经理', '13800001001', '县城批发市场一区 12 号', '0', '0', 103, 1, sysdate(), null, null, '演示供应商'
where not exists (select 1 from dzg_supplier where supplier_id = 23001001)
union all
select 23001002, '000000', '老街粮油配送站', '李师傅', '13800001002', '老街粮油市场 8 号', '0', '0', 103, 1, sysdate(), null, null, '演示供应商'
where not exists (select 1 from dzg_supplier where supplier_id = 23001002)
union all
select 23001003, '000000', '城北饮料仓配', '赵经理', '13800001003', '城北物流园 A3 仓', '0', '0', 103, 1, sysdate(), null, null, '演示供应商'
where not exists (select 1 from dzg_supplier where supplier_id = 23001003);

-- 执行后检查结果。
select table_name
from information_schema.tables
where table_schema = database()
  and table_name in ('dzg_supplier', 'dzg_product_supplier')
order by table_name;

select column_name, column_type
from information_schema.columns
where table_schema = database()
  and table_name = 'dzg_product'
  and column_name = 'image_oss_id';

select count(*) as supplier_count
from dzg_supplier
where del_flag = '0';

select count(*) as product_supplier_count
from dzg_product_supplier
where tenant_id = '000000'
  and status = '0'
  and del_flag = '0';
