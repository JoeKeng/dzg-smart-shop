<template>
  <div class="business-ai-page">
    <section class="analysis-hero">
      <div>
        <h2>AI 经营分析</h2>
        <p>{{ analysis.summary || '正在整理店铺经营情况。' }}</p>
      </div>
      <div class="hero-actions">
        <el-tag :type="analysis.generatedByAi ? 'success' : 'info'" size="large">
          {{ analysis.generatedByAi ? 'DeepSeek分析' : '本地分析' }}
        </el-tag>
        <el-button type="primary" icon="Refresh" :loading="loading" @click="loadAnalysis(true)">重新分析</el-button>
      </div>
    </section>

    <section class="metric-strip">
      <article v-for="item in analysis.metrics" :key="item.label" class="metric-tile">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </article>
    </section>

    <div class="analysis-layout">
      <main class="report-panel">
        <article v-for="item in analysis.sections" :key="`${item.title}-${item.horizon}`" class="report-section">
          <div class="section-head">
            <div>
              <span>{{ item.horizon }}</span>
              <h3>{{ item.title }}</h3>
            </div>
            <el-tag v-if="isVisibleLevel(item.level)" :type="tagType(item.level)" effect="plain">{{ levelText(item.level) }}</el-tag>
          </div>
          <p>{{ item.content }}</p>
          <ul>
            <li v-for="action in item.actions" :key="action">{{ action }}</li>
          </ul>
        </article>
      </main>

      <aside class="question-panel">
        <div class="question-card">
          <div class="question-head">
            <h3>经营分析问答</h3>
            <el-tag type="warning" effect="plain">专精经营</el-tag>
          </div>
          <div ref="messageBoxRef" class="question-messages">
            <article v-for="item in messages" :key="item.id" class="question-message" :class="item.role">
              <span>{{ item.role === 'user' ? '我问' : 'AI答' }}</span>
              <p>{{ item.content }}</p>
            </article>
            <div v-if="messages.length === 0" class="question-empty">
              <strong>还没有提问</strong>
              <span>可以直接输入经营问题，或点击下方建议快速开始。</span>
            </div>
          </div>
          <div class="suggestions">
            <button v-for="item in visibleSuggestions" :key="item" @click="askSuggestion(item)">
              {{ item }}
            </button>
          </div>
          <div class="question-composer">
            <el-input
              v-model="input"
              type="textarea"
              :rows="3"
              resize="none"
              placeholder="例如：未来三个月应该怎么规划采购？"
              @keydown.ctrl.enter.prevent="sendQuestion"
            />
            <el-button type="primary" icon="Promotion" :loading="asking" @click="sendQuestion">询问分析</el-button>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup name="ShopAiBusinessAnalysis" lang="ts">
import { askShopAiBusinessAnalysis } from '@/api/shop';
import { getCachedShopAiBusinessAnalysis } from '@/api/shop/ai-cache';
import { ShopAiBusinessAnalysis, ShopAiBusinessQuestionResult } from '@/api/shop/types';

interface QuestionMessage {
  id: number;
  role: 'user' | 'assistant';
  content: string;
}

const loading = ref(false);
const asking = ref(false);
const input = ref('');
const messageBoxRef = ref<HTMLElement>();
const messages = ref<QuestionMessage[]>([]);
const suggestions = ref<string[]>([]);
const visibleSuggestions = computed(() => suggestions.value.slice(0, 3));
const analysis = ref<ShopAiBusinessAnalysis>({
  summary: '',
  riskLevel: 'low',
  metrics: [],
  sections: [],
  suggestions: []
});

const loadAnalysis = async (force = false) => {
  loading.value = true;
  try {
    const res = await getCachedShopAiBusinessAnalysis(force);
    analysis.value = res.data || { summary: '', riskLevel: 'low', metrics: [], sections: [], suggestions: [] };
    suggestions.value = analysis.value.suggestions || [];
  } finally {
    loading.value = false;
  }
};

const sendQuestion = async () => {
  const content = input.value.trim();
  if (!content || asking.value) return;
  input.value = '';
  asking.value = true;
  messages.value.push({ id: Date.now(), role: 'user', content });
  try {
    const res = await askShopAiBusinessAnalysis({ content });
    const data = res.data as ShopAiBusinessQuestionResult;
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: data.answer || data.fallbackReason || '暂时没有分析结果。'
    });
    if (data.suggestions?.length) {
      suggestions.value = data.suggestions;
    }
  } finally {
    asking.value = false;
    scrollToBottom();
  }
};

const askSuggestion = (text: string) => {
  input.value = text;
  sendQuestion();
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messageBoxRef.value) {
      messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight;
    }
  });
};

const tagType = (level?: string) => {
  if (level === 'danger') return 'danger';
  if (level === 'warning') return 'warning';
  if (level === 'success') return 'success';
  return 'info';
};

const isVisibleLevel = (level?: string) => ['danger', 'warning', 'success'].includes(String(level || ''));

const levelText = (level?: string) => {
  if (level === 'danger') return '高风险';
  if (level === 'warning') return '需关注';
  if (level === 'success') return '稳健';
  return '';
};

onMounted(() => loadAnalysis());
</script>

<style scoped>
.business-ai-page {
  min-width: 0;
  min-height: calc(100vh - 84px);
  padding: 16px;
  background: #f6f4ef;
  color: #22312a;
}

.analysis-hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 22px;
  border: 1px solid #d9ded4;
  border-radius: 8px;
  background: #ffffff;
}

.analysis-hero > div {
  min-width: 0;
}

.analysis-hero h2,
.question-head h3,
.report-section h3 {
  margin: 0;
  color: #17251f;
}

.analysis-hero p {
  max-width: 920px;
  margin: 10px 0 0;
  line-height: 1.8;
  color: #526058;
}

.hero-actions,
.question-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.metric-tile {
  min-height: 112px;
  padding: 14px;
  border: 1px solid #d9ded4;
  border-radius: 8px;
  background: #fffdf9;
}

.metric-tile span,
.report-section span,
.question-message span {
  display: block;
  color: #6a756e;
  font-size: 13px;
}

.metric-tile strong {
  display: block;
  margin-top: 8px;
  color: #1f5c42;
  font-size: 24px;
  line-height: 1.25;
  word-break: break-word;
}

.metric-tile small {
  display: block;
  margin-top: 8px;
  color: #7a5d25;
  line-height: 1.5;
}

.analysis-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 340px);
  align-items: start;
  gap: 16px;
  margin-top: 14px;
}

.report-panel {
  min-width: 0;
  display: grid;
  gap: 12px;
}

.report-section {
  min-width: 0;
  padding: 18px;
  border: 1px solid #d9ded4;
  border-left: 5px solid #2f6f51;
  border-radius: 8px;
  background: #ffffff;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.report-section h3 {
  margin-top: 4px;
}

.report-section p {
  margin: 12px 0;
  color: #445049;
  line-height: 1.8;
}

.report-section ul {
  margin: 0;
  padding-left: 20px;
  color: #23352c;
  line-height: 1.9;
}

.question-panel {
  position: sticky;
  top: 72px;
  align-self: start;
  width: 100%;
  min-width: 0;
  max-height: calc(100dvh - 96px);
  z-index: 1;
}

.question-card {
  max-height: inherit;
  padding: 16px;
  border: 1px solid #d9ded4;
  border-radius: 8px;
  background: #ffffff;
  display: grid;
  grid-template-rows: auto minmax(150px, auto) auto auto;
  overflow: auto;
}

.question-head {
  justify-content: space-between;
}

.question-messages {
  min-height: 120px;
  max-height: 240px;
  overflow-y: auto;
  display: grid;
  align-content: start;
  gap: 10px;
  margin-top: 14px;
  padding-right: 4px;
}

.question-message {
  padding: 10px 12px;
  border-radius: 8px;
  background: #f2f6f0;
}

.question-message.user {
  background: #e7f0ff;
}

.question-message p {
  margin: 6px 0 0;
  line-height: 1.7;
  white-space: pre-wrap;
}

.question-empty {
  min-height: 110px;
  border: 1px dashed #e3d5ae;
  border-radius: 8px;
  background: #fffdf9;
  color: #6a756e;
  display: grid;
  place-content: center;
  gap: 8px;
  padding: 16px;
  text-align: center;
}

.question-empty strong {
  color: #22312a;
  font-size: 16px;
}

.question-empty span {
  font-size: 14px;
  line-height: 1.6;
}

.suggestions {
  display: grid;
  gap: 6px;
  margin-top: 12px;
}

.suggestions button {
  width: 100%;
  border: 1px solid #e3d5ae;
  border-radius: 8px;
  padding: 8px 10px;
  background: #fff8e8;
  color: #493817;
  line-height: 1.5;
  white-space: normal;
  text-align: left;
  cursor: pointer;
}

.question-composer {
  display: grid;
  gap: 10px;
  margin-top: 14px;
}

@media (max-width: 1280px) {
  .metric-strip {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .analysis-layout {
    grid-template-columns: 1fr;
  }

  .question-panel {
    position: static;
    max-height: none;
  }

  .question-card {
    max-height: none;
  }
}

@media (max-width: 760px) {
  .analysis-hero {
    flex-direction: column;
  }

  .hero-actions {
    justify-content: flex-start;
  }

  .metric-strip {
    grid-template-columns: 1fr;
  }
}
</style>
