import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import api, { AuthApi, TokenStore } from '../api/ApiClient.js'

const AuthContext = createContext(null)

/** เมนูที่แต่ละบทบาทเห็น — ตรงกับสิทธิ์ที่ backend บังคับไว้อีกชั้นหนึ่ง */
export const MENU_BY_ROLE = {
  ADMIN: [
    { to: '/dashboard', label: 'ภาพรวมวันนี้' },
    { to: '/appointments', label: 'นัดหมาย' },
    { to: '/queue', label: 'คิวหน้าห้องตรวจ' },
    { to: '/patients', label: 'ผู้ป่วย' },
    { to: '/doctors', label: 'แพทย์และตารางออกตรวจ' },
    { to: '/accounts', label: 'บัญชีผู้ใช้' }
  ],
  STAFF: [
    { to: '/dashboard', label: 'ภาพรวมวันนี้' },
    { to: '/appointments', label: 'นัดหมาย' },
    { to: '/queue', label: 'คิวหน้าห้องตรวจ' },
    { to: '/patients', label: 'ผู้ป่วย' },
    { to: '/doctors', label: 'แพทย์และตารางออกตรวจ' }
  ],
  DOCTOR: [
    { to: '/dashboard', label: 'ภาพรวมวันนี้' },
    { to: '/appointments', label: 'นัดหมาย' },
    { to: '/queue', label: 'คิวหน้าห้องตรวจ' },
    { to: '/patients', label: 'ผู้ป่วย' },
    { to: '/doctors', label: 'แพทย์และตารางออกตรวจ' }
  ],
  PATIENT: [
    { to: '/portal', label: 'นัดหมายของฉัน' },
    { to: '/portal/book', label: 'จองนัดใหม่' },
    { to: '/portal/records', label: 'ประวัติการรักษา' }
  ]
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => TokenStore.load())
  const [checking, setChecking] = useState(!!TokenStore.token())

  // ให้ ApiClient แจ้งกลับมาเมื่อเจอ 401 เพื่อเคลียร์สถานะล็อกอิน
  useEffect(() => {
    api.onUnauthorized = () => {
      TokenStore.clear()
      setSession(null)
    }
    return () => { api.onUnauthorized = null }
  }, [])

  // ตรวจว่า token ที่เก็บไว้ยังใช้ได้อยู่หรือไม่ตอนเปิดหน้าเว็บ
  useEffect(() => {
    if (!TokenStore.token()) { setChecking(false); return }
    AuthApi.me()
      .then(() => setChecking(false))
      .catch(() => { TokenStore.clear(); setSession(null); setChecking(false) })
  }, [])

  const value = useMemo(() => ({
    session,
    checking,
    isAuthenticated: !!session,
    role: session?.role || null,
    isPatient: session?.role === 'PATIENT',
    isAdmin: session?.role === 'ADMIN',
    menu: session ? (MENU_BY_ROLE[session.role] || []) : [],
    can: (permission) => !!session?.permissions?.includes(permission),

    async login(username, password) {
      const result = await AuthApi.login(username, password)
      TokenStore.save(result)
      setSession(result)
      return result
    },

    async register(username, password, patient) {
      const result = await AuthApi.register(username, password, patient)
      TokenStore.save(result)
      setSession(result)
      return result
    },

    logout() {
      TokenStore.clear()
      setSession(null)
    }
  }), [session, checking])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth ต้องอยู่ภายใน AuthProvider')
  return ctx
}
