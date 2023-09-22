package com.zaga.handler.query;


import java.util.ArrayList;
import java.util.List;

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
import com.zaga.repo.query.TraceQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceQueryHandler {

    @Inject
    TraceQueryRepo traceQueryRepo;

    @Inject
    MongoClient mongoClient;


    
    private final MongoCollection<Document> collection;

    public List<TraceDTO> getTraceProduct() {
        return traceQueryRepo.listAll();
    }



    public TraceQueryHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        collection = mongoClient.getDatabase("OtelTrace").getCollection("Trace");
    }

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

}
