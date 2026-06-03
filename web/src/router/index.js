import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' },
      },
      {
        path: 'device/list',
        name: 'DeviceList',
        component: () => import('@/views/device/list.vue'),
        meta: { title: '设备列表', icon: 'Monitor', parent: '设备管理' },
      },
      {
        path: 'device/product',
        name: 'Product',
        component: () => import('@/views/device/product.vue'),
        meta: { title: '产品管理', icon: 'Monitor', parent: '设备管理' },
      },
      {
        path: 'room',
        name: 'Room',
        component: () => import('@/views/room/index.vue'),
        meta: { title: '房间管理', icon: 'House' },
      },
      {
        path: 'scene',
        name: 'Scene',
        component: () => import('@/views/scene/index.vue'),
        meta: { title: '场景联动', icon: 'Connection' },
      },
      {
        path: 'alert/rules',
        name: 'AlertRules',
        component: () => import('@/views/alert/rules.vue'),
        meta: { title: '告警规则', icon: 'Bell', parent: '告警监控' },
      },
      {
        path: 'alert/logs',
        name: 'AlertLogs',
        component: () => import('@/views/alert/logs.vue'),
        meta: { title: '告警日志', icon: 'Bell', parent: '告警监控' },
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/profile.vue'),
        meta: { title: '个人中心', icon: 'User' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 如果访问的是公开页面（如登录页），直接放行
  if (to.meta.public) {
    // 如果已登录且访问登录页，跳转到首页
    if (token && to.path === '/login') {
      next('/')
    } else {
      next()
    }
    return
  }

  // 如果没有token，跳转到登录页
  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router
