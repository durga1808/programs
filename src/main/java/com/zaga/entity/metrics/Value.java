package com.zaga.entity.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Value {
    private String stringValue;
    private int intValue;
    private ArrayValue arrayValue;
}
