import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { LookupApi, PortalApi } from '../../api/ApiClient.js'
import { Card, Field, Notice, thaiDate, today } from '../../components/ui.jsx'

export default function BookAppointment() {
  const navigate = useNavigate()
  const [specialties, setSpecialties] = useState([])
  const [doctors, setDoctors] = useState([])
  const [types, setTypes] = useState([])
  const [form, setForm] = useState({
    specialtyId: '', doctorId: '', date: today(), startTime: '', type: 'NEW_CASE', symptomNote: ''
  })
  const [slots, setSlots] = useState([])
  const [loadingSlots, setLoadingSlots] = useState(false)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    PortalApi.specialties().then(setSpecialties).catch(setError)
    LookupApi.appointmentTypes().then(setTypes).catch(() => setTypes([
      { value: 'NEW_CASE', label: 'ผู้ป่วยใหม่' },
      { value: 'FOLLOW_UP', label: 'ติดตามอาการ' }
    ]))
  }, [])

  useEffect(() => {
    PortalApi.doctors(form.specialtyId || undefined)
      .then(setDoctors)
      .catch(setError)
  }, [form.specialtyId])

  useEffect(() => {
    if (!form.doctorId || !form.date) { setSlots([]); return }
    let cancelled = false
    setLoadingSlots(true)
    PortalApi.slots(form.doctorId, form.date)
      .then((list) => { if (!cancelled) setSlots(list) })
      .catch((err) => { if (!cancelled) { setSlots([]); setError(err) } })
      .finally(() => { if (!cancelled) setLoadingSlots(false) })
    return () => { cancelled = true }
  }, [form.doctorId, form.date])

  const set = (key, extra = {}) => (e) => setForm({ ...form, [key]: e.target.value, ...extra })

  const submit = async () => {
    setError(null)
    setBusy(true)
    try {
      await PortalApi.book({
        doctorId: Number(form.doctorId),
        date: form.date,
        startTime: form.startTime,
        type: form.type,
        symptomNote: form.symptomNote
      })
      navigate('/portal', { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  const selectedDoctor = doctors.find((d) => String(d.id) === String(form.doctorId))

  return (
    <>
      <div className="page-head">
        <div>
          <h1>จองนัดหมาย</h1>
          <p>เลือกแผนก แพทย์ และช่วงเวลาที่สะดวก ระบบจะแสดงเฉพาะช่องเวลาที่ยังจองได้จริง</p>
        </div>
      </div>

      <Notice error={error} onDismiss={() => setError(null)} />

      <Card title="ขั้นที่ 1 · เลือกแพทย์และวันที่">
        <div className="form-grid">
          <Field label="แผนก">
            <select value={form.specialtyId} onChange={set('specialtyId', { doctorId: '', startTime: '' })}>
              <option value="">ทุกแผนก</option>
              {specialties.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </Field>
          <Field label="แพทย์">
            <select value={form.doctorId} onChange={set('doctorId', { startTime: '' })}>
              <option value="">— เลือกแพทย์ —</option>
              {doctors.map((d) => <option key={d.id} value={d.id}>{d.displayName} · {d.specialtyName}</option>)}
            </select>
          </Field>
          <Field label="วันที่ต้องการ">
            <input type="date" min={today()} value={form.date} onChange={set('date', { startTime: '' })} />
          </Field>
          <Field label="ประเภทการเข้ารับบริการ">
            <select value={form.type} onChange={set('type')}>
              {types.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
            </select>
          </Field>
        </div>
        {selectedDoctor && (
          <p className="hint">
            ค่าตรวจเริ่มต้น {Number(selectedDoctor.consultationFee || 0).toLocaleString()} บาท
            · ห้องตรวจ {selectedDoctor.roomNo || '-'}
          </p>
        )}
      </Card>

      <Card title={`ขั้นที่ 2 · เลือกช่วงเวลา (${thaiDate(form.date)})`}>
        {!form.doctorId ? <p className="hint">กรุณาเลือกแพทย์ก่อน</p>
          : loadingSlots ? <p className="hint">กำลังตรวจสอบช่องเวลาว่าง…</p>
          : slots.length === 0 ? <p className="hint">แพทย์ไม่ออกตรวจในวันดังกล่าว กรุณาเลือกวันอื่น</p> : (
          <div className="slots">
            {slots.map((s) => (
              <button
                key={s.startTime}
                type="button"
                title={s.unavailableReason || 'ว่าง'}
                disabled={!s.available}
                className={`slot ${form.startTime === s.startTime ? 'selected' : ''}`}
                onClick={() => setForm({ ...form, startTime: s.startTime })}
              >
                {String(s.startTime).slice(0, 5)}
              </button>
            ))}
          </div>
        )}
      </Card>

      <Card title="ขั้นที่ 3 · อาการเบื้องต้นและยืนยัน">
        <Field label="อาการหรือเรื่องที่ต้องการปรึกษา">
          <textarea rows="3" value={form.symptomNote} onChange={set('symptomNote')} />
        </Field>
        <div className="row">
          <button className="primary" disabled={!form.startTime || busy} onClick={submit}>
            {busy ? 'กำลังจอง…' : 'ยืนยันการจองนัด'}
          </button>
          {form.startTime && (
            <span className="hint">
              นัดวันที่ {thaiDate(form.date)} เวลา {String(form.startTime).slice(0, 5)} น.
            </span>
          )}
        </div>
      </Card>
    </>
  )
}
