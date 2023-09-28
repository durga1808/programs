package com.zaga.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.oteltrace.scopeSpans.Spans;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.entity.queryentity.trace.TraceMetrics;
import com.zaga.entity.queryentity.trace.TraceQuery;
import com.zaga.handler.TraceQueryHandler;
import com.zaga.repo.TraceQueryRepo;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

@Path("/traces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TraceController {

  @Inject
  TraceQueryHandler traceQueryHandler;

  @Inject
  TraceQueryRepo traceQueryRepo;

  @GET
  @Path("/getAllTraceData")
  public Response getDetails() {
    try {
      List<TraceDTO> traceList = traceQueryHandler.getTraceProduct();

      ObjectMapper objectMapper = new ObjectMapper();
      String responseJson = objectMapper.writeValueAsString(traceList);

      return Response.ok(responseJson).build();
    } catch (Exception e) {

      e.printStackTrace();
      return Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("An error occurred: " + e.getMessage())
          .build();
    }
  }

@POST
@Path("/TraceQueryFilter")
public Response queryTraces(
    TraceQuery traceQuery,
    @QueryParam("page") @DefaultValue("1") int page,
    @QueryParam("pageSize") @DefaultValue("10") int pageSize,
    @QueryParam("minutesAgo") @DefaultValue("60") int minutesAgo) {
    try {
        int offset = (page - 1) * pageSize;

        List<TraceDTO> traceList = traceQueryHandler.searchTracesPaged(traceQuery,offset, pageSize, minutesAgo);

        long totalCount = traceQueryHandler.countQueryTraces(traceQuery,minutesAgo);

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("totalCount", totalCount);
        jsonResponse.put("data", traceList);

        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(jsonResponse);

        return Response.ok(responseJson).build();
    } catch (Exception e) {
        e.printStackTrace();

        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("An error occurred: " + e.getMessage())
            .build();
    }
}



  @GET
  @Path("/getAllDataByPagination")
  public Response findRecentData(
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
    try {
      long totalCount = traceQueryHandler.countData();
      // long totalPages = (long) Math.ceil((double) totalCount / pageSize);
      List<TraceDTO> recentData = traceQueryHandler.findRecentDataPaged(
          page,
          pageSize);

      Map<String, Object> jsonResponse = new HashMap<>();
      jsonResponse.put("totalCount", totalCount);
      jsonResponse.put("data", recentData);

      ObjectMapper objectMapper = new ObjectMapper();
      String responseJson = objectMapper.writeValueAsString(jsonResponse);

      return Response.ok(responseJson).build();
    } catch (Exception e) {
      return Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(e.getMessage())
          .build();
    }
  }



  @GET
  @Path("/getAllDataByServiceNameAndStatusCode")
  public Response findRecentDataPaged(
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("pageSize") @DefaultValue("10") int pageSize,
      @QueryParam("serviceName") String serviceName,
      @QueryParam("statusCode") @DefaultValue("0") int statusCode) {

    try {
      long totalCount = traceQueryHandler.countData();
      List<TraceDTO> traceList = traceQueryHandler.findByServiceNameAndStatusCode(page, pageSize, serviceName,
          statusCode);

      Map<String, Object> jsonResponse = new HashMap<>();
      jsonResponse.put("totalCount", totalCount);
      jsonResponse.put("data", traceList);

      ObjectMapper objectMapper = new ObjectMapper();
      String responseJson = objectMapper.writeValueAsString(jsonResponse);

      return Response.ok(responseJson).build();
    } catch (Exception e) {
      e.printStackTrace();

      return Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("An error occurred: " + e.getMessage())
          .build();
    }
  }

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Long> getTraceCount() {
    return traceQueryHandler.getTraceCountWithinHour();
  }

  @GET
  @Path("/TraceSumaryChartDataCount")
  @Produces(MediaType.APPLICATION_JSON)
  public List<TraceMetrics> getTraceMetricsCount(@QueryParam("timeAgoMinutes") @DefaultValue("60") int timeAgoMinutes) {
    return traceQueryHandler.getTraceMetricCount(timeAgoMinutes);
  }




  @GET
@Path("/getalldata-paginated-in-minute")
@Produces(MediaType.APPLICATION_JSON)
public Response getPaginatedTraces(
    @QueryParam("page") int page,
    @QueryParam("pageSize") int pageSize,
    @QueryParam("timeAgoMinutes") int timeAgoMinutes,
    @QueryParam("sortOrder") String sortOrder // Add a new query parameter for sorting
) throws JsonProcessingException {
    List<TraceDTO> traces;
    long totalCount = 0L; // Initialize total count to 0

    if ("new".equalsIgnoreCase(sortOrder)) {
        traces = traceQueryHandler.getNewestTraces(page, pageSize, timeAgoMinutes);
        totalCount = traceQueryHandler.getTraceCountInMinutes(timeAgoMinutes);
    } else if ("old".equalsIgnoreCase(sortOrder)) {
        traces = traceQueryHandler.getOldestTraces(page, pageSize, timeAgoMinutes);
        totalCount = traceQueryHandler.getTraceCountInMinutes(timeAgoMinutes);
    } else if ("error".equalsIgnoreCase(sortOrder)) {
        // Retrieve error traces and count using the new method
        Map<String, Object> errorData = traceQueryHandler.getErrorTracesWithCount(page, pageSize, timeAgoMinutes);
        traces = (List<TraceDTO>) errorData.get("data");
        totalCount = (long) errorData.get("totalCount");
    } else {
        // Default to the existing method for paginated traces
        traces = traceQueryHandler.getPaginatedTraces(page, pageSize, timeAgoMinutes);
        totalCount = traceQueryHandler.getTraceCountInMinutes(timeAgoMinutes);
    }

    Map<String, Object> response = new HashMap<>();
    response.put("data", traces);
    response.put("totalCount", totalCount);

    ObjectMapper objectMapper = new ObjectMapper();
    String responseJson = objectMapper.writeValueAsString(response);

    return Response.ok(responseJson).build();
}

public static List<Spans> sortingParentChildOrder(List<Spans> spanData) {
    // Create a dictionary to map parent spans to their child spans
    Map<String, List<Spans>> spanTree = new HashMap<>();

    // Create a list to hold the root (master) spans
    List<Spans> rootSpans = new ArrayList<>();

    // Build the parent-child relationship and identify root spans
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

    // Sort the spans based on "spanId" and "parentSpanId"
    List<Spans> sortedSpans = new ArrayList<>();

    // Sort child spans for each root span
    for (Spans rootSpan : rootSpans) {
      sortSpans(rootSpan, spanTree, sortedSpans);
    }

    return sortedSpans;
  }

  private static void sortSpans(Spans span, Map<String, List<Spans>> spanTree, List<Spans> sortedSpans) {
    sortedSpans.add(span);
    List<Spans> childSpans = spanTree.get(span.getSpanId());
    if (childSpans != null) {
      for (Spans childSpan : childSpans) {
        sortSpans(childSpan, spanTree, sortedSpans);
      }
    }
  }

  // Method to merge spans with the same traceId
  public static List<TraceDTO> mergeTraces(List<TraceDTO> traces) {
    Map<String, TraceDTO> traceMap = new HashMap<>();

    for (TraceDTO trace : traces) {
      String traceId = trace.getTraceId();

      // Check if the traceId already exists in the map
      if (traceMap.containsKey(traceId)) {
        // Merge the spans into the existing trace
        System.out.println("CONTAINES SAME------------------------------------------------ " + traceId);
        TraceDTO existingTrace = traceMap.get(traceId);
        existingTrace.getSpans().addAll(trace.getSpans());
      } else {
        // If traceId doesn't exist, add the trace to the map
        traceMap.put(traceId, trace);
      }
    }

    // Convert the map values (merged traces) back to a list
    return new ArrayList<>(traceMap.values());
  }

  @GET
  @Path("/findByTraceIdTest")
  public Response rearrangeSpans() {
    ObjectId id = new ObjectId("6513fcb3e75a3b5dc4237b06");

    // Assuming you have a method to retrieve a single TraceDTO by ID
    // TraceDTO data = traceQueryRepo.findById(id);

    // If you are searching by traceId, use "traceId" instead of "id"
    List<TraceDTO> data = traceQueryRepo.find("traceId = ?1", "2d6847588ce29c7f7cb4b07bea1f888a").list();

    // Merge spans
    List<TraceDTO> dto = mergeTraces(data);

    // Reorder spans based on parent-child relationships
    for (TraceDTO trace : dto) {
      List<Spans> orderedSpanData = sortingParentChildOrder(trace.getSpans());
      trace.setSpans(orderedSpanData);
    }

    // Print the ordered span data (assuming you have a Spans class with appropriate
    // getters)
    for (TraceDTO trace : dto) {
      for (Spans span : trace.getSpans()) {
        System.out.println(
            "Span ID: " + span.getSpanId() + ", Parent Span ID: " + span.getParentSpanId() + ", Name: "
                + span.getName());
      }
    }

    return Response.ok(dto).build();
}

  
}
