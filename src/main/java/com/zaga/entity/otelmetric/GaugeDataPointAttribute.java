package com.zaga.entity.otelmetric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GaugeDataPointAttribute {
    private String key;
    private GaugeDataPointAttributeValue value;

}
