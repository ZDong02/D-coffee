<template>
  <section class="category-page">
    <div class="page-toolbar">
      <div><h2>商品分类</h2><p>维护用户菜单中的分类名称、图标和展示顺序</p></div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
    </div>
    <el-alert class="category-alert" title="停用分类会让该分类下的商品暂时不出现在用户菜单中；已有关联商品和历史订单不会删除。" type="warning" show-icon :closable="false" />
    <el-card shadow="never">
      <el-table v-loading="loading" :data="categories" row-key="id" empty-text="暂无分类">
        <el-table-column label="分类" min-width="240">
          <template #default="{ row }">
            <div class="category-name-cell">
              <el-image v-if="row.iconUrl" :src="row.iconUrl" fit="cover" class="category-icon"><template #error><div class="category-icon category-icon--fallback">D</div></template></el-image>
              <div v-else class="category-icon category-icon--fallback">D</div>
              <div><strong>{{ row.name }}</strong><span>{{ row.code }}</span></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="120"><template #default="{ row }"><el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用中' : '已停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 'ACTIVE' ? 'danger' : 'success'" @click="toggleStatus(row)">{{ row.status === 'ACTIVE' ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="min(560px, 94vw)" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="95px">
        <el-form-item label="分类名称" prop="name"><el-input v-model="form.name" maxlength="64" show-word-limit placeholder="例如：咖啡" /></el-form-item>
        <el-form-item label="分类编码" prop="code"><el-input v-model="form.code" maxlength="48" placeholder="例如：coffee，仅支持字母、数字、下划线和短横线" /></el-form-item>
        <el-form-item label="图标地址" prop="iconUrl"><el-input v-model="form.iconUrl" maxlength="500" placeholder="图片 URL（选填）" /></el-form-item>
        <el-form-item label="展示排序" prop="sort"><el-input-number v-model="form.sort" :min="-9999" :max="9999" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存分类</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createCategory, fetchAdminCategories, updateCategory, updateCategoryStatus } from '../../api/admin.js'

const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const form = reactive({ code: '', name: '', iconUrl: '', sort: 0 })
const rules = {
  code: [
    { required: true, message: '请填写分类编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]{1,48}$/, message: '编码限 48 位字母、数字、下划线或短横线', trigger: 'blur' },
  ],
  name: [{ required: true, message: '请填写分类名称', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try { categories.value = await fetchAdminCategories() }
  catch (error) { ElMessage.error(error.message || '分类加载失败') }
  finally { loading.value = false }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { code: '', name: '', iconUrl: '', sort: 0 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { code: row.code, name: row.name, iconUrl: row.iconUrl || '', sort: row.sort ?? 0 })
  dialogVisible.value = true
}

async function save() {
  if (!await formRef.value?.validate().catch(() => false)) return
  saving.value = true
  const payload = { ...form, code: form.code.trim(), name: form.name.trim(), iconUrl: form.iconUrl.trim() || null }
  try {
    if (editingId.value) await updateCategory(editingId.value, payload)
    else await createCategory(payload)
    ElMessage.success(editingId.value ? '分类已更新' : '分类已创建')
    dialogVisible.value = false
    await load()
  } catch (error) { ElMessage.error(error.message || '保存失败') }
  finally { saving.value = false }
}

async function toggleStatus(row) {
  const nextStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const action = nextStatus === 'ACTIVE' ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(
      nextStatus === 'DISABLED' ? `停用“${row.name}”后，该分类下的商品将从用户菜单隐藏。继续吗？` : `启用“${row.name}”分类？`,
      `${action}分类`, { type: nextStatus === 'DISABLED' ? 'warning' : 'info', confirmButtonText: `确认${action}`, cancelButtonText: '返回' },
    )
  } catch { return }
  try {
    await updateCategoryStatus(row.id, nextStatus)
    ElMessage.success(`分类已${action}`)
    await load()
  } catch (error) { ElMessage.error(error.message || `${action}失败`) }
}

onMounted(load)
</script>

<style scoped lang="scss">
.page-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 20px; margin-bottom: 20px; }
.page-toolbar h2 { margin: 0; color: #382b22; font-size: 22px; }
.page-toolbar p { margin: 8px 0 0; color: #8c7e72; font-size: 13px; }
.category-alert { margin-bottom: 16px; }
.category-name-cell { display: flex; align-items: center; gap: 13px; }
.category-icon { width: 42px; height: 42px; flex: 0 0 auto; border-radius: 10px; background: #f1e8de; }
.category-icon--fallback { display: grid; place-items: center; color: #806449; font-family: Georgia, serif; font-size: 21px; }
.category-name-cell strong, .category-name-cell span { display: block; }
.category-name-cell strong { color: #453429; font-size: 14px; }
.category-name-cell span { margin-top: 5px; color: #a09284; font-size: 12px; }
@media (max-width: 640px) { .page-toolbar { align-items: flex-start; flex-direction: column; } }
</style>
