<template>
  <div class="module-container">
    <div class="module-card">
      <!-- 头部 -->
      <div class="header">
        <h1>请选择你要挑战的模块</h1>
      </div>

      <!-- 模块卡片列表 -->
      <div class="module-list">
        <div
          v-for="module in modules"
          :key="module.value"
          class="module-card-item"
          :class="{ selected: selectedModule === module.value }"
          @click="selectModule(module.value)"
        >
          <div class="card-icon">{{ module.icon }}</div>
          <div class="card-content">
            <div class="card-title">{{ module.name }}</div>
            <div class="card-desc">{{ module.desc }}</div>
            <div class="card-stats">
                <el-tag size="small" type="info" disable-transitions="false">📝 10道题</el-tag>
                <el-tag size="small" type="warning" disable-transitions="false">⏱️ 30分钟</el-tag>
            </div>
          </div>
          <div class="card-check" v-if="selectedModule === module.value">
            <el-icon><Check /></el-icon>
          </div>
        </div>
      </div>

      <!-- 开始按钮 -->
      <div class="action">
        <button
          class="start-btn"
          :disabled="!selectedModule"
          @click="startExam"
        >
          <span>开始考试</span>
          <span class="arrow">→</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const modules = [
  {
    value: 1,
    name: '反诈基础',
    desc: '电信诈骗、网络钓鱼、虚假信息识别',
    icon: '🛡️'
  },
  {
    value: 2,
    name: '信息网络安全',
    desc: '密码安全、个人信息保护、WiFi安全',
    icon: '🔒'
  },
  {
    value: 3,
    name: '资金应急处置',
    desc: '转账安全、账户冻结、紧急止付',
    icon: '💰'
  },
  {
    value: 4,
    name: '综合测试',
    desc: '随机抽取各模块题目，全面测试你的反诈能力',
    icon: '🎯'
  }
]

const selectedModule = ref(null)

const selectModule = (value) => {
  selectedModule.value = value
}

const startExam = () => {
  console.log('选中的值:', selectModule.value)
  if (selectedModule.value !== null) {
    router.push({
      path: '/front/examPaper',
      query: { examType: selectedModule.value }
    })
  }
}
</script>

<style scoped>
.module-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.module-card {
  max-width: 1000px;
  width: 100%;
  background: white;
  border-radius: 32px;
  padding: 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
}

.header {
  text-align: center;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 26px;
  color: #333;
  padding-bottom:12px;
}


/* 模块卡片网格 */
.module-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.module-card-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  position: relative;
}

.module-card-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.module-card-item.selected {
  background: linear-gradient(135deg, #667eea10 0%, #764ba210 100%);
  border-color: #667eea;
}

.card-icon {
  font-size: 48px;
  width: 70px;
  height: 70px;
  background: white;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.card-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.4;
}

.card-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.card-check {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 28px;
  height: 28px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

/* 开始按钮 */
.action {
  text-align: center;
}

.start-btn {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 14px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 40px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.start-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.start-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.arrow {
  font-size: 20px;
  transition: transform 0.3s;
}

.start-btn:hover:not(:disabled) .arrow {
  transform: translateX(4px);
}

/* 响应式 */
@media (max-width: 768px) {
  .module-card {
    padding: 24px;
  }

  .module-list {
    grid-template-columns: 1fr;
  }

  .header h1 {
    font-size: 24px;
  }
}
</style>