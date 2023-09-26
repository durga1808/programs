package com.zaga.handler;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.zaga.entity.oteltrace.scopeSpans.Spans;
import com.zaga.entity.queryentity.trace.StatusCodeRange;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.entity.queryentity.trace.TraceQuery;
import com.zaga.repo.TraceQueryRepo;

import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceQueryHandler {

    @Inject
    TraceQueryRepo traceQueryRepo;

    @Inject
    MongoClient mongoClient;


    
    private final MongoCollection<Document> collection;

    // public List<TraceDTO> getTraceProduct() {
    //     return traceQueryRepo.listAll();
    // }

    public List<TraceDTO> getTraceProduct() {
        List<TraceDTO> traceList = traceQueryRepo.listAll();
    
        // Sort the spans within each TraceDTO
        traceList.forEach(trace -> {
            trace.getSpans().sort(Comparator.comparing(span -> {
                if (span.getParentSpanId() == null || span.getParentSpanId().isEmpty()) {
                    // Root span should come first
                    return "0";
                } else {
                    // Sort by parentSpanId and then spanId
                    return span.getParentSpanId() + span.getSpanId();
                }
            }));
        });
    
        // Sort the TraceDTOs based on the first span in each TraceDTO
        traceList.sort(Comparator.comparing(trace -> {
            if (trace.getSpans().isEmpty()) {
                // Handle cases where there are no spans
                return "";
            } else {
                Spans firstSpan = trace.getSpans().get(0);
                if (firstSpan.getParentSpanId() == null || firstSpan.getParentSpanId().isEmpty()) {
                    // Root span should come first
                    return "0";
                } else {
                    // Sort by parentSpanId and then spanId of the first span
                    return firstSpan.getParentSpanId() + firstSpan.getSpanId();
                }
            }
        }));
    
        return traceList;
    }
     
    

    public List<TraceDTO> getMergedSpanData() {
        // Get all trace data from the repository
        List<TraceDTO> allTraces = traceQueryRepo.listAll();
    
        // Create a map to store merged traces by trace ID
        Map<String, TraceDTO> mergedTraceMap = new HashMap<>();
    
        // Iterate through all traces
        for (TraceDTO trace : allTraces) {
            boolean isMerged = false;
            List<Spans> mergedSpans = new ArrayList<>();
    
            // Iterate through the spans in the current trace
            for (Spans span : trace.getSpans()) {
                // Check if the parentSpanId is null or empty
                if (span.getParentSpanId() == null || span.getParentSpanId().isEmpty()) {
                    isMerged = true;
                    mergedSpans.add(span);
                }
            }
    
            if (isMerged) {
                // Check if a merged trace with the same trace ID already exists
                TraceDTO mergedTrace = mergedTraceMap.get(trace.getTraceId());
    
                if (mergedTrace == null) {
                    // If no merged trace exists, create a new one
                    mergedTrace = new TraceDTO();
                    mergedTrace.setTraceId(trace.getTraceId());
                    mergedTrace.setServiceName(trace.getServiceName());
                    mergedTrace.setMethodName(trace.getMethodName());
                    mergedTrace.setOperationName(trace.getOperationName());
                    mergedTrace.setDuration(trace.getDuration());
                    mergedTrace.setStatusCode(trace.getStatusCode());
                    mergedTrace.setSpanCount(trace.getSpanCount());
                    mergedTrace.setCreatedTime(trace.getCreatedTime());
                    mergedTrace.setSpans(new ArrayList<>()); // Initialize the spans list
                    mergedTraceMap.put(trace.getTraceId(), mergedTrace);
                }
    
                // Merge the spans into the merged trace
                mergedTrace.getSpans().addAll(mergedSpans);
            }
        }
    
        // Convert the map values (merged traces) to a list
        List<TraceDTO> mergedTraceDataList = new ArrayList<>(mergedTraceMap.values());
    
        return mergedTraceDataList;
    }





    
       


    public TraceQueryHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        collection = mongoClient.getDatabase("OtelTrace").getCollection("Trace");
    }


    // getTrace by serviceName from OtelTrace 
    public List<Document> getTraceByServiceName(String serviceName) {
        List<Document> trace = new ArrayList<>();

        Bson query = Filters.eq("resourceSpans.resource.attributes.value.stringValue", serviceName);

        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }

        return trace;
    }

  
    
    // getTrace by statusCode from OtelTrace 
    public List<Document> getTraceByStatusCode(Integer statusCode) {
        List<Document> trace = new ArrayList<>();
    
        Bson query = Filters.eq("resourceSpans.scopeSpans.spans.attributes.value.intValue", statusCode);
    
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }
    
        return trace;
    }
    
    // getTrace by HttpMethod from OtelTrace 
     public List<Document> getTraceByHttpMethod(String httpMethod) {
        List<Document> trace = new ArrayList<>();
    
        Bson query = Filters.eq("resourceSpans.scopeSpans.spans.attributes.value.stringValue", httpMethod);
    
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }
    
        return trace;
    }

    
    // getTrace by service name with http method from OtelTrace
    public List<Document> getTraceByServiceNameAndHttpMethod(String serviceName, String httpMethod) {
        List<Document> trace = new ArrayList<>();
    
        Bson query = Filters.and(
            Filters.eq("resourceSpans.resource.attributes.value.stringValue", serviceName),
            Filters.eq("resourceSpans.scopeSpans.spans.attributes.value.stringValue", httpMethod)
        );
    
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }
    
        return trace;
    }

    
    // getTrace by service name with http status from OtelTrace
    public List<Document> getTraceByServiceNameAndStatusCode(String serviceName, Integer statusCode) {
        List<Document> trace = new ArrayList<>();
    
        Bson query = Filters.and(
            Filters.eq("resourceSpans.resource.attributes.value.stringValue", serviceName),
            Filters.eq("resourceSpans.scopeSpans.spans.attributes.value.intValue", statusCode)
        );
    
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }
    
        return trace;
    }
    
       // getTrace by service name with multiple http method from OtelTrace
    public List<Document> getTraceByMultipleStatusCodes(List<Integer> statusCodes) {
        List<Document> trace = new ArrayList<>();
    
        Bson query = Filters.in("resourceSpans.scopeSpans.spans.attributes.value.intValue", statusCodes);
    
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }
    
        return trace;
    }
    

    // getTrace by multiple queries like serviceName, method, duration and statuscode from TraceDTO entity
    public List<TraceDTO> searchTraces(TraceQuery query) {
        List<Bson> filters = new ArrayList<>();
    
        // Check if methodName is provided in the query
        if (query.getMethodName() != null && !query.getMethodName().isEmpty()) {
            Bson methodNameFilter = Filters.in("methodName", query.getMethodName());
            filters.add(methodNameFilter);
        }
    
        // Add filters for serviceName, duration, and statusCode (unchanged)
        if (query.getServiceName() != null && !query.getServiceName().isEmpty()) {
            Bson serviceNameFilter = Filters.in("serviceName", query.getServiceName());
            filters.add(serviceNameFilter);
        }
    
        if (query.getDuration() != null) {
            Bson durationFilter = Filters.and(
                Filters.gte("duration", query.getDuration().getMin()),
                Filters.lte("duration", query.getDuration().getMax())
            );
            filters.add(durationFilter);
        }
    
        List<Bson> statusCodeFilters = new ArrayList<>();
        if (query.getStatusCode() != null && !query.getStatusCode().isEmpty()) {
            for (StatusCodeRange statusCodeRange : query.getStatusCode()) {
                statusCodeFilters.add(
                    Filters.and(
                        Filters.gte("statusCode", statusCodeRange.getMin()),
                        Filters.lte("statusCode", statusCodeRange.getMax())
                    )
                );
            }
        }
    
        if (!statusCodeFilters.isEmpty()) {
            Bson statusCodeFilter = Filters.or(statusCodeFilters);
            filters.add(statusCodeFilter);
        }
    
        Bson filter = Filters.and(filters);
    
        MongoCollection<Document> collection = mongoClient
            .getDatabase("OtelTrace")
            .getCollection("TraceDto");
    
        Bson projection = Projections.excludeId(); 
    
        FindIterable<Document> result = collection
            .find(filter)
            .projection(projection);
    
        List<TraceDTO> traceDTOList = new ArrayList<>();
        try (MongoCursor<Document> cursor = result.iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                TraceDTO traceDTO = new TraceDTO();
    
                traceDTO.setTraceId(document.getString("traceId"));
                traceDTO.setServiceName(document.getString("serviceName"));
                Object durationObject = document.get("duration");
                if (durationObject instanceof Integer) {
                    traceDTO.setDuration(((Integer) durationObject).longValue());
                } else if (durationObject instanceof Long) {
                    traceDTO.setDuration((Long) durationObject);
                }
        
                // Handle casting for statusCode field
                Object statusCodeObject = document.get("statusCode");
                if (statusCodeObject instanceof Integer) {
                    traceDTO.setStatusCode(((Integer) statusCodeObject).longValue());
                } else if (statusCodeObject instanceof Long) {
                    traceDTO.setStatusCode((Long) statusCodeObject);
                }
                traceDTO.setSpanCount(document.getString("spanCount")); 
                traceDTO.setCreatedTime(document.getString("createdTime"));
                traceDTO.setSpans((List<Spans>) document.get("spans"));
    
                traceDTOList.add(traceDTO);
            }
        }
    
        return traceDTOList;
}


public List<TraceDTO> findRecentDataPaged(String serviceName, int page, int pageSize) {
    PanacheQuery<TraceDTO> query = traceQueryRepo.find("serviceName = ?1 order by createdTime desc", serviceName);
    query.page(page, pageSize); // Apply paging

    return query.list();
}

public long countData(String serviceName) {
    return traceQueryRepo.count("serviceName = ?1", serviceName);
}








}
