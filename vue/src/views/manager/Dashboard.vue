<template>
  <div>
    <!-- 统计卡片（4个） -->
    <div style="display: flex; grid-gap: 10px">
      <!-- 1. 用户帖子总数 -->
      <div style="flex: 1; display: flex; padding: 20px 0; align-items: center" class="card">
        <div style="flex: 1; text-align: center">
          <el-icon size="60" color="#409eff"><Document /></el-icon>
        </div>
        <div style="flex: 1">
          <div style="font-size: 20px; margin-bottom: 10px">用户帖子总数</div>
          <div style="font-size: 20px; font-weight: bold">{{ data.baseData.article || 0 }}</div>
        </div>
      </div>

      <!-- 2. 反诈百科数量 -->
      <div style="flex: 1; display: flex; padding: 20px 0; align-items: center" class="card">
        <div style="flex: 1; text-align: center">
          <el-icon size="60" color="#67c23a"><Reading /></el-icon>
        </div>
        <div style="flex: 1">
          <div style="font-size: 20px; margin-bottom: 10px">反诈百科数量</div>
          <div style="font-size: 20px; font-weight: bold">{{ data.baseData.case || 0 }}</div>
        </div>
      </div>

      <!-- 3. AI模拟次数 -->
      <div style="flex: 1; display: flex; padding: 20px 0; align-items: center" class="card">
        <div style="flex: 1; text-align: center">
          <el-icon size="60" color="#e6a23c"><ChatDotRound /></el-icon>
        </div>
        <div style="flex: 1">
          <div style="font-size: 20px; margin-bottom: 10px">AI模拟次数</div>
          <div style="font-size: 20px; font-weight: bold">{{ data.baseData.simulation || 0 }}</div>
        </div>
      </div>

      <!-- 4. 平台用户总数 -->
      <div style="flex: 1; display: flex; padding: 20px 0; align-items: center" class="card">
        <div style="flex: 1; text-align: center">
          <el-icon size="60" color="#f56c6c"><User /></el-icon>
        </div>
        <div style="flex: 1">
          <div style="font-size: 20px; margin-bottom: 10px">平台用户总数</div>
          <div style="font-size: 20px; font-weight: bold">{{ data.baseData.user || 0 }}</div>
        </div>
      </div>
    </div>

    <!-- AI模拟趋势图（折线图） -->
    <div class="card" style="margin-top: 10px; height: 440px" id="line"></div>

    <!-- 饼图：帖子分类占比 + 考试分类占比 -->
    <div style="margin-top: 10px; display: flex; grid-gap: 10px">
      <div style="flex: 1; height: 380px" class="card" id="pie1"></div>
      <div style="flex: 1; height: 380px" class="card" id="pie2"></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import { Document, Reading, ChatDotRound, User } from "@element-plus/icons-vue";

// 统计数据
const data = reactive({
  baseData: {
    article: 0,
    case: 0,
    simulation: 0,
    user: 0
  }
})

// 加载统计数据
const loadBase = () => {
  request.get('/dashboard/base').then(res => {
    if (res.code === '200') {
      data.baseData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadBase()

// AI模拟趋势图
const loadLineData = () => {
  request.get('/dashboard/simulationTrend').then(res => {
    if (res.code === '200') {
      let chartDom = document.getElementById('line')
      let myChart = echarts.init(chartDom)
      lineOptions.xAxis.data = res.data.x
      lineOptions.series[0].data = res.data.y
      myChart.setOption(lineOptions)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 帖子分类占比（饼图1）
const loadPie1Data = () => {
  request.get('/dashboard/articleCategory').then(res => {
    if (res.code === '200') {
      let chartDom = document.getElementById('pie1')
      let myChart = echarts.init(chartDom)
      pieOptions1.series[0].data = res.data
      myChart.setOption(pieOptions1)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 考试分类占比（饼图2）
const loadPie2Data = () => {
  request.get('/dashboard/examCategory').then(res => {
    if (res.code === '200') {
      let chartDom = document.getElementById('pie2')
      let myChart = echarts.init(chartDom)
      pieOptions2.series[0].data = res.data
      myChart.setOption(pieOptions2)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

onMounted(() => {
  loadLineData()
  loadPie1Data()
  loadPie2Data()
})

// ==================== 图表配置 ====================

// AI模拟趋势图配置
let lineOptions = {
  title: {
    text: 'AI模拟演练趋势图',
    subtext: '统计维度：最近一个月',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    name: '日期',
    type: 'category',
    data: []
  },
  yAxis: {
    name: '模拟次数',
    type: 'value'
  },
  series: [
    {
      name: 'AI模拟次数',
      data: [],
      type: 'line',
      smooth: true
    }
  ]
}

// 饼图1配置（帖子分类）
let pieOptions1 = {
  title: {
    text: '帖子分类占比',
    subtext: '统计维度：反诈分类',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{b} : {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  color: ['#8b9dc3', '#b3cde0', '#e7e8c8', '#f0a78c', '#bc9a6c', '#99b898', '#c7b7a3'],
  series: [
    {
      name: '帖子数量',
      type: 'pie',
      radius: '50%',
      center: ['50%', '50%'],
      data: []
    }
  ]
}

// 饼图2配置（考试分类）
let pieOptions2 = {
  title: {
    text: '考试分类占比',
    subtext: '统计维度：考试类型',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{b} : {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  color: ['#6a8daf', '#b0c3d0', '#c9c7aa', '#e5ae94', '#b39274', '#95ae86', '#c5a59b'],
  series: [
    {
      name: '考试次数',
      type: 'pie',
      radius: '50%',
      center: ['50%', '50%'],
      data: []
    }
  ]
}
</script>