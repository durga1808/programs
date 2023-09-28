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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

  // getTrace by multiple queries like serviceName, method, duration and statuscode from TraceDTO entity
  public List<TraceDTO> searchTraces(TraceQuery query) {
    List<Bson> filters = new ArrayList<>();

    if (query.getMethodName() != null && !query.getMethodName().isEmpty()) {
      Bson methodNameFilter = Filters.in("methodName", query.getMethodName());
      filters.add(methodNameFilter);
    }

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

    // Filter by serviceName and statusCode
    traceList =
      filterByServiceNameAndStatusCode(traceList, serviceName, statusCode);

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, traceList.size());

    // System.out.println(
    //     "traceList of pagination: " + traceList.subList(startIndex, endIndex)
    // );
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

public List<TraceMetrics> getTraceMetricsForServiceNameInMinutes(
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
}
