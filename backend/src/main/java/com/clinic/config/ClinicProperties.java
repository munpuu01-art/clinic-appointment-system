package com.clinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** รวม business configuration ไว้ที่เดียว ไม่ hard-code ใน service */
@ConfigurationProperties(prefix = "clinic")
public class ClinicProperties {

    private final Booking booking = new Booking();
    private final Queue queue = new Queue();
    private final Notification notification = new Notification();
    private final Security security = new Security();

    public Booking getBooking() { return booking; }
    public Queue getQueue() { return queue; }
    public Notification getNotification() { return notification; }
    public Security getSecurity() { return security; }

    public static class Booking {
        private int advanceDays = 60;
        private int minLeadMinutes = 30;
        private int maxPerPatientPerDay = 2;
        public int getAdvanceDays() { return advanceDays; }
        public void setAdvanceDays(int v) { this.advanceDays = v; }
        public int getMinLeadMinutes() { return minLeadMinutes; }
        public void setMinLeadMinutes(int v) { this.minLeadMinutes = v; }
        public int getMaxPerPatientPerDay() { return maxPerPatientPerDay; }
        public void setMaxPerPatientPerDay(int v) { this.maxPerPatientPerDay = v; }
    }

    public static class Queue {
        private String defaultStrategy = "PRIORITY";
        public String getDefaultStrategy() { return defaultStrategy; }
        public void setDefaultStrategy(String v) { this.defaultStrategy = v; }
    }

    public static class Notification {
        private boolean emailEnabled = true;
        private boolean smsEnabled = true;
        public boolean isEmailEnabled() { return emailEnabled; }
        public void setEmailEnabled(boolean v) { this.emailEnabled = v; }
        public boolean isSmsEnabled() { return smsEnabled; }
        public void setSmsEnabled(boolean v) { this.smsEnabled = v; }
    }

    /** ค่าตั้งของระบบยืนยันตัวตน */
    public static class Security {
        private String jwtSecret = "clinic-appointment-system-development-secret-key-change-me";
        private int jwtExpirationMinutes = 480;
        private String seedDefaultPassword = "Clinic@123";
        public String getJwtSecret() { return jwtSecret; }
        public void setJwtSecret(String v) { this.jwtSecret = v; }
        public int getJwtExpirationMinutes() { return jwtExpirationMinutes; }
        public void setJwtExpirationMinutes(int v) { this.jwtExpirationMinutes = v; }
        public String getSeedDefaultPassword() { return seedDefaultPassword; }
        public void setSeedDefaultPassword(String v) { this.seedDefaultPassword = v; }
    }
}
