package com.zaga.entity.queryentity.trace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attributes {
    private String key;
    private TraceValue value;
    
}
