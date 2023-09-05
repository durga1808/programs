package com.zaga.entity.metrics;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistogramDataPointAttribute {
    private String key;
    private HistogramDataPointAttributeValue value;
}
