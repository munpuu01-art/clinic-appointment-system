package com.clinic.config;

import com.clinic.domain.common.ContactInfo;
import com.clinic.domain.common.Gender;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.doctor.DoctorSchedule;
import com.clinic.domain.doctor.Specialty;
import com.clinic.domain.person.Patient;
import com.clinic.domain.person.Staff;
import com.clinic.domain.person.StaffRole;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.repository.SpecialtyRepository;
import com.clinic.repository.StaffRepository;
import com.clinic.repository.UserAccountRepository;
import com.clinic.domain.auth.Role;
import com.clinic.domain.auth.UserAccount;
import com.clinic.domain.person.Person;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/** ข้อมูลตัวอย่างสำหรับ dev/demo (โปรไฟล์ dev เท่านั้น) */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final UserAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClinicProperties properties;

    public DataSeeder(SpecialtyRepository specialtyRepository, DoctorRepository doctorRepository,
                      PatientRepository patientRepository, StaffRepository staffRepository,
                      UserAccountRepository accountRepository, PasswordEncoder passwordEncoder,
                      ClinicProperties properties) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.specialtyRepository = specialtyRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    public void run(String... args) {
        if (specialtyRepository.count() > 0) return;

        Specialty internal = specialtyRepository.save(
                new Specialty("INT", "อายุรกรรม", 20, BigDecimal.valueOf(600)));
        Specialty dental = specialtyRepository.save(
                new Specialty("DEN", "ทันตกรรม", 30, BigDecimal.valueOf(800)));
        Specialty pediatric = specialtyRepository.save(
                new Specialty("PED", "กุมารเวชกรรม", 20, BigDecimal.valueOf(700)));

        Doctor somchai = new Doctor("ว.12345", internal, "สมชาย", "ใจดี", Gender.MALE,
                LocalDate.of(1980, 4, 12), "1100100100101",
                new ContactInfo("0812345678", "somchai@clinic.test", null));
        somchai.setRoomNo("A1");
        somchai.addSchedule(new DoctorSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), 20, "A1"));
        somchai.addSchedule(new DoctorSchedule(DayOfWeek.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(16, 0), 20, "A1"));
        somchai.addSchedule(new DoctorSchedule(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), 20, "A1"));

        Doctor malee = new Doctor("ว.22334", dental, "มาลี", "ฟันสวย", Gender.FEMALE,
                LocalDate.of(1985, 8, 3), "1100100100102",
                new ContactInfo("0823456789", "malee@clinic.test", null));
        malee.setRoomNo("B2");
        malee.addSchedule(new DoctorSchedule(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), 30, "B2"));
        malee.addSchedule(new DoctorSchedule(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), 30, "B2"));

        Doctor anan = new Doctor("ว.33445", pediatric, "อนันต์", "รักเด็ก", Gender.MALE,
                LocalDate.of(1978, 1, 20), "1100100100103",
                new ContactInfo("0834567890", "anan@clinic.test", null));
        anan.setRoomNo("C3");
        anan.addSchedule(new DoctorSchedule(DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(16, 0), 20, "C3"));
        anan.addSchedule(new DoctorSchedule(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), 20, "C3"));

        doctorRepository.save(somchai);
        doctorRepository.save(malee);
        doctorRepository.save(anan);

        Patient p1 = new Patient("HN-2026-0001", "ปิยะ", "ตั้งใจดี", Gender.MALE,
                LocalDate.of(1992, 6, 15), "1200100100201",
                new ContactInfo("0891112222", "piya@mail.test", null));
        p1.setBloodType("O");
        p1.addAllergy("Penicillin");

        Patient p2 = new Patient("HN-2026-0002", "สมหญิง", "มีสุข", Gender.FEMALE,
                LocalDate.of(1958, 2, 2), "1200100100202",
                new ContactInfo("0892223333", "somying@mail.test", null));
        p2.setChronicDisease("เบาหวาน");

        Patient p3 = new Patient("HN-2026-0003", "ธนกร", "แข็งแรง", Gender.MALE,
                LocalDate.of(2015, 11, 9), "1200100100203",
                new ContactInfo("0893334444", null, null));

        patientRepository.save(p1);
        patientRepository.save(p2);
        patientRepository.save(p3);

        Staff kamon = staffRepository.save(new Staff("ST-001", StaffRole.RECEPTIONIST, "กมล", "ยิ้มแย้ม",
                Gender.FEMALE, LocalDate.of(1995, 3, 3), "1300100100301",
                new ContactInfo("0895556666", "kamon@clinic.test", null)));

        seedAccounts(kamon, somchai, malee, p1, p2);
    }

    /**
     * สร้างบัญชีผู้ใช้ตัวอย่างครบทุกบทบาท
     * รหัสผ่านเริ่มต้นอ่านจาก clinic.security.seed-default-password (ค่าเริ่มต้น Clinic@123)
     */
    private void seedAccounts(Staff staff, Doctor doctor1, Doctor doctor2,
                              Patient patient1, Patient patient2) {
        String raw = properties.getSecurity().getSeedDefaultPassword();

        createAccount("admin", Role.ADMIN, null, "ผู้ดูแลระบบ", raw);
        createAccount("staff", Role.STAFF, staff, null, raw);
        createAccount("doctor", Role.DOCTOR, doctor1, null, raw);
        createAccount("doctor2", Role.DOCTOR, doctor2, null, raw);
        createAccount("piya", Role.PATIENT, patient1, null, raw);
        createAccount("somying", Role.PATIENT, patient2, null, raw);

        System.out.println("""
            ---------------------------------------------------------------
             บัญชีตัวอย่าง (โปรไฟล์ dev) — รหัสผ่านทุกบัญชีคือ %s
               admin    : ผู้ดูแลระบบ      เห็นทุกเมนู + จัดการบัญชีผู้ใช้
               staff    : เจ้าหน้าที่      จองนัด/คิว/การเงิน
               doctor   : แพทย์ สมชาย     คิว + เวชระเบียน
               piya     : ผู้ป่วย ปิยะ     พอร์ทัลผู้ป่วย จองนัดเอง
            ---------------------------------------------------------------
            """.formatted(raw));
    }

    private void createAccount(String username, Role role, Person person,
                               String displayName, String rawPassword) {
        if (accountRepository.existsByUsernameIgnoreCase(username)) return;
        accountRepository.save(new UserAccount(username, passwordEncoder.encode(rawPassword),
                role, person, displayName));
    }
}
