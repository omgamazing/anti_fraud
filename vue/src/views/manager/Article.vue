<template>
  <div class="card" style="margin-bottom: 5px">
    <div class="search-bar">
      <el-input
       v-model="data.title"
       prefix-icon="Search"
       style="width: 240px;
       margin-right: 10px"
       placeholder="请输入帖子名称"
       clearable
       @clear="load"
       @keyup.enter="load"
       ></el-input>
       <el-select
        v-model="data.status" placeholder="审核状态" style="width: 120px;margin-right: 10px"
         clearable @change="load">
         <el-option label="待审核" value="待审核" />
         <el-option label="审核通过" value="审核通过" />
         <el-option label="审核拒绝" value="审核拒绝" />
       </el-select>
       <!-- 反诈分类下拉框 -->
       <el-select
         v-model="data.categoryId"
         placeholder="反诈分类"
         style="width: 140px; margin-right: 10px"
         clearable
         @change="load"
       >
       <el-option
         v-for="item in data.categoryList"
         :key="item.id"
         :label="item.name"
         :value="item.id"
       />
       </el-select>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div >
        <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>


    <div >
      <el-table
      stripe
      :data="data.tableData"
      style="width: 100%; margin-top: 20px"
      @selection-change="handleSelectionChange"
      v-loading="loading"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="title" label="帖子名称" width="200" show-overflow-tooltip />
        <el-table-column prop="img" label="主图" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.img"
              style="width: 40px; height: 40px; border-radius: 5px; display: block; margin: 0 auto"
              :src="row.img"
              :preview-src-list="[row.img]"
              preview-teleported
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="反诈分类" align="center"/>
        <el-table-column prop="userName" label="发布者" align="center"/>
        <el-table-column prop="time" label="发布时间" width="140" align="center"/>
        <el-table-column prop="views" label="浏览量" width="70" align="center"/>
        <el-table-column prop="status" label="审核状态" width="100" align="center">
          <template v-slot="scope">
            <el-tag type="warning" v-if="scope.row.status === '待审核'">{{ scope.row.status }}</el-tag>
            <el-tag type="success" v-if="scope.row.status === '审核通过'">{{ scope.row.status }}</el-tag>
            <el-tag type="danger" v-if="scope.row.status === '审核拒绝'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
       <el-table-column prop="reason" label="审核说明" min-width="110" align="center" show-overflow-tooltip />



     <!-- 内容查看列 -->
     <el-table-column prop="content" label="详情" width="100" align="center">
       <template #default="{ row }">
         <el-button type="primary" link @click="viewInit(row.content)">查看</el-button>
       </template>
     </el-table-column>

     <!-- 操作列（审核 + 删除） -->
     <el-table-column label="操作" width="120" align="center" fixed="right">
       <template #default="{ row }">
         <el-button
           v-if="row.status === '待审核'"
           type="primary"
           circle
           :icon="Edit"
           @click="handleEdit(row)"
         />
         <el-button type="danger" circle :icon="Delete" @click="del(row.id)" />
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
       layout="total,prev, pager, next, jumper"
       @size-change="load"
       @current-change="load"
      />
    </div>

    <el-dialog title="审核信息" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form :model="data.form" label-width="80px" style="padding: 20px">
        <el-form-item prop="status" label="审核状态">
          <el-select v-model="data.form.status" placeholder="请选择状态">
            <el-option value="待审核" label="待审核"></el-option>
            <el-option value="审核通过" label="审核通过"></el-option>
            <el-option value="审核拒绝" label="审核拒绝"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="reason" label="审核说明">
          <el-input v-model="data.form.reason" placeholder="请输入审核说明"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="update">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="帖子内容" v-model="data.viewVisible" width="50%" destroy-on-close>
      <div style="padding: 10px 20px" v-html="data.viewContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>

import {reactive, ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import { View, Delete, Edit} from "@element-plus/icons-vue";


const data = reactive({
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  title: null,
  status:null,
  categoryId: null,        // 选中的分类ID
  categoryList: [],        // 分类列表
  ids: [],
  rules: {
    status: [
      { required: true, message: '请选择审核状态', trigger: 'blur' }
    ],
  },
  viewContent: null,
  viewVisible: false,

})

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
      status: data.status,
       categoryId: data.categoryId
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    }
  })
}
const loadCategoryList = () => {
  request.get('/category/selectAll').then(res => {
    console.log('分类接口完整返回:', res)
    console.log('res.data:', res.data)
    if (res.code === '200') {
      data.categoryList = res.data || []
      console.log('第一条数据:', data.categoryList[0])  // 查看字段名
    } else {
      ElMessage.error(res.msg)
    }
  })
}
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}
const update = () => {
  request.put('/article/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/article/delete/' + id).then(res => {
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
    request.delete("/article/delete/batch", {data: data.ids}).then(res => {
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
  data.status = null
  data.categoryId = null
  data.pageNum=1
  load()
}

loadCategoryList()
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