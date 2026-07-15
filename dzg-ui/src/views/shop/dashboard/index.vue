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

    <section class="ai-panel">
      <div class="ai-panel__head">
        <div>
          <h3>经营分析助手</h3>
          <p>{{ aiAnalysis.summary || '正在根据店铺经营数据生成分析。' }}</p>
        </div>
        <div class="ai-panel__tags">
          <el-tag :type="sourceTagType" size="large">{{ sourceText }}</el-tag>
          <el-tag :type="riskTagType" size="large">{{ riskText }}</el-tag>
        </div>
      </div>
      <div class="ai-grid">
        <article v-for="item in aiAnalysis.insights" :key="`${item.type}-${item.title}`" class="ai-card">
          <div class="ai-card__title">
            <span>{{ item.title }}</span>
            <el-tag :type="insightTagType(item.level)" effect="plain">{{ insightLevelText(item.level) }}</el-tag>
          </div>
          <p>{{ item.content }}</p>
          <el-button v-if="item.actionPath" class="ai-card__action" link type="primary" icon="Position" @click="go(item.actionPath)">
            {{ item.actionText || '查看' }}
          </el-button>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup name="ShopDashboard" lang="ts">
import { getShopAiAnalysis, getShopDashboard } from '@/api/shop';
import { ShopAiAnalysis, ShopDashboard } from '@/api/shop/types';

const router = useRouter();
const data = ref<ShopDashboard>({
  todaySales: 0,
  todayOrderCount: 0,
  todayCredit: 0,
  unpaidTotal: 0,
  lowStockCount: 0
});
const aiAnalysis = ref<ShopAiAnalysis>({
  summary: '',
  riskLevel: 'low',
  insights: []
});

const money = (value?: number) => Number(value || 0).toFixed(2);
const riskTagType = computed(() => {
  if (aiAnalysis.value.riskLevel === 'high') return 'danger';
  if (aiAnalysis.value.riskLevel === 'medium') return 'warning';
  return 'success';
});
const riskText = computed(() => {
  if (aiAnalysis.value.riskLevel === 'high') return '需要优先处理';
  if (aiAnalysis.value.riskLevel === 'medium') return '需要关注';
  return '经营平稳';
});
const sourceTagType = computed(() => (aiAnalysis.value.generatedByAi ? 'success' : 'info'));
const sourceText = computed(() => (aiAnalysis.value.generatedByAi ? 'DeepSeek分析' : '本地分析'));

const loadData = async () => {
  const res = await getShopDashboard();
  data.value = res.data;
  const aiRes = await getShopAiAnalysis();
  aiAnalysis.value = aiRes.data || { summary: '', riskLevel: 'low', insights: [] };
};

const go = (path: string) => {
  router.push(path);
};

const insightTagType = (level?: string) => {
  if (level === 'danger') return 'danger';
  if (level === 'warning') return 'warning';
  if (level === 'success') return 'success';
  return 'info';
};

const insightLevelText = (level?: string) => {
  if (level === 'danger') return '风险';
  if (level === 'warning') return '提醒';
  if (level === 'success') return '正常';
  return '建议';
};

onMounted(loadData);
</script>

<style scoped>
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
  font-size: 16px;
}
.ai-panel__tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}
</style>
