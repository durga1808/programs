package com.zaga.handler.query;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.entity.oteltrace.scopeSpans.Spans;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.entity.queryentity.trace.TraceQuery;
import com.zaga.repo.query.TraceQueryRepo;

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
    // Create filters for both methodName and serviceName
    Bson methodNameFilter = Filters.in("methodName", query.getMethodName());
    Bson serviceNameFilter = Filters.in("serviceName", query.getServiceName());

    // Combine the filters using the $or operator
    Bson filter = Filters.and(methodNameFilter, serviceNameFilter);

    MongoCollection<Document> collection = mongoClient
            .getDatabase("OtelTrace")
            .getCollection("TraceDto");

    // Define the projection to select all fields of TraceDTO
    Bson projection = Projections.excludeId(); // Exclude the _id field

    FindIterable<Document> result = collection
            .find(filter)
            .projection(projection);

    List<TraceDTO> traceDTOList = new ArrayList<>();
    try (MongoCursor<Document> cursor = result.iterator()) {
        while (cursor.hasNext()) {
            Document document = cursor.next();
            TraceDTO traceDTO = new TraceDTO();

            // Extract and handle different data types for all fields
            traceDTO.setTraceId(document.getString("traceId"));
            traceDTO.setServiceName(document.getString("serviceName"));
            traceDTO.setMethodName(document.getString("methodName"));
            traceDTO.setDuration(document.getString("duration"));
            traceDTO.setStatusCode(document.getString("statusCode"));
            traceDTO.setSpanCount(document.getString("spanCount")); // Add this line
            traceDTO.setCreatedTime(document.getString("createdTime")); // Add this line
            traceDTO.setSpans((List<Spans>) document.get("spans")); // Add this line

            traceDTOList.add(traceDTO);
        }
    }

    return traceDTOList;
}

}

    

