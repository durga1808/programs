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

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

        List<TraceDTO> traceList = traceQueryHandler.searchTracesPaged(traceQuery,page, pageSize, minutesAgo);

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
  
      if ("error".equalsIgnoreCase(sortOrder)) {
          // Retrieve error traces and count using the new method
          Map<String, Object> errorData = traceQueryHandler.getErrorTracesWithCount(page, pageSize, timeAgoMinutes);
          traces = (List<TraceDTO>) errorData.get("data");
          totalCount = (long) errorData.get("totalCount");
      }
      else if ("peakLatency".equalsIgnoreCase(sortOrder)) {
        // Retrieve peak latency traces and count using the new method
        Map<String, Object> peakLatencyData = traceQueryHandler.getPeakLatencyTraces(page, pageSize, timeAgoMinutes);
        traces = (List<TraceDTO>) peakLatencyData.get("data");
        totalCount = (long) peakLatencyData.get("totalCount");
    }
       else {
          // For other sort orders, use the existing methods
          if ("new".equalsIgnoreCase(sortOrder)) {
              traces = traceQueryHandler.getNewestTraces(page, pageSize, timeAgoMinutes);
          } else if ("old".equalsIgnoreCase(sortOrder)) {
              traces = traceQueryHandler.getOldestTraces(page, pageSize, timeAgoMinutes);
          } else {
              // Default to the existing method for paginated traces
              traces = traceQueryHandler.getPaginatedTraces(page, pageSize, timeAgoMinutes);
          }
  
          // Get the total count for non-error data
          totalCount = traceQueryHandler.getTraceCountInMinutes(timeAgoMinutes);
      }
  
      Map<String, Object> response = new HashMap<>();
      response.put("data", traces);
      response.put("totalCount", totalCount);
  
      ObjectMapper objectMapper = new ObjectMapper();
      String responseJson = objectMapper.writeValueAsString(response);
  
      return Response.ok(responseJson).build();
}
  


//get data by traceId and also have same traceId then merge it as a one
@GET
@Path("/findByTraceId")
public Response findByTraceId(@QueryParam("traceId") String traceId) {
    if (traceId == null || traceId.isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("traceId query parameter is required")
            .build();
    }

    List<TraceDTO> data = traceQueryRepo.find("traceId = ?1", traceId).list();

    if (data.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity("No TraceDTO found for traceId: " + traceId)
            .build();
    }

    List<TraceDTO> dto;
    if (data.size() > 1) {
        dto = traceQueryHandler.mergeTraces(data);
    } else {
        dto = data;
        for (TraceDTO trace : dto) {
            List<Spans> orderedSpanData = traceQueryHandler.sortingParentChildOrder(trace.getSpans());
            trace.setSpans(orderedSpanData);
        }
    }

    for (TraceDTO trace : dto) {
        for (Spans span : trace.getSpans()) {
            System.out.println(
                "Span ID: " + span.getSpanId() + ", Parent Span ID: " + span.getParentSpanId() + ", Name: "
                    + span.getName());
        }
    }

    Map<String, Object> response = new HashMap<>();
    response.put("data", dto); 

    try {
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(response);

        return Response.ok(responseJson).build();
    } catch (Exception e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("Error converting response to JSON")
            .build();
    }
}
}