package com.clinic.domain.common;

public enum Gender {
    MALE("ชาย"), FEMALE("หญิง"), OTHER("อื่น ๆ");

    private final String label;
    Gender(String label) { this.label = label; }
    public String getLabel() { return label; }
}
