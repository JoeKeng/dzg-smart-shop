<template>
  <div class="shop-page">
    <div class="shop-title">
      <div>
        <h2>经营报表</h2>
        <p>用最少数字看清店里赚没赚、欠款多不多。</p>
      </div>
      <el-button class="primary-action" icon="Refresh" :loading="loading" @click="loadReport">刷新</el-button>
    </div>

    <section class="metric-grid">
      <div class="metric-card primary">
        <span>销售总额</span>
        <strong>￥{{ money(data.salesTotal) }}</strong>
      </div>
      <div class="metric-card">
        <span>订单总数</span>
        <strong>{{ data.orderCount }}</strong>
      </div>
      <div class="metric-card profit">
        <span>粗略毛利</span>
        <strong>￥{{ money(data.grossProfit) }}</strong>
      </div>
      <div class="metric-card warning">
        <span>赊账总额</span>
        <strong>￥{{ money(data.creditTotal) }}</strong>
      </div>
      <div class="metric-card danger">
        <span>未还欠款</span>
        <strong>￥{{ money(data.unpaidTotal) }}</strong>
      </div>
      <div class="metric-card">
        <span>库存预警</span>
        <strong>{{ data.lowStockCount }}</strong>
      </div>
    </section>

    <section class="summary-section">
      <h3>老板提示</h3>
      <p v-if="Number(data.lowStockCount) > 0">有 {{ data.lowStockCount }} 个商品库存偏低，建议先去“采购入库”。</p>
      <p v-else>库存暂时正常，不需要马上补货。</p>
      <p v-if="Number(data.unpaidTotal) > 0">还有 ￥{{ money(data.unpaidTotal) }} 赊账未还，建议去“赊账管理”登记还款。</p>
      <p v-else>当前没有未还赊账。</p>
    </section>
  </div>
</template>

<script setup name="ShopReport" lang="ts">
import { getShopReport } from '@/api/shop';
import { ShopReport } from '@/api/shop/types';

const loading = ref(false);
const data = ref<ShopReport>({
  salesTotal: 0,
  orderCount: 0,
  creditTotal: 0,
  unpaidTotal: 0,
  grossProfit: 0,
  lowStockCount: 0
});

const money = (value?: number) => Number(value || 0).toFixed(2);

const loadReport = async () => {
  loading.value = true;
  try {
    const res = await getShopReport();
    data.value = res.data;
  } finally {
    loading.value = false;
  }
};

onMounted(loadReport);
</script>

<style scoped>
.shop-page {
  padding: 16px;
  background: #f5f7fb;
  font-size: 16px;
}
.shop-title {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}
.shop-title h2 {
  margin: 0;
  font-size: 28px;
}
.shop-title p {
  margin: 6px 0 0;
  color: #374151;
  font-size: 17px;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 14px;
}
.metric-card {
  min-height: 124px;
  padding: 18px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.metric-card span {
  color: #374151;
  font-size: 18px;
}
.metric-card strong {
  color: #111827;
  font-size: 34px;
}
.metric-card.primary strong,
.metric-card.profit strong {
  color: #0f766e;
}
.metric-card.warning strong {
  color: #b45309;
}
.metric-card.danger strong {
  color: #b91c1c;
}
.summary-section {
  margin-top: 16px;
  padding: 18px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
}
.summary-section h3 {
  margin: 0 0 10px;
  font-size: 22px;
}
.summary-section p {
  margin: 8px 0;
  color: #1f2937;
  font-size: 17px;
}
.primary-action {
  min-height: 44px;
  font-size: 16px;
}
</style>
