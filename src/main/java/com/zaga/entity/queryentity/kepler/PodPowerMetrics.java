package com.zaga.entity.queryentity.kepler;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PodPowerMetrics {
    private Date createdTime;
    private Long consumptionValue;
    
}
