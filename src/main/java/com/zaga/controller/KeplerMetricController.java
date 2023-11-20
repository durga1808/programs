package com.zaga.controller;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import com.zaga.entity.kepler.KeplerMetric;
import com.zaga.entity.queryentity.kepler.KeplerMetricDTO;
import com.zaga.entity.queryentity.kepler.KeplerMetricQuery;
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
    @Path("/getAllKepler-MetricData")
    public List<KeplerMetricDTO> getAllKeplerMetricDatas( 
        @QueryParam("from") LocalDate from,
        @QueryParam("to") LocalDate to,
        @QueryParam("minutesAgo") int minutesAgo) {
        
    List<KeplerMetricDTO> keplerMetricData = keplerMetricHandler.getAllKeplerByDateAndTime(from, to, minutesAgo);

    System.out.println("Number of records: " + keplerMetricData.size());

    return keplerMetricData;
    }
    


}