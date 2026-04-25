<template>
  <div class="card" style="margin-bottom: 5px">
    <div class="search-bar" >
      <el-input
       v-model="data.title"
       prefix-icon="Search"
       style="width: 240px;
       margin-right: 10px"
       placeholder="请输入案例"
       clearable
       @clear="load"
       @keyup.enter="load"
      ></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div >
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div>
      <el-table
      stripe
      :data="data.tableData"
      style="width: 100%; margin-top: 20px"
      @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="title" label="案例标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="img" label="封面" width="140" align="center">
          <template v-slot="scope" >
            <el-image
             style="width: 40px; height: 40px; border-radius: 5px; display: inline-block; cursor: pointer"
             v-if="scope.row.img"
             :src="scope.row.img"
             :preview-src-list="[scope.row.img]"
             preview-teleported
             fit="cover"
             @click="goToDetail(scope.row)"
             ></el-image>
          </template>
        </el-table-column>

        <el-table-column prop="time" label="发布时间" width="200" align="center"/>
        <el-table-column prop="views" label="浏览量" width="100"  align="center"/>
        <el-table-column prop="content" label="详情" width="100" align="center">
          <template v-slot="scope">
            <el-button type="primary" link @click="viewInit(scope.row.content)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240"  align="center">
          <template v-slot="scope">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)"></el-button>
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
       <el-pagination
        v-model:current-page="data.pageNum"
        v-model:page-size="data.pageSize"
        :total="data.total"
         layout="total, prev, pager, next, jumper"
         @size-change="load"
         @current-change="load"
        />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog title="案例信息" v-model="data.formVisible" width="50%" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px" style="padding: 20px">
        <el-form-item prop="title" label="案例标题">
          <el-input v-model="data.form.title" placeholder="请输入案例标题"></el-input>
        </el-form-item>

        <!-- 图片上传组件 -->
        <el-form-item prop="img" label="案例图片">
          <el-upload
            :action="baseUrl + '/files/upload'"
            :on-success="handleImgUpload"
            :on-remove="handleRemove"
            :file-list="data.fileList"
            list-type="picture"
          >
            <el-button type="primary">
              {{ data.form.img ? '重新上传' : '点击上传' }}
            </el-button>
          </el-upload>
        </el-form-item>

        <el-form-item prop="content" label="案例内容">
          <div style="border: 1px solid #ccc; width: 100%">
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

    <el-dialog title="案例内容" v-model="data.viewVisible" width="50%" destroy-on-close>
      <div style="padding: 10px 20px" v-html="data.viewContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>

import {reactive, ref, onBeforeUnmount, shallowRef} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const formRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 5,
  total: 0,
  title: null,
  categoryData: [],
  ids: [],
  fileList: [],  //添加文件列表
  rules: {
    title: [
      { required: true, message: '请输入案例标题', trigger: 'blur' },
    ],
    img: [
      { required: true, message: '请上传案例封面', trigger: 'blur' },
    ],
    content: [
      { required: true, message: '请输入案例内容', trigger: 'blur' },
    ],
  },
  viewContent: null,
  viewVisible: false,
})

/* wangEditor5 初始化开始 */
const editorRef = shallowRef()
const mode = 'default'
const editorConfig = { MENU_CONF: {} }
editorConfig.MENU_CONF['uploadImage'] = {
  headers: {
    token: data.user.token,
  },
  server: baseUrl + '/files/wang/upload',
  fieldName: 'file'
}
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
const handleCreated = (editor) => {
  editorRef.value = editor
}
/* wangEditor5 初始化结束 */

const viewInit = (content) => {
  data.viewContent = content
  data.viewVisible = true
}

const load = () => {
  request.get('/case/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    }
  })
}

// 新增
const handleAdd = () => {
  data.form = {}
  data.fileList = []
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  // 设置文件列表显示原有图片
  if (row.img) {
    data.fileList = [{ url: row.img }]
  } else {
    data.fileList = []
  }
  data.formVisible = true
}

const add = () => {
  request.post('/case/add', data.form).then(res => {
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
  request.put('/case/update', data.form).then(res => {
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
    request.delete('/case/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}

const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.delete("/case/delete/batch", {data: data.ids}).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const reset = () => {
  data.title = null
  load()
}

// 图片上传成功
const handleImgUpload = (res) => {
  data.form.img = res.data
  data.fileList = [{ url: res.data }]
}

// 删除图片
const handleRemove = () => {
  data.form.img = null
  data.fileList = []
}

// 点击图片跳转详情（如果需要的话）
const goToDetail = (row) => {
  // 如果需要点击图片跳转，在这里添加逻辑
  console.log('点击图片', row)
}

load()
</script>

<style scoped>
.card {
  background: white;
  padding: 10px 20px;
  border-radius: 8px;
}
.search-bar {
  background: white;
  padding: 10px 10px 15px 0px;
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