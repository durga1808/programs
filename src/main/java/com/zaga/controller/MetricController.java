package com.zaga.controller;

import java.util.List;

import org.bson.Document;

import com.zaga.entity.otelmetric.OtelMetric;
import com.zaga.handler.MetricQueryHandler;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MetricController {

  

    @Inject
    MetricQueryHandler metricQueryHandler;
   
  

    // @GET
    // public List<Map<String, Object>> getMetricsByServiceName(@QueryParam("serviceName") String serviceName) {
    //     return metricQueryHandler.getMetricsByServiceName(serviceName);
    // }

    @GET
    @Path("/getByServiceName")
    public List<Document> getMetricsByServiceName(@QueryParam("serviceName") String serviceName){
        return metricQueryHandler.getMetricsByServiceName(serviceName);
    }
}
