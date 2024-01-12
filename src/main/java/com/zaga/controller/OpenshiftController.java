package com.zaga.controller;

import com.zaga.handler.cloudPlatform.LoginHandler;

import io.fabric8.openshift.client.OpenShiftClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/openshift")
public class OpenshiftController {
    
    
    @Inject
    private LoginHandler loginHandler;

    private OpenShiftClient authenticatedClient;

    @GET
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public String login(
        @QueryParam("username") String username,
        @QueryParam("password") String password,
        @QueryParam("oauthToken") String oauthToken, 
        @QueryParam("useOAuthToken") boolean useOAuthToken,
        @QueryParam("clusterUrl") String clusterUrl
    ) {
        authenticatedClient = loginHandler.login(username, password, oauthToken, useOAuthToken,clusterUrl);
        return (authenticatedClient != null) ? "Login successful!" : "Login failed";
    }   

   
    @GET
    @Path("/listAllProjects")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllProjects() {
        return loginHandler.listAllServices(authenticatedClient);
    }


    @POST
    @Path("/instrument/{namespace}/{deploymentName}")
    public Response instrumentDeployment(
        @PathParam(value = "namespace") String namespace,
         @PathParam(value = "deploymentName") String deploymentName) {
            loginHandler.instrumentDeployment(authenticatedClient, namespace, deploymentName);
        return Response.ok("Deployment instrumented successfully.").build();
    }

    @POST
    @Path("/unInstrument/{namespace}/{deploymentName}")
    public Response unInstrumentDeployment(
        @PathParam(value = "namespace") String namespace,
         @PathParam(value = "deploymentName") String deploymentName) {
            loginHandler.unInstrumentDeployment(authenticatedClient, namespace, deploymentName);
        return Response.ok("Deployment instrumented successfully.").build();
    }
    


    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public String logout() {
        loginHandler.logout(authenticatedClient);
        return "Logged out successfully!";
    }


}
