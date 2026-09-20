-- ============================================================
-- Clinic Appointment System — PostgreSQL schema
-- ใช้ JOINED inheritance: person เป็นตารางแม่ของ patient / doctor / staff
-- ============================================================

DROP TABLE IF EXISTS user_account, payment, invoice_item, invoice, medical_record,
                     queue_ticket, appointment, doctor_leave, doctor_schedule,
                     staff, doctor, patient, person, specialty CASCADE;

-- ---------- แผนก ----------
CREATE TABLE specialty (
    id                    BIGSERIAL PRIMARY KEY,
    code                  VARCHAR(20)  NOT NULL UNIQUE,
    name                  VARCHAR(120) NOT NULL,
    description           VARCHAR(500),
    default_slot_minutes  INT          NOT NULL DEFAULT 20,
    base_fee              NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at            TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP
);

-- ---------- บุคคล (ตารางแม่) ----------
CREATE TABLE person (
    id           BIGSERIAL PRIMARY KEY,
    first_name   VARCHAR(80)  NOT NULL,
    last_name    VARCHAR(80)  NOT NULL,
    gender       VARCHAR(10)  NOT NULL,
    birth_date   DATE,
    national_id  VARCHAR(20)  UNIQUE,
    phone        VARCHAR(20),
    email        VARCHAR(120),
    address_line VARCHAR(200),
    district     VARCHAR(80),
    province     VARCHAR(80),
    postcode     VARCHAR(10),
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP
);

CREATE TABLE patient (
    id                BIGINT PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
    hn                VARCHAR(20) NOT NULL UNIQUE,
    blood_type        VARCHAR(5),
    allergies         VARCHAR(500),
    chronic_disease   VARCHAR(500),
    emergency_contact VARCHAR(200)
);

CREATE TABLE doctor (
    id               BIGINT PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
    license_no       VARCHAR(40) NOT NULL UNIQUE,
    specialty_id     BIGINT      NOT NULL REFERENCES specialty(id),
    consultation_fee NUMERIC(12,2) NOT NULL DEFAULT 0,
    room_no          VARCHAR(20),
    biography        VARCHAR(1000),
    active           BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE staff (
    id          BIGINT PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
    employee_no VARCHAR(30) NOT NULL UNIQUE,
    role        VARCHAR(30) NOT NULL,
    active      BOOLEAN     NOT NULL DEFAULT TRUE
);

-- ---------- ตารางออกตรวจ / วันลา ----------
CREATE TABLE doctor_schedule (
    id                BIGSERIAL PRIMARY KEY,
    doctor_id         BIGINT NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    day_of_week       VARCHAR(12) NOT NULL,
    start_time        TIME   NOT NULL,
    end_time          TIME   NOT NULL,
    slot_minutes      INT    NOT NULL DEFAULT 20,
    capacity_per_slot INT    NOT NULL DEFAULT 1,
    room_no           VARCHAR(20),
    effective_from    DATE,
    effective_to      DATE,
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    CONSTRAINT uk_schedule UNIQUE (doctor_id, day_of_week, start_time),
    CONSTRAINT ck_schedule_time CHECK (end_time > start_time),
    CONSTRAINT ck_schedule_slot CHECK (slot_minutes >= 5)
);

CREATE TABLE doctor_leave (
    id         BIGSERIAL PRIMARY KEY,
    doctor_id  BIGINT NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    leave_date DATE   NOT NULL,
    full_day   BOOLEAN NOT NULL DEFAULT TRUE,
    start_time TIME,
    end_time   TIME,
    reason     VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);
CREATE INDEX idx_leave_doctor_date ON doctor_leave(doctor_id, leave_date);

-- ---------- นัดหมาย ----------
CREATE TABLE appointment (
    id               BIGSERIAL PRIMARY KEY,
    appointment_no   VARCHAR(30) NOT NULL UNIQUE,
    patient_id       BIGINT NOT NULL REFERENCES patient(id),
    doctor_id        BIGINT NOT NULL REFERENCES doctor(id),
    appointment_date DATE   NOT NULL,
    start_time       TIME   NOT NULL,
    end_time         TIME   NOT NULL,
    type             VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    symptom_note     VARCHAR(1000),
    cancel_reason    VARCHAR(300),
    fee              NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_by       VARCHAR(80),
    checked_in_at    TIMESTAMP,
    started_at       TIMESTAMP,
    completed_at     TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP,
    CONSTRAINT ck_appt_time CHECK (end_time > start_time)
);
CREATE INDEX idx_appt_date_status  ON appointment(appointment_date, status);
CREATE INDEX idx_appt_doctor_date  ON appointment(doctor_id, appointment_date);
CREATE INDEX idx_appt_patient_date ON appointment(patient_id, appointment_date);

-- กันจองชนเฉพาะนัดที่ยังไม่ถูกยกเลิก (partial unique index)
CREATE UNIQUE INDEX uk_appt_slot
    ON appointment(doctor_id, appointment_date, start_time)
    WHERE status NOT IN ('CANCELLED', 'NO_SHOW');

-- ---------- บัตรคิว ----------
CREATE TABLE queue_ticket (
    id             BIGSERIAL PRIMARY KEY,
    ticket_no      VARCHAR(20) NOT NULL,
    queue_date     DATE   NOT NULL,
    doctor_id      BIGINT NOT NULL REFERENCES doctor(id),
    patient_id     BIGINT NOT NULL REFERENCES patient(id),
    appointment_id BIGINT REFERENCES appointment(id),
    priority       VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    sequence_no    INT    NOT NULL,
    issued_at      TIMESTAMP NOT NULL DEFAULT now(),
    called_at      TIMESTAMP,
    served_at      TIMESTAMP,
    completed_at   TIMESTAMP,
    counter_no     VARCHAR(20),
    symptom_note   VARCHAR(1000),
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP,
    CONSTRAINT uk_ticket_sequence UNIQUE (doctor_id, queue_date, sequence_no)
);
CREATE INDEX idx_ticket_board ON queue_ticket(doctor_id, queue_date, status);

-- ---------- เวชระเบียน ----------
CREATE TABLE medical_record (
    id             BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE REFERENCES appointment(id) ON DELETE CASCADE,
    patient_id     BIGINT NOT NULL REFERENCES patient(id),
    doctor_id      BIGINT NOT NULL REFERENCES doctor(id),
    symptoms       VARCHAR(1000),
    diagnosis      VARCHAR(1000),
    treatment      VARCHAR(1000),
    prescription   VARCHAR(1000),
    follow_up_date DATE,
    temperature    NUMERIC(5,2),
    systolic       INT,
    diastolic      INT,
    pulse          INT,
    weight_kg      NUMERIC(6,2),
    height_cm      NUMERIC(6,2),
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP
);

-- ---------- การเงิน ----------
CREATE TABLE invoice (
    id             BIGSERIAL PRIMARY KEY,
    invoice_no     VARCHAR(30) NOT NULL UNIQUE,
    appointment_id BIGINT REFERENCES appointment(id),
    patient_id     BIGINT NOT NULL REFERENCES patient(id),
    status         VARCHAR(20) NOT NULL,
    discount       NUMERIC(12,2) NOT NULL DEFAULT 0,
    issued_at      TIMESTAMP,
    paid_at        TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP,
    CONSTRAINT ck_discount CHECK (discount >= 0)
);

CREATE TABLE invoice_item (
    id          BIGSERIAL PRIMARY KEY,
    invoice_id  BIGINT NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    description VARCHAR(200) NOT NULL,
    quantity    INT NOT NULL DEFAULT 1,
    unit_price  NUMERIC(12,2) NOT NULL,
    CONSTRAINT ck_item_qty CHECK (quantity > 0)
);

CREATE TABLE payment (
    id           BIGSERIAL PRIMARY KEY,
    invoice_id   BIGINT NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    amount       NUMERIC(12,2) NOT NULL,
    method       VARCHAR(20) NOT NULL,
    paid_at      TIMESTAMP NOT NULL DEFAULT now(),
    reference_no VARCHAR(60),
    CONSTRAINT ck_payment_amount CHECK (amount > 0)
);

-- ---------- บัญชีผู้ใช้งาน ----------
CREATE TABLE user_account (
    id                   BIGSERIAL PRIMARY KEY,
    username             VARCHAR(60)  NOT NULL UNIQUE,
    password_hash        VARCHAR(120) NOT NULL,      -- BCrypt เท่านั้น ห้ามเก็บรหัสผ่านดิบ
    role                 VARCHAR(20)  NOT NULL,      -- ADMIN | STAFF | DOCTOR | PATIENT
    person_id            BIGINT REFERENCES person(id),
    display_name         VARCHAR(120),
    active               BOOLEAN   NOT NULL DEFAULT TRUE,
    failed_attempts      INT       NOT NULL DEFAULT 0,
    locked_until         TIMESTAMP,
    last_login_at        TIMESTAMP,
    must_change_password BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP,
    CONSTRAINT uk_account_person UNIQUE (person_id)
);
CREATE INDEX idx_account_role ON user_account(role);

-- ---------- ข้อมูลตั้งต้น ----------
INSERT INTO specialty (code, name, default_slot_minutes, base_fee) VALUES
  ('INT', 'อายุรกรรม',  20, 500),
  ('DEN', 'ทันตกรรม',   30, 800),
  ('PED', 'กุมารเวชกรรม', 20, 600);
