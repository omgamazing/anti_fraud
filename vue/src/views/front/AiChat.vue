<template>
  <div class="chat-container">
    <div class="chat-card">
      <!-- 头部：场景选择 + 开始按钮 -->
      <div class="chat-header">
        <div class="scene-wrapper">
          <select v-model="data.scene" class="scene-select":disabled="data.isSimulating">
            <option value="" disabled>请选择模拟场景</option>
            <option v-for="s in data.scenes" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>
         <div class="control-buttons">
         <button
             @click="startSimulation"
             class="start-btn"
             :disabled="data.isSimulating || !data.scene">
             <span class="btn-icon">🎬</span>
             开始模拟
         </button>
         <button
             @click="endSimulation"
             class="end-btn"
             :disabled="!data.isSimulating">
             <span class="btn-icon">💟 </span>
             结束模拟
         </button>
         </div>
      </div>

      <!-- 聊天内容区 -->
      <div class="chat-messages" :ref="(el) => data.chatContent = el" :class="{ 'empty-state': data.messages.length === 0 }">
        <div v-if="data.messages.length === 0" class="empty-message">
          <div class="empty-icon">💬</div>
          <p>选择场景，点击「开始模拟」开始对话</p>
        </div>
        <div
          v-for="(item, index) in data.messages"
          :key="index"
          :class="['message', item.role === 'ai' ? 'ai-message' : 'user-message']"
        >
          <div class="message-avatar">
            {{ item.role === 'ai' ? '🤖' : '👤' }}
          </div>
           <div class="message-content">
            <div class="message-bubble">{{ item.content }}</div>
          </div>
        </div>
      </div>

       <!-- 输入框（只在模拟中可用） -->
      <div class="chat-input-area" v-if="data.isSimulating">
        <div class="input-wrapper">
          <input
            v-model="data.inputMsg"
            @keyup.enter="sendMessage"
            placeholder="输入你的回复..."
            class="message-input"
          />
          <button @click="sendMessage" class="send-btn" :disabled="!data.inputMsg.trim()">发送</button>
        </div>
      </div>
      <div class="chat-input-area" v-else>
        <div class="input-wrapper disabled">
          <input disabled placeholder="请先点击「开始模拟」..." class="message-input disabled" />
        </div>
      </div>
    </div>
  </div>

<!-- 结果弹窗 -->
  <div v-if="data.showResultModal" class="modal-overlay">
    <div class="modal-content" :class="resultType">
      <div class="modal-icon">{{ data.resultType === 'success' ? '✅' : '❌' }}</div>
      <h3>{{ data.resultType === 'success' ? '模拟成功！' : '模拟失败！' }}</h3>
      <p>{{ data.resultMessage }}</p>
      <div class="modal-stats">
        <p>对话轮次: {{ data.currentRound }}</p>
        <p>模拟时长: {{ data.simulationDuration }}</p>
      </div>
      <div class="modal-buttons">
        <button @click="closeResultModal" class="modal-btn primary">继续模拟</button>
        <button @click="goToMySimulation" class="modal-btn secondary">查看记录</button>
      </div>
      <div class="modal-hint">💪 继续加油！换个场景再试试？</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick,reactive } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'  // 如果用了 Element Plus

//获取路由实例（Vue3组合式API写法）
const route = useRoute()
const router = useRouter()
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  scenes: ['刷单诈骗', '冒充客服', '网贷诈骗', '杀猪盘', '冒充公检法'],
  scene:'',
  inputMsg:'',      //输入框
  messages: [],          //聊天记录数组
  chatContent:null,     //引用聊天区域DOM,用于滚动
  isSimulating:false,       // 是否在模拟中
  sessionId:'',         // 生成或获取会话ID
  //结果弹窗
  showResultModal:false,
  resultType:'success',     // success 或 fail
  resultMessage:'',
  currentRound:0,
  simulationDuration:'0秒',
  simulationStartTime:null

})

// 测试：是否能拿到真正的用户ID
console.log("当前登录用户ID：", data.user.id)


// 关闭结果弹窗
const closeResultModal = () => {
  data.showResultModal = false
}

// "查看记录“跳转至MySimulation.vue
const goToMySimulation = () => {
  data.showResultModal = false
  router.push({
    path: '/front/mySimulation'
  })
}

// 生成会话ID（可以存在localStorage里）
onMounted(() => {
  let sid = localStorage.getItem('chat_session_id')
  if (!sid) {
    sid = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    localStorage.setItem('chat_session_id', sid)
  }
  data.sessionId = sid


  // 从 URL 获取场景参数
  const sceneFromUrl = route.query.scene
  if (sceneFromUrl && data.scenes.includes(sceneFromUrl)) {
    data.scene = sceneFromUrl
    // 自动开始模拟
    startSimulation()
  }
})

// ===== 定义：滚动函数，只在需要时调用 =====
const scrollToBottom = () => {
  if (data.chatContent) {
    setTimeout(() => {
      data.chatContent.scrollTop = data.chatContent.scrollHeight
    }, 100)  // 加个小延迟，确保DOM已更新
  }
}

// 滚动并等待DOM更新
const scrollAndWait = async () => {
  await nextTick()
  scrollToBottom()
}

// 计算时长
const getDuration = () => {
  if (!data.simulationStartTime) return '0秒'
  const duration = Math.floor((Date.now() - data.simulationStartTime) / 1000)
  return duration < 60 ? `${duration}秒` : `${Math.floor(duration / 60)}分${(duration % 60).toString().padStart(2, '0')}秒`
}
// 计算当前轮次（AI消息数量）
const getCurrentRound = () => {
  return data.messages.filter(m => m.role === 'ai').length-1
}

// 添加AI消息
const addAiMessage = (content) => {
  data.messages.push({ role: 'ai', content })
}

// 添加用户消息
const addUserMessage = (content) => {
  data.messages.push({ role: 'user', content })
}

// 清空并重置状态
const resetSimulation = () => {
  data.messages = []
  data.isSimulating = false
  data.currentRound = 0
  data.simulationStartTime = null
}

// 显示结果弹窗
const showResultModalWindow = (type, customMessage) => {
  const results = {
    success: {
      type: 'success',
      title: '模拟成功！',
      message: customMessage || '恭喜你，成功识别诈骗！本次模拟通过！'
    },
    fail: {
      type: 'fail',
      title: '模拟失败！',
      message: customMessage || '被骗啦，下次一定可以的！'
    }
  }

  const result = results[type]
  data.resultType = result.type
  data.resultMessage = result.message
  data.simulationDuration = getDuration()
  data.currentRound = getCurrentRound()
  data.showResultModal = true

}


// 保存记录到数据库
const saveRecord =() => {
console.log("进入函数 1")

  request.post('/api/record/save', {
      userId: data.user.id,
      sessionId:data.sessionId,
      scene: data.scene,
      result: data.resultType,
      rounds: getCurrentRound(),
      duration: getDuration(),
      messages: JSON.stringify(data.messages)
    }).then(res => {
      if (res.code === '200') {
        console.log("准备保存记录:", res.data)
        ElMessage.success({
          message: '记录已保存，可查看',
          duration: 1000
        })
      } else {
        ElMessage.error(res.msg)
        ElMessage.error("save服务器错误")
      }
    })



}


// ==================== 核心业务函数 ====================

// 开始模拟
const startSimulation = () => {
  if (data.isSimulating) return

  const userId = data.user.id
  resetSimulation()
  data.isSimulating = true
  data.simulationStartTime = Date.now()


  request.post('/api/ai/start', {
       //userId:userId,  //可以不要
       scene: data.scene,
       sessionId: data.sessionId

     }).then(async (res)=> {
       if (res.code === '200') {
         console.log('✅ 后端返回成功:', res.data)
         addAiMessage(res.data.aiReply)
         await scrollAndWait()

         //data.records = res.data
       }else {
         ElMessage.error(res.msg)
       }

     }).catch (err => {
         addAiMessage('❌ 服务连接失败，请检查后端')
         ElMessage.error('错误')
         data.isSimulating = false
     })


}

// 结束模拟
const endSimulation = () => {
  if (!data.isSimulating) return

  addAiMessage('你主动结束了模拟，有机会再试试吧')

  data.isSimulating = false
  showResultModalWindow('fail', '你主动结束了模拟，有机会再试试吧！')
  saveRecord()
}

// 发送消息
const sendMessage = async () => {
  const msg = data.inputMsg.trim()
  if (!msg || !data.isSimulating) return

  addUserMessage(msg)
  console.log('✅ 用户发送消息成功：', msg)
  data.inputMsg = ''
  await scrollAndWait()

  request.post('/api/ai/chat', {
      userId: data.user.id,
      scene: data.scene,
      message: msg,
      sessionId: data.sessionId

    }).then(async (res)=>  {
      if (res.code === '200') {
        console.log('✅ AI发送消息成功：', res.data)
        addAiMessage(res.data.aiReply)
        console.log('现在的消息列表:', data.messages)
        scrollAndWait()

        if (res.data.isFinish) {
          data.isSimulating = false
          if (res.data.resultType === 'success') {
            showResultModalWindow('success', '恭喜你，模拟成功！')
          } else {
            showResultModalWindow('fail', '很遗憾，模拟未通过！')
          }
          saveRecord();
        }
      } else {
        ElMessage.error(res.msg || '发送失败')
      }
    }).catch(err => {
      console.log('❌ 消息发送错误：', err)
      addAiMessage('服务连接失败，请检查后端')
      scrollAndWait()
    })


}


</script>

<style scoped>
.chat-container {
  min-height: 100vh;
  width:100vw;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  box-sizing: border-box;
}

.chat-card {
  width: 700px;
  height: 600px;
  max-width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  display: flex;
  flex-direction: column;

}

.chat-header {
  padding: 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.scene-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.scene-select {
  padding: 8px 12px;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  font-size: 14px;
  color: #495057;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 140px;
}

.scene-select:hover:not(:disabled) {
  border-color: #667eea;
}

.scene-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.scene-select:disabled {
  background: #e9ecef;
  cursor: not-allowed;
  opacity: 0.6;
}

.control-buttons {
  display: flex;
  gap: 20px;
}

.start-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.start-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
}

.start-btn:hover{
  transform: translateY(0);
  cursor: not-allowed;
}

.start-btn:active {
  transform: translateY(0);
}

.end-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(220, 53, 69, 0.3);
}

.end-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(220, 53, 69, 0.4);
}

.end-btn:disabled {
  cursor: not-allowed;
}

.btn-icon {
  font-size: 16px;
}

/* 聊天区域样式 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.chat-messages.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-message {
  text-align: center;
  color: #adb5bd;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-message p {
  font-size: 14px;
  margin: 0;
}

.message {
  display: flex;
  gap: 12px;
  animation: fadeIn 0.3s ease;
  width: 100%;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/*
.message-sender {
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 4px;
  font-weight: 500;
}*/

.message-bubble {
  max-width: 70%;  /* 最大宽度限制（超过就换行） */
  width: fit-content;  /* 宽度自适应内容 */
  min-width: 50px;  /* 最小宽度，避免太短 */
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.ai-message .message-bubble {
  background: white;
  border-bottom-left-radius: 4px;
  color: #212529;
}

.user-message {
  justify-content: flex-end;  /* 整体靠右 */
  flex-direction: row-reverse;
  gap: 12px;
  width: 100%;  /* 添加：占满整行 */
  display: flex;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
  margin-left: auto;  /* 添加：自动左边距，推向右 */
}

.user-message .message-sender {
  text-align: right;
}


.chat-input-area {
  padding: 20px;
  background: white;
  border-top: 1px solid #e9ecef;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.message-input {
  flex: 1;
  padding: 14px 18px;
  border: 2px solid #e9ecef;
  border-radius: 25px;
  font-size: 14px;
  transition: all 0.3s;
  background: #f8f9fa;
}

.message-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.message-input.disabled {
  background: #f1f3f5;
  color: #adb5bd;
  cursor: not-allowed;
}

.send-btn {
  padding: 14px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.send-btn:active:not(:disabled) {
  transform: translateY(0);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 20px;
  padding: 30px;
  width: 90%;
  max-width: 400px;
  text-align: center;
  animation: slideIn 0.3s ease;
}

.modal-content.success {
  border-top: 4px solid #28a745;
}

.modal-content.fail {
  border-top: 4px solid #dc3545;
}

.modal-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.modal-content h3 {
  font-size: 24px;
  margin-bottom: 10px;
  color: #212529;
}

.modal-content p {
  color: #6c757d;
  margin-bottom: 20px;
}

.modal-stats {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 20px;
}

.modal-stats p {
  margin: 5px 0;
  color: #495057;
}

.modal-buttons {
  display: flex;
  gap: 10px;
}

.modal-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.modal-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.modal-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

@keyframes slideIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 滚动条美化 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式 */
@media (max-width: 600px) {
  .chat-card {
    height: 100vh;
    max-height: none;
    border-radius: 0;
    width: 100%;
  }

  .chat-container {
    padding: 0;
  }

  .chat-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .scene-wrapper {
    width: 100%;
  }

  .control-buttons {
    width: 100%;
  }

  .start-btn, .end-btn {
    width: 100%;
    justify-content: center;
  }
}
.modal-hint {
  font-size: 13px;
  color: #6c757d;
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #e9ecef;
}
</style>