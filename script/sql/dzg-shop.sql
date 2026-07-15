-- 店掌柜智慧零售管理系统业务初始化脚本
-- 第一阶段：商品、库存、客户、收银、赊账、采购、报表、提醒
-- 注意：业务表放在 dzg_shop；菜单权限放在系统主库 dzg_cloud。

create database if not exists dzg_cloud default character set utf8mb4 collate utf8mb4_general_ci;
create database if not exists dzg_shop default character set utf8mb4 collate utf8mb4_general_ci;
use dzg_shop;

drop table if exists dzg_repayment;
drop table if exists dzg_credit_record;
drop table if exists dzg_order_item;
drop table if exists dzg_order;
drop table if exists dzg_purchase_item;
drop table if exists dzg_purchase_order;
drop table if exists dzg_product_supplier;
drop table if exists dzg_supplier;
drop table if exists dzg_notification;
drop table if exists dzg_ai_message;
drop table if exists dzg_ai_conversation;
drop table if exists dzg_customer;
drop table if exists dzg_stock_log;
drop table if exists dzg_stock;
drop table if exists dzg_product;
drop table if exists dzg_category;

create table dzg_category (
    category_id   bigint(20)   not null comment '分类ID',
    tenant_id     varchar(20)  default '000000' comment '租户编号',
    category_name varchar(80)  not null comment '分类名称',
    sort_order    int          default 0 comment '排序',
    status        char(1)      default '0' comment '状态',
    del_flag      char(1)      default '0' comment '删除标志',
    create_dept   bigint(20)   default null comment '创建部门',
    create_by     bigint(20)   default null comment '创建者',
    create_time   datetime     default null comment '创建时间',
    update_by     bigint(20)   default null comment '更新者',
    update_time   datetime     default null comment '更新时间',
    remark        varchar(500) default null comment '备注',
    primary key (category_id)
) engine=innodb comment='店掌柜商品分类';

create table dzg_product (
    product_id     bigint(20)     not null comment '商品ID',
    tenant_id      varchar(20)    default '000000' comment '租户编号',
    category_id    bigint(20)     default null comment '分类ID',
    product_name   varchar(120)   not null comment '商品名称',
    barcode        varchar(64)    default null comment '条码',
    unit_name      varchar(20)    default '件' comment '单位',
    sale_price     decimal(12,2)  not null default 0.00 comment '售价',
    purchase_price decimal(12,2)  default 0.00 comment '进价',
    warning_qty    int            default 10 comment '库存预警值',
    image_url      varchar(500)   default null comment '图片地址',
    image_oss_id   varchar(255)   default null comment '图片OSS ID',
    status         char(1)        default '0' comment '状态',
    del_flag       char(1)        default '0' comment '删除标志',
    create_dept    bigint(20)     default null comment '创建部门',
    create_by      bigint(20)     default null comment '创建者',
    create_time    datetime       default null comment '创建时间',
    update_by      bigint(20)     default null comment '更新者',
    update_time    datetime       default null comment '更新时间',
    remark         varchar(500)   default null comment '备注',
    primary key (product_id),
    key idx_dzg_product_name (product_name),
    key idx_dzg_product_barcode (barcode)
) engine=innodb comment='店掌柜商品档案';

insert into dzg_category values
(21001001, '000000', '饮料酒水', 1, '0', '0', 103, 1, sysdate(), null, null, '常用商品分类'),
(21001002, '000000', '休闲零食', 2, '0', '0', 103, 1, sysdate(), null, null, '常用商品分类'),
(21001003, '000000', '粮油调味', 3, '0', '0', 103, 1, sysdate(), null, null, '常用商品分类'),
(21001004, '000000', '日用百货', 4, '0', '0', 103, 1, sysdate(), null, null, '常用商品分类'),
(21001005, '000000', '清洁纸品', 5, '0', '0', 103, 1, sysdate(), null, null, '常用商品分类');

create table dzg_stock (
    stock_id    bigint(20)  not null comment '库存ID',
    tenant_id   varchar(20) default '000000' comment '租户编号',
    product_id  bigint(20)  not null comment '商品ID',
    quantity    int         not null default 0 comment '当前库存',
    warning_qty int         default 10 comment '预警库存',
    del_flag    char(1)     default '0' comment '删除标志',
    create_dept bigint(20)  default null comment '创建部门',
    create_by   bigint(20)  default null comment '创建者',
    create_time datetime    default null comment '创建时间',
    update_by   bigint(20)  default null comment '更新者',
    update_time datetime    default null comment '更新时间',
    primary key (stock_id),
    unique key uk_dzg_stock_product (tenant_id, product_id)
) engine=innodb comment='店掌柜当前库存';

create table dzg_stock_log (
    log_id      bigint(20)   not null comment '流水ID',
    tenant_id   varchar(20)  default '000000' comment '租户编号',
    product_id  bigint(20)   not null comment '商品ID',
    change_type varchar(20)  not null comment '变动类型',
    change_qty  int          not null comment '变动数量',
    before_qty  int          not null comment '变动前库存',
    after_qty   int          not null comment '变动后库存',
    biz_id      bigint(20)   default null comment '业务ID',
    del_flag    char(1)      default '0' comment '删除标志',
    create_dept bigint(20)   default null comment '创建部门',
    create_by   bigint(20)   default null comment '创建者',
    create_time datetime     default null comment '创建时间',
    update_by   bigint(20)   default null comment '更新者',
    update_time datetime     default null comment '更新时间',
    remark      varchar(500) default null comment '备注',
    primary key (log_id),
    key idx_dzg_stock_log_product (product_id)
) engine=innodb comment='店掌柜库存流水';

create table dzg_customer (
    customer_id   bigint(20)    not null comment '客户ID',
    tenant_id     varchar(20)   default '000000' comment '租户编号',
    customer_name varchar(80)   not null comment '客户姓名',
    phone         varchar(30)   default null comment '手机号',
    credit_limit  decimal(12,2) default 0.00 comment '赊账额度',
    current_debt  decimal(12,2) default 0.00 comment '当前欠款',
    status        char(1)       default '0' comment '状态',
    del_flag      char(1)       default '0' comment '删除标志',
    create_dept   bigint(20)    default null comment '创建部门',
    create_by     bigint(20)    default null comment '创建者',
    create_time   datetime      default null comment '创建时间',
    update_by     bigint(20)    default null comment '更新者',
    update_time   datetime      default null comment '更新时间',
    remark        varchar(500)  default null comment '备注',
    primary key (customer_id),
    key idx_dzg_customer_name (customer_name),
    key idx_dzg_customer_phone (phone)
) engine=innodb comment='店掌柜客户';

create table dzg_order (
    order_id     bigint(20)    not null comment '订单ID',
    tenant_id    varchar(20)   default '000000' comment '租户编号',
    order_no     varchar(64)   not null comment '订单号',
    customer_id  bigint(20)    default null comment '客户ID',
    total_amount decimal(12,2) not null default 0.00 comment '订单金额',
    paid_amount  decimal(12,2) not null default 0.00 comment '实收金额',
    pay_type     varchar(20)   not null comment '收款方式',
    pay_status   varchar(20)   not null comment '支付状态',
    order_time   datetime      not null comment '下单时间',
    del_flag     char(1)       default '0' comment '删除标志',
    create_dept  bigint(20)    default null comment '创建部门',
    create_by    bigint(20)    default null comment '创建者',
    create_time  datetime      default null comment '创建时间',
    update_by    bigint(20)    default null comment '更新者',
    update_time  datetime      default null comment '更新时间',
    remark       varchar(500)  default null comment '备注',
    primary key (order_id),
    unique key uk_dzg_order_no (order_no),
    key idx_dzg_order_time (order_time)
) engine=innodb comment='店掌柜销售订单';

create table dzg_order_item (
    item_id      bigint(20)    not null comment '明细ID',
    tenant_id    varchar(20)   default '000000' comment '租户编号',
    order_id     bigint(20)    not null comment '订单ID',
    product_id   bigint(20)    not null comment '商品ID',
    product_name varchar(120)  not null comment '商品名称',
    quantity     int           not null comment '数量',
    sale_price   decimal(12,2) not null comment '售价',
    subtotal     decimal(12,2) not null comment '小计',
    del_flag     char(1)       default '0' comment '删除标志',
    create_dept  bigint(20)    default null comment '创建部门',
    create_by    bigint(20)    default null comment '创建者',
    create_time  datetime      default null comment '创建时间',
    update_by    bigint(20)    default null comment '更新者',
    update_time  datetime      default null comment '更新时间',
    primary key (item_id),
    key idx_dzg_order_item_order (order_id)
) engine=innodb comment='店掌柜销售明细';

create table dzg_credit_record (
    credit_id     bigint(20)    not null comment '赊账ID',
    tenant_id     varchar(20)   default '000000' comment '租户编号',
    order_id      bigint(20)    not null comment '订单ID',
    customer_id   bigint(20)    not null comment '客户ID',
    credit_amount decimal(12,2) not null comment '赊账金额',
    paid_amount   decimal(12,2) not null default 0.00 comment '已还金额',
    unpaid_amount decimal(12,2) not null comment '未还金额',
    status        varchar(20)   not null comment '状态',
    del_flag      char(1)       default '0' comment '删除标志',
    create_dept   bigint(20)    default null comment '创建部门',
    create_by     bigint(20)    default null comment '创建者',
    create_time   datetime      default null comment '创建时间',
    update_by     bigint(20)    default null comment '更新者',
    update_time   datetime      default null comment '更新时间',
    remark        varchar(500)  default null comment '备注',
    primary key (credit_id),
    key idx_dzg_credit_customer (customer_id)
) engine=innodb comment='店掌柜赊账记录';

create table dzg_repayment (
    repayment_id     bigint(20)    not null comment '还款ID',
    tenant_id        varchar(20)   default '000000' comment '租户编号',
    credit_id        bigint(20)    not null comment '赊账ID',
    customer_id      bigint(20)    not null comment '客户ID',
    repayment_amount decimal(12,2) not null comment '还款金额',
    pay_type         varchar(20)   default null comment '收款方式',
    repayment_time   datetime      not null comment '还款时间',
    del_flag         char(1)       default '0' comment '删除标志',
    create_dept      bigint(20)    default null comment '创建部门',
    create_by        bigint(20)    default null comment '创建者',
    create_time      datetime      default null comment '创建时间',
    update_by        bigint(20)    default null comment '更新者',
    update_time      datetime      default null comment '更新时间',
    remark           varchar(500)  default null comment '备注',
    primary key (repayment_id),
    key idx_dzg_repayment_credit (credit_id)
) engine=innodb comment='店掌柜还款记录';

create table dzg_supplier (
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

create table dzg_product_supplier (
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

create table dzg_purchase_order (
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

create table dzg_purchase_item (
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

create table dzg_notification (
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

create table dzg_ai_conversation (
    conversation_id bigint(20)   not null comment '对话ID',
    tenant_id       varchar(20)  default '000000' comment '租户编号',
    title           varchar(120) not null comment '对话标题',
    status          char(1)      default '0' comment '状态',
    del_flag        char(1)      default '0' comment '删除标志',
    create_dept     bigint(20)   default null comment '创建部门',
    create_by       bigint(20)   default null comment '创建者',
    create_time     datetime     default null comment '创建时间',
    update_by       bigint(20)   default null comment '更新者',
    update_time     datetime     default null comment '更新时间',
    remark          varchar(500) default null comment '备注',
    primary key (conversation_id),
    key idx_dzg_ai_conversation_time (update_time, create_time)
) engine=innodb comment='店掌柜AI对话';

create table dzg_ai_message (
    message_id      bigint(20)  not null comment '消息ID',
    tenant_id       varchar(20) default '000000' comment '租户编号',
    conversation_id bigint(20)  not null comment '对话ID',
    role            varchar(20) not null comment '消息角色',
    content         text        not null comment '消息内容',
    del_flag        char(1)     default '0' comment '删除标志',
    create_dept     bigint(20)  default null comment '创建部门',
    create_by       bigint(20)  default null comment '创建者',
    create_time     datetime    default null comment '创建时间',
    update_by       bigint(20)  default null comment '更新者',
    update_time     datetime    default null comment '更新时间',
    primary key (message_id),
    key idx_dzg_ai_message_conversation (conversation_id, create_time)
) engine=innodb comment='店掌柜AI消息';

-- 店铺经营菜单。导入前请确认这些 menu_id 未被占用。
use dzg_cloud;

insert into sys_menu values ('20000', '店铺经营', '0', '1', 'shop', null, '', 1, 0, 'M', '0', '0', '', 'shop', 103, 1, sysdate(), null, null, '店掌柜店铺经营目录');
insert into sys_menu values ('20100', 'AI助手', '0', '2', 'ai', null, '', 1, 0, 'M', '0', '0', '', 'education', 103, 1, sysdate(), null, null, '店掌柜AI助手一级菜单');
insert into sys_menu values ('20101', 'AI对话', '20100', '1', 'chat', 'shop/ai/index', '', 1, 0, 'C', '0', '0', 'shop:ai:chat', 'message', 103, 1, sysdate(), null, null, 'AI聊天与经营问答');
insert into sys_menu values ('20102', 'AI经营分析', '20100', '2', 'business-analysis', 'shop/ai/analysis/index', '', 1, 0, 'C', '0', '0', 'shop:ai:analysis', 'chart', 103, 1, sysdate(), null, null, 'AI详细经营分析和长期规划');
insert into sys_menu values ('20001', '首页看板', '20000', '1', 'dashboard', 'shop/dashboard/index', '', 1, 0, 'C', '1', '0', 'shop:dashboard:list', 'dashboard', 103, 1, sysdate(), null, null, '首页经营看板，演示阶段使用顶部首页');
insert into sys_menu values ('20002', '收银台', '20000', '2', 'cashier', 'shop/cashier/index', '', 1, 0, 'C', '0', '0', 'shop:cashier:add', 'money', 103, 1, sysdate(), null, null, '快速收银台');
insert into sys_menu values ('20003', '商品管理', '20000', '3', 'product', 'shop/product/index', '', 1, 0, 'C', '0', '0', 'shop:product:list', 'goods', 103, 1, sysdate(), null, null, '商品档案管理');
insert into sys_menu values ('20004', '库存管理', '20000', '4', 'stock', 'shop/stock/index', '', 1, 0, 'C', '0', '0', 'shop:stock:list', 'list', 103, 1, sysdate(), null, null, '库存管理');
insert into sys_menu values ('20005', '客户管理', '20000', '5', 'customer', 'shop/customer/index', '', 1, 0, 'C', '0', '0', 'shop:customer:list', 'user', 103, 1, sysdate(), null, null, '客户熟客管理');
insert into sys_menu values ('20006', '赊账管理', '20000', '6', 'credit', 'shop/credit/index', '', 1, 0, 'C', '0', '0', 'shop:credit:list', 'wallet', 103, 1, sysdate(), null, null, '赊账和还款管理');
insert into sys_menu values ('20007', '销售记录', '20000', '7', 'order', 'shop/order/index', '', 1, 0, 'C', '0', '0', 'shop:order:list', 'list', 103, 1, sysdate(), null, null, '销售订单记录');
insert into sys_menu values ('20008', '采购入库', '20000', '8', 'purchase', 'shop/purchase/index', '', 1, 0, 'C', '0', '0', 'shop:purchase:list', 'shopping', 103, 1, sysdate(), null, null, '采购入库');
insert into sys_menu values ('20009', '供应商管理', '20000', '9', 'supplier', 'shop/supplier/index', '', 1, 0, 'C', '0', '0', 'shop:supplier:list', 'peoples', 103, 1, sysdate(), null, null, '供应商管理');
insert into sys_menu values ('20010', '经营报表', '20000', '10', 'report', 'shop/report/index', '', 1, 0, 'C', '0', '0', 'shop:report:list', 'chart', 103, 1, sysdate(), null, null, '经营报表');
insert into sys_menu values ('20011', '提醒中心', '20000', '11', 'notification', 'shop/notification/index', '', 1, 0, 'C', '0', '0', 'shop:notification:list', 'message', 103, 1, sysdate(), null, null, '库存和赊账提醒');

insert into sys_menu values ('20031', '商品新增', '20003', '1', '#', '', '', 1, 0, 'F', '0', '0', 'shop:product:add', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20032', '商品修改', '20003', '2', '#', '', '', 1, 0, 'F', '0', '0', 'shop:product:edit', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20033', '商品删除', '20003', '3', '#', '', '', 1, 0, 'F', '0', '0', 'shop:product:remove', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20041', '库存调整', '20004', '1', '#', '', '', 1, 0, 'F', '0', '0', 'shop:stock:adjust', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20051', '客户新增', '20005', '1', '#', '', '', 1, 0, 'F', '0', '0', 'shop:customer:add', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20052', '客户修改', '20005', '2', '#', '', '', 1, 0, 'F', '0', '0', 'shop:customer:edit', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20061', '还款登记', '20006', '1', '#', '', '', 1, 0, 'F', '0', '0', 'shop:credit:repay', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20081', '采购入库', '20008', '1', '#', '', '', 1, 0, 'F', '0', '0', 'shop:purchase:add', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20091', '供应商新增', '20009', '1', '#', '', '', 1, 0, 'F', '0', '0', 'shop:supplier:add', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20092', '供应商修改', '20009', '2', '#', '', '', 1, 0, 'F', '0', '0', 'shop:supplier:edit', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values ('20093', '供应商删除', '20009', '3', '#', '', '', 1, 0, 'F', '0', '0', 'shop:supplier:remove', '#', 103, 1, sysdate(), null, null, '');

-- 演示阶段隐藏复杂入口，保留底层表和代码。
update sys_menu set visible = '1' where menu_id in (2, 3, 4, 5, 6, 115, 121, 122, 11616);
