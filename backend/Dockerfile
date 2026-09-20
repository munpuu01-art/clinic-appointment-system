# Multi-stage build — ขั้นแรก build ด้วย Maven, ขั้นสอง run ด้วย JRE เบา ๆ เท่านั้น
# ทำให้ image สุดท้ายเล็กและ deploy ได้เร็ว

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# ดาวน์โหลด dependency ก่อนแยกต่างหาก เพื่อให้ Docker cache ชั้นนี้ไว้ (build รอบถัดไปเร็วขึ้นมาก)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/clinic-appointment-system-1.0.0.jar app.jar

# Render กำหนดพอร์ตผ่าน environment variable PORT ให้เองอัตโนมัติ
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
