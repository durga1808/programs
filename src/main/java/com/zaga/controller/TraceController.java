package com.zaga.controller;

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
  public List<TraceDTO> getDetails() {
    return traceQueryHandler.getTraceProduct();
  }

  @POST
  @Path("/TraceQueryFilter")
  public List<TraceDTO> queryTraces(TraceQuery traceQuery) {
    return traceQueryHandler.searchTraces(traceQuery);
  }

  @GET
  @Path("/getAllDataByPagination")
  public Response findRecentData(
    @QueryParam("page") int page,
    @QueryParam("pageSize") int pageSize
  ) {
    try {
      long totalCount = traceQueryHandler.countData();
    //   long totalPages = (long) Math.ceil((double) totalCount / pageSize);
      List<TraceDTO> recentData = traceQueryHandler.findRecentDataPaged(
        page,
        pageSize
      );

      Map<String, Object> jsonResponse = new HashMap<>();
      jsonResponse.put("totalCount", totalCount);
      jsonResponse.put("data", recentData);

    //   ObjectMapper objectMapper = new ObjectMapper();
    //   String responseJson = objectMapper.writeValueAsString(jsonResponse);

      return Response.ok(jsonResponse).build();
    } catch (Exception e) {
      return Response
        .status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(e.getMessage())
        .build();
    }
  }

  @GET
  @Path("/getAllDataByServiceNameAndStatusCode")
  public List<TraceDTO> findRecentDataPaged(
          @QueryParam("page") @DefaultValue("1") int page,
          @QueryParam("pageSize") @DefaultValue("10") int pageSize,
          @QueryParam("serviceName") String serviceName,
          @QueryParam("statusCode") @DefaultValue("0") int statusCode) {

      // Call your service method to retrieve the data
      List<TraceDTO> traceList = traceQueryHandler.findRecentDataPaged(page, pageSize, serviceName, statusCode);

      return traceList;
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

}
