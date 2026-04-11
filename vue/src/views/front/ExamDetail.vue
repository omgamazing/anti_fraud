<template>
  <div class="exam-detail-container">
    <div class="exam-detail-card">
      <!-- 头部 -->
      <div class="detail-header">
        <div class="header-left">
          <div class="title-row">
            <h2>{{ data.record?.examTypeName }}</h2>
            <h2 :class="['score', data.record?.userScore >= 60 ? 'pass' : 'fail']">
              {{ data.record?.userScore }}分
            </h2>
          </div>
          <div class="time-row">
            <span class="duration">用时：{{ formatDuration(data.record?.duration) }}</span>
          </div>
        </div>
        <button @click="goBack" class="back-btn">
            <el-icon><ArrowLeftBold /></el-icon>
            <span >返回</span>
        </button>
      </div>

      <!-- 题目列表 -->
      <div class="question-list">
        <div v-for="(detail, idx) in data.details" :key="detail.id" class="question-item">
          <!-- 题目头部 -->
          <div class="question-header">
            <span class="question-num">{{ idx + 1 }}.</span>
            <el-tag :type="getTypeTag(detail.questionType)" size="small">
              {{ getTypeName(detail.questionType) }}
            </el-tag>
            <span class="question-text">{{ detail.questionTitle }}</span>
            <span class="question-score">({{ detail.score }}分)</span>
          </div>

          <!-- 选项区域 -->
          <div class="options-area">
            <div class="options-list">
              <div
                v-for="opt in getOptions(detail)"
                :key="opt.value"
                class="option-item"
                :class="getOptionClass(detail, opt.value)"
              >
                <span class="option-radio">
                  <span :class="['radio-circle', { checked: isUserSelected(detail, opt.value) }]"></span>
                </span>
                <span class="option-label">{{ opt.label }}.</span>
                <span class="option-text">{{ opt.text }}</span>
                <span v-if="isCorrectAnswer(detail, opt.value)" class="correct-mark">✓</span>
              </div>
            </div>
          </div>

          <!-- 答案区域 -->
          <div class="answer-area">
            <div class="user-answer" :class="detail.isCorrect ? 'correct' : 'wrong'">
              <span class="label">你的答案：</span>
              <span class="value">{{ formatAnswer(detail.userAnswer, detail.questionType) }}</span>

            </div>
            <div v-if="!detail.isCorrect" class="correct-answer">
              <span class="label">正确答案：</span>
              <span class="value">{{ formatAnswer(detail.correctAnswer, detail.questionType) }}</span>
            </div>
            <div class="analysis">
              <span class="label">📖 解析：</span>
              <span class="value">{{ detail.analysis || '暂无解析' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部总结 -->
      <div class="detail-footer">
        <div class="summary">
          <span>✅ 正确：{{ data.correctCount }}题</span>
          <span>❌ 错误：{{ data.wrongCount }}题</span>
          <span>📊 得分：{{ data.record?.userScore }}分</span>
        </div>
        <button class="retry-btn" @click="retryExam">再来一次💪</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 所有数据用 reactive 管理
const data = reactive({
  record: null,
  details: [],
  correctCount:0,
  wrongCount:0
})

// 计算正确/错误题数
const correctCount = computed(() => data.details.filter(d => d.isCorrect).length)
const wrongCount = computed(() => data.details.length - correctCount.value)

// 题型映射
const typeName = { 1: '单选题', 2: '多选题', 3: '判断题' }
const typeTag = { 1: 'primary', 2: 'success', 3: 'warning' }

const getTypeName = (type) => typeName[type]
const getTypeTag = (type) => typeTag[type]

// 获取选项列表
const getOptions = (detail) => {
  const options = []
  if (detail.questionType === 3) {
    return [
      { label: 'A', value: '√', text: '正确' },
      { label: 'B', value: '×', text: '错误' }
    ]
  }
  if (detail.optionA) options.push({ label: 'A', value: 'A', text: detail.optionA })
  if (detail.optionB) options.push({ label: 'B', value: 'B', text: detail.optionB })
  if (detail.optionC) options.push({ label: 'C', value: 'C', text: detail.optionC })
  if (detail.optionD) options.push({ label: 'D', value: 'D', text: detail.optionD })
  return options
}

// 判断用户是否选中该选项
const isUserSelected = (detail, value) => {
  if (!detail.userAnswer) return false
  if (detail.questionType === 2) {
    return detail.userAnswer.split(',').includes(value)
  }
  return detail.userAnswer === value
}

// 判断是否是正确答案
const isCorrectAnswer = (detail, value) => {
  return detail.correctAnswer.includes(value)
}

// 获取选项样式
const getOptionClass = (detail, value) => {
  const isCorrect = isCorrectAnswer(detail, value)
  const isSelected = isUserSelected(detail, value)
  return {
    'correct-option': isCorrect,
    'user-selected': isSelected,
    'wrong-selected': isSelected && !isCorrect
  }
}

// 格式化答案
const formatAnswer = (answer, type) => {
  if (!answer) return '未答'
  if (type === 2) {
    return answer.split(',').map(a => a.trim()).join('、')
  }
  if (type === 3) {
    return answer === 'A' ? '正确' : '错误'
  }
  return answer
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '0秒'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (mins === 0) return `${secs}秒`
  return `${mins}分${secs}秒`
}

// 获取考试记录和详情
const fetchDetail = () => {
  const id = route.query.id
  console.log('选取的记录号：', id)

  request.get(`/exam/record/detail/${id}`).then(res => {
    if (res.code === '200') {
      data.record = res.data.record
      data.details = res.data.details
      data.correctCount=data.record.correctCount
      data.wrongCount=data.record.wrongCount
      console.log('考试记录：', data.record)
      console.log('答题详情：', data.details)
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(err => {
    console.log('请求失败:', err)
    ElMessage.error('获取详情失败')
  })
}

// 返回记录页
const goBack = () => {
  router.push('/front/examRecord')
}

// 重新测试
const retryExam = () => {
  router.push({
    path: '/front/examSelect',
    query: { examType: data.record?.examType }
  })
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.exam-detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.exam-detail-card {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 32px;
  padding: 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
}

/* 头部 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 20px;
  border-bottom: 1px solid #e9ecef;
  margin-bottom: 24px;
}

.header-left {
  flex: 1;
}

.title-row {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 8px;
}

.title-row h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.score {
  font-size: 20px;
  font-weight: bold;
}

.score.pass {
  color: #28a745;
}

.score.fail {
  color: #dc3545;
}

.time-row {
  font-size: 14px;
  color: #666;
}

.back-btn {
  padding: 8px 16px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;

}

/* 题目列表 */
.question-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-item {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 16px;
}

.question-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
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
  flex: 1;
}

.question-score {
  font-size: 14px;
  padding:0px 10px ;
  color: #999;
}

/* 选项区域（参考 ExamPaper.vue） */
.options-area {
  margin-top: 20px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: white;
  border-radius: 12px;
  transition: all 0.2s;
  border: 1px solid transparent;
  position: relative;
}

.option-item.correct-option {
  background: #d4edda;
  border-color: #28a745;
}

.option-item.user-selected {
  background: #fff3cd;
  border-color: #ffc107;
}

.option-item.wrong-selected {
  background: #f8d7da;
  border-color: #dc3545;
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

.correct-mark {
  position: absolute;
  right: 16px;
  color: #28a745;
  font-weight: bold;
  font-size: 18px;
}

/* 答案区域 */
.answer-area {
  margin-left: 28px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #ddd;
}

.user-answer, .correct-answer, .analysis {
  display: flex;
  gap: 8px;
  font-size: 13px;
  margin-bottom: 8px;
}

.user-answer .label, .correct-answer .label, .analysis .label {
  font-weight: 600;
  color: #666;
  min-width: 70px;
}

.user-answer.correct {
  color: #28a745;
}

.user-answer.wrong {
  color: #dc3545;
}

.result-icon {
  font-weight: bold;
  margin-left: 4px;
}

.correct-answer {
  color: #28a745;
}

.analysis {
  color: #6c757d;
}

/* 底部 */
.detail-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary {
  display: flex;
  gap: 24px;
  font-size: 14px;
  font-weight: 500;
}

.retry-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}
</style>