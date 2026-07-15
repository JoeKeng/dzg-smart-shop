<template>
  <div class="shop-page product-page">
    <div class="shop-title">
      <div>
        <h2>商品管理</h2>
        <p>维护商品、分类、常用供应商和商品图片，收银与采购会自动使用这些资料。</p>
      </div>
      <el-button class="primary-action" type="primary" icon="Plus" @click="openProduct()">新增商品</el-button>
    </div>

    <section class="toolbar" aria-label="商品查询">
      <el-input v-model="query.productName" clearable placeholder="输入商品名称" @keyup.enter="loadProducts" />
      <el-input v-model="query.barcode" clearable placeholder="输入条码" @keyup.enter="loadProducts" />
      <el-select v-model="query.categoryId" clearable placeholder="选择分类">
        <el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
      </el-select>
      <el-button class="action-button" icon="Search" @click="loadProducts">查询</el-button>
      <el-button class="action-button" icon="Refresh" @click="resetQuery">重置</el-button>
    </section>

    <el-alert
      v-if="!categoryLoading && categories.length === 0"
      class="empty-alert"
      title="还没有商品分类，请先新增分类，再录入商品。"
      type="warning"
      show-icon
      :closable="false"
    >
      <template #default>
        <el-button class="inline-action" type="warning" plain icon="Plus" @click="openCategory()">新增分类</el-button>
      </template>
    </el-alert>

    <el-table v-loading="loading" border :data="products" empty-text="暂无商品，点击右上角新增商品">
      <el-table-column label="图片" width="96" align="center">
        <template #default="{ row }">
          <ImagePreview v-if="row.imageUrl" :width="56" :height="56" :src="row.imageUrl" :preview-src-list="[row.imageUrl]" />
          <span v-else class="image-empty">无图</span>
        </template>
      </el-table-column>
      <el-table-column label="商品名称" prop="productName" min-width="180" show-overflow-tooltip />
      <el-table-column label="分类" min-width="120">
        <template #default="{ row }">{{ row.categoryName || categoryName(row.categoryId) || '未分类' }}</template>
      </el-table-column>
      <el-table-column label="常用供应商" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ supplierNameText(row) }}</template>
      </el-table-column>
      <el-table-column label="条码" prop="barcode" min-width="130" />
      <el-table-column label="售价" width="120" align="right">
        <template #default="{ row }">￥{{ money(row.salePrice) }}</template>
      </el-table-column>
      <el-table-column label="进价" width="120" align="right">
        <template #default="{ row }">￥{{ money(row.purchasePrice) }}</template>
      </el-table-column>
      <el-table-column label="单位" prop="unitName" width="90" />
      <el-table-column label="预警值" prop="warningQty" width="100" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="openProduct(row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="removeProduct(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadProducts" />

    <el-dialog v-model="dialogVisible" :title="form.productId ? '修改商品' : '新增商品'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="product-form">
        <div class="form-section">
          <h3>基础信息</h3>
          <el-form-item label="商品名称" prop="productName">
            <el-input v-model="form.productName" name="productName" autocomplete="off" placeholder="例如：矿泉水" />
          </el-form-item>
          <el-form-item label="分类" prop="categoryId">
            <div class="field-with-action">
              <el-select v-model="form.categoryId" filterable clearable placeholder="选择分类">
                <el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
              </el-select>
              <el-button class="small-action" icon="Plus" @click="openCategory()">新增分类</el-button>
            </div>
          </el-form-item>
          <el-form-item label="常用供应商" prop="supplierIds">
            <el-select
              v-model="form.supplierIds"
              multiple
              filterable
              clearable
              :multiple-limit="3"
              :disabled="suppliers.length === 0"
              placeholder="至少选 1 个，最多 3 个"
            >
              <el-option v-for="item in suppliers" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
            </el-select>
            <p v-if="suppliers.length === 0" class="field-tip">请先在供应商管理里新增供应商。</p>
          </el-form-item>
          <el-form-item label="条码">
            <el-input v-model="form.barcode" name="barcode" autocomplete="off" placeholder="可扫码或手动输入" />
          </el-form-item>
        </div>

        <div class="form-section">
          <h3>价格库存</h3>
          <div class="number-grid">
            <el-form-item label="售价" prop="salePrice">
              <el-input-number v-model="form.salePrice" :min="0" :precision="2" :step="0.5" />
            </el-form-item>
            <el-form-item label="进价">
              <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" :step="0.5" />
            </el-form-item>
            <el-form-item label="单位">
              <el-input v-model="form.unitName" name="unitName" autocomplete="off" placeholder="件、瓶、箱" />
            </el-form-item>
            <el-form-item label="库存预警">
              <el-input-number v-model="form.warningQty" :min="0" :step="1" />
            </el-form-item>
          </div>
        </div>

        <div class="form-section">
          <h3>商品图片</h3>
          <el-form-item label="图片">
            <ImageUpload v-model="form.imageOssId" :limit="1" :file-size="3" :compress-support="true" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button class="action-button" @click="dialogVisible = false">取消</el-button>
        <el-button class="action-button" type="primary" :loading="submitting" @click="submitProduct">保存商品</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialogVisible" :title="categoryForm.categoryId ? '修改分类' : '新增分类'" width="460px">
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="96px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" name="categoryName" autocomplete="off" placeholder="例如：饮料、日用品" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="action-button" @click="categoryDialogVisible = false">取消</el-button>
        <el-button class="action-button" type="primary" :loading="categorySubmitting" @click="submitCategory">保存分类</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopProduct" lang="ts">
import { categoryOptions, deleteProduct, listProduct, saveCategory, saveProduct, supplierOptions } from '@/api/shop';
import { optionList } from '@/api/shop/response';
import { ShopCategory, ShopProduct, ShopSupplier } from '@/api/shop/types';
import { listByIds } from '@/api/system/oss';
import ImagePreview from '@/components/ImagePreview/index.vue';
import ImageUpload from '@/components/ImageUpload/index.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(false);
const categoryLoading = ref(false);
const submitting = ref(false);
const categorySubmitting = ref(false);
const products = ref<ShopProduct[]>([]);
const categories = ref<ShopCategory[]>([]);
const suppliers = ref<ShopSupplier[]>([]);
const total = ref(0);
const dialogVisible = ref(false);
const categoryDialogVisible = ref(false);
const formRef = ref<ElFormInstance>();
const categoryFormRef = ref<ElFormInstance>();
const query = reactive({ pageNum: 1, pageSize: 10, productName: '', barcode: '', categoryId: undefined as string | number | undefined });
const form = reactive<ShopProduct>({});
const categoryForm = reactive<ShopCategory>({});
const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  supplierIds: [
    {
      validator: (_rule: unknown, value: Array<string | number> | undefined, callback: (error?: Error) => void) => {
        if (!value || value.length === 0) {
          callback(new Error('请选择至少一个常用供应商'));
          return;
        }
        if (value.length > 3) {
          callback(new Error('常用供应商最多选择 3 个'));
          return;
        }
        callback();
      },
      trigger: 'change'
    }
  ],
  salePrice: [{ required: true, message: '请输入售价', trigger: 'blur' }]
};
const categoryRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
};

const moneyFormatter = new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const money = (value?: number) => moneyFormatter.format(Number(value || 0));

const categoryName = (categoryId?: string | number) => categories.value.find((item) => String(item.categoryId) === String(categoryId))?.categoryName;
const supplierName = (supplierId?: string | number) => suppliers.value.find((item) => String(item.supplierId) === String(supplierId))?.supplierName;

const supplierNameText = (row: ShopProduct) => {
  if (row.supplierNames) {
    return row.supplierNames;
  }
  const names = (row.supplierIds || []).map((id) => supplierName(id)).filter(Boolean);
  return names.length ? names.join('、') : '未绑定';
};

const loadProducts = async () => {
  loading.value = true;
  try {
    const res = await listProduct(query);
    products.value = res.rows || [];
    total.value = res.total || 0;
    await hydrateProductImages();
  } finally {
    loading.value = false;
  }
};

const hydrateProductImages = async () => {
  const ossIds = products.value.map((item) => item.imageOssId).filter(Boolean) as Array<string | number>;
  if (!ossIds.length) return;
  const res = await listByIds(ossIds.join(','));
  const imageMap = new Map((res.data || []).map((item) => [String(item.ossId), item.url]));
  products.value.forEach((product) => {
    if (product.imageOssId) {
      product.imageUrl = imageMap.get(String(product.imageOssId)) || product.imageUrl;
    }
  });
};

const loadCategories = async () => {
  categoryLoading.value = true;
  try {
    const res = await categoryOptions();
    categories.value = optionList<ShopCategory>(res);
  } catch {
    categories.value = [];
    proxy?.$modal.msgError('商品分类加载失败，请检查 Shop 服务和数据库分类表');
  } finally {
    categoryLoading.value = false;
  }
};

const loadSuppliers = async () => {
  const res = await supplierOptions({ status: '0' });
  suppliers.value = optionList<ShopSupplier>(res);
};

const resetQuery = async () => {
  Object.assign(query, { pageNum: 1, pageSize: query.pageSize, productName: '', barcode: '', categoryId: undefined });
  await loadProducts();
};

const openProduct = (row?: ShopProduct) => {
  Object.assign(
    form,
    {
      productId: undefined,
      categoryId: undefined,
      supplierIds: [],
      productName: '',
      barcode: '',
      unitName: '件',
      salePrice: 0,
      purchasePrice: 0,
      warningQty: 10,
      imageOssId: '',
      status: '0'
    },
    row || {}
  );
  form.supplierIds = [...(row?.supplierIds || [])];
  dialogVisible.value = true;
};

const openCategory = (row?: ShopCategory) => {
  Object.assign(categoryForm, { categoryId: undefined, categoryName: '', sortOrder: 0, status: '0' }, row || {});
  categoryDialogVisible.value = true;
};

const submitCategory = () => {
  categoryFormRef.value?.validate(async (valid) => {
    if (!valid) return;
    categorySubmitting.value = true;
    try {
      await saveCategory(categoryForm);
      proxy?.$modal.msgSuccess('分类已保存');
      categoryDialogVisible.value = false;
      const savedCategoryName = categoryForm.categoryName;
      await loadCategories();
      const savedCategory = categories.value.find((item) => item.categoryName === savedCategoryName);
      if (!form.categoryId && savedCategory) {
        form.categoryId = savedCategory.categoryId;
      }
    } finally {
      categorySubmitting.value = false;
    }
  });
};

const submitProduct = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    submitting.value = true;
    try {
      await saveProduct(form);
      proxy?.$modal.msgSuccess('商品已保存，分类、供应商和库存记录已同步');
      dialogVisible.value = false;
      await loadProducts();
    } finally {
      submitting.value = false;
    }
  });
};

const removeProduct = async (row: ShopProduct) => {
  await proxy?.$modal.confirm(`确认删除商品“${row.productName}”？`);
  await deleteProduct(row.productId!);
  proxy?.$modal.msgSuccess('商品已删除');
  await loadProducts();
};

onMounted(async () => {
  await Promise.all([loadCategories(), loadSuppliers()]);
  await loadProducts();
});
</script>

<style scoped>
.toolbar {
  margin-bottom: 14px;
}

.empty-alert {
  margin-bottom: 12px;
}

.inline-action {
  margin-top: 8px;
}

.small-action {
  flex: 0 0 auto;
}

.image-empty {
  display: inline-flex;
  width: 56px;
  height: 56px;
  align-items: center;
  justify-content: center;
  border: 1px dashed var(--dzg-shop-border-strong);
  border-radius: 8px;
  color: var(--dzg-shop-muted);
  font-size: 15px;
}

.product-form {
  display: grid;
  gap: 16px;
}

.form-section {
  padding: 14px 14px 4px;
  border: 1px solid var(--dzg-shop-border);
  border-radius: 8px;
  background: var(--dzg-shop-surface);
}

.form-section h3 {
  margin: 0 0 12px;
  color: var(--dzg-shop-text);
  font-size: 18px;
}

.field-with-action {
  width: 100%;
}

.field-tip {
  margin: 6px 0 0;
  color: var(--dzg-shop-clay);
  font-size: 13px;
}

.number-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 12px;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  min-height: 40px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-table .cell) {
  line-height: 1.5;
}

@media (max-width: 768px) {
  .primary-action {
    width: 100%;
  }

  .number-grid {
    grid-template-columns: 1fr;
  }
}
</style>
