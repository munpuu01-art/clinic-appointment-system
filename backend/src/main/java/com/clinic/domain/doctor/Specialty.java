package com.clinic.domain.doctor;

import com.clinic.domain.common.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** แผนก/ความเชี่ยวชาญ เช่น อายุรกรรม ทันตกรรม */
@Entity
@Table(name = "specialty")
public class Specialty extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    /** ระยะเวลาตรวจมาตรฐานของแผนกนี้ (นาที) */
    @Column(name = "default_slot_minutes", nullable = false)
    private int defaultSlotMinutes = 20;

    @Column(name = "base_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFee = BigDecimal.valueOf(500);

    @OneToMany(mappedBy = "specialty", fetch = FetchType.LAZY)
    private List<Doctor> doctors = new ArrayList<>();

    protected Specialty() { }

    public Specialty(String code, String name, int defaultSlotMinutes, BigDecimal baseFee) {
        this.code = code;
        this.name = name;
        this.defaultSlotMinutes = defaultSlotMinutes;
        this.baseFee = baseFee;
    }

    public String getCode() { return code; }
    public void setCode(String v) { this.code = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public int getDefaultSlotMinutes() { return defaultSlotMinutes; }
    public void setDefaultSlotMinutes(int v) { this.defaultSlotMinutes = v; }
    public BigDecimal getBaseFee() { return baseFee; }
    public void setBaseFee(BigDecimal v) { this.baseFee = v; }
    public List<Doctor> getDoctors() { return doctors; }
}
