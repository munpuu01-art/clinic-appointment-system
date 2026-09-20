import React from 'react'

export function Card({ title, actions, children }) {
  return (
    <section className="card">
      {(title || actions) && (
        <div className="card-title">
          <h2>{title}</h2>
          <div className="row">{actions}</div>
        </div>
      )}
      {children}
    </section>
  )
}

export function Stat({ label, value, tone }) {
  return (
    <div className="stat">
      <div className="label">{label}</div>
      <div className="value" style={tone ? { color: tone } : undefined}>{value}</div>
    </div>
  )
}

const STATUS_TONE = {
  REQUESTED: '', CONFIRMED: 'pine', CHECKED_IN: 'amber', IN_PROGRESS: 'amber',
  COMPLETED: 'pine', CANCELLED: 'alert', NO_SHOW: 'alert',
  WAITING: '', CALLED: 'amber', SERVING: 'amber', DONE: 'pine', SKIPPED: 'alert'
}

export function Pill({ status, label }) {
  return <span className={`pill ${STATUS_TONE[status] || ''}`}>{label || status}</span>
}

export function Field({ label, children }) {
  return (
    <div className="field">
      <label>{label}</label>
      {children}
    </div>
  )
}

export function Modal({ title, onClose, children, footer }) {
  return (
    <div className="backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={title}>
        <h2>{title}</h2>
        {children}
        <div className="modal-actions">{footer}</div>
      </div>
    </div>
  )
}

export function Notice({ error, message, onDismiss }) {
  if (!error && !message) return null
  return (
    <div className={`notice ${error ? 'error' : 'ok'}`} onClick={onDismiss}>
      {error ? error.message : message}
    </div>
  )
}

export function Empty({ children }) {
  return <div className="empty">{children}</div>
}

export function today() {
  return new Date().toISOString().slice(0, 10)
}

export function thaiDate(iso) {
  if (!iso) return '-'
  const [y, m, d] = iso.split('-')
  return `${d}/${m}/${y}`
}
