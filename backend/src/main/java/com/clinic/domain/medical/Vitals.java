package com.clinic.domain.medical;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Value Object: สัญญาณชีพที่พยาบาลวัดก่อนพบแพทย์ */
@Embeddable
public class Vitals {

    @Column(name = "temperature_c")
    private Double temperatureC;
    @Column(name = "systolic")
    private Integer systolic;
    @Column(name = "diastolic")
    private Integer diastolic;
    @Column(name = "pulse")
    private Integer pulse;
    @Column(name = "weight_kg")
    private Double weightKg;
    @Column(name = "height_cm")
    private Double heightCm;

    protected Vitals() { }

    public Vitals(Double temperatureC, Integer systolic, Integer diastolic,
                  Integer pulse, Double weightKg, Double heightCm) {
        this.temperatureC = temperatureC;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.pulse = pulse;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
    }

    public Double bmi() {
        if (weightKg == null || heightCm == null || heightCm == 0) return null;
        double m = heightCm / 100.0;
        return Math.round((weightKg / (m * m)) * 10) / 10.0;
    }

    public boolean hasFever() { return temperatureC != null && temperatureC >= 37.5; }

    public boolean isHypertensive() {
        return systolic != null && diastolic != null && (systolic >= 140 || diastolic >= 90);
    }

    /** ธงเตือนให้พยาบาลอัปเกรดคิวเป็นเร่งด่วน */
    public boolean needsUrgentAttention() { return hasFever() && isHypertensive(); }

    public Double getTemperatureC() { return temperatureC; }
    public Integer getSystolic() { return systolic; }
    public Integer getDiastolic() { return diastolic; }
    public Integer getPulse() { return pulse; }
    public Double getWeightKg() { return weightKg; }
    public Double getHeightCm() { return heightCm; }
}
