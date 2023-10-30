package com.zaga.handler;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.zaga.entity.otellog.ScopeLogs;
import com.zaga.entity.otellog.scopeLogs.LogRecord;
import com.zaga.entity.otellog.scopeLogs.logRecord.LogAttribute;
import com.zaga.entity.oteltrace.scopeSpans.Event;
import com.zaga.entity.oteltrace.scopeSpans.Spans;
import com.zaga.entity.oteltrace.scopeSpans.spans.Attributes;
import com.zaga.entity.queryentity.log.LogDTO;
import com.zaga.entity.queryentity.trace.SpanDTO;
import com.zaga.entity.queryentity.trace.StatusCodeRange;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.entity.queryentity.trace.TraceMetrics;
import com.zaga.entity.queryentity.trace.TraceQuery;
import com.zaga.entity.queryentity.trace.TraceSpanDTO;
import com.zaga.repo.LogQueryRepo;
import com.zaga.repo.TraceQueryRepo;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
  LogQueryRepo logQueryRepo;

  @Inject
  MongoClient mongoClient;

  // getting all the datas from traceDTO entity
  public List<TraceDTO> getSampleTrace() {
    List<TraceDTO> traceList = traceQueryRepo.listAll();
    return traceList;
}



public List<TraceDTO> getTraceProduct() {
  List<TraceDTO> traceList = traceQueryRepo.listAll();
  return traceList;
}


private Bson createCustomDateFilter(LocalDate from, LocalDate to) {
  return Filters.and(
          Filters.gte("createdTime", from.atStartOfDay()),
          Filters.lt("createdTime", to.plusDays(1).atStartOfDay())
  );
}


private FindIterable<Document> getFilteredResults(TraceQuery query, int page, int pageSize, LocalDate from, LocalDate to, int minutesAgo) {
  List<Bson> filters = new ArrayList<>();

  if (from != null && to != null) {
      Bson timeFilter = createCustomDateFilter(from, to);
      filters.add(timeFilter);
  } else if (minutesAgo > 0) {
    LocalDate currentDate = LocalDate.now();

    if (from != null && from.isEqual(currentDate)) {
        // If the date is the current date, apply time filter based on minutes ago
        long currentTimeInMillis = System.currentTimeMillis();
        long timeAgoInMillis = currentTimeInMillis - (minutesAgo * 60 * 1000);

        // Ensure that the time filter doesn't go beyond the current day
        long startOfDayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        if (timeAgoInMillis < startOfDayMillis) {
            timeAgoInMillis = startOfDayMillis;
        }

        Bson timeFilter = Filters.gte("createdTime", new Date(timeAgoInMillis));
        filters.add(timeFilter);
    }
else if (from != null) {
          // If a specific date is provided, use it for filtering
          Bson timeFilter = createCustomDateFilter(from, from);
          filters.add(timeFilter);
      }
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
          .getCollection("TraceDTO");

  Bson projection = Projections.excludeId();

  System.out.println("Skip: " + (page - 1) * pageSize);
  System.out.println("Limit: " + pageSize);

  Bson sort = Sorts.descending("createdTime");

  return collection
          .find(filter)
          .projection(projection)
          .sort(sort)
          .skip((page - 1) * pageSize)
          .limit(pageSize);
}



  // getTrace by multiple queries like serviceName, method, duration and statuscode from TraceDTO entity
  public List<TraceDTO> searchTracesPaged(TraceQuery query, int page, int pageSize, LocalDate from, LocalDate to, int minutesAgo) {
    System.out.println("from Date --------------" + from);
    System.out.println("to Date --------------" + to);

    // Swap 'from' and 'to' if 'to' is earlier than 'from'
    if (from != null && to != null && to.isBefore(from)) {
        LocalDate temp = from;
        from = to;
        to = temp;
    }

    FindIterable<Document> result = getFilteredResults(query, page, pageSize, from, to, minutesAgo);

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
            List<Spans> spansList = (List<Spans>) document.get("spans");
            traceDTO.setSpans(spansList);

            traceDTOList.add(traceDTO);
        }
    }

    return traceDTOList;
}

public long countQueryTraces(TraceQuery query, LocalDate from, LocalDate to, int minutesAgo) {
    // Swap 'from' and 'to' if 'to' is earlier than 'from'
    if (from != null && to != null && to.isBefore(from)) {
        LocalDate temp = from;
        from = to;
        to = temp;
    }

    FindIterable<Document> result = getFilteredResults(query, 0, Integer.MAX_VALUE, from, to, minutesAgo);
    System.out.println("countQueryTraces: " + result.into(new ArrayList<>()).size());
    long totalCount = result.into(new ArrayList<>()).size();
    return totalCount;
}


  // pagination data with merge and sorting implementations
  public List<TraceDTO> findRecentDataPaged(int page, int pageSize) {
    List<TraceDTO> traceList = traceQueryRepo.listAll();
    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, traceList.size());
    return traceList.subList(startIndex, endIndex);
  }

  //Count calculation for pagination
  public long countData() {
    System.out.println("TraceQueryHandler.countData()" + traceQueryRepo.count());
    return traceQueryRepo.count();
  }




public List<TraceDTO> findErrorsLastTwoHours(String serviceName) {
  Date twoHoursAgo = new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000);

  PanacheQuery<TraceDTO> query = traceQueryRepo.find("serviceName = ?1 and createdTime >= ?2", serviceName, twoHoursAgo);
  List<TraceDTO> traceList = query.list();

  List<TraceDTO> sortedTraceList = traceList.stream()
  .filter(traceDTO -> traceDTO.getStatusCode() != null && traceDTO.getStatusCode() >= 400 && traceDTO.getStatusCode() <= 599)
  .sorted(Comparator.comparing(TraceDTO::getCreatedTime).reversed())
  .collect(Collectors.toList());

return sortedTraceList;
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


  



//method for filtering page and page size, time and sortorder list out the data
   public List<TraceDTO> getPaginatedTraces(int page, int pageSize, int timeAgoMinutes) {
    Instant startTime = Instant.now().minus(timeAgoMinutes, ChronoUnit.MINUTES);
    List<TraceDTO> traces = traceQueryRepo.find("createdTime >= ?1", startTime)
            .page(Page.of(page - 1, pageSize))
            .list();

    return traces;
}

//time method for sortorder pagination
public long getTraceCountInMinutes(int page, int pageSize, int timeAgoMinutes) {
    Instant startTime = Instant.now().minus(timeAgoMinutes, ChronoUnit.MINUTES);
    return traceQueryRepo.find("createdTime >= ?1", startTime).count();
}

//sort order decending
public List<TraceDTO> getAllTracesOrderByCreatedTimeDesc(List<String> serviceNameList) {
  return traceQueryRepo.findAllOrderByCreatedTimeDesc(serviceNameList);
}

//sort order ascending
public List<TraceDTO> getAllTracesAsc(List<String> serviceNameList){
  return traceQueryRepo.findAllOrderByCreatedTimeAsc(serviceNameList);
}

// sort order error first
public List<TraceDTO> findAllOrderByErrorFirst(List<String> serviceNameList) {
  MongoCollection<Document> traceCollection = mongoClient
          .getDatabase("OtelTrace")
          .getCollection("TraceDTO");

  List<TraceDTO> allTraces = traceCollection.find(TraceDTO.class).into(new ArrayList<>());

  List<TraceDTO> sortedTraces = allTraces.stream()
          .filter(trace -> serviceNameList.contains(trace.getServiceName())) // Filter by service name list
          .sorted(Comparator
                  // Sort by error status first (statusCode >= 400 && statusCode <= 599)
                  .comparing((TraceDTO trace) -> {
                      Long statusCode = trace.getStatusCode();
                      return (statusCode != null && statusCode >= 400 && statusCode <= 599) ? 0 : 1;
                  })
                  // Then sort by status code in descending order
                  .thenComparing(TraceDTO::getStatusCode, Comparator.nullsLast(Comparator.reverseOrder()))
                  // Finally, sort by created time in descending order
                  .thenComparing(TraceDTO::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder())))
          .collect(Collectors.toList());

  return sortedTraces;
}






public List<TraceDTO> findAllOrderByDuration(List<String> serviceNameList) {
  MongoCollection<Document> traceCollection = mongoClient
          .getDatabase("OtelTrace")
          .getCollection("TraceDTO");

  List<TraceDTO> allTraces = traceCollection.find(TraceDTO.class).into(new ArrayList<>());

  List<TraceDTO> sortedTraces = allTraces.stream()
          .filter(trace -> serviceNameList.contains(trace.getServiceName()))
          .filter(trace -> trace.getDuration() != null) // Add a null check for duration
          .sorted(Comparator
                  .comparing(TraceDTO::getDuration, Comparator.reverseOrder()))
          .collect(Collectors.toList());

  return sortedTraces;
}





// filter api sort
// Sort by created time in descending order
public List<TraceDTO> getTraceFilterOrderByCreatedTimeDesc(List<TraceDTO> traceList) {
  return traceList.stream()
      .sorted(Comparator.comparing(TraceDTO::getCreatedTime, Comparator.reverseOrder()))
      .collect(Collectors.toList());
}

// Sort by created time in ascending order
public List<TraceDTO> getTraceFilterAsc(List<TraceDTO> traceList) {
  return traceList.stream()
      .sorted(Comparator.comparing(TraceDTO::getCreatedTime))
      .collect(Collectors.toList());
}

// Sort by error first
public List<TraceDTO> getTraceFilterOrderByErrorFirst(List<TraceDTO> traceList) {
  return traceList.stream()
      .sorted(Comparator
          .comparing((TraceDTO trace) -> {
              Long statusCode = trace.getStatusCode();
              return (statusCode != null && statusCode >= 400 && statusCode <= 599) ? 0 : 1;
          })
          .thenComparing(TraceDTO::getStatusCode, Comparator.nullsLast(Comparator.reverseOrder()))
          .thenComparing(TraceDTO::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder()))
      )
      .collect(Collectors.toList());
}

// Sort by duration in descending order
public List<TraceDTO> getTraceFilterOrderByDuration(List<TraceDTO> traceList) {
  return traceList.stream()
      .filter(trace -> trace.getDuration() != null)
      .sorted(Comparator.comparing(TraceDTO::getDuration, Comparator.reverseOrder()))
      .collect(Collectors.toList());
}







// getByTraceId sort the spans and if some traceId Has same value it will merge the value
public List<Spans> sortingParentChildOrder(List<Spans> spanData) {
  Map<String, List<Spans>> spanTree = new HashMap<>();

  List<Spans> rootSpans = new ArrayList<>();

  for (Spans span : spanData) {
    // String spanId = span.getSpanId();
    String parentId = span.getParentSpanId();
    if (parentId == null || parentId.isEmpty()) {
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
  Map<String, TraceDTO> parentTraces = new HashMap<>();
  Map<String, TraceDTO> childTraces = new HashMap<>();

  for (TraceDTO trace : traces) {
      String traceId = trace.getTraceId();
      boolean hasNullParentSpan = false;

      for (Spans span : trace.getSpans()) {
          if (span.getParentSpanId() == null || span.getParentSpanId().isEmpty()) {
              hasNullParentSpan = true;
              break;
          }
      }

      if (hasNullParentSpan) {
          parentTraces.put(traceId, trace);
      } else {
          childTraces.put(traceId, trace);
      }
  }

  for (TraceDTO parentTrace : parentTraces.values()) {
      String traceId = parentTrace.getTraceId();
      TraceDTO childTrace = childTraces.get(traceId);

      if (childTrace != null) {
          // Merge the spans of the child trace into the parent trace
          parentTrace.getSpans().addAll(childTrace.getSpans());
      }
  }

  // Sort the spans within each merged trace
  for (TraceDTO mergedTrace : parentTraces.values()) {
      mergedTrace.setSpans(sortingParentChildOrder(mergedTrace.getSpans()));
  }

  return new ArrayList<>(parentTraces.values());
}



public List<TraceSpanDTO> getModifiedTraceSpanDTO(List<TraceDTO> mergedTraces) {
  List<TraceSpanDTO> traceSpanDTOList = new ArrayList<>();

  for (TraceDTO trace : mergedTraces) {
      String traceID = trace.getTraceId();
      List<Spans> spans = trace.getSpans();

      List<SpanDTO> spanDTOList = new ArrayList<>();

      for (Spans span : spans) {
          SpanDTO spanDTO = new SpanDTO();
          spanDTO.setSpans(span);

          List<LogDTO> logDTOs = fetchLogDTOsForSpanId(span.getSpanId());

          // Filter the LogDTO objects with severityText "ERROR" or "SEVERE"
          List<LogDTO> matchingLogDTOs = logDTOs.stream()
                  .filter(logDTO -> "ERROR".equals(logDTO.getSeverityText()) || "SEVERE".equals(logDTO.getSeverityText()))
                  .collect(Collectors.toList());
      
          if (!matchingLogDTOs.isEmpty()) {
              spanDTO.setErrorStatus(true);
              spanDTO.setLogAttributes(matchingLogDTOs.stream()
                      .flatMap(logDTO -> extractLogAttributes(logDTO).stream())
                      .collect(Collectors.toList()));
      
              // Set traceId and spanId from the first matching LogRecord (assuming there's at least one)
              LogRecord firstMatchingLogRecord = matchingLogDTOs.get(0).getScopeLogs().get(0).getLogRecords().get(0);
              spanDTO.setLogTraceId(firstMatchingLogRecord.getTraceId());
              spanDTO.setLogSpanId(firstMatchingLogRecord.getSpanId());
              spanDTO.setErrorMessage(firstMatchingLogRecord.getBody()); // Assuming Body is a string
          }
      
          spanDTOList.add(spanDTO);
      }
          

      // Create a new TraceSpanDTO with the same properties but using the modified spanDTOList
      TraceSpanDTO traceSpanDTO = new TraceSpanDTO();

      traceSpanDTO.setTraceId(traceID);
      traceSpanDTO.setServiceName(trace.getServiceName());
      traceSpanDTO.setMethodName(trace.getMethodName());
      traceSpanDTO.setOperationName(trace.getOperationName());
      traceSpanDTO.setDuration(trace.getDuration());
      traceSpanDTO.setStatusCode(trace.getStatusCode());
      traceSpanDTO.setSpanCount(trace.getSpanCount());
      traceSpanDTO.setCreatedTime(trace.getCreatedTime());
      traceSpanDTO.setSpanDTOs(spanDTOList);

      traceSpanDTOList.add(traceSpanDTO);
  }

  return traceSpanDTOList;
}



// Function to fetch LogDTO objects based on spanId (adjust this based on your data source)
public List<LogDTO> fetchLogDTOsForSpanId(String spanId) {
  PanacheQuery<LogDTO> query = logQueryRepo.find("spanId", spanId);
  List<LogDTO> logDTOs = query.list();

  return logDTOs;
}

private List<LogAttribute> extractLogAttributes(LogDTO logDTO) {
  List<LogAttribute> logAttributes = new ArrayList<>();

  List<ScopeLogs> scopeLogs = logDTO.getScopeLogs();
  if (scopeLogs != null) {
      for (ScopeLogs scopeLog : scopeLogs) {
          List<LogRecord> logRecords = scopeLog.getLogRecords();
          if (logRecords != null) {
              for (LogRecord logRecord : logRecords) {
                  // Extract the LogAttributes from the LogRecord's attributes list
                  List<LogAttribute> attributes = logRecord.getAttributes();
                  if (attributes != null) {
                      logAttributes.addAll(attributes);
                  }
              }
          }
      }
  }

  return logAttributes;
}



public List<LogDTO> getErroredLogDTO(List<TraceDTO> mergedTraces) {

  List<LogDTO> matchingLogDTOList = new ArrayList<>();
  for (TraceDTO trace : mergedTraces) {
    String traceID = trace.getTraceId();
    List<Spans> spans = trace.getSpans();
    List<LogDTO> logDTOList = logQueryRepo.find("traceId", traceID).list();
    // System.out.println("--traceID-"+traceID + "SPANS--------"+spans +"-----------logs"+logDTOList);
    // System.out.println("logsDTO-------------------------"+logDTOList);
    for (LogDTO logDTO : logDTOList) {
      for (Spans span : spans) {
          if (logDTO.getSpanId().equals(span.getSpanId())) {
              // Add the matching LogDTO to the list
              matchingLogDTOList.add(logDTO);
              // System.out.println("-----------matchingLogDTOList-----*********---"+matchingLogDTOList);
          }
        }

      }
    }
    List<LogDTO> filteredLogDTOList = matchingLogDTOList.stream()
            .filter(logDTO -> "ERROR".equals(logDTO.getSeverityText()) || "SEVERE".equals(logDTO.getSeverityText()))
            .collect(Collectors.toList());

            System.out.println("----**------filteredLogDTOList--**---"+filteredLogDTOList.size());
  return filteredLogDTOList;
}


public List<TraceDTO> getAllDBTraceDTOs(){
  MongoDatabase database = mongoClient.getDatabase("OtelTrace");
  MongoCollection<TraceDTO> collection = database.getCollection("TraceDTO", TraceDTO.class);

  // Define the aggregation pipeline
  List<Bson> pipeline = Arrays.asList(
      Aggregates.unwind("$spans"),
      Aggregates.match(Filters.regex("spans.attributes.key", "^db", "m"))
  );

  // Execute the aggregation pipeline and retrieve the result
  List<Document> documents = collection.aggregate(pipeline, Document.class).into(new ArrayList<>());

  // Convert the Documents back to TraceDTO objects
  List<TraceDTO> result = new ArrayList<>();
  for (Document document : documents) {
    System.out.println("---------DOCUMENtml: " + document);
      TraceDTO traceDTO = new TraceDTO();
      traceDTO.setTraceId(document.getString("traceId"));
      traceDTO.setServiceName(document.getString("serviceName"));
      traceDTO.setMethodName(document.getString("methodName"));
      Spans spans = new Spans();
      spans.setTraceId(document.getString("traceId"));
      spans.setSpanId(document.getString("spanId"));
      spans.setParentSpanId(document.getString("parentSpanId"));
      spans.setName(document.getString("name"));
      spans.setStartTimeUnixNano(document.getString("startTimeUnixNano"));
      spans.setEndTimeUnixNano(document.getString("endTimeUnixNano"));
      spans.setAttributes(document.get("attributes", List.class)); // Adjust as per your data structure
      spans.setStatus(document.get("status", Map.class)); // Adjust as per your data structure

      // Set the spans field in the TraceDTO
      traceDTO.setSpans(Arrays.asList(spans));
      result.add(traceDTO);
  }

  return result;
}

// public List<TraceDTO> getAllDBTraceMetricCount(List<String> serviceNameList, LocalDate from, LocalDate to, int minutesAgo) {
// //   List<TraceDTO> traceList = traceQueryRepo.listAll();
// //   Map<String, TraceMetrics> metricsMap = new HashMap<>();

// //   Instant fromInstant = null;
// //   Instant toInstant = null;

// //   if (from != null && to != null) {
// //     // If both from and to are provided, consider the date range
// //     Instant startOfFrom = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
// //     Instant startOfTo = to.atStartOfDay(ZoneId.systemDefault()).toInstant();

// //     // Ensure that fromInstant is earlier than toInstant
// //     fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
// //     toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

// //     toInstant = toInstant.plus(1, ChronoUnit.DAYS);
// //   } else if (minutesAgo > 0) {
// //     // If minutesAgo is provided, calculate the time range based on minutesAgo
// //     Instant currentInstant = Instant.now();
// //     Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

// //     // Calculate the start of the current day
// //     Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

// //     // Ensure that fromInstant is later than the start of the current day
// //     if (minutesAgoInstant.isBefore(startOfCurrentDay)) {
// //         fromInstant = startOfCurrentDay;
// //     } else {
// //         fromInstant = minutesAgoInstant;
// //     }

// //     toInstant = currentInstant;
// // } else {
// //     // Handle the case when neither date range nor minutesAgo is provided
// //     throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
// //   }

// // // Filter the traceList based on your date and time criteria
// // List<TraceDTO> filteredTraces = new ArrayList<>();
// // for (TraceDTO traceDTO : traceList) {
// //     if (traceDTO != null) {
// //         Date traceCreateTime = traceDTO.getCreatedTime();
// //         if (traceCreateTime != null) {
// //             Instant traceInstant = traceCreateTime.toInstant();
// //             if (serviceNameList.contains(traceDTO.getServiceName()) &&
// //                 traceInstant.isAfter(fromInstant) &&
// //                 traceInstant.isBefore(toInstant)) {
// //                 filteredTraces.add(traceDTO);
// //                 System.out.println("Filtered data----------"+filteredTraces);
// //             }
// //         }
// //     }
// // }

// // Your MongoDB connection and collection initialization
// MongoDatabase database = mongoClient.getDatabase("OtelTrace");
// MongoCollection<Document> traceCollection = database.getCollection("TraceDTO");

// List<Bson> pipeline = Arrays.asList(
//   Aggregates.unwind("$spans"),
//   Aggregates.match(new Document("spans.attributes.key",
//       new Document("$regex", "^db").append("$options", "m"))
//   )
// );
// AggregateIterable<Document> aggregationResult = traceCollection.aggregate(pipeline);


// List<TraceDTO> matchingTraces = new ArrayList<>();

// for (Document document : aggregationResult) {
//     try {
//         // Create a TraceDTO for each document
//         TraceDTO traceDTO = new TraceDTO();
        
//         // Extract and set relevant data from the document in the TraceDTO object
//         traceDTO.setTraceId(document.getString("traceId"));
//         traceDTO.setServiceName(document.getString("serviceName"));
//         traceDTO.setMethodName(document.getString("methodName"));
//         traceDTO.setOperationName(document.getString("operationName"));
//         traceDTO.setDuration(document.getLong("duration"));
//         traceDTO.setStatusCode(document.getLong("statusCode"));
//         traceDTO.setSpanCount(document.getString("spanCount"));
//         traceDTO.setCreatedTime(document.getDate("createdTime"));

//         List<Spans> spansList = new ArrayList<>();

//         // Extract and map the "spans" field
//         Document spansDocument = document.get("spans", Document.class);
//         if (spansDocument != null) {
//             Spans spans = new Spans();
            
//             // Map the fields for the Spans object
//             spans.setTraceId(spansDocument.getString("traceId"));
//             spans.setSpanId(spansDocument.getString("spanId"));
//             spans.setParentSpanId(spansDocument.getString("parentSpanId"));
//             spans.setName(spansDocument.getString("name"));
//             spans.setKind(spansDocument.getInteger("kind"));
//             spans.setStartTimeUnixNano(spansDocument.getString("startTimeUnixNano"));
//             spans.setEndTimeUnixNano(spansDocument.getString("endTimeUnixNano"));
            
//             // Assuming "attributes" is a list of Documents, you can extract and map it accordingly
//             List<Document> attributesDocuments = spansDocument.getList("attributes", Document.class);
//             List<Attributes> attributesList = new ArrayList<>();
            
//             for (Document attributesDocument : attributesDocuments) {
//                 Attributes attributes = new Attributes();
//                 // Map the attributes fields here
//                 // attributes.setSomeField(attributesDocument.getString("someField"));
//                 // Set other attributes for Attributes
//                 attributesList.add(attributes);
//             }
//             spans.setAttributes(attributesList);
            
//             // Map "status" field (assuming it's a map)
//             Map<String, Object> statusMap = spansDocument.get("status", Map.class);
//             spans.setStatus(statusMap);

//             // Map "events" field (assuming it's a list)
//             List<Document> eventsDocuments = spansDocument.getList("events", Document.class);
//             List<Event> eventsList = new ArrayList<>();
            
//             for (Document eventDocument : eventsDocuments) {
//                 Event event = new Event();
//                 // Map the event fields here
//                 // event.setSomeField(eventDocument.getString("someField"));
//                 // Set other attributes for Event
//                 eventsList.add(event);
//             }
//             spans.setEvents(eventsList);
            
//             traceDTO.setSpans(spansList); 
//         }

//         // Set other attributes based on the document's fields

//         matchingTraces.add(traceDTO);
//     } catch (Exception e) {
//         // Log any exceptions that may occur during extraction
//         System.err.println("Error extracting data: " + e.getMessage());
//     }
// }

// return matchingTraces;
// }






public List<TraceMetrics> getAllTraceMetricCount(List<String> serviceNameList, LocalDate from, LocalDate to, int minutesAgo) {
  List<TraceDTO> traceList = traceQueryRepo.listAll();
  Map<String, TraceMetrics> metricsMap = new HashMap<>();

  Instant fromInstant = null;
  Instant toInstant = null;

  if (from != null && to != null) {
    // If both from and to are provided, consider the date range
    Instant startOfFrom = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant startOfTo = to.atStartOfDay(ZoneId.systemDefault()).toInstant();

    // Ensure that fromInstant is earlier than toInstant
    fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
    toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

    toInstant = toInstant.plus(1, ChronoUnit.DAYS);
  } else if (minutesAgo > 0) {
    // If minutesAgo is provided, calculate the time range based on minutesAgo
    Instant currentInstant = Instant.now();
    Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

    // Calculate the start of the current day
    Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

    // Ensure that fromInstant is later than the start of the current day
    if (minutesAgoInstant.isBefore(startOfCurrentDay)) {
        fromInstant = startOfCurrentDay;
    } else {
        fromInstant = minutesAgoInstant;
    }

    toInstant = currentInstant;
} else {
    // Handle the case when neither date range nor minutesAgo is provided
    throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
  }

  for (TraceDTO traceDTO : traceList) {
    Date traceCreateTime = traceDTO.getCreatedTime();
    if (traceCreateTime == null) {
      // Log or print an error message, including details about the null value
      System.out.println("traceCreateTime is null in getTraceCount method");
      continue;
    }

    Instant traceInstant = traceCreateTime.toInstant();

    if (!serviceNameList.contains(traceDTO.getServiceName()) ||
        !traceInstant.isAfter(fromInstant) ||
        !traceInstant.isBefore(toInstant)) {
      // Skip processing if the service name is not in the specified list
      // or if traceInstant is not within the specified range
      continue;
    }

    String serviceName = traceDTO.getServiceName();
    TraceMetrics metrics = metricsMap.get(serviceName);

    if (metrics == null) {
      metrics = new TraceMetrics();
      metrics.setServiceName(serviceName);
      metrics.setApiCallCount(0L);
      metrics.setPeakLatency(0L);
      metrics.setTotalErrorCalls(0L);
      metrics.setTotalSuccessCalls(0L);
      metricsMap.put(serviceName, metrics);
    }

    calculateTraces(traceDTO, metrics);
  }

  return new ArrayList<>(metricsMap.values());
}

private void calculateTraces(TraceDTO traceDTO, TraceMetrics metrics) {
  if (traceDTO == null) {
    // Log or print an error message, including details about the null value
    System.out.println("traceDTO is null in calculateTraces method");
    return;
  }

  if (traceDTO.getStatusCode() != null) {
    if (traceDTO.getStatusCode() >= 400 && traceDTO.getStatusCode() <= 599) {
      metrics.setTotalErrorCalls(metrics.getTotalErrorCalls() + 1);
    } else if (traceDTO.getStatusCode() >= 200 && traceDTO.getStatusCode() <= 299) {
      metrics.setTotalSuccessCalls(metrics.getTotalSuccessCalls() + 1);
    }
  }

  System.out.println("Before: metrics=" + metrics + ", apiCallCount=" + metrics.getApiCallCount() + ", duration=" + traceDTO.getDuration());

  if (metrics.getApiCallCount() == null) {
    // Log or print an error message if apiCallCount is unexpectedly null
    System.out.println("apiCallCount is unexpectedly null in calculateTraces method");
    return;
  }

  metrics.setApiCallCount(metrics.getTotalErrorCalls() + metrics.getTotalSuccessCalls());

  System.out.println("After: metrics=" + metrics + ", apiCallCount=" + metrics.getApiCallCount() + ", duration=" + traceDTO.getDuration());

  if (traceDTO.getDuration() != null && traceDTO.getDuration() > 500) {
    metrics.setPeakLatency(metrics.getPeakLatency() + 1);
  } else {
    // Log or print a message if statusCode is unexpectedly null
    System.out.println("statusCode is unexpectedly null in calculateTraces method");
  }
}

}
