<template>
  <div style="min-height: 100vh; background: #f5f7fa">
    <!-- 搜索栏 -->
    <div style=" padding: 30px 20px">
      <div style="width: 60%; margin: 0 auto; display: flex; justify-content: center; gap: 12px">
       <el-input
         prefix-icon="Search"
         v-model="data.title"
         @keyup.enter="loadCase"
         clearable
         @clear="loadCase"
         placeholder="搜索案例..."
         style="width: 450px"
         class="search-input"
       />
       <el-button type="primary"
       @click="loadCase"
       style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);  border-radius: 25px; padding: 0 28px; font-weight: 500；; margin-top:4px">
         查询
       </el-button>
      </div>

    </div>

    <!-- 案例列表 -->
    <div style="width: 75%; margin: 10px auto">
      <div v-if="data.caseData && data.caseData.length > 0">
        <el-row :gutter="24">
          <el-col :span="6" v-for="item in data.caseData" :key="item.id" style="margin-bottom: 24px">
            <div class="case-card" @click="router.push('/front/caseDetail?id=' + item.id)">
              <div class="card-img">
                <img :src="item.img" alt="" />
                <div class="card-overlay">
                  <span>查看详情</span>
                </div>
              </div>
              <div class="card-content">
                <div class="card-title line1">{{ item.title }}</div>
                <div class="card-stats">
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    {{ item.views || 0 }}
                  </span>
                  <span class="stat-item">
                    <el-icon><Clock /></el-icon>
                    {{ item.time }}
                  </span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无案例数据</p>
      </div>

      <!-- 分页（右下角） -->
      <div v-if="data.total > 0" class="pagination-wrapper">
        <el-pagination
          @current-change="loadCase"
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
import { ElMessage } from "element-plus";
import router from "@/router/index.js";
import { View, Clock } from "@element-plus/icons-vue";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  pageNum: 1,
  pageSize: 8,
  total: 0,
  caseData: [],
})

const loadCase = () => {
  request.get('/case/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
    }
  }).then(res => {
    if (res.code === '200') {
      data.caseData = res.data?.list || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg)
    }
  })
}

loadCase()
</script>

<style scoped>
.search-input :deep(.el-input__wrapper) {
  background: white;
  border-radius: 30px;
  padding: 5px 16px;
  height: 45px;
}

.search-input :deep(.el-input__inner) {
  font-size: 14px;
}

/* 案例卡片 */
.case-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.case-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-img {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.case-card:hover .card-img img {
  transform: scale(1.05);
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.case-card:hover .card-overlay {
  opacity: 1;
}

.card-overlay span {
  color: white;
  font-size: 14px;
  padding: 6px 16px;
  border: 1px solid white;
  border-radius: 25px;
}

.card-content {
  padding: 15px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin-bottom: 12px;
}

.card-stats {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.line1 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  color: #999;
  font-size: 14px;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
}
</style>