package com.zaga.entity.queryentity.trace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaMetrics {
    private String serviceName;
    private Long kafkaCallCount;
    private Long kafkaPeakLatency;
}
