<template>
  <div class="shop-page">
    <div class="shop-title">
      <h2>客户管理</h2>
      <el-button class="primary-action" type="primary" icon="Plus" @click="openCustomer()">新增客户</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.customerName" clearable placeholder="客户姓名" @keyup.enter="loadCustomers" />
      <el-input v-model="query.phone" clearable maxlength="11" placeholder="手机号" @input="query.phone = onlyDigits(query.phone)" @keyup.enter="loadCustomers" />
      <el-button class="action-button" icon="Search" @click="loadCustomers">查询</el-button>
    </div>

    <el-table v-loading="loading" border :data="customers">
      <el-table-column label="姓名" prop="customerName" min-width="150" />
      <el-table-column label="手机号" prop="phone" min-width="140" />
      <el-table-column label="赊账额度" prop="creditLimit" width="130" />
      <el-table-column label="当前欠款" prop="currentDebt" width="130">
        <template #default="{ row }">
          <strong :class="{ danger: Number(row.currentDebt || 0) > 0 }">￥{{ money(row.currentDebt) }}</strong>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="openCustomer(row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="removeCustomer(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadCustomers" />

    <el-dialog v-model="dialogVisible" :title="form.customerId ? '修改客户' : '新增客户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="姓名" prop="customerName">
          <el-input v-model="form.customerName" placeholder="客户姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="11位手机号" @input="form.phone = onlyDigits(form.phone)" />
        </el-form-item>
        <el-form-item label="赊账额度">
          <el-input-number v-model="form.creditLimit" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="例如：村东头王叔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="action-button" @click="dialogVisible = false">取消</el-button>
        <el-button class="action-button" type="primary" @click="submitCustomer">保存客户</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopCustomer" lang="ts">
import { deleteCustomer, listCustomer, saveCustomer } from '@/api/shop';
import { ShopCustomer } from '@/api/shop/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(false);
const customers = ref<ShopCustomer[]>([]);
const total = ref(0);
const dialogVisible = ref(false);
const formRef = ref<ElFormInstance>();
const query = reactive({ pageNum: 1, pageSize: 10, customerName: '', phone: '' });
const form = reactive<ShopCustomer>({});
const phoneRule = { pattern: /^\d{11}$/, message: '手机号必须是11位数字', trigger: 'blur' };
const rules = {
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  phone: [phoneRule]
};

const money = (value?: number) => Number(value || 0).toFixed(2);
const onlyDigits = (value?: string) => String(value || '').replace(/\D/g, '').slice(0, 11);

const loadCustomers = async () => {
  loading.value = true;
  const res = await listCustomer(query);
  customers.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const openCustomer = (row?: ShopCustomer) => {
  Object.assign(form, {
    customerId: undefined,
    customerName: '',
    phone: '',
    creditLimit: 0,
    currentDebt: 0,
    status: '0',
    remark: ''
  }, row || {});
  dialogVisible.value = true;
};

const submitCustomer = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    await saveCustomer(form);
    proxy?.$modal.msgSuccess('客户已保存');
    dialogVisible.value = false;
    await loadCustomers();
  });
};

const removeCustomer = async (row: ShopCustomer) => {
  await proxy?.$modal.confirm(`确认删除客户“${row.customerName}”？`);
  await deleteCustomer(row.customerId!);
  proxy?.$modal.msgSuccess('客户已删除');
  await loadCustomers();
};

onMounted(loadCustomers);
</script>

<style scoped>
.danger {
  color: #b42318;
}
</style>
