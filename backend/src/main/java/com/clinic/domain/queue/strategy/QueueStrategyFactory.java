package com.clinic.domain.queue.strategy;

import com.clinic.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory Pattern: เลือก Strategy จากชื่อ
 * Spring ฉีด Strategy ทุกตัวที่เป็น @Component เข้ามาอัตโนมัติ (DI)
 */
@Component
public class QueueStrategyFactory {

    private final Map<String, QueueOrderingStrategy> strategies;

    public QueueStrategyFactory(List<QueueOrderingStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(QueueOrderingStrategy::name, Function.identity()));
    }

    public QueueOrderingStrategy create(String name) {
        QueueOrderingStrategy strategy = strategies.get(name == null ? "" : name.toUpperCase());
        if (strategy == null) {
            throw new BusinessRuleException("UNKNOWN_STRATEGY",
                    "ไม่รู้จักรูปแบบการจัดคิว: " + name + " (ที่รองรับ: " + strategies.keySet() + ")");
        }
        return strategy;
    }

    public List<String> availableStrategies() { return List.copyOf(strategies.keySet()); }
}
