import { Link } from 'react-router-dom'
import type { PageMeta } from '../lib/routes'
import type { CSSVars } from '../lib/css'
import PhaseBadge from '../components/PhaseBadge'

/** P47 404：盖歪的邮戳 */
export default function NotFoundPage({ meta }: { meta: PageMeta }) {
  return (
    <>
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 20px', textAlign: 'center' }}>
        <div>
          <div className="rubber muted-stamp" style={{ '--s': '150px', margin: '0 auto', transform: 'rotate(14deg)' } as CSSVars}>
            <span className="r-main" style={{ fontSize: 26 }}>
              404
            </span>
            <span className="r-date">NOT FOUND</span>
          </div>
          <h1 className="serif-cn mt-lg" style={{ fontSize: 30 }}>
            这一站，在地图上还不存在
          </h1>
          <p className="muted mt-md">你要找的页面可能被搬走了，或者地址写错了。不如回到熟悉的路线上。</p>
          <div className="row gap-md mt-xl" style={{ justifyContent: 'center' }}>
            <Link className="btn btn-primary btn-lg" to="/home">
              回到今日首页
            </Link>
            <Link className="btn btn-ghost btn-lg" to="/course-map">
              查看课程地图
            </Link>
          </div>
        </div>
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
