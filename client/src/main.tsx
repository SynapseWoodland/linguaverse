import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { router } from './router'
import Toaster from './components/Toaster'
import OfflineBanner from './components/OfflineBanner'
import './styles/theme.css'
import './styles/passport.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <OfflineBanner />
    <RouterProvider router={router} />
    <Toaster />
  </React.StrictMode>,
)
