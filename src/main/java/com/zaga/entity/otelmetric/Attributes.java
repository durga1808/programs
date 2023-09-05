package com.zaga.entity.otelmetric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attributes {
    private String key;

   private Value value;
}
