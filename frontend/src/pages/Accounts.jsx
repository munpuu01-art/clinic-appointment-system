import React, { useEffect, useState } from 'react'
import { AccountApi, DoctorApi, PatientApi } from '../api/ApiClient.js'
import { Card, Empty, Field, Modal, Notice, Pill } from '../components/ui.jsx'

const ROLES = [
  { value: 'ADMIN', label: 'ผู้ดูแลระบบ' },
  { value: 'STAFF', label: 'เจ้าหน้าที่เวชระเบียน' },
  { value: 'DOCTOR', label: 'แพทย์' },
  { value: 'PATIENT', label: 'ผู้ป่วย' }
]

const EMPTY = { username: '', password: '', role: 'STAFF', personId: '', displayName: '' }

export default function Accounts() {
  const [accounts, setAccounts] = useState([])
  const [doctors, setDoctors] = useState([])
  const [patients, setPatients] = useState([])
  const [form, setForm] = useState(null)
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)

  const load = () => AccountApi.list().then(setAccounts).catch(setError)

  useEffect(() => {
    load()
    DoctorApi.list().then(setDoctors).catch(() => {})
    PatientApi.search('').then((p) => setPatients(p.content || [])).catch(() => {})
  }, [])

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  const act = async (fn, text) => {
    try { await fn(); setMessage(text); load() } catch (err) { setError(err) }
  }

  const save = async () => {
    try {
      await AccountApi.create({
        username: form.username,
        password: form.password,
        role: form.role,
        personId: form.personId ? Number(form.personId) : null,
        displayName: form.displayName || null
      })
      setMessage(`สร้างบัญชี ${form.username} เรียบร้อย ผู้ใช้ต้องเปลี่ยนรหัสผ่านเมื่อเข้าใช้ครั้งแรก`)
      setForm(null)
      load()
    } catch (err) { setError(err) }
  }

  const personOptions = form?.role === 'DOCTOR' ? doctors : form?.role === 'PATIENT' ? patients : []

  return (
    <>
      <div className="page-head">
        <div>
          <h1>บัญชีผู้ใช้</h1>
          <p>กำหนดบทบาทเพื่อควบคุมว่าผู้ใช้แต่ละคนเข้าถึงส่วนใดของระบบได้บ้าง</p>
        </div>
        <button className="primary" onClick={() => setForm(EMPTY)}>สร้างบัญชีใหม่</button>
      </div>

      <Notice error={error} message={message} onDismiss={() => { setError(null); setMessage(null) }} />

      <Card title={`บัญชีทั้งหมด ${accounts.length} บัญชี`}>
        {accounts.length === 0 ? <Empty>ยังไม่มีบัญชีผู้ใช้</Empty> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>ชื่อผู้ใช้</th><th>ชื่อที่แสดง</th><th>บทบาท</th><th>สถานะ</th><th>เข้าใช้ล่าสุด</th><th>การจัดการ</th></tr>
              </thead>
              <tbody>
                {accounts.map((a) => (
                  <tr key={a.id}>
                    <td className="numeric strong">{a.username}</td>
                    <td>{a.displayName}</td>
                    <td>{a.roleLabel}</td>
                    <td>
                      {a.locked
                        ? <Pill status="CANCELLED" label="ถูกล็อก" />
                        : <Pill status={a.active ? 'COMPLETED' : 'CANCELLED'} label={a.active ? 'ใช้งานได้' : 'ระงับ'} />}
                    </td>
                    <td className="numeric">{a.lastLoginAt ? a.lastLoginAt.replace('T', ' ').slice(0, 16) : '-'}</td>
                    <td>
                      <div className="row wrap">
                        {a.active
                          ? <button className="ghost danger" onClick={() => act(() => AccountApi.deactivate(a.id), 'ระงับบัญชีแล้ว')}>ระงับ</button>
                          : <button className="ghost" onClick={() => act(() => AccountApi.activate(a.id), 'เปิดใช้งานบัญชีแล้ว')}>เปิดใช้งาน</button>}
                        <button className="ghost" onClick={() => {
                          const pwd = window.prompt('รหัสผ่านใหม่ (อย่างน้อย 8 ตัวอักษร)')
                          if (pwd) act(() => AccountApi.resetPassword(a.id, pwd), 'ตั้งรหัสผ่านใหม่แล้ว')
                        }}>ตั้งรหัสผ่านใหม่</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {form && (
        <Modal
          title="สร้างบัญชีผู้ใช้"
          onClose={() => setForm(null)}
          footer={<>
            <button onClick={() => setForm(null)}>ยกเลิก</button>
            <button className="primary" onClick={save}>สร้างบัญชี</button>
          </>}
        >
          <div className="form-grid">
            <Field label="ชื่อผู้ใช้"><input value={form.username} onChange={set('username')} /></Field>
            <Field label="รหัสผ่านเริ่มต้น (อย่างน้อย 8 ตัว)">
              <input type="password" value={form.password} onChange={set('password')} />
            </Field>
            <Field label="บทบาท">
              <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value, personId: '' })}>
                {ROLES.map((r) => <option key={r.value} value={r.value}>{r.label}</option>)}
              </select>
            </Field>
            <Field label="ชื่อที่แสดง"><input value={form.displayName} onChange={set('displayName')} /></Field>
          </div>

          {form.role !== 'ADMIN' && (
            <Field label={form.role === 'DOCTOR' ? 'ผูกกับแพทย์' : form.role === 'PATIENT' ? 'ผูกกับผู้ป่วย' : 'รหัสบุคลากร (personId)'}>
              {personOptions.length > 0 ? (
                <select value={form.personId} onChange={set('personId')}>
                  <option value="">— เลือก —</option>
                  {personOptions.map((p) => (
                    <option key={p.id} value={p.id}>{p.displayName || `${p.hn} · ${p.fullName}`}</option>
                  ))}
                </select>
              ) : (
                <input value={form.personId} onChange={set('personId')} placeholder="ระบุ id ของบุคคล" />
              )}
            </Field>
          )}
          <p className="hint">ผู้ดูแลระบบไม่จำเป็นต้องผูกกับบุคคลในเวชระเบียน ส่วนบทบาทอื่นต้องระบุเสมอ</p>
        </Modal>
      )}
    </>
  )
}
