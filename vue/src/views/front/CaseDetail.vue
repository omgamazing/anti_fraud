<template>
  <div style="margin: 20px auto; width: 60%; min-height: 100vh">
    <div class="card" style="padding: 50px 80px">
      <div style="text-align: center; font-size: 22px; font-weight: 400">{{ data.caseData.title }}</div>
      <div style="text-align: center; color: #666666; margin-top: 20px">
        <span>发布时间：{{ data.caseData.time }}</span>
        <span style="margin: 0 20px">浏览量：{{ data.caseData.views }}</span>
      </div>
      <div style="margin-top: 50px" v-html="data.caseData.content"></div>
    </div>
  </div>

</template>

<script setup>
import {reactive} from "vue";
import { useRoute } from "vue-router";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";

const route = useRoute();

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  caseId: route.query.id,
  caseData: {},
  content: null,
})

const loadCaseDetail = () => {
  console.log("用户点击的案例CaseID：", data.caseId);
  request.get('/case/selectById/' + data.caseId).then(res => {
    if (res.code === '200') {
      data.caseData = res.data
      data.caseData.views = data.caseData.views + 1
      request.put('/case/update', data.caseData)
      console.log('用户选择的案例',data.caseData)
    } else {
      ElMessage.error("进入失败")
      ElMessage.error(res.msg)
    }
  })
}
loadCaseDetail()


</script>

<style scoped>

</style>