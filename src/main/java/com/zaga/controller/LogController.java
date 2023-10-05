package com.zaga.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.BsonRegularExpression;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.zaga.entity.otellog.ScopeLogs;
import com.zaga.entity.queryentity.log.LogDTO;
import com.zaga.entity.queryentity.log.LogMetrics;
import com.zaga.entity.queryentity.log.LogQuery;
import com.zaga.handler.LogQueryHandler;
import com.zaga.repo.LogQueryRepo;

import io.quarkus.mongodb.panache.PanacheQuery;
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
        return Response.status(Response.Status.OK)
                .entity(new ArrayList<>())
                .build();
    }

    // Return the actual data if found
    return Response.status(Response.Status.OK).entity(data).build();
}


 @GET
  @Path("/LogSumaryChartDataCount")
  @Produces(MediaType.APPLICATION_JSON)
  public List<LogMetrics> getLogMetricsCount(@QueryParam("timeAgoMinutes") @DefaultValue("60") int timeAgoMinutes) {
    return logQueryHandler.getLogMetricCount(timeAgoMinutes);

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
        logs = logQueryHandler.getAllLogssOrderByCreatedTimeDesc();
          } else if ("old".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getAllLogssAsc();
          }  
          else if ("error".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getAllErrorLogsOrderBySeverityAndCreatedTimeDesc();
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



// @POST
// @Path("/LogQueryFilter")
// public Response queryLogs(
//     LogQuery logQuery,
//     @QueryParam("page") @DefaultValue("1") int page,
//     @QueryParam("pageSize") @DefaultValue("10") int pageSize,
//     @QueryParam("minutesAgo") @DefaultValue("60") int minutesAgo) {
//     try {
//         List<LogDTO> logList = logQueryHandler.searchLogsPaged(logQuery, page, pageSize, minutesAgo);
//         long totalCount = logQueryHandler.countQueryLogs(logQuery, minutesAgo);
        
//         Map<String, Object> jsonResponse = new HashMap<>();
//         jsonResponse.put("totalCount", totalCount);
//         jsonResponse.put("data", logList);
        
//         ObjectMapper objectMapper = new ObjectMapper();
//         String responseJson = objectMapper.writeValueAsString(jsonResponse);
        
//         return Response.ok(responseJson).build();
//     } catch (Exception e) {
//         e.printStackTrace();
//         return Response
//             .status(Response.Status.INTERNAL_SERVER_ERROR)
//             .entity("An error occurred: " + e.getMessage())
//             .build();
//     }
// }

@POST
@Path("/LogFilterQuery")
public Response filterLogs(
        LogQuery logQuery,
        @QueryParam("page") @DefaultValue("1") int page,
        @QueryParam("pageSize") @DefaultValue("10") int pageSize,
        @QueryParam("minutesAgo") @DefaultValue("60") int minutesAgo) {

    // Validate parameters
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

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal Server Error")
                    .build();
        }
    }


    // @GET
    // @Path("/search")
    // public Response search(@QueryParam("keyword") String keyword) {
    //     if (keyword == null || keyword.isEmpty()) {
    //         return Response.status(Response.Status.BAD_REQUEST)
    //            .entity("keyword query parameter is required")
    //            .build();
    //     }

    // List<LogDTO> data = repo.find("keyword=?1", keyword).list();
    // System.out.println(data);
    // if (data.isEmpty()){
    //     return Response.status(Response.Status.NOT_FOUND)
    //       .entity("No LogDTO found for keyword: " + keyword)
    //       .build();
    // }
    // return Response.status(200).entity(data).build();


    // }
   

    // @GET
    // @Path("/search")
    // public List<LogDTO> searchLogs(@QueryParam("keyword") String keyword) {
    //     List<LogDTO> results = new ArrayList<>();
    //     String regexPattern = ".*" + Pattern.quote(keyword) + ".*";
    //     BsonRegularExpression regex = new BsonRegularExpression(regexPattern, "i");

    //     try {
    //         MongoCollection<Document> collection = mongoClient
    //                 .getDatabase("OtelLog")
    //                 .getCollection("LogDTO");

    //         Document query = new Document("$or", List.of(
    //             new Document("serviceName", regex),
    //             new Document("traceId", regex),
    //             new Document("spanId", regex),
    //             new Document("severityText", regex)
    //         ));

    //         MongoCursor<Document> cursor = collection.find(query).iterator();

    //         while (cursor.hasNext()) {
    //             Document document = cursor.next();
    //             LogDTO logDTO = mapDocumentToLogDTO(document);
    //             results.add(logDTO);
    //         }
    //     } catch (Exception e) {
    //         // Handle any exceptions or errors
    //     }

    //     return results;
    // }

    // // Helper method to map a Document to LogDTO
    // private LogDTO mapDocumentToLogDTO(Document document) {
    //     LogDTO logDTO = new LogDTO();
    //     logDTO.setServiceName(document.getString("serviceName"));
    //     logDTO.setTraceId(document.getString("traceId"));
    //     logDTO.setSpanId(document.getString("spanId"));
    //     logDTO.setCreatedTime(document.getDate("createdTime"));
    //     logDTO.setSeverityText(document.getString("severityText"));
        
    //     // Map the scopeLogs field
    //     List<Document> scopeLogsDocuments = (List<Document>) document.get("scopeLogs");
    //     if (scopeLogsDocuments != null) {
    //         List<ScopeLogs> scopeLogsList = new ArrayList<>();
    //         for (Document scopeLogsDocument : scopeLogsDocuments) {
    //             // Map the scopeLogsDocument to ScopeLogs if needed
    //             // Example: ScopeLogs scopeLogs = mapScopeLogsDocumentToScopeLogs(scopeLogsDocument);
    //             // Add scopeLogs to the scopeLogsList
    //         }
    //         logDTO.setScopeLogs(scopeLogsList);
    //     }
        
    //     // Set other fields as needed
    //     return logDTO;
    // }


   
    @GET
    @Path("/searchFunction")
    public List<LogDTO> searchLogs(@QueryParam("keyword") String keyword) {
        return logQueryHandler.searchLogs(keyword);
    }
}