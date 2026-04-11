<template>
  <div style="margin: 20px auto; width: 60%; min-height: 100vh">
    <div class="card" style="padding: 50px 80px">
      <div style="text-align: center; font-size: 22px; font-weight: 400">{{ data.newsData.title }}</div>
      <div style="text-align: center; color: #666666; margin-top: 20px">
        <span>发布时间：{{ data.newsData.time }}</span>
        <span style="margin: 0 20px">浏览量：{{ data.newsData.views }}</span>
      </div>
      <div style="margin-top: 50px" v-html="data.newsData.content"></div>
    </div>
  </div>

</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  newsId: router.currentRoute.value.query.id,
  newsData: {},
  content: null,
})

const loadActivity = () => {
  request.get('/news/selectById/' + data.newsId).then(res => {
    if (res.code === '200') {
      data.newsData = res.data
      data.newsData.views = data.newsData.views + 1
      request.put('/news/update', data.newsData)
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadActivity()


</script>

<style scoped>

</style>