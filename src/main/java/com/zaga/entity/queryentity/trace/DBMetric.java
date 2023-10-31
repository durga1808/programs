package com.zaga.entity.queryentity.trace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBMetric {
 private String serviceName;
 private Long dbCallCount;
// private String dbName;
 private Long dbPeakLatencyCount;
}
