<template>
  <div class="shop-page">
    <div class="shop-title">
      <div>
        <h2>销售记录</h2>
        <p>查看每天卖了多少单、收了多少钱。</p>
      </div>
      <el-button class="primary-action" icon="Refresh" @click="loadOrders">刷新</el-button>
    </div>

    <div class="toolbar">
      <el-select v-model="query.payType" clearable placeholder="收款方式">
        <el-option label="现金" value="cash" />
        <el-option label="微信" value="wechat" />
        <el-option label="支付宝" value="alipay" />
        <el-option label="赊账" value="credit" />
      </el-select>
      <el-button class="action-button" icon="Search" @click="loadOrders">查询</el-button>
    </div>

    <el-table v-loading="loading" border :data="orders">
      <el-table-column label="订单号" prop="orderNo" min-width="180" />
      <el-table-column label="订单金额" prop="totalAmount" width="130">
        <template #default="{ row }">￥{{ money(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="实收金额" prop="paidAmount" width="130">
        <template #default="{ row }">￥{{ money(row.paidAmount) }}</template>
      </el-table-column>
      <el-table-column label="收款方式" prop="payType" width="110">
        <template #default="{ row }">{{ payTypeText(row.payType) }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="payStatus" width="110">
        <template #default="{ row }">
          <el-tag :type="row.payStatus === 'paid' ? 'success' : 'warning'" size="large">{{ row.payStatus === 'paid' ? '已收款' : '未结清' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" prop="orderTime" min-width="170" />
      <el-table-column label="备注" prop="remark" min-width="160" />
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadOrders" />
  </div>
</template>

<script setup name="ShopOrder" lang="ts">
import { listOrder } from '@/api/shop';

const loading = ref(false);
const orders = ref<any[]>([]);
const total = ref(0);
const query = reactive({ pageNum: 1, pageSize: 10, payType: '' });

const money = (value?: number) => Number(value || 0).toFixed(2);
const payTypeText = (value?: string) => ({ cash: '现金', wechat: '微信', alipay: '支付宝', credit: '赊账' }[value || ''] || value || '-');

const loadOrders = async () => {
  loading.value = true;
  const res = await listOrder(query);
  orders.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

onMounted(loadOrders);
</script>
