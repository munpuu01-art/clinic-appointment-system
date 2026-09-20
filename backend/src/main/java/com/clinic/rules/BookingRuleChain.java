package com.clinic.rules;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * ประกอบกฎทุกข้อเป็นโซ่เดียว เรียงตาม order()
 * เพิ่มกฎใหม่ = เพิ่มคลาส @Component ใหม่ ไม่ต้องแก้ Service (Open-Closed Principle)
 */
@Component
public class BookingRuleChain {

    private final BookingRule head;
    private final List<BookingRule> rules;

    public BookingRuleChain(List<BookingRule> ruleList) {
        this.rules = ruleList.stream()
                .sorted(Comparator.comparingInt(BookingRule::order))
                .toList();
        for (int i = 0; i < rules.size() - 1; i++) {
            rules.get(i).setNext(rules.get(i + 1));
        }
        this.head = rules.isEmpty() ? null : rules.get(0);
    }

    /** ผ่านทุกกฎ = จองได้, ผิดกฎข้อใดข้อหนึ่ง = โยน BusinessRuleException */
    public void validate(BookingContext context) {
        if (head != null) head.validate(context);
    }

    /** ตรวจแบบไม่โยน exception ใช้ตอนสร้างรายการช่องเวลาว่าง */
    public boolean isBookable(BookingContext context) {
        try {
            validate(context);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public List<String> ruleNames() { return rules.stream().map(BookingRule::ruleName).toList(); }
}
