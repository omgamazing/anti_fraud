<template>
  <div class="my-simulation-container">
    <div class="header">
      <h1> 考试记录📋</h1>
    </div>


    <div style="background-color: #f8f8f8; padding: 20px">
        <div style="width: 100%; margin: 0 auto ">
            <el-input
            prefix-icon="Search"
                v-model="data.searchKey"
                placeholder="请输入考试类型查询"
                style="width: 300px;height: 35px;margin-right: 18px"
                @keyup.enter="Search"
                clearable
                @clear="clearSearch"
            ></el-input>
            <el-button type="info" plain @click="Search">查询</el-button>
            <el-table
                :data="data.records"
                style="width: 100%; margin-top: 20px"
                :default-sort="{ prop: 'createTime', order: 'descending' }"
                >
                <el-table-column prop="examTypeName" label="考试类型" width="120" align="center" margin-left:10px/>
                <el-table-column prop="userScore" label="得分" sortable width="120" align="center"/>
                <el-table-column prop="duration" label="用时" sortable width="100" align="center">
                  <template #default="{ row }">
                    {{ formatDuration(row.duration) }}
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="考试时间" sortable width="150" align="center">
                    <template #default="{ row }">
                        {{ formatTime(row.createTime) }}
                    </template>
                </el-table-column>
                <el-table-column label="状态" sortable width="120" align="center">
                    <template #default="{row}">
                        <!-- 得分 ≥ 60 → 通过，否则未通过 -->
                        <el-tag :type="row.userScore >= 60 ? 'success' : 'danger'" disable-transitions="false">
                            {{ row.result === 'success' ? '通过' : '未通过' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="100" align="center">
                    <template #default="{row}">
                        <el-button type="primary" size="small" @click="showDetail(row)">详情</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div v-if="data.total" style="margin-top: 20px">
                <el-pagination
                    @current-change="load"
                    layout="total, prev, pager, next"
                    :page-size="data.pageSize"
                    v-model:current-page="data.pageNum"
                    :total="data.total"
                />
            </div>

         </div>
    </div>
    <!-- 记录列表 -->
    <div class="record-list">
        <div v-if="data.records.length === 0" class="empty-state">
            <div class="empty-icon">📭</div>
            <p>暂无考试记录</p>
            <button @click="goToExam" class="start-exam-btn">开始考试</button>
        </div>
    </div>


  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request.js'

const router = useRouter()
// 所有数据统一用 reactive 管理
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  records: [],
  searchKey: null,
  currentPage: 1,
  pageSize: 10,
})


// 获取记录列表
const fetchRecords = async () => {
    const userId = data.user.id
    console.log('userId',userId)

    request.get('/exam/record/statistics', {
        params: { userId }
      }).then(res => {
        if (res.code === '200') {
        data.records = res.data
        console.log('打印该用户考试记录',data.records)
        }else {
          ElMessage.error(res.msg)
          ElMessage.error('考试记录获取失败')
        }
      })
}

// 加载数据
const load = () => {
    request.get('/exam/record/selectPage', {
        params: {
          pageNum: data.pageNum,
          pageSize: data.pageSize,
          examTypeName: data.searchKey,
          userId: data.user.id
        }
      }).then(res => {
        if (res.code === '200') {
        console.log('打印后端返回res',res)
           data.records = res.data?.list
           data.total = res.data?.total
           console.log('打印该页数据',data.records)
        }else {
          ElMessage.error(res.msg)
        }
      })

}
// 搜索
const Search = () => {
  data.pageNum = 1
  load()
}
// 清空：清空关键词，重置页码，调用 load
const clearSearch = () => {
  data.searchKey = ''
  data.pageNum = 1
  load()
}
// 翻页
const handlePageChange = (page) => {
  data.pageNum = page
  load()
}

// 格式化开始时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  //return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  return date.toLocaleString('zh-CN', { hour12: false })
}
// 格式化 考试用时
const formatDuration = (seconds) => {
  if (!seconds && seconds !== 0) return '0秒'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (mins === 0) return `${secs}秒`
  return `${mins}分${secs}秒`
}
// 打开答卷
const showDetail = (row) => {
  data.currentRecord = row
  console.log("showDetails:",row)
  router.push({
        path: '/front/examDetail',
        query: {
           id: row.id  // 只传 id！！！
        }
      })
}


// 重新测试
const goToExam = () => {
  router.push('/front/ExamSelect')
}

onMounted(() => {
  fetchRecords()
  load()
})
</script>

<style scoped>
.my-simulation-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background: #f8f9fa;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h1 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.back-btn {
  padding: 8px 16px;
  background:linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  transform: translateY(-2px);
}


/* 记录列表 */
.record-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;

}

.empty-state p {
  color: #6c757d;
  margin-bottom: 20px;
}

.start-exam-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e9ecef;
}


.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #6c757d;
}


.detail-message {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 12px;
}

.msg-role {
  font-size: 12px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 4px;
  display: block;
}

.msg-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}


</style>