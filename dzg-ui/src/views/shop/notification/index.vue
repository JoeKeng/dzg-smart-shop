<template>
  <div class="shop-page">
    <div class="shop-title">
      <div>
        <h2>提醒中心</h2>
        <p>把需要马上处理的库存和赊账放在一起。</p>
      </div>
      <el-button class="primary-action" icon="Refresh" :loading="loading" @click="loadNotices">刷新</el-button>
    </div>

    <el-empty v-if="!loading && notices.length === 0" description="当前没有需要处理的提醒" />

    <div v-else class="notice-list">
      <article v-for="(item, index) in notices" :key="`${item.noticeType}-${item.bizType}-${item.bizId}-${index}`" class="notice-card" :class="item.noticeType">
        <div>
          <strong>{{ item.noticeTitle }}</strong>
          <p>{{ item.noticeContent }}</p>
          <div class="notice-card__actions">
            <el-button link type="primary" icon="Position" @click="goNotice(item)">{{ actionText(item) }}</el-button>
          </div>
        </div>
        <div class="notice-card__meta">
          <el-tag :type="noticeTagType(item.noticeType)" size="large">{{ noticeTypeText(item.noticeType) }}</el-tag>
          <el-tag v-if="item.status === '0'" type="danger" effect="plain">未读</el-tag>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup name="ShopNotification" lang="ts">
import { listNotification } from '@/api/shop';
import { optionList } from '@/api/shop/response';
import { ShopNotification } from '@/api/shop/types';
import { useNoticeStore } from '@/store/modules/notice';
import { ElNotification } from 'element-plus';

const router = useRouter();
const noticeStore = useNoticeStore();
const loading = ref(false);
const notices = ref<ShopNotification[]>([]);
const notifiedKeys = new Set<string>();

const loadNotices = async () => {
  loading.value = true;
  try {
    const res = await listNotification();
    notices.value = optionList<ShopNotification>(res);
    noticeStore.state.notices = notices.value.map((item) => ({
      title: item.noticeTitle,
      message: item.noticeContent,
      read: item.status !== '0',
      time: noticeTypeText(item.noticeType)
    }));
    notifyUrgentMessages();
  } finally {
    loading.value = false;
  }
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

const goNotice = (item: ShopNotification) => {
  router.push(noticePath(item));
};

const notifyUrgentMessages = () => {
  notices.value
    .filter((item) => item.noticeType === 'stock' || item.noticeType === 'credit')
    .slice(0, 3)
    .forEach((item) => {
      const key = `${item.noticeType}-${item.bizType}-${item.bizId}`;
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
