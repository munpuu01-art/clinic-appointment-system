package com.clinic.domain.billing;

import java.math.BigDecimal;

/** Interface: สิ่งที่คิดเงินได้ (Invoice, อนาคตอาจมี Package/Membership) */
public interface Billable {
    BigDecimal calculateTotal();
    BigDecimal outstandingAmount();
    boolean isSettled();
}
