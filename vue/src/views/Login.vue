<template>
  <div class="auth-container">
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>
    <div class="bg-shape shape-3"></div>

    <div class="auth-box">
      <div class="logo-area">
        <div class="logo-text">网络安全反诈模拟系统</div>
        <div class="logo-sub">Anti-Fraud Simulation System</div>
      </div>

      <div class="welcome-text">
        <h2>欢迎登录</h2>
        <p>请输入您的账号信息</p>
      </div>

      <el-form ref="formRef" :model="data.form" :rules="data.rules">
        <el-form-item prop="username">
          <el-input
            size="large"
            v-model="data.form.username"
            placeholder="请输入账号"
            :prefix-icon="User"
            class="custom-input"
          ></el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            show-password
            size="large"
            v-model="data.form.password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            class="custom-input"
          ></el-input>
        </el-form-item>

        <el-form-item prop="role">
          <el-select
            size="large"
            v-model="data.form.role"
            placeholder="请选择角色"
            class="custom-select"
          >
            <el-option value="ADMIN" label="管理员"></el-option>
            <el-option value="USER" label="普通用户"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button size="large" type="primary" class="auth-btn" @click="login">
            <span>登 录</span>
            <el-icon><Right /></el-icon>
          </el-button>
        </el-form-item>

        <div class="auth-link">
          还没有账号？请 <router-link to="/register">注册</router-link>
        </div>
      </el-form>

      <div class="auth-footer">
        <span class="dot"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock, Right } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import "@/assets/css/front.css";

const data = reactive({
  form: {
    username: '',
    password: '',
    role: ''
  },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { min: 2, max: 20, message: '账号长度在2-20个字符之间', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
    ],
    role: [
      { required: true, message: '请选择角色', trigger: 'blur' }
    ],
  }
})

const formRef = ref()

const login = () => {
  formRef.value.validate(valid => {
    if (valid) {
      request.post('/login', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('登录成功')
          localStorage.setItem('xm-user', JSON.stringify(res.data))
          setTimeout(() => {
            if ('ADMIN' === res.data.role) {
              location.href = '/manager/dashBoard'
            } else {
              location.href = '/front/home'
            }
          }, 500)
        } else {
          ElMessage.error(res.msg)
        }
      }).catch(() => {
        ElMessage.error('登录失败，请检查网络')
      })
    }
  })
}
</script>

<style scoped>
/* 登录页特有样式 */
.custom-select {
  width: 100%;
}

.custom-select :deep(.el-input__wrapper) {
  border-radius: 12px;
}
</style>