<template>
  <div class="shop-page">
    <div class="shop-title">
      <div>
        <h2>库存管理</h2>
        <p>采购入库会自动增加库存；这里只处理盘点、损耗、退货等手工调整。</p>
      </div>
      <el-button class="primary-action" type="primary" icon="Plus" @click="dialogVisible = true">库存调整</el-button>
    </div>

    <el-table v-loading="loading" border :data="stocks" row-key="stockId">
      <el-table-column label="商品" min-width="180">
        <template #default="{ row }">
          <span class="name-cell">{{ row.productName || '未命名商品' }}</span>
          <small>ID {{ row.productId }}</small>
        </template>
      </el-table-column>
      <el-table-column label="当前库存" prop="quantity" min-width="160">
        <template #default="{ row }">
          <strong :class="{ danger: row.quantity <= row.warningQty }">{{ row.quantity }}</strong>
        </template>
      </el-table-column>
      <el-table-column label="预警值" prop="warningQty" width="120" />
      <el-table-column label="状态" width="140">
        <template #default="{ row }">
          <el-tag v-if="row.quantity <= row.warningQty" type="danger" size="large">需要补货</el-tag>
          <el-tag v-else type="success" size="large">正常</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadStocks" />

    <h3 class="section-title">库存流水</h3>
    <el-table border :data="logs">
      <el-table-column label="商品" min-width="180">
        <template #default="{ row }">
          <span class="name-cell">{{ row.productName || '未命名商品' }}</span>
          <small>ID {{ row.productId }}</small>
        </template>
      </el-table-column>
      <el-table-column label="类型" prop="changeType" width="110" />
      <el-table-column label="变化数量" prop="changeQty" width="120" />
      <el-table-column label="变动前" prop="beforeQty" width="120" />
      <el-table-column label="变动后" prop="afterQty" width="120" />
      <el-table-column label="备注" prop="remark" min-width="180" />
    </el-table>

    <el-dialog v-model="dialogVisible" title="库存调整" width="520px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="商品">
          <el-select v-model="form.productId" filterable placeholder="选择商品">
            <el-option v-for="item in products" :key="item.productId" :label="item.productName" :value="item.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整方式">
          <el-segmented v-model="form.changeType" :options="typeOptions" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.quantity" :min="1" :step="1" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="例如：进货入库" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="action-button" @click="dialogVisible = false">取消</el-button>
        <el-button class="action-button" type="primary" @click="submitAdjust">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopStock" lang="ts">
import { adjustStock, listStock, listStockLog, productOptions } from '@/api/shop';
import { optionList } from '@/api/shop/response';
import { ShopProduct, ShopStock } from '@/api/shop/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(false);
const stocks = ref<ShopStock[]>([]);
const logs = ref<any[]>([]);
const products = ref<ShopProduct[]>([]);
const total = ref(0);
const dialogVisible = ref(false);
const query = reactive({ pageNum: 1, pageSize: 10 });
const form = reactive({ productId: undefined as string | number | undefined, changeType: 'in', quantity: 1, remark: '' });
const typeOptions = [
  { label: '增加库存', value: 'in' },
  { label: '减少库存', value: 'out' }
];

const loadStocks = async () => {
  loading.value = true;
  const res = await listStock(query);
  stocks.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const loadLogs = async () => {
  const res = await listStockLog({ pageNum: 1, pageSize: 8 });
  logs.value = res.rows;
};

const loadProducts = async () => {
  const res = await productOptions({});
  products.value = optionList<ShopProduct>(res);
};

const submitAdjust = async () => {
  if (!form.productId) {
    proxy?.$modal.msgWarning('请先选择商品');
    return;
  }
  await adjustStock(form);
  proxy?.$modal.msgSuccess(form.changeType === 'in' ? '库存已增加' : '库存已减少');
  dialogVisible.value = false;
  await loadStocks();
  await loadLogs();
};

onMounted(() => {
  loadProducts();
  loadStocks();
  loadLogs();
});
</script>

<style scoped>
.section-title {
  margin: 20px 0 12px;
  color: var(--dzg-shop-text);
  font-size: 18px;
}
.danger {
  color: var(--dzg-shop-clay);
  font-size: 18px;
}

.name-cell {
  display: block;
  color: var(--dzg-shop-text);
  font-weight: 800;
}

small {
  display: block;
  margin-top: 4px;
  color: var(--dzg-shop-muted);
  font-size: 12px;
}
</style>
