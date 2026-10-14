import type { ReactNode } from 'react'
import { createBrowserRouter, Navigate, type RouteObject } from 'react-router-dom'
import { PAGES, type PageMeta } from './lib/routes'
import LearnerShell from './components/LearnerShell'
import AdminShell from './components/AdminShell'
import PlaceholderPage from './pages/PlaceholderPage'
import ArchivePage from './pages/ArchivePage'
import LandingPage from './pages/landing/LandingPage'
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import OnboardingPage from './pages/OnboardingPage'
import NotFoundPage from './pages/NotFoundPage'
import ErrorPage from './pages/ErrorPage'
import PlacementPage from './pages/learn/PlacementPage'
import PlacementResultPage from './pages/learn/PlacementResultPage'
import CoursesPage from './pages/learn/CoursesPage'
import CourseMapPage from './pages/learn/CourseMapPage'
import LessonPage from './pages/learn/LessonPage'
import GrammarPage from './pages/learn/GrammarPage'
import ListeningPage from './pages/learn/ListeningPage'
import SpeakingPage from './pages/learn/SpeakingPage'
import LessonCompletePage from './pages/learn/LessonCompletePage'
import ReviewPage from './pages/learn/ReviewPage'
import HomePage from './pages/home/HomePage'
import DashboardPage from './pages/progress/DashboardPage'
import AchievementsPage from './pages/progress/AchievementsPage'
import CommunityPage from './pages/community/CommunityPage'
import PostEditorPage from './pages/community/PostEditorPage'
import PostDetailPage from './pages/community/PostDetailPage'
import ProfilePage from './pages/me/ProfilePage'
import { RequireAuth } from './lib/ui'

/** 免登录可访问的页 */
const PUBLIC = new Set(['/', '/register', '/login', '/forgot-password', '/404', '/error'])

/** 已迁移的真实页（path → 组件），其余仍走占位页 */
const REAL: Record<string, (meta: PageMeta) => ReactNode> = {
  '/': (m) => <LandingPage meta={m} />,
  '/register': (m) => <RegisterPage meta={m} />,
  '/login': (m) => <LoginPage meta={m} />,
  '/forgot-password': (m) => <ForgotPasswordPage meta={m} />,
  '/onboarding': (m) => <OnboardingPage meta={m} />,
  '/404': (m) => <NotFoundPage meta={m} />,
  '/error': (m) => <ErrorPage meta={m} />,
  '/placement': (m) => <PlacementPage meta={m} />,
  '/placement-result': (m) => <PlacementResultPage meta={m} />,
  '/courses': (m) => <CoursesPage meta={m} />,
  '/course-map': (m) => <CourseMapPage meta={m} />,
  '/lesson': (m) => <LessonPage meta={m} />,
  '/grammar': (m) => <GrammarPage meta={m} />,
  '/listening': (m) => <ListeningPage meta={m} />,
  '/speaking': (m) => <SpeakingPage meta={m} />,
  '/lesson-complete': (m) => <LessonCompletePage meta={m} />,
  '/review': (m) => <ReviewPage meta={m} />,
  '/home': (m) => <HomePage meta={m} />,
  '/dashboard': (m) => <DashboardPage meta={m} />,
  '/achievements': (m) => <AchievementsPage meta={m} />,
  '/community': (m) => <CommunityPage meta={m} />,
  '/post-editor': (m) => <PostEditorPage meta={m} />,
  '/post-detail': (m) => <PostDetailPage meta={m} />,
  '/profile': (m) => <ProfilePage meta={m} />,
}

function leaf(p: PageMeta): RouteObject {
  const element = REAL[p.path] ? REAL[p.path](p) : <PlaceholderPage meta={p} />
  return {
    path: p.path,
    element: PUBLIC.has(p.path) ? element : <RequireAuth>{element}</RequireAuth>,
    handle: { nav: p.nav, phase: p.phase },
  }
}

const bare = PAGES.filter((p) => p.shell === 'none').map(leaf)
const appPages = PAGES.filter((p) => p.shell === 'app').map(leaf)
const adminPages = PAGES.filter((p) => p.shell === 'admin').map(leaf)

export const router = createBrowserRouter([
  { path: '/archive', element: <ArchivePage /> },
  ...bare,
  { element: <LearnerShell />, children: appPages },
  { element: <AdminShell />, children: adminPages },
  { path: '*', element: <Navigate to="/404" replace /> },
])
