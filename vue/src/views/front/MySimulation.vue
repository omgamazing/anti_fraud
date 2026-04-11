<template>
  <div class="my-simulation-container">
    <div class="header">
      <h1> 模拟记录📋</h1>
      <button @click="goBack" class="back-btn">去模拟💪</button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card total">
        <div class="stat-value">{{ data.stats.totalCount }}</div>
        <div class="stat-label">总模拟次数</div>
      </div>
      <div class="stat-card success">
        <div class="stat-value">{{ data.stats.successCount }}</div>
        <div class="stat-label">成功识破</div>
      </div>
      <div class="stat-card fail">
        <div class="stat-value">{{ data.stats.failCount }}</div>
        <div class="stat-label">被骗次数</div>
      </div>
      <div class="stat-card rate">
        <div class="stat-value">{{ data.stats.successRate }}%</div>
        <div class="stat-label">识破率</div>
      </div>
    </div>

    <div style="background-color: #f8f8f8; padding: 20px">
        <div style="width: 100%; margin: 0 auto ">
            <el-input
            prefix-icon="Search"
                v-model="data.searchKey"
                placeholder="请输入诈骗场景查询"
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
                <el-table-column prop="scene" label="诈骗类型" width="120" align="center" margin-left:10px/>
                <el-table-column prop="rounds" label="对话轮次" sortable width="110" align="center"/>
                <el-table-column prop="duration" label="用时" sortable width="80"/>
                <el-table-column prop="createTime" label="模拟时间" sortable width="150" align="center">
                    <template #default="{ row }">
                        {{ formatTime(row.createTime) }}
                    </template>
                </el-table-column>
                <el-table-column prop="result" label="模拟结果" sortable width="120" align="center">
                    <template #default="{row}">
                        <el-tag :type="row.result === 'success' ? 'success' : 'danger'" disable-transitions="false">
                            {{ row.result === 'success' ? '识破骗局' : '被欺骗' }}
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
        <p>暂无模拟记录, 点击右上角按钮前往模拟吧</p>
        <button @click="goToSimulate" class="start-simulate-btn">开始模拟</button>
      </div>
    </div>

    <!-- 对话详情弹窗 -->
    <div v-if="data.showDetailModal" class="modal-overlay" @click="closeDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ data.currentRecord?.scene }} - {{ data.currentRecord?.result === 'success' ? '✅ 成功' : '❌ 失败' }}</h3>
          <button class="close-btn" @click="closeDetail">✕</button>
        </div>
        <div class="modal-messages">
          <div v-for="(msg, idx) in data.currentMessages" :key="idx" class="detail-message">
            <span class="msg-role">{{ msg.role === 'ai' ? '🤖 AI' : '👤 你' }}</span>
            <div class="msg-content">{{ msg.content }}</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="close-modal-btn" @click="closeDetail">关闭</button>
        </div>
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
  stats: {
    totalCount: 0,
    successCount: 0,
    failCount: 0,
    successRate: 0
  },
  searchKey: null,
  currentPage: 1,
  pageSize: 10,
  showDetailModal: false,
  currentRecord: null,
  currentMessages: []
})


// 获取记录列表
const fetchRecords = async () => {
    const userId = data.user.id
    console.log('userId',userId)

    request.get('/api/record/statistics', {
        params: { userId }
      }).then(res => {
        if (res.code === '200') {
        data.records = res.data
        console.log('打印该用户所有记录',data.records)
        }else {
          ElMessage.error(res.msg)
          ElMessage.error('fetchRecords有问题')
        }
      })
}
const fetchStats = async () => {
    const userId = data.user.id
    console.log('userId',userId)

    request.get('/api/record/count', {
        params: { userId }
      }).then(res => {
        if (res.code === '200') {
        console.log('打印后端返回res',res)
          data.stats = res.data
          console.log('打印该用户所有stats',data.stats)
        }else {
          ElMessage.error(res.msg)
        }
      })
}

// 加载数据
const load = () => {
    request.get('/api/record/selectPage', {
        params: {
          pageNum: data.pageNum,
          pageSize: data.pageSize,
          scene: data.searchKey,
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

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  //return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  return date.toLocaleString('zh-CN', { hour12: false })
}

// 显示对话详情
const showDetail = (record) => {
  data.currentRecord = record
  try {
    data.currentMessages = JSON.parse(record.messages)
  } catch (e) {
    data.currentMessages = []
  }
  data.showDetailModal = true
}

// 关闭详情
const closeDetail = () => {
   data.showDetailModal = false
   data.currentRecord = null
   data.currentMessages = []
}

// 返回模拟页面
const goBack = () => {
  router.push('/front/aiChat')
}

// 开始模拟
const goToSimulate = () => {
  router.push('/front/aiChat')
}

onMounted(() => {
  fetchRecords()
  fetchStats()
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

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 12px;
  color: #6c757d;
}

.stat-card.total .stat-value { color: #6c757d; }
.stat-card.success .stat-value { color: #28a745; }
.stat-card.fail .stat-value { color: #dc3545; }
.stat-card.rate .stat-value { color: #667eea; }

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

.start-simulate-btn {
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

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #6c757d;
}

.modal-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
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

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #e9ecef;
  text-align: center;
}

.close-modal-btn {
  padding: 8px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}
</style>