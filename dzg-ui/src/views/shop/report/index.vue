<template>
  <div class="shop-page">
    <div class="shop-title">
      <div>
        <h2>经营报表</h2>
        <p>{{ selectedReportRangeLabel }}经营数据，当前欠款和库存预警单独展示。</p>
      </div>
      <div class="report-actions">
        <el-radio-group v-model="reportRange" class="report-range" size="small" @change="refreshReport(false)">
          <el-radio-button v-for="item in reportRangeOptions" :key="item.value" :label="item.value">
            {{ item.label }}
          </el-radio-button>
        </el-radio-group>
        <el-button class="primary-action" icon="Refresh" :loading="loading || aiLoading" @click="refreshReport(true)">刷新</el-button>
      </div>
    </div>

    <section class="metric-grid">
      <div class="metric-card primary">
        <span>{{ selectedReportRangeLabel }}销售额</span>
        <strong>￥{{ money(data.salesTotal) }}</strong>
      </div>
      <div class="metric-card">
        <span>{{ selectedReportRangeLabel }}订单数</span>
        <strong>{{ data.orderCount }}</strong>
      </div>
      <div class="metric-card profit">
        <span>{{ selectedReportRangeLabel }}粗略毛利</span>
        <strong>￥{{ money(data.grossProfit) }}</strong>
      </div>
      <div class="metric-card warning">
        <span>{{ selectedReportRangeLabel }}赊账额</span>
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
          <p>{{ aiAnalysis.summary || (aiLoading ? '正在根据销售、库存和赊账自动生成经营建议。' : '打开经营报表后会自动生成经营建议。') }}</p>
        </div>
        <div class="ai-panel__actions">
          <el-tag :type="sourceTagType" size="large">{{ sourceText }}</el-tag>
          <el-button class="action-button" icon="Refresh" :loading="aiLoading" @click="loadAiAnalysis(true)">重新分析</el-button>
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

<script setup name="ShopReport" lang="ts">
import { getShopAiAnalysis, getShopReport } from '@/api/shop';
import { ShopAiAnalysis, ShopReport } from '@/api/shop/types';

const router = useRouter();
const loading = ref(false);
const aiLoading = ref(false);
const reportRangeOptions = [
  { label: '今日', value: 'today' },
  { label: '近7天', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '全部', value: 'all' }
];
const reportRange = ref('today');
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
const selectedReportRangeLabel = computed(() => reportRangeOptions.find((item) => item.value === reportRange.value)?.label || '今日');

const loadReport = async () => {
  loading.value = true;
  try {
    const res = await getShopReport(reportRange.value);
    data.value = res.data;
  } finally {
    loading.value = false;
  }
};

const loadAiAnalysis = async (force = false) => {
  aiLoading.value = true;
  try {
    const res = await getShopAiAnalysis(force);
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

const refreshReport = async (forceAi = false) => {
  await Promise.all([loadReport(), loadAiAnalysis(forceAi)]);
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
const sourceTagType = computed(() => (aiAnalysis.value.generatedByAi ? 'success' : 'info'));
const sourceText = computed(() => (aiAnalysis.value.generatedByAi ? 'DeepSeek分析' : '本地分析'));

onMounted(refreshReport);
</script>

<style scoped>
.ai-panel__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.report-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.report-range :deep(.el-radio-button__inner) {
  min-height: 34px;
  padding: 8px 13px;
  border-color: var(--dzg-shop-border);
  background: color-mix(in srgb, var(--dzg-shop-surface) 86%, var(--dzg-shop-gold-weak));
  color: var(--dzg-shop-text);
  font-weight: 800;
}

.report-range :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  border-color: var(--dzg-shop-wood-dark);
  background: linear-gradient(180deg, #d88a34, var(--dzg-shop-wood));
  color: #fff6d7;
  box-shadow: none;
}

@media (max-width: 640px) {
  .report-actions {
    width: 100%;
    justify-content: stretch;
  }

  .report-range {
    width: 100%;
  }

  .report-range :deep(.el-radio-button) {
    width: 25%;
  }

  .report-range :deep(.el-radio-button__inner) {
    width: 100%;
    padding-right: 4px;
    padding-left: 4px;
  }
}
</style>
