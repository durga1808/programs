package com.zaga.entity.queryentity.kepler.Response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KeplerResponseData {
    private String displayName;
    private List<ContainerPowerMetrics> containerPowerMetrics;
    private Integer totalCount;
}
