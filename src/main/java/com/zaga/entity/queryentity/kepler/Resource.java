package com.zaga.entity.queryentity.kepler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Resource {
    private String nameSpace;
    private String podName;
    private String containerId;
    private String containerName;
    private String metricsName;
    private String node;
    private String host;

}
