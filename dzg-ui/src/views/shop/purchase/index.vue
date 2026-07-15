<template>
  <div class="shop-page">
    <div class="shop-title">
      <div>
        <h2>采购入库</h2>
        <p>把新进的货录入系统，库存会自动增加。</p>
      </div>
      <el-button class="primary-action" type="primary" icon="Plus" @click="addLine">添加商品</el-button>
    </div>

    <section class="purchase-panel">
      <el-form label-width="96px">
        <el-form-item label="供应商">
          <el-select v-model="form.supplierId" clearable filterable placeholder="选择供应商后只显示绑定商品" @change="handleSupplierChange">
            <el-option v-for="item in suppliers" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="例如：早上批发市场进货" />
        </el-form-item>
      </el-form>

      <div class="table-scroll">
        <el-table class="purchase-table" border :data="form.items">
          <el-table-column label="商品" min-width="320">
            <template #default="{ row }">
              <el-select v-model="row.productId" filterable placeholder="选择商品" @change="fillPrice(row)">
                <el-option v-for="item in products" :key="item.productId" :label="item.productName" :value="item.productId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="220">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" :step="1" size="large" />
            </template>
          </el-table-column>
          <el-table-column label="进价" width="240">
            <template #default="{ row }">
              <el-input-number v-model="row.purchasePrice" :min="0" :precision="2" :step="0.5" size="large" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="130">
            <template #default="{ row }">￥{{ money(Number(row.purchasePrice || 0) * Number(row.quantity || 0)) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button link type="danger" icon="Delete" aria-label="移除商品" title="移除商品" @click="removeLine($index)" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="total-row">
        <span>本次入库金额</span>
        <strong>￥{{ money(totalAmount) }}</strong>
      </div>

      <el-button class="finish-button" type="primary" icon="Select" :loading="submitting" @click="submitPurchase">确认入库</el-button>
    </section>

    <section class="history-section">
      <h3>最近采购记录</h3>
      <el-table border :data="purchases">
        <el-table-column label="采购单号" prop="purchaseNo" min-width="180" />
        <el-table-column label="金额" prop="totalAmount" width="120">
          <template #default="{ row }">￥{{ money(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="采购时间" prop="purchaseTime" min-width="170" />
        <el-table-column label="备注" prop="remark" min-width="160" />
      </el-table>
    </section>
  </div>
</template>

<script setup name="ShopPurchase" lang="ts">
import { createPurchase, listPurchase, productOptions, supplierOptions } from '@/api/shop';
import { optionList } from '@/api/shop/response';
import { ShopProduct, ShopPurchaseForm, ShopSupplier } from '@/api/shop/types';

type PurchaseLine = { productId?: string | number; quantity: number; purchasePrice: number };

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const submitting = ref(false);
const products = ref<ShopProduct[]>([]);
const suppliers = ref<ShopSupplier[]>([]);
const purchases = ref<any[]>([]);
const form = reactive<ShopPurchaseForm & { items: PurchaseLine[] }>({
  supplierId: undefined,
  remark: '',
  items: [{ productId: undefined, quantity: 1, purchasePrice: 0 }]
});

const totalAmount = computed(() => form.items.reduce((sum, item) => sum + Number(item.purchasePrice || 0) * Number(item.quantity || 0), 0));
const money = (value?: number) => Number(value || 0).toFixed(2);

const addLine = () => {
  form.items.push({ productId: undefined, quantity: 1, purchasePrice: 0 });
};

const removeLine = (index: number) => {
  if (form.items.length === 1) {
    proxy?.$modal.msgWarning('至少保留一行商品');
    return;
  }
  form.items.splice(index, 1);
};

const fillPrice = (row: PurchaseLine) => {
  const product = products.value.find((item) => item.productId === row.productId);
  row.purchasePrice = Number(product?.purchasePrice || 0);
};

const loadProducts = async () => {
  const productRes = await productOptions(form.supplierId ? { supplierId: form.supplierId } : {});
  products.value = optionList<ShopProduct>(productRes);
};

const handleSupplierChange = async () => {
  form.items.forEach((item) => {
    item.productId = undefined;
    item.purchasePrice = 0;
  });
  await loadProducts();
  if (form.supplierId && products.value.length === 0) {
    proxy?.$modal.msgWarning('这个供应商还没有绑定商品，商品档案里可先绑定常用供应商');
  }
};

const loadOptions = async () => {
  await loadProducts();
  const supplierRes = await supplierOptions({});
  suppliers.value = optionList<ShopSupplier>(supplierRes);
};

const loadPurchases = async () => {
  const res = await listPurchase({ pageNum: 1, pageSize: 8 });
  purchases.value = res.rows || [];
};

const submitPurchase = async () => {
  const validItems = form.items.filter((item) => item.productId && item.quantity > 0);
  if (!validItems.length) {
    proxy?.$modal.msgWarning('请先选择入库商品');
    return;
  }
  submitting.value = true;
  try {
    await createPurchase({ supplierId: form.supplierId, remark: form.remark, items: validItems });
    proxy?.$modal.msgSuccess('采购入库已完成，库存已增加');
    form.supplierId = undefined;
    form.remark = '';
    form.items = [{ productId: undefined, quantity: 1, purchasePrice: 0 }];
    await loadProducts();
    await loadPurchases();
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadOptions();
  loadPurchases();
});
</script>

<style scoped>
.purchase-panel,
.history-section {
  padding: 16px;
}
.field-with-action {
  width: 100%;
}
.table-scroll {
  width: 100%;
  overflow-x: auto;
}
.purchase-table {
  min-width: 990px;
}
:deep(.purchase-table .el-input-number) {
  width: 100%;
  min-width: 168px;
}
.finish-button {
  width: 100%;
  min-height: 52px;
}
</style>
