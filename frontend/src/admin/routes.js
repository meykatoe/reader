import { adminAuth, can, clearAdmin, loadAdmin } from './store'

const LOGIN = '/admin/login'

export const adminRoutes = [
  {
    path: LOGIN,
    component: () => import('./views/AdminLoginView.vue'),
    meta: { admin: true, public: true },
  },
  {
    path: '/admin',
    component: () => import('./views/AdminLayout.vue'),
    meta: { admin: true },
    redirect: '/admin/books',
    children: [
      { path: 'books', component: () => import('./views/AdminBooksView.vue') },
      {
        path: 'users',
        component: () => import('./views/AdminUsersView.vue'),
        meta: { authority: 'USER_VIEW' },
      },
    ],
  },
]

export async function adminGuard(to) {
  await loadAdmin()
  if (!to.meta.public && !adminAuth.admin) return LOGIN
  if (to.path === LOGIN && adminAuth.admin) return '/admin'
  if (to.meta.authority && !can(to.meta.authority)) return '/admin'
}

export function handleAdminUnauthorized(router) {
  clearAdmin()
  router.push(LOGIN)
}
