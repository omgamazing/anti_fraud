<template>
  <div style="min-height: 100vh">
    <div style="width: 50%; margin: 20px auto">
      <div class="card" style="padding: 20px">
        <div>
          <el-input type="textarea" :rows="5" v-model="data.content" placeholder="请输入你要举报的内容"></el-input>
        </div>
        <div style="margin-top: 10px; display: flex; align-items: center">
          <el-upload
              :action="baseUrl + '/files/upload'"
              :on-success="handleImgUpload"
              :show-file-list="false"
          >
            <img src="@/assets/imgs/img.png" alt="" style="width: 25px; height: 25px">
          </el-upload>
          <div style="flex: 1; text-align: right">
            <el-button type="primary" @click="submit">提交</el-button>
          </div>
        </div>
      </div>
      <div class="card" style="margin-top: 10px; display: flex; grid-gap: 20px; padding: 30px" v-for="item in data.reportData">
        <img :src="item.userAvatar" alt="" style="width: 50px; height: 50px; border-radius: 50%">
        <div style="flex: 1">
          <div>
            <span>{{ item.userName }}</span>
            <span style="margin: 0 20px; color: #999999">举报于：{{ item.time }}</span>
            <el-tag v-if="item.status === '已处理'" type="success">{{ item.status }}</el-tag>
            <el-tag v-if="item.status === '待处理'" type="danger">{{ item.status }}</el-tag>
          </div>
          <div style="margin-top: 10px">{{ item.content }}</div>
          <img v-if="item.img" :src="item.img" alt="" style="width: 60px; height: 60px; margin-top: 10px">
          <div v-if="item.reason" style="margin-top: 10px"><strong>处理说明：</strong>{{ item.reason }}</div>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  content: null,
  img: null,
  pageNum: 1,
  pageSize: 5,
  total: 0,
  reportData: [],
})

const loadReports = () => {
  request.get('/report/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
    }
  }).then(res => {
    if (res.code === '200') {
      data.reportData = res.data?.list
      data.total = res.data?.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadReports()

const submit = () => {
  if (!data.content) {
    ElMessage.warning('请输入举报内容')
    return
  }
  request.post('/report/add', {
    userId: data.user.id,
    content: data.content,
    img: data.img,
    status: '待处理'
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('举报成功，我们会尽快处理')
      data.content = null
      data.img = null
      loadReports()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleImgUpload = (res) => {
  ElMessage.success('图片上传成功')
  data.img = res.data
}
</script>

<style scoped>

</style>