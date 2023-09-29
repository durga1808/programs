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
import com.mongodb.client.model.Sorts;
import com.zaga.entity.oteltrace.scopeSpans.Spans;
import com.zaga.entity.queryentity.trace.StatusCodeRange;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.entity.queryentity.trace.TraceMetrics;
import com.zaga.entity.queryentity.trace.TraceQuery;
import com.zaga.repo.TraceQueryRepo;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

@ApplicationScoped
public class TraceQueryHandler {

  @Inject
  TraceQueryRepo traceQueryRepo;

  @Inject
  MongoClient mongoClient;

  //getting all the datas from traceDTO entity

//   public List<TraceDTO> getTraceProduct() {
//     List<TraceDTO> traceList = traceQueryRepo.listAll();

//     traceList.forEach(this::sortSpans);

//     // Sort the traceList based on the comparison logic
//     traceList.sort(this::compareTraceDTOs);

//     return traceList;
// }



public List<TraceDTO> getTraceProduct() {
  List<TraceDTO> traceList = traceQueryRepo.listAll();

  // Sort the traceList using a custom comparator
  traceList.sort(Comparator.comparing(this::getTraceSortingKey));

  return traceList;
}

private String getTraceSortingKey(TraceDTO trace) {
  List<Spans> spans = trace.getSpans();

  if (spans.isEmpty()) {
      return "";
  }

  Map<String, Spans> spanMap = spans.stream()
          .collect(Collectors.toMap(Spans::getSpanId, span -> span));

  List<Spans> sortedSpans = new ArrayList<>();

  Spans currentSpan = spans.stream()
          .filter(span -> span.getParentSpanId().isEmpty())
          .findFirst()
          .orElse(null);

  while (currentSpan != null) {
      sortedSpans.add(currentSpan);
      String nextSpanId = currentSpan.getSpanId();
      currentSpan = spanMap.get(nextSpanId);
      spanMap.remove(nextSpanId); 
  }

  StringBuilder keyBuilder = new StringBuilder();
  for (Spans span : sortedSpans) {
      keyBuilder.append(span.getParentSpanId()).append(span.getSpanId());
  }

  return keyBuilder.toString();
}



// Method to compare two TraceDTOs for sorting
private int compareTraceDTOs(TraceDTO trace1, TraceDTO trace2) {
  List<Spans> spans1 = trace1.getSpans();
  List<Spans> spans2 = trace2.getSpans();

  // Compare spans one by one
  for (int i = 0; i < Math.min(spans1.size(), spans2.size()); i++) {
      Spans span1 = spans1.get(i);
      Spans span2 = spans2.get(i);

      int parentSpanIdComparison = span1.getParentSpanId().compareTo(span2.getParentSpanId());
      if (parentSpanIdComparison != 0) {
          return parentSpanIdComparison;
      }

      int spanIdComparison = span1.getSpanId().compareTo(span2.getSpanId());
      if (spanIdComparison != 0) {
          return spanIdComparison;
      }
  }

  // If all compared spans are equal, the trace with fewer spans should come first
  return Integer.compare(spans1.size(), spans2.size());
}

private void sortSpans(TraceDTO trace) {
    trace.getSpans().sort(Comparator.comparing(Spans::getParentSpanId)
            .thenComparing(Spans::getSpanId));
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

private FindIterable<Document> getFilteredResults(TraceQuery query, int skip, int limit, int minutesAgo) {
    List<Bson> filters = new ArrayList<>();

    if (minutesAgo > 0) {
      long currentTimeInMillis = System.currentTimeMillis();
      long timeAgoInMillis = currentTimeInMillis - (minutesAgo * 60 * 1000); 
      Bson timeFilter = Filters.gte("createdTime", new Date(timeAgoInMillis));
      filters.add(timeFilter);
  }

    if (query.getMethodName() != null && !query.getMethodName().isEmpty()) {
        Bson methodNameFilter = Filters.in("methodName", query.getMethodName());
        filters.add(methodNameFilter);
    }

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

    return collection
            .find(filter)
            .projection(projection)
            .skip(skip)
            .limit(limit);
}


  // getTrace by multiple queries like serviceName, method, duration and statuscode from TraceDTO entity
  public List<TraceDTO> searchTracesPaged(TraceQuery query, int page, int pageSize, int minutesAgo) {
    int skip = (page - 0) * pageSize;
    int limit = pageSize;

    FindIterable<Document> result = getFilteredResults(query, skip, limit, minutesAgo);
   
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
            traceDTO.setMethodName(document.getString("methodName"));
            traceDTO.setOperationName(document.getString("operationName"));
            traceDTO.setSpanCount(document.getString("spanCount"));
            traceDTO.setCreatedTime(document.getDate("createdTime"));
            traceDTO.setSpans((List<Spans>) document.get("spans"));

            traceDTOList.add(traceDTO);
        }
    }

    return traceDTOList;
}

public long countQueryTraces(TraceQuery query, int minutesAgo) {
  FindIterable<Document> result = getFilteredResults(query, 0, Integer.MAX_VALUE, minutesAgo);
  long totalCount = result.into(new ArrayList<>()).size(); 

  return totalCount;
}





  // pagination data with merge and sorting implementations
  public List<TraceDTO> findRecentDataPaged(int page, int pageSize) {
    List<TraceDTO> traceList = traceQueryRepo.listAll();
    traceList = mergeAndSortTraceDTOs(traceList);

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, traceList.size());
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
  public List<TraceDTO> findByServiceNameAndStatusCode(
    int page,
    int pageSize,
    String serviceName,
    int statusCode
  ) {
    List<TraceDTO> traceList = traceQueryRepo.listAll();
    traceList = mergeAndSortTraceDTOs(traceList);
    traceList =
      filterByServiceNameAndStatusCode(traceList, serviceName, statusCode);

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, traceList.size());
    return traceList.subList(startIndex, endIndex);
  }



  // Create a method to filter TraceDTOs by serviceName and statusCode
  private List<TraceDTO> filterByServiceNameAndStatusCode(
    List<TraceDTO> traceList,
    String serviceName,
    int statusCode
  ) {
    if (serviceName == null && statusCode == 0) {
      return traceList;
    }

    List<TraceDTO> filteredTraceList = new ArrayList<>();
    for (TraceDTO traceDTO : traceList) {
      if (
        (
          serviceName == null ||
          serviceName.isEmpty() ||
          traceDTO.getServiceName().equals(serviceName)
        ) &&
        (statusCode == 0 || traceDTO.getStatusCode() == statusCode)
      ) {
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


  
public List<TraceMetrics> getTraceMetricCount(
    int timeAgoMinutes
) {
    List<TraceDTO> traceList = TraceDTO.listAll();
    Map<String, TraceMetrics> metricsMap = new HashMap<>();

    Instant cutoffTime = Instant.now().minus(timeAgoMinutes, ChronoUnit.MINUTES);

    for (TraceDTO trace : traceList) {
        Date traceCreateTime = trace.getCreatedTime();
        if (traceCreateTime != null) {
            Instant traceInstant = traceCreateTime.toInstant();

            if (!traceInstant.isBefore(cutoffTime)) {
                String serviceName = trace.getServiceName();

                TraceMetrics metrics = metricsMap.get(serviceName);
                if (metrics == null) {
                    metrics = new TraceMetrics();
                    metrics.setServiceName(serviceName);
                    metrics.setApiCallCount(0L);
                    metrics.setTotalErrorCalls(0L);
                    metrics.setTotalSuccessCalls(0L);
                    metrics.setPeakLatency(0L);
                }
                metrics.setApiCallCount(metrics.getApiCallCount() + 1);
                metricsMap.put(serviceName, metrics);
            }
        }
    }


    Map<String, Long> errorCounts = calculateErrorCountsByService();
    Map<String, Long> successCounts = calculateSuccessCountsByService();
    Map<String, Long> peakLatency = calculatePeakLatencyCountsByService();

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

    for (Map.Entry<String, Long> entry : peakLatency.entrySet()) {
      String serviceName = entry.getKey();
      Long peakLatencyCount = entry.getValue();

      // Update the TraceMetrics object in metricsMap with peak latency count
      TraceMetrics metrics = metricsMap.get(serviceName);
      if (metrics != null) {
        metrics.setPeakLatency(peakLatencyCount);
      }
    }

    return new ArrayList<>(metricsMap.values());
  }

  public Map<String, Long> calculateErrorCountsByService() {
    MongoCollection<Document> traceCollection = mongoClient
      .getDatabase("OtelTrace")
      .getCollection("TraceDto");

    List<Bson> aggregationStages = new ArrayList<>();
    aggregationStages.add(
      Aggregates.match(
        Filters.and(
          Filters.gte("statusCode", 400L),
          Filters.lte("statusCode", 599L)
        )
      )
    );
    aggregationStages.add(
      Aggregates.group("$serviceName", Accumulators.sum("errorCount", 1L))
    );

    AggregateIterable<Document> results = traceCollection.aggregate(
      aggregationStages
    );

    Map<String, Long> errorCounts = new HashMap<>();
    for (Document result : results) {
      String serviceName = result.getString("_id");
      Long count = result.getLong("errorCount");
      errorCounts.put(serviceName, count);
    }

    return errorCounts;
  }

  public Map<String, Long> calculateSuccessCountsByService() {
    MongoCollection<Document> traceCollection = mongoClient
      .getDatabase("OtelTrace")
      .getCollection("TraceDto");

    // Define aggregation stages to group and count successes by serviceName and statusCode
    List<Bson> aggregationStages = new ArrayList<>();
    aggregationStages.add(
      Aggregates.match(
        Filters.and(
          Filters.gte("statusCode", 200L),
          Filters.lte("statusCode", 299L)
        )
      )
    );
    aggregationStages.add(
      Aggregates.group("$serviceName", Accumulators.sum("successCount", 1L))
    );

    // Execute the aggregation pipeline
    AggregateIterable<Document> results = traceCollection.aggregate(
      aggregationStages
    );

    // Process the results into a map
    Map<String, Long> successCounts = new HashMap<>();
    for (Document result : results) {
      String serviceName = result.getString("_id");
      Long count = result.getLong("successCount");
      successCounts.put(serviceName, count);
    }

    return successCounts;
  }

  public Map<String, Long> calculatePeakLatencyCountsByService() {
    MongoCollection<Document> traceCollection = mongoClient
      .getDatabase("OtelTrace")
      .getCollection("TraceDto");

    List<Bson> aggregationStages = new ArrayList<>();

    aggregationStages.add(Aggregates.match(Filters.gt("duration", 500L)));

    aggregationStages.add(
      Aggregates.group("$serviceName", Accumulators.sum("peakLatency", 1L))
    );

    AggregateIterable<Document> results = traceCollection.aggregate(
      aggregationStages
    );

    Map<String, Long> peakLatency = new HashMap<>();
    for (Document result : results) {
      String serviceName = result.getString("_id");
      Long count = result.getLong("peakLatency");
      peakLatency.put(serviceName, count);
    }

    return peakLatency;
  }


//method for filtering page and page size, time and sortorder list out the data
   //method for filtering page and page size, time and sortorder list out the data
   public List<TraceDTO> getPaginatedTraces(int page, int pageSize, int timeAgoMinutes) {
    Instant startTime = Instant.now().minus(timeAgoMinutes, ChronoUnit.MINUTES);
    List<TraceDTO> traces = traceQueryRepo.find("createdTime >= ?1", startTime)
            .page(Page.of(page - 1, pageSize))
            .list();

    return traces;
}

//time method for sortorder pagination
public long getTraceCountInMinutes(int timeAgoMinutes) {
    Instant startTime = Instant.now().minus(timeAgoMinutes, ChronoUnit.MINUTES);
    return traceQueryRepo.find("createdTime >= ?1", startTime).count();
}

//newest data listout based on time 
public List<TraceDTO> getNewestTraces(int page, int pageSize, int timeAgoMinutes) {
  Instant startTime = Instant.now().minus(timeAgoMinutes, ChronoUnit.MINUTES);

  // Create a PanacheQuery to find traces with createdTime >= startTime
  PanacheQuery<TraceDTO> query = TraceDTO.find("createdTime >= ?1", startTime);

  // Manually sort the query by createdTime in descending order
  List<TraceDTO> sortedTraces = query.page(Page.of(page - 1, pageSize)).list();
  sortedTraces.sort((trace1, trace2) -> trace2.getCreatedTime().compareTo(trace1.getCreatedTime()));

  return sortedTraces;
}

//oldest data listout based on time 
public List<TraceDTO> getOldestTraces(int page, int pageSize, int timeAgoMinutes) {
Instant startTime = Instant.now().minus(timeAgoMinutes, ChronoUnit.MINUTES);

PanacheQuery<TraceDTO> query = TraceDTO.find("createdTime >= ?1", startTime);

List<TraceDTO> sortedTraces = query.page(Page.of(page - 1, pageSize)).list();
sortedTraces.sort((trace1, trace2) -> trace1.getCreatedTime().compareTo(trace2.getCreatedTime()));

return sortedTraces;
}



//error data listout in sorted order
public Map<String, Object> getErrorTracesWithCount(int page, int pageSize, int timeAgoMinutes) {
  MongoCollection<Document> traceCollection = mongoClient
          .getDatabase("OtelTrace")
          .getCollection("TraceDto");

  // Define the aggregation stage to filter and count errors within the time range
  Instant startTime = Instant.now().minus(timeAgoMinutes, ChronoUnit.MINUTES);
  List<Bson> aggregationStages = new ArrayList<>();
  aggregationStages.add(
          Aggregates.match(
                  Filters.and(
                          Filters.gte("statusCode", 400), // Minimum HTTP status code for errors
                          Filters.lte("statusCode", 599), // Maximum HTTP status code for errors
                          Filters.gte("createdTime", Date.from(startTime)) // Created time within the time range
                  )
          )
  );
  aggregationStages.add(
          Aggregates.count("errorCount")
  );

  // Execute the aggregation pipeline
  AggregateIterable<Document> result = traceCollection.aggregate(aggregationStages);

  // Extract the error count
  Long errorCount = 0L;
  for (Document doc : result) {
      Object errorCountObj = doc.get("errorCount");
      if (errorCountObj instanceof Number) {
          errorCount = ((Number) errorCountObj).longValue();
          break; // Stop iterating after finding the count
      }
  }

  // Retrieve error data with pagination within the time range
  List<Document> errorDocuments = traceCollection.aggregate(
    Arrays.asList(
        Aggregates.match(
            Filters.and(
                Filters.gte("statusCode", 400),
                Filters.lte("statusCode", 599),
                Filters.gte("createdTime", Date.from(startTime))
            )
        ),
        Aggregates.sort(Sorts.descending("createdTime")),
        Aggregates.skip((page - 1) * pageSize),
        Aggregates.limit(pageSize),
        Aggregates.project(
            Projections.exclude("_id", "date", "timestamp")
        )
    )
).into(new ArrayList<>());

  // Create a response map
  Map<String, Object> response = new HashMap<>();
  response.put("data", errorDocuments);
  response.put("totalCount", errorCount);

  return response;
}





//peak latency in sort order
public Map<String, Object> getPeakLatencyTraces(int page, int pageSize, int timeAgoMinutes) {
  MongoCollection<Document> traceCollection = mongoClient
          .getDatabase("OtelTrace")
          .getCollection("TraceDto");

  // Define the aggregation stage to filter and count peak latency traces within the time range
  Instant startTime = Instant.now().minus(timeAgoMinutes, ChronoUnit.MINUTES);
  List<Bson> aggregationStages = new ArrayList<>();
  aggregationStages.add(
          Aggregates.match(
                  Filters.and(
                          Filters.gte("duration", 100L), // Minimum duration for peak latency (100 milliseconds)
                          Filters.gte("createdTime", Date.from(startTime)) // Created time within the time range
                  )
          )
  );
  aggregationStages.add(
          Aggregates.count("peakLatencyCount")
  );

  // Execute the aggregation pipeline to get the peak latency count
  AggregateIterable<Document> countResult = traceCollection.aggregate(aggregationStages);

  // Extract the peak latency count
  Long peakLatencyCount = 0L;
  for (Document doc : countResult) {
      Object peakLatencyCountObj = doc.get("peakLatencyCount");
      if (peakLatencyCountObj instanceof Number) {
          peakLatencyCount = ((Number) peakLatencyCountObj).longValue();
          break; // Stop iterating after finding the count
      }
  }

  // Retrieve peak latency data with pagination within the time range
  List<Document> peakLatencyDocuments = traceCollection.aggregate(
          Arrays.asList(
                  Aggregates.match(
                          Filters.and(
                                  Filters.gte("duration", 100L), // Minimum duration for peak latency (100 milliseconds)
                                  Filters.gte("createdTime", Date.from(startTime)) // Created time within the time range
                          )
                  ),
                  Aggregates.sort(Sorts.descending("createdTime")),
                  Aggregates.skip((page - 1) * pageSize),
                  Aggregates.limit(pageSize),
                  Aggregates.project(
                          Projections.exclude("_id", "date", "timestamp")
                  )
          )
  ).into(new ArrayList<>());

  // Create a response map
  Map<String, Object> response = new HashMap<>();
  response.put("data", peakLatencyDocuments);
  response.put("totalCount", peakLatencyCount);

  return response;
}



// getByTraceId sort the spans and if some traceId Has same value it will merge the value
public List<Spans> sortingParentChildOrder(List<Spans> spanData) {
  Map<String, List<Spans>> spanTree = new HashMap<>();

  List<Spans> rootSpans = new ArrayList<>();

  for (Spans span : spanData) {
    String spanId = span.getSpanId();
    String parentId = span.getParentSpanId();
    if (parentId == null || parentId.isEmpty()) {
      // Span with empty parentSpanId is a root span
      rootSpans.add(span);
    } else {
      spanTree.computeIfAbsent(parentId, k -> new ArrayList<>()).add(span);
    }
  }

  List<Spans> sortedSpans = new ArrayList<>();

  for (Spans rootSpan : rootSpans) {
    sortSpans(rootSpan, spanTree, sortedSpans);
  }

  return sortedSpans;
}

private void sortSpans(Spans span, Map<String, List<Spans>> spanTree, List<Spans> sortedSpans) {
  sortedSpans.add(span);
  List<Spans> childSpans = spanTree.get(span.getSpanId());
  if (childSpans != null) {
    for (Spans childSpan : childSpans) {
      sortSpans(childSpan, spanTree, sortedSpans);
    }
  }
}

// Method to merge spans with the same traceId
public List<TraceDTO> mergeTraces(List<TraceDTO> traces) {
  Map<String, TraceDTO> traceMap = new HashMap<>();

  for (TraceDTO trace : traces) {
    String traceId = trace.getTraceId();

    if (traceMap.containsKey(traceId)) {
      System.out.println("CONTAINES SAME------------------------------------------------ " + traceId);
      TraceDTO existingTrace = traceMap.get(traceId);
      existingTrace.getSpans().addAll(trace.getSpans());
    } else {
      traceMap.put(traceId, trace);
    }
  }
  return new ArrayList<>(traceMap.values());
}

}
