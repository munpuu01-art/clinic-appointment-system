package com.clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * จุดเริ่มต้นของระบบนัดหมายคลินิก (Clinic Appointment System)
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ClinicApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClinicApplication.class, args);
    }
}
