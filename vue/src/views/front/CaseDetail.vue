<template>
  <div style="margin: 20px auto; width: 60%; min-height: 100vh">
    <div class="card" style="padding: 50px 80px">
      <div style="text-align: center; font-size: 22px; font-weight: 400">{{ data.caseData.title }}</div>
      <div style="text-align: center; color: #666666; margin-top: 20px">
        <span>发布时间：{{ data.caseData.time }}</span>
        <span style="margin: 0 20px">浏览量：{{ data.caseData.views }}</span>
        <el-button v-if="data.collectFlag" type="danger" size="small" @click="cancelCollect">取消收藏</el-button>
        <el-button v-else type="primary" size="small" @click="addCollect">收藏</el-button>
      </div>
      <div style="margin-top: 50px" v-html="data.caseData.content"></div>
    </div>
  </div>
</template>

<script setup>
import {reactive, onMounted} from "vue";
import { useRoute } from "vue-router";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";

const route = useRoute();

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  caseId: route.query.id,
  caseData: {},
  collectFlag: false,
})

const loadCaseData = async () => {
  // 只加载案例详情，不处理收藏状态
  const caseRes = await request.get('/case/selectById/' + data.caseId)
  if (caseRes.code === '200') {
    data.caseData = caseRes.data
    // 增加浏览量（保持原有逻辑不变）
    await request.put('/case/update', { ...data.caseData, views: (data.caseData.views || 0) + 1 })
  }
}

const loadCollectStatus = async () => {
  // 单独检查收藏状态
  if (!data.user.id) return
  const res = await request.get('/collect/isCollected', {
    params: { userId: data.user.id, caseId: data.caseId }
  })
  if (res.code === '200') {
    data.collectFlag = res.data
  }
}

const addCollect = async () => {
  const res = await request.post('/collect/add', {
    userId: data.user.id,
    caseId: data.caseId
  })
  if (res.code === '200') {
    ElMessage.success('收藏成功')
    data.collectFlag = true  // 直接更新状态，不重新加载
  }
}

const cancelCollect = async () => {
  const res = await request.delete('/collect/cancel', {
    params: { userId: data.user.id, caseId: data.caseId }
  })
  if (res.code === '200') {
    ElMessage.success('取消收藏成功')
    data.collectFlag = false  // 直接更新状态
  }
}

onMounted(() => {
  loadCaseData()
  loadCollectStatus()
})
</script>