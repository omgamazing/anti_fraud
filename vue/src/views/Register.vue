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
        <h2>创建账号</h2>
        <p>加入我们，开启新的旅程</p>
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

        <el-form-item prop="confirmPassword">
          <el-input
            show-password
            size="large"
            v-model="data.form.confirmPassword"
            placeholder="请确认密码"
            :prefix-icon="Lock"
            class="custom-input"
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button size="large" type="primary" class="auth-btn" @click="register">
            <span>注 册</span>
            <el-icon><Right /></el-icon>
          </el-button>
        </el-form-item>

        <div class="auth-link">
          已有账号？请 <router-link to="/login">登录</router-link>
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

const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== data.form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const data = reactive({
  form: {
    username: '',
    password: '',
    confirmPassword: ''
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
    confirmPassword: [
      { validator: validatePass, trigger: 'blur' }
    ]
  }
})

const formRef = ref()

const register = () => {
  formRef.value.validate(valid => {
    if (valid) {
      request.post('/register', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('注册成功，即将跳转到登录页')
          setTimeout(() => {
            location.href = '/login'
          }, 1500)
        } else {
          ElMessage.error(res.msg)
        }
      }).catch(() => {
        ElMessage.error('注册失败，请检查网络')
      })
    }
  })
}
</script>

