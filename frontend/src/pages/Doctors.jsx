import React, { useEffect, useState } from 'react'
import { DoctorApi, LookupApi } from '../api/ApiClient.js'
import { Card, Empty, Field, Modal, Notice, Pill, today, thaiDate } from '../components/ui.jsx'

const DAYS = [
  { value: 'MONDAY', label: 'จันทร์' },
  { value: 'TUESDAY', label: 'อังคาร' },
  { value: 'WEDNESDAY', label: 'พุธ' },
  { value: 'THURSDAY', label: 'พฤหัสบดี' },
  { value: 'FRIDAY', label: 'ศุกร์' },
  { value: 'SATURDAY', label: 'เสาร์' },
  { value: 'SUNDAY', label: 'อาทิตย์' }
]

const dayLabel = (value) => DAYS.find((d) => d.value === value)?.label || value

const EMPTY_DOCTOR = {
  licenseNo: '', specialtyId: '', firstName: '', lastName: '', gender: 'MALE',
  birthDate: '', nationalId: '', phone: '', email: '',
  consultationFee: '500', roomNo: '', biography: ''
}

const EMPTY_SCHEDULE = {
  dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '12:00',
  slotMinutes: 20, capacityPerSlot: 1, roomNo: '', effectiveFrom: '', effectiveTo: ''
}

export default function Doctors() {
  const [doctors, setDoctors] = useState([])
  const [specialties, setSpecialties] = useState([])
  const [selectedId, setSelectedId] = useState(null)
  const [doctorForm, setDoctorForm] = useState(null)
  const [scheduleForm, setScheduleForm] = useState(null)
  const [leaveForm, setLeaveForm] = useState(null)
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)

  const load = () => DoctorApi.list()
    .then((list) => {
      setDoctors(list)
      setSelectedId((current) => current ?? list[0]?.id ?? null)
    })
    .catch(setError)

  useEffect(() => {
    load()
    LookupApi.specialties().then(setSpecialties).catch(setError)
  }, [])

  const selected = doctors.find((d) => d.id === selectedId) || null

  const setDoctorField = (key) => (e) => setDoctorForm({ ...doctorForm, [key]: e.target.value })
  const setScheduleField = (key) => (e) => setScheduleForm({ ...scheduleForm, [key]: e.target.value })

  const saveDoctor = async () => {
    try {
      const saved = await DoctorApi.create({
        ...doctorForm,
        specialtyId: Number(doctorForm.specialtyId),
        consultationFee: doctorForm.consultationFee ? Number(doctorForm.consultationFee) : null,
        birthDate: doctorForm.birthDate || null
      })
      setMessage(`เพิ่มแพทย์ ${saved.displayName} เรียบร้อย`)
      setDoctorForm(null)
      setSelectedId(saved.id)
      load()
    } catch (err) { setError(err) }
  }

  const saveSchedule = async () => {
    try {
      await DoctorApi.addSchedule(selectedId, {
        ...scheduleForm,
        slotMinutes: Number(scheduleForm.slotMinutes),
        capacityPerSlot: Number(scheduleForm.capacityPerSlot),
        effectiveFrom: scheduleForm.effectiveFrom || null,
        effectiveTo: scheduleForm.effectiveTo || null
      })
      setMessage('เพิ่มตารางออกตรวจเรียบร้อย')
      setScheduleForm(null)
      load()
    } catch (err) { setError(err) }
  }

  const removeSchedule = async (scheduleId) => {
    try {
      await DoctorApi.removeSchedule(selectedId, scheduleId)
      setMessage('ลบตารางออกตรวจแล้ว')
      load()
    } catch (err) { setError(err) }
  }

  const saveLeave = async () => {
    try {
      await DoctorApi.addLeave(selectedId, leaveForm.date, leaveForm.reason)
      setMessage(`บันทึกวันลาวันที่ ${thaiDate(leaveForm.date)} แล้ว`)
      setLeaveForm(null)
    } catch (err) { setError(err) }
  }

  return (
    <>
      <div className="page-head">
        <div>
          <h1>แพทย์และตารางออกตรวจ</h1>
          <p>ตารางออกตรวจคือแหล่งที่มาของช่องเวลาว่างทั้งหมดในระบบจองนัด</p>
        </div>
        <button className="primary" onClick={() => setDoctorForm(EMPTY_DOCTOR)}>เพิ่มแพทย์</button>
      </div>

      <Notice error={error} message={message} onDismiss={() => { setError(null); setMessage(null) }} />

      <div className="split">
        <Card title={`แพทย์ทั้งหมด ${doctors.length} คน`}>
          {doctors.length === 0 ? <Empty>ยังไม่มีข้อมูลแพทย์</Empty> : (
            <ul className="list-pick">
              {doctors.map((d) => (
                <li key={d.id}>
                  <button
                    className={d.id === selectedId ? 'pick active' : 'pick'}
                    onClick={() => setSelectedId(d.id)}
                  >
                    <span className="pick-main">{d.displayName}</span>
                    <span className="pick-sub">
                      {d.specialtyName} · ห้อง {d.roomNo || '-'} · {Number(d.consultationFee || 0).toLocaleString()} บาท
                    </span>
                    {!d.active && <Pill status="CANCELLED" label="ปิดใช้งาน" />}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card
          title={selected ? `ตารางออกตรวจ · ${selected.displayName}` : 'ตารางออกตรวจ'}
          actions={selected && <>
            <button onClick={() => setLeaveForm({ date: today(), reason: '' })}>ลงวันลา</button>
            <button className="primary" onClick={() => setScheduleForm(EMPTY_SCHEDULE)}>เพิ่มช่วงเวลา</button>
          </>}
        >
          {!selected ? <Empty>เลือกแพทย์จากรายการด้านซ้าย</Empty>
            : selected.schedules.length === 0 ? <Empty>แพทย์ท่านนี้ยังไม่มีตารางออกตรวจ จึงยังจองนัดไม่ได้</Empty> : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>วัน</th><th>เวลา</th><th>ช่องละ</th><th>รับ/ช่อง</th>
                    <th>ความจุ</th><th>ห้อง</th><th>มีผลถึง</th><th></th>
                  </tr>
                </thead>
                <tbody>
                  {selected.schedules.map((s) => (
                    <tr key={s.id}>
                      <td>{dayLabel(s.dayOfWeek)}</td>
                      <td className="numeric">{s.startTime} – {s.endTime}</td>
                      <td className="numeric">{s.slotMinutes} นาที</td>
                      <td className="numeric">{s.capacityPerSlot}</td>
                      <td className="numeric">{s.totalCapacity} คิว</td>
                      <td>{s.roomNo || selected.roomNo || '-'}</td>
                      <td>{s.effectiveTo ? thaiDate(s.effectiveTo) : 'ไม่กำหนด'}</td>
                      <td>
                        <button className="ghost danger" onClick={() => removeSchedule(s.id)}>ลบ</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>
      </div>

      {doctorForm && (
        <Modal
          title="เพิ่มแพทย์ใหม่"
          onClose={() => setDoctorForm(null)}
          footer={<>
            <button onClick={() => setDoctorForm(null)}>ยกเลิก</button>
            <button className="primary" onClick={saveDoctor}>บันทึก</button>
          </>}
        >
          <div className="form-grid">
            <Field label="เลขใบประกอบวิชาชีพ">
              <input value={doctorForm.licenseNo} onChange={setDoctorField('licenseNo')} />
            </Field>
            <Field label="แผนก">
              <select value={doctorForm.specialtyId} onChange={setDoctorField('specialtyId')}>
                <option value="">— เลือกแผนก —</option>
                {specialties.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </Field>
            <Field label="ชื่อ"><input value={doctorForm.firstName} onChange={setDoctorField('firstName')} /></Field>
            <Field label="นามสกุล"><input value={doctorForm.lastName} onChange={setDoctorField('lastName')} /></Field>
            <Field label="เพศ">
              <select value={doctorForm.gender} onChange={setDoctorField('gender')}>
                <option value="MALE">ชาย</option>
                <option value="FEMALE">หญิง</option>
                <option value="OTHER">อื่น ๆ</option>
              </select>
            </Field>
            <Field label="วันเกิด"><input type="date" value={doctorForm.birthDate} onChange={setDoctorField('birthDate')} /></Field>
            <Field label="โทรศัพท์"><input value={doctorForm.phone} onChange={setDoctorField('phone')} /></Field>
            <Field label="อีเมล"><input value={doctorForm.email} onChange={setDoctorField('email')} /></Field>
            <Field label="ค่าตรวจ (บาท)">
              <input type="number" value={doctorForm.consultationFee} onChange={setDoctorField('consultationFee')} />
            </Field>
            <Field label="ห้องตรวจ"><input value={doctorForm.roomNo} onChange={setDoctorField('roomNo')} /></Field>
          </div>
          <Field label="ประวัติโดยย่อ">
            <textarea rows="3" value={doctorForm.biography} onChange={setDoctorField('biography')} />
          </Field>
        </Modal>
      )}

      {scheduleForm && (
        <Modal
          title={`เพิ่มตารางออกตรวจ · ${selected?.displayName || ''}`}
          onClose={() => setScheduleForm(null)}
          footer={<>
            <button onClick={() => setScheduleForm(null)}>ยกเลิก</button>
            <button className="primary" onClick={saveSchedule}>บันทึกตาราง</button>
          </>}
        >
          <div className="form-grid">
            <Field label="วันในสัปดาห์">
              <select value={scheduleForm.dayOfWeek} onChange={setScheduleField('dayOfWeek')}>
                {DAYS.map((d) => <option key={d.value} value={d.value}>{d.label}</option>)}
              </select>
            </Field>
            <Field label="ความยาวช่องเวลา (นาที)">
              <input type="number" min="5" step="5" value={scheduleForm.slotMinutes} onChange={setScheduleField('slotMinutes')} />
            </Field>
            <Field label="เวลาเริ่ม"><input type="time" value={scheduleForm.startTime} onChange={setScheduleField('startTime')} /></Field>
            <Field label="เวลาสิ้นสุด"><input type="time" value={scheduleForm.endTime} onChange={setScheduleField('endTime')} /></Field>
            <Field label="รับได้กี่คนต่อช่อง">
              <input type="number" min="1" value={scheduleForm.capacityPerSlot} onChange={setScheduleField('capacityPerSlot')} />
            </Field>
            <Field label="ห้องตรวจ"><input value={scheduleForm.roomNo} onChange={setScheduleField('roomNo')} /></Field>
            <Field label="เริ่มใช้วันที่"><input type="date" value={scheduleForm.effectiveFrom} onChange={setScheduleField('effectiveFrom')} /></Field>
            <Field label="ใช้ถึงวันที่"><input type="date" value={scheduleForm.effectiveTo} onChange={setScheduleField('effectiveTo')} /></Field>
          </div>
        </Modal>
      )}

      {leaveForm && (
        <Modal
          title={`ลงวันลา · ${selected?.displayName || ''}`}
          onClose={() => setLeaveForm(null)}
          footer={<>
            <button onClick={() => setLeaveForm(null)}>ยกเลิก</button>
            <button className="primary" onClick={saveLeave}>บันทึกวันลา</button>
          </>}
        >
          <Field label="วันที่ลา">
            <input type="date" value={leaveForm.date} onChange={(e) => setLeaveForm({ ...leaveForm, date: e.target.value })} />
          </Field>
          <Field label="เหตุผล">
            <input value={leaveForm.reason} onChange={(e) => setLeaveForm({ ...leaveForm, reason: e.target.value })} />
          </Field>
          <p className="hint">เมื่อบันทึกวันลาแล้ว ช่องเวลาของวันนั้นจะถูกปิดโดยกฎ DoctorAvailabilityRule ทันที</p>
        </Modal>
      )}
    </>
  )
}
