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
        <span>今日实收</span>
        <strong>￥{{ money(data.todaySales) }}</strong>
        <small>不含赊账销售</small>
      </div>
      <div class="metric-panel">
        <span>今日订单数</span>
        <strong>{{ data.todayOrderCount }}</strong>
      </div>
      <div class="metric-panel warning">
        <span>今日新增赊账</span>
        <strong>￥{{ money(data.todayCredit) }}</strong>
        <small>按今天赊账订单统计</small>
      </div>
      <div class="metric-panel danger">
        <span>未还余额</span>
        <strong>￥{{ money(data.unpaidTotal) }}</strong>
        <small>所有未结清欠款余额</small>
      </div>
      <div class="metric-panel">
        <span>库存预警</span>
        <strong>{{ data.lowStockCount }}</strong>
      </div>
    </section>

    <section class="chart-grid" aria-label="经营可视化图表">
      <article class="chart-panel">
        <div class="chart-panel__head chart-panel__head--stacked">
          <div>
            <h2>{{ selectedPaymentRangeLabel }}已收与未还</h2>
            <p>按订单时间统计，赊账扣除已还款</p>
          </div>
          <el-radio-group v-model="paymentRange" class="chart-range" size="small" @change="loadPaymentSummary">
            <el-radio-button v-for="item in paymentRangeOptions" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
        <div ref="incomeChartRef" class="chart-canvas" role="img" :aria-label="`${selectedPaymentRangeLabel}已收与未还图表`"></div>
      </article>
      <article class="chart-panel">
        <div class="chart-panel__head">
          <h2>经营提醒概览</h2>
          <p>订单、库存和赊账风险</p>
        </div>
        <div ref="todoChartRef" class="chart-canvas" role="img" aria-label="经营提醒概览图表"></div>
      </article>
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
        <p v-if="Number(data.unpaidTotal) > 0">未还赊账余额合计 ￥{{ money(data.unpaidTotal) }}。</p>
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
import * as echarts from 'echarts/core';
import { BarChart, PieChart } from 'echarts/charts';
import { GraphicComponent, GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { getShopDashboard, getShopPaymentSummary } from '@/api/shop';
import { preloadShopAiBusinessAnalysis } from '@/api/shop/ai-cache';
import { ShopDashboard, ShopPaymentSummary } from '@/api/shop/types';

echarts.use([BarChart, PieChart, GraphicComponent, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

const router = useRouter();
const loading = ref(false);
const hasError = ref(false);
const incomeChartRef = ref<HTMLElement>();
const todoChartRef = ref<HTMLElement>();
let incomeChart: echarts.ECharts | undefined;
let todoChart: echarts.ECharts | undefined;
let themeObserver: MutationObserver | undefined;
const data = ref<ShopDashboard>({
  todaySales: 0,
  todayOrderCount: 0,
  todayCredit: 0,
  unpaidTotal: 0,
  lowStockCount: 0
});
const paymentRangeOptions = [
  { label: '今日', value: 'today' },
  { label: '近7天', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '全部', value: 'all' }
];
const paymentRange = ref('today');
const paymentSummary = ref<ShopPaymentSummary>({
  range: 'today',
  orderCount: 0,
  paidAmount: 0,
  creditAmount: 0,
  repaidCreditAmount: 0,
  unpaidAmount: 0,
  collectedAmount: 0,
  totalAmount: 0
});

const money = (value?: number) => Number(value || 0).toFixed(2);
const selectedPaymentRangeLabel = computed(() => paymentRangeOptions.find((item) => item.value === paymentRange.value)?.label || '今日');

const loadData = async () => {
  loading.value = true;
  hasError.value = false;
  try {
    const [dashboardRes, paymentRes] = await Promise.all([getShopDashboard(), getShopPaymentSummary(paymentRange.value)]);
    data.value = dashboardRes.data;
    paymentSummary.value = paymentRes.data || paymentSummary.value;
  } catch {
    hasError.value = true;
  } finally {
    loading.value = false;
    await nextTick();
    renderCharts();
  }
};

const loadPaymentSummary = async () => {
  hasError.value = false;
  try {
    const res = await getShopPaymentSummary(paymentRange.value);
    paymentSummary.value = res.data || paymentSummary.value;
  } catch {
    hasError.value = true;
  } finally {
    await nextTick();
    renderCharts();
  }
};

const go = (path: string) => {
  router.push(path);
};

const cssVar = (name: string, fallback: string) => getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback;

const renderCharts = () => {
  if (!incomeChartRef.value || !todoChartRef.value) return;

  incomeChart ??= echarts.init(incomeChartRef.value);
  todoChart ??= echarts.init(todoChartRef.value);

  const textColor = cssVar('--dzg-shop-text', '#243126');
  const mutedColor = cssVar('--dzg-shop-muted', '#6d705f');
  const borderColor = cssVar('--dzg-shop-border', '#dccfb8');
  const primaryColor = cssVar('--dzg-shop-primary', '#3f7d58');
  const goldColor = cssVar('--dzg-shop-gold', '#c79a3b');
  const clayColor = cssVar('--dzg-shop-clay', '#a9543f');
  const surfaceColor = cssVar('--dzg-shop-surface', '#fffaf0');

  const paidAmount = Number(paymentSummary.value.paidAmount || 0);
  const creditAmount = Number(paymentSummary.value.creditAmount || 0);
  const repaidCreditAmount = Number(paymentSummary.value.repaidCreditAmount || 0);
  const collectedAmount = Number(paymentSummary.value.collectedAmount ?? paidAmount + repaidCreditAmount);
  const fallbackUnpaidAmount = paymentRange.value === 'all' ? Number(data.value.unpaidTotal || 0) : Math.max(creditAmount - repaidCreditAmount, 0);
  const unpaidAmount = Number(paymentSummary.value.unpaidAmount ?? fallbackUnpaidAmount);
  const incomeTotal = collectedAmount + unpaidAmount;
  const hasIncome = incomeTotal > 0;
  const incomeData = hasIncome
    ? [
        { name: '已收款', value: collectedAmount },
        { name: '未还余额', value: unpaidAmount }
      ]
    : [{ name: '暂无收款', value: 1 }];

  incomeChart.setOption(
    {
      backgroundColor: 'transparent',
      color: hasIncome ? [primaryColor, clayColor] : [borderColor],
      tooltip: {
        trigger: 'item',
        confine: true,
        formatter: (params: any) => {
          if (!hasIncome) {
            return '暂无实收与赊账数据';
          }
          const note = params.name === '已收款' && repaidCreditAmount > 0 ? `<br/>含赊账已还 ￥${money(repaidCreditAmount)}` : '';
          return `${params.name}<br/>￥${money(params.value)} (${params.percent}%)${note}`;
        }
      },
      legend: {
        bottom: 0,
        icon: 'roundRect',
        textStyle: { color: mutedColor }
      },
      graphic: [
        {
          type: 'text',
          left: 'center',
          top: '38%',
          style: {
            text: hasIncome ? '合计' : '暂无',
            fill: mutedColor,
            fontSize: 12,
            fontWeight: 700,
            textAlign: 'center'
          }
        },
        {
          type: 'text',
          left: 'center',
          top: '47%',
          style: {
            text: hasIncome ? `￥${money(incomeTotal)}` : '暂无数据',
            fill: textColor,
            fontSize: hasIncome ? 18 : 15,
            fontWeight: 800,
            textAlign: 'center'
          }
        }
      ],
      series: [
        {
          name: `${selectedPaymentRangeLabel.value}已收与未还`,
          type: 'pie',
          radius: [50, 74],
          center: ['50%', '49%'],
          avoidLabelOverlap: true,
          label: {
            show: false
          },
          labelLine: {
            show: false
          },
          itemStyle: {
            borderColor: surfaceColor,
            borderWidth: 3,
            borderRadius: 6
          },
          data: incomeData
        }
      ]
    },
    true
  );

  const unpaidTotal = Number(data.value.unpaidTotal || 0);
  const todoData = [
    { name: '今日订单', value: Number(data.value.todayOrderCount || 0), amount: 0, itemStyle: { color: primaryColor } },
    { name: '库存预警', value: Number(data.value.lowStockCount || 0), amount: 0, itemStyle: { color: clayColor } },
    { name: '赊账风险', value: unpaidTotal > 0 ? 1 : 0, itemStyle: { color: goldColor }, amount: unpaidTotal }
  ];

  todoChart.setOption(
    {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params: any) => {
          const item = params?.[0];
          if (!item) return '';
          const raw = todoData[item.dataIndex];
          return item.name === '赊账风险' ? `${item.name}<br/>未还 ￥${money(raw.amount)}` : `${item.name}<br/>${item.value}`;
        }
      },
      grid: { left: 46, right: 16, top: 20, bottom: 42 },
      xAxis: {
        type: 'category',
        data: todoData.map((item) => item.name),
        axisTick: { show: false },
        axisLine: { lineStyle: { color: borderColor } },
        axisLabel: { color: mutedColor, interval: 0 }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        splitLine: { lineStyle: { color: borderColor, type: 'dashed' } },
        axisLabel: { color: mutedColor }
      },
      series: [
        {
          name: '经营提醒',
          type: 'bar',
          barMaxWidth: 36,
          data: todoData,
          label: {
            show: true,
            position: 'top',
            color: textColor,
            fontWeight: 700,
            formatter: (params: any) => (params.name === '赊账风险' ? (params.value > 0 ? '需关注' : '正常') : params.value)
          },
          itemStyle: {
            borderRadius: [6, 6, 2, 2]
          }
        }
      ]
    },
    true
  );
};

const resizeCharts = () => {
  incomeChart?.resize();
  todoChart?.resize();
};

const preloadAiAnalysis = () => {
  void preloadShopAiBusinessAnalysis();
};

const scheduleAiPreload = () => {
  if ('requestIdleCallback' in window) {
    window.requestIdleCallback(preloadAiAnalysis, { timeout: 1500 });
    return;
  }
  window.setTimeout(preloadAiAnalysis, 800);
};

onMounted(() => {
  window.addEventListener('resize', resizeCharts);
  themeObserver = new MutationObserver(renderCharts);
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
  loadData();
  scheduleAiPreload();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts);
  themeObserver?.disconnect();
  incomeChart?.dispose();
  todoChart?.dispose();
});
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

.metric-panel small {
  color: var(--dzg-shop-muted);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.3;
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

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.chart-panel {
  min-width: 0;
  padding: 16px;
  border: 2px solid var(--dzg-shop-border);
  border-radius: 8px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--dzg-shop-gold-weak) 42%, transparent), transparent 56%),
    var(--dzg-shop-surface);
  box-shadow: var(--dzg-shop-shadow);
}

.chart-panel__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.chart-panel__head--stacked {
  align-items: flex-start;
}

.chart-panel__head h2 {
  margin: 0;
  color: var(--dzg-shop-text);
  font-size: 22px;
  font-weight: 800;
}

.chart-panel__head p {
  margin: 0;
  color: var(--dzg-shop-muted);
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
}

.chart-range {
  flex: 0 0 auto;
}

.chart-range :deep(.el-radio-button__inner) {
  min-height: 32px;
  padding: 7px 12px;
  border-color: var(--dzg-shop-border);
  background: color-mix(in srgb, var(--dzg-shop-surface) 86%, var(--dzg-shop-gold-weak));
  color: var(--dzg-shop-text);
  font-weight: 800;
}

.chart-range :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  border-color: var(--dzg-shop-wood-dark);
  background: linear-gradient(180deg, #d88a34, var(--dzg-shop-wood));
  color: #fff6d7;
  box-shadow: none;
}

.chart-canvas {
  width: 100%;
  height: 220px;
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

  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .chart-panel__head {
    display: grid;
    grid-template-columns: 1fr;
  }

  .chart-panel__head p {
    white-space: normal;
  }

  .chart-range {
    width: 100%;
  }

  .chart-range :deep(.el-radio-button) {
    width: 25%;
  }

  .chart-range :deep(.el-radio-button__inner) {
    width: 100%;
    padding-right: 4px;
    padding-left: 4px;
  }
}
</style>
