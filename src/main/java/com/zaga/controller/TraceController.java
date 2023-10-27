package com.zaga.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.oteltrace.scopeSpans.Spans;
import com.zaga.entity.queryentity.log.LogDTO;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.entity.queryentity.trace.TraceMetrics;
import com.zaga.entity.queryentity.trace.TraceQuery;
import com.zaga.entity.queryentity.trace.TraceSpanDTO;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



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
  public Response getAllDetails() {
    try {
      List<TraceDTO> traceList = traceQueryHandler.getSampleTrace();

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
@Path("/findById")
public Response findById(@QueryParam("traceId") String traceId) {
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

    Map<String, Object> response = new HashMap<>();
    response.put("data", data);

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

  
// @POST
// @Path("/TraceQueryFilter")
// public Response queryTraces(
//     TraceQuery traceQuery,
//     @QueryParam("page") @DefaultValue("1") int page,
//     @QueryParam("pageSize") @DefaultValue("10") int pageSize,
//     @QueryParam("from") LocalDate from,
//     @QueryParam("to") LocalDate to,
//     @QueryParam("minutesAgo") int minutesAgo) {
//         System.out.println("from controller: " + from);
//         System.out.println("to controller: " + to);

//         List<TraceDTO> traceList = traceQueryHandler.searchTracesPaged(traceQuery,page, pageSize,from,to,minutesAgo);

//         long totalCount = traceQueryHandler.countQueryTraces(traceQuery,from,to,minutesAgo);

//         Map<String, Object> jsonResponse = new HashMap<>();
//         jsonResponse.put("totalCount", totalCount);
//         jsonResponse.put("data", traceList);

//         ObjectMapper objectMapper = new ObjectMapper();
//         try {
//             String responseJson = objectMapper.writeValueAsString(jsonResponse);
//             return Response.ok(responseJson).build();
//         } catch (JsonProcessingException e) {
//              e.printStackTrace();
//         }

//         return Response.ok(traceList).build();
//     }

@POST
@Path("/TraceQueryFilter")
public Response queryTraces(
    TraceQuery traceQuery,
    @QueryParam("page") @DefaultValue("1") int page,
    @QueryParam("pageSize") @DefaultValue("10") int pageSize,
    @QueryParam("from") LocalDate from,
    @QueryParam("to") LocalDate to,
    @QueryParam("minutesAgo") int minutesAgo,
    @QueryParam("sortOrder") String sortOrder) {
        System.out.println("from controller: " + from);
        System.out.println("to controller: " + to);

        List<TraceDTO> traceList = traceQueryHandler.searchTracesPaged(traceQuery, page, pageSize, from, to, minutesAgo);

        if (sortOrder != null) {
            if ("new".equalsIgnoreCase(sortOrder)) {
                traceList = traceQueryHandler.getTraceFilterOrderByCreatedTimeDesc(traceList);
            } else if ("old".equalsIgnoreCase(sortOrder)) {
                traceList = traceQueryHandler.getTraceFilterAsc(traceList);
            } else if ("error".equalsIgnoreCase(sortOrder)) {
                traceList = traceQueryHandler.getTraceFilterOrderByErrorFirst(traceList);
            } else if ("peakLatency".equalsIgnoreCase(sortOrder)) {
                traceList = traceQueryHandler.getTraceFilterOrderByDuration(traceList);
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid sortOrder parameter. Use 'new', 'old', 'error', 'peakLatency'.")
                    .build();
            }
        }

        long totalCount = traceQueryHandler.countQueryTraces(traceQuery, from, to, minutesAgo);

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("totalCount", totalCount);
        jsonResponse.put("data", traceList);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String responseJson = objectMapper.writeValueAsString(jsonResponse);
            return Response.ok(responseJson).build();
        } catch (JsonProcessingException e) {
             e.printStackTrace();
        }

        return Response.ok(traceList).build();
    }



@GET
@Path("/getErroredDataForLastTwo")
@Produces(MediaType.APPLICATION_JSON)
public Response findErroredDataForLastTwo(
        @QueryParam("page") @DefaultValue("1") int page,
        @QueryParam("pageSize") @DefaultValue("10") int pageSize,
        @QueryParam("serviceName") String serviceName) {

    try {
        List<TraceDTO> traces = traceQueryHandler.findErrorsLastTwoHours(serviceName);

        int totalCount = traces.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        if (startIndex >= endIndex || traces.isEmpty()) {
            Map<String, Object> emptyResponse = new HashMap<>();
            emptyResponse.put("data", Collections.emptyList());
            emptyResponse.put("totalCount", 0);

            return Response.ok(emptyResponse).build();
        }

        List<TraceDTO> erroredData = traces.subList(startIndex, endIndex);

        Map<String, Object> response = new HashMap<>();
        response.put("data", erroredData);
        response.put("totalCount", totalCount);

      ObjectMapper objectMapper = new ObjectMapper();
      String responseJson = objectMapper.writeValueAsString(response);

      return Response.ok(responseJson).build();
    } catch (Exception e) {
      return Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(e.getMessage())
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
    public List<TraceMetrics> getTraceMetricCount(
            @QueryParam("serviceNameList") List<String> serviceNames,
            @QueryParam("from") LocalDate from,
            @QueryParam("to") LocalDate to,
            @QueryParam("minutesAgo") int minutesAgo){
                System.out.println("----------minutesAgo--------------------"+minutesAgo);
        return traceQueryHandler.getAllTraceMetricCount(serviceNames,from,to,minutesAgo);
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

 
    List<TraceSpanDTO> traceDTO = traceQueryHandler.getModifiedTraceSpanDTO(dto);      

    Map<String, Object> response = new HashMap<>();
    response.put("data", traceDTO); 

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
  
  




// @GET
// @Path("/getalldata-sortorder")
// @Produces(MediaType.APPLICATION_JSON)
// public Response sortOrderTrace(
//     @QueryParam("sortOrder") String sortOrder,
//     @QueryParam("page") int page,
//     @QueryParam("pageSize") int pageSize,
//     @QueryParam("from") LocalDate fromDate,
//     @QueryParam("to") LocalDate toDate,
//     @QueryParam("minutesAgo") Integer minutesAgo,
//     @QueryParam("serviceNameList") List<String> serviceNameList) {

//     if (page <= 0 || pageSize <= 0) {
//         return Response.status(Response.Status.BAD_REQUEST)
//                 .entity("Invalid page or pageSize parameters.")
//                 .build();
//     }

//     List<TraceDTO> traces;

//     if ("new".equalsIgnoreCase(sortOrder)) {
//         traces = traceQueryHandler.getAllTracesOrderByCreatedTimeDesc(serviceNameList);
//     } else if ("old".equalsIgnoreCase(sortOrder)) {
//         traces = traceQueryHandler.getAllTracesAsc(serviceNameList);
//     } else if ("error".equalsIgnoreCase(sortOrder)) {
//         traces = traceQueryHandler.findAllOrderByErrorFirst(serviceNameList);
//     } else if ("peakLatency".equalsIgnoreCase(sortOrder)) {
//         traces = traceQueryHandler.findAllOrderByDuration(serviceNameList);
//     } else {
//         return Response.status(Response.Status.BAD_REQUEST)
//                 .entity("Invalid sortOrder parameter. Use 'new', 'old', 'error', 'peakLatency'.")
//                 .build();
//     }

//     if (fromDate != null && toDate != null) {
//         // Swap 'fromDate' and 'toDate' if 'toDate' is earlier than 'fromDate'
//         if (toDate.isBefore(fromDate)) {
//             LocalDate temp = fromDate;
//             fromDate = toDate;
//             toDate = temp;
//         }

//         traces = filterTracesByDateRange(traces, fromDate, toDate);
//     } else if (minutesAgo != null && minutesAgo > 0) {
//         traces = filterTracesByMinutesAgo(traces, minutesAgo);
//     } else {
//         return Response.status(Response.Status.BAD_REQUEST)
//                 .entity("Either fromDate and toDate or minutesAgo must be provided.")
//                 .build();
//     }

//     int startIndex = (page - 1) * pageSize;
//     int endIndex = Math.min(startIndex + pageSize, traces.size());

//     if (startIndex >= endIndex || traces.isEmpty()) {
//         Map<String, Object> emptyResponse = new HashMap<>();
//         emptyResponse.put("data", Collections.emptyList());
//         emptyResponse.put("totalCount", 0);

//         // try {
//         //     ObjectMapper objectMapper = new ObjectMapper();
//         //     String responseJson = objectMapper.writeValueAsString(emptyResponse);

//         //     return Response.ok(responseJson).build();
//         // } catch (Exception e) {
//         //     return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//         //             .entity("Error converting response to JSON")
//         //             .build();
//         // }
//     }

//     List<TraceDTO> paginatedTraces = traces.subList(startIndex, endIndex);
//     int totalCount = traces.size();

//     Map<String, Object> response = new HashMap<>();
//     response.put("data", paginatedTraces);
//     response.put("totalCount", totalCount);
// return Response.ok(paginatedTraces).build();
    
//     // try {
//     //     ObjectMapper objectMapper = new ObjectMapper();
//     //     String responseJson = objectMapper.writeValueAsString(response);

//     //     return Response.ok(responseJson).build();
//     // } catch (Exception e) {
//     //     return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//     //             .entity("Error converting response to JSON")
//     //             .build();
//     // }
// }


// private List<TraceDTO> filterTracesByMinutesAgo(List<TraceDTO> traces, int minutesAgo) {
//     LocalDate today = LocalDate.now(); // Get the current date
//     LocalDateTime fromDateTime = today.atStartOfDay(); // Start of the current day
//     LocalDateTime toDateTime = LocalDateTime.now();

//     Instant currentInstant = Instant.now();
//     Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

//     if (minutesAgoInstant.isBefore(fromDateTime.atZone(ZoneId.systemDefault()).toInstant())) {
//         minutesAgoInstant = fromDateTime.atZone(ZoneId.systemDefault()).toInstant();
//     }

//     return filterTracesByDateTimeRange(traces, fromDateTime, toDateTime);
// }

// private List<TraceDTO> filterTracesByDateRange(List<TraceDTO> traces, LocalDate fromDate, LocalDate toDate) {
//     LocalDateTime fromDateTime = fromDate.atStartOfDay();
//     LocalDateTime toDateTime = toDate.atTime(LocalTime.MAX);

//     return filterTracesByDateTimeRange(traces, fromDateTime, toDateTime);
// }

// private List<TraceDTO> filterTracesByDateTimeRange(List<TraceDTO> traces, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
//     return traces.stream()
//             .filter(trace -> {
//                 Date createdTime = trace.getCreatedTime();
//                 if (createdTime != null) {
//                     LocalDateTime traceDateTime = createdTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//                     return !traceDateTime.isBefore(fromDateTime) && !traceDateTime.isAfter(toDateTime);
//                 }
//                 return false; 
//             })
//             .collect(Collectors.toList());
// }

@GET
@Path("/getalldata-sortorder")
@Produces(MediaType.APPLICATION_JSON)
public Response sortOrderTrace(
    @QueryParam("sortOrder") String sortOrder,
    @QueryParam("page") int page,
    @QueryParam("pageSize") int pageSize,
    @QueryParam("from") LocalDate fromDate,
    @QueryParam("to") LocalDate toDate,
    @QueryParam("minutesAgo") Integer minutesAgo,
    @QueryParam("serviceNameList") List<String> serviceNameList) {
    if (page <= 0 || pageSize <= 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid page or pageSize parameters.")
                .build();
    }
    List<TraceDTO> traces;
    if ("new".equalsIgnoreCase(sortOrder)) {
        traces = traceQueryHandler.getAllTracesOrderByCreatedTimeDesc(serviceNameList);
    } else if ("old".equalsIgnoreCase(sortOrder)) {
        traces = traceQueryHandler.getAllTracesAsc(serviceNameList);
    } else if ("error".equalsIgnoreCase(sortOrder)) {
        traces = traceQueryHandler.findAllOrderByErrorFirst(serviceNameList);
    } else if ("peakLatency".equalsIgnoreCase(sortOrder)) {
        traces = traceQueryHandler.findAllOrderByDuration(serviceNameList);
    } else {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid sortOrder parameter. Use 'new', 'old', 'error', 'peakLatency'.")
                .build();
    }
    if (fromDate != null && toDate != null) {
        // Swap 'fromDate' and 'toDate' if 'toDate' is earlier than 'fromDate'
        if (toDate.isBefore(fromDate)) {
            LocalDate temp = fromDate;
            fromDate = toDate;
            toDate = temp;
        }
        traces = filterTracesByDateRange(traces, fromDate, toDate);
    } else if (minutesAgo != null && minutesAgo > 0) {
        traces = filterTracesByMinutesAgo(traces, minutesAgo);
    } else {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Either fromDate and toDate or minutesAgo must be provided.")
                .build();
    }
    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, traces.size());
    if (startIndex >= endIndex || traces.isEmpty()) {
        Map<String, Object> emptyResponse = new HashMap<>();
        emptyResponse.put("data", Collections.emptyList());
        emptyResponse.put("totalCount", 0);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String responseJson = objectMapper.writeValueAsString(emptyResponse);
            return Response.ok(responseJson).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error converting response to JSON")
                    .build();
        }
    }
    List<TraceDTO> paginatedTraces = traces.subList(startIndex, endIndex);
    int totalCount = traces.size();
    Map<String, Object> response = new HashMap<>();
    response.put("data", paginatedTraces);
    response.put("totalCount", totalCount);
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

private List<TraceDTO> filterTracesByMinutesAgo(List<TraceDTO> traces, int minutesAgo) {
    Instant currentInstant = Instant.now();
    Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

    LocalDateTime fromDateTime = minutesAgoInstant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime toDateTime = LocalDateTime.now();

    return filterTracesByDateTimeRange(traces, fromDateTime, toDateTime);
}   
  
private List<TraceDTO> filterTracesByDateRange(List<TraceDTO> traces, LocalDate fromDate, LocalDate toDate) {
    LocalDateTime fromDateTime = fromDate.atStartOfDay();
    LocalDateTime toDateTime = toDate.atTime(LocalTime.MAX);
    return filterTracesByDateTimeRange(traces, fromDateTime, toDateTime);
}
private List<TraceDTO> filterTracesByDateTimeRange(List<TraceDTO> traces, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    return traces.stream()
            .filter(trace -> {
                Date createdTime = trace.getCreatedTime();
                if (createdTime != null) {
                    LocalDateTime traceDateTime = createdTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    return !traceDateTime.isBefore(fromDateTime) && !traceDateTime.isAfter(toDateTime);
                }
                return false; 
            })
            .collect(Collectors.toList());
}

}