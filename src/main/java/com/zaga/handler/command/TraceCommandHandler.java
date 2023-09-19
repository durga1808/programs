package com.zaga.handler.command;

import java.util.ArrayList;
import java.util.List;
import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.entity.oteltrace.ResourceSpans;
import com.zaga.entity.oteltrace.ScopeSpans;
import com.zaga.entity.oteltrace.resource.attributes.Value;
import com.zaga.entity.oteltrace.scopeSpans.Spans;
import com.zaga.entity.queryentity.trace.SpansData;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.entity.queryentity.trace.TraceValue;
import com.zaga.repo.command.TraceCommandRepo;
import com.zaga.repo.query.TraceQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceCommandHandler {

    @Inject
    TraceCommandRepo traceCommandRepo;

    @Inject
    TraceQueryRepo traceQueryRepo;

    public void createTraceProduct(OtelTrace trace) {
        traceCommandRepo.persist(trace);

        List<TraceDTO> traceDTOs = extractAndMapData(trace);
        if (!traceDTOs.isEmpty()) {
            for (TraceDTO traceDTO : traceDTOs) {
                traceQueryRepo.persist(traceDTO);
            }
        } else {
            System.out.println("No trace id");
        }
    }

   
private List<TraceDTO> extractAndMapData(OtelTrace trace) {
    List<TraceDTO> traceDTOs = new ArrayList<>();

    try {
        for (ResourceSpans resourceSpans : trace.getResourceSpans()) {
            String serviceName = getServiceName(resourceSpans);

            for (ScopeSpans scopeSpans : resourceSpans.getScopeSpans()) {
                for (Spans span : scopeSpans.getSpans()) {
                    TraceDTO traceDTO = new TraceDTO();
                    traceDTO.setServiceName(serviceName);
                    traceDTO.setTraceId(span.getTraceId());
                    traceDTO.setMethodName(extractMethodName(span));
                    traceDTO.setDuration(calculateDuration(span));
                    traceDTO.setStatusCode(extractStatusCode(span));
                    traceDTO.setSpanCount(String.valueOf(scopeSpans.getSpans().size()));
                    traceDTO.setCreatedTime(span.getStartTimeUnixNano());

                    List<com.zaga.entity.queryentity.trace.Attributes> attributesList = new ArrayList<>();
                    for (com.zaga.entity.oteltrace.scopeSpans.spans.Attributes sourceAttribute : span.getAttributes()) {
                        com.zaga.entity.queryentity.trace.Attributes targetAttribute = mapAttributes(sourceAttribute);
                        attributesList.add(targetAttribute);
                    }

                    SpansData spansData = new SpansData();
                    spansData.setAttributes(attributesList);
                    // Set other fields in spansData
                    spansData.setEndTimeUnixNano(span.getEndTimeUnixNano());
                    spansData.setKind(span.getKind());
                    spansData.setName(span.getName());
                    spansData.setParentSpanId(span.getParentSpanId());
                    spansData.setSpanId(span.getSpanId());
                    spansData.setStartTimeUnixNano(span.getStartTimeUnixNano());
                    spansData.setStatus(span.getStatus());

                    List<SpansData> spansList = new ArrayList<>();
                    spansList.add(spansData);

                    traceDTO.setSpans(spansList);

                    traceDTOs.add(traceDTO);
                }
            }
        }

        return traceDTOs;
    } catch (Exception e) {
        e.printStackTrace();
        return traceDTOs;
    }
}



     private com.zaga.entity.queryentity.trace.Attributes mapAttributes(com.zaga.entity.oteltrace.scopeSpans.spans.Attributes source) {
        com.zaga.entity.queryentity.trace.Attributes target = new com.zaga.entity.queryentity.trace.Attributes();
        
        Value sourceValue = source.getValue(); 
        
        TraceValue traceValue = new TraceValue();
        traceValue.setIntValue(sourceValue.getIntValue());
        traceValue.setStringValue(sourceValue.getStringValue());
        
        String key = source.getKey();
        
        target.setKey(key);
        target.setValue(traceValue);
        
        return target;
    }

    
    private String getServiceName(ResourceSpans resourceSpans) {
        return resourceSpans.getResource().getAttributes().stream()
                .filter(attribute -> "service.name".equals(attribute.getKey()))
                .findFirst()
                .map(attribute -> attribute.getValue().getStringValue())
                .orElse(null);
    }

    private String calculateDuration(Spans span) {
        return "CalculatedDuration";
    }

    private String extractMethodName(Spans span) {
        try {
            // Extract the "http.method" attribute from the span's attributes
            return span.getName(); 
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown"; 
        }
    }

    private String extractStatusCode(Spans span) {
        if (span == null || span.getAttributes() == null) {
            return "Unknown";
        }
    
        return span.getAttributes()
                .stream()
                .filter(attribute -> "http.status_code".equals(attribute.getKey()))
                .findFirst()
                .map(attribute -> String.valueOf(attribute.getValue().getIntValue()))
                .orElse("0");
    }
    
    public List<OtelTrace> getTraceProduct(OtelTrace trace) {
        return traceCommandRepo.listAll();
    }
}

