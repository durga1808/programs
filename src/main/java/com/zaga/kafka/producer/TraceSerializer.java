// package com.zaga.kafka.producer;

// import com.fasterxml.jackson.databind.ObjectMapper;

// public class TraceSerializer  implements Serializer<OtelTrace> {
    
// }
//  private final objectMapper objectMapper;

//     public KafkaSerializer() {
//         this.objectMapper = new ObjectMapper();
//     }

//     @Override
//     public byte[] serialize(String topic, ProductDetails productDetails) {
//         try {
//             return objectMapper.writeValueAsBytes(productDetails);
//         } catch (Exception e) {
//             throw new RuntimeException("Error serializing to JSON", e);
//         }
//     }

//     @Override
//     public void close() {
//         // No resources to close for this serializer
//     }
// }
