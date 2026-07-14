use dzg_cloud;

-- 已导入旧版 dzg-shop.sql 的库执行本补丁。
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

create table if not exists dzg_purchase_order (
    purchase_id   bigint(20)    not null comment '采购单ID',
    tenant_id     varchar(20)   default '000000' comment '租户编号',
    purchase_no   varchar(64)   not null comment '采购单号',
    supplier_id   bigint(20)    default null comment '供应商ID',
    total_amount  decimal(12,2) not null default 0.00 comment '采购金额',
    status        varchar(20)   not null comment '状态',
    purchase_time datetime      not null comment '采购时间',
    del_flag      char(1)       default '0' comment '删除标志',
    create_dept   bigint(20)    default null comment '创建部门',
    create_by     bigint(20)    default null comment '创建者',
    create_time   datetime      default null comment '创建时间',
    update_by     bigint(20)    default null comment '更新者',
    update_time   datetime      default null comment '更新时间',
    remark        varchar(500)  default null comment '备注',
    primary key (purchase_id),
    unique key uk_dzg_purchase_no (purchase_no),
    key idx_dzg_purchase_time (purchase_time)
) engine=innodb comment='店掌柜采购入库单';

create table if not exists dzg_purchase_item (
    item_id        bigint(20)    not null comment '明细ID',
    tenant_id      varchar(20)   default '000000' comment '租户编号',
    purchase_id    bigint(20)    not null comment '采购单ID',
    product_id     bigint(20)    not null comment '商品ID',
    product_name   varchar(120)  not null comment '商品名称',
    quantity       int           not null comment '数量',
    purchase_price decimal(12,2) not null comment '进价',
    subtotal       decimal(12,2) not null comment '小计',
    del_flag       char(1)       default '0' comment '删除标志',
    create_dept    bigint(20)    default null comment '创建部门',
    create_by      bigint(20)    default null comment '创建者',
    create_time    datetime      default null comment '创建时间',
    update_by      bigint(20)    default null comment '更新者',
    update_time    datetime      default null comment '更新时间',
    primary key (item_id),
    key idx_dzg_purchase_item_order (purchase_id)
) engine=innodb comment='店掌柜采购明细';

create table if not exists dzg_notification (
    notice_id      bigint(20)   not null comment '提醒ID',
    tenant_id      varchar(20)  default '000000' comment '租户编号',
    notice_type    varchar(30)  not null comment '提醒类型',
    notice_title   varchar(120) not null comment '提醒标题',
    notice_content varchar(500) not null comment '提醒内容',
    biz_type       varchar(30)  default null comment '业务类型',
    biz_id         bigint(20)   default null comment '业务ID',
    status         char(1)      default '0' comment '状态',
    del_flag       char(1)      default '0' comment '删除标志',
    create_dept    bigint(20)   default null comment '创建部门',
    create_by      bigint(20)   default null comment '创建者',
    create_time    datetime     default null comment '创建时间',
    update_by      bigint(20)   default null comment '更新者',
    update_time    datetime     default null comment '更新时间',
    primary key (notice_id)
) engine=innodb comment='店掌柜经营提醒';

update sys_menu set visible = '1' where menu_id = 20001;

insert into sys_menu values ('20007', '销售记录', '20000', '7', 'order', 'shop/order/index', '', 1, 0, 'C', '0', '0', 'shop:order:list', 'list', 103, 1, sysdate(), null, null, '销售订单记录');
insert into sys_menu values ('20008', '采购入库', '20000', '8', 'purchase', 'shop/purchase/index', '', 1, 0, 'C', '0', '0', 'shop:purchase:list', 'shopping', 103, 1, sysdate(), null, null, '采购入库');
insert into sys_menu values ('20009', '供应商管理', '20000', '9', 'supplier', 'shop/supplier/index', '', 1, 0, 'C', '0', '0', 'shop:supplier:list', 'peoples', 103, 1, sysdate(), null, null, '供应商管理');
insert into sys_menu values ('20010', '经营报表', '20000', '10', 'report', 'shop/report/index', '', 1, 0, 'C', '0', '0', 'shop:report:list', 'chart', 103, 1, sysdate(), null, null, '经营报表');
insert into sys_menu values ('20011', '提醒中心', '20000', '11', 'notification', 'shop/notification/index', '', 1, 0, 'C', '0', '0', 'shop:notification:list', 'message', 103, 1, sysdate(), null, null, '库存和赊账提醒');

insert into sys_menu values ('20081', '采购入库', '20008', '1', '#', '', '', 1, 0, 'F', '0', '0', 'shop:purchase:add', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20091', '供应商新增', '20009', '1', '#', '', '', 1, 0, 'F', '0', '0', 'shop:supplier:add', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20092', '供应商修改', '20009', '2', '#', '', '', 1, 0, 'F', '0', '0', 'shop:supplier:edit', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20093', '供应商删除', '20009', '3', '#', '', '', 1, 0, 'F', '0', '0', 'shop:supplier:remove', '#', 103, 1, sysdate(), null, null, '');
