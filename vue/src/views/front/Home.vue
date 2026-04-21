<template>
  <div style="min-height: 100vh">
    <div>
      <el-carousel height="400px">
        <el-carousel-item v-for="item in data.carouselData" :key="item.id">
          <img :src="item.img" alt="" style="height: 400px; width: 100%; cursor: pointer" @click="router.push('/front/articleDetail?id=' + item.articleId)">
        </el-carousel-item>
      </el-carousel>
    </div>
    <div style="width: 60%; margin: 5px auto; padding-bottom: 50px">
      <div style="margin-top: 30px; display: flex; grid-gap: 30px">
        <div style="width: 300px">
          <div style="display: flex; align-items: center">
            <div style="flex: 1; font-size: 20px">反诈热帖</div>
            <div style="width: 80px; text-align: right; cursor: pointer; color: #666666" @click="router.push('/front/article')">更多 ></div>
          </div>
          <div style="margin-top: 20px">
            <el-row :gutter="20">
              <el-col :span="24" v-for="item in data.articleData" style="margin-bottom: 10px">
                <div class="front_card">
                  <div>
                    <img :src="item.img" alt="" style="height: 180px; width: 100%; border-radius: 5px; cursor: pointer" @click="router.push('/front/articleDetail?id=' + item.id)">
                    <div style="padding: 10px">
                      <div style="font-size: 16px; font-weight: bold" class="line1">{{ item.title }}</div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
          <div style="display: flex; align-items: center; margin-top: 10px">
            <div style="flex: 1; font-size: 20px">AI模拟</div>
            <div style="width: 80px; text-align: right; cursor: pointer; color: #666666"
            @click="router.push('/front/aichat')">体验 ></div>
          </div>
        </div>
        <div style="flex: 1">
          <div style="display: flex; align-items: center">
            <div style="flex: 1; font-size: 20px">百科：你想知道的我都有</div>
            <div style="width: 80px; text-align: right; cursor: pointer; color: #666666"
            @click="router.push('/front/case')">更多 ></div>
          </div>
          <div style="margin-top: 20px">
            <el-row :gutter="20">
              <el-col :span="12" v-for="item in data.newsData" style="margin-bottom: 20px">
                <div class="front_card">
                  <div>
                    <img :src="item.img" alt="" style="height: 230px; width: 100%; border-radius: 5px; cursor: pointer" @click="router.push('/front/newsDetail?id=' + item.id)">
                    <div style="padding: 10px">
                      <div style="font-size: 16px; font-weight: bold" class="line1">{{ item.title }}</div>
                      <div style="margin-top: 15px; display: flex; grid-gap: 20px">
                        <div style="color: #666666; display: flex; grid-gap: 5px; align-items: center">
                          <el-icon size="18"><View /></el-icon>
                          <div>{{ item.views }}</div>
                        </div>
                        <div style="color: #666666; display: flex; grid-gap: 5px; align-items: center">
                          <el-icon size="18"><Clock /></el-icon>
                          <div>{{ item.time }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
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


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  carouselData: [],
  articleData: [],
  activityData: [],
  newsData: [],
})

const loadCarousel = () => {
  request.get('/carousel/selectAll').then(res => {
    if (res.code === '200') {
      data.carouselData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadCarousel()

const loadArticle = () => {
  request.get('/article/selectTop2').then(res => {
    if (res.code === '200') {
      data.articleData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadArticle()


const loadCase = () => {
  request.get('/case/selectTop4').then(res => {
    if (res.code === '200') {
      data.caseData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadCase()

</script>