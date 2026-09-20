import React, { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { Field, Notice } from '../components/ui.jsx'

const EMPTY_SIGNUP = {
  username: '', password: '', confirm: '',
  firstName: '', lastName: '', gender: 'MALE', birthDate: '',
  nationalId: '', phone: '', email: ''
}

export default function Login() {
  const { login, register } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [mode, setMode] = useState('staff')   // staff | patient | signup
  const [credentials, setCredentials] = useState({ username: '', password: '' })
  const [signup, setSignup] = useState(EMPTY_SIGNUP)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const go = (session) => {
    const fallback = session.role === 'PATIENT' ? '/portal' : '/dashboard'
    navigate(location.state?.from || fallback, { replace: true })
  }

  const submitLogin = async (e) => {
    e.preventDefault()
    setError(null)
    setBusy(true)
    try {
      go(await login(credentials.username, credentials.password))
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  const submitSignup = async (e) => {
    e.preventDefault()
    setError(null)
    if (signup.password !== signup.confirm) {
      setError({ message: 'รหัสผ่านทั้งสองช่องไม่ตรงกัน' })
      return
    }
    setBusy(true)
    try {
      const session = await register(signup.username, signup.password, {
        firstName: signup.firstName,
        lastName: signup.lastName,
        gender: signup.gender,
        birthDate: signup.birthDate || null,
        nationalId: signup.nationalId || null,
        phone: signup.phone,
        email: signup.email
      })
      go(session)
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  const setCred = (key) => (e) => setCredentials({ ...credentials, [key]: e.target.value })
  const setSign = (key) => (e) => setSignup({ ...signup, [key]: e.target.value })

  return (
    <div className="auth-shell">
      <section className="auth-side">
        <div className="auth-brand">
          <strong>คลินิกใจดี</strong>
          <span>ระบบนัดหมายและจัดคิว</span>
        </div>
        <p className="auth-tagline">
          จองนัดกับแพทย์ ตรวจสอบคิวหน้าห้องตรวจ และดูประวัติการรักษาของคุณได้ในที่เดียว
        </p>
        <div className="auth-demo">
          <div className="auth-demo-title">บัญชีสำหรับทดลองใช้ (โปรไฟล์ dev)</div>
          <ul>
            <li><code>admin</code> — ผู้ดูแลระบบ</li>
            <li><code>staff</code> — เจ้าหน้าที่เวชระเบียน</li>
            <li><code>doctor</code> — แพทย์</li>
            <li><code>piya</code> — ผู้ป่วย</li>
          </ul>
          <div className="auth-demo-note">รหัสผ่านทุกบัญชี: <code>Clinic@123</code></div>
        </div>
      </section>

      <section className="auth-main">
        <div className="auth-card">
          <div className="auth-tabs" role="tablist">
            <button
              role="tab"
              aria-selected={mode === 'staff'}
              className={mode === 'staff' ? 'active' : ''}
              onClick={() => { setMode('staff'); setError(null) }}
            >เจ้าหน้าที่ / แพทย์</button>
            <button
              role="tab"
              aria-selected={mode === 'patient'}
              className={mode === 'patient' ? 'active' : ''}
              onClick={() => { setMode('patient'); setError(null) }}
            >ผู้ป่วย</button>
            <button
              role="tab"
              aria-selected={mode === 'signup'}
              className={mode === 'signup' ? 'active' : ''}
              onClick={() => { setMode('signup'); setError(null) }}
            >สมัครสมาชิก</button>
          </div>

          <Notice error={error} onDismiss={() => setError(null)} />

          {mode !== 'signup' ? (
            <form onSubmit={submitLogin}>
              <h1>{mode === 'staff' ? 'เข้าสู่ระบบสำหรับบุคลากร' : 'เข้าสู่ระบบผู้ป่วย'}</h1>
              <p className="hint">
                {mode === 'staff'
                  ? 'สำหรับผู้ดูแลระบบ เจ้าหน้าที่เวชระเบียน และแพทย์'
                  : 'ดูนัดหมาย จองนัดใหม่ และดูประวัติการรักษาของคุณ'}
              </p>
              <Field label="ชื่อผู้ใช้">
                <input value={credentials.username} onChange={setCred('username')} autoComplete="username" />
              </Field>
              <Field label="รหัสผ่าน">
                <input type="password" value={credentials.password} onChange={setCred('password')} autoComplete="current-password" />
              </Field>
              <button className="primary block" type="submit" disabled={busy}>
                {busy ? 'กำลังเข้าสู่ระบบ…' : 'เข้าสู่ระบบ'}
              </button>
              {mode === 'patient' && (
                <p className="hint center">
                  ยังไม่มีบัญชี? <button type="button" className="link" onClick={() => setMode('signup')}>สมัครสมาชิกที่นี่</button>
                </p>
              )}
            </form>
          ) : (
            <form onSubmit={submitSignup}>
              <h1>สมัครสมาชิกผู้ป่วย</h1>
              <p className="hint">ระบบจะออกเลขประจำตัวผู้ป่วย (HN) ให้อัตโนมัติหลังสมัครสำเร็จ</p>
              <div className="form-grid">
                <Field label="ชื่อผู้ใช้"><input value={signup.username} onChange={setSign('username')} autoComplete="username" /></Field>
                <Field label="เบอร์โทรศัพท์"><input value={signup.phone} onChange={setSign('phone')} /></Field>
                <Field label="รหัสผ่าน (อย่างน้อย 8 ตัว)">
                  <input type="password" value={signup.password} onChange={setSign('password')} autoComplete="new-password" />
                </Field>
                <Field label="ยืนยันรหัสผ่าน">
                  <input type="password" value={signup.confirm} onChange={setSign('confirm')} autoComplete="new-password" />
                </Field>
                <Field label="ชื่อ"><input value={signup.firstName} onChange={setSign('firstName')} /></Field>
                <Field label="นามสกุล"><input value={signup.lastName} onChange={setSign('lastName')} /></Field>
                <Field label="เพศ">
                  <select value={signup.gender} onChange={setSign('gender')}>
                    <option value="MALE">ชาย</option>
                    <option value="FEMALE">หญิง</option>
                    <option value="OTHER">อื่น ๆ</option>
                  </select>
                </Field>
                <Field label="วันเกิด"><input type="date" value={signup.birthDate} onChange={setSign('birthDate')} /></Field>
                <Field label="เลขบัตรประชาชน"><input value={signup.nationalId} onChange={setSign('nationalId')} /></Field>
                <Field label="อีเมล"><input value={signup.email} onChange={setSign('email')} /></Field>
              </div>
              <button className="primary block" type="submit" disabled={busy}>
                {busy ? 'กำลังสมัคร…' : 'สมัครและเข้าสู่ระบบ'}
              </button>
            </form>
          )}
        </div>
      </section>
    </div>
  )
}
