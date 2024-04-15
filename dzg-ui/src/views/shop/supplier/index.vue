<template>
  <div class="shop-page">
    <div class="shop-title">
      <div>
        <h2>供应商管理</h2>
        <p>记录常用进货渠道，采购入库时可直接选择。</p>
      </div>
      <el-button class="primary-action" type="primary" icon="Plus" @click="openSupplier()">新增供应商</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.supplierName" clearable placeholder="供应商名称" @keyup.enter="loadSuppliers" />
      <el-input v-model="query.phone" clearable maxlength="11" placeholder="手机号" @input="query.phone = onlyDigits(query.phone)" @keyup.enter="loadSuppliers" />
      <el-button class="action-button" icon="Search" @click="loadSuppliers">查询</el-button>
    </div>

    <el-table v-loading="loading" border :data="suppliers">
      <el-table-column label="供应商" prop="supplierName" min-width="180" />
      <el-table-column label="联系人" prop="contactName" width="120" />
      <el-table-column label="手机号" prop="phone" width="150" />
      <el-table-column label="绑定商品" width="120" align="center">
        <template #default="{ row }">
          <strong class="count-text">{{ row.productCount || 0 }}</strong>
        </template>
      </el-table-column>
      <el-table-column label="地址" prop="address" min-width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" @click="openSupplier(row)">修改</el-button>
            <el-button link type="danger" @click="removeSupplier(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadSuppliers" />

    <el-dialog v-model="dialogVisible" :title="form.supplierId ? '修改供应商' : '新增供应商'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="供应商" prop="supplierName">
          <el-input v-model="form.supplierName" placeholder="例如：县城批发部" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactName" placeholder="联系人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="11位手机号" @input="form.phone = onlyDigits(form.phone)" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="供应商地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="常送哪些货、结算习惯等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="action-button" @click="dialogVisible = false">取消</el-button>
        <el-button class="action-button" type="primary" @click="submitSupplier">保存供应商</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopSupplier" lang="ts">
import { deleteSupplier, listSupplier, saveSupplier } from '@/api/shop';
import { ShopSupplier } from '@/api/shop/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(false);
const suppliers = ref<ShopSupplier[]>([]);
const total = ref(0);
const dialogVisible = ref(false);
const formRef = ref<ElFormInstance>();
const query = reactive({ pageNum: 1, pageSize: 10, supplierName: '', phone: '' });
const form = reactive<ShopSupplier>({});
const phoneRule = { pattern: /^\d{11}$/, message: '手机号必须是11位数字', trigger: 'blur' };
const rules = {
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  phone: [phoneRule]
};

const onlyDigits = (value?: string) => String(value || '').replace(/\D/g, '').slice(0, 11);

const loadSuppliers = async () => {
  loading.value = true;
  const res = await listSupplier(query);
  suppliers.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const openSupplier = (row?: ShopSupplier) => {
  Object.assign(form, { supplierId: undefined, supplierName: '', contactName: '', phone: '', address: '', status: '0', remark: '' }, row || {});
  dialogVisible.value = true;
};

const submitSupplier = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    await saveSupplier(form);
    proxy?.$modal.msgSuccess('供应商已保存');
    dialogVisible.value = false;
    await loadSuppliers();
  });
};

const removeSupplier = async (row: ShopSupplier) => {
  await proxy?.$modal.confirm(`确认删除供应商“${row.supplierName}”？`);
  await deleteSupplier(row.supplierId!);
  proxy?.$modal.msgSuccess('供应商已删除');
  await loadSuppliers();
};

onMounted(loadSuppliers);
</script>

<style scoped>
.count-text {
  color: var(--dzg-shop-primary);
  font-size: 18px;
  font-variant-numeric: tabular-nums;
}

.table-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100%;
  white-space: nowrap;
}

.table-actions .el-button {
  margin: 0;
}
</style>
