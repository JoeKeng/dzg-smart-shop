-- 店掌柜店铺经营模块真实演示数据
-- 用途：本地/测试环境填充一组小规模、可重复执行的数据，便于检查页面、报表、提醒和 AI 分析。
-- 说明：
-- 1. 本脚本只操作 dzg_shop 业务库，不操作 dzg_cloud 菜单权限库。
-- 2. 本脚本使用固定演示 ID 段，重复执行会先清理这些演示 ID 段。
-- 3. 不要在生产库执行。

use dzg_shop;

set @tenant_id = '000000';
set @now = sysdate();

start transaction;

delete from dzg_repayment where repayment_id between 29100000 and 29199999;
delete from dzg_credit_record where credit_id between 29000000 and 29099999;
delete from dzg_order_item where item_id between 28100000 and 28199999;
delete from dzg_order where order_id between 28000000 and 28099999;
delete from dzg_purchase_item where item_id between 26100000 and 26199999;
delete from dzg_purchase_order where purchase_id between 26000000 and 26099999;
delete from dzg_stock_log where log_id between 24100000 and 24299999;
delete from dzg_stock where stock_id between 24000000 and 24099999;
delete from dzg_product_supplier where id between 23100000 and 23199999;
delete from dzg_notification where notice_id between 29200000 and 29299999;
delete from dzg_product where product_id between 22000000 and 22099999;
delete from dzg_supplier where supplier_id between 23001001 and 23001099;
delete from dzg_customer where customer_id between 25000001 and 25000099;
delete from dzg_category where category_id between 21001001 and 21001099;

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
(21001010, @tenant_id, '个人护理', 10, '0', '0', 103, 1, @now, null, null, '演示分类');

insert into dzg_supplier (
    supplier_id, tenant_id, supplier_name, contact_name, phone, address, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(23001001, @tenant_id, '县城综合批发部', '王经理', '13800001001', '县城批发市场一区12号', '0', '0', 103, 1, @now, null, null, '饮料、零食、日用百货'),
(23001002, @tenant_id, '老街粮油配送站', '李师傅', '13800001002', '老街粮油市场8号', '0', '0', 103, 1, @now, null, null, '粮油调味主供'),
(23001003, @tenant_id, '城北饮料仓配', '赵经理', '13800001003', '城北物流园A3仓', '0', '0', 103, 1, @now, null, null, '饮料酒水主供'),
(23001004, @tenant_id, '天天纸品商行', '孙老板', '13800001004', '人民路46号', '0', '0', 103, 1, @now, null, null, '纸品清洁用品'),
(23001005, @tenant_id, '惠民冷链配送', '周经理', '13800001005', '冷链园2号库', '0', '0', 103, 1, @now, null, null, '冷藏生鲜'),
(23001006, @tenant_id, '南门百货批发', '吴老板', '13800001006', '南门批发街19号', '0', '0', 103, 1, @now, null, null, '百货文具'),
(23001007, @tenant_id, '诚信烟酒茶行', '郑经理', '13800001007', '茶城3排6号', '0', '0', 103, 1, @now, null, null, '烟酒茶叶'),
(23001008, @tenant_id, '晨光文具供货点', '冯经理', '13800001008', '学校路88号', '0', '0', 103, 1, @now, null, null, '文具玩具'),
(23001009, @tenant_id, '家家乐小电器', '陈经理', '13800001009', '家电城B区15号', '0', '0', 103, 1, @now, null, null, '小家电'),
(23001010, @tenant_id, '康洁洗护配送', '褚经理', '13800001010', '工业路22号', '0', '0', 103, 1, @now, null, null, '洗护用品');

insert into dzg_product (
    product_id, tenant_id, category_id, product_name, barcode, unit_name,
    sale_price, purchase_price, warning_qty, image_url, image_oss_id, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(22000001, @tenant_id, 21001001, '农夫山泉550ml', '690000000001', '瓶', 2.00, 1.20, 24, null, null, '0', '0', 103, 1, @now, null, null, '高频饮料'),
(22000002, @tenant_id, 21001001, '可口可乐500ml', '690000000002', '瓶', 3.50, 2.40, 18, null, null, '0', '0', 103, 1, @now, null, null, '高频饮料'),
(22000003, @tenant_id, 21001003, '东北大米5kg', '690000000003', '袋', 39.90, 31.00, 8, null, null, '0', '0', 103, 1, @now, null, null, '家庭刚需'),
(22000004, @tenant_id, 21001003, '金龙鱼食用油1.8L', '690000000004', '桶', 32.00, 25.50, 6, null, null, '0', '0', 103, 1, @now, null, null, '家庭刚需'),
(22000005, @tenant_id, 21001003, '海天生抽500ml', '690000000005', '瓶', 8.90, 6.10, 10, null, null, '0', '0', 103, 1, @now, null, null, '调味品'),
(22000006, @tenant_id, 21001002, '康师傅红烧牛肉面', '690000000006', '袋', 3.00, 2.10, 20, null, null, '0', '0', 103, 1, @now, null, null, '方便食品'),
(22000007, @tenant_id, 21001002, '洽洽香瓜子160g', '690000000007', '袋', 8.00, 5.80, 12, null, null, '0', '0', 103, 1, @now, null, null, '休闲零食'),
(22000008, @tenant_id, 21001005, '维达抽纸3包', '690000000008', '提', 16.90, 12.50, 10, null, null, '0', '0', 103, 1, @now, null, null, '纸品'),
(22000009, @tenant_id, 21001005, '蓝月亮洗手液500g', '690000000009', '瓶', 15.90, 10.80, 8, null, null, '0', '0', 103, 1, @now, null, null, '清洁用品'),
(22000010, @tenant_id, 21001006, '鲜鸡蛋30枚', '690000000010', '盒', 28.80, 22.00, 8, null, null, '0', '0', 103, 1, @now, null, null, '冷藏生鲜'),
(22000011, @tenant_id, 21001006, '双汇火腿肠10支', '690000000011', '袋', 12.90, 9.20, 10, null, null, '0', '0', 103, 1, @now, null, null, '冷藏食品'),
(22000012, @tenant_id, 21001008, '晨光中性笔黑色', '690000000012', '支', 2.50, 1.20, 20, null, null, '0', '0', 103, 1, @now, null, null, '文具'),
(22000013, @tenant_id, 21001004, '南孚5号电池2粒', '690000000013', '卡', 6.00, 4.20, 12, null, null, '0', '0', 103, 1, @now, null, null, '百货'),
(22000014, @tenant_id, 21001009, '公牛三位插排1.8米', '690000000014', '个', 39.00, 28.00, 4, null, null, '0', '0', 103, 1, @now, null, null, '小电器'),
(22000015, @tenant_id, 21001010, '舒肤佳香皂115g', '690000000015', '块', 5.50, 3.60, 16, null, null, '0', '0', 103, 1, @now, null, null, '个人护理');

insert into dzg_product_supplier (
    id, tenant_id, product_id, supplier_id, sort_order, status, del_flag, create_dept, create_by, create_time, update_by, update_time
) values
(23100001, @tenant_id, 22000001, 23001003, 1, '0', '0', 103, 1, @now, null, null),
(23100002, @tenant_id, 22000002, 23001003, 1, '0', '0', 103, 1, @now, null, null),
(23100003, @tenant_id, 22000003, 23001002, 1, '0', '0', 103, 1, @now, null, null),
(23100004, @tenant_id, 22000004, 23001002, 1, '0', '0', 103, 1, @now, null, null),
(23100005, @tenant_id, 22000005, 23001002, 1, '0', '0', 103, 1, @now, null, null),
(23100006, @tenant_id, 22000006, 23001001, 1, '0', '0', 103, 1, @now, null, null),
(23100007, @tenant_id, 22000007, 23001001, 1, '0', '0', 103, 1, @now, null, null),
(23100008, @tenant_id, 22000008, 23001004, 1, '0', '0', 103, 1, @now, null, null),
(23100009, @tenant_id, 22000009, 23001010, 1, '0', '0', 103, 1, @now, null, null),
(23100010, @tenant_id, 22000010, 23001005, 1, '0', '0', 103, 1, @now, null, null),
(23100011, @tenant_id, 22000011, 23001005, 1, '0', '0', 103, 1, @now, null, null),
(23100012, @tenant_id, 22000012, 23001008, 1, '0', '0', 103, 1, @now, null, null),
(23100013, @tenant_id, 22000013, 23001006, 1, '0', '0', 103, 1, @now, null, null),
(23100014, @tenant_id, 22000014, 23001009, 1, '0', '0', 103, 1, @now, null, null),
(23100015, @tenant_id, 22000015, 23001010, 1, '0', '0', 103, 1, @now, null, null);

insert into dzg_stock (
    stock_id, tenant_id, product_id, quantity, warning_qty, del_flag,
    create_dept, create_by, create_time, update_by, update_time
) values
(24000001, @tenant_id, 22000001, 36, 24, '0', 103, 1, @now, null, null),
(24000002, @tenant_id, 22000002, 14, 18, '0', 103, 1, @now, null, null),
(24000003, @tenant_id, 22000003, 7, 8, '0', 103, 1, @now, null, null),
(24000004, @tenant_id, 22000004, 10, 6, '0', 103, 1, @now, null, null),
(24000005, @tenant_id, 22000005, 22, 10, '0', 103, 1, @now, null, null),
(24000006, @tenant_id, 22000006, 42, 20, '0', 103, 1, @now, null, null),
(24000007, @tenant_id, 22000007, 9, 12, '0', 103, 1, @now, null, null),
(24000008, @tenant_id, 22000008, 18, 10, '0', 103, 1, @now, null, null),
(24000009, @tenant_id, 22000009, 6, 8, '0', 103, 1, @now, null, null),
(24000010, @tenant_id, 22000010, 5, 8, '0', 103, 1, @now, null, null),
(24000011, @tenant_id, 22000011, 16, 10, '0', 103, 1, @now, null, null),
(24000012, @tenant_id, 22000012, 35, 20, '0', 103, 1, @now, null, null),
(24000013, @tenant_id, 22000013, 28, 12, '0', 103, 1, @now, null, null),
(24000014, @tenant_id, 22000014, 3, 4, '0', 103, 1, @now, null, null),
(24000015, @tenant_id, 22000015, 30, 16, '0', 103, 1, @now, null, null);

insert into dzg_customer (
    customer_id, tenant_id, customer_name, phone, credit_limit, current_debt, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(25000001, @tenant_id, '王建国', '13900002001', 500.00, 51.60, '0', '0', 103, 1, @now, null, null, '村东头常客'),
(25000002, @tenant_id, '李桂兰', '13900002002', 300.00, 0.00, '0', '0', 103, 1, @now, null, null, '现金结算'),
(25000003, @tenant_id, '张师傅', '13900002003', 800.00, 128.00, '0', '0', 103, 1, @now, null, null, '饭店采购'),
(25000004, @tenant_id, '刘小梅', '13900002004', 300.00, 39.90, '0', '0', 103, 1, @now, null, null, '社区熟客'),
(25000005, @tenant_id, '陈立军', '13900002005', 500.00, 0.00, '0', '0', 103, 1, @now, null, null, '每周采购'),
(25000006, @tenant_id, '周阿姨', '13900002006', 300.00, 28.80, '0', '0', 103, 1, @now, null, null, '老人客户'),
(25000007, @tenant_id, '孙明', '13900002007', 200.00, 0.00, '0', '0', 103, 1, @now, null, null, '学生家长'),
(25000008, @tenant_id, '赵大爷', '13900002008', 300.00, 16.90, '0', '0', 103, 1, @now, null, null, '需要电话提醒'),
(25000009, @tenant_id, '何老板', '13900002009', 1000.00, 0.00, '0', '0', 103, 1, @now, null, null, '小饭馆'),
(25000010, @tenant_id, '杨丽', '13900002010', 300.00, 0.00, '0', '0', 103, 1, @now, null, null, '微信付款'),
(25000011, @tenant_id, '马老师', '13900002011', 300.00, 0.00, '0', '0', 103, 1, @now, null, null, '文具采购'),
(25000012, @tenant_id, '郑阿姨', '13900002012', 400.00, 55.00, '0', '0', 103, 1, @now, null, null, '月底结算');

insert into dzg_purchase_order (
    purchase_id, tenant_id, purchase_no, supplier_id, total_amount, status, purchase_time, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(26000001, @tenant_id, 'CG202607150001', 23001003, 144.00, 'done', date_sub(@now, interval 7 day), '0', 103, 1, @now, null, null, '饮料补货'),
(26000002, @tenant_id, 'CG202607150002', 23001002, 186.00, 'done', date_sub(@now, interval 6 day), '0', 103, 1, @now, null, null, '大米补货'),
(26000003, @tenant_id, 'CG202607150003', 23001002, 153.00, 'done', date_sub(@now, interval 6 day), '0', 103, 1, @now, null, null, '食用油补货'),
(26000004, @tenant_id, 'CG202607150004', 23001001, 168.00, 'done', date_sub(@now, interval 5 day), '0', 103, 1, @now, null, null, '方便面补货'),
(26000005, @tenant_id, 'CG202607150005', 23001004, 150.00, 'done', date_sub(@now, interval 5 day), '0', 103, 1, @now, null, null, '纸品补货'),
(26000006, @tenant_id, 'CG202607150006', 23001005, 154.00, 'done', date_sub(@now, interval 4 day), '0', 103, 1, @now, null, null, '鸡蛋补货'),
(26000007, @tenant_id, 'CG202607150007', 23001008, 72.00, 'done', date_sub(@now, interval 3 day), '0', 103, 1, @now, null, null, '文具补货'),
(26000008, @tenant_id, 'CG202607150008', 23001006, 126.00, 'done', date_sub(@now, interval 3 day), '0', 103, 1, @now, null, null, '电池补货'),
(26000009, @tenant_id, 'CG202607150009', 23001009, 112.00, 'done', date_sub(@now, interval 2 day), '0', 103, 1, @now, null, null, '插排补货'),
(26000010, @tenant_id, 'CG202607150010', 23001010, 108.00, 'done', date_sub(@now, interval 1 day), '0', 103, 1, @now, null, null, '香皂补货');

insert into dzg_purchase_item (
    item_id, tenant_id, purchase_id, product_id, product_name, quantity, purchase_price, subtotal, del_flag,
    create_dept, create_by, create_time, update_by, update_time
) values
(26100001, @tenant_id, 26000001, 22000001, '农夫山泉550ml', 120, 1.20, 144.00, '0', 103, 1, @now, null, null),
(26100002, @tenant_id, 26000002, 22000003, '东北大米5kg', 6, 31.00, 186.00, '0', 103, 1, @now, null, null),
(26100003, @tenant_id, 26000003, 22000004, '金龙鱼食用油1.8L', 6, 25.50, 153.00, '0', 103, 1, @now, null, null),
(26100004, @tenant_id, 26000004, 22000006, '康师傅红烧牛肉面', 80, 2.10, 168.00, '0', 103, 1, @now, null, null),
(26100005, @tenant_id, 26000005, 22000008, '维达抽纸3包', 12, 12.50, 150.00, '0', 103, 1, @now, null, null),
(26100006, @tenant_id, 26000006, 22000010, '鲜鸡蛋30枚', 7, 22.00, 154.00, '0', 103, 1, @now, null, null),
(26100007, @tenant_id, 26000007, 22000012, '晨光中性笔黑色', 60, 1.20, 72.00, '0', 103, 1, @now, null, null),
(26100008, @tenant_id, 26000008, 22000013, '南孚5号电池2粒', 30, 4.20, 126.00, '0', 103, 1, @now, null, null),
(26100009, @tenant_id, 26000009, 22000014, '公牛三位插排1.8米', 4, 28.00, 112.00, '0', 103, 1, @now, null, null),
(26100010, @tenant_id, 26000010, 22000015, '舒肤佳香皂115g', 30, 3.60, 108.00, '0', 103, 1, @now, null, null);

insert into dzg_order (
    order_id, tenant_id, order_no, customer_id, total_amount, paid_amount, pay_type, pay_status, order_time, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(28000001, @tenant_id, 'XS202607150001', 25000002, 12.00, 12.00, 'cash', 'paid', date_sub(@now, interval 10 hour), '0', 103, 1, @now, null, null, '现金购买'),
(28000002, @tenant_id, 'XS202607150002', 25000010, 16.90, 16.90, 'wechat', 'paid', date_sub(@now, interval 9 hour), '0', 103, 1, @now, null, null, '微信支付'),
(28000003, @tenant_id, 'XS202607150003', 25000003, 128.00, 0.00, 'credit', 'unpaid', date_sub(@now, interval 8 hour), '0', 103, 1, @now, null, null, '饭店赊账'),
(28000004, @tenant_id, 'XS202607150004', 25000005, 39.90, 39.90, 'alipay', 'paid', date_sub(@now, interval 7 hour), '0', 103, 1, @now, null, null, '支付宝'),
(28000005, @tenant_id, 'XS202607150005', 25000001, 51.60, 0.00, 'credit', 'unpaid', date_sub(@now, interval 6 hour), '0', 103, 1, @now, null, null, '熟客赊账'),
(28000006, @tenant_id, 'XS202607150006', 25000007, 15.00, 15.00, 'cash', 'paid', date_sub(@now, interval 5 hour), '0', 103, 1, @now, null, null, '学生用品'),
(28000007, @tenant_id, 'XS202607150007', 25000006, 28.80, 0.00, 'credit', 'unpaid', date_sub(@now, interval 4 hour), '0', 103, 1, @now, null, null, '鸡蛋赊账'),
(28000008, @tenant_id, 'XS202607150008', 25000009, 64.00, 64.00, 'wechat', 'paid', date_sub(@now, interval 3 hour), '0', 103, 1, @now, null, null, '小饭馆采购'),
(28000009, @tenant_id, 'XS202607150009', 25000004, 39.90, 0.00, 'credit', 'unpaid', date_sub(@now, interval 2 hour), '0', 103, 1, @now, null, null, '社区赊账'),
(28000010, @tenant_id, 'XS202607150010', 25000011, 25.00, 25.00, 'cash', 'paid', date_sub(@now, interval 100 minute), '0', 103, 1, @now, null, null, '文具购买'),
(28000011, @tenant_id, 'XS202607150011', 25000008, 16.90, 0.00, 'credit', 'partial', date_sub(@now, interval 80 minute), '0', 103, 1, @now, null, null, '纸品赊账'),
(28000012, @tenant_id, 'XS202607150012', 25000012, 55.00, 0.00, 'credit', 'unpaid', date_sub(@now, interval 70 minute), '0', 103, 1, @now, null, null, '月底结算'),
(28000013, @tenant_id, 'XS202607150013', 25000002, 6.00, 6.00, 'cash', 'paid', date_sub(@now, interval 55 minute), '0', 103, 1, @now, null, null, '电池'),
(28000014, @tenant_id, 'XS202607150014', 25000010, 31.80, 31.80, 'wechat', 'paid', date_sub(@now, interval 40 minute), '0', 103, 1, @now, null, null, '洗护'),
(28000015, @tenant_id, 'XS202607150015', 25000005, 8.90, 8.90, 'alipay', 'paid', date_sub(@now, interval 25 minute), '0', 103, 1, @now, null, null, '调味品');

insert into dzg_order_item (
    item_id, tenant_id, order_id, product_id, product_name, quantity, sale_price, subtotal, del_flag,
    create_dept, create_by, create_time, update_by, update_time
) values
(28100001, @tenant_id, 28000001, 22000001, '农夫山泉550ml', 6, 2.00, 12.00, '0', 103, 1, @now, null, null),
(28100002, @tenant_id, 28000002, 22000008, '维达抽纸3包', 1, 16.90, 16.90, '0', 103, 1, @now, null, null),
(28100003, @tenant_id, 28000003, 22000004, '金龙鱼食用油1.8L', 4, 32.00, 128.00, '0', 103, 1, @now, null, null),
(28100004, @tenant_id, 28000004, 22000003, '东北大米5kg', 1, 39.90, 39.90, '0', 103, 1, @now, null, null),
(28100005, @tenant_id, 28000005, 22000011, '双汇火腿肠10支', 4, 12.90, 51.60, '0', 103, 1, @now, null, null),
(28100006, @tenant_id, 28000006, 22000012, '晨光中性笔黑色', 6, 2.50, 15.00, '0', 103, 1, @now, null, null),
(28100007, @tenant_id, 28000007, 22000010, '鲜鸡蛋30枚', 1, 28.80, 28.80, '0', 103, 1, @now, null, null),
(28100008, @tenant_id, 28000008, 22000004, '金龙鱼食用油1.8L', 2, 32.00, 64.00, '0', 103, 1, @now, null, null),
(28100009, @tenant_id, 28000009, 22000003, '东北大米5kg', 1, 39.90, 39.90, '0', 103, 1, @now, null, null),
(28100010, @tenant_id, 28000010, 22000012, '晨光中性笔黑色', 10, 2.50, 25.00, '0', 103, 1, @now, null, null),
(28100011, @tenant_id, 28000011, 22000008, '维达抽纸3包', 1, 16.90, 16.90, '0', 103, 1, @now, null, null),
(28100012, @tenant_id, 28000012, 22000015, '舒肤佳香皂115g', 10, 5.50, 55.00, '0', 103, 1, @now, null, null),
(28100013, @tenant_id, 28000013, 22000013, '南孚5号电池2粒', 1, 6.00, 6.00, '0', 103, 1, @now, null, null),
(28100014, @tenant_id, 28000014, 22000009, '蓝月亮洗手液500g', 2, 15.90, 31.80, '0', 103, 1, @now, null, null),
(28100015, @tenant_id, 28000015, 22000005, '海天生抽500ml', 1, 8.90, 8.90, '0', 103, 1, @now, null, null);

insert into dzg_credit_record (
    credit_id, tenant_id, order_id, customer_id, credit_amount, paid_amount, unpaid_amount, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(29000001, @tenant_id, 28000003, 25000003, 128.00, 0.00, 128.00, 'unpaid', '0', 103, 1, @now, null, null, '饭店本日采购'),
(29000002, @tenant_id, 28000005, 25000001, 51.60, 0.00, 51.60, 'unpaid', '0', 103, 1, @now, null, null, '熟客赊账'),
(29000003, @tenant_id, 28000007, 25000006, 28.80, 0.00, 28.80, 'unpaid', '0', 103, 1, @now, null, null, '鸡蛋赊账'),
(29000004, @tenant_id, 28000009, 25000004, 39.90, 0.00, 39.90, 'unpaid', '0', 103, 1, @now, null, null, '大米赊账'),
(29000005, @tenant_id, 28000011, 25000008, 26.90, 10.00, 16.90, 'partial', '0', 103, 1, @now, null, null, '纸品已部分还款'),
(29000006, @tenant_id, 28000012, 25000012, 55.00, 0.00, 55.00, 'unpaid', '0', 103, 1, @now, null, null, '月底结算');

insert into dzg_repayment (
    repayment_id, tenant_id, credit_id, customer_id, repayment_amount, pay_type, repayment_time, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(29100001, @tenant_id, 29000005, 25000008, 10.00, 'cash', date_sub(@now, interval 30 minute), '0', 103, 1, @now, null, null, '先还10元');

insert into dzg_stock_log (
    log_id, tenant_id, product_id, change_type, change_qty, before_qty, after_qty, biz_id, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(24100001, @tenant_id, 22000001, 'in', 120, 0, 120, 26000001, '0', 103, 1, @now, null, null, '采购入库'),
(24100002, @tenant_id, 22000003, 'in', 6, 0, 6, 26000002, '0', 103, 1, @now, null, null, '采购入库'),
(24100003, @tenant_id, 22000004, 'in', 6, 0, 6, 26000003, '0', 103, 1, @now, null, null, '采购入库'),
(24100004, @tenant_id, 22000006, 'in', 80, 0, 80, 26000004, '0', 103, 1, @now, null, null, '采购入库'),
(24100005, @tenant_id, 22000008, 'in', 12, 0, 12, 26000005, '0', 103, 1, @now, null, null, '采购入库'),
(24100006, @tenant_id, 22000010, 'in', 7, 0, 7, 26000006, '0', 103, 1, @now, null, null, '采购入库'),
(24100007, @tenant_id, 22000012, 'in', 60, 0, 60, 26000007, '0', 103, 1, @now, null, null, '采购入库'),
(24100008, @tenant_id, 22000014, 'in', 4, 0, 4, 26000009, '0', 103, 1, @now, null, null, '采购入库'),
(24100009, @tenant_id, 22000001, 'out', 6, 42, 36, 28000001, '0', 103, 1, @now, null, null, '收银出库'),
(24100010, @tenant_id, 22000003, 'out', 1, 8, 7, 28000004, '0', 103, 1, @now, null, null, '收银出库'),
(24100011, @tenant_id, 22000010, 'out', 1, 6, 5, 28000007, '0', 103, 1, @now, null, null, '收银出库'),
(24100012, @tenant_id, 22000012, 'out', 10, 45, 35, 28000010, '0', 103, 1, @now, null, null, '收银出库'),
(24100013, @tenant_id, 22000008, 'out', 1, 19, 18, 28000011, '0', 103, 1, @now, null, null, '收银出库'),
(24100014, @tenant_id, 22000009, 'out', 2, 8, 6, 28000014, '0', 103, 1, @now, null, null, '收银出库'),
(24100015, @tenant_id, 22000005, 'out', 1, 23, 22, 28000015, '0', 103, 1, @now, null, null, '收银出库');

insert into dzg_notification (
    notice_id, tenant_id, notice_type, notice_title, notice_content, biz_type, biz_id, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time
) values
(29200001, @tenant_id, 'stock', '可口可乐库存偏低', '可口可乐500ml 当前库存14瓶，已低于18瓶预警值。', 'stock', 22000002, '0', '0', 103, 1, @now, null, null),
(29200002, @tenant_id, 'stock', '东北大米需要补货', '东北大米5kg 当前库存7袋，建议今天补货。', 'stock', 22000003, '0', '0', 103, 1, @now, null, null),
(29200003, @tenant_id, 'stock', '鲜鸡蛋即将断货', '鲜鸡蛋30枚 当前库存5盒，低于8盒预警值。', 'stock', 22000010, '0', '0', 103, 1, @now, null, null),
(29200004, @tenant_id, 'credit_unpaid', '张师傅赊账未还', '张师傅还有128.00元未结清，建议今天电话提醒。', 'credit', 29000001, '0', '0', 103, 1, @now, null, null),
(29200005, @tenant_id, 'credit_unpaid', '王建国赊账未还', '王建国还有51.60元未结清，可在下次到店时提醒。', 'credit', 29000002, '0', '0', 103, 1, @now, null, null),
(29200006, @tenant_id, 'credit_unpaid', '郑阿姨月底结算', '郑阿姨还有55.00元未结清，月底结算前需要确认。', 'credit', 29000006, '0', '0', 103, 1, @now, null, null),
(29200007, @tenant_id, 'suggestion', '优先补基础货', '低库存集中在饮料、粮油和生鲜，建议优先补高频刚需品。', 'report', 0, '0', '0', 103, 1, @now, null, null),
(29200008, @tenant_id, 'suggestion', '赊账金额可控但需跟进', '当前未还赊账320.20元，建议先联系金额较大的客户。', 'credit', 0, '0', '0', 103, 1, @now, null, null),
(29200009, @tenant_id, 'stock', '公牛插排库存偏低', '公牛三位插排1.8米 当前库存3个，建议随下次采购补货。', 'stock', 22000014, '0', '0', 103, 1, @now, null, null),
(29200010, @tenant_id, 'suggestion', '热销商品放前排', '农夫山泉、文具和纸品近期动销快，可放在收银台常用商品区。', 'cashier', 0, '0', '0', 103, 1, @now, null, null);

commit;

select 'category' as data_type, count(*) as total from dzg_category where category_id between 21001001 and 21001099
union all select 'supplier', count(*) from dzg_supplier where supplier_id between 23001001 and 23001099
union all select 'product', count(*) from dzg_product where product_id between 22000000 and 22099999
union all select 'stock', count(*) from dzg_stock where stock_id between 24000000 and 24099999
union all select 'customer', count(*) from dzg_customer where customer_id between 25000001 and 25000099
union all select 'purchase_order', count(*) from dzg_purchase_order where purchase_id between 26000000 and 26099999
union all select 'order', count(*) from dzg_order where order_id between 28000000 and 28099999
union all select 'credit_record', count(*) from dzg_credit_record where credit_id between 29000000 and 29099999
union all select 'repayment', count(*) from dzg_repayment where repayment_id between 29100000 and 29199999
union all select 'notification', count(*) from dzg_notification where notice_id between 29200000 and 29299999;
