import React, { useEffect, useMemo, useState } from 'react'
import { AppointmentApi, DoctorApi, LookupApi, PatientApi } from '../api/ApiClient.js'
import { Card, Empty, Field, Modal, Notice, Pill, today, thaiDate } from '../components/ui.jsx'

const EMPTY_BOOKING = {
  patientId: '', doctorId: '', date: today(), startTime: '',
  type: 'NEW_CASE', symptomNote: '', createdBy: 'เจ้าหน้าที่เวชระเบียน'
}

export default function Appointments() {
  const [date, setDate] = useState(today())
  const [rows, setRows] = useState([])
  const [doctors, setDoctors] = useState([])
  const [patients, setPatients] = useState([])
  const [types, setTypes] = useState([])
  const [booking, setBooking] = useState(null)
  const [slots, setSlots] = useState([])
  const [loadingSlots, setLoadingSlots] = useState(false)
  const [reschedule, setReschedule] = useState(null)
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)

  // endpoint คืนค่าเป็น Page ของ Spring Data จึงต้องอ่านจาก content
  const load = (d = date) => AppointmentApi.byDate(d)
    .then((page) => setRows(Array.isArray(page) ? page : (page?.content || [])))
    .catch(setError)

  useEffect(() => { load(date) }, [date])
  useEffect(() => {
    DoctorApi.list().then(setDoctors).catch(setError)
    PatientApi.search('').then((page) => setPatients(page.content || [])).catch(setError)
    LookupApi.appointmentTypes().then(setTypes).catch(setError)
  }, [])

  // ดึงช่องเวลาว่างใหม่ทุกครั้งที่เปลี่ยนแพทย์หรือวันที่ในฟอร์มจอง
  useEffect(() => {
    if (!booking?.doctorId || !booking?.date) { setSlots([]); return }
    let cancelled = false
    setLoadingSlots(true)
    DoctorApi.slots(booking.doctorId, booking.date, booking.patientId || undefined)
      .then((list) => { if (!cancelled) setSlots(list) })
      .catch((err) => { if (!cancelled) { setSlots([]); setError(err) } })
      .finally(() => { if (!cancelled) setLoadingSlots(false) })
    return () => { cancelled = true }
  }, [booking?.doctorId, booking?.date, booking?.patientId])

  const summary = useMemo(() => {
    const count = (status) => rows.filter((r) => r.status === status).length
    return {
      total: rows.length,
      waiting: count('REQUESTED') + count('CONFIRMED'),
      inClinic: count('CHECKED_IN') + count('IN_PROGRESS'),
      done: count('COMPLETED')
    }
  }, [rows])

  const act = async (fn, successText) => {
    try {
      await fn()
      setMessage(successText)
      load()
    } catch (err) { setError(err) }
  }

  const submitBooking = async () => {
    try {
      const saved = await AppointmentApi.book({
        ...booking,
        patientId: Number(booking.patientId),
        doctorId: Number(booking.doctorId)
      })
      setMessage(`จองนัดสำเร็จ เลขที่ ${saved.appointmentNo} เวลา ${saved.startTime}`)
      setBooking(null)
      setDate(saved.date)
      load(saved.date)
    } catch (err) { setError(err) }
  }

  const submitReschedule = async () => {
    try {
      await AppointmentApi.reschedule(reschedule.id, {
        date: reschedule.date,
        startTime: reschedule.startTime,
        reason: reschedule.reason
      })
      setMessage('เลื่อนนัดเรียบร้อย')
      setReschedule(null)
      load()
    } catch (err) { setError(err) }
  }

  const can = (row, status) => (row.allowedTransitions || []).includes(status)

  return (
    <>
      <div className="page-head">
        <div>
          <h1>นัดหมาย</h1>
          <p>ทุกปุ่มด้านล่างถูกเปิด/ปิดตาม State Pattern ของใบนัด ไม่ใช่เงื่อนไขในหน้าจอ</p>
        </div>
        <div className="row">
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          <button className="primary" onClick={() => setBooking({ ...EMPTY_BOOKING, date })}>จองนัดใหม่</button>
        </div>
      </div>

      <Notice error={error} message={message} onDismiss={() => { setError(null); setMessage(null) }} />

      <div className="stat-row">
        <div className="stat"><div className="label">นัดทั้งหมด</div><div className="value">{summary.total}</div></div>
        <div className="stat"><div className="label">รอมาถึง</div><div className="value">{summary.waiting}</div></div>
        <div className="stat"><div className="label">อยู่ในคลินิก</div><div className="value">{summary.inClinic}</div></div>
        <div className="stat"><div className="label">ตรวจเสร็จ</div><div className="value">{summary.done}</div></div>
      </div>

      <Card title={`ตารางนัดวันที่ ${thaiDate(date)}`}>
        {rows.length === 0 ? <Empty>ยังไม่มีนัดหมายในวันนี้</Empty> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>เวลา</th><th>เลขที่นัด</th><th>ผู้ป่วย</th><th>แพทย์</th>
                  <th>ประเภท</th><th>สถานะ</th><th>คิว</th><th>การจัดการ</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id}>
                    <td className="numeric">{r.startTime} – {r.endTime}</td>
                    <td className="numeric">{r.appointmentNo}</td>
                    <td>{r.patientName}<span className="sub"> {r.hn}</span></td>
                    <td>{r.doctorName}<span className="sub"> {r.specialtyName}</span></td>
                    <td>{r.typeLabel}</td>
                    <td><Pill status={r.status} label={r.statusLabel} /></td>
                    <td className="numeric">{r.ticketNo || '-'}</td>
                    <td>
                      <div className="row wrap">
                        {can(r, 'CONFIRMED') && (
                          <button className="ghost" onClick={() => act(() => AppointmentApi.confirm(r.id), 'ยืนยันนัดแล้ว')}>ยืนยัน</button>
                        )}
                        {can(r, 'CHECKED_IN') && (
                          <button className="ghost" onClick={() => act(() => AppointmentApi.checkIn(r.id), 'เช็คอินและออกบัตรคิวแล้ว')}>เช็คอิน</button>
                        )}
                        {can(r, 'CONFIRMED') && (
                          <button className="ghost" onClick={() => setReschedule({ id: r.id, date: r.date, startTime: r.startTime, reason: '' })}>เลื่อนนัด</button>
                        )}
                        {can(r, 'NO_SHOW') && (
                          <button className="ghost" onClick={() => act(() => AppointmentApi.noShow(r.id), 'บันทึกว่าไม่มาตามนัด')}>ไม่มาตามนัด</button>
                        )}
                        {can(r, 'CANCELLED') && (
                          <button className="ghost danger" onClick={() => {
                            const reason = window.prompt('เหตุผลในการยกเลิกนัด')
                            if (reason) act(() => AppointmentApi.cancel(r.id, reason), 'ยกเลิกนัดแล้ว')
                          }}>ยกเลิก</button>
                        )}
                        {(r.allowedTransitions || []).length === 0 && <span className="sub">จบกระบวนการแล้ว</span>}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {booking && (
        <Modal
          title="จองนัดหมายใหม่"
          onClose={() => setBooking(null)}
          footer={<>
            <button onClick={() => setBooking(null)}>ยกเลิก</button>
            <button className="primary" disabled={!booking.startTime} onClick={submitBooking}>ยืนยันการจอง</button>
          </>}
        >
          <div className="form-grid">
            <Field label="ผู้ป่วย">
              <select value={booking.patientId} onChange={(e) => setBooking({ ...booking, patientId: e.target.value, startTime: '' })}>
                <option value="">— เลือกผู้ป่วย —</option>
                {patients.map((p) => <option key={p.id} value={p.id}>{p.hn} · {p.fullName}</option>)}
              </select>
            </Field>
            <Field label="แพทย์">
              <select value={booking.doctorId} onChange={(e) => setBooking({ ...booking, doctorId: e.target.value, startTime: '' })}>
                <option value="">— เลือกแพทย์ —</option>
                {doctors.map((d) => <option key={d.id} value={d.id}>{d.displayName} · {d.specialtyName}</option>)}
              </select>
            </Field>
            <Field label="วันที่">
              <input type="date" value={booking.date} onChange={(e) => setBooking({ ...booking, date: e.target.value, startTime: '' })} />
            </Field>
            <Field label="ประเภทการเข้ารับบริการ">
              <select value={booking.type} onChange={(e) => setBooking({ ...booking, type: e.target.value })}>
                {types.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </Field>
          </div>

          <Field label="ช่องเวลาที่ว่าง">
            {!booking.doctorId ? <p className="hint">เลือกแพทย์และวันที่ก่อน ระบบจะคำนวณช่องเวลาจากตารางออกตรวจให้อัตโนมัติ</p>
              : loadingSlots ? <p className="hint">กำลังคำนวณช่องเวลา…</p>
              : slots.length === 0 ? <p className="hint">วันดังกล่าวแพทย์ไม่ออกตรวจ หรือเกินช่วงที่เปิดให้จองล่วงหน้า</p> : (
              <div className="slots">
                {slots.map((s) => (
                  <button
                    key={s.startTime}
                    type="button"
                    title={s.unavailableReason || 'ว่าง'}
                    disabled={!s.available}
                    className={`slot ${booking.startTime === s.startTime ? 'selected' : ''}`}
                    onClick={() => setBooking({ ...booking, startTime: s.startTime })}
                  >
                    {String(s.startTime).slice(0, 5)}
                  </button>
                ))}
              </div>
            )}
          </Field>

          <Field label="อาการเบื้องต้น">
            <textarea rows="3" value={booking.symptomNote} onChange={(e) => setBooking({ ...booking, symptomNote: e.target.value })} />
          </Field>
        </Modal>
      )}

      {reschedule && (
        <Modal
          title="เลื่อนนัดหมาย"
          onClose={() => setReschedule(null)}
          footer={<>
            <button onClick={() => setReschedule(null)}>ยกเลิก</button>
            <button className="primary" onClick={submitReschedule}>บันทึกการเลื่อนนัด</button>
          </>}
        >
          <div className="form-grid">
            <Field label="วันที่ใหม่">
              <input type="date" value={reschedule.date} onChange={(e) => setReschedule({ ...reschedule, date: e.target.value })} />
            </Field>
            <Field label="เวลาใหม่">
              <input type="time" value={reschedule.startTime} onChange={(e) => setReschedule({ ...reschedule, startTime: e.target.value })} />
            </Field>
          </div>
          <Field label="เหตุผล">
            <input value={reschedule.reason} onChange={(e) => setReschedule({ ...reschedule, reason: e.target.value })} />
          </Field>
          <p className="hint">เวลาใหม่จะถูกตรวจด้วยกฎชุดเดียวกับการจองครั้งแรกทั้ง 7 ข้อ</p>
        </Modal>
      )}
    </>
  )
}
