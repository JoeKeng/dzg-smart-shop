<template>
  <div class="ai-workbench">
    <aside class="conversation-panel">
      <div class="panel-head">
        <div>
          <h2>AI 助手</h2>
          <p>保留每次经营问答。</p>
        </div>
        <el-button type="primary" icon="Plus" @click="newConversation">新对话</el-button>
      </div>
      <div class="conversation-list">
        <button
          v-for="item in conversations"
          :key="item.conversationId"
          class="conversation-item"
          :class="{ active: item.conversationId === currentConversationId }"
          @click="openConversation(item)"
        >
          <span>{{ item.title || '新的 AI 对话' }}</span>
          <small>{{ item.updateTime || item.createTime || '刚刚' }}</small>
        </button>
      </div>
    </aside>

    <main class="chat-panel">
      <section class="chat-head">
        <div>
          <h3>{{ currentTitle }}</h3>
          <p>可以问库存、采购、赊账、营业额，也可以让 AI 帮你整理今天先做什么。</p>
        </div>
        <el-tag :type="lastGeneratedByAi ? 'success' : 'info'" size="large">{{ lastGeneratedByAi ? 'DeepSeek回复' : '本地兜底' }}</el-tag>
      </section>

      <section ref="messageBoxRef" class="message-box">
        <el-empty v-if="messages.length === 0" description="从右侧选一个问题，或直接输入你想问的事。" />
        <article v-for="item in messages" :key="item.messageId || `${item.role}-${item.createTime}-${item.content}`" class="message" :class="item.role">
          <div class="message-role">{{ item.role === 'user' ? '老板' : 'AI' }}</div>
          <p>{{ item.content }}</p>
        </article>
      </section>

      <section class="composer">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          resize="none"
          placeholder="例如：今天最该先补哪些货？"
          @keydown.ctrl.enter.prevent="sendMessage"
        />
        <div class="composer-actions">
          <el-button icon="Microphone" :disabled="!speechSupported || recognizing" @click="startVoiceInput">
            {{ recognizing ? '正在听' : '语音输入' }}
          </el-button>
          <el-button type="primary" icon="Promotion" :loading="sending" @click="sendMessage">发送</el-button>
        </div>
      </section>
    </main>

    <aside class="suggestion-panel">
      <h3>推荐提问</h3>
      <button v-for="item in suggestions" :key="item" class="suggestion-item" @click="askSuggestion(item)">
        {{ item }}
      </button>
      <div class="tip-box">
        <strong>提示</strong>
        <p>语音输入由浏览器提供。如果按钮不可用，请换 Chrome 或直接打字。</p>
      </div>
    </aside>
  </div>
</template>

<script setup name="ShopAiWorkbench" lang="ts">
import { createAiConversation, getAiSuggestions, listAiConversations, listAiMessages, sendAiChat } from '@/api/shop';
import { optionList } from '@/api/shop/response';
import { ShopAiChatResult, ShopAiConversation, ShopAiMessage } from '@/api/shop/types';

const input = ref('');
const sending = ref(false);
const recognizing = ref(false);
const lastGeneratedByAi = ref(true);
const conversations = ref<ShopAiConversation[]>([]);
const messages = ref<ShopAiMessage[]>([]);
const suggestions = ref<string[]>([]);
const currentConversationId = ref<string | number>();
const messageBoxRef = ref<HTMLElement>();

const currentTitle = computed(() => conversations.value.find((item) => item.conversationId === currentConversationId.value)?.title || '新的 AI 对话');
const speechSupported = computed(() => typeof window !== 'undefined' && Boolean((window as any).SpeechRecognition || (window as any).webkitSpeechRecognition));

const loadConversations = async () => {
  const res = await listAiConversations();
  conversations.value = optionList<ShopAiConversation>(res);
  if (!currentConversationId.value && conversations.value.length > 0) {
    await openConversation(conversations.value[0]);
  }
};

const loadSuggestions = async () => {
  const res = await getAiSuggestions();
  suggestions.value = optionList<string>(res);
};

const openConversation = async (item: ShopAiConversation) => {
  currentConversationId.value = item.conversationId;
  if (!item.conversationId) {
    messages.value = [];
    return;
  }
  const res = await listAiMessages(item.conversationId);
  messages.value = optionList<ShopAiMessage>(res);
  scrollToBottom();
};

const newConversation = async () => {
  const res = await createAiConversation();
  const conversation = res.data as ShopAiConversation;
  conversations.value.unshift(conversation);
  currentConversationId.value = conversation.conversationId;
  messages.value = [];
};

const sendMessage = async () => {
  const content = input.value.trim();
  if (!content || sending.value) return;
  sending.value = true;
  input.value = '';
  messages.value.push({ role: 'user', content, createTime: new Date().toLocaleString() });
  try {
    const res = await sendAiChat({ conversationId: currentConversationId.value, content });
    const data = res.data as ShopAiChatResult;
    if (data.conversation?.conversationId) {
      currentConversationId.value = data.conversation.conversationId;
    }
    if (data.reply) {
      messages.value.push(data.reply);
    }
    lastGeneratedByAi.value = data.generatedByAi !== false;
    if (data.suggestions?.length) {
      suggestions.value = data.suggestions;
    }
    await loadConversations();
  } finally {
    sending.value = false;
    scrollToBottom();
  }
};

const askSuggestion = (text: string) => {
  input.value = text;
  sendMessage();
};

const startVoiceInput = () => {
  const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
  if (!SpeechRecognition) return;
  const recognition = new SpeechRecognition();
  recognition.lang = 'zh-CN';
  recognition.interimResults = false;
  recognition.maxAlternatives = 1;
  recognizing.value = true;
  recognition.onresult = (event: any) => {
    input.value = event.results?.[0]?.[0]?.transcript || input.value;
  };
  recognition.onend = () => {
    recognizing.value = false;
  };
  recognition.start();
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messageBoxRef.value) {
      messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight;
    }
  });
};

onMounted(async () => {
  await Promise.all([loadSuggestions(), loadConversations()]);
});
</script>

<style scoped>
.ai-workbench {
  min-height: calc(100vh - 84px);
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr) 260px;
  gap: 16px;
  padding: 16px;
  background: #f4f7f5;
}
.conversation-panel,
.chat-panel,
.suggestion-panel {
  background: #ffffff;
  border: 1px solid #d9e4dc;
  border-radius: 8px;
}
.conversation-panel,
.suggestion-panel {
  padding: 16px;
}
.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.panel-head h2,
.chat-head h3,
.suggestion-panel h3 {
  margin: 0;
  color: #20352b;
}
.panel-head p,
.chat-head p,
.tip-box p {
  margin: 6px 0 0;
  color: #68756d;
}
.conversation-list {
  margin-top: 16px;
  display: grid;
  gap: 8px;
}
.conversation-item,
.suggestion-item {
  width: 100%;
  border: 1px solid #d9e4dc;
  background: #f9fbf8;
  border-radius: 8px;
  padding: 12px;
  text-align: left;
  cursor: pointer;
}
.conversation-item.active {
  border-color: #3d7c59;
  background: #eaf5ee;
}
.conversation-item span {
  display: block;
  color: #20352b;
  font-weight: 700;
}
.conversation-item small {
  display: block;
  margin-top: 4px;
  color: #7a887f;
}
.chat-panel {
  display: grid;
  grid-template-rows: auto minmax(360px, 1fr) auto;
  min-width: 0;
}
.chat-head {
  padding: 18px;
  border-bottom: 1px solid #d9e4dc;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.message-box {
  overflow-y: auto;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.message {
  max-width: 78%;
  padding: 12px 14px;
  border-radius: 8px;
}
.message.user {
  align-self: flex-end;
  background: #2f6f51;
  color: #ffffff;
}
.message.assistant {
  align-self: flex-start;
  background: #eef4ef;
  color: #20352b;
}
.message-role {
  font-size: 12px;
  opacity: 0.75;
  margin-bottom: 6px;
}
.message p {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
}
.composer {
  border-top: 1px solid #d9e4dc;
  padding: 14px;
}
.composer-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.suggestion-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.suggestion-item {
  color: #20352b;
  font-weight: 700;
}
.tip-box {
  margin-top: auto;
  padding: 12px;
  border-radius: 8px;
  background: #fff8e8;
  color: #5a4420;
}
@media (max-width: 1180px) {
  .ai-workbench {
    grid-template-columns: 1fr;
  }
  .chat-panel {
    min-height: 640px;
  }
}
</style>
