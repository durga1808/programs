package com.zaga.controller;

import com.zaga.entity.metrics.MetricMain;
import com.zaga.service.MetricService;


import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MetricController {

    @Inject
    MetricService metricService;
   
    @POST
    @Path("/create")
    public Response createProduvct(MetricMain metric) {
        try {
            //System.out.println("----------------");
            metricService.createProduct(metric);
            return Response.status(200).entity(metric).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }
}
