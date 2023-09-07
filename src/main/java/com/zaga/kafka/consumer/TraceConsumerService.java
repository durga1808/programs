package com.zaga.kafka.consumer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.oteltrace.OtelTrace;

public class TraceConsumerService {
    

private List<OtelTrace> productDetailsList = new ArrayList();

    @Incoming("product") 
    public void consumeProductDetails(OtelTrace trace) {
        System.out.println("consumer++++++++++++++"+trace);
        productDetailsList.add(trace);
    }

    public List<OtelTrace> getDetails() {
        System.out.println("consumerget"+productDetailsList);
        return productDetailsList;
    }
}