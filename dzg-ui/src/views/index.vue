<template>
  <div class="home-page">
    <section class="home-header">
      <div>
        <h1>店掌柜</h1>
        <p>今天经营情况</p>
      </div>
      <el-button class="refresh-button" icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
    </section>

    <section class="metric-grid">
      <div class="metric-panel primary">
        <span>今日营业额</span>
        <strong>￥{{ money(data.todaySales) }}</strong>
      </div>
      <div class="metric-panel">
        <span>今日订单数</span>
        <strong>{{ data.todayOrderCount }}</strong>
      </div>
      <div class="metric-panel warning">
        <span>今日赊账</span>
        <strong>￥{{ money(data.todayCredit) }}</strong>
      </div>
      <div class="metric-panel danger">
        <span>未还总额</span>
        <strong>￥{{ money(data.unpaidTotal) }}</strong>
      </div>
      <div class="metric-panel">
        <span>库存预警</span>
        <strong>{{ data.lowStockCount }}</strong>
      </div>
    </section>

    <section class="quick-section">
      <el-button type="primary" icon="ShoppingCart" @click="go('/shop/cashier')">收银台</el-button>
      <el-button icon="Box" @click="go('/shop/purchase')">采购入库</el-button>
      <el-button icon="Wallet" @click="go('/shop/credit')">赊账还款</el-button>
      <el-button icon="List" @click="go('/shop/order')">销售记录</el-button>
      <el-button icon="TrendCharts" @click="go('/shop/report')">经营报表</el-button>
      <el-button icon="Bell" @click="go('/shop/notification')">提醒中心</el-button>
    </section>

    <section class="status-section">
      <div class="status-block">
        <h2>今日重点</h2>
        <p v-if="data.lowStockCount > 0">有 {{ data.lowStockCount }} 个商品库存偏低。</p>
        <p v-else>当前没有库存预警。</p>
        <p v-if="Number(data.unpaidTotal) > 0">未还赊账合计 ￥{{ money(data.unpaidTotal) }}。</p>
        <p v-else>当前没有未还赊账。</p>
      </div>
      <div class="status-block">
        <h2>常用操作</h2>
        <p>常用功能已经放在上方：收银、入库、还款、销售记录和报表。</p>
        <p>赊账收银会自动生成欠款记录。</p>
      </div>
    </section>

    <el-alert v-if="hasError" class="home-alert" type="warning" :closable="false" show-icon title="店铺经营服务暂未返回数据" />
  </div>
</template>

<script setup name="Index" lang="ts">
import { getShopDashboard } from '@/api/shop';
import { ShopDashboard } from '@/api/shop/types';

const router = useRouter();
const loading = ref(false);
const hasError = ref(false);
const data = ref<ShopDashboard>({
  todaySales: 0,
  todayOrderCount: 0,
  todayCredit: 0,
  unpaidTotal: 0,
  lowStockCount: 0
});

const money = (value?: number) => Number(value || 0).toFixed(2);

const loadData = async () => {
  loading.value = true;
  hasError.value = false;
  try {
    const res = await getShopDashboard();
    data.value = res.data;
  } catch {
    hasError.value = true;
  } finally {
    loading.value = false;
  }
};

const go = (path: string) => {
  router.push(path);
};

onMounted(loadData);
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 84px);
  padding: 20px;
  background: #f5f7fb;
  color: #111827;
  font-size: 16px;
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.home-header h1 {
  margin: 0;
  font-size: 34px;
  font-weight: 700;
}

.home-header p {
  margin: 6px 0 0;
  color: #374151;
  font-size: 18px;
}

.refresh-button {
  min-height: 44px;
  padding: 0 22px;
  font-size: 16px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 14px;
}

.metric-panel {
  min-height: 124px;
  padding: 18px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.metric-panel span {
  color: #374151;
  font-size: 18px;
}

.metric-panel strong {
  color: #111827;
  font-size: 34px;
  line-height: 1.1;
}

.metric-panel.primary strong {
  color: #0f766e;
}

.metric-panel.warning strong {
  color: #b45309;
}

.metric-panel.danger strong {
  color: #b91c1c;
}

.quick-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.quick-section .el-button {
  width: 100%;
  min-height: 58px;
  margin: 0;
  font-size: 18px;
}

.status-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.status-block {
  min-height: 132px;
  padding: 18px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
}

.status-block h2 {
  margin: 0 0 10px;
  font-size: 22px;
}

.status-block p {
  margin: 8px 0;
  color: #1f2937;
  font-size: 17px;
}

.home-alert {
  margin-top: 18px;
}
</style>
