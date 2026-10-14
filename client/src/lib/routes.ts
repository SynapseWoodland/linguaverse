/* 53 视图清单：与 prototype/index.html 一致（52 页面 + /archive 索引） */
export type Phase = 'm' | '1' | '2'
export type ShellKind = 'none' | 'app' | 'admin'

export interface PageMeta {
  pid: string
  path: string
  title: string
  desc?: string
  phase: Phase
  shell: ShellKind
  nav?: string
  group: string
}

const A = 'A · 游客与认证'
const B = 'B · 学习主区'
const C = 'C · 课程与学习'
const D = 'D · 进度与激励'
const E = 'E · 社区与社交'
const F = 'F · 我的与设置'
const G = 'G · 管理后台（P1）'

export const PAGES: PageMeta[] = [
  { pid: 'P01', path: '/', title: '品牌落地页', desc: '护照封面 hero · 三语词排版 · 三邮戳', phase: 'm', shell: 'none', group: A },
  { pid: 'P02', path: '/register', title: '注册', phase: 'm', shell: 'none', group: A },
  { pid: 'P03', path: '/login', title: '登录', desc: '错误态 / 锁定提示', phase: 'm', shell: 'none', group: A },
  { pid: 'P04', path: '/forgot-password', title: '找回密码', desc: '邮箱验证三步', phase: '1', shell: 'none', group: A },
  { pid: 'P05', path: '/onboarding', title: '入门引导', desc: '欢迎 → 选语种 → 定级/直学', phase: 'm', shell: 'none', group: A },
  { pid: 'P47', path: '/404', title: '404', phase: 'm', shell: 'none', group: A },
  { pid: 'P48', path: '/error', title: '断网 / 服务异常', phase: 'm', shell: 'none', group: A },

  { pid: 'P06', path: '/home', title: '学习首页', desc: '今日计划 · 目标环 · Streak', phase: 'm', shell: 'app', nav: 'home', group: B },
  { pid: 'P07', path: '/messages', title: '消息中心', phase: '1', shell: 'app', nav: 'home', group: B },
  { pid: 'P08', path: '/notification-settings', title: '提醒设置', desc: '站内/浏览器/邮件/推送', phase: '1', shell: 'app', nav: 'dashboard', group: B },
  { pid: 'P09', path: '/courses', title: '课程中心', desc: '三语种卡 + 新语种预告', phase: 'm', shell: 'app', nav: 'courses', group: B },

  { pid: 'P10', path: '/course-map', title: '课程地图', desc: '六级虚线路线 · 站点三态', phase: 'm', shell: 'app', nav: 'courses', group: C },
  { pid: 'P11', path: '/placement', title: '定级测试', phase: 'm', shell: 'none', group: C },
  { pid: 'P12', path: '/placement-result', title: '定级结果', phase: 'm', shell: 'none', group: C },
  { pid: 'P13', path: '/lesson', title: '课时学习（护照对页）', phase: 'm', shell: 'none', group: C },
  { pid: 'P14', path: '/flashcards', title: '单词闪卡', phase: 'm', shell: 'none', group: C },
  { pid: 'P15', path: '/grammar', title: '语法练习', phase: 'm', shell: 'none', group: C },
  { pid: 'P16', path: '/listening', title: '听力训练', desc: '含真人发音角标变体', phase: 'm', shell: 'none', group: C },
  { pid: 'P17', path: '/speaking', title: '口语跟读', desc: '评分态 / 自评降级态', phase: 'm', shell: 'none', group: C },
  { pid: 'P18', path: '/lesson-complete', title: '课时结算 · 盖章时刻', phase: 'm', shell: 'none', group: C },
  { pid: 'P19', path: '/review', title: '复习中心（到期队列）', phase: 'm', shell: 'app', nav: 'review', group: C },
  { pid: 'P20', path: '/vocabulary-book', title: '生词本', phase: '1', shell: 'app', nav: 'courses', group: C },
  { pid: 'P21', path: '/forgetting-curve', title: '遗忘曲线', phase: '1', shell: 'app', nav: 'review', group: C },
  { pid: 'P22', path: '/search', title: '搜索', phase: '1', shell: 'app', nav: 'courses', group: C },
  { pid: 'P23', path: '/favorites', title: '我的收藏', phase: '1', shell: 'app', nav: 'dashboard', group: C },
  { pid: 'P24', path: '/ai-chat', title: 'AI 对话练习', phase: '2', shell: 'none', group: C },
  { pid: 'P25', path: '/speaking-pvp', title: '口语 PK', phase: '2', shell: 'none', group: C },

  { pid: 'P26', path: '/dashboard', title: '进度仪表盘', phase: 'm', shell: 'app', nav: 'dashboard', group: D },
  { pid: 'P27', path: '/achievements', title: '成就墙', desc: '邮戳徽章 · 称号区', phase: 'm', shell: 'app', nav: 'achievements', group: D },
  { pid: 'P28', path: '/badge-share', title: '徽章分享卡', phase: '1', shell: 'app', nav: 'achievements', group: D },
  { pid: 'P29', path: '/leaderboard', title: '排行榜', phase: '1', shell: 'app', nav: 'achievements', group: D },
  { pid: 'P30', path: '/certificate', title: '学习证书', phase: '2', shell: 'app', nav: 'dashboard', group: D },
  { pid: 'P31', path: '/data-export', title: '学习数据导出', phase: '2', shell: 'app', nav: 'dashboard', group: D },

  { pid: 'P32', path: '/community', title: '社区信息流', phase: 'm', shell: 'app', nav: 'community', group: E },
  { pid: 'P33', path: '/post-editor', title: '发帖编辑器', phase: 'm', shell: 'app', nav: 'community', group: E },
  { pid: 'P34', path: '/post-detail', title: '帖子详情（盖楼）', phase: '1', shell: 'app', nav: 'community', group: E },
  { pid: 'P35', path: '/profile-user', title: '用户主页', phase: '1', shell: 'app', nav: 'community', group: E },
  { pid: 'P36', path: '/following', title: '关注 / 粉丝', phase: '1', shell: 'app', nav: 'community', group: E },
  { pid: 'P37', path: '/messages-list', title: '私信列表', phase: '1', shell: 'app', nav: 'home', group: E },
  { pid: 'P38', path: '/chat', title: '私信对话', phase: '1', shell: 'app', nav: 'home', group: E },
  { pid: 'P39', path: '/report', title: '举报弹层页', phase: '1', shell: 'none', group: E },

  { pid: 'P40', path: '/profile', title: '个人资料', phase: 'm', shell: 'app', nav: 'dashboard', group: F },
  { pid: 'P41', path: '/account-security', title: '账户与安全', desc: '头像/改密/注销/重置', phase: '1', shell: 'app', nav: 'dashboard', group: F },
  { pid: 'P42', path: '/daily-goal', title: '每日目标', phase: 'm', shell: 'app', nav: 'dashboard', group: F },
  { pid: 'P43', path: '/membership', title: '会员订阅', phase: '2', shell: 'app', nav: 'dashboard', group: F },
  { pid: 'P44', path: '/checkout', title: '支付收银台', phase: '2', shell: 'app', nav: 'dashboard', group: F },
  { pid: 'P45', path: '/classroom', title: '班级 / 教务', phase: '2', shell: 'app', nav: 'dashboard', group: F },
  { pid: 'P46', path: '/download-app', title: '下载 App / PWA', phase: '2', shell: 'app', nav: 'dashboard', group: F },

  { pid: 'P50', path: '/admin-login', title: '后台登录', phase: '1', shell: 'none', group: G },
  { pid: 'P50', path: '/admin-dashboard', title: '数据看板', desc: '含监控指标位', phase: '1', shell: 'admin', nav: 'dash', group: G },
  { pid: 'P51', path: '/admin-users', title: '用户管理', phase: '1', shell: 'admin', nav: 'users', group: G },
  { pid: 'P52', path: '/admin-courses', title: '课程内容管理', phase: '1', shell: 'admin', nav: 'courses', group: G },
  { pid: 'P53', path: '/admin-moderation', title: '内容审核', phase: '1', shell: 'admin', nav: 'mod', group: G },
]

export const TOTAL_VIEWS = PAGES.length // 53 视图（含两个共用 P50 编号的后台页）

export function groupPages(): [string, PageMeta[]][] {
  const order: string[] = []
  const map = new Map<string, PageMeta[]>()
  for (const p of PAGES) {
    if (!map.has(p.group)) {
      map.set(p.group, [])
      order.push(p.group)
    }
    map.get(p.group)!.push(p)
  }
  return order.map((g) => [g, map.get(g)!])
}
