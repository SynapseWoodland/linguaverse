import { apiFetch } from './api'
import { useSession, type SessionUser } from './session'

/* ================= 类型（对齐 server/CONTRACT.md + DTO） ================= */

export interface PageVO<T> {
  page: number
  size: number
  total: number
  totalPages: number
  items: T[]
}

export interface Enrollment {
  langCode: string
  placementLevel: number | null
  startedAt: string
}

export interface MeResponse {
  user: SessionUser & { bio?: string | null; role?: string }
  enrollments: Enrollment[]
}

export interface LessonNode {
  id: number
  lessonNo: number
  seqNo: number
  slug: string
  title: string
  titleNative: string
  completed: boolean
  unlocked: boolean
}

export interface UnitNode {
  unitNo: number
  title: string
  titleNative: string
  lessons: LessonNode[]
}

export interface LevelNode {
  levelNo: number
  title: string
  released: boolean
  units: UnitNode[]
}

export interface Language {
  code: string
  nameZh: string
  nameNative: string
  color: string
  sortNo: number
  enrolled: boolean
  enrollmentPlacementLevel?: number
  placementLevel: number | null
  levels: LevelNode[]
}

export interface Vocab {
  word: string
  phonetic: string
  meaningZh: string
  example: string
  exampleZh: string
}

export interface Question {
  id: number
  sortNo: number
  stem: string
  options: string[]
}

export interface GrammarDetail {
  title: string
  explanation: string
  questions: Question[]
}

export interface ListeningLine {
  speaker: string
  text: string
  zh: string
}

export interface ListeningDetail {
  id: number
  title: string
  lines: ListeningLine[]
  questionStem: string
  options: string[]
}

export interface SpeakingSentence {
  id: number
  sortNo: number
  text: string
  textZh: string
}

export interface LessonDetail {
  id: number
  langCode: string
  seqNo: number
  slug: string
  title: string
  titleNative: string
  scene: string
  unlocked: boolean
  completed: boolean
  vocab: Vocab[]
  grammar: GrammarDetail
  listening: ListeningDetail
  speaking: SpeakingSentence[]
  completedStages: string[]
}

export interface GrammarResult {
  lessonId: number
  correctCount: number
  total: number
  score: number
  results: { questionId: number; choiceIndex: number; correct: boolean; correctIndex: number; explanation: string }[]
}

export interface ListeningResult {
  lessonId: number
  correct: boolean
  choiceIndex: number
  correctIndex: number
  score: number
  explanation: string
}

export interface SpeakingResult {
  lessonId: number
  mode: string
  score: number
  results: { sentenceId: number; score: number | null; feedback: string }[]
}

export interface PlacementPaper {
  langCode: string
  total: number
  questions: { id: number; sortNo: number; stem: string; options: string[] }[]
}

export interface PlacementResultData {
  langCode: string
  correctCount: number
  total: number
  suggestedLevel: number
  enrolled: boolean
}

export interface WordCard {
  id: number
  langCode: string
  word: string
  phonetic: string
  meaningZh: string
  stage: number
  dueDate: string
  lastRating: string | null
  mastered: boolean
}

export interface DueQueue {
  today: string
  total: number
  cards: WordCard[]
}

export interface CompleteResult {
  lessonId: number
  alreadyCompleted: boolean
  xp: { base: number; bonus: number; total: number }
  accuracy: number
  wordsCount: number
  durationSec: number
  streak: number
  completedAt: string
  nextLesson: { id: number; seqNo: number; slug: string; title: string } | null
  newAchievements: string[]
}

export interface Dashboard {
  level: { level: number; totalXp: number; xpIntoLevel: number; xpForNext: number; progressPercent: number }
  streak: number
  today: { xp: number; goalXp: number; goalReached: boolean; remainingXp: number; minutes: number; completedLessons: number }
  weekXp: { date: string; xp: number }[]
  languages: {
    langCode: string
    nameZh: string
    color: string
    placementLevel: number | null
    completedLessons: number
    totalLessons: number
    percent: number
    masteredWords: number
    learningWords: number
    avgAccuracy: number | null
  }[]
  modules: { code: string; labelZh: string; avgScore: number | null; attempts: number }[]
  vocab: { mastered: number; learning: number; dueToday: number; byLang: { langCode: string; mastered: number; learning: number; dueToday: number }[] }
}

export interface Recommendation {
  key: string
  type: string
  title: string
  reason: string
  ctaText: string
  deeplink: string
  priority: number
}

export interface TodayPlan {
  date: string
  todayXp: number
  goalXp: number
  goalReached: boolean
  message: string
  recommendations: Recommendation[]
}

export interface Achievement {
  code: string
  nameZh: string
  descZh: string
  icon: string
  unlocked: boolean
  unlockedAt: string | null
  current: number
  target: number
}

export interface Author {
  id: number
  username: string
  nickname: string
  avatarSeed: string
}

export interface Post {
  id: number
  langCode: string | null
  title: string | null
  content: string
  likeCount: number
  commentCount: number
  liked: boolean
  author: Author
  createdAt: string
}

export interface Comment {
  id: number
  content: string
  author: Author
  createdAt: string
}

/* ================= 端点封装 ================= */

const get = <T>(path: string): Promise<T> => apiFetch<T>(path)
const post = <T>(path: string, body?: unknown): Promise<T> =>
  apiFetch<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) })
const patch = <T>(path: string, body: unknown): Promise<T> =>
  apiFetch<T>(path, { method: 'PATCH', body: JSON.stringify(body) })
const put = <T>(path: string, body: unknown): Promise<T> =>
  apiFetch<T>(path, { method: 'PUT', body: JSON.stringify(body) })
const del = <T>(path: string): Promise<T> => apiFetch<T>(path, { method: 'DELETE' })

export const sdk = {
  register: (username: string, password: string) =>
    post<{ token: string; user: SessionUser }>('/auth/register', { username, password }),
  login: (username: string, password: string) =>
    post<{ token: string; user: SessionUser }>('/auth/login', { username, password }),
  me: () => get<MeResponse>('/me'),
  updateMe: (body: { nickname?: string; bio?: string; avatarSeed?: string; dailyGoalXp?: number }) =>
    patch<MeResponse['user']>('/me', body),
  onboarding: (mode: 'PLACEMENT' | 'DIRECT' | 'SKIP', langCode: string, placementLevel?: number) =>
    post<MeResponse>('/me/onboarding', { mode, langCode, placementLevel }),

  courses: () => get<Language[]>('/courses'),
  courseTree: (lang: string) => get<Language>(`/courses/${lang}`),
  enroll: (langCode: string) => post<unknown>('/enrollments', { langCode }),

  placementPaper: (lang: string) => get<PlacementPaper>(`/placement/${lang}`),
  placementSubmit: (lang: string, answers: number[]) =>
    post<PlacementResultData>(`/placement/${lang}`, { answers }),

  lesson: (id: number) => get<LessonDetail>(`/lessons/${id}`),
  grammarSubmit: (id: number, answers: { questionId: number; choiceIndex: number }[]) =>
    post<GrammarResult>(`/lessons/${id}/grammar/submit`, { answers }),
  listeningSubmit: (id: number, choiceIndex: number) =>
    post<ListeningResult>(`/lessons/${id}/listening/submit`, { choiceIndex }),
  speakingSubmit: (
    id: number,
    mode: 'RECOGNITION' | 'SELF',
    sentences: { sentenceId: number; transcript?: string; score?: number; rating?: string }[],
  ) => post<SpeakingResult>(`/lessons/${id}/speaking/submit`, { mode, sentences }),
  stageProgress: (id: number, stage: 'VOCAB' | 'GRAMMAR' | 'LISTENING' | 'SPEAKING', score: number) =>
    put<unknown>(`/lessons/${id}/progress`, { stage, score }).catch(() => undefined),
  complete: (id: number, durationSec: number) =>
    post<CompleteResult>(`/lessons/${id}/complete`, { durationSec }),

  due: (lang: string, limit?: number) =>
    get<DueQueue>(`/review/due?lang=${lang}${limit ? `&limit=${limit}` : ''}`),
  rate: (langCode: string, word: string, rating: 'UNKNOWN' | 'VAGUE' | 'KNOWN') =>
    post<WordCard>('/review/rate', { langCode, word, rating }),

  dashboard: () => get<Dashboard>('/dashboard'),
  todayPlan: () => get<TodayPlan>('/today/recommendations'),

  posts: (params: { lang?: string; page?: number; size?: number } = {}) => {
    const q = new URLSearchParams()
    if (params.lang) q.set('lang', params.lang)
    if (params.page) q.set('page', String(params.page))
    if (params.size) q.set('size', String(params.size))
    const qs = q.toString()
    return get<PageVO<Post>>(`/posts${qs ? `?${qs}` : ''}`)
  },
  createPost: (body: { langCode?: string; title?: string; content: string }) =>
    post<{ id: number }>('/posts', body),
  toggleLike: (id: number) => post<{ postId: number; liked: boolean; likeCount: number }>(`/posts/${id}/like`),
  comments: (id: number) => get<Comment[]>(`/posts/${id}/comments`),
  addComment: (id: number, content: string) => post<Comment>(`/posts/${id}/comments`, { content }),
  deletePost: (id: number) => del<null>(`/posts/${id}`),

  achievements: () => get<Achievement[]>('/achievements'),
}

/** deeplink 可能是 /lessons/1 或 /review?lang=en —— 统一转站内路由跳转。 */
export function followDeeplink(deeplink: string): string {
  if (deeplink.startsWith('/lessons/')) {
    const id = deeplink.substring('/lessons/'.length).split(/[?&]/)[0]
    return `/lesson?id=${id}`
  }
  if (deeplink.startsWith('/review')) {
    const lang = new URLSearchParams(deeplink.includes('?') ? deeplink.substring(deeplink.indexOf('?')) : '').get('lang')
    return lang ? `/review?lang=${lang}` : '/review'
  }
  return deeplink
}

export function signOut() {
  useSession.getState().clear()
  window.location.assign('/login')
}
