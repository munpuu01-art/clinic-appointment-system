package com.clinic.security;

import com.clinic.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * ตั้งค่าความปลอดภัยทั้งระบบไว้ที่เดียว
 * - Stateless ด้วย JWT (ไม่ใช้ session)
 * - แบ่งสิทธิ์ตาม Role: ADMIN / STAFF / DOCTOR / PATIENT
 */
@Configuration
@EnableMethodSecurity   // เปิดใช้ @PreAuthorize ในชั้น controller/service
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;

    // โดเมนของ frontend ที่ deploy จริง (เช่น https://clinic-app.vercel.app) — ตั้งผ่าน
    // environment variable FRONTEND_URL บน Render ได้ ถ้าไม่ตั้งจะไม่มีผลอะไรเพิ่ม
    @Value("${FRONTEND_URL:}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, ObjectMapper objectMapper) {
        this.jwtFilter = jwtFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())          // ไม่ใช้ cookie session จึงไม่ต้องมี CSRF token
            .cors(cors -> cors.configurationSource(corsSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ---------- เปิดสาธารณะ ----------
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/h2-console/**", "/swagger-ui/**", "/swagger-ui.html",
                                 "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ---------- พอร์ทัลผู้ป่วย ----------
                .requestMatchers("/api/portal/**").hasRole("PATIENT")

                // ---------- งานผู้ดูแลระบบ ----------
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/doctors/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers(HttpMethod.DELETE, "/api/doctors/**").hasAnyRole("ADMIN", "DOCTOR")

                // ---------- งานหน้าเคาน์เตอร์ ----------
                .requestMatchers("/api/patients/**").hasAnyRole("ADMIN", "STAFF", "DOCTOR")
                .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "STAFF", "DOCTOR")
                .requestMatchers("/api/queues/**").hasAnyRole("ADMIN", "STAFF", "DOCTOR")
                .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/api/dashboard").hasAnyRole("ADMIN", "STAFF", "DOCTOR")

                // ข้อมูลอ้างอิงและรายชื่อแพทย์ ผู้ป่วยที่ล็อกอินแล้วก็ดูได้ (ใช้ตอนจองนัดเอง)
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> write(res, 401, "UNAUTHENTICATED",
                        "กรุณาเข้าสู่ระบบก่อนใช้งาน", req.getRequestURI()))
                .accessDeniedHandler((req, res, e) -> write(res, 403, "FORBIDDEN",
                        "บัญชีของคุณไม่มีสิทธิ์ใช้งานส่วนนี้", req.getRequestURI()))
            )
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))   // ให้ H2 console แสดงผลได้
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void write(jakarta.servlet.http.HttpServletResponse res, int status,
                       String code, String message, String path) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(res.getWriter(), ApiError.of(code, message, status, path));
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        // อนุญาต origin แบบ ngrok/Vercel เพิ่ม เพราะโดเมนของทั้งสองบริการมีรูปแบบตายตัว
        // แต่ subdomain เปลี่ยนได้ทุกครั้งที่ deploy ใหม่
        java.util.ArrayList<String> patterns = new java.util.ArrayList<>(List.of(
                "http://localhost:*",
                "https://*.ngrok-free.app",
                "https://*.ngrok-free.dev",
                "https://*.ngrok.io",
                "https://*.vercel.app"));
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            patterns.add(frontendUrl.trim());   // โดเมนที่ตั้งไว้ผ่าน FRONTEND_URL (ถ้ามี)
        }
        config.setAllowedOriginPatterns(patterns);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
