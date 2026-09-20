import React, { useEffect, useState } from 'react'
import { PortalApi } from '../../api/ApiClient.js'
import { Card, Empty, Notice, thaiDate } from '../../components/ui.jsx'

export default function MyRecords() {
  const [records, setRecords] = useState([])
  const [error, setError] = useState(null)

  useEffect(() => { PortalApi.medicalRecords().then(setRecords).catch(setError) }, [])

  return (
    <>
      <div className="page-head">
        <div>
          <h1>ประวัติการรักษา</h1>
          <p>บันทึกจากแพทย์ในแต่ละครั้งที่เข้ารับการตรวจ</p>
        </div>
      </div>

      <Notice error={error} onDismiss={() => setError(null)} />

      {records.length === 0 ? (
        <Card><Empty>ยังไม่มีประวัติการรักษา</Empty></Card>
      ) : records.map((r) => (
        <Card key={r.id} title={`${r.appointmentNo} · ${r.doctorName}`}>
          <div className="record-grid">
            <div><span className="label">อาการสำคัญ</span><p>{r.chiefComplaint || '-'}</p></div>
            <div><span className="label">การวินิจฉัย</span><p>{r.diagnosis || '-'}</p></div>
            <div><span className="label">การรักษา</span><p>{r.treatment || '-'}</p></div>
            <div><span className="label">ยาที่ได้รับ</span><p>{r.prescription || '-'}</p></div>
          </div>
          <div className="row wrap vitals">
            {r.temperatureC != null && <span className="pill">อุณหภูมิ {r.temperatureC} °C</span>}
            {r.systolic != null && <span className="pill">ความดัน {r.systolic}/{r.diastolic}</span>}
            {r.bmi != null && <span className="pill">BMI {r.bmi}</span>}
            {r.followUpDate && <span className="pill amber">นัดติดตาม {thaiDate(r.followUpDate)}</span>}
          </div>
        </Card>
      ))}
    </>
  )
}
