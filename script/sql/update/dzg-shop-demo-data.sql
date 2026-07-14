-- 店掌柜店铺经营模块演示数据
-- 用途：本地/测试环境快速填充大量业务数据，便于检查页面、下拉框、报表、提醒和收银流程。
-- 说明：
-- 1. 本脚本只操作 dzg_shop 业务库，不操作 dzg_cloud 菜单权限库。
-- 2. 本脚本使用固定演示 ID 段，重复执行会先清理这些演示 ID 段，再重新写入。
-- 3. 不要在生产库执行。

use dzg_shop;

set @tenant_id = '000000';
set @now = sysdate();

start transaction;

-- 先清理本脚本生成的演示数据，避免重复执行产生重复记录。
delete from dzg_repayment where repayment_id between 29100000 and 29199999;
delete from dzg_credit_record where credit_id between 29000000 and 29099999;
delete from dzg_order_item where item_id between 28100000 and 28199999;
delete from dzg_order where order_id between 28000000 and 28099999;
delete from dzg_purchase_item where item_id between 26100000 and 26199999;
delete from dzg_purchase_order where purchase_id between 26000000 and 26099999;
delete from dzg_stock_log where log_id between 24100000 and 24299999;
delete from dzg_stock where stock_id between 24000000 and 24099999;
delete from dzg_product_supplier where id between 23100000 and 23199999;
delete from dzg_product where product_id between 22000000 and 22099999;
delete from dzg_supplier where supplier_id between 23001001 and 23001012;
delete from dzg_customer where customer_id between 25000001 and 25000030;
delete from dzg_notification where notice_id between 29200000 and 29299999;

-- 分类：补充到 10 个常见小店分类。
insert into dzg_category (
    category_id, tenant_id, category_name, sort_order, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(21001001, @tenant_id, '饮料酒水', 1, '0', '0', 103, 1, @now, null, null, '演示分类'),
(21001002, @tenant_id, '休闲零食', 2, '0', '0', 103, 1, @now, null, null, '演示分类'),
(21001003, @tenant_id, '粮油调味', 3, '0', '0', 103, 1, @now, null, null, '演示分类'),
(21001004, @tenant_id, '日用百货', 4, '0', '0', 103, 1, @now, null, null, '演示分类'),
(21001005, @tenant_id, '清洁纸品', 5, '0', '0', 103, 1, @now, null, null, '演示分类'),
(21001006, @tenant_id, '生鲜冷藏', 6, '0', '0', 103, 1, @now, null, null, '演示分类'),
(21001007, @tenant_id, '烟酒茶叶', 7, '0', '0', 103, 1, @now, null, null, '演示分类'),
(21001008, @tenant_id, '文具玩具', 8, '0', '0', 103, 1, @now, null, null, '演示分类'),
(21001009, @tenant_id, '小家电', 9, '0', '0', 103, 1, @now, null, null, '演示分类'),
(21001010, @tenant_id, '个人护理', 10, '0', '0', 103, 1, @now, null, null, '演示分类')
on duplicate key update
    category_name = values(category_name),
    sort_order = values(sort_order),
    status = '0',
    del_flag = '0',
    update_by = 1,
    update_time = @now,
    remark = values(remark);

-- 供应商：12 个常见进货渠道。
insert into dzg_supplier (
    supplier_id, tenant_id, supplier_name, contact_name, phone, address, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(23001001, @tenant_id, '县城综合批发部', '王经理', '13800001001', '县城批发市场一区 12 号', '0', '0', 103, 1, @now, null, null, '饮料、零食、日用百货'),
(23001002, @tenant_id, '老街粮油配送站', '李师傅', '13800001002', '老街粮油市场 8 号', '0', '0', 103, 1, @now, null, null, '粮油调味主供'),
(23001003, @tenant_id, '城北饮料仓配', '赵经理', '13800001003', '城北物流园 A3 仓', '0', '0', 103, 1, @now, null, null, '饮料酒水主供'),
(23001004, @tenant_id, '天天纸品商行', '孙老板', '13800001004', '人民路 46 号', '0', '0', 103, 1, @now, null, null, '纸品清洁用品'),
(23001005, @tenant_id, '惠民冷链配送', '周经理', '13800001005', '冷链园 2 号库', '0', '0', 103, 1, @now, null, null, '冷藏生鲜'),
(23001006, @tenant_id, '南门百货批发', '吴老板', '13800001006', '南门批发街 19 号', '0', '0', 103, 1, @now, null, null, '百货文具'),
(23001007, @tenant_id, '诚信烟酒茶行', '郑经理', '13800001007', '茶城 3 排 6 号', '0', '0', 103, 1, @now, null, null, '烟酒茶叶'),
(23001008, @tenant_id, '晨光文具供货点', '冯经理', '13800001008', '学校路 88 号', '0', '0', 103, 1, @now, null, null, '文具玩具'),
(23001009, @tenant_id, '家家乐小电器', '陈经理', '13800001009', '家电城 B 区 15 号', '0', '0', 103, 1, @now, null, null, '小家电'),
(23001010, @tenant_id, '康洁洗护配送', '褚经理', '13800001010', '工业路 22 号', '0', '0', 103, 1, @now, null, null, '洗护用品'),
(23001011, @tenant_id, '便民社区团配', '杨经理', '13800001011', '社区团配仓', '0', '0', 103, 1, @now, null, null, '应急补货'),
(23001012, @tenant_id, '乡镇赶集供货车', '马师傅', '13800001012', '流动供货车', '0', '0', 103, 1, @now, null, null, '赶集日补货')
on duplicate key update
    supplier_name = values(supplier_name),
    contact_name = values(contact_name),
    phone = values(phone),
    address = values(address),
    status = '0',
    del_flag = '0',
    update_by = 1,
    update_time = @now,
    remark = values(remark);

-- 商品：10 个分类 * 每类 12 个商品 = 120 个商品。
insert into dzg_product (
    product_id, tenant_id, category_id, product_name, barcode, unit_name,
    sale_price, purchase_price, warning_qty, image_url, image_oss_id, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
)
select
    22000000 + ((c.category_id - 21001001) * 100) + n.n as product_id,
    @tenant_id,
    c.category_id,
    concat(c.category_name, '-', case n.n
        when 1 then '热销款'
        when 2 then '实惠装'
        when 3 then '家庭装'
        when 4 then '小包装'
        when 5 then '经典款'
        when 6 then '升级款'
        when 7 then '儿童款'
        when 8 then '老人常买'
        when 9 then '整箱装'
        when 10 then '促销款'
        when 11 then '高端款'
        else '备用款'
    end, lpad(n.n, 2, '0')) as product_name,
    concat('690', lpad(22000000 + ((c.category_id - 21001001) * 100) + n.n, 10, '0')) as barcode,
    case
        when c.category_id in (21001001, 21001007) then '瓶'
        when c.category_id in (21001003, 21001005, 21001006) then '袋'
        when c.category_id in (21001009) then '个'
        else '件'
    end as unit_name,
    round(3.50 + c.sort_order * 1.70 + n.n * 0.80, 2) as sale_price,
    round(2.10 + c.sort_order * 1.10 + n.n * 0.45, 2) as purchase_price,
    case when n.n in (3, 7, 11) then 20 else 10 end as warning_qty,
    null,
    null,
    '0',
    '0',
    103,
    1,
    date_sub(@now, interval (c.sort_order + n.n) day),
    null,
    null,
    '演示商品，可用于收银、采购、库存和报表测试'
from dzg_category c
join (
    select ones.n + tens.n * 10 + 1 as n
    from (
        select 0 n union all select 1 union all select 2 union all select 3 union all select 4 union all
        select 5 union all select 6 union all select 7 union all select 8 union all select 9
    ) ones
    cross join (
        select 0 n union all select 1
    ) tens
    where ones.n + tens.n * 10 + 1 <= 12
) n
where c.category_id between 21001001 and 21001010
on duplicate key update
    category_id = values(category_id),
    product_name = values(product_name),
    barcode = values(barcode),
    unit_name = values(unit_name),
    sale_price = values(sale_price),
    purchase_price = values(purchase_price),
    warning_qty = values(warning_qty),
    status = '0',
    del_flag = '0',
    update_by = 1,
    update_time = @now,
    remark = values(remark);

-- 商品和供应商：每个商品绑定 2 个常用供应商。
insert into dzg_product_supplier (
    id, tenant_id, product_id, supplier_id, sort_order, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time
)
select
    23100000 + (p.product_id - 22000000) * 10 + 1,
    @tenant_id,
    p.product_id,
    23001001 + mod(p.product_id, 12),
    1,
    '0',
    '0',
    103,
    1,
    @now,
    null,
    null
from dzg_product p
where p.product_id between 22000000 and 22099999
union all
select
    23100000 + (p.product_id - 22000000) * 10 + 2,
    @tenant_id,
    p.product_id,
    23001001 + mod(p.product_id + 5, 12),
    2,
    '0',
    '0',
    103,
    1,
    @now,
    null,
    null
from dzg_product p
where p.product_id between 22000000 and 22099999;

-- 当前库存：生成正常库存和部分低库存，用于提醒中心检查。
insert into dzg_stock (
    stock_id, tenant_id, product_id, quantity, warning_qty, del_flag,
    create_dept, create_by, create_time, update_by, update_time
)
select
    24000000 + (p.product_id - 22000000),
    @tenant_id,
    p.product_id,
    case
        when mod(p.product_id, 13) = 0 then greatest(p.warning_qty - 2, 0)
        when mod(p.product_id, 17) = 0 then p.warning_qty
        else 25 + mod(p.product_id, 80)
    end,
    p.warning_qty,
    '0',
    103,
    1,
    @now,
    null,
    null
from dzg_product p
where p.product_id between 22000000 and 22099999
on duplicate key update
    quantity = values(quantity),
    warning_qty = values(warning_qty),
    del_flag = '0',
    update_by = 1,
    update_time = @now;

-- 初始入库流水：每个商品一条。
insert into dzg_stock_log (
    log_id, tenant_id, product_id, change_type, change_qty, before_qty, after_qty, biz_id, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
)
select
    24100000 + (s.product_id - 22000000),
    @tenant_id,
    s.product_id,
    'init',
    s.quantity,
    0,
    s.quantity,
    null,
    '0',
    103,
    1,
    date_sub(@now, interval 12 day),
    null,
    null,
    '演示数据初始化库存'
from dzg_stock s
where s.stock_id between 24000000 and 24099999;

-- 客户：30 个常用熟客，部分有赊账额度。
insert into dzg_customer (
    customer_id, tenant_id, customer_name, phone, credit_limit, current_debt, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
)
select
    25000000 + n.n,
    @tenant_id,
    concat('演示客户', lpad(n.n, 2, '0')),
    concat('1390000', lpad(n.n, 4, '0')),
    case when mod(n.n, 3) = 0 then 1000.00 else 500.00 end,
    0.00,
    '0',
    '0',
    103,
    1,
    date_sub(@now, interval n.n day),
    null,
    null,
    case when mod(n.n, 4) = 0 then '常赊账客户' else '普通熟客' end
from (
    select ones.n + tens.n * 10 + 1 as n
    from (
        select 0 n union all select 1 union all select 2 union all select 3 union all select 4 union all
        select 5 union all select 6 union all select 7 union all select 8 union all select 9
    ) ones
    cross join (
        select 0 n union all select 1 union all select 2
    ) tens
    where ones.n + tens.n * 10 + 1 <= 30
) n
on duplicate key update
    customer_name = values(customer_name),
    phone = values(phone),
    credit_limit = values(credit_limit),
    current_debt = 0.00,
    status = '0',
    del_flag = '0',
    update_by = 1,
    update_time = @now,
    remark = values(remark);

-- 采购入库单：20 张，每张 3 个商品。
insert into dzg_purchase_order (
    purchase_id, tenant_id, purchase_no, supplier_id, total_amount, status, purchase_time, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
)
select
    26000000 + n.n,
    @tenant_id,
    concat('CG-DEMO-', date_format(@now, '%Y%m%d'), '-', lpad(n.n, 3, '0')),
    23001001 + mod(n.n, 12),
    0.00,
    'done',
    date_sub(@now, interval (20 - n.n) day),
    '0',
    103,
    1,
    date_sub(@now, interval (20 - n.n) day),
    null,
    null,
    '演示采购入库单'
from (
    select ones.n + tens.n * 10 + 1 as n
    from (
        select 0 n union all select 1 union all select 2 union all select 3 union all select 4 union all
        select 5 union all select 6 union all select 7 union all select 8 union all select 9
    ) ones
    cross join (
        select 0 n union all select 1
    ) tens
) n
where n.n <= 20;

insert into dzg_purchase_item (
    item_id, tenant_id, purchase_id, product_id, product_name, quantity, purchase_price, subtotal, del_flag,
    create_dept, create_by, create_time, update_by, update_time
)
select
    26100000 + o.n * 10 + l.n,
    @tenant_id,
    26000000 + o.n,
    p.product_id,
    p.product_name,
    8 + mod(o.n + l.n, 18),
    p.purchase_price,
    round((8 + mod(o.n + l.n, 18)) * p.purchase_price, 2),
    '0',
    103,
    1,
    date_sub(@now, interval (20 - o.n) day),
    null,
    null
from (
    select ones.n + tens.n * 10 + 1 as n
    from (
        select 0 n union all select 1 union all select 2 union all select 3 union all select 4 union all
        select 5 union all select 6 union all select 7 union all select 8 union all select 9
    ) ones
    cross join (
        select 0 n union all select 1
    ) tens
    where ones.n + tens.n * 10 + 1 <= 20
) o
cross join (
    select 1 n union all select 2 union all select 3
) l
join dzg_product p on p.product_id = 22000000 + mod(o.n + l.n, 10) * 100 + mod(o.n + l.n, 12) + 1;

update dzg_purchase_order po
set po.total_amount = (
    select coalesce(sum(pi.subtotal), 0.00)
    from dzg_purchase_item pi
    where pi.purchase_id = po.purchase_id
)
where po.purchase_id between 26000000 and 26099999;

-- 销售订单：80 张，每张 2 个商品，包含现金、微信、支付宝、赊账。
insert into dzg_order (
    order_id, tenant_id, order_no, customer_id, total_amount, paid_amount, pay_type, pay_status, order_time, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
)
select
    28000000 + n.n,
    @tenant_id,
    concat('XS-DEMO-', date_format(@now, '%Y%m%d'), '-', lpad(n.n, 4, '0')),
    case when mod(n.n, 5) = 0 then null else 25000001 + mod(n.n, 30) end,
    0.00,
    0.00,
    case
        when mod(n.n, 4) = 0 then 'credit'
        when mod(n.n, 4) = 1 then 'cash'
        when mod(n.n, 4) = 2 then 'wechat'
        else 'alipay'
    end,
    case when mod(n.n, 4) = 0 then 'unpaid' else 'paid' end,
    date_sub(@now, interval mod(80 - n.n, 28) day),
    '0',
    103,
    1,
    date_sub(@now, interval mod(80 - n.n, 28) day),
    null,
    null,
    '演示销售订单'
from (
    select ones.n + tens.n * 10 + 1 as n
    from (
        select 0 n union all select 1 union all select 2 union all select 3 union all select 4 union all
        select 5 union all select 6 union all select 7 union all select 8 union all select 9
    ) ones
    cross join (
        select 0 n union all select 1 union all select 2 union all select 3 union all
        select 4 union all select 5 union all select 6 union all select 7
    ) tens
    where ones.n + tens.n * 10 + 1 <= 80
) n;

insert into dzg_order_item (
    item_id, tenant_id, order_id, product_id, product_name, quantity, sale_price, subtotal, del_flag,
    create_dept, create_by, create_time, update_by, update_time
)
select
    28100000 + o.n * 10 + l.n,
    @tenant_id,
    28000000 + o.n,
    p.product_id,
    p.product_name,
    1 + mod(o.n + l.n, 4),
    p.sale_price,
    round((1 + mod(o.n + l.n, 4)) * p.sale_price, 2),
    '0',
    103,
    1,
    date_sub(@now, interval mod(80 - o.n, 28) day),
    null,
    null
from (
    select ones.n + tens.n * 10 + 1 as n
    from (
        select 0 n union all select 1 union all select 2 union all select 3 union all select 4 union all
        select 5 union all select 6 union all select 7 union all select 8 union all select 9
    ) ones
    cross join (
        select 0 n union all select 1 union all select 2 union all select 3 union all
        select 4 union all select 5 union all select 6 union all select 7
    ) tens
    where ones.n + tens.n * 10 + 1 <= 80
) o
cross join (
    select 1 n union all select 2
) l
join dzg_product p on p.product_id = 22000000 + mod(o.n + l.n + 2, 10) * 100 + mod(o.n + l.n + 2, 12) + 1;

update dzg_order o
set o.total_amount = (
    select coalesce(sum(oi.subtotal), 0.00)
    from dzg_order_item oi
    where oi.order_id = o.order_id
)
where o.order_id between 28000000 and 28099999;

update dzg_order
set paid_amount = case when pay_type = 'credit' then 0.00 else total_amount end,
    pay_status = case when pay_type = 'credit' then 'unpaid' else 'paid' end
where order_id between 28000000 and 28099999;

-- 销售出库流水：每条销售明细一条。
insert into dzg_stock_log (
    log_id, tenant_id, product_id, change_type, change_qty, before_qty, after_qty, biz_id, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
)
select
    24200000 + (oi.item_id - 28100000),
    @tenant_id,
    oi.product_id,
    'sale',
    -oi.quantity,
    s.quantity + oi.quantity,
    s.quantity,
    oi.order_id,
    '0',
    103,
    1,
    oi.create_time,
    null,
    null,
    '演示销售扣减库存流水'
from dzg_order_item oi
join dzg_stock s on s.product_id = oi.product_id
where oi.item_id between 28100000 and 28199999;

-- 赊账记录：每 4 张销售订单生成 1 条赊账，部分模拟已还一部分。
insert into dzg_credit_record (
    credit_id, tenant_id, order_id, customer_id, credit_amount, paid_amount, unpaid_amount, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
)
select
    29000000 + (o.order_id - 28000000),
    @tenant_id,
    o.order_id,
    o.customer_id,
    o.total_amount,
    case when mod(o.order_id, 8) = 0 then round(o.total_amount * 0.40, 2) else 0.00 end,
    case when mod(o.order_id, 8) = 0 then round(o.total_amount * 0.60, 2) else o.total_amount end,
    case when mod(o.order_id, 8) = 0 then 'partial' else 'unpaid' end,
    '0',
    103,
    1,
    o.create_time,
    null,
    null,
    '演示赊账记录'
from dzg_order o
where o.order_id between 28000000 and 28099999
  and o.pay_type = 'credit'
  and o.customer_id is not null;

-- 还款记录：给部分赊账生成一次还款。
insert into dzg_repayment (
    repayment_id, tenant_id, credit_id, customer_id, repayment_amount, pay_type, repayment_time, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
)
select
    29100000 + (cr.credit_id - 29000000),
    @tenant_id,
    cr.credit_id,
    cr.customer_id,
    cr.paid_amount,
    'wechat',
    date_add(cr.create_time, interval 1 day),
    '0',
    103,
    1,
    date_add(cr.create_time, interval 1 day),
    null,
    null,
    '演示部分还款'
from dzg_credit_record cr
where cr.credit_id between 29000000 and 29099999
  and cr.paid_amount > 0;

-- 回写客户当前欠款。
update dzg_customer c
set c.current_debt = (
    select coalesce(sum(cr.unpaid_amount), 0.00)
    from dzg_credit_record cr
    where cr.customer_id = c.customer_id
      and cr.status in ('unpaid', 'partial')
)
where c.customer_id between 25000001 and 25000030;

-- 提醒：低库存提醒 + 赊账未还提醒。
insert into dzg_notification (
    notice_id, tenant_id, notice_type, notice_title, notice_content, biz_type, biz_id, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time
)
select
    29200000 + row_number() over (order by s.product_id),
    @tenant_id,
    'low_stock',
    '库存偏低',
    concat(p.product_name, ' 当前库存 ', s.quantity, '，已达到预警值 ', s.warning_qty, '，建议尽快补货。'),
    'product',
    p.product_id,
    '0',
    '0',
    103,
    1,
    @now,
    null,
    null
from dzg_stock s
join dzg_product p on p.product_id = s.product_id
where s.stock_id between 24000000 and 24099999
  and s.quantity <= s.warning_qty
limit 30;

insert into dzg_notification (
    notice_id, tenant_id, notice_type, notice_title, notice_content, biz_type, biz_id, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time
)
select
    29210000 + row_number() over (order by cr.credit_id),
    @tenant_id,
    'credit_unpaid',
    '赊账未还',
    concat(c.customer_name, ' 还有未还赊账 ', cr.unpaid_amount, ' 元，请适时提醒还款。'),
    'credit',
    cr.credit_id,
    '0',
    '0',
    103,
    1,
    @now,
    null,
    null
from dzg_credit_record cr
join dzg_customer c on c.customer_id = cr.customer_id
where cr.credit_id between 29000000 and 29099999
  and cr.unpaid_amount > 0
limit 30;

commit;

-- 执行后可用下面的查询检查数据量：
-- select 'category' name, count(*) count_value from dzg_category where category_id between 21001001 and 21001010
-- union all select 'supplier', count(*) from dzg_supplier where supplier_id between 23001001 and 23001012
-- union all select 'product', count(*) from dzg_product where product_id between 22000000 and 22099999
-- union all select 'product_supplier', count(*) from dzg_product_supplier where id between 23100000 and 23199999
-- union all select 'stock', count(*) from dzg_stock where stock_id between 24000000 and 24099999
-- union all select 'customer', count(*) from dzg_customer where customer_id between 25000001 and 25000030
-- union all select 'purchase_order', count(*) from dzg_purchase_order where purchase_id between 26000000 and 26099999
-- union all select 'purchase_item', count(*) from dzg_purchase_item where item_id between 26100000 and 26199999
-- union all select 'order', count(*) from dzg_order where order_id between 28000000 and 28099999
-- union all select 'order_item', count(*) from dzg_order_item where item_id between 28100000 and 28199999
-- union all select 'credit_record', count(*) from dzg_credit_record where credit_id between 29000000 and 29099999
-- union all select 'repayment', count(*) from dzg_repayment where repayment_id between 29100000 and 29199999
-- union all select 'notification', count(*) from dzg_notification where notice_id between 29200000 and 29299999;
