<template>
  <div class="home-page">
    <section class="home-header">
      <div>
        <h1>店掌柜</h1>
        <p>今天经营情况</p>
      </div>
      <el-button class="refresh-button" icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
      <div class="home-header__farm" aria-hidden="true"></div>
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
  padding: 18px;
  background:
    linear-gradient(90deg, color-mix(in srgb, var(--dzg-shop-border) 26%, transparent) 1px, transparent 1px) 0 0 / 24px 24px,
    linear-gradient(180deg, var(--dzg-shop-bg-soft), var(--dzg-shop-bg));
  color: var(--dzg-shop-text);
  font-size: 16px;
}

.home-header {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  min-height: 182px;
  padding: 18px;
  border: 3px solid var(--dzg-shop-wood-dark);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(255, 247, 206, 0.18), rgba(255, 247, 206, 0)),
    url('@/assets/images/dzg-farm-banner.svg') center bottom / cover no-repeat;
  box-shadow: var(--dzg-shop-shadow);
}

.home-header h1 {
  position: relative;
  z-index: 1;
  margin: 0;
  display: inline-flex;
  padding: 10px 18px;
  border: 3px solid var(--dzg-shop-wood-dark);
  border-radius: 8px;
  background: linear-gradient(180deg, #d88a34, var(--dzg-shop-wood));
  color: #fff6d7;
  box-shadow: 0 4px 0 var(--dzg-shop-wood-dark);
  font-size: clamp(28px, 4vw, 42px);
  font-weight: 850;
  line-height: 1.1;
  text-shadow: 2px 2px 0 var(--dzg-shop-wood-dark);
}

.home-header p {
  position: relative;
  z-index: 1;
  margin: 6px 0 0;
  display: inline-flex;
  padding: 6px 12px;
  border: 2px solid var(--dzg-shop-border-strong);
  border-radius: 8px;
  background: var(--dzg-shop-surface);
  color: var(--dzg-shop-text);
  font-size: 18px;
  font-weight: 700;
}

.refresh-button {
  position: relative;
  z-index: 1;
  min-height: 44px;
  padding: 0 22px;
  font-size: 16px;
}

.home-header__farm {
  position: absolute;
  right: 18px;
  bottom: 16px;
  width: 160px;
  height: 70px;
  border-bottom: 10px solid var(--dzg-shop-wood-dark);
  background:
    linear-gradient(90deg, var(--dzg-shop-primary) 0 18px, transparent 18px 28px, var(--dzg-shop-primary) 28px 48px, transparent 48px 58px, var(--dzg-shop-primary) 58px 82px, transparent 82px),
    linear-gradient(180deg, transparent 0 42px, #7a451c 42px 52px, transparent 52px);
  image-rendering: pixelated;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 14px;
}

.metric-panel {
  position: relative;
  min-height: 124px;
  padding: 18px;
  overflow: hidden;
  border: 2px solid var(--dzg-shop-border);
  border-radius: 8px;
  background: var(--dzg-shop-surface);
  box-shadow: var(--dzg-shop-shadow);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.metric-panel::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 7px;
  background: var(--dzg-shop-gold);
}

.metric-panel span {
  color: var(--dzg-shop-muted);
  font-size: 16px;
  font-weight: 700;
}

.metric-panel strong {
  color: var(--dzg-shop-text);
  font-size: 34px;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.metric-panel.primary strong {
  color: var(--dzg-shop-primary);
}

.metric-panel.warning strong {
  color: var(--dzg-shop-gold);
}

.metric-panel.danger strong {
  color: var(--dzg-shop-clay);
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
  border: 2px solid var(--dzg-shop-wood-dark);
  background: linear-gradient(180deg, #d88a34, var(--dzg-shop-wood));
  color: #fff6d7;
  box-shadow: 0 4px 0 var(--dzg-shop-wood-dark);
  font-size: 17px;
  font-weight: 800;
  text-shadow: 1px 1px 0 var(--dzg-shop-wood-dark);
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
  border: 2px solid var(--dzg-shop-border);
  border-radius: 8px;
  background: var(--dzg-shop-surface);
  box-shadow: var(--dzg-shop-shadow);
}

.status-block h2 {
  margin: 0 0 10px;
  color: var(--dzg-shop-text);
  font-size: 22px;
}

.status-block p {
  margin: 8px 0;
  color: var(--dzg-shop-muted);
  font-size: 16px;
  line-height: 1.6;
}

.home-alert {
  margin-top: 18px;
}

@media (max-width: 768px) {
  .home-page {
    padding: 12px;
  }

  .home-header {
    min-height: 150px;
    align-items: flex-start;
  }

  .home-header__farm {
    display: none;
  }
}
</style>
