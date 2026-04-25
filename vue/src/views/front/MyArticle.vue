<template>
  <div style="width: 70%; margin: 20px auto; min-height: 100vh">
    <div style="font-size: 18px; font-weight: bold; margin-bottom: 16px">
      📝 我的帖子
    </div>

    <div class="card" style="padding: 10px; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05)">
      <!-- 搜索栏 -->
      <div style="margin-bottom: 20px; display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-input
          clearable
          @clear="reset"
          @keyup.enter="load"
          v-model="data.title"
          prefix-icon="Search"
          style="width: 240px"
          placeholder="请输入帖子标题查询"
          class="search-input"
        />
        <el-button type="info" plain @click="load" >查询</el-button>
        <el-button type="warning" plain style="margin:0px" @click="reset">重置</el-button>
        <el-button type="success" plain @click="handleAdd" style="margin-left:5px">发布帖子</el-button>
      </div>

      <!-- 表格 -->
      <el-table stripe :data="data.tableData" style="border-radius: 8px; overflow: hidden">
        <el-table-column prop="title" label="帖子名称" width="200" show-overflow-tooltip />
        <el-table-column prop="img" label="封面" width="80" align="center">
          <template v-slot="scope">
            <el-image
              style="width: 40px; height: 40px; border-radius: 5px"
              v-if="scope.row.img"
              :src="scope.row.img"
              :preview-src-list="[scope.row.img]"
              preview-teleported
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="反诈分类" width="80" />
        <el-table-column prop="time" label="发布时间" width="160" align="center"/>
        <el-table-column prop="content" label="详情" width="100" align="center">
          <template v-slot="scope">
            <el-button type="primary" link @click="viewInit(scope.row.content)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览量" width="80" align="center" />
        <el-table-column prop="status" label="审核状态" width="100" align="center">
          <template v-slot="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="审核说明" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)" />
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)" />
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="data.total" style="margin-top: 20px; display: flex; justify-content: flex-end">
        <el-pagination
          @current-change="load"
          background
          layout="total, prev, pager, next"
          :page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
        />
      </div>
    </div>

    <!-- 弹窗保持原样，只改圆角 -->
    <el-dialog title="帖子信息" v-model="data.formVisible" width="50%" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px" style="padding: 20px">
        <el-form-item prop="title" label="帖子标题">
          <el-input v-model="data.form.title" placeholder="请输入帖子标题"></el-input>
        </el-form-item>
        <el-form-item prop="img" label="帖子封面">
          <el-upload
              :action="baseUrl + '/files/upload'"
              :on-success="handleImgUpload"
              list-type="picture"
              :file-list="data.fileList"
          >
            <el-button type="primary">
              {{ data.form.img ? '重新上传' : '点击上传' }}
            </el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="categoryId" label="反诈分类">
          <el-select v-model="data.form.categoryId" placeholder="请选择反诈分类" style="width: 100%">
            <el-option
                v-for="item in data.categoryData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item prop="content" label="帖子内容">
          <div style="border: 1px solid #ccc; width: 100%; border-radius: 8px; overflow: hidden">
            <Toolbar
                style="border-bottom: 1px solid #ccc"
                :editor="editorRef"
                :mode="mode"
            />
            <Editor
                style="height: 500px; overflow-y: hidden;"
                v-model="data.form.content"
                :mode="mode"
                :defaultConfig="editorConfig"
                @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="帖子内容" v-model="data.viewVisible" width="50%" destroy-on-close>
      <div style="padding: 10px 20px" v-html="data.viewContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {Delete, Edit} from "@element-plus/icons-vue";
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const formRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL

// 获取状态样式
const getStatusType = (status) => {
  const map = { '待审核': 'warning', '审核通过': 'success', '审核拒绝': 'danger' }
  return map[status] || 'info'
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  pageNum: 1,
  pageSize: 5,
  form: {},
  formVisible: false,
  tableData: [],
  total: 0,
  categoryData: [],
  fileList: [],
  rules: {
    title: [{ required: true, message: '请输入帖子标题', trigger: 'blur' }],
    img: [{ required: true, message: '请上传帖子封面', trigger: 'blur' }],
    categoryId: [{ required: true, message: '请选择帖子分类', trigger: 'blur' }],
    content: [{ required: true, message: '请输入帖子内容', trigger: 'blur' }],
  },
  viewContent: null,
  viewVisible: false
})

// 富文本编辑器
const editorRef = shallowRef()
const mode = 'default'
const editorConfig = { MENU_CONF: {} }
editorConfig.MENU_CONF['uploadImage'] = {
  headers: { token: data.user.token },
  server: baseUrl + '/files/wang/upload',
  fieldName: 'file'
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) editor.destroy()
})

const handleCreated = (editor) => {
  editorRef.value = editor
}

const viewInit = (content) => {
  data.viewContent = content
  data.viewVisible = true
}

const load = () => {
  request.get('/article/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      userId: data.user.id
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total || 0
    }
  })
}
load()

const loadCategory = () => {
  request.get('/category/selectAll').then(res => {
    if (res.code === '200') {
      data.categoryData = res.data || []
    }
  })
}
loadCategory()

const handleAdd = () => {
  data.form = { userId: data.user.id, status: '待审核' }
  data.fileList = []
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  //设置文件列表显示原有图片
    if (row.img) {
      data.fileList = [{ url: row.img }]
    } else {
      data.fileList = []
    }
  data.formVisible = true
}

const add = () => {
  request.post('/article/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  data.form.status = '待审核'
  data.form.reason = ''
  request.put('/article/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    }
  })
}

const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? update() : add()
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/article/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const reset = () => {
  data.title = null
  load()
}

const handleImgUpload = (res) => {
  data.form.img = res.data
  data.fileList = [{ url: res.data }]
}
// 删除图片
const handleRemove = () => {
  data.form.img = null
  data.fileList = []
}
</script>

<style scoped>
.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}



/* 表格内按钮优化 */
.el-button.is-link {
  margin: 0 2px;
}
</style>