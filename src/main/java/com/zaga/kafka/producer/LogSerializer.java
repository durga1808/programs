package com.zaga.kafka.producer;

import org.apache.kafka.common.serialization.Serializer;

import com.zaga.entity.otellog.OtelLog;

public class LogSerializer implements Serializer<OtelLog> {

    @Override
    public byte[] serialize(String arg0, OtelLog arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'serialize'");
    }
    
}
