import React, { useEffect, useState } from 'react'
import { DoctorApi, LookupApi, PatientApi, QueueApi } from '../api/ApiClient.js'
import { Card, Empty, Field, Modal, Notice, Pill, today, thaiDate } from '../components/ui.jsx'

const STRATEGY_LABEL = {
  FIFO: 'มาก่อนได้ก่อน (FIFO)',
  PRIORITY: 'ตามลำดับความสำคัญ',
  APPOINTMENT_TIME: 'ตามเวลานัดหมาย'
}

export default function QueueBoard() {
  const [doctors, setDoctors] = useState([])
  const [doctorId, setDoctorId] = useState('')
  const [date, setDate] = useState(today())
  const [strategy, setStrategy] = useState('PRIORITY')
  const [strategies, setStrategies] = useState([])
  const [board, setBoard] = useState(null)
  const [priorities, setPriorities] = useState([])
  const [patients, setPatients] = useState([])
  const [walkIn, setWalkIn] = useState(null)
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)

  useEffect(() => {
    DoctorApi.list()
      .then((list) => { setDoctors(list); setDoctorId((c) => c || list[0]?.id || '') })
      .catch(setError)
    QueueApi.strategies().then(setStrategies).catch(setError)
    LookupApi.queuePriorities().then(setPriorities).catch(setError)
    PatientApi.search('').then((page) => setPatients(page.content || [])).catch(setError)
  }, [])

  const load = () => {
    if (!doctorId) return
    QueueApi.board(doctorId, date, strategy).then(setBoard).catch(setError)
  }

  // รีเฟรชจอคิวอัตโนมัติทุก 15 วินาที เหมือนจอหน้าห้องตรวจจริง
  useEffect(() => {
    load()
    if (!doctorId) return undefined
    const timer = setInterval(load, 15000)
    return () => clearInterval(timer)
  }, [doctorId, date, strategy])

  const act = async (fn, successText) => {
    try {
      await fn()
      setMessage(successText)
      load()
    } catch (err) { setError(err) }
  }

  const submitWalkIn = async () => {
    try {
      const ticket = await QueueApi.walkIn({
        ...walkIn,
        patientId: Number(walkIn.patientId),
        doctorId: Number(doctorId)
      })
      setMessage(`ออกบัตรคิว ${ticket.ticketNo} ให้ ${ticket.patientName} แล้ว`)
      setWalkIn(null)
      load()
    } catch (err) { setError(err) }
  }

  const nowServing = board?.nowServing

  return (
    <>
      <div className="page-head">
        <div>
          <h1>คิวหน้าห้องตรวจ</h1>
          <p>ลำดับการเรียกคิวเปลี่ยนได้ทันทีด้วย Strategy Pattern โดยไม่แก้โค้ดส่วนอื่น</p>
        </div>
        <div className="row">
          <select value={doctorId} onChange={(e) => setDoctorId(e.target.value)}>
            {doctors.map((d) => <option key={d.id} value={d.id}>{d.displayName}</option>)}
          </select>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          <select value={strategy} onChange={(e) => setStrategy(e.target.value)}>
            {(strategies.length ? strategies : ['FIFO', 'PRIORITY', 'APPOINTMENT_TIME']).map((s) => (
              <option key={s} value={s}>{STRATEGY_LABEL[s] || s}</option>
            ))}
          </select>
          <button className="primary" onClick={() => setWalkIn({ patientId: '', priority: 'NORMAL', symptomNote: '', createdBy: 'เจ้าหน้าที่คัดกรอง' })}>
            ลงทะเบียน Walk-in
          </button>
        </div>
      </div>

      <Notice error={error} message={message} onDismiss={() => { setError(null); setMessage(null) }} />

      <section className="hero">
        <div>
          <div className="hero-label">กำลังเรียก · ห้อง {board?.roomNo || '-'}</div>
          <div className="hero-number">{nowServing ? nowServing.ticketNo : '—'}</div>
          <div className="hero-sub">
            {nowServing
              ? `${nowServing.patientName} (${nowServing.hn}) · ${nowServing.statusLabel}`
              : 'ยังไม่มีคิวที่กำลังเรียก'}
          </div>
        </div>
        <div className="hero-side">
          <div><span>รอคิว</span><strong>{board?.waitingCount ?? 0}</strong></div>
          <div><span>ตรวจเสร็จ</span><strong>{board?.doneCount ?? 0}</strong></div>
          <div><span>รอโดยประมาณ</span><strong>{board?.estimatedWaitMinutes ?? 0} นาที</strong></div>
        </div>
      </section>

      <div className="row wrap gap">
        <button className="primary" onClick={() => act(() => QueueApi.callNext(doctorId, board?.roomNo || '1'), 'เรียกคิวถัดไปแล้ว')}>
          เรียกคิวถัดไป
        </button>
        {nowServing && (
          <>
            <button onClick={() => act(() => QueueApi.recall(nowServing.id), 'เรียกซ้ำแล้ว')}>เรียกซ้ำ</button>
            <button onClick={() => act(() => QueueApi.serve(nowServing.id), 'เริ่มตรวจแล้ว')}>เริ่มตรวจ</button>
            <button onClick={() => act(() => QueueApi.complete(nowServing.id), 'ปิดคิวเรียบร้อย')}>ตรวจเสร็จ</button>
            <button className="ghost danger" onClick={() => act(() => QueueApi.skip(nowServing.id), 'ข้ามคิวแล้ว')}>ข้ามคิว</button>
          </>
        )}
      </div>

      <Card title={`คิวที่รออยู่ · ${thaiDate(date)} · ${STRATEGY_LABEL[board?.strategy] || board?.strategy || ''}`}>
        {!board || board.waiting.length === 0 ? <Empty>ไม่มีคิวที่รออยู่</Empty> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ลำดับ</th><th>บัตรคิว</th><th>ผู้ป่วย</th><th>ความสำคัญ</th>
                  <th>สถานะ</th><th>รอมาแล้ว</th><th>นัดหมาย</th><th></th>
                </tr>
              </thead>
              <tbody>
                {board.waiting.map((t, index) => (
                  <tr key={t.id}>
                    <td className="numeric">{index + 1}</td>
                    <td className="numeric strong">{t.ticketNo}</td>
                    <td>{t.patientName}<span className="sub"> {t.hn}</span></td>
                    <td><Pill status={t.priority === 'EMERGENCY' ? 'CANCELLED' : 'CALLED'} label={t.priorityLabel} /></td>
                    <td><Pill status={t.status} label={t.statusLabel} /></td>
                    <td className="numeric">{t.waitingMinutes} นาที</td>
                    <td className="numeric">{t.appointmentId ? 'มีนัด' : 'Walk-in'}</td>
                    <td>
                      <div className="row wrap">
                        {t.status === 'SKIPPED'
                          ? <button className="ghost" onClick={() => act(() => QueueApi.requeue(t.id), 'นำคิวกลับเข้าแถวแล้ว')}>เข้าคิวใหม่</button>
                          : <button className="ghost danger" onClick={() => act(() => QueueApi.skip(t.id), 'ข้ามคิวแล้ว')}>ข้าม</button>}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {walkIn && (
        <Modal
          title="ลงทะเบียนผู้ป่วย Walk-in"
          onClose={() => setWalkIn(null)}
          footer={<>
            <button onClick={() => setWalkIn(null)}>ยกเลิก</button>
            <button className="primary" disabled={!walkIn.patientId} onClick={submitWalkIn}>ออกบัตรคิว</button>
          </>}
        >
          <Field label="ผู้ป่วย">
            <select value={walkIn.patientId} onChange={(e) => setWalkIn({ ...walkIn, patientId: e.target.value })}>
              <option value="">— เลือกผู้ป่วย —</option>
              {patients.map((p) => <option key={p.id} value={p.id}>{p.hn} · {p.fullName}</option>)}
            </select>
          </Field>
          <Field label="ระดับความสำคัญ (จากการคัดกรอง)">
            <select value={walkIn.priority} onChange={(e) => setWalkIn({ ...walkIn, priority: e.target.value })}>
              {priorities.map((p) => <option key={p.value} value={p.value}>{p.label}</option>)}
            </select>
          </Field>
          <Field label="อาการเบื้องต้น">
            <textarea rows="3" value={walkIn.symptomNote} onChange={(e) => setWalkIn({ ...walkIn, symptomNote: e.target.value })} />
          </Field>
          <p className="hint">คิวฉุกเฉินจะถูกจัดขึ้นหน้าสุดเสมอเมื่อใช้กลยุทธ์ “ตามลำดับความสำคัญ”</p>
        </Modal>
      )}
    </>
  )
}
