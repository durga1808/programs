package com.zaga.controller;

import java.util.List;

import com.zaga.entity.queryentity.metric.MetricDTO;
import com.zaga.handler.MetricQueryHandler;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MetricController {

  

    @Inject
    MetricQueryHandler metricQueryHandler;
   
    @GET
    @Path("/getAllMetricData")
    public List<MetricDTO> getAllMetricDatas() {
        return metricQueryHandler.getAllMetricData();
    }
  

}
