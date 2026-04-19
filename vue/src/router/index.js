import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/manager/home' },
    {
      path: '/manager',//父路径：管理员后台根路径
      component: () => import('@/views/Manager.vue'),
      children: [   //子路由：嵌套在Manager.vue中，path是相对路径，@代表src
        { path: 'home', meta: { name: '系统首页' }, component: () => import('@/views/manager/Home.vue'),  },
        { path: 'admin', meta: { name: '管理员信息' }, component: () => import('@/views/manager/Admin.vue'), },
        //{ path: 'notice', meta: { name: '系统公告' }, component: () => import('@/views/manager/Notice.vue'), },
        { path: 'person', meta: { name: '个人资料' }, component: () => import('@/views/manager/Person.vue'), },
        { path: 'password', meta: { name: '修改密码' }, component: () => import('@/views/manager/Password.vue'), },
        { path: 'user', meta: { name: '用户信息' }, component: () => import('@/views/manager/User.vue'), },
        { path: 'category', meta: { name: '反诈分类' }, component: () => import('@/views/manager/Category.vue'), },
        { path: 'article', meta: { name: '论坛帖子' }, component: () => import('@/views/manager/Article.vue'), },
        { path: 'collect', meta: { name: '收藏信息' }, component: () => import('@/views/manager/Collect.vue'), },
        { path: 'comment', meta: { name: '评论信息' }, component: () => import('@/views/manager/Comment.vue'), },
        { path: 'activity', meta: { name: '活动信息' }, component: () => import('@/views/manager/Activity.vue'), },
        { path: 'case', meta: { name: '案例管理' }, component: () => import('@/views/manager/Case.vue'), },
        { path: 'carousel', meta: { name: '轮播图信息' }, component: () => import('@/views/manager/Carousel.vue'), },
        { path: 'dashboard', meta: { name: '数据统计' }, component: () => import('@/views/manager/Dashboard.vue'), },
        { path: 'exam', meta: { name: '考题管理' }, component: () => import('@/views/manager/Exam.vue'), },
      ]
    },
    {
      path: '/front',//前端用户
      component: () => import('@/views/Front.vue'),
      children: [
        { path: 'home', component: () => import('@/views/front/Home.vue'),  },
        { path: 'person', component: () => import('@/views/front/Person.vue'),  },
        { path: 'password', component: () => import('@/views/front/Password.vue'),  },
        { path: 'myArticle', component: () => import('@/views/front/MyArticle.vue'),  },
        { path: 'myCollect', component: () => import('@/views/front/MyCollect.vue'),  },
        { path: 'myComment', component: () => import('@/views/front/MyComment.vue'),  },
        { path: 'article', component: () => import('@/views/front/Article.vue'),  },
        { path: 'articleDetail', component: () => import('@/views/front/ArticleDetail.vue'),  },
        { path: 'case', component: () => import('@/views/front/Case.vue'),  },
        { path: 'caseDetail', component: () => import('@/views/front/CaseDetail.vue'),  },
        { path: 'report', component: () => import('@/views/front/Report.vue'),  },
        { path: 'notice', component: () => import('@/views/front/Notice.vue'),  },
        { path: 'aichat', component: () => import('@/views/front/AiChat.vue'),  },
        { path: 'mySimulation', component: () => import('@/views/front/MySimulation.vue'),  },
        { path: 'examSelect', component: () => import('@/views/front/ExamSelect.vue'),  },
        { path: 'examPaper', component: () => import('@/views/front/ExamPaper.vue'),  },
        { path: 'examRecord', component: () => import('@/views/front/ExamRecord.vue'),  },
        { path: 'examDetail', component: () => import('@/views/front/ExamDetail.vue'),  },

      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') },
    { path: '/404', component: () => import('@/views/404.vue') },
    { path: '/:pathMatch(.*)', redirect: '/404' } //兜底路由，通配符路由，访问不存在的路由会重定向/404
  ]
})

export default router
//把创建好的路由实例导出，在 Vue 项目的入口文件（main.js）中引入并挂载到 Vue 应用上，路由才能生效。
