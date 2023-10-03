package com.zaga.entity.queryentity.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogMetrics {
    private String serviceName;
    private Long errorCallCount;
    private Long warnCallCount;
    private Long debugCallCount;
}
