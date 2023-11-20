package com.zaga.entity.queryentity.kepler.Response;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContainerPowerMetrics {
    private Date createdTime;
    private Double consumptionValue;
}
