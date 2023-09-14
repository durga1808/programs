package com.zaga.repo.query;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.zaga.entity.oteltrace.OtelTrace;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
// import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TraceQueryRepo implements PanacheMongoRepository<OtelTrace> {


    public List<OtelTrace> findByHttpStatusValue(int intValue) {
        return list("resourceSpans.scopeSpans.spans.attributes.value.intValue", intValue);
    }

    public List<OtelTrace> findByHttpStatusValues(List<Integer> intValues) {
        return list("resourceSpans.scopeSpans.spans.attributes.value.intValue in ?1", intValues);
    }
    // public List<OtelTrace> findByStatusCodeAndQueryParam(String valueParam) {
    //     String[] values = valueParam.split(",");
        
    //     if (values.length == 1) {
    //         int intValue = Integer.parseInt(values[0]);
    //         return list("resourceSpans.scopeSpans.spans.attributes.value.intValue", intValue);
    //     } else {
    //         List<Integer> intValues = Arrays.stream(values)
    //                 .map(s -> Integer.parseInt(s)) // Explicitly specify the type conversion
    //                 .collect(Collectors.toList());
                    
    //         return list("resourceSpans.scopeSpans.spans.attributes.value.intValue in ?1", intValues);
    //     }
    // }
}
