package com.zaga.entity.queryentity.log;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogQuery {
   private String serviceName; 
   private String severityText;
}
