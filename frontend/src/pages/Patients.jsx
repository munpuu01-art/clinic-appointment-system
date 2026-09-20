import React, { useEffect, useState } from 'react'
import { PatientApi } from '../api/ApiClient.js'
import { Card, Empty, Field, Modal, Notice, Pill } from '../components/ui.jsx'

const EMPTY_FORM = {
  firstName: '', lastName: '', gender: 'MALE', birthDate: '', nationalId: '',
  phone: '', email: '', addressLine: '', district: '', province: '', postcode: '',
  bloodType: '', allergies: '', chronicDisease: '', emergencyContact: ''
}

export default function Patients() {
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(null)
  const [form, setForm] = useState(null)
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)

  const load = () => PatientApi.search(keyword).then(setPage).catch(setError)
  useEffect(() => { load() }, [])

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  const save = async () => {
    try {
      const saved = await PatientApi.register(form)
      setMessage(`ลงทะเบียนผู้ป่วยสำเร็จ เลข ${saved.hn}`)
      setForm(null)
      load()
    } catch (err) { setError(err) }
  }

  return (
    <>
      <div className="page-head">
        <div>
          <h1>ทะเบียนผู้ป่วย</h1>
          <p>ค้นหาด้วยชื่อ เลข HN หรือเบอร์โทร</p>
        </div>
        <div className="row">
          <input
            placeholder="ค้นหาผู้ป่วย"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && load()}
            style={{ width: 240 }}
          />
          <button onClick={load}>ค้นหา</button>
          <button className="primary" onClick={() => setForm(EMPTY_FORM)}>ลงทะเบียนผู้ป่วยใหม่</button>
        </div>
      </div>

      <Notice error={error} message={message} onDismiss={() => { setError(null); setMessage(null) }} />

      <Card title={`พบ ${page?.totalElements ?? 0} รายการ`}>
        {!page || page.content.length === 0 ? <Empty>ยังไม่มีผู้ป่วยที่ตรงกับคำค้น</Empty> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>HN</th><th>ชื่อ-นามสกุล</th><th>อายุ</th><th>โทรศัพท์</th><th>แพ้ยา</th><th>โรคประจำตัว</th></tr>
              </thead>
              <tbody>
                {page.content.map((p) => (
                  <tr key={p.id}>
                    <td className="numeric">{p.hn}</td>
                    <td>{p.fullName} {p.elderly && <Pill status="CALLED" label="ผู้สูงอายุ" />}</td>
                    <td className="numeric">{p.age ?? '-'}</td>
                    <td className="numeric">{p.phone || '-'}</td>
                    <td>{p.allergies || '-'}</td>
                    <td>{p.chronicDisease || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {form && (
        <Modal
          title="ลงทะเบียนผู้ป่วยใหม่"
          onClose={() => setForm(null)}
          footer={<>
            <button onClick={() => setForm(null)}>ยกเลิก</button>
            <button className="primary" onClick={save}>บันทึกผู้ป่วย</button>
          </>}
        >
          <div className="form-grid">
            <Field label="ชื่อ"><input value={form.firstName} onChange={set('firstName')} /></Field>
            <Field label="นามสกุล"><input value={form.lastName} onChange={set('lastName')} /></Field>
            <Field label="เพศ">
              <select value={form.gender} onChange={set('gender')}>
                <option value="MALE">ชาย</option>
                <option value="FEMALE">หญิง</option>
                <option value="OTHER">อื่น ๆ</option>
              </select>
            </Field>
            <Field label="วันเกิด"><input type="date" value={form.birthDate} onChange={set('birthDate')} /></Field>
            <Field label="เลขบัตรประชาชน"><input value={form.nationalId} onChange={set('nationalId')} /></Field>
            <Field label="โทรศัพท์"><input value={form.phone} onChange={set('phone')} /></Field>
            <Field label="อีเมล"><input value={form.email} onChange={set('email')} /></Field>
            <Field label="กรุ๊ปเลือด"><input value={form.bloodType} onChange={set('bloodType')} /></Field>
          </div>
          <Field label="ที่อยู่"><input value={form.addressLine} onChange={set('addressLine')} /></Field>
          <div className="form-grid">
            <Field label="อำเภอ/เขต"><input value={form.district} onChange={set('district')} /></Field>
            <Field label="จังหวัด"><input value={form.province} onChange={set('province')} /></Field>
            <Field label="รหัสไปรษณีย์"><input value={form.postcode} onChange={set('postcode')} /></Field>
          </div>
          <Field label="ประวัติแพ้ยา (คั่นด้วยจุลภาค)"><input value={form.allergies} onChange={set('allergies')} /></Field>
          <Field label="โรคประจำตัว"><input value={form.chronicDisease} onChange={set('chronicDisease')} /></Field>
          <Field label="ผู้ติดต่อฉุกเฉิน"><input value={form.emergencyContact} onChange={set('emergencyContact')} /></Field>
        </Modal>
      )}
    </>
  )
}
