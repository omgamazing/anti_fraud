<template>
  <div style="min-height: 100vh; background: #f5f7fa">
    <!-- 搜索栏 -->
    <div style=" padding: 30px 20px">
       <div style="width: 60%; margin: 0 auto; display: flex; justify-content: center; gap: 12px">
        <el-input
          prefix-icon="Search"
          v-model="data.title"
          @keyup.enter="load"
          clearable
          @clear="load"
          placeholder="搜索帖子..."
          style="width: 450px"
          class="search-input"
        />
        <el-button type="primary"
        @click="loadCase"
        style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);  border-radius: 25px; padding: 0 28px; font-weight: 500；; margin-top: 4px">
        查询
        </el-button>
      </div>
    </div>

   <!-- 分类标签 -->
   <div style="width: 60%; margin: 0px auto">
     <div style="display: flex; flex-wrap: wrap; gap: 16px; margin-bottom: 24px; border-bottom: 1px solid #e9ecef; padding-bottom: 12px">
       <span
         @click="changCategory(null)"
         :class="['category-tab', { active: checkType(null) }]"
       >
         全部
       </span>
       <span
         v-for="item in data.categoryData"
         :key="item.id"
         @click="changCategory(item.id)"
         :class="['category-tab', { active: checkType(item.id) }]"
       >
         {{ item.name }}
       </span>
     </div>


      <!-- 帖子列表 -->
      <div v-if="data.articleData && data.articleData.length > 0">
        <div class="post-card" v-for="item in data.articleData" :key="item.id">
          <div style="display: flex; gap: 20px">
            <img
              :src="item.img"
              alt=""
              class="post-img"
              @click="router.push('/front/articleDetail?id=' + item.id)"
            />
            <div style="flex: 1">
              <div class="post-title" @click="router.push('/front/articleDetail?id=' + item.id)">
                {{ item.title }}
              </div>
              <div class="post-desc line2">{{ item.description || item.content?.substring(0, 80) }}...</div>
              <div class="post-meta">
                <div class="meta-item">
                  <img :src="item.userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="" class="avatar">
                  <span>{{ item.userName }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><View /></el-icon>
                  <span>{{ item.views || 0 }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ item.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无帖子</p>
      </div>

      <!-- 分页 -->
      <div v-if="data.total > 0" class="pagination-wrapper">
        <el-pagination
          @current-change="load"
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
  categoryId: null,
  pageNum: 1,
  pageSize: 5,
  total: 0,
  articleData: [],
  categoryData: [],
})

const loadCategory = () => {
  request.get('/category/selectAll').then(res => {
    if (res.code === '200') {
      data.categoryData = res.data || []
    }
  })
}
loadCategory()

const changCategory = (categoryId) => {
  data.categoryId = categoryId
  data.pageNum = 1
  load()
}

const checkType = (categoryId) => {
  return data.categoryId === categoryId
}

const load = () => {
  request.get('/article/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      categoryId: data.categoryId,
      status: '审核通过'
    }
  }).then(res => {
    if (res.code === '200') {
      data.articleData = res.data?.list || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()
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
/* 分类标签样式 */
.category-tab {
  padding: 6px 16px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 20px;
  background: #f5f7fa;
}

.category-tab:hover {
  color: #409eff;
  background: #ecf5ff;
}

.category-tab.active {
  color: #409eff;
  background: #ecf5ff;
  font-weight: 500;
}
/* 帖子卡片 */
.post-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
  cursor: pointer;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.post-img {
  width: 140px;
  height: 100px;
  border-radius: 12px;
  object-fit: cover;
  cursor: pointer;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  cursor: pointer;
}

.post-title:hover {
  color: #409eff;
}

.post-desc {
  font-size: 13px;
  color: #888;
  line-height: 1.5;
  margin-bottom: 12px;
}

.post-meta {
  display: flex;
  gap: 20px;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.line2 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 16px;
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
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>