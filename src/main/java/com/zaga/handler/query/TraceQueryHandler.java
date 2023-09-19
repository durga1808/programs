package com.zaga.handler.query;


import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

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

    public TraceQueryHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        collection = mongoClient.getDatabase("OtelTrace").getCollection("Trace");
    }

    public List<Document> getTraceByServiceName(String serviceName) {
        List<Document> trace = new ArrayList<>();

        // Create a query to filter documents where "resourceLogs.resource.attributes.value.stringValue" matches serviceName
        Bson query = Filters.eq("resourceSpans.resource.attributes.value.stringValue", serviceName);

        // Perform the query and return the documents
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }

        return trace;
    }

    // public List<OtelTrace> findByStatusCodeAndQueryParam(String valueParam) {
    //     String[] values = valueParam.split(",");
        
    //     if (values.length == 1) {
    //         int intValue = Integer.parseInt(values[0]);
    //         return traceQueryRepo.findByHttpStatusValue(intValue);
    //     } else {
    //         List<Integer> intValues = Arrays.stream(values)
    //                 .map(Integer::parseInt)
    //                 .collect(Collectors.toList());
                    
    //         return traceQueryRepo.findByHttpStatusValues(intValues);
    //     }
    // }

  
    

    public List<Document> getTraceByStatusCode(Integer statusCode) {
        List<Document> trace = new ArrayList<>();
    
        // Create a query to filter documents where "resourceSpans.resource.attributes.value.intValue" matches the statusCode
        Bson query = Filters.eq("resourceSpans.scopeSpans.spans.attributes.value.intValue", statusCode);
    
        // Perform the query and return the documents
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }
    
        return trace;
    }
    
     public List<Document> getTraceByHttpMethod(String httpMethod) {
        List<Document> trace = new ArrayList<>();
    
        // Create a query to filter documents where "resourceSpans.resource.attributes.value.intValue" matches the statusCode
        Bson query = Filters.eq("resourceSpans.scopeSpans.spans.attributes.value.stringValue", httpMethod);
    
        // Perform the query and return the documents
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }
    
        return trace;
    }
     
    public List<Document> getTraceByServiceNameAndHttpMethod(String serviceName, String httpMethod) {
        List<Document> trace = new ArrayList<>();
    
        // Create a query to filter documents where both service name and HTTP method match
        Bson query = Filters.and(
            Filters.eq("resourceSpans.resource.attributes.value.stringValue", serviceName),
            Filters.eq("resourceSpans.scopeSpans.spans.attributes.value.stringValue", httpMethod)
        );
    
        // Perform the query and return the documents
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }
    
        return trace;
    }
    
    public List<Document> getTraceByServiceNameAndStatusCode(String serviceName, Integer statusCode) {
        List<Document> trace = new ArrayList<>();
    
        // Create a query to filter documents where both status code and service name match
        Bson query = Filters.and(
            Filters.eq("resourceSpans.resource.attributes.value.stringValue", serviceName),
            Filters.eq("resourceSpans.scopeSpans.spans.attributes.value.intValue", statusCode)
        );
    
        // Perform the query and return the documents
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }
    
        return trace;
    }
    
   
    public List<Document> getTraceByMultipleStatusCodes(List<Integer> statusCodes) {
        List<Document> trace = new ArrayList<>();
    
        // Create a query to filter documents where "resourceSpans.scopeSpans.spans.attributes.value.intValue" matches any of the specified status codes
        Bson query = Filters.in("resourceSpans.scopeSpans.spans.attributes.value.intValue", statusCodes);
    
        // Perform the query and return the documents
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                trace.add(cursor.next());
            }
        }
    
        return trace;
    }
    
    
    
    
    
    

    
}
