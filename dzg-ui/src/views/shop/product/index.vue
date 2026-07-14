<template>
  <div class="shop-page">
    <div class="shop-title">
      <h2>商品管理</h2>
      <el-button class="primary-action" type="primary" icon="Plus" @click="openProduct()">新增商品</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.productName" clearable placeholder="输入商品名称" @keyup.enter="loadProducts" />
      <el-input v-model="query.barcode" clearable placeholder="条码" @keyup.enter="loadProducts" />
      <el-button class="action-button" icon="Search" @click="loadProducts">查询</el-button>
    </div>

    <el-table v-loading="loading" border :data="products">
      <el-table-column label="商品名称" prop="productName" min-width="180" />
      <el-table-column label="条码" prop="barcode" min-width="130" />
      <el-table-column label="售价" prop="salePrice" width="110" />
      <el-table-column label="进价" prop="purchasePrice" width="110" />
      <el-table-column label="单位" prop="unitName" width="90" />
      <el-table-column label="预警值" prop="warningQty" width="100" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="openProduct(row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="removeProduct(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadProducts" />

    <el-dialog v-model="dialogVisible" :title="form.productId ? '修改商品' : '新增商品'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="例如：矿泉水" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" clearable placeholder="选择分类">
            <el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="条码">
          <el-input v-model="form.barcode" placeholder="可扫码或手动输入" />
        </el-form-item>
        <el-form-item label="售价" prop="salePrice">
          <el-input-number v-model="form.salePrice" :min="0" :precision="2" :step="0.5" />
        </el-form-item>
        <el-form-item label="进价">
          <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" :step="0.5" />
        </el-form-item>
        <el-form-item label="库存预警">
          <el-input-number v-model="form.warningQty" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="action-button" @click="dialogVisible = false">取消</el-button>
        <el-button class="action-button" type="primary" @click="submitProduct">保存商品</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopProduct" lang="ts">
import { categoryOptions, deleteProduct, listProduct, saveProduct } from '@/api/shop';
import { ShopCategory, ShopProduct } from '@/api/shop/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(false);
const products = ref<ShopProduct[]>([]);
const categories = ref<ShopCategory[]>([]);
const total = ref(0);
const dialogVisible = ref(false);
const formRef = ref<ElFormInstance>();
const query = reactive({ pageNum: 1, pageSize: 10, productName: '', barcode: '' });
const form = reactive<ShopProduct>({});
const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  salePrice: [{ required: true, message: '请输入售价', trigger: 'blur' }]
};

const loadProducts = async () => {
  loading.value = true;
  const res = await listProduct(query);
  products.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const loadCategories = async () => {
  const res = await categoryOptions();
  categories.value = res.data || [];
};

const openProduct = (row?: ShopProduct) => {
  Object.assign(form, {
    productId: undefined,
    categoryId: undefined,
    productName: '',
    barcode: '',
    unitName: '件',
    salePrice: 0,
    purchasePrice: 0,
    warningQty: 10,
    status: '0'
  }, row || {});
  dialogVisible.value = true;
};

const submitProduct = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    await saveProduct(form);
    proxy?.$modal.msgSuccess('商品已保存，库存记录已同步');
    dialogVisible.value = false;
    await loadProducts();
  });
};

const removeProduct = async (row: ShopProduct) => {
  await proxy?.$modal.confirm(`确认删除商品“${row.productName}”？`);
  await deleteProduct(row.productId!);
  proxy?.$modal.msgSuccess('商品已删除');
  await loadProducts();
};

onMounted(() => {
  loadCategories();
  loadProducts();
});
</script>

<style scoped>
.shop-page {
  padding: 16px;
  font-size: 16px;
}
.shop-title,
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}
.shop-title {
  justify-content: space-between;
}
.shop-title h2 {
  margin: 0;
  font-size: 26px;
}
.toolbar .el-input {
  max-width: 260px;
}
.primary-action,
.action-button {
  min-height: 44px;
  font-size: 16px;
}
:deep(.el-table) {
  font-size: 16px;
}
</style>
