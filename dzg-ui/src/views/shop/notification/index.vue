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
      <div v-for="item in notices" :key="`${item.noticeType}-${item.bizType}-${item.bizId}`" class="notice-card" :class="item.noticeType">
        <div>
          <strong>{{ item.noticeTitle }}</strong>
          <p>{{ item.noticeContent }}</p>
        </div>
        <el-tag :type="item.noticeType === 'stock' ? 'danger' : 'warning'" size="large">
          {{ item.noticeType === 'stock' ? '库存' : '赊账' }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup name="ShopNotification" lang="ts">
import { listNotification } from '@/api/shop';
import { ShopNotification } from '@/api/shop/types';

const loading = ref(false);
const notices = ref<ShopNotification[]>([]);

const loadNotices = async () => {
  loading.value = true;
  try {
    const res = await listNotification();
    notices.value = res.data || [];
  } finally {
    loading.value = false;
  }
};

onMounted(loadNotices);
</script>

<style scoped>
.shop-page {
  padding: 16px;
  background: #f5f7fb;
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
}
.shop-title p {
  margin: 6px 0 0;
  color: #374151;
  font-size: 17px;
}
.notice-list {
  display: grid;
  gap: 12px;
}
.notice-card {
  min-height: 88px;
  padding: 18px;
  border: 1px solid #d1d5db;
  border-left: 6px solid #b45309;
  border-radius: 8px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.notice-card.stock {
  border-left-color: #b91c1c;
}
.notice-card strong {
  color: #111827;
  font-size: 20px;
}
.notice-card p {
  margin: 8px 0 0;
  color: #1f2937;
  font-size: 17px;
}
.primary-action {
  min-height: 44px;
  font-size: 16px;
}
</style>
