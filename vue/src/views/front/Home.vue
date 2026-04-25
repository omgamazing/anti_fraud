<template>
  <div style="min-height: 100vh">
    <div>
      <el-carousel height="400px">
        <el-carousel-item v-for="item in data.carouselData" :key="item.id">
          <img
            :src="item.img"
            alt=""
            style="height: 400px; width: 100%; cursor: pointer"
            @click="goToDetail(item)"
          >
        </el-carousel-item>
      </el-carousel>
    </div>
    <div style="width: 70%; margin: 5px auto; padding-bottom: 50px">
      <div style="margin-top: 30px; display: flex; grid-gap: 50px">
        <div style="width: 300px">
          <div style="display: flex; align-items: center">
            <div style="flex: 1; font-size: 18px; font-weight: bold">原来骗局就在我身边</div>
            <div style="width: 80px; text-align: right; cursor: pointer; color: #666666"
            @click="router.push('/front/article')">参与 ></div>
          </div>
          <div style="margin-top: 20px">
            <el-row :gutter="20">
              <el-col :span="24" v-for="item in data.articleData" style="margin-bottom: 10px">
                <div class="front_card">
                  <div>
                    <img :src="item.img" alt="" style="height: 180px; width: 100%; border-radius: 5px; cursor: pointer"
                    @click="router.push('/front/articleDetail?id=' + item.id)">
                    <div style="padding: 15px">
                      <div style="font-size: 14px; font-weight: bold" class="line1">{{ item.title }}</div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
          <!-- AI模拟演练 -->
          <div style="margin-top: 20px">
            <div style="display: flex; align-items: center; margin-bottom: 12px">
              <div style="flex: 1; font-size: 18px; font-weight: bold">想来一场沉浸式的模拟演练吗？</div>
              <div style="font-size: 13px; color: #666666; cursor: pointer"
              @click="router.push('/front/aichat')">体验 ></div>
            </div>
            <div style="display: flex; flex-wrap: wrap; gap: 8px">
              <span
                v-for="scene in aiScenes"
                :key="scene"
                style="padding: 5px 14px; background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%); border-radius: 20px; font-size: 13px; color: #667eea; cursor: pointer"
                @click="router.push({ path: '/front/aichat', query: { scene } })"
              >
                {{ scene }}
              </span>
            </div>
            <div style="font-size: 12px; color: #999; margin-top: 10px">
              💡 选择场景，与AI骗子对话，测试你的反诈能力
            </div>
          </div>

        </div>

        <div style="flex: 1">
          <div style="display: flex; align-items: center">
            <div style="flex: 1; font-size: 18px; font-weight: bold">多看案例多提防，诈骗永远不上当</div>
            <div style="width: 80px; text-align: right; cursor: pointer; color: #666666"
            @click="router.push('/front/case')">学习 ></div>
          </div>
          <div style="margin-top: 20px">
            <el-row :gutter="20">
              <el-col :span="12" v-for="item in data.caseData" style="margin-bottom: 20px">
                <div class="front_card">
                  <div>
                    <img :src="item.img" alt="" style="height: 200px; width: 100%; border-radius: 5px; cursor: pointer"
                    @click="router.push('/front/caseDetail?id=' + item.id)">
                    <div style="padding: 10px">
                      <div style="font-size: 14px; font-weight: bold" class="line1">{{ item.title }}</div>
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
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import { View, Clock } from "@element-plus/icons-vue";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  carouselData: [],
  articleData: [],
  caseData: [],
})

const aiScenes = ['刷单诈骗', '冒充客服', '网贷诈骗', '杀猪盘', '冒充公检法']

// 跳转详情
const goToDetail = (item) => {
  console.log('点击的轮播图数据:', item)

  const targetId = item.targetId || item.target_id
  const targetType = item.targetType || item.target_type

  // 严格校验
  if (!targetId || targetId === 'undefined' || targetId === 'null' || targetId === '') {
    ElMessage.warning('轮播图关联内容不存在');
    return;
  }

  const id = Number(targetId)
  if (isNaN(id)) {
    ElMessage.warning('关联内容ID无效');
    return;
  }

  console.log('跳转参数:', { targetType, id })

  if (targetType === 'article') {
    router.push('/front/articleDetail?id=' + id)
  } else if (targetType === 'case') {
    router.push('/front/caseDetail?id=' + id)
  } else {
    ElMessage.warning('未知的链接类型: ' + targetType)
  }
}

const loadCarousel = () => {
  request.get('/carousel/selectAll').then(res => {
    if (res.code === '200') {
      // 过滤无效数据
      data.carouselData = (res.data || []).filter(item => {
        const targetId = item.targetId || item.target_id
        const targetType = item.targetType || item.target_type
        return targetId && targetId !== 'undefined' && targetId !== 'null' && targetType
      })
      console.log('有效轮播图数据:', data.carouselData)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadArticle = () => {
  request.get('/article/selectTop2').then(res => {
    if (res.code === '200') {
      data.articleData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadCase = () => {
  request.get('/case/selectTop4').then(res => {
    if (res.code === '200') {
      data.caseData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

loadCarousel()
loadArticle()
loadCase()
</script>

<style scoped>
.front_card {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  background: white;
}
.front_card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
}
.line1 {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>