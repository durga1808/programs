package com.zaga.controller;

import java.util.List;
import com.zaga.entity.kepler.KeplerMetric;
import com.zaga.entity.queryentity.kepler.KeplerMetricQuery;
import com.zaga.handler.KeplerMetricHandler;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/kepler")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KeplerMetricController {
    
    @Inject
    KeplerMetricHandler keplerMetricHandler;

@GET
@Path("/getAllKepler")
@Produces(MediaType.APPLICATION_JSON)
public List<KeplerMetric> getLogMetricsCount(
    ) {
    return keplerMetricHandler.getAllKeplerData();
}

@GET
    @Path("/getKeplerData")
    public List<KeplerMetricQuery> getKeplerByTimedased(
        // @QueryParam("from") LocalDate from,
        // @QueryParam("to") LocalDate to,
        // @QueryParam("minutesAgo") int minutesAgo
    ) {
       return keplerMetricHandler.getKeplerData();
                 
}

}