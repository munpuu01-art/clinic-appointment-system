import React, { useEffect, useState } from 'react'
import { LookupApi } from '../api/ApiClient.js'
import { Card, Empty, Notice, Stat, today } from '../components/ui.jsx'

export default function Dashboard() {
  const [date, setDate] = useState(today())
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    LookupApi.dashboard(date).then(setData).catch(setError)
  }, [date])

  return (
    <>
      <div className="page-head">
        <div>
          <h1>ภาพรวมการให้บริการ</h1>
          <p>ติดตามนัดหมายและคิวของทั้งคลินิกในวันเดียวกัน</p>
        </div>
        <input type="date" value={date} onChange={(e) => setDate(e.target.value)} style={{ width: 180 }} />
      </div>

      <Notice error={error} onDismiss={() => setError(null)} />

      {!data ? <Empty>กำลังโหลดข้อมูล</Empty> : (
        <>
          <div className="grid cols-4">
            <Stat label="นัดหมายทั้งหมด" value={data.totalAppointments} />
            <Stat label="ยืนยันแล้ว" value={data.confirmed} />
            <Stat label="เช็คอินแล้ว" value={data.checkedIn} />
            <Stat label="รอเรียกคิว" value={data.waitingInQueue} tone="var(--amber)" />
            <Stat label="ตรวจเสร็จ" value={data.completed} tone="var(--pine)" />
            <Stat label="ยกเลิก" value={data.cancelled} />
            <Stat label="ไม่มาตามนัด" value={data.noShow} tone="var(--alert)" />
          </div>

          <Card title="ภาระงานรายแพทย์">
            {data.doctorLoads.length === 0 ? <Empty>ยังไม่มีแพทย์ออกตรวจในวันนี้</Empty> : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr><th>แพทย์</th><th>นัดหมาย</th><th>คิวที่รออยู่</th></tr>
                  </thead>
                  <tbody>
                    {data.doctorLoads.map((d) => (
                      <tr key={d.doctorId}>
                        <td>{d.doctorName}</td>
                        <td className="numeric">{d.appointmentCount}</td>
                        <td className="numeric">{d.waitingCount}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
        </>
      )}
    </>
  )
}
