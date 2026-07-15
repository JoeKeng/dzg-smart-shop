<template>
  <div class="shop-page notification-page">
    <div class="shop-title">
      <div>
        <h2>提醒中心</h2>
        <p>先处理未读消息，已读提醒会收进归档区，后续仍可改回未读。</p>
      </div>
      <div class="title-actions">
        <el-button class="action-button" icon="Refresh" :loading="loading" @click="loadNotices">刷新</el-button>
        <el-button class="primary-action" type="primary" icon="Finished" :disabled="unreadNotices.length === 0" @click="markAllRead">全部已读</el-button>
      </div>
    </div>

    <section class="notification-summary">
      <div>
        <span>未读</span>
        <strong>{{ unreadNotices.length }}</strong>
      </div>
      <div>
        <span>已归档</span>
        <strong>{{ readNotices.length }}</strong>
      </div>
      <el-switch v-model="showArchived" active-text="显示归档" inactive-text="隐藏归档" />
    </section>

    <el-empty v-if="!loading && notices.length === 0" description="当前没有需要处理的提醒" />

    <section v-if="unreadNotices.length > 0" class="notice-section">
      <div class="notice-section__head">
        <h3>待处理消息</h3>
        <span>建议按库存、赊账、经营建议的顺序处理。</span>
      </div>
      <div class="notice-list">
        <article v-for="item in unreadNotices" :key="noticeKey(item)" class="notice-card" :class="[noticeClass(item), 'is-unread']">
          <div class="notice-card__main">
            <div class="notice-card__title">
              <strong>{{ item.noticeTitle }}</strong>
              <el-tag type="danger" effect="dark">未读</el-tag>
            </div>
            <p>{{ item.noticeContent }}</p>
            <div class="notice-card__actions">
              <el-button link type="primary" icon="Position" @click="goNotice(item)">{{ actionText(item) }}</el-button>
              <el-button link type="success" icon="Check" @click="setReadStatus(item, '1')">标记已读</el-button>
            </div>
          </div>
          <div class="notice-card__meta">
            <el-tag :type="noticeTagType(item.noticeType)" size="large">{{ noticeTypeText(item.noticeType) }}</el-tag>
          </div>
        </article>
      </div>
    </section>

    <section v-if="showArchived && readNotices.length > 0" class="notice-section archive-section">
      <div class="notice-section__head">
        <h3>已读归档</h3>
        <span>这些提醒已缩小显示，可随时改回未读。</span>
      </div>
      <div class="archive-list">
        <article v-for="item in readNotices" :key="noticeKey(item)" class="archive-card">
          <div>
            <strong>{{ item.noticeTitle }}</strong>
            <span>{{ item.noticeContent }}</span>
          </div>
          <div class="archive-card__actions">
            <el-tag :type="noticeTagType(item.noticeType)" effect="plain">{{ noticeTypeText(item.noticeType) }}</el-tag>
            <el-button link type="warning" icon="RefreshLeft" @click="setReadStatus(item, '0')">标记未读</el-button>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup name="ShopNotification" lang="ts">
import { listNotification, readAllNotification, updateNotificationStatus } from '@/api/shop';
import { optionList } from '@/api/shop/response';
import { ShopNotification } from '@/api/shop/types';
import { useNoticeStore } from '@/store/modules/notice';
import { ElNotification } from 'element-plus';

const router = useRouter();
const noticeStore = useNoticeStore();
const loading = ref(false);
const showArchived = ref(false);
const notices = ref<ShopNotification[]>([]);
const notifiedKeys = new Set<string>();

const unreadNotices = computed(() => notices.value.filter((item) => item.status === '0'));
const readNotices = computed(() => notices.value.filter((item) => item.status !== '0'));

const loadNotices = async () => {
  loading.value = true;
  try {
    const res = await listNotification();
    notices.value = optionList<ShopNotification>(res);
    if (unreadNotices.value.length === 0 && readNotices.value.length > 0) {
      showArchived.value = true;
    }
    syncNoticeStore();
    notifyUrgentMessages();
  } finally {
    loading.value = false;
  }
};

const syncNoticeStore = () => {
  noticeStore.state.notices = notices.value.map((item) => ({
    id: item.noticeId,
    title: item.noticeTitle,
    message: item.noticeContent,
    read: item.status !== '0',
    time: noticeTypeText(item.noticeType)
  }));
};

const noticeTagType = (type?: string) => {
  if (type === 'stock') return 'danger';
  if (type === 'suggestion') return 'success';
  return 'warning';
};

const noticeTypeText = (type?: string) => {
  if (type === 'stock') return '库存';
  if (type === 'suggestion') return '建议';
  return '赊账';
};

const noticeClass = (item: ShopNotification) => {
  if (item.noticeType === 'stock') return 'stock';
  if (item.noticeType === 'suggestion') return 'suggestion';
  return 'credit';
};

const actionText = (item: ShopNotification) => {
  if (item.noticeType === 'stock') return '去补货';
  if (item.noticeType === 'suggestion') return '看分析';
  return '处理赊账';
};

const noticePath = (item: ShopNotification) => {
  if (item.noticeType === 'stock') return '/shop/purchase';
  if (item.noticeType === 'suggestion') return '/shop/report';
  return '/shop/credit';
};

const noticeKey = (item: ShopNotification) => String(item.noticeId || `${item.noticeType}-${item.bizType}-${item.bizId}`);

const goNotice = (item: ShopNotification) => {
  router.push(noticePath(item));
};

const setReadStatus = async (item: ShopNotification, status: '0' | '1') => {
  if (!item.noticeId) return;
  await updateNotificationStatus(item.noticeId, status);
  item.status = status;
  syncNoticeStore();
};

const markAllRead = async () => {
  await readAllNotification();
  notices.value.forEach((item) => {
    item.status = '1';
  });
  showArchived.value = true;
  syncNoticeStore();
};

const notifyUrgentMessages = () => {
  unreadNotices.value
    .filter((item) => item.noticeType === 'stock' || item.noticeType === 'credit_unpaid')
    .slice(0, 3)
    .forEach((item) => {
      const key = noticeKey(item);
      if (notifiedKeys.has(key)) return;
      notifiedKeys.add(key);
      ElNotification({
        title: item.noticeTitle || '经营提醒',
        message: item.noticeContent || '有新的店铺消息需要处理',
        type: item.noticeType === 'stock' ? 'warning' : 'info',
        duration: 4500,
        position: 'bottom-right'
      });
    });
};

onMounted(loadNotices);
</script>
