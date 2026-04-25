<template>
  <div style="width: 70%; margin: 20px auto; min-height: 100vh">
    <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 16px">
      <div style="font-size: 18px; font-weight: bold">
        💬 我的评论
       </div>
    </div>

    <!-- 搜索栏 -->
    <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: center">
      <el-input
        prefix-icon="Search"
        v-model="data.searchKey"
        placeholder="搜索评论内容..."
        style="width: 260px"
        @keyup.enter="load"
        clearable
        @clear="load"
      />
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin:0px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="background: white; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05)">
      <el-table stripe :data="data.tableData" style="border-radius: 8px; overflow: hidden">
        <el-table-column prop="articleTitle" label="帖子标题" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <a :href="'/front/articleDetail?id=' + row.articleId" style="color: #409eff; text-decoration: none">
              {{ row.articleTitle }}
            </a>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="time" label="评论时间" width="160" />
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" circle :icon="Delete" @click="del(row.id)" />
          </template>
        </el-table-column>
      </el-table>

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
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import router from "@/router/index.js";
import { Delete } from "@element-plus/icons-vue";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  searchKey: '',
  pageNum: 1,
  pageSize: 5,
  tableData: [],
  total: 0,
})

const reset = () => {
  data.searchKey = ''
  data.pageNum = 1
  load()
}

const load = () => {
  request.get('/comment/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id,
      content: data.searchKey
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total || 0
    }
  })
}
load()

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/comment/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}
</script>

<style scoped>
.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 链接样式 */
a:hover {
  text-decoration: underline !important;
}
</style>