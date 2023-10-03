package com.zaga.controller;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zaga.entity.queryentity.log.LogDTO;
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

@GET
@Path("/getAllDataByServiceName")
public Response getAllDataByServiceName(
    @QueryParam("page") @DefaultValue("1") int page,
    @QueryParam("pageSize") @DefaultValue("10") int pageSize,
    @QueryParam("serviceName") String serviceName) {

    try {
        // Call your service method to retrieve the data
        List<LogDTO> logRecords = logQueryHandler.getLogsByServiceName(serviceName, page, pageSize);
        
        // Get the total count
        long totalCount = logQueryHandler.getTotalLogCountByServiceName(serviceName);

        // Create an ObjectMapper to serialize the JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        jsonResponse.put("totalCount", totalCount);
        // Serialize the logRecords list to JSON
        jsonResponse.set("data", objectMapper.valueToTree(logRecords));

        // Convert the JSON response to a string and return it as the response
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
        @POST
        @Path("/LogQueryfilter")
        public Response queryLog(
            LogQuery logQuery,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("minutesAgo") @DefaultValue("60") int minutesAgo) {
            try {
                // Replace traceQuery with logQuery and trace-related functions with log-related ones.
                List<LogDTO> logList = logQueryHandler.searchLogsPaged(logQuery, page, pageSize, minutesAgo);
        
                long totalCount = logQueryHandler.countQueryLogs(logQuery, minutesAgo);
        
                Map<String, Object> jsonResponse = new HashMap<>();
                jsonResponse.put("totalCount", totalCount);
                jsonResponse.put("data", logList);
        
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
    @Path("/findByTraceId")
    public Response findByTraceId(@QueryParam("traceId") String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("traceId query parameter is required")
                .build();
        }
    List<LogDTO> data = repo.find("traceId=?1", traceId).list();
    if (data.isEmpty()){
        return Response.status(Response.Status.NOT_FOUND)
           .entity("No LogDTO found for traceId: " + traceId)
           .build();
    }
    return Response.status(200).entity(data).build();
}


@GET
@Path("/getallLogdata-sortorder")
@Produces(MediaType.APPLICATION_JSON)
public Response sortOrderTrace(
    @QueryParam("sortOrder") String sortOrder,
    @QueryParam("page") int page,
    @QueryParam("pageSize") int pageSize,
    @QueryParam("minutesAgo") int minutesAgo) {

      if (page <= 0 || pageSize <= 0 || minutesAgo < 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid page, pageSize, or minutesAgo parameters.")
                .build();
    }
    List<LogDTO> logs;
        if ("new".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getAllTracesOrderByCreatedTimeDesc();
          } else if ("old".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getAllTracesAsc();
          } else {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid sortOrder parameter. Use 'new', 'old', or 'error'.")
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
}