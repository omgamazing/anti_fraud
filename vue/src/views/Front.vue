<template>
  <div>
<!--    <div class="front-notice"><el-icon><Bell /></el-icon>公告：{{ data.top }}</div>-->
    <div class="front-header">
      <div class="front-header-left">
        <img src="@/assets/imgs/logo.png" alt="">
        <div class="title">网络安全反诈模拟系统</div>
      </div>
      <div class="front-header-center">
        <el-menu :default-active="router.currentRoute.value.path" router mode="horizontal">
          <el-menu-item index="/front/home">首页</el-menu-item>
          <el-menu-item index="/front/news">反诈百科</el-menu-item>
          <el-menu-item index="/front/article">反诈论坛</el-menu-item>
          <el-menu-item index="/front/aichat">AI模拟</el-menu-item>
          <el-menu-item index="/front/examSelect">知识考核</el-menu-item>
          <!--

          <el-menu-item index="/front/notice">系统公告</el-menu-item>
          -->
        </el-menu>
      </div>
      <div class="front-header-right">
        <div v-if="!data.user.id">
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button @click="router.push('/register')">注册</el-button>
        </div>
        <div v-else>
          <el-dropdown style="cursor: pointer; height: 60px">
            <div style="display: flex; align-items: center; color: white">
              <img style="width: 40px; height: 40px; border-radius: 50%;" :src="data.user.avatar" alt="">
              <span style="margin-left: 5px;">{{ data.user.name }}</span><el-icon><arrow-down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/front/myArticle')">我的帖子</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/myCollect')">我的收藏</el-dropdown-item>

                <el-dropdown-item @click="router.push('/front/myComment')">我的评论</el-dropdown-item>

                <el-dropdown-item @click="router.push('/front/mySimulation')">模拟记录</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/examRecord')">考试记录</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/person')">个人资料</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/password')">修改密码</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
    <div class="main-body">
      <RouterView @updateUser="updateUser" />
    </div>
  </div>
</template>

<script setup>
  import router from "@/router/index.js";
  import { reactive } from "vue";
  import request from "@/utils/request.js";

  const data = reactive({
    user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
    top: '',
    noticeData: []
  })

  const logout = () => {
    localStorage.removeItem('xm-user')
    router.push('/login')
  }

  const updateUser = () => {
    data.user =  JSON.parse(localStorage.getItem('xm-user') || '{}')
  }

  const loadNotice = () => {
    request.get('/notice/selectAll').then(res => {
      data.noticeData = res.data
      let i = 0
      if (data.noticeData && data.noticeData.length) {
        data.top = data.noticeData[0].content
        setInterval(() => {
          data.top = data.noticeData[i].content
          i++
          if (i === data.noticeData.length) {
            i = 0
          }
        }, 2500)
      }
    })
  }
  loadNotice()
</script>

<style scoped>
@import "@/assets/css/front.css";
</style>