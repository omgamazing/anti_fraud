<template>
  <div>
    <!-- 欢迎卡片 -->
    <div class="card welcome-card">
      您好！{{ data.user?.name }}，欢迎使用本系统！
    </div>

    <!-- 两列布局 -->
    <div style="display: flex; gap: 20px; margin-top: 20px">
      <!-- 左侧：系统公告 -->
      <div class="card" style="flex: 1; padding: 20px">
        <div style="font-weight: bold; font-size: 18px; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #e9ecef">
          📢 系统公告
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in data.noticeData"
            :key="index"
            :timestamp="item.time"
            placement="top"
          >
            {{ item.content }}
          </el-timeline-item>
          <div v-if="data.noticeData.length === 0" style="text-align: center; color: #999; padding: 40px">
            暂无公告
          </div>
        </el-timeline>
      </div>

      <!-- 右侧：预留空白 -->
      <div class="card" style="flex: 1; padding: 20px; display: flex; align-items: center; justify-content: center; color: #999">
        <div style="text-align: center">
          <el-icon size="48"><InfoFilled /></el-icon>
          <div style="margin-top: 12px">更多功能开发中...</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import { InfoFilled } from "@element-plus/icons-vue";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  noticeData: []
})

const loadNotice = () => {
  request.get('/notice/selectAll').then(res => {
    if (res.code === '200') {
      data.noticeData = res.data || []
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadNotice()
</script>

<style scoped>
.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 16px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.el-timeline-item :deep(.el-timeline-item__timestamp) {
  font-size: 12px;
  color: #999;
}
</style>