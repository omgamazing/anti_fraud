<template>
  <div class="exam-question-manage" style="margin-bottom: 5px">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="state.searchKeyword"
        prefix-icon="Search"
        placeholder="请输入题目关键词"
        style="width: 250px; margin-right: 10px"
        clearable
        @clear="loadData"
        @keyup.enter="loadData"
      />
      <el-select v-model="state.searchType" placeholder="题型" style="width: 100px; margin-right: 10px" clearable @change="loadData">
        <el-option label="单选题" :value="1" />
        <el-option label="多选题" :value="2" />
        <el-option label="判断题" :value="3" />
      </el-select>
      <el-select v-model="state.searchCategory" placeholder="分类" style="width: 100px; margin-right: 10px" clearable @change="loadData">
        <el-option label="反诈基础" :value="1" />
        <el-option label="信息网络安全" :value="2" />
        <el-option label="资金应急处置" :value="3" />
      </el-select>
       <el-select v-model="state.searchStatus" placeholder="状态" style="width: 100px; margin-right: 10px" clearable @change="loadData">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
       </el-select>
      <el-button type="info" plain @click="loadData">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <el-table
      :data="state.tableData"
      border
      stripe
      style="width: 100%; margin-top: 20px"
      v-loading="state.loading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="title" label="题目" min-width="300" show-overflow-tooltip />
      <el-table-column prop="type" label="题型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getTypeTag(row.type)">
            {{ getTypeName(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="120" align="center">
        <template #default="{ row }">
          {{ getCategoryName(row.category) }}
        </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="toggleStatus(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" circle :icon="Edit" @click="openEditDialog(row)"></el-button>
          <el-button type="danger" circle :icon="Delete" @click="handleDelete(row)"></el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="state.pageNum"
        v-model:page-size="state.pageSize"
        :total="state.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="state.dialogVisible"
      :title="state.dialogTitle"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="state.formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="题型" prop="type">
          <el-radio-group v-model="state.formData.type">
            <el-radio :value="1">单选题</el-radio>
            <el-radio :value="2">多选题</el-radio>
            <el-radio :value="3">判断题</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="state.formData.category" placeholder="请选择分类">
            <el-option label="反诈基础" :value="1" />
            <el-option label="信息网络安全" :value="2" />
            <el-option label="资金应急处置" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="题目" prop="title">
          <el-input
            v-model="state.formData.title"
            type="textarea"
            :rows="3"
            placeholder="请输入题目内容"
          />
        </el-form-item>

        <!-- 单选题/多选题选项 -->
        <template v-if="state.formData.type !== 3">
          <el-form-item label="选项A" prop="optionA">
            <el-input v-model="state.formData.optionA" placeholder="请输入选项A内容" />
          </el-form-item>
          <el-form-item label="选项B" prop="optionB">
            <el-input v-model="state.formData.optionB" placeholder="请输入选项B内容" />
          </el-form-item>
          <el-form-item label="选项C" prop="optionC">
            <el-input v-model="state.formData.optionC" placeholder="请输入选项C内容" />
          </el-form-item>
          <el-form-item label="选项D" prop="optionD">
            <el-input v-model="state.formData.optionD" placeholder="请输入选项D内容" />
          </el-form-item>
        </template>

        <el-form-item label="正确答案" prop="answer">
          <!-- 判断题用下拉选择 -->
          <el-select
          v-if="state.formData.type === 3"
          v-model="state.formData.answer"
          placeholder="请选择正确答案" style="width: 100%">
           <el-option label="正确" :value="'√'">
             <el-icon><Check /></el-icon> 正确
           </el-option>
           <el-option label="错误" :value="'×'">
             <el-icon><Close /></el-icon> 错误
           </el-option>
          </el-select>

          <!-- 单选/多选题用输入框 -->
          <el-input
          v-else
          v-model="state.formData.answer"
          :placeholder="answerPlaceholder"
          />

          <div style="font-size: 12px; color: #999; margin-top: 5px">
            {{ answerTip }}
          </div>

        </el-form-item>



        <el-form-item label="答案解析" prop="analysis">
          <el-input
            v-model="state.formData.analysis"
            type="textarea"
            :rows="3"
            placeholder="请输入答案解析"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="state.dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { Edit, Delete } from '@element-plus/icons-vue'

// 全部用 reactive 管理
const state = reactive({
  // 表格数据
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  loading: false,
  selectedRows: [], // 选中的行

  // 搜索条件
  searchKeyword: '',
  searchType: '',
  searchCategory: '',
  searchStatus: '',

  // 弹窗
  dialogVisible: false,
  dialogTitle: '新增题目',
  isEdit: false,

  // 表单数据
  formData: {
    id: null,
    type: 1,
    category: 1,
    title: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    answer: '',
    analysis: '',
    status: 1
  }
})

const formRef = ref(null)

// 表单校验规则
const formRules = {
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入题目', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }],
}

// 辅助方法
const getTypeName = (type) => {
  const map = { 1: '单选题', 2: '多选题', 3: '判断题' }
  return map[type]
}

const getTypeTag = (type) => {
  const map = { 1: 'primary', 2: 'success', 3: 'warning' }
  return map[type]
}

const getCategoryName = (category) => {
  const map = { 1: '反诈基础', 2: '信息网络安全', 3: '资金应急处置' }
  return map[category] || '未知'
}

const answerPlaceholder = computed(() => {
  if (state.formData.type === 2) return '多选答案，如：AC'
  if (state.formData.type === 3) return '请选择正确或错误'
  return '请输入正确答案选项（如：A）'
})

const answerTip = computed(() => {
  if (state.formData.type === 2) return '示例：正确答案是A和C，则填写"AC"'
  if (state.formData.type === 3) return '请选择正确或错误'
  return '示例：正确答案是A，则填写"A"'
})

// 选中行变化
const handleSelectionChange = (rows) => {
  state.selectedRows = rows
}

// 重置搜索
const reset = () => {
  state.searchKeyword = ''
  state.searchType = ''
  state.searchCategory = ''
  state.pageNum = 1
  loadData()
}

// 加载数据
const loadData = () => {
  state.loading = true
  request.get('/exam/selectPage', {
    params: {
      pageNum: state.pageNum,
      pageSize: state.pageSize,
      title: state.searchKeyword,
      type: state.searchType,
      category: state.searchCategory,
      status:state.searchStatus
    }
  }).then(res => {
    if (res.code === '200') {
      state.tableData = res.data?.list || []
      state.total = res.data?.total || 0
      console.log('加载数据成功', state.tableData)
    } else {
      ElMessage.error(res.msg)
      ElMessage.error("题库加载失败")
      console.log('加载数据失败', res.msg)
    }
  }).catch(err => {
    console.log('错误:', err)
    ElMessage.error('加载数据失败')
  }).finally(() => {
    state.loading = false
  })
}

// 新增
const handleAdd = () => {
  state.isEdit = false
  state.dialogTitle = '新增题目'
  Object.assign(state.formData, {
    id: null,
    type: 1,
    category: 1,
    title: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    answer: '',
    analysis: '',
    status: 1
  })
  state.dialogVisible = true
}

// 编辑
const openEditDialog = (row) => {
  state.isEdit = true
  state.dialogTitle = '编辑题目'
  Object.assign(state.formData, row)
  state.dialogVisible = true
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate()

    const url = state.isEdit ? '/exam/update' : '/exam/add'
    const method = state.isEdit ? 'put' : 'post'
    const res = await request[method](url, state.formData)

    if (res.code === '200') {
      ElMessage.success(state.isEdit ? '编辑成功' : '新增成功')
      state.dialogVisible = false
      loadData()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    console.log('表单校验失败', error)
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除题目「${row.title}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete('/exam/delete/' + row.id)
      if (res.code === '200') {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.msg)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 批量删除
const delBatch = () => {
  if (state.selectedRows.length === 0) {
    ElMessage.warning('请先选中要删除的题目')
    return
  }

  ElMessageBox.confirm(`确定删除选中的 ${state.selectedRows.length} 个题目吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = state.selectedRows.map(row => row.id)
      const res = await request.delete('/exam/delete/batch', { data: ids })
      if (res.code === '200') {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.msg)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 切换状态
const toggleStatus = async (row) => {
  try {
    const res = await request.put('/exam/update', {
      id: row.id,
      status: row.status
    })
    if (res.code !== '200') {
      row.status = row.status === 1 ? 0 : 1
      ElMessage.error('更新状态失败')
    }
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('更新状态失败')
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>

.search-bar {
  background: white;
  padding: 10px;
  border-radius: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.card {
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: white;
  padding: 20px;
  border-radius: 8px;
}
</style>