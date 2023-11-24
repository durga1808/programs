package com.zaga.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.queryentity.kepler.KeplerMetricDTO;
import com.zaga.entity.queryentity.kepler.Response.ContainerPowerMetrics;
import com.zaga.entity.queryentity.kepler.Response.KeplerResponseData;
import com.zaga.handler.KeplerMetricHandler;
import com.zaga.repo.KeplerMetricRepo;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/kepler")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KeplerMetricController {

  @Inject
  KeplerMetricHandler keplerMetricHandler;

  @Inject
  KeplerMetricRepo keplerMetricRepo;



  @POST
  @Path("/addKeplerMock")
  public KeplerMetricDTO addKeplerMetricDTO(
    @RequestBody KeplerMetricDTO keplerMetricDTO
  ) {
    keplerMetricRepo.persist(keplerMetricDTO);
    return keplerMetricDTO;
  }

  @GET
  @Path("/getByTimeMock")
  public Response getKeplerMetricByTime(
    @QueryParam("minutesAgo") Integer minutesAgo
  ) {
    Instant currentInstant = Instant.now();
    Instant minutesAgoInstant = currentInstant.minus(
      minutesAgo,
      ChronoUnit.MINUTES
    );

    List<KeplerMetricDTO> resDto = keplerMetricRepo
      .find("date >= ?1", minutesAgoInstant)
      .list();

    List<String> uniqueServiceNamesList = new ArrayList<>();

    for (KeplerMetricDTO metricEntry : resDto) {
      if (!uniqueServiceNamesList.contains(metricEntry.getServiceName())) {
        uniqueServiceNamesList.add(metricEntry.getServiceName());
      }
    }

    List<KeplerResponseData> finalResponse = new ArrayList<>();

    // Find By ServiceName from response
    for (String serviceName : uniqueServiceNamesList) {
      List<ContainerPowerMetrics> containerPowerMetricsList = new ArrayList<>();
      for (KeplerMetricDTO entry : resDto) {
        if (entry.getServiceName().equals(serviceName)) {
          ContainerPowerMetrics containerPowerMetrics = new ContainerPowerMetrics(
            entry.getDate(),
            entry.getPowerConsumption()
          );
          containerPowerMetricsList.add(containerPowerMetrics);
        }
      }
      KeplerResponseData keplerResponseData = new KeplerResponseData(
        serviceName,
        containerPowerMetricsList
      );
      finalResponse.add(keplerResponseData);
    }

    return Response.ok(finalResponse).build();
  }


 @GET
  @Path("/getAllKepler-MetricData")
  public Response getAllKeplerMetricDatas(
    @QueryParam("from") LocalDate from,
    @QueryParam("to") LocalDate to,
    @QueryParam("minutesAgo") int minutesAgo,
    @QueryParam("type") String type,
    @QueryParam("keplerType") List<String> keplerTypeList
  ) throws JsonProcessingException {
    LocalDateTime APICallStart = LocalDateTime.now();

    System.out.println(
      "------------API call startTimestamp------ " + APICallStart
    );

    List<KeplerResponseData> keplerMetricData = keplerMetricHandler.getAllKeplerByDateAndTime(
      from,
      to,
      minutesAgo,
      type,
      keplerTypeList
    );
      String responseJson = "";
      ObjectMapper objectMapper = new ObjectMapper();
      responseJson = objectMapper.writeValueAsString(keplerMetricData);

    

    try {
      LocalDateTime APICallEnd = LocalDateTime.now();

      System.out.println(
        "------------API call endTimestamp------ " + APICallEnd
      );

      System.out.println(
        "-----------API call duration------- " +
        (Duration.between(APICallStart, APICallEnd))
      );

      return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
    } catch (Exception e) {
      return Response
        .status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity("Error converting response to JSON")
        .build();
    }
  }


//   @GET
//   @Path("/getAllKepler-MetricData")
//   public Response getAllKeplerMetricDatas(
//     @QueryParam("from") LocalDate from,
//     @QueryParam("to") LocalDate to,
//     @QueryParam("minutesAgo") int minutesAgo,
//     @QueryParam("type") String type,
//     @QueryParam("keplerType") List<String> keplerTypeList
//   ) throws JsonProcessingException {
//     LocalDateTime APICallStart = LocalDateTime.now();

//     System.out.println(
//       "------------API call startTimestamp------ " + APICallStart
//     );

//     List<KeplerMetricDTO> keplerMetricData = keplerMetricHandler.getAllKeplerByDateAndTime(
//       from,
//       to,
//       minutesAgo,
//       type,
//       keplerTypeList
//     );

//     List<String> uniqueServiceNamesList = new ArrayList<>();
//     List<String> matchedSystemEntries = new ArrayList<>();
//     List<KeplerResponseData> finalResponse = new ArrayList<>();
//     String responseJson = "";

//     if (keplerMetricData.size() > 0) {
//       for (KeplerMetricDTO metricEntry : keplerMetricData) {
//         if (!uniqueServiceNamesList.contains(metricEntry.getServiceName())) {
//           uniqueServiceNamesList.add(metricEntry.getServiceName());
//         }
//       }

//       // Identify all items starting with "system" and store their indices
//       for (int i = 0; i < uniqueServiceNamesList.size(); i++) {
//         if (uniqueServiceNamesList.get(i).startsWith("system")) {
//           matchedSystemEntries.add(uniqueServiceNamesList.get(i));
//         }
//       }

//       // Move all matched entries to the front of the list
//       for (String matchedEntry : matchedSystemEntries) {
//         uniqueServiceNamesList.remove(matchedEntry);
//         uniqueServiceNamesList.add(0, matchedEntry);
//       }

//       // Find By ServiceName from response
//       for (String serviceName : uniqueServiceNamesList) {
//         List<ContainerPowerMetrics> containerPowerMetricsList = new ArrayList<>();
//         int totalCount = 0; 
//         int countForService = 0; // Counter variable
    
//         for (KeplerMetricDTO entry : keplerMetricData) {
//             if (entry.getServiceName().equals(serviceName)) {
//                 ContainerPowerMetrics containerPowerMetrics = new ContainerPowerMetrics(
//                         entry.getDate(),
//                         entry.getPowerConsumption()
//                 );
//                 containerPowerMetricsList.add(containerPowerMetrics);
//                 countForService++; // Increment the counter
//             }
//         }
    
//         // Print the count for the current serviceName
//         System.out.println("Count for " + serviceName + ": " + countForService);
    
        
//         KeplerResponseData keplerResponseData = new KeplerResponseData(
//           serviceName,
//           containerPowerMetricsList
//         );
//         finalResponse.add(keplerResponseData);

//           // Increment the total count
//     totalCount += countForService;
    
//       System.out.println("--------------Total Count:----------- " + totalCount);
//       }
//       ObjectMapper objectMapper = new ObjectMapper();
//       responseJson = objectMapper.writeValueAsString(finalResponse);

//     }

//     try {
//       LocalDateTime APICallEnd = LocalDateTime.now();

//       System.out.println(
//         "------------API call endTimestamp------ " + APICallEnd
//       );

//       System.out.println(
//         "-----------API call duration------- " +
//         (Duration.between(APICallStart, APICallEnd))
//       );

//       return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
//     } catch (Exception e) {
//       return Response
//         .status(Response.Status.INTERNAL_SERVER_ERROR)
//         .entity("Error converting response to JSON")
//         .build();
//     }
//   }





}
