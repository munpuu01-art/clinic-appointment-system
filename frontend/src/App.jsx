import React from 'react'
import { NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import { useAuth } from './auth/AuthContext.jsx'
import RequireRole from './auth/RequireRole.jsx'
import Login from './pages/Login.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Patients from './pages/Patients.jsx'
import Doctors from './pages/Doctors.jsx'
import Appointments from './pages/Appointments.jsx'
import QueueBoard from './pages/QueueBoard.jsx'
import Accounts from './pages/Accounts.jsx'
import MyAppointments from './pages/portal/MyAppointments.jsx'
import BookAppointment from './pages/portal/BookAppointment.jsx'
import MyRecords from './pages/portal/MyRecords.jsx'

const STAFF_ROLES = ['ADMIN', 'STAFF', 'DOCTOR']

/** โครงหน้าจอหลังล็อกอิน — เมนูมาจากบทบาทของผู้ใช้ */
function Shell({ children }) {
  const { session, menu, logout } = useAuth()
  const navigate = useNavigate()

  const signOut = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <strong>คลินิกใจดี</strong>
          <span>{session?.role === 'PATIENT' ? 'พอร์ทัลผู้ป่วย' : 'ระบบนัดหมายและคิว'}</span>
        </div>

        <nav className="nav">
          {menu.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === '/portal'}>{item.label}</NavLink>
          ))}
        </nav>

        <div className="who">
          <div className="who-name">{session?.displayName}</div>
          <div className="who-role">{session?.roleLabel}{session?.hn ? ` · ${session.hn}` : ''}</div>
          <button className="signout" onClick={signOut}>ออกจากระบบ</button>
        </div>
      </aside>

      <main className="content">{children}</main>
    </div>
  )
}

/** ส่งผู้ใช้ไปยังหน้าแรกที่เหมาะกับบทบาทของตน */
function HomeRedirect() {
  const { isAuthenticated, role, checking } = useAuth()
  if (checking) return <div className="empty">กำลังตรวจสอบสิทธิ์…</div>
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <Navigate to={role === 'PATIENT' ? '/portal' : '/dashboard'} replace />
}

export default function App() {
  const { isAuthenticated, role } = useAuth()

  return (
    <Routes>
      <Route
        path="/login"
        element={isAuthenticated
          ? <Navigate to={role === 'PATIENT' ? '/portal' : '/dashboard'} replace />
          : <Login />}
      />

      {/* ---------- หน้าจอหลังบ้าน ---------- */}
      <Route path="/dashboard" element={
        <RequireRole roles={STAFF_ROLES}><Shell><Dashboard /></Shell></RequireRole>} />
      <Route path="/appointments" element={
        <RequireRole roles={STAFF_ROLES}><Shell><Appointments /></Shell></RequireRole>} />
      <Route path="/queue" element={
        <RequireRole roles={STAFF_ROLES}><Shell><QueueBoard /></Shell></RequireRole>} />
      <Route path="/patients" element={
        <RequireRole roles={STAFF_ROLES}><Shell><Patients /></Shell></RequireRole>} />
      <Route path="/doctors" element={
        <RequireRole roles={STAFF_ROLES}><Shell><Doctors /></Shell></RequireRole>} />
      <Route path="/accounts" element={
        <RequireRole roles={['ADMIN']}><Shell><Accounts /></Shell></RequireRole>} />

      {/* ---------- พอร์ทัลผู้ป่วย ---------- */}
      <Route path="/portal" element={
        <RequireRole roles={['PATIENT']}><Shell><MyAppointments /></Shell></RequireRole>} />
      <Route path="/portal/book" element={
        <RequireRole roles={['PATIENT']}><Shell><BookAppointment /></Shell></RequireRole>} />
      <Route path="/portal/records" element={
        <RequireRole roles={['PATIENT']}><Shell><MyRecords /></Shell></RequireRole>} />

      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  )
}
