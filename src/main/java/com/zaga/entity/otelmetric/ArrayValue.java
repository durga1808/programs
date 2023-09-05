package com.zaga.entity.otelmetric;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArrayValue  {
    private List<Value> values;
}
