import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { PortalApi } from '../../api/ApiClient.js'
import { Card, Empty, Notice, Pill, thaiDate, today } from '../../components/ui.jsx'

export default function MyAppointments() {
  const [profile, setProfile] = useState(null)
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)

  const load = () => PortalApi.appointments().then(setRows).catch(setError)

  useEffect(() => {
    PortalApi.profile().then(setProfile).catch(setError)
    load()
  }, [])

  const cancel = async (row) => {
    const reason = window.prompt('เหตุผลในการยกเลิกนัด')
    if (!reason) return
    try {
      await PortalApi.cancel(row.id, reason)
      setMessage('ยกเลิกนัดเรียบร้อย')
      load()
    } catch (err) { setError(err) }
  }

  const upcoming = rows.filter((r) => r.date >= today() && !['CANCELLED', 'COMPLETED', 'NO_SHOW'].includes(r.status))
  const history = rows.filter((r) => !upcoming.includes(r))

  return (
    <>
      <div className="page-head">
        <div>
          <h1>นัดหมายของฉัน</h1>
          <p>{profile ? `${profile.fullName} · เลขประจำตัวผู้ป่วย ${profile.hn}` : 'กำลังโหลดข้อมูล…'}</p>
        </div>
        <Link className="btn primary" to="/portal/book">จองนัดใหม่</Link>
      </div>

      <Notice error={error} message={message} onDismiss={() => { setError(null); setMessage(null) }} />

      <Card title={`นัดที่กำลังจะถึง (${upcoming.length})`}>
        {upcoming.length === 0 ? <Empty>ยังไม่มีนัดหมายที่กำลังจะถึง</Empty> : (
          <div className="appt-cards">
            {upcoming.map((r) => (
              <article key={r.id} className="appt-card">
                <div className="appt-date">
                  <span className="d">{thaiDate(r.date)}</span>
                  <span className="t numeric">{String(r.startTime).slice(0, 5)}</span>
                </div>
                <div className="appt-body">
                  <strong>{r.doctorName}</strong>
                  <span className="sub">{r.specialtyName} · {r.typeLabel}</span>
                  <span className="sub">เลขที่นัด {r.appointmentNo}{r.ticketNo ? ` · บัตรคิว ${r.ticketNo}` : ''}</span>
                </div>
                <div className="appt-actions">
                  <Pill status={r.status} label={r.statusLabel} />
                  {(r.allowedTransitions || []).includes('CANCELLED') && (
                    <button className="ghost danger" onClick={() => cancel(r)}>ยกเลิกนัด</button>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}
      </Card>

      <Card title={`ประวัตินัดหมาย (${history.length})`}>
        {history.length === 0 ? <Empty>ยังไม่มีประวัติ</Empty> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>วันที่</th><th>เวลา</th><th>แพทย์</th><th>ประเภท</th><th>สถานะ</th></tr>
              </thead>
              <tbody>
                {history.map((r) => (
                  <tr key={r.id}>
                    <td className="numeric">{thaiDate(r.date)}</td>
                    <td className="numeric">{String(r.startTime).slice(0, 5)}</td>
                    <td>{r.doctorName}<span className="sub"> {r.specialtyName}</span></td>
                    <td>{r.typeLabel}</td>
                    <td><Pill status={r.status} label={r.statusLabel} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </>
  )
}
