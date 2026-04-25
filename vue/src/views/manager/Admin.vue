<template>
  <div class="card" style="margin-bottom: 5px">
    <div class="search-bar">
      <el-input v-model="data.keyword" prefix-icon="Search" style="width: 240px; margin-right: 10px"
      placeholder="请输入账号/昵称查询"
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

    <div >
      <el-table stripe :data="data.tableData"
      @selection-change="handleSelectionChange"
      style="width: 100%; margin-top: 20px"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="avatar" label="头像" width="100">
          <template v-slot="scope">
            <el-image style="width: 40px; height: 40px; border-radius: 50%; display: block; cursor: pointer" v-if="scope.row.avatar"
                      :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="昵称" align="center" show-overflow-tooltip />
        <el-table-column prop="role" label="角色" align="center"/>
        <el-table-column prop="phone" label="电话" align="center"/>
        <el-table-column prop="email" label="邮箱" align="center"/>
        <el-table-column label="操作" width="180" fixed="right" align="center">
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
    <el-dialog title="管理员信息" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form ref="form" :model="data.form" label-width="70px" style="padding: 20px">
        <el-form-item prop="username" label="账号" required>
          <el-input v-model="data.form.username" placeholder="请输入账号"></el-input>
        </el-form-item>

        <!-- 头像上传组件 -->
        <el-form-item prop="avatar" label="头像">
          <el-upload
            :action="baseUrl + '/files/upload'"
            :on-success="handleImgUpload"
            :on-remove="handleRemove"
            :file-list="data.fileList"
            list-type="picture"
          >
            <el-button type="primary">
              {{ data.form.avatar ? '重新上传' : '点击上传' }}
            </el-button>
          </el-upload>
        </el-form-item>

        <el-form-item prop="name" label="昵称" show-overflow-tooltip >
          <el-input v-model="data.form.name" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item prop="phone" label="电话">
          <el-input v-model="data.form.phone" placeholder="请输入电话"></el-input>
        </el-form-item>
        <el-form-item prop="email" label="邮箱">
          <el-input v-model="data.form.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import {reactive, ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  keyword: null,
  ids: [],
  fileList: [],  //添加文件列表
})

const load = () => {
  request.get('/admin/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      keyword: data.keyword
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
  data.fileList = []  //清空文件列表
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  //设置文件列表显示原有头像
  if (row.avatar) {
    data.fileList = [{ url: row.avatar }]
  } else {
    data.fileList = []
  }
  data.formVisible = true
}

const add = () => {
  request.post('/admin/add', data.form).then(res => {
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
  request.put('/admin/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    }
  })
}

const save = () => {
  data.form.id ? update() : add()
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/admin/delete/' + id).then(res => {
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
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/admin/delete/batch", {data: data.ids}).then(res => {
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

// 图片上传成功
const handleImgUpload = (res) => {
  data.form.avatar = res.data
  data.fileList = [{ url: res.data }]  // 更新文件列表
}

// 删除图片
const handleRemove = () => {
  data.form.avatar = null
  data.fileList = []
}

const reset = () => {
  data.keyword = null
  load()
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