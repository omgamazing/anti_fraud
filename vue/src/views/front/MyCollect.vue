<template>
  <div style="width: 70%; margin: 20px auto; min-height: 100vh">
    <!-- 头部统计 -->
    <div style="margin-bottom: 24px">
      <div style="font-size: 20px; font-weight: bold">
        ⭐ 我的收藏
      </div>
    </div>

    <!-- 收藏列表 -->
    <div v-if="data.tableData && data.tableData.length > 0">
      <el-row :gutter="24">
        <el-col :span="6" v-for="item in data.tableData" :key="item.id" style="margin-bottom: 24px">
          <div class="collect-card" @click="router.push('/front/caseDetail?id=' + item.caseObj.id)">
            <div class="card-img">
              <img :src="item.caseObj.img" alt="" />
              <div class="card-overlay">
                <span>查看详情</span>
              </div>
            </div>
            <div class="card-content">
              <div class="card-title line2">{{ item.caseObj.title }}</div>
              <div class="card-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ item.caseObj.views || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><Clock /></el-icon>
                  {{ item.caseObj.time }}
                </span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">⭐</div>
      <p>暂无收藏</p>
      <el-button type="primary" plain @click="router.push('/front/case')">去逛逛</el-button>
    </div>

    <!-- 分页 -->
    <div v-if="data.total > 0" class="pagination-wrapper">
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
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import router from "@/router/index.js";
import { View, Clock } from "@element-plus/icons-vue";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  pageNum: 1,
  pageSize: 8,
  tableData: [],
  total: 0,
})

const load = () => {
  // 检查用户是否登录
  if (!data.user.id) {
    ElMessage.warning('请先登录')
    return
  }

  request.get('/collect/selectPage', {
    params: {
      userId: data.user.id,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(err => {
    console.error('加载收藏列表失败:', err)
    ElMessage.error('加载失败，请稍后重试')
  })
}

// 初始化加载
load()
</script>

<style scoped>
/* 收藏卡片 */
.collect-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.collect-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-img {
  position: relative;
  height: 150px;
  overflow: hidden;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.collect-card:hover .card-img img {
  transform: scale(1.05);
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.collect-card:hover .card-overlay {
  opacity: 1;
}

.card-overlay span {
  color: white;
  font-size: 14px;
  padding: 6px 16px;
  border: 1px solid white;
  border-radius: 25px;
  background: rgba(0,0,0,0.2);
}

.card-content {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin-bottom: 10px;
  min-height: 38px;
}

.card-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.line2 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 16px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  color: #999;
  font-size: 14px;
  margin-bottom: 16px;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>