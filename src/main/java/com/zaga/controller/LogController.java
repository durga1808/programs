package com.zaga.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.zaga.entity.queryentity.log.LogDTO;
import com.zaga.entity.queryentity.log.LogMetrics;
import com.zaga.entity.queryentity.log.LogQuery;
import com.zaga.handler.LogQueryHandler;
import com.zaga.repo.LogQueryRepo;

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

@Path("/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogController {
    
    @Inject
    LogQueryRepo repo;

    @Inject
    LogQueryHandler logQueryHandler;

    @Inject
    MongoClient mongoClient;

@GET
@Path("/getAllDataByServiceName")
public Response getAllDataByServiceName(
    @QueryParam("page") @DefaultValue("1") int page,
    @QueryParam("pageSize") @DefaultValue("10") int pageSize,
    @QueryParam("serviceName") String serviceName) {

    try {
        List<LogDTO> logRecords = logQueryHandler.getLogsByServiceName(serviceName, page, pageSize);
        
        long totalCount = logQueryHandler.getTotalLogCountByServiceName(serviceName);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        jsonResponse.put("totalCount", totalCount);
        jsonResponse.set("data", objectMapper.valueToTree(logRecords));

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
    @Path("/getAllLogDataByPagination")
    public Response getAllLogDataByPagination(
        
        @QueryParam("page") int page,  @QueryParam("pageSize") int pageSize) {
        try {
            long totalCount = logQueryHandler.countLogRecords(); // Use the new method.

            List<LogDTO> logData = logQueryHandler.findLogDataPaged(page, pageSize);

            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("totalCount", totalCount);
            jsonResponse.put("data", logData);

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
    @Path("/findByTraceId")
    public Response findByTraceId(@QueryParam("traceId") String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("traceId query parameter is required")
                .build();
        }
    List<LogDTO> data = repo.find("traceId=?1", traceId).list();
    if (data.isEmpty()) {
        // Return an empty array if no LogDTO is found
        return Response.ok(new ArrayList<>()).build();
    }

    try {
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(data);

        return Response.ok(responseJson).build();
    } catch (Exception e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error converting response to JSON")
                .build();
    }
}


  @GET
  @Path("/LogSumaryChartDataCount")
  @Produces(MediaType.APPLICATION_JSON)
  public List<LogMetrics> getLogMetricsCount(@QueryParam("timeAgoMinutes") @DefaultValue("60") int timeAgoMinutes, @QueryParam("serviceNameList") List<String> serviceNameList) {
    return logQueryHandler.getLogMetricCount(timeAgoMinutes, serviceNameList);
  }


@GET
@Path("/getallLogdata-sortorder")
@Produces(MediaType.APPLICATION_JSON)
public Response sortOrderTrace(
    @QueryParam("sortOrder") String sortOrder,
    @QueryParam("page") int page,
    @QueryParam("pageSize") int pageSize,
    @QueryParam("minutesAgo") int minutesAgo, @QueryParam("serviceNameList") List<String> serviceNameList) {

      if (page <= 0 || pageSize <= 0 || minutesAgo < 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid page, pageSize, or minutesAgo parameters.")
                .build();
    }
    List<LogDTO> logs;
        if ("new".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getAllLogssOrderByCreatedTimeDesc(serviceNameList);
          } else if ("old".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getAllLogssAsc(serviceNameList);
          }  
          else if ("error".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getErrorLogsByServiceNamesOrderBySeverityAndCreatedTimeDesc(serviceNameList);
          }
          else {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid sortOrder parameter. Use 'new', 'old'.")
                .build();
    }

    if (minutesAgo > 0) {
    Date cutoffDate = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(minutesAgo));
    logs = logs.stream()
    .filter(log -> {
        Date createdTime = log.getCreatedTime();
        return createdTime != null && createdTime.after(cutoffDate);
    })
    .collect(Collectors.toList());
}

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, logs.size());

if (startIndex >= endIndex || logs.isEmpty()) {
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

  List<LogDTO> paginatedTraces = logs.subList(startIndex, endIndex);
    int totalCount = logs.size();

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



@POST
@Path("/LogFilterQuery")
public Response filterLogs(
        LogQuery logQuery,
        @QueryParam("page") @DefaultValue("1") int page,
        @QueryParam("pageSize") @DefaultValue("10") int pageSize,
        @QueryParam("minutesAgo") @DefaultValue("60") int minutesAgo) {

    if (page <= 0 || pageSize <= 0 || minutesAgo < 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid page, pageSize, or minutesAgo parameters.")
                .build();
    }

    List<LogDTO> logs = logQueryHandler.searchlogPaged(logQuery, page, pageSize, minutesAgo);

    if (minutesAgo > 0) {
        Date cutoffDate = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(minutesAgo));
        logs = logs.stream()
                .filter(log -> {
                    Date createdTime = log.getCreatedTime();
                    return createdTime != null && createdTime.after(cutoffDate);
                })
                .collect(Collectors.toList());
    }

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, logs.size());

    if (startIndex >= endIndex || logs.isEmpty()) {
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

    Map<String, Object> response = new HashMap<>();
    response.put("data", logs.subList(startIndex, endIndex));
    response.put("totalCount", logs.size());

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



    @GET
    @Path("/getErroredLogDataForLastTwo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getErroredLogDataForLastTwo(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("serviceName") String serviceName) {

        try {
            List<LogDTO> logList = logQueryHandler.findByMatching(serviceName);

            int totalCount = logList.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalCount);

            if (startIndex >= endIndex || logList.isEmpty()) {
                Map<String, Object> emptyResponse = new HashMap<>();
                emptyResponse.put("data", Collections.emptyList());
                emptyResponse.put("totalCount", 0);

                return Response.ok(emptyResponse).build();
            }

            List<LogDTO> paginatedLogs = logList.subList(startIndex, endIndex);

            Map<String, Object> response = new HashMap<>();
            response.put("data", paginatedLogs);
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
    @Path("/searchFunction")
    public Response searchLogs(
            @QueryParam("minutesAgo") @DefaultValue("60") int minutesAgo,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("keyword") String keyword) {

        try {
            List<LogDTO> logList = logQueryHandler.searchLogs(keyword);

            if (minutesAgo > 0) {
                Date cutoffDate = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(minutesAgo));
                logList = logList.stream()
                        .filter(log -> {
                            Date createdTime = log.getCreatedTime();
                            return createdTime != null && createdTime.after(cutoffDate);
                        })
                        .collect(Collectors.toList());
            }

            int totalCount = logList.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalCount);

            if (startIndex >= endIndex || logList.isEmpty()) {
                Map<String, Object> emptyResponse = new HashMap<>();
                emptyResponse.put("data", Collections.emptyList());
                emptyResponse.put("totalCount", 0);

                return Response.ok(emptyResponse).build();
            }

            List<LogDTO> paginatedLogs = logList.subList(startIndex, endIndex);

            Map<String, Object> response = new HashMap<>();
            response.put("data", paginatedLogs);
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

}