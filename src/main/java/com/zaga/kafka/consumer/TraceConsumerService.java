package com.zaga.kafka.consumer;


import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.service.TraceService;

import jakarta.inject.Inject;

public class TraceConsumerService {

    @Inject
    TraceService traceService;
    

    @Incoming("trace-in") 
    public void consumeProductDetails(OtelTrace trace) {
        System.out.println("consumer--+-+-+-+-+-+-+-+"+trace);
        traceService.createProduct(trace);
    }

}