import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import DashboardView from './views/DashboardView.vue'
import TenantsView from './views/TenantsView.vue'
import SponsorsView from './views/SponsorsView.vue'
import ContestsView from './views/ContestsView.vue'
import RubricsView from './views/RubricsView.vue'
import CertificatesView from './views/CertificatesView.vue'
import MyView from './views/MyView.vue'
import JudgeView from './views/JudgeView.vue'
import SettingsView from './views/SettingsView.vue'
import SubmitWorkView from './views/SubmitWorkView.vue'
import MembersView from './views/MembersView.vue'
import ShopView from './views/ShopView.vue'
import ShopAdminView from './views/ShopAdminView.vue'
import PublicHome from './views/PublicHome.vue'
import ContestList from './views/ContestList.vue'
import ContestDetail from './views/ContestDetail.vue'
import VerifyView from './views/VerifyView.vue'
import NotFoundView from './views/NotFoundView.vue'
import { auth } from './auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: PublicHome, meta: { public: true } },
    { path: '/contests', component: ContestList, meta: { public: true } },
    { path: '/contests/:id', component: ContestDetail, meta: { public: true } },
    { path: '/shop', component: ShopView, meta: { public: true } },
    { path: '/verify/:code?', component: VerifyView, meta: { public: true } },
    { path: '/login', component: LoginView, meta: { public: true } },
    { path: '/me', component: MyView },
    { path: '/submit/:contestId', component: SubmitWorkView },
    { path: '/judge', component: JudgeView },
    { path: '/settings', component: SettingsView },
    { path: '/members', component: MembersView },
    { path: '/shop-admin', component: ShopAdminView },
    { path: '/dashboard', component: DashboardView },
    { path: '/tenants', component: TenantsView },
    { path: '/sponsors', component: SponsorsView },
    { path: '/contests-admin', component: ContestsView },
    { path: '/rubrics', component: RubricsView },
    { path: '/certificates', component: CertificatesView },
    { path: '/:pathMatch(.*)*', component: NotFoundView, meta: { public: true } }
  ]
})

router.beforeEach((to) => {
  if (!to.meta.public && !auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/dashboard' && auth.user?.role === 'PLAYER') {
    return '/me'
  }
  if (to.path === '/dashboard' && auth.user?.role === 'JUDGE') {
    return '/judge'
  }
})

export default router
