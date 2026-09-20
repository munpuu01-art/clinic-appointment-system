import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext.jsx'

/**
 * ยามเฝ้าเส้นทาง — ตรวจ 2 ชั้น: ล็อกอินหรือยัง และบทบาทตรงหรือไม่
 * (ฝั่งเซิร์ฟเวอร์ยังบังคับสิทธิ์ซ้ำอีกชั้นเสมอ หน้าจอเป็นเพียงความสะดวก)
 */
export default function RequireRole({ roles, children }) {
  const { isAuthenticated, role, checking } = useAuth()
  const location = useLocation()

  if (checking) {
    return <div className="empty">กำลังตรวจสอบสิทธิ์…</div>
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (roles && !roles.includes(role)) {
    return (
      <div className="page-head">
        <div>
          <h1>ไม่มีสิทธิ์เข้าถึง</h1>
          <p>บัญชีของคุณ ({role}) ไม่ได้รับอนุญาตให้ใช้งานหน้านี้</p>
        </div>
      </div>
    )
  }

  return children
}
