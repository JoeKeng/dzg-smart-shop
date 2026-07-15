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

    <section class="ai-panel">
      <div class="ai-panel__head">
        <div>
          <h3>经营分析助手</h3>
          <p>{{ aiAnalysis.summary || '刷新后会根据销售、库存和赊账生成经营建议。' }}</p>
        </div>
        <el-button class="action-button" icon="Refresh" :loading="aiLoading" @click="loadAiAnalysis">重新分析</el-button>
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

<script setup name="ShopReport" lang="ts">
import { getShopAiAnalysis, getShopReport } from '@/api/shop';
import { ShopAiAnalysis, ShopReport } from '@/api/shop/types';

const router = useRouter();
const loading = ref(false);
const aiLoading = ref(false);
const data = ref<ShopReport>({
  salesTotal: 0,
  orderCount: 0,
  creditTotal: 0,
  unpaidTotal: 0,
  grossProfit: 0,
  lowStockCount: 0
});
const aiAnalysis = ref<ShopAiAnalysis>({ summary: '', riskLevel: 'low', insights: [] });

const money = (value?: number) => Number(value || 0).toFixed(2);

const loadReport = async () => {
  loading.value = true;
  try {
    const res = await getShopReport();
    data.value = res.data;
  } finally {
    loading.value = false;
  }
  await loadAiAnalysis();
};

const loadAiAnalysis = async () => {
  aiLoading.value = true;
  try {
    const res = await getShopAiAnalysis();
    aiAnalysis.value = res.data || { summary: '', riskLevel: 'low', insights: [] };
  } finally {
    aiLoading.value = false;
  }
};

const go = (path?: string) => {
  if (path) {
    router.push(path);
  }
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

onMounted(loadReport);
</script>
