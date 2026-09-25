<template>
  <section class="product-page">
    <div class="product-toolbar">
      <div>
        <h2>商品目录</h2>
        <p>维护商品信息、销售状态与库存</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增商品</el-button>
    </div>

    <el-card shadow="never" class="product-card">
      <el-form inline class="filter-form" @submit.prevent="search">
        <el-form-item label="关键词"><el-input v-model="filters.keyword" clearable placeholder="商品名称或编码" @keyup.enter="search" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="filters.categoryId" clearable placeholder="全部分类" @change="search"><el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" /></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="filters.status" clearable placeholder="全部状态" @change="search"><el-option label="草稿" value="DRAFT" /><el-option label="销售中" value="ON_SALE" /><el-option label="已下架" value="OFF_SALE" /></el-select></el-form-item>
        <el-form-item><el-button :icon="Search" @click="search">查询</el-button><el-button @click="resetFilters">重置</el-button></el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" class="product-error" />
      <el-table v-loading="loading" :data="products" row-key="id" empty-text="暂无商品，请先新增商品">
        <el-table-column label="商品" min-width="230">
          <template #default="{ row }">
            <div class="product-name-cell">
              <el-image v-if="row.imageUrl" :src="row.imageUrl" fit="cover" class="product-thumb"><template #error><div class="product-thumb__empty">咖</div></template></el-image>
              <div v-else class="product-thumb product-thumb__empty">咖</div>
              <div><strong>{{ row.name }}</strong><span>{{ row.productCode }}</span></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="115" />
        <el-table-column label="售价" width="105"><template #default="{ row }">¥{{ money(row.price) }}</template></el-table-column>
        <el-table-column label="库存" width="100"><template #default="{ row }"><span :class="{ 'stock-low': row.stock < 5 }">{{ row.stock }}</span></template></el-table-column>
        <el-table-column label="标记" width="125"><template #default="{ row }"><el-tag v-if="row.recommended" size="small" effect="plain">推荐</el-tag><el-tag v-if="row.isNew" size="small" type="warning" effect="plain">新品</el-tag><span v-if="!row.recommended && !row.isNew" class="muted">—</span></template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="操作" fixed="right" width="285">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openConfiguration(row)">规格加料</el-button>
            <el-button link type="primary" @click="openInventory(row)">调整库存</el-button>
            <el-dropdown @command="(status) => changeStatus(row, status)">
              <el-button link type="primary">更多</el-button>
              <template #dropdown><el-dropdown-menu><el-dropdown-item v-if="row.status !== 'ON_SALE'" command="ON_SALE">上架销售</el-dropdown-item><el-dropdown-item v-if="row.status !== 'OFF_SALE'" command="OFF_SALE">下架商品</el-dropdown-item><el-dropdown-item v-if="row.status !== 'DRAFT'" command="DRAFT">设为草稿</el-dropdown-item></el-dropdown-menu></template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" @current-change="loadProducts" @size-change="changePageSize" /></div>
    </el-card>

    <el-dialog v-model="productDialogVisible" :title="editingId ? '编辑商品' : '新增商品'" width="min(680px, 94vw)" destroy-on-close>
      <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="100px">
        <el-form-item label="商品名称" prop="name"><el-input v-model="productForm.name" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item label="商品编码" prop="productCode"><el-input v-model="productForm.productCode" maxlength="48" /></el-form-item>
        <el-form-item label="商品分类" prop="categoryId"><el-select v-model="productForm.categoryId" placeholder="请选择分类" style="width: 100%"><el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" /></el-select><div v-if="!categories.length" class="form-tip">当前没有可用分类，请先在数据库维护启用分类。</div></el-form-item>
        <div class="form-row"><el-form-item label="售价" prop="price"><el-input-number v-model="productForm.price" :precision="2" :min="0" :step="1" /></el-form-item><el-form-item label="划线价"><el-input-number v-model="productForm.originalPrice" :precision="2" :min="0" :step="1" :value-on-clear="null" /></el-form-item></div>
        <el-form-item v-if="!editingId" label="初始库存" prop="stock"><el-input-number v-model="productForm.stock" :min="0" :max="2147483647" :step="1" /></el-form-item>
        <el-form-item label="商品图片"><el-input v-model="productForm.imageUrl" maxlength="500" placeholder="图片 URL（可选）" /></el-form-item>
        <el-form-item label="商品描述"><el-input v-model="productForm.description" type="textarea" :rows="3" maxlength="1000" show-word-limit /></el-form-item>
        <el-form-item label="展示设置"><el-checkbox v-model="productForm.recommended">人气推荐</el-checkbox><el-checkbox v-model="productForm.isNew">新品</el-checkbox><span class="field-gap">排序</span><el-input-number v-model="productForm.sort" :min="-9999" :max="9999" /></el-form-item>
        <el-alert v-if="editingId" title="编辑商品不会直接修改库存；请使用列表中的“调整库存”操作以保留库存记录。" type="info" :closable="false" />
      </el-form>
      <template #footer><el-button @click="productDialogVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveProduct">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="configurationDialogVisible" :title="`${configurationProduct?.name || '商品'} · 规格与加料`" width="min(900px, 96vw)" destroy-on-close>
      <div class="configuration-content">
        <el-alert title="保存后会替换该商品当前启用的规格和加料；历史记录使用名称与价格快照，不受影响。" type="info" :closable="false" />
        <div class="configuration-heading"><h3>规格组</h3><el-button size="small" @click="addOptionGroup">新增规格组</el-button></div>
        <el-empty v-if="!configurationOptions.length" description="还没有规格组" :image-size="56" />
        <el-card v-for="(option, optionIndex) in configurationOptions" :key="option.localId" shadow="never" class="configuration-card">
          <template #header>
            <div class="configuration-card__header"><strong>规格组 {{ optionIndex + 1 }}</strong><el-button link type="danger" @click="removeOptionGroup(optionIndex)">删除规格组</el-button></div>
          </template>
          <div class="configuration-grid">
            <label>规格名称<el-input v-model="option.name" maxlength="64" placeholder="例如：杯型" /></label>
            <label>选择方式<el-select v-model="option.selectionType" @change="changeSelectionType(option)"><el-option label="单选" value="SINGLE" /><el-option label="多选" value="MULTIPLE" /></el-select></label>
            <label class="required-field"><el-checkbox v-model="option.required">必选</el-checkbox></label>
            <label>最少选择<el-input-number v-model="option.minSelect" :min="option.required ? 1 : 0" :max="option.values.length" /></label>
            <label>最多选择<el-input-number v-model="option.maxSelect" :min="1" :max="option.values.length || 1" :disabled="option.selectionType === 'SINGLE'" /></label>
          </div>
          <div class="configuration-subheading"><strong>选项值</strong><el-button link type="primary" @click="addOptionValue(option)">添加选项</el-button></div>
          <div v-for="(value, valueIndex) in option.values" :key="value.localId" class="value-row">
            <el-input v-model="value.name" maxlength="64" placeholder="选项名称，例如：中杯" />
            <el-input-number v-model="value.priceDelta" :precision="2" :min="0" :step="1" />
            <span class="unit-label">加价（元）</span>
            <el-button link type="danger" @click="removeOptionValue(option, valueIndex)">移除</el-button>
          </div>
          <p v-if="!option.values.length" class="configuration-tip">至少添加一个有效选项值。</p>
        </el-card>

        <div class="configuration-heading"><h3>加料</h3><el-button size="small" @click="addExtra">新增加料</el-button></div>
        <el-empty v-if="!configurationExtras.length" description="还没有加料" :image-size="56" />
        <div v-for="(extra, extraIndex) in configurationExtras" :key="extra.localId" class="extra-editor-row">
          <el-input v-model="extra.name" maxlength="64" placeholder="加料名称，例如：浓缩咖啡液" />
          <el-input-number v-model="extra.price" :precision="2" :min="0" :step="1" />
          <span class="unit-label">售价（元）</span>
          <el-input-number v-model="extra.stock" :min="0" :max="2147483647" />
          <span class="unit-label">库存</span>
          <el-button link type="danger" @click="configurationExtras.splice(extraIndex, 1)">删除</el-button>
        </div>
      </div>
      <template #footer><el-button @click="configurationDialogVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveConfiguration">保存规格与加料</el-button></template>
    </el-dialog>

    <el-dialog v-model="inventoryDialogVisible" title="调整库存" width="min(460px, 94vw)">
      <div class="inventory-summary"><strong>{{ inventoryProduct?.name }}</strong><span>当前库存：{{ inventoryProduct?.stock ?? '—' }}</span></div>
      <el-form ref="inventoryFormRef" :model="inventoryForm" :rules="inventoryRules" label-width="95px">
        <el-form-item label="调整数量" prop="delta"><el-input-number v-model="inventoryForm.delta" :min="-2147483647" :max="2147483647" :step="1" /></el-form-item>
        <p class="form-tip inventory-hint">填正数为入库，负数为扣减。扣减后库存不能小于 0。</p>
        <el-form-item label="调整原因" prop="reason"><el-input v-model="inventoryForm.reason" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="例如：到货入库、盘点修正" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="inventoryDialogVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveInventory">确认调整</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { adjustProductInventory, createProduct, fetchCategories, fetchProduct, fetchProducts, updateProduct, updateProductConfiguration, updateProductStatus } from '../../api/admin.js'

const products = ref([])
const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filters = reactive({ keyword: '', categoryId: null, status: '' })
const productDialogVisible = ref(false)
const productFormRef = ref()
const editingId = ref(null)
const productForm = reactive(emptyProduct())
const productRules = {
  name: [{ required: true, message: '请填写商品名称', trigger: 'blur' }],
  productCode: [{ required: true, message: '请填写商品编码', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请填写商品售价', trigger: 'change' }],
  stock: [{ required: true, message: '请填写初始库存', trigger: 'change' }],
}
const inventoryDialogVisible = ref(false)
const inventoryFormRef = ref()
const inventoryProduct = ref(null)
const inventoryForm = reactive({ delta: 0, reason: '' })
const inventoryRules = {
  delta: [{ validator: (_rule, value, done) => value === 0 ? done(new Error('调整数量不能为 0')) : done(), trigger: 'change' }],
  reason: [{ required: true, message: '请填写调整原因', trigger: 'blur' }],
}
const configurationDialogVisible = ref(false)
const configurationProduct = ref(null)
const configurationOptions = ref([])
const configurationExtras = ref([])
let localIdSeed = 0

function emptyProduct() {
  return { name: '', productCode: '', categoryId: null, price: 0, originalPrice: null, stock: 0, imageUrl: '', description: '', recommended: false, isNew: false, sort: 0 }
}

function money(value) { return Number(value || 0).toFixed(2) }
function statusLabel(status) { return ({ DRAFT: '草稿', ON_SALE: '销售中', OFF_SALE: '已下架' })[status] || status }
function statusType(status) { return ({ DRAFT: 'info', ON_SALE: 'success', OFF_SALE: 'warning' })[status] || 'info' }

async function loadProducts() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await fetchProducts({ page: page.value, pageSize: pageSize.value, keyword: filters.keyword.trim() || undefined, categoryId: filters.categoryId || undefined, status: filters.status || undefined })
    products.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    errorMessage.value = error.message || '商品加载失败，请检查后端服务和数据库连接。'
  } finally {
    loading.value = false
  }
}

function search() { page.value = 1; loadProducts() }
function resetFilters() { filters.keyword = ''; filters.categoryId = null; filters.status = ''; search() }
function changePageSize() { page.value = 1; loadProducts() }

async function loadCategories() {
  try { categories.value = await fetchCategories() }
  catch (error) { ElMessage.error(`分类加载失败：${error.message}`) }
}

function openCreate() {
  editingId.value = null
  Object.assign(productForm, emptyProduct())
  productDialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(productForm, {
    name: row.name, productCode: row.productCode, categoryId: row.categoryId, price: Number(row.price),
    originalPrice: row.originalPrice == null ? null : Number(row.originalPrice), stock: row.stock,
    imageUrl: row.imageUrl || '', description: row.description || '', recommended: row.recommended,
    isNew: row.isNew, sort: row.sort,
  })
  productDialogVisible.value = true
}

async function saveProduct() {
  try {
    await productFormRef.value.validate()
    saving.value = true
    const payload = {
      ...productForm,
      price: Number(productForm.price),
      originalPrice: productForm.originalPrice === null || productForm.originalPrice === '' ? null : Number(productForm.originalPrice),
      sort: Number(productForm.sort),
    }
    if (editingId.value) {
      const { stock: _stock, ...updatePayload } = payload
      await updateProduct(editingId.value, updatePayload)
    } else {
      await createProduct(payload)
    }
    ElMessage.success(editingId.value ? '商品信息已更新' : '商品已创建为草稿')
    productDialogVisible.value = false
    await loadProducts()
  } catch (error) {
    if (error instanceof Error) ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function changeStatus(row, status) {
  const action = status === 'ON_SALE' ? '上架' : status === 'OFF_SALE' ? '下架' : '设为草稿'
  try {
    await ElMessageBox.confirm(`确定将“${row.name}”${action}吗？`, '确认商品状态', { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' })
    await updateProductStatus(row.id, status)
    ElMessage.success(`商品已${action}`)
    await loadProducts()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close' && error?.message) ElMessage.error(error.message)
  }
}

function openInventory(row) {
  inventoryProduct.value = row
  inventoryForm.delta = 0
  inventoryForm.reason = ''
  inventoryDialogVisible.value = true
}

function localId() { localIdSeed += 1; return `配置项-${localIdSeed}` }
function createCode(prefix) { localIdSeed += 1; return `${prefix}_${Date.now().toString(36)}_${localIdSeed.toString(36)}` }

async function openConfiguration(row) {
  configurationProduct.value = row
  configurationDialogVisible.value = true
  configurationOptions.value = []
  configurationExtras.value = []
  try {
    const detail = await fetchProduct(row.id)
    configurationOptions.value = (detail.options || []).map((option) => ({
      localId: localId(), code: option.code, name: option.name, selectionType: option.selectionType,
      required: Boolean(option.required), minSelect: Number(option.minSelect || 0),
      maxSelect: Number(option.maxSelect || 1), sort: Number(option.sort || 0),
      values: (option.values || []).map((value) => ({
        localId: localId(), code: value.code, name: value.name,
        priceDelta: Number(value.priceDelta || 0), sort: Number(value.sort || 0),
      })),
    }))
    configurationExtras.value = (detail.extras || []).map((extra) => ({
      localId: localId(), code: extra.code, name: extra.name, price: Number(extra.price || 0),
      stock: Number(extra.stock || 0), sort: Number(extra.sort || 0),
    }))
  } catch (error) {
    ElMessage.error(`商品规格加载失败：${error.message}`)
    configurationDialogVisible.value = false
  }
}

function addOptionGroup() {
  configurationOptions.value.push({ localId: localId(), code: '', name: '', selectionType: 'SINGLE', required: true, minSelect: 1, maxSelect: 1, sort: configurationOptions.value.length, values: [] })
}

function removeOptionGroup(index) { configurationOptions.value.splice(index, 1) }

function addOptionValue(option) {
  option.values.push({ localId: localId(), code: '', name: '', priceDelta: 0, sort: option.values.length })
  if (option.selectionType === 'SINGLE') option.maxSelect = 1
}

function removeOptionValue(option, index) {
  option.values.splice(index, 1)
  if (option.selectionType === 'SINGLE') option.maxSelect = 1
  else option.maxSelect = Math.max(1, Math.min(option.maxSelect, option.values.length || 1))
  option.minSelect = Math.min(option.minSelect, option.values.length)
  if (option.required && option.values.length) option.minSelect = Math.max(1, option.minSelect)
}

function changeSelectionType(option) {
  if (option.selectionType === 'SINGLE') {
    option.maxSelect = 1
    option.minSelect = option.required && option.values.length ? 1 : Math.min(option.minSelect, 1)
  } else {
    option.maxSelect = Math.max(1, Math.min(option.maxSelect, option.values.length || 1))
  }
}

async function saveConfiguration() {
  for (const option of configurationOptions.value) {
    if (!option.name.trim() || !option.values.length || option.values.some((value) => !value.name.trim())) {
      ElMessage.warning('请填写规格组名称，并为每组添加名称完整的选项值')
      return
    }
    if (option.required && option.minSelect < 1) {
      ElMessage.warning(`必选规格“${option.name}”的最少选择数不能为 0`)
      return
    }
    if (option.minSelect > option.maxSelect || option.maxSelect > option.values.length) {
      ElMessage.warning(`请检查规格“${option.name}”的最少和最多选择数`)
      return
    }
  }
  if (configurationExtras.value.some((extra) => !extra.name.trim())) {
    ElMessage.warning('请填写所有加料名称')
    return
  }
  saving.value = true
  try {
    const payload = {
      options: configurationOptions.value.map((option, optionIndex) => ({
        code: option.code || createCode('OPT'), name: option.name.trim(),
        selectionType: option.selectionType, required: option.required,
        minSelect: Number(option.minSelect), maxSelect: Number(option.maxSelect), sort: optionIndex,
        values: option.values.map((value, valueIndex) => ({
          code: value.code || createCode('VAL'), name: value.name.trim(),
          priceDelta: Number(value.priceDelta), sort: valueIndex,
        })),
      })),
      extras: configurationExtras.value.map((extra, index) => ({
        code: extra.code || createCode('EXT'), name: extra.name.trim(), price: Number(extra.price),
        stock: Number(extra.stock), sort: index,
      })),
    }
    await updateProductConfiguration(configurationProduct.value.id, payload)
    ElMessage.success('商品规格与加料已保存')
    configurationDialogVisible.value = false
    await loadProducts()
  } catch (error) {
    ElMessage.error(error.message || '规格与加料保存失败')
  } finally {
    saving.value = false
  }
}

async function saveInventory() {
  try {
    await inventoryFormRef.value.validate()
    saving.value = true
    await adjustProductInventory(inventoryProduct.value.id, { delta: Number(inventoryForm.delta), reason: inventoryForm.reason.trim() })
    ElMessage.success('库存已调整，变更记录已保存')
    inventoryDialogVisible.value = false
    await loadProducts()
  } catch (error) {
    if (error instanceof Error) ElMessage.error(error.message || '库存调整失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => { loadCategories(); loadProducts() })
</script>

<style scoped lang="scss">
.product-page { display: flex; flex-direction: column; gap: 20px; }
.product-toolbar { display: flex; align-items: center; justify-content: space-between; }
.product-toolbar h2 { margin: 0; font-size: 22px; }
.product-toolbar p { margin: 8px 0 0; color: #8f8174; font-size: 13px; }
.product-card { border-color: #e9e0d6; border-radius: 12px; }
.filter-form { display: flex; flex-wrap: wrap; gap: 4px 10px; }
.filter-form :deep(.el-input) { width: 190px; }
.filter-form :deep(.el-select) { width: 155px; }
.product-error { margin-bottom: 16px; }
.product-name-cell { display: flex; align-items: center; gap: 12px; }
.product-name-cell strong, .product-name-cell span { display: block; }
.product-name-cell strong { color: #433126; font-size: 13px; }
.product-name-cell span { margin-top: 5px; color: #9a8b7e; font-size: 11px; }
.product-thumb { width: 46px; height: 46px; flex: 0 0 auto; overflow: hidden; border-radius: 8px; background: #f1e8de; }
.product-thumb__empty { display: grid; place-items: center; color: #806652; font-size: 19px; }
.stock-low { color: #c45656; font-weight: 600; }
.muted { color: #b8ada3; }
.pagination { display: flex; justify-content: flex-end; padding-top: 20px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; }
.form-tip { margin: -12px 0 0 100px; color: #9a8b7e; font-size: 12px; }
.field-gap { margin: 0 10px 0 18px; color: #606266; }
.inventory-summary { display: flex; justify-content: space-between; margin: 0 0 24px 95px; color: #716357; }
.inventory-hint { margin: -10px 0 20px 95px; }
.configuration-content { max-height: 68vh; overflow: auto; padding: 0 4px; }
.configuration-heading, .configuration-card__header, .configuration-subheading { display: flex; align-items: center; justify-content: space-between; }
.configuration-heading { margin: 22px 0 12px; }
.configuration-heading h3 { margin: 0; color: #433126; font-size: 16px; }
.configuration-card { margin-bottom: 12px; border-color: #eee4da; }
.configuration-card__header strong, .configuration-subheading strong { color: #6c523e; font-size: 13px; }
.configuration-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; align-items: end; }
.configuration-grid label { display: flex; flex-direction: column; gap: 7px; color: #887767; font-size: 12px; }
.configuration-grid :deep(.el-select), .configuration-grid :deep(.el-input-number) { width: 100%; }
.required-field { min-height: 54px; justify-content: center; }
.configuration-subheading { margin: 20px 0 10px; }
.value-row, .extra-editor-row { display: flex; align-items: center; gap: 10px; margin-top: 9px; }
.value-row :deep(.el-input), .extra-editor-row > :deep(.el-input) { flex: 1; }
.value-row :deep(.el-input-number), .extra-editor-row :deep(.el-input-number) { width: 140px; }
.unit-label { flex: 0 0 auto; color: #9a8b7e; font-size: 12px; white-space: nowrap; }
.configuration-tip { margin-bottom: 0; color: #a09284; font-size: 12px; }
@media (max-width: 720px) { .product-toolbar { align-items: flex-start; gap: 12px; } .filter-form { display: block; } .filter-form :deep(.el-form-item) { margin-right: 8px; } .filter-form :deep(.el-input), .filter-form :deep(.el-select) { width: min(190px, 56vw); } .pagination { justify-content: center; overflow-x: auto; } }
</style>
