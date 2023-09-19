package com.zaga.controller;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.handler.command.TraceCommandHandler;
import com.zaga.handler.query.TraceQueryHandler;


import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/traces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class TraceController {

    @Inject
    TraceCommandHandler traceCommandHandler;

    @Inject
    TraceQueryHandler traceQueryHandler;
   
    @POST
    @Path("/create")
    public Response createProduvct(OtelTrace trace) {
        try {
            //System.out.println("----------------");
            traceCommandHandler.createTraceProduct(trace);
            return Response.status(200).entity(trace).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("/getByServiceName")
    public List<Document> getTraceByServiceName(@QueryParam("serviceName") String serviceName){
        return traceQueryHandler.getTraceByServiceName(serviceName);
    }         
    
    @GET
    @Path("/getByStatuscode")
    public List<Document> getTraceByStatusCode(@QueryParam("statusCode") Integer statusCode){
        return traceQueryHandler.getTraceByStatusCode(statusCode);
    }

    @GET
    @Path("/getByHttpMethod")
    public List<Document> getTraceByHttpMethod(@QueryParam("httpMethod") String httpMethod){
        return traceQueryHandler.getTraceByHttpMethod(httpMethod);
    }

    @GET
    @Path("/getServiceNameToHttpMethod")
    public List<Document> getServiceNameToHttpMethod(@QueryParam("serviceName") String serviceName, @QueryParam("httpMethod") String httpMethod){
        return traceQueryHandler.getTraceByServiceNameAndHttpMethod(serviceName, httpMethod);
    }

    @GET
    @Path("/getServiceNameToStatusCode")
    public List<Document> getServiceNameToStatusCode(@QueryParam("serviceName") String serviceName, @QueryParam("statusCode") Integer statusCode){
        return traceQueryHandler.getTraceByServiceNameAndStatusCode(serviceName, statusCode);
    }


    @GET
    @Path("/getByMultipleStatusCode")
    public List<Document> getTracesByStatusCodes(
    @QueryParam("statusCodes") List<Integer> statusCodes,
    @QueryParam("statusCodeOne") Integer statusCodeOne,
    @QueryParam("statusCodeTwo") Integer statusCodeTwo,
    @QueryParam("statusCodeFour") Integer statusCodeFour) {

    List<Integer> allStatusCodes = new ArrayList<>();

    // Add the status codes from query parameters to the list
    if (statusCodes != null) {
        allStatusCodes.addAll(statusCodes);
    }
    if (statusCodeOne != null) {
        allStatusCodes.add(statusCodeOne);
    }
    if (statusCodeTwo != null) {
        allStatusCodes.add(statusCodeTwo);
    }
    if (statusCodeFour != null) {
        allStatusCodes.add(statusCodeFour);
    }

    return traceQueryHandler.getTraceByMultipleStatusCodes(allStatusCodes);
}


@GET
@Path("/getTraceData")
public List<TraceDTO> getDetails(){
        return traceQueryHandler.getTraceProduct();
    }
}
