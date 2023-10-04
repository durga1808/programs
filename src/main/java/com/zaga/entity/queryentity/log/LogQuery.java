package com.zaga.entity.queryentity.log;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogQuery {
   private List<String> serviceName; 
   private List<String> severityText;
}
