<template>
  <div style="min-height: 100vh">
    <div style="width: 50%; margin: 20px auto" class="card">
      <div style="font-size: 18px; font-weight: bold; padding: 20px 30px">系统公告</div>
      <el-timeline style="max-width: 1000px">
        <el-timeline-item color="#0bbd87" :timestamp="item.time" placement="top" v-for="item in data.noticeData">
          <h4>{{ item.title }}</h4>
          <p>{{ item.content }}</p>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>
<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";


const data = reactive({
  noticeData: [],
})

const loadNotice = () => {
  request.get('/notice/selectAll').then(res => {
    if (res.code === '200') {
      data.noticeData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

loadNotice()

</script>