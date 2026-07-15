-- 店掌柜真实感演示数据：生鲜便利店/小超市
-- 用途：填充水果蔬菜、饮料乳品、粮油调味、方便速食、纸品日用等主流商品，并关联 OSS 图片记录。
-- 图片文件：script/assets/dzg-shop-products/*.svg
-- 上传建议：把上述图片上传到 OSS 的 images/dzg-shop/products/ 目录，再执行本 SQL。
-- 说明：本脚本使用固定演示 ID 段，重复执行会先清理这些演示数据；不要在生产库执行。

set @tenant_id = '000000';
set @now = sysdate();
set @oss_base = 'https://ruoyi-yibin-hovoy.oss-cn-chengdu.aliyuncs.com/images/dzg-shop/products/';

use dzg_cloud;

delete from sys_oss where oss_id between 1930000000000002001 and 1930000000000002018;

insert into sys_oss (
    oss_id, tenant_id, file_name, original_name, file_suffix, url, ext1,
    create_dept, create_time, create_by, update_time, update_by, service
) values
(1930000000000002001, @tenant_id, 'images/dzg-shop/products/fuji-apple.svg', '红富士苹果.svg', '.svg', concat(@oss_base, 'fuji-apple.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002002, @tenant_id, 'images/dzg-shop/products/banana.svg', '海南香蕉.svg', '.svg', concat(@oss_base, 'banana.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002003, @tenant_id, 'images/dzg-shop/products/orange.svg', '赣南脐橙.svg', '.svg', concat(@oss_base, 'orange.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002004, @tenant_id, 'images/dzg-shop/products/tomato.svg', '本地西红柿.svg', '.svg', concat(@oss_base, 'tomato.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002005, @tenant_id, 'images/dzg-shop/products/cucumber.svg', '新鲜黄瓜.svg', '.svg', concat(@oss_base, 'cucumber.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002006, @tenant_id, 'images/dzg-shop/products/potato.svg', '黄心土豆.svg', '.svg', concat(@oss_base, 'potato.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002007, @tenant_id, 'images/dzg-shop/products/cabbage.svg', '大白菜.svg', '.svg', concat(@oss_base, 'cabbage.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002008, @tenant_id, 'images/dzg-shop/products/mineral-water.svg', '矿泉水550ml.svg', '.svg', concat(@oss_base, 'mineral-water.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002009, @tenant_id, 'images/dzg-shop/products/coca-cola.svg', '可乐500ml.svg', '.svg', concat(@oss_base, 'coca-cola.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002010, @tenant_id, 'images/dzg-shop/products/milk.svg', '纯牛奶250ml.svg', '.svg', concat(@oss_base, 'milk.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002011, @tenant_id, 'images/dzg-shop/products/rice.svg', '东北大米5kg.svg', '.svg', concat(@oss_base, 'rice.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002012, @tenant_id, 'images/dzg-shop/products/cooking-oil.svg', '压榨菜籽油1.8L.svg', '.svg', concat(@oss_base, 'cooking-oil.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002013, @tenant_id, 'images/dzg-shop/products/soy-sauce.svg', '海天生抽500ml.svg', '.svg', concat(@oss_base, 'soy-sauce.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002014, @tenant_id, 'images/dzg-shop/products/noodles.svg', '红烧牛肉面.svg', '.svg', concat(@oss_base, 'noodles.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002015, @tenant_id, 'images/dzg-shop/products/eggs.svg', '鲜鸡蛋30枚.svg', '.svg', concat(@oss_base, 'eggs.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002016, @tenant_id, 'images/dzg-shop/products/tissue.svg', '抽纸3包.svg', '.svg', concat(@oss_base, 'tissue.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002017, @tenant_id, 'images/dzg-shop/products/soap.svg', '香皂115g.svg', '.svg', concat(@oss_base, 'soap.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun'),
(1930000000000002018, @tenant_id, 'images/dzg-shop/products/battery.svg', '5号电池2粒.svg', '.svg', concat(@oss_base, 'battery.svg'), '{"contentType":"image/svg+xml"}', 103, @now, 1, @now, 1, 'aliyun');

use dzg_shop;

start transaction;

delete from dzg_repayment where repayment_id between 49100000 and 49199999;
delete from dzg_credit_record where credit_id between 49000000 and 49099999;
delete from dzg_order_item where item_id between 48100000 and 48199999;
delete from dzg_order where order_id between 48000000 and 48099999;
delete from dzg_purchase_item where item_id between 46100000 and 46199999;
delete from dzg_purchase_order where purchase_id between 46000000 and 46099999;
delete from dzg_stock_log where log_id between 44100000 and 44199999;
delete from dzg_stock where stock_id between 44000000 and 44099999;
delete from dzg_product_supplier where id between 43100000 and 43199999;
delete from dzg_notification where notice_id between 49200000 and 49299999;
delete from dzg_product where product_id between 42000000 and 42099999;
delete from dzg_supplier where supplier_id between 43001001 and 43001099;
delete from dzg_customer where customer_id between 45000001 and 45000099;
delete from dzg_category where category_id between 41001001 and 41001099;

insert into dzg_category (
    category_id, tenant_id, category_name, sort_order, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(41001001, @tenant_id, '水果蔬菜', 1, '0', '0', 103, 1, @now, null, null, '真实演示分类'),
(41001002, @tenant_id, '饮料乳品', 2, '0', '0', 103, 1, @now, null, null, '真实演示分类'),
(41001003, @tenant_id, '粮油调味', 3, '0', '0', 103, 1, @now, null, null, '真实演示分类'),
(41001004, @tenant_id, '方便速食', 4, '0', '0', 103, 1, @now, null, null, '真实演示分类'),
(41001005, @tenant_id, '清洁纸品', 5, '0', '0', 103, 1, @now, null, null, '真实演示分类'),
(41001006, @tenant_id, '日用百货', 6, '0', '0', 103, 1, @now, null, null, '真实演示分类');

insert into dzg_supplier (
    supplier_id, tenant_id, supplier_name, contact_name, phone, address, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(43001001, @tenant_id, '城南果蔬直配站', '林老板', '13861001001', '城南农贸市场2排18号', '0', '0', 103, 1, @now, null, null, '水果蔬菜每日配送'),
(43001002, @tenant_id, '城北饮料乳品仓', '赵经理', '13861001002', '城北物流园B6仓', '0', '0', 103, 1, @now, null, null, '饮料、牛奶主供'),
(43001003, @tenant_id, '老街粮油配送站', '李师傅', '13861001003', '老街粮油市场8号', '0', '0', 103, 1, @now, null, null, '粮油调味主供'),
(43001004, @tenant_id, '县城方便食品批发部', '王经理', '13861001004', '县城批发市场一区12号', '0', '0', 103, 1, @now, null, null, '方便面、速食'),
(43001005, @tenant_id, '天天纸品洗护商行', '孙老板', '13861001005', '人民路46号', '0', '0', 103, 1, @now, null, null, '纸品洗护'),
(43001006, @tenant_id, '南门百货批发', '吴老板', '13861001006', '南门批发街19号', '0', '0', 103, 1, @now, null, null, '日用百货'),
(43001007, @tenant_id, '清晨水果快线', '周经理', '13861001007', '东郊水果批发区5栋3号', '0', '0', 103, 1, @now, null, null, '水果备用供货'),
(43001008, @tenant_id, '安心鲜蛋冷链', '何经理', '13861001008', '冷链园区C2仓', '0', '0', 103, 1, @now, null, null, '鸡蛋、牛奶冷链'),
(43001009, @tenant_id, '小镇饮料批发中心', '郭老板', '13861001009', '新车站批发街22号', '0', '0', 103, 1, @now, null, null, '饮料备用供货'),
(43001010, @tenant_id, '惠民粮油调味商行', '范老板', '13861001010', '农贸市场西门11号', '0', '0', 103, 1, @now, null, null, '粮油调味备用');

insert into dzg_product (
    product_id, tenant_id, category_id, product_name, barcode, unit_name,
    sale_price, purchase_price, warning_qty, image_url, image_oss_id, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(42000001, @tenant_id, 41001001, '红富士苹果', '692024070001', '斤', 8.80, 5.60, 18, concat(@oss_base, 'fuji-apple.svg'), '1930000000000002001', '0', '0', 103, 1, @now, null, null, '上午进货，下午补架'),
(42000002, @tenant_id, 41001001, '海南香蕉', '692024070002', '斤', 4.50, 2.80, 25, concat(@oss_base, 'banana.svg'), '1930000000000002002', '0', '0', 103, 1, @now, null, null, '周转快，容易损耗'),
(42000003, @tenant_id, 41001001, '赣南脐橙', '692024070003', '斤', 7.20, 4.80, 16, concat(@oss_base, 'orange.svg'), '1930000000000002003', '0', '0', 103, 1, @now, null, null, '水果主推'),
(42000004, @tenant_id, 41001001, '本地西红柿', '692024070004', '斤', 5.60, 3.20, 20, concat(@oss_base, 'tomato.svg'), '1930000000000002004', '0', '0', 103, 1, @now, null, null, '饭店常买'),
(42000005, @tenant_id, 41001001, '新鲜黄瓜', '692024070005', '斤', 4.80, 2.90, 18, concat(@oss_base, 'cucumber.svg'), '1930000000000002005', '0', '0', 103, 1, @now, null, null, '当日蔬菜'),
(42000006, @tenant_id, 41001001, '黄心土豆', '692024070006', '斤', 3.60, 2.00, 30, concat(@oss_base, 'potato.svg'), '1930000000000002006', '0', '0', 103, 1, @now, null, null, '耐放基础菜'),
(42000007, @tenant_id, 41001001, '大白菜', '692024070007', '斤', 2.80, 1.50, 25, concat(@oss_base, 'cabbage.svg'), '1930000000000002007', '0', '0', 103, 1, @now, null, null, '晚饭前动销快'),
(42000008, @tenant_id, 41001002, '矿泉水550ml', '692024070008', '瓶', 2.00, 1.15, 48, concat(@oss_base, 'mineral-water.svg'), '1930000000000002008', '0', '0', 103, 1, @now, null, null, '高频刚需'),
(42000009, @tenant_id, 41001002, '可乐500ml', '692024070009', '瓶', 3.50, 2.35, 36, concat(@oss_base, 'coca-cola.svg'), '1930000000000002009', '0', '0', 103, 1, @now, null, null, '夏季动销'),
(42000010, @tenant_id, 41001002, '纯牛奶250ml', '692024070010', '盒', 4.20, 2.80, 24, concat(@oss_base, 'milk.svg'), '1930000000000002010', '0', '0', 103, 1, @now, null, null, '早餐常购'),
(42000011, @tenant_id, 41001003, '东北大米5kg', '692024070011', '袋', 42.00, 33.00, 8, concat(@oss_base, 'rice.svg'), '1930000000000002011', '0', '0', 103, 1, @now, null, null, '家庭刚需'),
(42000012, @tenant_id, 41001003, '压榨菜籽油1.8L', '692024070012', '桶', 35.90, 29.50, 8, concat(@oss_base, 'cooking-oil.svg'), '1930000000000002012', '0', '0', 103, 1, @now, null, null, '家庭刚需'),
(42000013, @tenant_id, 41001003, '海天生抽500ml', '692024070013', '瓶', 9.90, 6.50, 12, concat(@oss_base, 'soy-sauce.svg'), '1930000000000002013', '0', '0', 103, 1, @now, null, null, '调味品'),
(42000014, @tenant_id, 41001004, '康师傅红烧牛肉面', '692024070014', '袋', 3.20, 2.15, 40, concat(@oss_base, 'noodles.svg'), '1930000000000002014', '0', '0', 103, 1, @now, null, null, '夜间热销'),
(42000015, @tenant_id, 41001001, '鲜鸡蛋30枚', '692024070015', '盒', 29.90, 23.00, 10, concat(@oss_base, 'eggs.svg'), '1930000000000002015', '0', '0', 103, 1, @now, null, null, '冷藏生鲜'),
(42000016, @tenant_id, 41001005, '维达抽纸3包', '692024070016', '提', 16.90, 11.80, 14, concat(@oss_base, 'tissue.svg'), '1930000000000002016', '0', '0', 103, 1, @now, null, null, '家庭日用'),
(42000017, @tenant_id, 41001005, '舒肤佳香皂115g', '692024070017', '块', 5.90, 3.80, 20, concat(@oss_base, 'soap.svg'), '1930000000000002017', '0', '0', 103, 1, @now, null, null, '个人清洁'),
(42000018, @tenant_id, 41001006, '南孚5号电池2粒', '692024070018', '卡', 6.90, 4.40, 16, concat(@oss_base, 'battery.svg'), '1930000000000002018', '0', '0', 103, 1, @now, null, null, '收银台挂架');

insert into dzg_product_supplier (
    id, tenant_id, product_id, supplier_id, sort_order, status, del_flag, create_dept, create_by, create_time, update_by, update_time
) values
(43100001, @tenant_id, 42000001, 43001001, 1, '0', '0', 103, 1, @now, null, null),
(43100002, @tenant_id, 42000002, 43001001, 1, '0', '0', 103, 1, @now, null, null),
(43100003, @tenant_id, 42000003, 43001001, 1, '0', '0', 103, 1, @now, null, null),
(43100004, @tenant_id, 42000004, 43001001, 1, '0', '0', 103, 1, @now, null, null),
(43100005, @tenant_id, 42000005, 43001001, 1, '0', '0', 103, 1, @now, null, null),
(43100006, @tenant_id, 42000006, 43001001, 1, '0', '0', 103, 1, @now, null, null),
(43100007, @tenant_id, 42000007, 43001001, 1, '0', '0', 103, 1, @now, null, null),
(43100008, @tenant_id, 42000008, 43001002, 1, '0', '0', 103, 1, @now, null, null),
(43100009, @tenant_id, 42000009, 43001002, 1, '0', '0', 103, 1, @now, null, null),
(43100010, @tenant_id, 42000010, 43001002, 1, '0', '0', 103, 1, @now, null, null),
(43100011, @tenant_id, 42000011, 43001003, 1, '0', '0', 103, 1, @now, null, null),
(43100012, @tenant_id, 42000012, 43001003, 1, '0', '0', 103, 1, @now, null, null),
(43100013, @tenant_id, 42000013, 43001003, 1, '0', '0', 103, 1, @now, null, null),
(43100014, @tenant_id, 42000014, 43001004, 1, '0', '0', 103, 1, @now, null, null),
(43100015, @tenant_id, 42000015, 43001001, 1, '0', '0', 103, 1, @now, null, null),
(43100016, @tenant_id, 42000016, 43001005, 1, '0', '0', 103, 1, @now, null, null),
(43100017, @tenant_id, 42000017, 43001005, 1, '0', '0', 103, 1, @now, null, null),
(43100018, @tenant_id, 42000018, 43001006, 1, '0', '0', 103, 1, @now, null, null),
(43100019, @tenant_id, 42000002, 43001007, 2, '0', '0', 103, 1, @now, null, null),
(43100020, @tenant_id, 42000003, 43001007, 2, '0', '0', 103, 1, @now, null, null),
(43100021, @tenant_id, 42000010, 43001008, 2, '0', '0', 103, 1, @now, null, null),
(43100022, @tenant_id, 42000015, 43001008, 2, '0', '0', 103, 1, @now, null, null),
(43100023, @tenant_id, 42000008, 43001009, 2, '0', '0', 103, 1, @now, null, null),
(43100024, @tenant_id, 42000009, 43001009, 2, '0', '0', 103, 1, @now, null, null),
(43100025, @tenant_id, 42000011, 43001010, 2, '0', '0', 103, 1, @now, null, null),
(43100026, @tenant_id, 42000012, 43001010, 2, '0', '0', 103, 1, @now, null, null),
(43100027, @tenant_id, 42000013, 43001010, 2, '0', '0', 103, 1, @now, null, null);

insert into dzg_stock (
    stock_id, tenant_id, product_id, quantity, warning_qty, del_flag,
    create_dept, create_by, create_time, update_by, update_time
) values
(44000001, @tenant_id, 42000001, 26, 18, '0', 103, 1, @now, null, null),
(44000002, @tenant_id, 42000002, 19, 25, '0', 103, 1, @now, null, null),
(44000003, @tenant_id, 42000003, 22, 16, '0', 103, 1, @now, null, null),
(44000004, @tenant_id, 42000004, 12, 20, '0', 103, 1, @now, null, null),
(44000005, @tenant_id, 42000005, 10, 18, '0', 103, 1, @now, null, null),
(44000006, @tenant_id, 42000006, 36, 30, '0', 103, 1, @now, null, null),
(44000007, @tenant_id, 42000007, 21, 25, '0', 103, 1, @now, null, null),
(44000008, @tenant_id, 42000008, 72, 48, '0', 103, 1, @now, null, null),
(44000009, @tenant_id, 42000009, 28, 36, '0', 103, 1, @now, null, null),
(44000010, @tenant_id, 42000010, 34, 24, '0', 103, 1, @now, null, null),
(44000011, @tenant_id, 42000011, 6, 8, '0', 103, 1, @now, null, null),
(44000012, @tenant_id, 42000012, 9, 8, '0', 103, 1, @now, null, null),
(44000013, @tenant_id, 42000013, 18, 12, '0', 103, 1, @now, null, null),
(44000014, @tenant_id, 42000014, 58, 40, '0', 103, 1, @now, null, null),
(44000015, @tenant_id, 42000015, 7, 10, '0', 103, 1, @now, null, null),
(44000016, @tenant_id, 42000016, 20, 14, '0', 103, 1, @now, null, null),
(44000017, @tenant_id, 42000017, 24, 20, '0', 103, 1, @now, null, null),
(44000018, @tenant_id, 42000018, 12, 16, '0', 103, 1, @now, null, null);

insert into dzg_customer (
    customer_id, tenant_id, customer_name, phone, credit_limit, current_debt, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(45000001, @tenant_id, '王建国', '13961002001', 500.00, 77.90, '0', '0', 103, 1, @now, null, null, '村东头常客，常买粮油'),
(45000002, @tenant_id, '李桂兰', '13961002002', 300.00, 0.00, '0', '0', 103, 1, @now, null, null, '现金结算'),
(45000003, @tenant_id, '张师傅', '13961002003', 1000.00, 154.20, '0', '0', 103, 1, @now, null, null, '小饭馆采购蔬菜'),
(45000004, @tenant_id, '刘小梅', '13961002004', 300.00, 40.80, '0', '0', 103, 1, @now, null, null, '社区熟客'),
(45000005, @tenant_id, '陈立军', '13961002005', 500.00, 0.00, '0', '0', 103, 1, @now, null, null, '微信付款'),
(45000006, @tenant_id, '周阿姨', '13961002006', 300.00, 45.80, '0', '0', 103, 1, @now, null, null, '月底结算'),
(45000007, @tenant_id, '赵大爷', '13961002007', 300.00, 0.00, '0', '0', 103, 1, @now, null, null, '常买鸡蛋水果'),
(45000008, @tenant_id, '何老板', '13961002008', 1200.00, 65.60, '0', '0', 103, 1, @now, null, null, '早餐店老板'),
(45000009, @tenant_id, '杨丽', '13961002009', 300.00, 0.00, '0', '0', 103, 1, @now, null, null, '年轻客户，支付宝'),
(45000010, @tenant_id, '马老师', '13961002010', 300.00, 0.00, '0', '0', 103, 1, @now, null, null, '学校老师'),
(45000011, @tenant_id, '孙明', '13961002011', 200.00, 0.00, '0', '0', 103, 1, @now, null, null, '学生家长'),
(45000012, @tenant_id, '郑阿姨', '13961002012', 400.00, 0.00, '0', '0', 103, 1, @now, null, null, '常买纸品日化');

insert into dzg_purchase_order (
    purchase_id, tenant_id, purchase_no, supplier_id, total_amount, status, purchase_time, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(46000001, @tenant_id, 'CG202607150101', 43001001, 872.00, 'done', date_sub(@now, interval 5 day), '0', 103, 1, @now, null, null, '果蔬批量补货'),
(46000002, @tenant_id, 'CG202607150102', 43001002, 386.50, 'done', date_sub(@now, interval 4 day), '0', 103, 1, @now, null, null, '饮料乳品补货'),
(46000003, @tenant_id, 'CG202607150103', 43001003, 486.00, 'done', date_sub(@now, interval 3 day), '0', 103, 1, @now, null, null, '粮油调味补货'),
(46000004, @tenant_id, 'CG202607150104', 43001004, 215.00, 'done', date_sub(@now, interval 2 day), '0', 103, 1, @now, null, null, '方便面补货'),
(46000005, @tenant_id, 'CG202607150105', 43001005, 351.40, 'done', date_sub(@now, interval 1 day), '0', 103, 1, @now, null, null, '纸品洗护补货'),
(46000006, @tenant_id, 'CG202607150106', 43001006, 176.00, 'done', date_sub(@now, interval 1 day), '0', 103, 1, @now, null, null, '收银台小百货补货'),
(46000007, @tenant_id, 'CG202607150107', 43001007, 334.20, 'done', date_sub(@now, interval 18 hour), '0', 103, 1, @now, null, null, '水果备用渠道补货'),
(46000008, @tenant_id, 'CG202607150108', 43001008, 399.60, 'done', date_sub(@now, interval 12 hour), '0', 103, 1, @now, null, null, '鸡蛋牛奶冷链补货'),
(46000009, @tenant_id, 'CG202607150109', 43001009, 330.00, 'pending', date_sub(@now, interval 3 hour), '0', 103, 1, @now, null, null, '明早饮料预订单'),
(46000010, @tenant_id, 'CG202607150110', 43001010, 474.80, 'pending', date_sub(@now, interval 2 hour), '0', 103, 1, @now, null, null, '周末粮油备货');

insert into dzg_purchase_item (
    item_id, tenant_id, purchase_id, product_id, product_name, quantity, purchase_price, subtotal, del_flag,
    create_dept, create_by, create_time, update_by, update_time
) values
(46100001, @tenant_id, 46000001, 42000001, '红富士苹果', 40, 5.60, 224.00, '0', 103, 1, @now, null, null),
(46100002, @tenant_id, 46000001, 42000002, '海南香蕉', 45, 2.80, 126.00, '0', 103, 1, @now, null, null),
(46100003, @tenant_id, 46000001, 42000004, '本地西红柿', 55, 3.20, 176.00, '0', 103, 1, @now, null, null),
(46100004, @tenant_id, 46000001, 42000005, '新鲜黄瓜', 40, 2.90, 116.00, '0', 103, 1, @now, null, null),
(46100005, @tenant_id, 46000001, 42000015, '鲜鸡蛋30枚', 10, 23.00, 230.00, '0', 103, 1, @now, null, null),
(46100006, @tenant_id, 46000002, 42000008, '矿泉水550ml', 120, 1.15, 138.00, '0', 103, 1, @now, null, null),
(46100007, @tenant_id, 46000002, 42000009, '可乐500ml', 70, 2.35, 164.50, '0', 103, 1, @now, null, null),
(46100008, @tenant_id, 46000002, 42000010, '纯牛奶250ml', 30, 2.80, 84.00, '0', 103, 1, @now, null, null),
(46100009, @tenant_id, 46000003, 42000011, '东北大米5kg', 6, 33.00, 198.00, '0', 103, 1, @now, null, null),
(46100010, @tenant_id, 46000003, 42000012, '压榨菜籽油1.8L', 8, 29.50, 236.00, '0', 103, 1, @now, null, null),
(46100011, @tenant_id, 46000003, 42000013, '海天生抽500ml', 8, 6.50, 52.00, '0', 103, 1, @now, null, null),
(46100012, @tenant_id, 46000004, 42000014, '康师傅红烧牛肉面', 100, 2.15, 215.00, '0', 103, 1, @now, null, null),
(46100013, @tenant_id, 46000005, 42000016, '维达抽纸3包', 18, 11.80, 212.40, '0', 103, 1, @now, null, null),
(46100014, @tenant_id, 46000005, 42000017, '舒肤佳香皂115g', 25, 3.80, 95.00, '0', 103, 1, @now, null, null),
(46100015, @tenant_id, 46000005, 42000018, '南孚5号电池2粒', 10, 4.40, 44.00, '0', 103, 1, @now, null, null),
(46100016, @tenant_id, 46000006, 42000018, '南孚5号电池2粒', 40, 4.40, 176.00, '0', 103, 1, @now, null, null),
(46100017, @tenant_id, 46000007, 42000002, '海南香蕉', 60, 2.75, 165.00, '0', 103, 1, @now, null, null),
(46100018, @tenant_id, 46000007, 42000003, '赣南脐橙', 36, 4.70, 169.20, '0', 103, 1, @now, null, null),
(46100019, @tenant_id, 46000008, 42000015, '鲜鸡蛋30枚', 12, 22.50, 270.00, '0', 103, 1, @now, null, null),
(46100020, @tenant_id, 46000008, 42000010, '纯牛奶250ml', 48, 2.70, 129.60, '0', 103, 1, @now, null, null),
(46100021, @tenant_id, 46000009, 42000008, '矿泉水550ml', 120, 1.10, 132.00, '0', 103, 1, @now, null, null),
(46100022, @tenant_id, 46000009, 42000009, '可乐500ml', 90, 2.20, 198.00, '0', 103, 1, @now, null, null),
(46100023, @tenant_id, 46000010, 42000011, '东北大米5kg', 5, 32.00, 160.00, '0', 103, 1, @now, null, null),
(46100024, @tenant_id, 46000010, 42000012, '压榨菜籽油1.8L', 8, 29.00, 232.00, '0', 103, 1, @now, null, null),
(46100025, @tenant_id, 46000010, 42000013, '海天生抽500ml', 12, 6.90, 82.80, '0', 103, 1, @now, null, null);

insert into dzg_order (
    order_id, tenant_id, order_no, customer_id, total_amount, paid_amount, pay_type, pay_status, order_time, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(48000001, @tenant_id, 'XS202607150101', 45000002, 42.30, 42.30, 'cash', 'paid', date_sub(@now, interval 10 hour), '0', 103, 1, @now, null, null, '早市水果蔬菜'),
(48000002, @tenant_id, 'XS202607150102', 45000005, 28.00, 28.00, 'wechat', 'paid', date_sub(@now, interval 9 hour), '0', 103, 1, @now, null, null, '饮料速食'),
(48000003, @tenant_id, 'XS202607150103', 45000001, 77.90, 0.00, 'credit', 'unpaid', date_sub(@now, interval 8 hour), '0', 103, 1, @now, null, null, '熟客粮油赊账'),
(48000004, @tenant_id, 'XS202607150104', 45000009, 22.40, 22.40, 'alipay', 'paid', date_sub(@now, interval 7 hour), '0', 103, 1, @now, null, null, '饮料牛奶'),
(48000005, @tenant_id, 'XS202607150105', 45000004, 40.80, 0.00, 'credit', 'unpaid', date_sub(@now, interval 6 hour), '0', 103, 1, @now, null, null, '蔬菜赊账'),
(48000006, @tenant_id, 'XS202607150106', 45000007, 46.80, 46.80, 'alipay', 'paid', date_sub(@now, interval 5 hour), '0', 103, 1, @now, null, null, '鸡蛋纸品'),
(48000007, @tenant_id, 'XS202607150107', 45000011, 37.40, 37.40, 'cash', 'paid', date_sub(@now, interval 4 hour), '0', 103, 1, @now, null, null, '日用品'),
(48000008, @tenant_id, 'XS202607150108', 45000012, 46.80, 46.80, 'wechat', 'paid', date_sub(@now, interval 3 hour), '0', 103, 1, @now, null, null, '水果'),
(48000009, @tenant_id, 'XS202607150109', 45000003, 154.20, 0.00, 'credit', 'unpaid', date_sub(@now, interval 2 hour), '0', 103, 1, @now, null, null, '饭店蔬菜赊账'),
(48000010, @tenant_id, 'XS202607150110', 45000010, 45.00, 45.00, 'cash', 'paid', date_sub(@now, interval 100 minute), '0', 103, 1, @now, null, null, '饮料'),
(48000011, @tenant_id, 'XS202607150111', 45000005, 51.80, 51.80, 'alipay', 'paid', date_sub(@now, interval 80 minute), '0', 103, 1, @now, null, null, '调味速食'),
(48000012, @tenant_id, 'XS202607150112', 45000006, 75.80, 30.00, 'credit', 'partial', date_sub(@now, interval 60 minute), '0', 103, 1, @now, null, null, '部分还款'),
(48000013, @tenant_id, 'XS202607150113', 45000009, 23.80, 23.80, 'wechat', 'paid', date_sub(@now, interval 40 minute), '0', 103, 1, @now, null, null, '饮料牛奶'),
(48000014, @tenant_id, 'XS202607150114', 45000008, 65.60, 0.00, 'credit', 'unpaid', date_sub(@now, interval 25 minute), '0', 103, 1, @now, null, null, '早餐店水果赊账');

insert into dzg_order_item (
    item_id, tenant_id, order_id, product_id, product_name, quantity, sale_price, subtotal, del_flag,
    create_dept, create_by, create_time, update_by, update_time
) values
(48100001, @tenant_id, 48000001, 42000001, '红富士苹果', 2, 8.80, 17.60, '0', 103, 1, @now, null, null),
(48100002, @tenant_id, 48000001, 42000002, '海南香蕉', 3, 4.50, 13.50, '0', 103, 1, @now, null, null),
(48100003, @tenant_id, 48000001, 42000004, '本地西红柿', 2, 5.60, 11.20, '0', 103, 1, @now, null, null),
(48100004, @tenant_id, 48000002, 42000008, '矿泉水550ml', 6, 2.00, 12.00, '0', 103, 1, @now, null, null),
(48100005, @tenant_id, 48000002, 42000014, '康师傅红烧牛肉面', 5, 3.20, 16.00, '0', 103, 1, @now, null, null),
(48100006, @tenant_id, 48000003, 42000011, '东北大米5kg', 1, 42.00, 42.00, '0', 103, 1, @now, null, null),
(48100007, @tenant_id, 48000003, 42000012, '压榨菜籽油1.8L', 1, 35.90, 35.90, '0', 103, 1, @now, null, null),
(48100008, @tenant_id, 48000004, 42000009, '可乐500ml', 4, 3.50, 14.00, '0', 103, 1, @now, null, null),
(48100009, @tenant_id, 48000004, 42000010, '纯牛奶250ml', 2, 4.20, 8.40, '0', 103, 1, @now, null, null),
(48100010, @tenant_id, 48000005, 42000005, '新鲜黄瓜', 3, 4.80, 14.40, '0', 103, 1, @now, null, null),
(48100011, @tenant_id, 48000005, 42000006, '黄心土豆', 5, 3.60, 18.00, '0', 103, 1, @now, null, null),
(48100012, @tenant_id, 48000005, 42000007, '大白菜', 3, 2.80, 8.40, '0', 103, 1, @now, null, null),
(48100013, @tenant_id, 48000006, 42000015, '鲜鸡蛋30枚', 1, 29.90, 29.90, '0', 103, 1, @now, null, null),
(48100014, @tenant_id, 48000006, 42000016, '维达抽纸3包', 1, 16.90, 16.90, '0', 103, 1, @now, null, null),
(48100015, @tenant_id, 48000007, 42000017, '舒肤佳香皂115g', 4, 5.90, 23.60, '0', 103, 1, @now, null, null),
(48100016, @tenant_id, 48000007, 42000018, '南孚5号电池2粒', 2, 6.90, 13.80, '0', 103, 1, @now, null, null),
(48100017, @tenant_id, 48000008, 42000003, '赣南脐橙', 4, 7.20, 28.80, '0', 103, 1, @now, null, null),
(48100018, @tenant_id, 48000008, 42000002, '海南香蕉', 4, 4.50, 18.00, '0', 103, 1, @now, null, null),
(48100019, @tenant_id, 48000009, 42000004, '本地西红柿', 10, 5.60, 56.00, '0', 103, 1, @now, null, null),
(48100020, @tenant_id, 48000009, 42000005, '新鲜黄瓜', 8, 4.80, 38.40, '0', 103, 1, @now, null, null),
(48100021, @tenant_id, 48000009, 42000015, '鲜鸡蛋30枚', 2, 29.90, 59.80, '0', 103, 1, @now, null, null),
(48100022, @tenant_id, 48000010, 42000008, '矿泉水550ml', 12, 2.00, 24.00, '0', 103, 1, @now, null, null),
(48100023, @tenant_id, 48000010, 42000009, '可乐500ml', 6, 3.50, 21.00, '0', 103, 1, @now, null, null),
(48100024, @tenant_id, 48000011, 42000013, '海天生抽500ml', 2, 9.90, 19.80, '0', 103, 1, @now, null, null),
(48100025, @tenant_id, 48000011, 42000014, '康师傅红烧牛肉面', 10, 3.20, 32.00, '0', 103, 1, @now, null, null),
(48100026, @tenant_id, 48000012, 42000011, '东北大米5kg', 1, 42.00, 42.00, '0', 103, 1, @now, null, null),
(48100027, @tenant_id, 48000012, 42000016, '维达抽纸3包', 2, 16.90, 33.80, '0', 103, 1, @now, null, null),
(48100028, @tenant_id, 48000013, 42000010, '纯牛奶250ml', 4, 4.20, 16.80, '0', 103, 1, @now, null, null),
(48100029, @tenant_id, 48000013, 42000009, '可乐500ml', 2, 3.50, 7.00, '0', 103, 1, @now, null, null),
(48100030, @tenant_id, 48000014, 42000001, '红富士苹果', 5, 8.80, 44.00, '0', 103, 1, @now, null, null),
(48100031, @tenant_id, 48000014, 42000003, '赣南脐橙', 3, 7.20, 21.60, '0', 103, 1, @now, null, null);

insert into dzg_credit_record (
    credit_id, tenant_id, order_id, customer_id, credit_amount, paid_amount, unpaid_amount, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(49000001, @tenant_id, 48000003, 45000001, 77.90, 0.00, 77.90, 'unpaid', '0', 103, 1, @now, null, null, '粮油赊账'),
(49000002, @tenant_id, 48000005, 45000004, 40.80, 0.00, 40.80, 'unpaid', '0', 103, 1, @now, null, null, '蔬菜赊账'),
(49000003, @tenant_id, 48000009, 45000003, 154.20, 0.00, 154.20, 'unpaid', '0', 103, 1, @now, null, null, '饭店采购'),
(49000004, @tenant_id, 48000012, 45000006, 75.80, 30.00, 45.80, 'partial', '0', 103, 1, @now, null, null, '已部分还款'),
(49000005, @tenant_id, 48000014, 45000008, 65.60, 0.00, 65.60, 'unpaid', '0', 103, 1, @now, null, null, '早餐店水果');

insert into dzg_repayment (
    repayment_id, tenant_id, credit_id, customer_id, repayment_amount, pay_type, repayment_time, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(49100001, @tenant_id, 49000004, 45000006, 30.00, 'cash', date_sub(@now, interval 50 minute), '0', 103, 1, @now, null, null, '先还30元');

insert into dzg_stock_log (
    log_id, tenant_id, product_id, change_type, change_qty, before_qty, after_qty, biz_id, del_flag,
    create_dept, create_by, create_time, update_by, update_time, remark
) values
(44100001, @tenant_id, 42000001, 'in', 40, 0, 40, 46000001, '0', 103, 1, @now, null, null, '采购入库'),
(44100002, @tenant_id, 42000002, 'in', 45, 0, 45, 46000001, '0', 103, 1, @now, null, null, '采购入库'),
(44100003, @tenant_id, 42000004, 'in', 55, 0, 55, 46000001, '0', 103, 1, @now, null, null, '采购入库'),
(44100004, @tenant_id, 42000005, 'in', 40, 0, 40, 46000001, '0', 103, 1, @now, null, null, '采购入库'),
(44100005, @tenant_id, 42000008, 'in', 120, 0, 120, 46000002, '0', 103, 1, @now, null, null, '采购入库'),
(44100006, @tenant_id, 42000009, 'in', 70, 0, 70, 46000002, '0', 103, 1, @now, null, null, '采购入库'),
(44100007, @tenant_id, 42000011, 'in', 6, 0, 6, 46000003, '0', 103, 1, @now, null, null, '采购入库'),
(44100008, @tenant_id, 42000014, 'in', 100, 0, 100, 46000004, '0', 103, 1, @now, null, null, '采购入库'),
(44100009, @tenant_id, 42000001, 'out', 7, 33, 26, 48000014, '0', 103, 1, @now, null, null, '收银出库'),
(44100010, @tenant_id, 42000002, 'out', 7, 26, 19, 48000008, '0', 103, 1, @now, null, null, '收银出库'),
(44100011, @tenant_id, 42000004, 'out', 12, 24, 12, 48000009, '0', 103, 1, @now, null, null, '收银出库'),
(44100012, @tenant_id, 42000005, 'out', 11, 21, 10, 48000009, '0', 103, 1, @now, null, null, '收银出库'),
(44100013, @tenant_id, 42000008, 'out', 18, 90, 72, 48000010, '0', 103, 1, @now, null, null, '收银出库'),
(44100014, @tenant_id, 42000009, 'out', 12, 40, 28, 48000013, '0', 103, 1, @now, null, null, '收银出库'),
(44100015, @tenant_id, 42000011, 'out', 2, 8, 6, 48000012, '0', 103, 1, @now, null, null, '收银出库'),
(44100016, @tenant_id, 42000015, 'out', 3, 10, 7, 48000009, '0', 103, 1, @now, null, null, '收银出库'),
(44100017, @tenant_id, 42000016, 'out', 3, 23, 20, 48000012, '0', 103, 1, @now, null, null, '收银出库'),
(44100018, @tenant_id, 42000018, 'out', 2, 14, 12, 48000007, '0', 103, 1, @now, null, null, '收银出库');

insert into dzg_notification (
    notice_id, tenant_id, notice_type, notice_title, notice_content, biz_type, biz_id, status, del_flag,
    create_dept, create_by, create_time, update_by, update_time
) values
(49200001, @tenant_id, 'stock', '海南香蕉需要尽快补货', '海南香蕉当前库存19斤，低于25斤预警值，且损耗较快，建议今天补货。', 'stock', 42000002, '0', '0', 103, 1, @now, null, null),
(49200002, @tenant_id, 'stock', '本地西红柿库存偏低', '本地西红柿当前库存12斤，低于20斤预警值，饭店客户常买。', 'stock', 42000004, '0', '0', 103, 1, @now, null, null),
(49200003, @tenant_id, 'stock', '鲜鸡蛋即将断货', '鲜鸡蛋30枚当前库存7盒，低于10盒预警值。', 'stock', 42000015, '0', '0', 103, 1, @now, null, null),
(49200004, @tenant_id, 'stock', '东北大米库存偏低', '东北大米5kg当前库存6袋，低于8袋预警值，建议随下次粮油补货。', 'stock', 42000011, '0', '0', 103, 1, @now, null, null),
(49200005, @tenant_id, 'credit_unpaid', '张师傅赊账金额较高', '张师傅还有154.20元未结清，建议今天电话确认还款时间。', 'credit', 49000003, '0', '0', 103, 1, @now, null, null),
(49200006, @tenant_id, 'credit_unpaid', '王建国粮油赊账未还', '王建国还有77.90元未结清，可在下次到店时提醒。', 'credit', 49000001, '0', '0', 103, 1, @now, null, null),
(49200007, @tenant_id, 'suggestion', '优先补生鲜和饮料', '低库存集中在水果蔬菜、鸡蛋和可乐，建议优先补高频刚需品。', 'report', 0, '0', '0', 103, 1, @now, null, null),
(49200008, @tenant_id, 'suggestion', '赊账总体可控但要跟进', '当前演示未还赊账384.30元，建议先跟进饭店客户和粮油赊账。', 'credit', 0, '0', '0', 103, 1, @now, null, null),
(49200009, @tenant_id, 'purchase', '明早饮料预订单待确认', '矿泉水和可乐已生成预订单，建议今晚确认到货时间，避免午后断档。', 'purchase', 46000009, '0', '0', 103, 1, @now, null, null),
(49200010, @tenant_id, 'purchase', '周末粮油备货待确认', '大米、菜籽油、生抽周末需求会升高，建议确认惠民粮油调味商行的配送。', 'purchase', 46000010, '0', '0', 103, 1, @now, null, null),
(49200011, @tenant_id, 'stock', '南孚电池低于预警', '南孚5号电池当前库存12卡，低于16卡预警值，可放在收银台补足陈列。', 'stock', 42000018, '0', '0', 103, 1, @now, null, null),
(49200012, @tenant_id, 'operation', '生鲜损耗需要复盘', '香蕉、黄瓜、西红柿属于快周转易损耗商品，建议每天晚间记录报损和打折清仓情况。', 'report', 0, '0', '0', 103, 1, @now, null, null);

commit;

select 'oss' as data_type, count(*) as total from dzg_cloud.sys_oss where oss_id between 1930000000000002001 and 1930000000000002018
union all select 'category', count(*) from dzg_category where category_id between 41001001 and 41001099
union all select 'supplier', count(*) from dzg_supplier where supplier_id between 43001001 and 43001099
union all select 'product', count(*) from dzg_product where product_id between 42000000 and 42099999
union all select 'product_supplier', count(*) from dzg_product_supplier where id between 43100000 and 43199999
union all select 'stock', count(*) from dzg_stock where stock_id between 44000000 and 44099999
union all select 'customer', count(*) from dzg_customer where customer_id between 45000001 and 45000099
union all select 'purchase_order', count(*) from dzg_purchase_order where purchase_id between 46000000 and 46099999
union all select 'purchase_item', count(*) from dzg_purchase_item where item_id between 46100000 and 46199999
union all select 'order', count(*) from dzg_order where order_id between 48000000 and 48099999
union all select 'order_item', count(*) from dzg_order_item where item_id between 48100000 and 48199999
union all select 'credit_record', count(*) from dzg_credit_record where credit_id between 49000000 and 49099999
union all select 'repayment', count(*) from dzg_repayment where repayment_id between 49100000 and 49199999
union all select 'stock_log', count(*) from dzg_stock_log where log_id between 44100000 and 44199999
union all select 'notification', count(*) from dzg_notification where notice_id between 49200000 and 49299999;
