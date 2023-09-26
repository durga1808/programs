package com.zaga.handler;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.zaga.entity.oteltrace.scopeSpans.Spans;
import com.zaga.entity.queryentity.trace.StatusCodeRange;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.entity.queryentity.trace.TraceMetrics;
import com.zaga.entity.queryentity.trace.TraceQuery;
import com.zaga.repo.TraceQueryRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.conversions.Bson;

@ApplicationScoped
public class TraceQueryHandler {

  @Inject
  TraceQueryRepo traceQueryRepo;

  @Inject
  MongoClient mongoClient;


  //getting all the datas from traceDTO entity

  public List<TraceDTO> getTraceProduct() {
    List<TraceDTO> traceList = traceQueryRepo.listAll();

    traceList.forEach(this::sortSpans);

    traceList.sort(this::compareTraceDTOs);

    return traceList;
  }

  // Method to sorting and arranging the spans within a TraceDTO
  private void sortSpans(TraceDTO trace) {
    trace
      .getSpans()
      .sort(
        Comparator.comparing(span -> {
          if (
            span.getParentSpanId() == null || span.getParentSpanId().isEmpty()
          ) {
            // Root span should come first
            return "0";
          } else {
            // Sort by parentSpanId and then spanId
            System.out.println(
              "span valuesss" + span.getParentSpanId() + span.getSpanId()
            );
            return span.getParentSpanId() + span.getSpanId();
          }
        })
      );
  }

  // Method to compare two TraceDTOs for sorting
  private int compareTraceDTOs(TraceDTO trace1, TraceDTO trace2) {
    if (trace1.getSpans().isEmpty() && trace2.getSpans().isEmpty()) {
      // Handle cases where both TraceDTOs have no spans
      return 0;
    } else if (trace1.getSpans().isEmpty()) {
      // Handle cases where trace1 has no spans
      return -1;
    } else if (trace2.getSpans().isEmpty()) {
      // Handle cases where trace2 has no spans
      return 1;
    } else {
      Spans firstSpan1 = trace1.getSpans().get(0);
      Spans firstSpan2 = trace2.getSpans().get(0);

      if (
        firstSpan1.getParentSpanId() == null ||
        firstSpan1.getParentSpanId().isEmpty()
      ) {
        if (
          firstSpan2.getParentSpanId() == null ||
          firstSpan2.getParentSpanId().isEmpty()
        ) {

          return firstSpan1.getSpanId().compareTo(firstSpan2.getSpanId());
        } else {
          return -1;
        }
      } else {
        if (
          firstSpan2.getParentSpanId() == null ||
          firstSpan2.getParentSpanId().isEmpty()
        ) {
          return 1;
        } else {
          String key1 = firstSpan1.getParentSpanId() + firstSpan1.getSpanId();
          String key2 = firstSpan2.getParentSpanId() + firstSpan2.getSpanId();
          return key1.compareTo(key2);
        }
      }
    }
  }


  private TraceDTO mergeTraceDTOs(List<TraceDTO> traceDTOList) {
    TraceDTO mergedTrace = new TraceDTO();

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
    mergedSpans.sort(
      Comparator.comparing(span -> {
        if (
          span.getParentSpanId() == null || span.getParentSpanId().isEmpty()
        ) {
          return "0";
        } else {
          return span.getParentSpanId() + span.getSpanId();
        }
      })
    );

    mergedTrace.setSpans(mergedSpans);

    return mergedTrace;
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
      Bson serviceNameFilter = Filters.in(
        "serviceName",
        query.getServiceName()
      );
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

  // Create a method to merge and sort TraceDTOs
  private List<TraceDTO> mergeAndSortTraceDTOs(List<TraceDTO> traceList) {
    // Merge records with the same traceId
    List<TraceDTO> mergedTraceDTOs = new ArrayList<>();
    Map<String, List<TraceDTO>> groupedTraceDTOs = new HashMap<>();

    for (TraceDTO traceDTO : traceList) {
      groupedTraceDTOs
        .computeIfAbsent(traceDTO.getTraceId(), k -> new ArrayList<>())
        .add(traceDTO);
    }

    for (List<TraceDTO> traceDTOList : groupedTraceDTOs.values()) {
      if (traceDTOList.size() > 1) {
        TraceDTO mergedTrace = mergeTraceDTOs(traceDTOList);
        mergedTraceDTOs.add(mergedTrace);
      } else {
        mergedTraceDTOs.addAll(traceDTOList);
      }
    }

    // Sort the mergedTraceDTOs
    mergedTraceDTOs.sort(this::compareTraceDTOs);

    return mergedTraceDTOs;
  }





  // pagination data with merge and sorting implementations
  public List<TraceDTO> findRecentDataPaged(int page, int pageSize) {

    List<TraceDTO> traceList = traceQueryRepo.listAll();
    traceList = mergeAndSortTraceDTOs(traceList);

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, traceList.size());

    // System.out.println(
    //   "traceList of pagination: " + traceList.subList(startIndex, endIndex)
    // );
    return traceList.subList(startIndex, endIndex);
  }

  //Count calculation for pagination
  public long countData() {
    System.out.println(
      "TraceQueryHandler.countData()" + traceQueryRepo.count()
    );
    return traceQueryRepo.count();
  }


  // pagination data for trace summary page based on serviceName and statusCode
  public List<TraceDTO> findRecentDataPaged(int page, int pageSize, String serviceName, int statusCode) {
    List<TraceDTO> traceList = traceQueryRepo.listAll();
    traceList = mergeAndSortTraceDTOs(traceList);

    // Filter by serviceName and statusCode
    traceList = filterByServiceNameAndStatusCode(traceList, serviceName, statusCode);

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, traceList.size());

    // System.out.println(
    //     "traceList of pagination: " + traceList.subList(startIndex, endIndex)
    // );
    return traceList.subList(startIndex, endIndex);
}

// Create a method to filter TraceDTOs by serviceName and statusCode
private List<TraceDTO> filterByServiceNameAndStatusCode(List<TraceDTO> traceList, String serviceName, int statusCode) {
    if (serviceName == null && statusCode == 0) {
        return traceList; // No filtering required
    }

    List<TraceDTO> filteredTraceList = new ArrayList<>();
    for (TraceDTO traceDTO : traceList) {
        if ((serviceName == null || serviceName.isEmpty() || traceDTO.getServiceName().equals(serviceName)) &&
            (statusCode == 0 || traceDTO.getStatusCode() == statusCode)) {
            filteredTraceList.add(traceDTO);
        }
    }
    return filteredTraceList;
}

  


  // appicalll counts calculations
  public Map<String, Long> getTraceCountWithinHour() {
    List<TraceDTO> traceList = TraceDTO.listAll();

    Map<String, Long> serviceNameCounts = new HashMap<>();

    for (TraceDTO trace : traceList) {
      String serviceName = trace.getServiceName();
      serviceNameCounts.put(
        serviceName,
        serviceNameCounts.getOrDefault(serviceName, 0L) + 1
      );
    }

    return serviceNameCounts;
  }

  public List<TraceMetrics> getTraceMetricsForServiceNameInMinutes(int timeAgoMinutes) {
    List<TraceDTO> traceList = TraceDTO.listAll();
    Map<String, TraceMetrics> metricsMap = new HashMap<>();

    // Calculate the cutoffTime based on the numeric value and unit (in minutes)
    LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeAgoMinutes);

    // Define a DateTimeFormatter for parsing the createdTime string
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    // Iterate through the traceList and accumulate metrics for each serviceName
    for (TraceDTO trace : traceList) {
        String createdTimeString = trace.getCreatedTime();
        if (createdTimeString != null) {
            LocalDateTime traceCreateTime = LocalDateTime.parse(createdTimeString, formatter);

            if (traceCreateTime.isAfter(cutoffTime)) {
                String serviceName = trace.getServiceName();

                // Get or create a TraceMetrics object for the serviceName
                TraceMetrics metrics = metricsMap.get(serviceName);
                if (metrics == null) {
                    metrics = new TraceMetrics();
                    metrics.setServiceName(serviceName);
                    metrics.setApiCallCount(0L); // Initialize apiCallCount to 0
                    metrics.setTotalErrorCalls(0L); // Initialize totalErrorCalls to 0
                    metrics.setTotalSuccessCalls(0L);
                }

                // Update metrics
                metrics.setApiCallCount(metrics.getApiCallCount() + 1);
                // You would need to add logic to update peakLatency, totalSuccessCalls, and any other metrics here.

                // Put the updated metrics back into the map
                metricsMap.put(serviceName, metrics);
            }
        }
    }

    // Now, calculate error counts and update the totalErrorCalls property
    Map<String, Long> errorCounts = calculateErrorCountsByService(); // Call your error count calculation method
    Map<String, Long> successCounts = calculateSuccessCountsByService(); // Call your success count calculation method

    for (Map.Entry<String, Long> entry : errorCounts.entrySet()) {
        String serviceName = entry.getKey();
        Long errorCount = entry.getValue();

        // Update the TraceMetrics object in metricsMap
        TraceMetrics metrics = metricsMap.get(serviceName);
        if (metrics != null) {
            metrics.setTotalErrorCalls(errorCount);
        }
    }

    for (Map.Entry<String, Long> entry : successCounts.entrySet()) {
        String serviceName = entry.getKey();
        Long successCount = entry.getValue();

        // Update the TraceMetrics object in metricsMap
        TraceMetrics metrics = metricsMap.get(serviceName);
        if (metrics != null) {
            metrics.setTotalSuccessCalls(successCount);
        }
    }

    // Convert the map values (TraceMetrics) into a list
    return new ArrayList<>(metricsMap.values());
}


public Map<String, Long> calculateErrorCountsByService() {
  MongoCollection<Document> traceCollection = mongoClient.getDatabase("OtelTrace").getCollection("TraceDto");

  List<Bson> aggregationStages = new ArrayList<>();
  aggregationStages.add(Aggregates.match(Filters.and(
      Filters.gte("statusCode", 400L),
      Filters.lte("statusCode", 599L)
  )));
  aggregationStages.add(Aggregates.group("$serviceName", Accumulators.sum("errorCount", 1L)));

  AggregateIterable<Document> results = traceCollection.aggregate(aggregationStages);

  Map<String, Long> errorCounts = new HashMap<>();
  for (Document result : results) {
      String serviceName = result.getString("_id");
      Long count = result.getLong("errorCount");
      errorCounts.put(serviceName, count);
  }

  return errorCounts;
}


public Map<String, Long> calculateSuccessCountsByService() {
  MongoCollection<Document> traceCollection = mongoClient.getDatabase("OtelTrace").getCollection("TraceDto");

  // Define aggregation stages to group and count successes by serviceName and statusCode
  List<Bson> aggregationStages = new ArrayList<>();
  aggregationStages.add(Aggregates.match(Filters.and(
      Filters.gte("statusCode", 200L),
      Filters.lte("statusCode", 299L)
  )));
  aggregationStages.add(Aggregates.group("$serviceName", Accumulators.sum("successCount", 1L)));

  // Execute the aggregation pipeline
  AggregateIterable<Document> results = traceCollection.aggregate(aggregationStages);

  // Process the results into a map
  Map<String, Long> successCounts = new HashMap<>();
  for (Document result : results) {
      String serviceName = result.getString("_id");
      Long count = result.getLong("successCount");
      successCounts.put(serviceName, count);
  }

  return successCounts;
}
}
