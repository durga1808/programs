package com.zaga.entity.otelmetric.sum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SumDataPointAttribute {
    private String key;
    private SumDataPointAttributeValue value;

}
