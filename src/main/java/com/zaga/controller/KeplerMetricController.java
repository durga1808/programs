package com.zaga.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.kepler.KeplerMetric;
import com.zaga.entity.queryentity.kepler.KeplerMetricDTO;
import com.zaga.entity.queryentity.kepler.KeplerMetricQuery;
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

@Path("/kepler")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KeplerMetricController {

    @Inject
    KeplerMetricHandler keplerMetricHandler;

    @Inject
    KeplerMetricRepo keplerMetricRepo;

    // @GET
    // @Path("/getAllKepler")
    // @Produces(MediaType.APPLICATION_JSON)
    // public List<KeplerMetric> getLogMetricsCount() {
    // return keplerMetricHandler.getAllKeplerData();
    // }

    @GET
    @Path("/getKeplerData")
    public List<KeplerMetricQuery> getKeplerByTimedased(
    // @QueryParam("from") LocalDate from,
    // @QueryParam("to") LocalDate to,
    // @QueryParam("minutesAgo") int minutesAgo
    ) {
        return keplerMetricHandler.getKeplerData();
    }

    @POST
    @Path("/addKeplerMock")
    public KeplerMetricDTO addKeplerMetricDTO(@RequestBody KeplerMetricDTO keplerMetricDTO) {
        keplerMetricRepo.persist(keplerMetricDTO);
        return keplerMetricDTO;
    }

    @GET
    @Path("/getByTimeMock")
    public Response getKeplerMetricByTime(@QueryParam("minutesAgo") Integer minutesAgo) {
        Instant currentInstant = Instant.now();
        Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

        List<KeplerMetricDTO> resDto = keplerMetricRepo.find("date >= ?1", minutesAgoInstant).list();

        List<String> uniqueServiceNamesList = new ArrayList<>();

        for (KeplerMetricDTO metricEntry : resDto) {
            if (!uniqueServiceNamesList.contains(metricEntry.getServiceName())) {
                uniqueServiceNamesList.add(metricEntry.getServiceName());
            }
        }

        for (String serviceName : uniqueServiceNamesList) {
            System.out.println(serviceName);
        }

        System.out.println("=====================================================");

        List<KeplerResponseData> finalResponse = new ArrayList<>();

        // Find By ServiceName from response
        for (String serviceName : uniqueServiceNamesList) {
            List<ContainerPowerMetrics> containerPowerMetricsList = new ArrayList<>();
            for (KeplerMetricDTO entry : resDto) {
                if (entry.getServiceName().equals(serviceName)) {
                    ContainerPowerMetrics containerPowerMetrics = new ContainerPowerMetrics(entry.getDate(),
                            entry.getPowerConsumption());
                    containerPowerMetricsList.add(containerPowerMetrics);
                }
            }
            KeplerResponseData keplerResponseData = new KeplerResponseData(serviceName, containerPowerMetricsList);
            finalResponse.add(keplerResponseData);
        }

        System.out.println("Final output " + finalResponse);
        return Response.ok(finalResponse).build();

    }

    @GET
    @Path("/getAllKepler-MetricData")
    public Response getAllKeplerMetricDatas(
            @QueryParam("from") LocalDate from,
            @QueryParam("to") LocalDate to,
            @QueryParam("minutesAgo") int minutesAgo) {

        List<KeplerMetricDTO> keplerMetricData = keplerMetricHandler.getAllKeplerByDateAndTime(from, to, minutesAgo);

        System.out.println("+++++++++++++++++++++++++++Number of records: " + keplerMetricData.size());

        List<String> uniqueServiceNamesList = new ArrayList<>();

        for (KeplerMetricDTO metricEntry : keplerMetricData) {
            if (!uniqueServiceNamesList.contains(metricEntry.getServiceName())) {
                uniqueServiceNamesList.add(metricEntry.getServiceName());
            }
        }

        for (String serviceName : uniqueServiceNamesList) {
            System.out.println(serviceName);
        }

        System.out.println("=====================================================");

        List<KeplerResponseData> finalResponse = new ArrayList<>();

        // Find By ServiceName from response
        for (String serviceName : uniqueServiceNamesList) {
            List<ContainerPowerMetrics> containerPowerMetricsList = new ArrayList<>();
            for (KeplerMetricDTO entry : keplerMetricData) {
                if (entry.getServiceName().equals(serviceName)) {
                    ContainerPowerMetrics containerPowerMetrics = new ContainerPowerMetrics(entry.getDate(),
                            entry.getPowerConsumption());
                    containerPowerMetricsList.add(containerPowerMetrics);
                }
            }
            KeplerResponseData keplerResponseData = new KeplerResponseData(serviceName, containerPowerMetricsList);
            finalResponse.add(keplerResponseData);
        }

        System.out.println("Final output " + finalResponse);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String responseJson = objectMapper.writeValueAsString(finalResponse);

            return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error converting response to JSON")
                    .build();
        }
    }

}