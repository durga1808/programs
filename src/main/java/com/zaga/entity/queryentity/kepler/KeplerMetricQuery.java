package com.zaga.entity.queryentity.kepler;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KeplerMetricQuery {

  public String podName;
  public String containerNamespace;
  public List<PodPowerMetrics> podPowerMetrics;
}
