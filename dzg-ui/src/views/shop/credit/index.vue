<template>
  <div class="shop-page">
    <div class="shop-title">
      <h2>赊账管理</h2>
      <el-button class="primary-action" icon="Refresh" @click="loadCredits">刷新</el-button>
    </div>

    <el-table v-loading="loading" border :data="credits">
      <el-table-column label="赊账ID" prop="creditId" width="110" />
      <el-table-column label="客户ID" prop="customerId" width="110" />
      <el-table-column label="赊账金额" prop="creditAmount" width="130">
        <template #default="{ row }">￥{{ money(row.creditAmount) }}</template>
      </el-table-column>
      <el-table-column label="已还" prop="paidAmount" width="120">
        <template #default="{ row }">￥{{ money(row.paidAmount) }}</template>
      </el-table-column>
      <el-table-column label="未还" prop="unpaidAmount" width="130">
        <template #default="{ row }">
          <strong :class="{ danger: Number(row.unpaidAmount || 0) > 0 }">￥{{ money(row.unpaidAmount) }}</strong>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'settled'" type="success" size="large">已结清</el-tag>
          <el-tag v-else type="danger" size="large">未结清</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="150" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button :disabled="row.status === 'settled'" link type="primary" icon="Wallet" @click="openRepay(row)">还款</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadCredits" />

    <el-dialog v-model="dialogVisible" title="还款登记" width="500px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="未还金额">
          <strong class="danger">￥{{ money(currentCredit?.unpaidAmount) }}</strong>
        </el-form-item>
        <el-form-item label="还款金额">
          <el-input-number v-model="form.repaymentAmount" :min="0.01" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="收款方式">
          <el-segmented v-model="form.payType" :options="payOptions" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="例如：现金还款" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="action-button" @click="dialogVisible = false">取消</el-button>
        <el-button class="action-button" type="primary" @click="submitRepay">登记还款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopCredit" lang="ts">
import { listCredit, repayCredit } from '@/api/shop';
import { ShopCreditRecord } from '@/api/shop/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(false);
const credits = ref<ShopCreditRecord[]>([]);
const total = ref(0);
const query = reactive({ pageNum: 1, pageSize: 10 });
const dialogVisible = ref(false);
const currentCredit = ref<ShopCreditRecord>();
const form = reactive({ creditId: undefined as string | number | undefined, repaymentAmount: 0, payType: 'cash', remark: '' });
const payOptions = [
  { label: '现金', value: 'cash' },
  { label: '微信', value: 'wechat' },
  { label: '支付宝', value: 'alipay' }
];

const money = (value?: number) => Number(value || 0).toFixed(2);

const loadCredits = async () => {
  loading.value = true;
  const res = await listCredit(query);
  credits.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const openRepay = (row: ShopCreditRecord) => {
  currentCredit.value = row;
  form.creditId = row.creditId;
  form.repaymentAmount = Number(row.unpaidAmount || 0);
  form.payType = 'cash';
  form.remark = '';
  dialogVisible.value = true;
};

const submitRepay = async () => {
  await repayCredit(form);
  proxy?.$modal.msgSuccess('还款已登记，欠款已减少');
  dialogVisible.value = false;
  await loadCredits();
};

onMounted(loadCredits);
</script>

<style scoped>
.danger {
  color: #b42318;
  font-size: 18px;
}
</style>
