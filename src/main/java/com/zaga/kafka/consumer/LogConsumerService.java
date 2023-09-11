package com.zaga.kafka.consumer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.service.LogService;

import jakarta.inject.Inject;

public class LogConsumerService {
    
      @Inject
      private LogService logService;
      

   
   
    
  private List<OtelLog> logList = new ArrayList<>();

    @Incoming("logData") 
    public void consumeProductDetails(OtelLog logs) {
        System.out.println("consumer++++++++++++++"+logs);
       logService.createProduct(logs);
    }
}
