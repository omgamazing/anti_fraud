<template>
  <div class="exam-container">
    <div class="exam-card">
      <!-- 顶部栏 -->
      <div class="exam-header">
        <div class="header-left">
          <h2>{{ data.examTypeName }}</h2>
          <div class="exam-timer">
            <el-icon><Timer /></el-icon>
            <span>{{ formatTime(data.timeLeft) }}</span>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="submitExam">提交试卷</el-button>
        </div>
      </div>

      <!-- 主体内容 -->
      <div class="exam-main">
        <!-- 左侧：题号导航栏 -->
        <div class="exam-sidebar">
          <div class="sidebar-title">
            <span>题目列表</span>
            <span class="question-count">共 {{ data.questions.length }} 题</span>
          </div>
         <!-- 单选题组 -->
         <div class="question-group">
          <div class="group-title">单选题</div>
             <div class="question-nav">
               <el-button
                  v-for="(q, idx) in singleQuestions"
                  :key="q.index"
                  :type="getButtonType(q.index)"
                  :class="['nav-btn', { current: data.currentIndex === q.index }]"
                  size="small"
                  @click="goToQuestion(q.index)"
                >
                  {{ getOriginalIndex(q.index) }}
               </el-button>
             </div>
          </div>

          <!-- 多选题组 -->
          <div class="question-group">
             <div class="group-title">多选题</div>
              <div class="question-nav">
                <el-button
                  v-for="(q, idx) in multiQuestions"
                  :key="q.index"
                  :type="getButtonType(q.index)"
                  :class="['nav-btn', { current: data.currentIndex === q.index }]"
                  size="small"
                  @click="goToQuestion(q.index)"
                >
                  {{ getOriginalIndex(q.index) }}
                </el-button>
              </div>
          </div>

          <!-- 判断题组 -->
          <div class="question-group">
             <div class="group-title">判断题</div>
              <div class="question-nav">
                <el-button
                  v-for="(q, idx) in judgeQuestions"
                  :key="q.index"
                  :type="getButtonType(q.index)"
                  :class="['nav-btn', { current: data.currentIndex === q.index }]"
                  size="small"
                  @click="goToQuestion(q.index)"
                >
                  {{ getOriginalIndex(q.index) }}
                </el-button>
              </div>
          </div>

          <div class="sidebar-legend">
            <div class="legend-item">
              <span class="legend-dot answered"></span>
              <span>已答</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot current"></span>
              <span>当前</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot unanswered"></span>
              <span>未答</span>
            </div>
          </div>
        </div>

        <!-- 右侧：答题区 -->
        <div class="exam-content">
          <div class="question-card" v-if="currentQuestion">
            <!-- 题目头部 -->
            <div class="question-header">
              <div class="question-title">
                <span class="question-num">{{ data.currentIndex + 1 }}.</span>
                <el-tag :type="getTypeTag(currentQuestion.type)" size="small">
                  {{ getTypeName(currentQuestion.type) }}
                </el-tag>
                <span class="question-text">{{ currentQuestion.title }}</span>
                <span class="question-score">({{ getScoreByType(currentQuestion.type) }}分)</span>
              </div>
            </div>

            <!-- 选项区域 -->
            <div class="options-area">
              <!-- 单选题/多选题 -->
              <div class="options-list">
                <div
                  v-for="opt in currentOptions"
                  :key="opt.value"
                  class="option-item"
                  :class="{ selected: isSelected(opt.value) }"
                  @click="selectOption(opt.value)"
                >
                  <span class="option-radio">
                     <span :class="['radio-circle', { checked: isSelected(opt.value) }]"></span>

                  </span>
                  <span class="option-label">{{ opt.label }}.</span>
                  <span class="option-text">{{ opt.text }}</span>
                </div>
              </div>
            </div>

          <!-- 底部导航按钮 -->
          <div class="question-footer">
            <el-button
              :disabled="data.currentIndex === 0"
              @click="prevQuestion"
            >
              <el-icon><ArrowLeft /></el-icon> 上一题
            </el-button>
            <el-button
              v-if="data.currentIndex < data.questions.length - 1"
              type="primary"
              @click="nextQuestion"
            >
              下一题 <el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button
              v-else
              type="success"
              @click="submitExam"
            >
              提交试卷
            </el-button>
          </div>
        </div>
      </div>
    </div>

      <!-- 结果弹窗 -->
      <el-dialog v-model="data.showResult" :close-on-click-modal="false" width="500px" center>
        <div class="result-content">
          <div class="result-icon">{{ data.score >= 60 ? '🎉' : '😢' }}</div>
          <h3>{{ data.score >= 60 ? '恭喜通过！' : '再接再厉！' }}</h3>
          <div class="score-display">
            <span class="score-value">{{ data.score }}</span>
            <span class="score-total">/{{ data.totalScore }}</span>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="正确题数">
              <el-tag type="success">{{ data.correctCount }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="错误题数">
              <el-tag type="danger">{{ data.wrongCount }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <template #footer>
          <el-button @click="retry">再来一次</el-button>
          <el-button type="primary" @click="goToRecords">查看记录</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Timer, Check, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 所有数据用 reactive 管理
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  examType: parseInt(route.query.examType || 0),
  examTypeName: '',
  questions: [],
  userAnswers: [],
  currentIndex: 0,
  totalScore: 0,
  timeLeft: 1800,
  showResult: false,
  score: 0,
  correctCount: 0,
  wrongCount: 0,
  recordId:0
})

let timer = null

// 计算属性
const currentQuestion = computed(() => data.questions[data.currentIndex])

// 计算属性：按题型分组
const singleQuestions = computed(() => {
  return data.questions
    .map((q, idx) => ({ ...q, index: idx }))
    .filter(q => q.type === 1)
})

const multiQuestions = computed(() => {
  return data.questions
    .map((q, idx) => ({ ...q, index: idx }))
    .filter(q => q.type === 2)
})

const judgeQuestions = computed(() => {
  return data.questions
    .map((q, idx) => ({ ...q, index: idx }))
    .filter(q => q.type === 3)
})

// 获取原始题号（按顺序 1,2,3...）
const getOriginalIndex = (idx) => {
  return idx + 1
}
// 题型映射
const typeName = { 1: '单选题', 2: '多选题', 3: '判断题' }
const typeTag = { 1: 'primary', 2: 'success', 3: 'warning' }
const scoreMap = { 1: 6, 2: 20, 3: 10 }

const getTypeName = (type) => typeName[type]
const getTypeTag = (type) => typeTag[type]
const getScoreByType = (type) => scoreMap[type]

// 获取按钮类型（已答/未答/当前）
const getButtonType = (idx) => {
  if (data.currentIndex === idx) return 'primary'
  if (data.userAnswers[idx] !== null && data.userAnswers[idx] !== undefined) return 'success'
  return 'info'
}

// 获取选项列表
// 计算属性：根据题型动态生成选项（复用）
const currentOptions = computed(() => {
  const q = currentQuestion.value
  if (!q) return []

  // 判断题特殊处理
  if (q.type === 3) {
    return [
      { label: 'A', value: '√', text: '正确' },
      { label: 'B', value: '×', text: '错误' }
    ]
  }

  // 单选题/多选题
  const options = []
  if (q.optionA) options.push({ label: 'A', value: 'A', text: q.optionA })
  if (q.optionB) options.push({ label: 'B', value: 'B', text: q.optionB })
  if (q.optionC) options.push({ label: 'C', value: 'C', text: q.optionC })
  if (q.optionD) options.push({ label: 'D', value: 'D', text: q.optionD })
  return options
})

// 判断选项是否被选中
const isSelected = (value) => {
  const userAnswer = data.userAnswers[data.currentIndex]
  const q = currentQuestion.value
  if (!q) return false

  if (q.type === 2) {
    // 多选题：答案可能是数组
    return Array.isArray(userAnswer) && userAnswer.includes(value)
  }
  // 单选/判断：直接比较
  return userAnswer === value
}

// 选择答案
const selectOption = (value) => {
  const q = currentQuestion.value
  if (!q) return
  if (q.type === 2) {

  // 多选题：切换选中状态
  let answers = data.userAnswers[data.currentIndex] || []
    if (Array.isArray(answers)) {
        if (answers.includes(value)) {
            answers = answers.filter(v => v !== value)
        } else {
        answers = [...answers, value]
        }
     } else {
        answers = [value]
     }
     data.userAnswers[data.currentIndex] = answers
    } else {
      // 单选题/判断题：直接赋值
      data.userAnswers[data.currentIndex] = value
    }
}

// 跳转题目
const goToQuestion = (idx) => {
  data.currentIndex = idx
}

// 上一题
const prevQuestion = () => {
  if (data.currentIndex > 0) {
    data.currentIndex--
  }
}

// 下一题
const nextQuestion = () => {
  if (data.currentIndex < data.questions.length - 1) {
    data.currentIndex++
  }
}

// 生成试卷
const generatePaper = async () => {
    request.post('/exam/generatePaper', {
        examType: data.examType
      }).then(res => {
        if (res.code === '200') {
        console.log('是否进入打印试卷')
        data.questions = res.data.questions
        data.userAnswers = new Array(data.questions.length).fill(null)
        data.totalScore = data.questions.reduce((sum, q) => sum + getScoreByType(q.type), 0)
        data.examTypeName=res.data.examTypeName
        console.log('设置的 examTypeName:', data.examTypeName)
        console.log('打印试卷',res.data)
        }else {
          ElMessage.error(res.msg)
          ElMessage.error('生成试卷失败')
        }
      }).catch (err => {
        ElMessage.error('错误')
      })

}

// 提交试卷
const submitExam = async () => {
    const unanswered = data.userAnswers.filter(a => a === null).length
      if (unanswered > 0) {
        ElMessage.warning(`还有 ${unanswered} 题未作答`)
        return
      }

    request.post('/exam/submitPaper', {
        userId:data.user.id,
        questions: data.questions,
        userAnswers: data.userAnswers,
        examType: data.examType,
        duration: 1800 - data.timeLeft
      }).then(res => {
        if (res.code === '200') {
         data.recordId=res.data.recordId
         data.score = res.data.totalScore
         data.correctCount = res.data.correctCount
         data.wrongCount = res.data.wrongCount
         data.showResult = true
         if (timer) clearInterval(timer)
         console.log('打印用户考试记录',res.data)
        }else {
          ElMessage.error(res.msg)
          ElMessage.error('提交失败')
        }
      }).catch (err => {
        ElMessage.error('错误')
      })

}

// 重新测试
const retry = () => {
  router.push('/front/ExamSelect')
}

// 查看记录
const goToRecords = () => {

   router.push({
         path: '/front/examDetail',
         query: {
              id: data.recordId
         }
   })
}

// 计时器
const startTimer = () => {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    if (data.timeLeft > 0 && !data.showResult) {
      data.timeLeft--
    } else if (data.timeLeft <= 0 && !data.showResult) {
      submitExam()
    }
  }, 1000)
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

onMounted(() => {
  generatePaper()
  startTimer()
})

//清除计时器
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.exam-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;

}

.exam-card {
  max-width: 1000px;
  width: 100%;
  background: white;
  border-radius: 32px;
  padding: 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
}

/* 顶部栏 */
.exam-header {
  background: white;
  padding: 12px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.exam-timer {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

/* 主体布局 */
.exam-main {
  display: flex;
  flex: 1;
  padding: 24px 0px;  /* 左右边设为0 */
  gap: 24px;
}

/* 左侧导航栏 */
.exam-sidebar {
  width: 260px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  height: fit-content;
  position: sticky;
  top: 80px;
}

.sidebar-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #e9ecef;
  margin-bottom: 16px;
  font-weight: 600;
  color: #333;
}

.question-group {
  margin-bottom: 20px;
}

.group-title {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 5px;
}

.question-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.nav-btn {
  width: 42px;
  height: 42px;
  font-size: 16px;
  font-weight: 500;
  margin: 0 !important;
}

.nav-btn.el-button--primary {
  background: #409eff;
  border-color: #409eff;
}

.nav-btn.el-button--success {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
}

.nav-btn.el-button--info {
  background: #f0f0f0;
  border-color: #f0f0f0;
  color: #999;
}

.sidebar-legend {
  display: flex;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-dot.answered {
  background: #67c23a;
}

.legend-dot.current {
  background: #409eff;
}

.legend-dot.unanswered {
  background: #f0f0f0;
  border: 1px solid #ddd;
}

/* 右侧答题区 */
.exam-content {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
}

.question-card {
  flex: 1;
}

.question-header {
  margin-bottom: 24px;
}

.question-title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.question-num {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.question-text {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  line-height: 1.5;
}

.question-score {
  font-size: 12px;
  color: #999;
}

/* 选项区域 */
.options-area {
  margin-top: 20px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.option-item:hover {
  background: #f0f0f0;
}

.option-item.selected {
  background: #ecf5ff;
  border-color: #409eff;
}

.option-radio {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.radio-circle {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #ddd;
  background: white;
  position: relative;
  transition: all 0.2s;
}

.radio-circle.checked::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #409eff;
}
.option-label {
  font-weight: 600;
  color: #409eff;
  width: 28px;
}

.option-text {
  flex: 1;
  color: #333;
}


/* 底部按钮 */
.question-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: center;
  gap: 16px;
}

/* 结果弹窗 */
.result-content {
  text-align: center;
}

.result-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.score-display {
  margin: 20px 0;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  color: #409eff;
}

.score-total {
  font-size: 24px;
  color: #999;
}
</style>