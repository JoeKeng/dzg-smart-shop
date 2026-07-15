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

    <section class="chart-grid" aria-label="经营可视化图表">
      <article class="chart-panel">
        <div class="chart-panel__head">
          <h3>今日收款构成</h3>
          <p>实收与赊账占比</p>
        </div>
        <div ref="incomeChartRef" class="chart-canvas" role="img" aria-label="今日收款构成图表"></div>
      </article>
      <article class="chart-panel">
        <div class="chart-panel__head">
          <h3>经营提醒概览</h3>
          <p>订单、库存和建议数量</p>
        </div>
        <div ref="todoChartRef" class="chart-canvas" role="img" aria-label="经营提醒概览图表"></div>
      </article>
    </section>

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
import * as echarts from 'echarts';
import { getShopAiAnalysis, getShopDashboard } from '@/api/shop';
import { ShopAiAnalysis, ShopDashboard } from '@/api/shop/types';

const router = useRouter();
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
  await nextTick();
  renderCharts();
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

  const todaySales = Number(data.value.todaySales || 0);
  const todayCredit = Number(data.value.todayCredit || 0);
  const paidAmount = Math.max(todaySales - todayCredit, 0);
  const hasIncome = paidAmount + todayCredit > 0;
  const incomeData = hasIncome
    ? [
        { name: '已收款', value: paidAmount },
        { name: '赊账', value: todayCredit }
      ]
    : [{ name: '暂无收款', value: 1 }];

  incomeChart.setOption(
    {
      backgroundColor: 'transparent',
      color: hasIncome ? [primaryColor, goldColor] : [borderColor],
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => (hasIncome ? `${params.name}<br/>￥${money(params.value)} (${params.percent}%)` : '暂无收款数据')
      },
      legend: {
        bottom: 0,
        icon: 'roundRect',
        textStyle: { color: mutedColor }
      },
      graphic: {
        type: 'text',
        left: 'center',
        top: '38%',
        style: {
          text: hasIncome ? `￥${money(todaySales)}` : '暂无数据',
          fill: textColor,
          fontSize: hasIncome ? 20 : 16,
          fontWeight: 700,
          textAlign: 'center'
        }
      },
      series: [
        {
          name: '收款构成',
          type: 'pie',
          radius: ['58%', '78%'],
          center: ['50%', '42%'],
          avoidLabelOverlap: true,
          label: {
            color: textColor,
            formatter: hasIncome ? '{b}\n￥{c}' : '暂无数据'
          },
          labelLine: {
            lineStyle: { color: borderColor }
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

  const todoData = [
    { name: '今日订单', value: Number(data.value.todayOrderCount || 0), itemStyle: { color: primaryColor } },
    { name: '库存预警', value: Number(data.value.lowStockCount || 0), itemStyle: { color: clayColor } },
    { name: '分析建议', value: aiAnalysis.value.insights?.length || 0, itemStyle: { color: goldColor } }
  ];

  todoChart.setOption(
    {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      grid: { left: 36, right: 16, top: 20, bottom: 42 },
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
          name: '数量',
          type: 'bar',
          barMaxWidth: 36,
          data: todoData,
          label: {
            show: true,
            position: 'top',
            color: textColor,
            fontWeight: 700
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

onMounted(() => {
  window.addEventListener('resize', resizeCharts);
  themeObserver = new MutationObserver(renderCharts);
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
  loadData();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts);
  themeObserver?.disconnect();
  incomeChart?.dispose();
  todoChart?.dispose();
});
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
.chart-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.chart-panel {
  min-width: 0;
  padding: 14px;
  border: 1px solid var(--dzg-shop-border);
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
.chart-panel__head h3 {
  margin: 0;
  color: var(--dzg-shop-text);
  font-size: 18px;
  font-weight: 750;
}
.chart-panel__head p {
  margin: 0;
  color: var(--dzg-shop-muted);
  font-size: 13px;
  white-space: nowrap;
}
.chart-canvas {
  width: 100%;
  height: 260px;
}
.ai-panel__tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}
@media (max-width: 900px) {
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
}
</style>
