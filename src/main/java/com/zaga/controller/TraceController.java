package com.zaga.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.entity.queryentity.trace.TraceQuery;
import com.zaga.handler.TraceQueryHandler;
import com.zaga.repo.TraceQueryRepo;

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
    TraceQueryHandler traceQueryHandler;

    @Inject
    TraceQueryRepo traceQueryRepo;
   
    
    
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


// @GET
// @Path("/merged-spans")
// public List<TraceDTO> getMergedSpanData() {
//     return traceQueryHandler.getMergedSpanData();
// }


@POST
@Path("/TraceQuery")
public List<TraceDTO> queryTraces(TraceQuery traceQuery) {
        return traceQueryHandler.searchTraces(traceQuery);
}

@GET
@Path("/recent")
public Response findRecentData(
        @QueryParam("serviceName") String serviceName,
        @QueryParam("page") int page,
        @QueryParam("pageSize") int pageSize) {

    try {
        long totalCount = traceQueryHandler.countData(serviceName);
        long totalPages = (long) Math.ceil((double) totalCount / pageSize);

        List<TraceDTO> recentData = traceQueryHandler.findRecentDataPaged(serviceName, page, pageSize);

        // Construct the JSON response manually
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("totalCount", totalCount);
        jsonResponse.put("data", recentData);

        // Create an ObjectMapper to serialize the response
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(jsonResponse);

        return Response.ok(responseJson).build();
    } catch (Exception e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
}

@GET
@Path("/count")
@Produces(MediaType.APPLICATION_JSON)
public Map<String, Long> getTraceCount() {
        return traceQueryHandler.getTraceCountWithinHour();
  
}


@GET
@Path("/countbyparam")
@Produces(MediaType.APPLICATION_JSON)
public Map<String, Long> getTraceCountForServiceName(@QueryParam("timeAgo") int timeAgoHours) {
        return traceQueryHandler.getTraceCountWithinHour();
  
}
   
    @GET
    @Path("/error-counts")
    public Map<String, Long> getErrorCounts() {
        return traceQueryHandler.calculateErrorCountsByService();
    }
    
}
    

