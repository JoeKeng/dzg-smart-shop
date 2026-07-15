<template>
  <div class="cashier-page">
    <div class="cashier-header">
      <h2>收银台</h2>
      <el-input v-model="keyword" class="scan-input" clearable autofocus placeholder="扫码或输入商品名称" @keyup.enter="searchProduct" />
      <el-button class="action-button" type="primary" icon="Search" @click="searchProduct">找商品</el-button>
    </div>

    <div class="cashier-layout">
      <section class="product-panel">
        <h3>常用商品</h3>
        <div class="product-grid">
          <button v-for="item in products" :key="item.productId" type="button" class="product-button" @click="addToCart(item)">
            <span>{{ item.productName }}</span>
            <strong>￥{{ money(item.salePrice) }}</strong>
          </button>
        </div>
      </section>

      <section class="cart-panel">
        <h3>购物清单</h3>
        <el-table border :data="cart">
          <el-table-column label="商品" prop="productName" min-width="130" />
          <el-table-column label="数量" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" :step="1" size="large" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="110">
            <template #default="{ row }">￥{{ money(row.salePrice * row.quantity) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button link type="danger" icon="Delete" aria-label="移除商品" title="移除商品" @click="cart.splice($index, 1)" />
            </template>
          </el-table-column>
        </el-table>

        <div class="total-row">
          <span>应收</span>
          <strong>￥{{ money(totalAmount) }}</strong>
        </div>

        <el-segmented v-model="payType" class="pay-type" :options="payOptions" />

        <el-select v-if="payType === 'credit'" v-model="customerId" class="customer-select" filterable placeholder="选择赊账客户">
          <el-option v-for="item in customers" :key="item.customerId" :label="`${item.customerName} 欠￥${money(item.currentDebt)}`" :value="item.customerId" />
        </el-select>

        <el-button class="finish-button" type="primary" icon="Select" :disabled="cart.length === 0" @click="finishOrder">
          {{ payType === 'credit' ? '确认挂账' : '完成收银' }}
        </el-button>
      </section>
    </div>
  </div>
</template>

<script setup name="ShopCashier" lang="ts">
import { createCashierOrder, customerOptions, productOptions } from '@/api/shop';
import { optionList } from '@/api/shop/response';
import { ShopCustomer, ShopProduct } from '@/api/shop/types';

interface CartItem extends ShopProduct {
  quantity: number;
}

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const keyword = ref('');
const products = ref<ShopProduct[]>([]);
const customers = ref<ShopCustomer[]>([]);
const cart = ref<CartItem[]>([]);
const payType = ref('cash');
const customerId = ref<string | number>();
const payOptions = [
  { label: '现金', value: 'cash' },
  { label: '微信', value: 'wechat' },
  { label: '支付宝', value: 'alipay' },
  { label: '赊账', value: 'credit' }
];

const totalAmount = computed(() => cart.value.reduce((sum, item) => sum + Number(item.salePrice || 0) * item.quantity, 0));
const money = (value?: number) => Number(value || 0).toFixed(2);

const loadProducts = async () => {
  const res = await productOptions(keyword.value ? { productName: keyword.value } : {});
  products.value = optionList<ShopProduct>(res);
};

const loadCustomers = async () => {
  const res = await customerOptions({});
  customers.value = optionList<ShopCustomer>(res);
};

const searchProduct = async () => {
  await loadProducts();
  if (products.value.length === 1) {
    addToCart(products.value[0]);
    keyword.value = '';
    await loadProducts();
  }
};

const addToCart = (product: ShopProduct) => {
  const exists = cart.value.find((item) => item.productId === product.productId);
  if (exists) {
    exists.quantity += 1;
    return;
  }
  cart.value.push({ ...product, quantity: 1 });
};

const finishOrder = async () => {
  if (payType.value === 'credit' && !customerId.value) {
    proxy?.$modal.msgWarning('赊账要先选择客户');
    return;
  }
  await createCashierOrder({
    customerId: customerId.value,
    payType: payType.value,
    paidAmount: totalAmount.value,
    items: cart.value.map((item) => ({ productId: item.productId, quantity: item.quantity }))
  });
  proxy?.$modal.msgSuccess(payType.value === 'credit' ? '已挂账，欠款记录已生成' : '已收款，库存已扣减');
  cart.value = [];
  customerId.value = undefined;
};

onMounted(() => {
  loadProducts();
  loadCustomers();
});
</script>

<style scoped>
.cashier-header {
  display: grid;
  grid-template-columns: auto minmax(240px, 1fr) auto;
  gap: 12px;
}
.scan-input {
  min-width: 0;
}
.cashier-layout {
  display: grid;
  grid-template-columns: minmax(300px, 1fr) minmax(420px, 0.9fr);
  gap: 14px;
}
.product-panel,
.cart-panel {
  padding: 14px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
}
.product-button {
  min-height: 76px;
  border: 1px solid #c8d1dc;
  border-radius: 8px;
  background: #f9fafb;
  color: #172033;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  font-size: 15px;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}
.product-button:hover,
.product-button:focus-visible {
  border-color: #087f73;
  box-shadow: 0 4px 10px rgba(8, 127, 115, 0.12);
  outline: none;
  transform: translateY(-1px);
}
.product-button strong {
  color: #087f73;
  font-size: 18px;
}
.pay-type,
.customer-select,
.finish-button {
  width: 100%;
  margin-top: 10px;
}
.finish-button {
  min-height: 52px;
}
@media (max-width: 900px) {
  .cashier-header,
  .cashier-layout {
    grid-template-columns: 1fr;
  }
}
</style>
