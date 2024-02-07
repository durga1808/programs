package com.zaga.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.queryentity.podMetrics.PodMetricsResponseData;
import com.zaga.handler.PodMetricsHandler;
import com.zaga.repo.PodMetricDTORepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/podMetrics")
public class PodMetricController {
  
    @Inject
    PodMetricDTORepo podMetricRepo;




    @Inject
    PodMetricsHandler podMetricsHandler;

    @GET
    @Path("/getAllPodMetrics")
    public Response getAllPodMetrics(
            @QueryParam("from") LocalDate from,
            @QueryParam("to") LocalDate to,
            @QueryParam("page") int page,
        @QueryParam("pageSize") int pageSize,
        @QueryParam("minutesAgo")int minutesAgo
    ) throws JsonProcessingException {
        LocalDateTime APICallStart = LocalDateTime.now();

        System.out.println("------------API call startTimestamp------ " + APICallStart);

        List<PodMetricsResponseData> podMetricsData = podMetricsHandler.getAllPodMetricsByDate(
                from,
                to,
                page,
                pageSize,
                minutesAgo
        );

        String responseJson = "";
        ObjectMapper objectMapper = new ObjectMapper();
        responseJson = objectMapper.writeValueAsString(podMetricsData);

        try {
            LocalDateTime APICallEnd = LocalDateTime.now();

            System.out.println("------------API call endTimestamp------ " + APICallEnd);

            System.out.println("-----------API call duration------- " +
                    (Duration.between(APICallStart, APICallEnd)));

            return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error converting response to JSON")
                    .build();
        }
    }
}


//     @GET
//     @Produces(MediaType.APPLICATION_JSON)
//     public void mergePodMetrics() {
//         Document groupStage = new Document(
//                 "$group",
//                 new Document("_id", "$podName")
//                         .append("podName", new Document("$first", "$podName"))
//                         .append("metrics",
//                                 new Document("$push",
//                                         new Document("cpuUsage",
//                                                 new Document("$arrayElemAt", Arrays.asList("$metrics.cpuUsage", 0L)))
//                                                 .append("date",
//                                                         new Document("$arrayElemAt", Arrays.asList("$metrics.date", 0L)))
//                                                 .append("memoryUsage",
//                                                         new Document("$arrayElemAt", Arrays.asList("$metrics.memoryUsage", 0L)))
//                                 )
//                         )
//         );

//         Document mergeStage = new Document(
//                 "$merge",
//                 new Document("into", "mergedPodMetrics")
//                         .append("whenMatched", "merge")
//                         .append("whenNotMatched", "insert")
//         );

//         podMetricsCollection.aggregate(Arrays.asList(groupStage, mergeStage));
//     }
// }


