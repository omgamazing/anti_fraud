<template>
  <div class="card" style="margin-bottom: 5px">
    <!-- 搜索栏 -->
    <div class="search-bar" >
      <el-input
        v-model="state.searchKeyword"
        prefix-icon="Search"
        placeholder="请输入题目"
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

    <div >
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <el-table
      :data="state.tableData"
      stripe
      style="width: 100%; margin-top: 20px"
      v-loading="state.loading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50"  />
      <el-table-column prop="title" label="题目" min-width="250" show-overflow-tooltip />
      <el-table-column prop="type" label="题型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getTypeTag(row.type)">
            {{ getTypeName(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="140" align="center">
        <template #default="{ row }">
          {{ getCategoryName(row.category) }}
        </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="toggleStatus(row)"
          />
        </template>
      </el-table-column>
      <!-- 内容查看列 -->
      <el-table-column prop="content" label="详情" width="100" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" >
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
        layout="total,prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>



    <!-- 查看/新增/编辑弹窗 -->
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
          <el-radio-group v-model="state.formData.type"
          :disabled="state.mode === 'view' || state.mode === 'edit'"
          >
            <el-radio :value="1">单选题</el-radio>
            <el-radio :value="2">多选题</el-radio>
            <el-radio :value="3">判断题</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="state.formData.category" :disabled="state.mode === 'view'" placeholder="请选择分类">
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
            :disabled="state.mode === 'view'"
            placeholder="请输入题目内容"
          />
        </el-form-item>

        <!-- 单选题/多选题选项 -->
        <template v-if="state.formData.type !== 3">
          <el-form-item
            v-for="opt in ['A', 'B', 'C', 'D']"
            :key="opt"
            :label="`选项${opt}`"
            :prop="`option${opt}`"
            required
          >
            <el-input
              v-model="state.formData[`option${opt}`]"
              :disabled="state.mode === 'view'"
              :placeholder="`请输入选项${opt}内容`"
            />
          </el-form-item>
        </template>

        <el-form-item label="正确答案" prop="answer">
          <!-- 判断题用下拉选择 -->
          <el-select
          v-if="state.formData.type === 3"
          v-model="state.formData.answer"
          :disabled="state.mode === 'view'"
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
          :disabled="state.mode === 'view'"
          :placeholder="answerPlaceholder"
          />

          <div v-if="state.mode !== 'view'" style="font-size: 12px; color: #999; margin-top: 5px">
            {{ answerTip }}
          </div>

        </el-form-item>



        <el-form-item label="答案解析" prop="analysis">
          <el-input
            v-model="state.formData.analysis"
            type="textarea"
            :rows="3"
            :disabled="state.mode === 'view'"
            placeholder="请输入答案解析"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button
         @click="state.dialogVisible = false">{{ state.mode === 'view' ? '关闭' : '取消' }}</el-button>
        <el-button
         v-if="state.mode !== 'view'"
         type="primary"
         @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { Edit, Delete} from '@element-plus/icons-vue'

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
  mode: 'add', // 'add' | 'edit' | 'view'
  dialogVisible: false,
  //dialogTitle: '',
  //isEdit: false,
  detailVisible: false,
  //detailData: {},

  // 题库的数据结构
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
// 自定义校验：正确答案格式
const validateAnswer = (rule, value, callback) => {
  // 判断题不校验格式
  if (state.formData.type === 3) {
    callback()
    return
  }

  const answer = value?.toUpperCase().trim() || ''
  const pattern = /^[A-D]+(?:,[A-D]+)*$/

  if (!pattern.test(answer)) {
    callback(new Error('只能输入A、B、C、D四个字母'))
  } else {
    state.formData.answer = answer
    callback()
  }
}

// 自定义校验：题目不能为空格
const validateTitle = (rule, value, callback) => {
  if (value && value.trim() === '') {
    callback(new Error('题目不能为空格'))
  } else {
    callback()
  }
}

// 表单校验规则
const formRules = {
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  title: [
      { required: true, message: '请输入题目', trigger: 'blur' },
      { validator: validateTitle, trigger: 'blur' }
    ],
    answer: [
      { required: true, message: '请输入正确答案', trigger: 'blur' },
      { validator: validateAnswer, trigger: 'blur' }
    ]
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
  if (state.formData.type === 2) return '请输入多个正确答案选项（如：AC）'
  if (state.formData.type === 3) return '请选择正确答案'
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


const dialogTitle = computed(() => {
  if (state.mode === 'add') return '新增题目'
  if (state.mode === 'edit') return '编辑题目'
  return '题目详情'
})
// 新增，重置state.formData
const handleAdd = () => {
  state.mode = 'add'
  //state.isEdit = false
  //state.dialogTitle = '新增题目'
  Object.assign(state.formData, {
    id: null,
    type: 1,
    category: '',
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
  state.mode = 'edit'
  //state.isEdit = true
  Object.assign(state.formData, row)
  state.dialogVisible = true
}

// 查看详情
const viewDetail = (row) => {
  state.mode = 'view'
  Object.assign(state.formData, row)
  state.dialogVisible = true
}

// 校验选项（单选/多选题专用）
const validateOptions = () => {
  // 判断题不需要校验选项
  if (state.formData.type === 3) {
    return true
  }

  const options = [
    { key: 'optionA', name: '选项A' },
    { key: 'optionB', name: '选项B' },
    { key: 'optionC', name: '选项C' },
    { key: 'optionD', name: '选项D' }
  ]

  for (let opt of options) {
    if (!state.formData[opt.key] || !state.formData[opt.key].trim()) {
      ElMessage.error(`${opt.name}不能为空`)
      return false
    }
  }
  return true
}

// 提交表单
const submitForm = async () => { await formRef.value.validate()
   if (state.mode === 'view') return

  //formRules表单规则校验：判断基础字段是否填写
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  //选项校验（单选/多选），判定四个选项是否都有值
  if (!validateOptions()) return

  //提交表单
  const url = state.mode === 'edit' ? '/exam/update' : '/exam/add'
  const method = state.mode === 'edit' ? 'put' : 'post'

  try {
    const res = await request[method](url, state.formData)
    if (res.code === '200') {
      ElMessage.success(state.mode === 'edit'? '编辑成功' : '新增成功')
      state.dialogVisible = false
      loadData()
    } else {
      ElMessage.error(res.msg)
      ElMessage.error("表单提交失败")
    }
  } catch (error) {
    console.log('操作失败', error)
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
        ElMessage.error('删除失败')
      }
    } catch (error) {
      ElMessage.error('请求异常')
    }
  }).catch(() => {})  // 用户点"取消"，什么都不做，不需要提示
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
        ElMessage.error('删除失败')
      }
    } catch (error) {
      ElMessage.error('请求异常')
    }
  }).catch(() => {})
}

// 切换状态
const toggleStatus = (row) => {
  const originalStatus = row.status
  request.put('/exam/update', { id: row.id, status: row.status })
    .then(res => {
      if (res.code !== '200') throw new Error()
    })
    .catch(() => {
      row.status = originalStatus === 1 ? 0 : 1
      ElMessage.error('更新状态失败')
    })
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card {
  background: white;
  padding: 10px 20px;
  border-radius: 8px;
}

.search-bar {
  background: white;
  padding: 10px 10px 15px 0px;  /* 上 右 下 左 */
  margin-bottom: 5px;
  border-radius: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.pagination {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  background: white;
  padding: 20px;
  border-radius: 8px;
}
</style>