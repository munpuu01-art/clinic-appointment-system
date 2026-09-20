/**
 * ชั้นเรียกใช้ REST API ฝั่ง frontend เขียนแบบ OOP
 * - ApiClient: จัดการ fetch/JSON/ข้อผิดพลาดที่เดียว
 * - Resource แต่ละกลุ่ม (patients, doctors, ...) สืบทอดพฤติกรรมร่วมจาก ApiClient
 */
export class ApiError extends Error {
  constructor(payload, status) {
    super(payload?.message || 'เกิดข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์')
    this.name = 'ApiError'
    this.code = payload?.code
    this.status = status
    this.details = payload?.details || []
  }
}

/**
 * เก็บ token ไว้ที่เดียว — คลาสอื่นไม่ต้องรู้ว่าเก็บที่ไหน (Encapsulation)
 */
export class TokenStore {
  static KEY = 'clinic.auth'

  static save(session) {
    localStorage.setItem(TokenStore.KEY, JSON.stringify(session))
  }

  static load() {
    try {
      const raw = localStorage.getItem(TokenStore.KEY)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  }

  static token() { return TokenStore.load()?.token || null }

  static clear() { localStorage.removeItem(TokenStore.KEY) }
}

export class ApiClient {
  // ตอน deploy จริง (Vercel) ตั้งค่า VITE_API_URL ชี้ไปที่ backend บน Render เช่น
  // https://clinic-backend-xxxx.onrender.com/api — ถ้าไม่ตั้งจะใช้ '/api' (proxy ของ Vite ตอนรันในเครื่อง)
  constructor(baseUrl = import.meta.env.VITE_API_URL || '/api') {
    this.baseUrl = baseUrl
    this.onUnauthorized = null
  }

  async request(path, { method = 'GET', body, query } = {}) {
    const url = new URL(this.baseUrl + path, window.location.origin)
    if (query) {
      Object.entries(query)
        .filter(([, v]) => v !== undefined && v !== null && v !== '')
        .forEach(([k, v]) => url.searchParams.set(k, v))
    }

    // ข้าม "หน้าเตือน" ของ ngrok free plan ที่ปกติจะคั่นก่อนถึง API จริง
    const headers = { 'Content-Type': 'application/json', 'ngrok-skip-browser-warning': 'true' }
    const token = TokenStore.token()
    if (token) headers.Authorization = `Bearer ${token}`

    const response = await fetch(url, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined
    })

    // token หมดอายุหรือถูกเพิกถอน → บังคับออกจากระบบ
    if (response.status === 401 && this.onUnauthorized) {
      this.onUnauthorized()
    }

    if (response.status === 204) return null

    const payload = await response.json().catch(() => null)
    if (!response.ok) throw new ApiError(payload, response.status)
    return payload
  }

  get(path, query) { return this.request(path, { query }) }
  post(path, body, query) { return this.request(path, { method: 'POST', body, query }) }
  put(path, body) { return this.request(path, { method: 'PUT', body }) }
  patch(path, body) { return this.request(path, { method: 'PATCH', body }) }
  delete(path) { return this.request(path, { method: 'DELETE' }) }
}

const api = new ApiClient()

export const PatientApi = {
  search: (q, page = 0) => api.get('/patients', { q, page, size: 20 }),
  byId: (id) => api.get(`/patients/${id}`),
  register: (data) => api.post('/patients', data),
  update: (id, data) => api.put(`/patients/${id}`, data),
  appointments: (id) => api.get(`/patients/${id}/appointments`)
}

export const DoctorApi = {
  list: (specialtyId) => api.get('/doctors', { specialtyId }),
  byId: (id) => api.get(`/doctors/${id}`),
  create: (data) => api.post('/doctors', data),
  addSchedule: (id, data) => api.post(`/doctors/${id}/schedules`, data),
  removeSchedule: (id, scheduleId) => api.delete(`/doctors/${id}/schedules/${scheduleId}`),
  addLeave: (id, date, reason) => api.post(`/doctors/${id}/leaves`, { date, reason }),
  slots: (id, date, patientId) => api.get(`/doctors/${id}/slots`, { date, patientId })
}

export const AppointmentApi = {
  byDate: (date) => api.get('/appointments', { date }),
  byId: (id) => api.get(`/appointments/${id}`),
  book: (data) => api.post('/appointments', data),
  confirm: (id) => api.patch(`/appointments/${id}/confirm`),
  cancel: (id, reason) => api.patch(`/appointments/${id}/cancel`, { reason }),
  reschedule: (id, data) => api.patch(`/appointments/${id}/reschedule`, data),
  checkIn: (id) => api.patch(`/appointments/${id}/check-in`),
  noShow: (id) => api.patch(`/appointments/${id}/no-show`),
  saveRecord: (id, data) => api.put(`/appointments/${id}/medical-record`, data)
}

export const QueueApi = {
  board: (doctorId, date, strategy) => api.get('/queues/board', { doctorId, date, strategy }),
  walkIn: (data) => api.post('/queues/walk-in', data),
  callNext: (doctorId, counterNo) => api.post('/queues/call-next', { counterNo }, { doctorId }),
  recall: (ticketId) => api.patch(`/queues/tickets/${ticketId}/recall`),
  serve: (ticketId) => api.patch(`/queues/tickets/${ticketId}/serve`),
  complete: (ticketId) => api.patch(`/queues/tickets/${ticketId}/complete`),
  skip: (ticketId) => api.patch(`/queues/tickets/${ticketId}/skip`),
  strategies: () => api.get('/queues/strategies')
}

export const AuthApi = {
  login: (username, password) => api.post('/auth/login', { username, password }),
  register: (username, password, patient) => api.post('/auth/register', { username, password, patient }),
  me: () => api.get('/auth/me'),
  changePassword: (currentPassword, newPassword) =>
    api.post('/auth/change-password', { currentPassword, newPassword })
}

/** พอร์ทัลผู้ป่วย — ทุกเส้นทางอิงผู้ป่วยที่ล็อกอินอยู่ ไม่ต้องส่ง patientId */
export const PortalApi = {
  profile: () => api.get('/portal/profile'),
  appointments: () => api.get('/portal/appointments'),
  doctors: (specialtyId) => api.get('/portal/doctors', { specialtyId }),
  specialties: () => api.get('/portal/specialties'),
  slots: (doctorId, date) => api.get(`/portal/doctors/${doctorId}/slots`, { date }),
  book: (data) => api.post('/portal/appointments', data),
  cancel: (id, reason) => api.patch(`/portal/appointments/${id}/cancel`, { reason }),
  reschedule: (id, data) => api.patch(`/portal/appointments/${id}/reschedule`, data),
  medicalRecords: () => api.get('/portal/medical-records')
}

export const AccountApi = {
  list: () => api.get('/admin/accounts'),
  create: (data) => api.post('/admin/accounts', data),
  activate: (id) => api.patch(`/admin/accounts/${id}/activate`),
  deactivate: (id) => api.patch(`/admin/accounts/${id}/deactivate`),
  resetPassword: (id, newPassword) => api.patch(`/admin/accounts/${id}/reset-password`, { newPassword })
}

export const LookupApi = {
  specialties: () => api.get('/specialties'),
  appointmentTypes: () => api.get('/appointment-types'),
  queuePriorities: () => api.get('/queue-priorities'),
  dashboard: (date) => api.get('/dashboard', { date })
}

export default api
