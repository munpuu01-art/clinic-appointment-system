package com.clinic.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Value Object: ที่อยู่ผู้ป่วย */
@Embeddable
public class Address {

    @Column(name = "addr_line", length = 200)
    private String line;
    @Column(name = "addr_district", length = 80)
    private String district;
    @Column(name = "addr_province", length = 80)
    private String province;
    @Column(name = "addr_postcode", length = 5)
    private String postcode;

    protected Address() { }

    public Address(String line, String district, String province, String postcode) {
        this.line = line;
        this.district = district;
        this.province = province;
        this.postcode = postcode;
    }

    public String getLine() { return line; }
    public String getDistrict() { return district; }
    public String getProvince() { return province; }
    public String getPostcode() { return postcode; }

    public String fullAddress() {
        return String.join(" ", safe(line), safe(district), safe(province), safe(postcode)).trim();
    }

    private String safe(String s) { return s == null ? "" : s; }
}
