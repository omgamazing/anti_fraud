<template>
  <div class="card" style="margin-bottom: 5px">
    <div class="search-bar" >
      <el-input
        v-model="data.articleTitle"
        prefix-icon="Search"
        style="width: 240px;
        margin-right: 10px"
        placeholder="请输入帖子名称查询"
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
      style="width: 100%; margin-top: 20px"
      @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="img" label="展示图" width="200">
          <template v-slot="scope">
            <el-image style="width: 100px; height: 50px; border-radius: 5px; display: block"
             v-if="scope.row.img"
             :src="scope.row.img"
             :preview-src-list="[scope.row.img]"
             preview-teleported
             fit="cover"
             ></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="articleTitle" label="帖子标题" align="center" />
        <el-table-column label="操作" width="240" align="center">
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

    <el-dialog title="轮播图信息" v-model="data.formVisible" width="50%" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px" style="padding: 20px">
        <el-form-item prop="img" label="图片">
          <el-upload
              :action="baseUrl + '/files/upload'"
              :on-success="handleImgUpload"
              list-type="picture"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="articleId" label="选择帖子">
          <el-select v-model="data.form.articleId" placeholder="请选择帖子">
            <el-option
                v-for="item in data.articleData"
                :key="item.id"
                :label="item.title"
                :value="item.id"
            />
          </el-select>
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
import {Editor, Toolbar} from "@wangeditor/editor-for-vue";

const formRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  articleTitle: null,
  ids: [],
  articleData: [],
  rules: {
    img: [
      { required: true, message: '请上传轮播图图片', trigger: 'blur' },
    ],
    articleId: [
      { required: true, message: '请选择帖子', trigger: 'blur' },
    ],
  }
})

const loadArticle = () => {
  request.get('/article/selectAll', {
    params: {
      status: '审核通过'
    }
  }).then(res => {
    if (res.code === '200') {
      data.articleData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadArticle()

const load = () => {
  request.get('/carousel/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      articleTitle: data.articleTitle
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    }
  })
}
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
  console.log(data.form)
}
const add = () => {
  request.post('/carousel/add', data.form).then(res => {
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
  request.put('/carousel/update', data.form).then(res => {
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
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/carousel/delete/' + id).then(res => {
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
    request.delete("/carousel/delete/batch", {data: data.ids}).then(res => {
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
  data.articleTitle = null
  load()
}

const handleImgUpload = (res) => {
  data.form.img = res.data
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