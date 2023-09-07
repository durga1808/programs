package com.zaga.entity.otelmetric.resource;



import com.zaga.entity.otellog.resource.attributes.Value;

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
