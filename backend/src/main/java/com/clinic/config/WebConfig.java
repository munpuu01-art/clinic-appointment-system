package com.clinic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ตั้งค่า MVC ทั่วไป
 * หมายเหตุ: ตั้งแต่เพิ่มระบบล็อกอิน CORS ถูกย้ายไปประกาศที่ SecurityConfig
 * เพื่อไม่ให้มีการตั้งค่าซ้ำซ้อนสองที่
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
}
