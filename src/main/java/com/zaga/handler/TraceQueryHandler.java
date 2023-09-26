package com.zaga.handler;


import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import io.quarkus.mongodb.panache.PanacheMongoEntity;
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
     
    

  



    // public List<TraceDTO> getAllMergedTraceDTOs() {
    //     // Connect to the database
    //     MongoCollection<TraceDTO> traceCollection = mongoClient.getDatabase("OtelTrace")
    //             .getCollection("TraceDto", TraceDTO.class);

    //     // Retrieve all TraceDTO records
    //     FindIterable<TraceDTO> traceDTOs = traceCollection.find();

    //     // Create a map to group records by traceId
    //     Map<String, List<TraceDTO>> groupedTraceDTOs = new HashMap<>();

    //     // Iterate through the records and group by traceId
    //     for (TraceDTO traceDTO : traceDTOs) {
    //         groupedTraceDTOs
    //                 .computeIfAbsent(traceDTO.getTraceId(), k -> new ArrayList<>())
    //                 .add(traceDTO);
    //     }

    //     // Create a list to store merged TraceDTO records
    //     List<TraceDTO> mergedTraceDTOs = new ArrayList<>();

    //     // Merge records with the same traceId
    //     for (List<TraceDTO> traceDTOList : groupedTraceDTOs.values()) {
    //         if (traceDTOList.size() > 1) {
    //             // Merge records with the same traceId
    //             TraceDTO mergedTrace = mergeTraceDTOs(traceDTOList);
    //             mergedTraceDTOs.add(mergedTrace);
    //         } else {
    //             // Add single records without merging
    //             mergedTraceDTOs.addAll(traceDTOList);
    //         }
    //     }

    //     return mergedTraceDTOs;
    // }

    // private TraceDTO mergeTraceDTOs(List<TraceDTO> traceDTOList) {
    //     TraceDTO mergedTrace = new TraceDTO();
    
    //     // You can choose any appropriate strategy to merge the records here.
    //     // For example, if you want to keep fields from the first record and
    //     // update the spans, you can do something like this:
    
    //     TraceDTO firstTraceDTO = traceDTOList.get(0);
    //     mergedTrace.setTraceId(firstTraceDTO.getTraceId());
    //     mergedTrace.setServiceName(firstTraceDTO.getServiceName());
    //     mergedTrace.setMethodName(firstTraceDTO.getMethodName());
    //     mergedTrace.setOperationName(firstTraceDTO.getOperationName());
    //     mergedTrace.setDuration(firstTraceDTO.getDuration());
    //     mergedTrace.setStatusCode(firstTraceDTO.getStatusCode());
    //     mergedTrace.setSpanCount(firstTraceDTO.getSpanCount());
    //     mergedTrace.setCreatedTime(firstTraceDTO.getCreatedTime());
    
    //     // Merge the spans from all records into one list
    //     List<Spans> mergedSpans = new ArrayList<>();
    //     for (TraceDTO traceDTO : traceDTOList) {
    //         mergedSpans.addAll(traceDTO.getSpans());
    //     }
    //     mergedTrace.setSpans(mergedSpans);
    
    //     return mergedTrace;
    // }





    public List<TraceDTO> getAllMergedTraceDTOs() {
        // Connect to the database
        MongoCollection<TraceDTO> traceCollection = mongoClient.getDatabase("OtelTrace")
                .getCollection("TraceDto", TraceDTO.class);
    
        // Retrieve all TraceDTO records
        FindIterable<TraceDTO> traceDTOs = traceCollection.find();
    
        // Create a map to group records by traceId
        Map<String, List<TraceDTO>> groupedTraceDTOs = new HashMap<>();
    
        // Iterate through the records and group by traceId
        for (TraceDTO traceDTO : traceDTOs) {
            groupedTraceDTOs
                    .computeIfAbsent(traceDTO.getTraceId(), k -> new ArrayList<>())
                    .add(traceDTO);
        }
    
        // Create a list to store merged TraceDTO records
        List<TraceDTO> mergedTraceDTOs = new ArrayList<>();
    
        // Merge records with the same traceId
        for (List<TraceDTO> traceDTOList : groupedTraceDTOs.values()) {
            if (traceDTOList.size() > 1) {
                // Merge records with the same traceId
                TraceDTO mergedTrace = mergeTraceDTOs(traceDTOList);
                mergedTraceDTOs.add(mergedTrace);
            } else {
                // Add single records without merging
                mergedTraceDTOs.addAll(traceDTOList);
            }
        }
    
        return mergedTraceDTOs;
    }
    
    private TraceDTO mergeTraceDTOs(List<TraceDTO> traceDTOList) {
        TraceDTO mergedTrace = new TraceDTO();
    
        // You can choose any appropriate strategy to merge the records here.
        // For example, if you want to keep fields from the first record and
        // update the spans, you can do something like this:
    
        TraceDTO firstTraceDTO = traceDTOList.get(0);
        mergedTrace.setTraceId(firstTraceDTO.getTraceId());
        mergedTrace.setServiceName(firstTraceDTO.getServiceName());
        mergedTrace.setMethodName(firstTraceDTO.getMethodName());
        mergedTrace.setOperationName(firstTraceDTO.getOperationName());
        mergedTrace.setDuration(firstTraceDTO.getDuration());
        mergedTrace.setStatusCode(firstTraceDTO.getStatusCode());
        mergedTrace.setSpanCount(firstTraceDTO.getSpanCount());
        mergedTrace.setCreatedTime(firstTraceDTO.getCreatedTime());
    
        // Merge the spans from all records into one list
        List<Spans> mergedSpans = new ArrayList<>();
        for (TraceDTO traceDTO : traceDTOList) {
            mergedSpans.addAll(traceDTO.getSpans());
        }
    
        // Sort the merged spans
        mergedSpans.sort(Comparator.comparing(span -> {
            if (span.getParentSpanId() == null || span.getParentSpanId().isEmpty()) {
                // Root span should come first
                return "0";
            } else {
                // Sort by parentSpanId and then spanId
                return span.getParentSpanId() + span.getSpanId();
            }
        }));
    
        mergedTrace.setSpans(mergedSpans);
    
        return mergedTrace;
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


  public Map<String, Long> getTraceCountWithinHour() {
    List<TraceDTO> traceList = TraceDTO.listAll();

    Map<String, Long> serviceNameCounts = new HashMap<>();

    // Iterate through the traceList and count occurrences of each serviceName
    for (TraceDTO trace : traceList) {
        String serviceName = trace.getServiceName();
        serviceNameCounts.put(serviceName, serviceNameCounts.getOrDefault(serviceName, 0L) + 1);
    }

    return serviceNameCounts;
}

public Map<String, Long> getTraceCountForServiceName(int timeAgoHours) {
  List<TraceDTO> traceList = TraceDTO.listAll();
  Map<String, Long> serviceNameCounts = new HashMap<>();

  // Calculate the cutoffTime based on the numeric value and unit
  LocalDateTime cutoffTime = LocalDateTime.now().minusHours(timeAgoHours);

  // Define a DateTimeFormatter for parsing the createdTime string
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

  // Iterate through the traceList and count occurrences of each serviceName
  for (TraceDTO trace : traceList) {
      String createdTimeString = trace.getCreatedTime();
      if (createdTimeString != null) { // Add a null check here
          LocalDateTime traceCreateTime = LocalDateTime.parse(createdTimeString, formatter);

          if (traceCreateTime.isAfter(cutoffTime)) {
              String serviceName = trace.getServiceName();
              serviceNameCounts.put(serviceName, serviceNameCounts.getOrDefault(serviceName, 0L) + 1);
          }
      }
  }

  return serviceNameCounts;
}

}