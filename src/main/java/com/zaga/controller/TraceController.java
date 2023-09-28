package com.zaga.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  public Response queryTraces(TraceQuery traceQuery) {
    try {
      List<TraceDTO> traceList = traceQueryHandler.searchTraces(traceQuery);

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

  @GET
  @Path("/getAllDataByPagination")
  public Response findRecentData(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize) {
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
      // Call your service method to retrieve the data
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
  @Path("/countbyparam")
  @Produces(MediaType.APPLICATION_JSON)
  public List<TraceMetrics> getTraceMetricsForServiceNameInMinutes(@QueryParam("timeAgoMinutes") int timeAgoMinutes) {
    return traceQueryHandler.getTraceMetricsForServiceNameInMinutes(timeAgoMinutes);
  }

  @GET
  @Path("/getalldata-paginated-in-minute")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPaginatedTraces(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("timeAgoMinutes") int timeAgoMinutes) throws JsonProcessingException {
    List<TraceDTO> traces = traceQueryHandler.getPaginatedTraces(page, pageSize, timeAgoMinutes);
    long totalCount = traceQueryHandler.getTraceCountInMinutes(timeAgoMinutes);

    Map<String, Object> response = new HashMap<>();
    response.put("data", traces);
    response.put("totalCount", totalCount);

    ObjectMapper objectMapper = new ObjectMapper();
    String responseJson = objectMapper.writeValueAsString(response);

    return Response.ok(responseJson).build();
  }
}
