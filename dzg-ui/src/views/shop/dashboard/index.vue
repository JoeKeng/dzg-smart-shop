<template>
  <div class="shop-page">
    <div class="shop-title">
      <div>
        <h2>首页看板</h2>
        <p>今天店里的经营情况</p>
      </div>
      <el-button class="primary-action" type="primary" icon="Refresh" @click="loadData">刷新</el-button>
    </div>

    <div class="metric-grid">
      <div class="metric-card">
        <span>今日营业额</span>
        <strong>￥{{ money(data.todaySales) }}</strong>
      </div>
      <div class="metric-card">
        <span>今日订单数</span>
        <strong>{{ data.todayOrderCount }}</strong>
      </div>
      <div class="metric-card warn">
        <span>今日赊账</span>
        <strong>￥{{ money(data.todayCredit) }}</strong>
      </div>
      <div class="metric-card danger">
        <span>未还总额</span>
        <strong>￥{{ money(data.unpaidTotal) }}</strong>
      </div>
      <div class="metric-card">
        <span>库存预警</span>
        <strong>{{ data.lowStockCount }}</strong>
      </div>
    </div>

    <div class="quick-grid">
      <el-button type="primary" icon="ShoppingCart" @click="go('/shop/cashier')">去收银</el-button>
      <el-button icon="Goods" @click="go('/shop/product')">管商品</el-button>
      <el-button icon="Box" @click="go('/shop/stock')">看库存</el-button>
      <el-button icon="Wallet" @click="go('/shop/credit')">查赊账</el-button>
    </div>
  </div>
</template>

<script setup name="ShopDashboard" lang="ts">
import { getShopDashboard } from '@/api/shop';
import { ShopDashboard } from '@/api/shop/types';

const router = useRouter();
const data = ref<ShopDashboard>({
  todaySales: 0,
  todayOrderCount: 0,
  todayCredit: 0,
  unpaidTotal: 0,
  lowStockCount: 0
});

const money = (value?: number) => Number(value || 0).toFixed(2);

const loadData = async () => {
  const res = await getShopDashboard();
  data.value = res.data;
};

const go = (path: string) => {
  router.push(path);
};

onMounted(loadData);
</script>

<style scoped>
.shop-page {
  padding: 16px;
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
  color: #1f2a37;
}
.shop-title p {
  margin: 6px 0 0;
  color: #4b5563;
}
.primary-action {
  min-height: 44px;
  font-size: 16px;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 12px;
}
.metric-card {
  min-height: 120px;
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
  font-size: 17px;
}
.metric-card strong {
  font-size: 34px;
  color: #111827;
}
.metric-card.warn strong {
  color: #b45309;
}
.metric-card.danger strong {
  color: #b91c1c;
}
.quick-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}
.quick-grid .el-button {
  width: 100%;
  min-height: 56px;
  margin: 0;
  font-size: 18px;
}
</style>
