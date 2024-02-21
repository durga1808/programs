package com.zaga.handler.cloudPlatform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zaga.entity.queryentity.openshift.ClusterNetwork;
import com.zaga.entity.queryentity.openshift.ServiceList;
import com.zaga.repo.ServiceListRepo;

import io.fabric8.kubernetes.api.model.ComponentCondition;
import io.fabric8.kubernetes.api.model.ComponentStatus;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.storage.StorageClassList;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.config.v1.ClusterVersion;
import io.fabric8.openshift.api.model.config.v1.InfrastructureList;
import io.fabric8.openshift.client.OpenShiftClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class OpenshiftLoginHandler  implements LoginHandler{

     @Inject
    ServiceListRepo serviceListRepo;

    @Inject
    ClusterNetwork clusterNetwork;

    public OpenShiftClient login(String username, String password, String oauthToken, boolean useOAuthToken, String clusterUrl) {
        try {
            KubernetesClient kubernetesClient;
    
            if (useOAuthToken) {
                kubernetesClient = new KubernetesClientBuilder()       //"https://api.zagaopenshift.zagaopensource.com:6443"
                    .withConfig(new ConfigBuilder()
                        .withOauthToken(oauthToken)
                        .withMasterUrl(clusterUrl)
                        .withTrustCerts(true) 
                        .build())
                    .build();
            } else {
                kubernetesClient = new KubernetesClientBuilder()
                    .withConfig(new ConfigBuilder()
                        .withPassword(password)
                        .withUsername(username)
                        .withMasterUrl(clusterUrl)
                        .withTrustCerts(true) 
                        .build())
                    .build();
            }
    
            OpenShiftClient openShiftClient = kubernetesClient.adapt(OpenShiftClient.class);
    
            // Attempt to list OpenShift projects as a verification step
            if (isLoginSuccessful(openShiftClient)) {
                logSuccess("Login successful");
                return openShiftClient;
            } else {
                System.out.println("Login failed. Invalid credentials or insufficient permissions.");
                return null;
            }
        } catch (Exception e) {
            logError("Error while logging in", e);
            return null;
        }
    } 
    private boolean isLoginSuccessful(OpenShiftClient openShiftClient) {
        // Attempt to list OpenShift projects or perform other verification steps
        try {
            openShiftClient.projects().list();
            return true; // Login is successful
        } catch (KubernetesClientException e) {
            logError("Error while verifying login: " + e.getMessage(), e);
            return false; // Login is unsuccessful
        }
    }
    
    private void logError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }
    
    private void logSuccess(String message) {
        System.out.println(message);
    }
    




 @Override
 public Response listAllServices(OpenShiftClient authenticatedClient) {
    System.out.println("clientservices"+authenticatedClient);
        if (authenticatedClient != null) {
            try {
                OpenShiftClient openShiftClient = authenticatedClient.adapt(OpenShiftClient.class);

                NamespaceList namespaceList = openShiftClient.namespaces().list();
                List<ServiceList> deploymentsInfoList = new ArrayList<>();

                for (Namespace namespace : namespaceList.getItems()) {
                    String namespaceName = namespace.getMetadata().getName();

                    DeploymentList deploymentList = openShiftClient.apps().deployments().inNamespace(namespaceName).list();

                    for (Deployment deployment : deploymentList.getItems()) {
                        String serviceName = deployment.getMetadata().getLabels().get("app");

                        String deploymentName = deployment.getMetadata().getName();


                        Map<String, String> annotations = deployment.getSpec().getTemplate().getMetadata().getAnnotations();
                        String injectJavaValue = annotations.get("instrumentation.opentelemetry.io/inject-java");

                         // Extracting createdTime from metadata
                        String createdTime = deployment.getMetadata().getCreationTimestamp();


                        if (injectJavaValue == null) {
                            injectJavaValue = "false";
                        }

                        ServiceList serviceList = new ServiceList();
                        serviceList.setNamespaceName(namespaceName);  // Correct usage of setNamespaceName
                        serviceList.setServiceName(serviceName);
                        serviceList.setInstrumented(injectJavaValue);
                        serviceList.setDeploymentName(deploymentName);
                        serviceList.setCreatedTime(createdTime);

                        // serviceListRepo.persist(serviceList);

                        deploymentsInfoList.add(serviceList);
                    }
                }

                return Response.ok(deploymentsInfoList).build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("You are unauthorized to do this action.")
                        .build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("You are not logged in.")
                    .build();
        }
    }


    @Override
    public void instrumentDeployment(OpenShiftClient authenticatedClient,String namespace, String deploymentName) {
        System.out.println("namespace: " + namespace);
        System.out.println("deploymentName: " + deploymentName);
        System.out.println("client: " + authenticatedClient);
        if (authenticatedClient !=null && namespace != null && deploymentName != null) {
            try {
                OpenShiftClient openShiftClient = authenticatedClient.adapt(OpenShiftClient.class);
                
                System.out.print("authern----------"+authenticatedClient);

                Deployment deployment = openShiftClient.apps().deployments()
                        .inNamespace(namespace)
                        .withName(deploymentName)
                        .get();

                if (deployment != null) {

                    Map<String, String> annotations = deployment.getSpec().getTemplate().getMetadata().getAnnotations();
                    System.out.println("---------Before instrumentation: ---------" + annotations.get("instrumentation.opentelemetry.io/inject-java"));
                    annotations.put("instrumentation.opentelemetry.io/inject-java", "true");
                    System.out.println("--------------After instrumentation:------- " + annotations.get("instrumentation.opentelemetry.io/inject-java"));

                    openShiftClient.apps().deployments()
                            .inNamespace(namespace)
                            .withName(deploymentName)
                            .patch(deployment);
                } else {
                    System.out.println("Deployment not found in namespace: " + namespace + ", deploymentName: " + deploymentName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid parameters");
        }
    }



    
    @Override
        public void unInstrumentDeployment(OpenShiftClient authenticatedClient,String namespace, String deploymentName) {
        System.out.println("namespace: " + namespace);
        System.out.println("deploymentName: " + deploymentName);
        System.out.println("client: " + authenticatedClient);
        if (authenticatedClient !=null && namespace != null && deploymentName != null) {
            try {
                OpenShiftClient openShiftClient = authenticatedClient.adapt(OpenShiftClient.class);
                
                System.out.print("authern----------"+authenticatedClient);

                Deployment deployment = openShiftClient.apps().deployments()
                        .inNamespace(namespace)
                        .withName(deploymentName)
                        .get();

                if (deployment != null) {

                    Map<String, String> annotations = deployment.getSpec().getTemplate().getMetadata().getAnnotations();
                    System.out.println("---------Before instrumentation: ---------" + annotations.get("instrumentation.opentelemetry.io/inject-java"));
                    annotations.put("instrumentation.opentelemetry.io/inject-java", "false");
                    System.out.println("--------------After instrumentation:------- " + annotations.get("instrumentation.opentelemetry.io/inject-java"));

                    openShiftClient.apps().deployments()
                            .inNamespace(namespace)
                            .withName(deploymentName)
                            .patch(deployment);
                } else {
                    System.out.println("Deployment not found in namespace: " + namespace + ", deploymentName: " + deploymentName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid parameters");
        }
    }

    @Override
    public String logout(OpenShiftClient authenticatedClient) {
        if (authenticatedClient != null) {
            try {
                // Perform OpenShift-specific logout actions, e.g., token revocation or session clearing
                // authenticatedClient.logout();
                // or equivalent OpenShift API calls
            } catch (Exception e) {
                e.printStackTrace();
                return "Logout failed"; // Handle the exception as needed
            } finally {
                authenticatedClient.close(); // Close the client
            }
        }
        return "Logged out successfully!";
    }
    @Override
    public Response viewClusterInfo(OpenShiftClient authenticatedClient) {

        if (authenticatedClient == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("You are not logged in.")
                    .build();
        }
        else{
            OpenShiftClient openShiftClient = authenticatedClient.adapt(OpenShiftClient.class); 
        try {
            List<ClusterVersion>  clusterInfo = openShiftClient.config().clusterVersions().list().getItems();
            System.out.println(clusterInfo);
            List<Map<String,String>> clusterListInfo = new ArrayList<>();
            for (ClusterVersion clusterVersion : clusterInfo) {
                Gson gson = new Gson();
                JsonElement jsonElement = gson.toJsonTree(clusterVersion);
                JsonObject jsonObject = (JsonObject) jsonElement.getAsJsonObject().get("spec");
                // System.out.println(jsonElement.getAsJsonObject().get("kind"));
                System.out.println("clusterID " + jsonObject.get("clusterID"));
                System.out.println("Channel " + jsonObject.get("channel"));
                JsonObject jsonObject2 = (JsonObject) jsonElement.getAsJsonObject().get("status");
                System.out.println("Version " + jsonObject2.get("desired").getAsJsonObject().get("version"));
                Map<String, String> clusterInfoMap = new HashMap<>();
                clusterInfoMap.put("clusterID", jsonObject.get("clusterID").getAsString());
                clusterInfoMap.put("channel " , jsonObject.get("channel").getAsString());
                clusterInfoMap.put("version " , jsonObject2.get("desired").getAsJsonObject().get("version").getAsString());
                clusterListInfo.add(clusterInfoMap);
   }
        return Response.ok(clusterListInfo).build();            } 
        catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("You are unauthorized to do this action.")
                    .build();
        }
        }
        
}

    @Override
    public Response viewClusterCondition(OpenShiftClient authenticatedClient) {
        if (authenticatedClient == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("You are not logged in.")
                    .build();
        }
        else{
            
            OpenShiftClient openShiftClient = authenticatedClient.adapt(OpenShiftClient.class);

            try {
            List<ComponentStatus> clusterStatus = openShiftClient.componentstatuses().list().getItems();

            List<Map<String, String>> clusterListInfo = new ArrayList<>();

            for (ComponentStatus componentStatus : clusterStatus) {
                String componentName = componentStatus.getMetadata().getName();

                List<String> types = new ArrayList<>();
                for (ComponentCondition condition : componentStatus.getConditions()) {
                    types.add(condition.getType());
                }

                Map<String, String> clusterMap = new HashMap<>();
                clusterMap.put("name", componentName);
                clusterMap.put("condition", String.join(", ", types)); 

                // Adding the map to the list
                clusterListInfo.add(clusterMap);
            }

            Gson gson = new Gson();
            String jsonOutput = gson.toJson(clusterListInfo);

            System.out.println(jsonOutput);

            return Response.ok(jsonOutput).build();   }
            catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("You are unauthorized to do this action.")
                        .build();
            }
            }

    
}
    @Override
    public Response viewClusterInventory(OpenShiftClient authenticatedClient) {
        if (authenticatedClient == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("You are not logged in.")
                    .build();
        }
        else{
            try{
                OpenShiftClient openShiftClient = authenticatedClient.adapt(OpenShiftClient.class);
                NodeList nodeList = openShiftClient.nodes().list();
                Gson nodeGson = new Gson();
                JsonElement nodeJsonElement = nodeGson.toJsonTree(nodeList);
                JsonArray nodeJsonArray = nodeJsonElement.getAsJsonObject().get("items").getAsJsonArray();
                Integer nodeCount = nodeJsonArray.size();
                System.out.println(nodeCount);
                PersistentVolumeClaimList pvc = openShiftClient.persistentVolumeClaims().inAnyNamespace().list();
                Gson pvcGson = new Gson();
                JsonElement pvcJsonElement = pvcGson.toJsonTree(pvc);
                JsonArray pvcArray = pvcJsonElement.getAsJsonObject().get("items").getAsJsonArray();
                System.out.println(pvcArray.size());
                Integer pvcCount = pvcArray.size();
                PodList podList = openShiftClient.pods().inAnyNamespace().list();
                Gson podGson = new Gson();
                JsonElement podJsonElement = podGson.toJsonTree(podList);
                JsonArray podArray = podJsonElement.getAsJsonObject().get("items").getAsJsonArray();
                System.out.println(podArray.size());
                Integer PodCount = podArray.size();
                StorageClassList storage = openShiftClient.storage().storageClasses().list();
                Gson gson = new Gson();
                JsonElement jsonElement = gson.toJsonTree(storage);

                JsonArray jsonArray = jsonElement.getAsJsonObject().get("items").getAsJsonArray();
                System.out.println(jsonArray.size());
                Integer StorageClass = jsonArray.size();
                List<Map<String, Integer>> clusterInventory = new ArrayList<>();
                Map<String,Integer> clusterInventoryMap = new HashMap<>();
                clusterInventoryMap.put("Node", nodeCount);
                clusterInventoryMap.put("StorageClass", StorageClass);
                clusterInventoryMap.put("PersistentVolumeClaims", pvcCount);
                clusterInventoryMap.put("Pods", PodCount);
                clusterInventory.add(clusterInventoryMap);
                return Response.ok(clusterInventory).build();
            }
            catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("You are unauthorized to do this action.")
                        .build();
            }
            }
        }
        
        @Override
        public Response viewClusterNetwork(OpenShiftClient authenticatedClient) {
                if (authenticatedClient == null) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("You are not logged in.")
                            .build();
                }
                else{
                    try{
                    OpenShiftClient openShiftClient = authenticatedClient.adapt(OpenShiftClient.class);
                    List<io.fabric8.openshift.api.model.config.v1.Network>  clusterInfo = openShiftClient.config().networks().list().getItems();
                    List<ClusterNetwork> clusterNetworkInfo = new ArrayList<>();
                    for (io.fabric8.openshift.api.model.config.v1.Network network : clusterInfo) {
                        Gson gson = new Gson();
                        JsonElement jsonElement = gson.toJsonTree(network);
                        JsonObject jsonObject = (JsonObject) jsonElement.getAsJsonObject().get("spec");
                        // System.out.println(jsonElement.getAsJsonObject().get("kind"));
                        System.out.println("Network Type " + jsonObject.get("networkType"));
                        System.out.println("Service Network " + jsonObject.get("serviceNetwork"));
                        JsonArray clusterNetworkArray = jsonObject.getAsJsonArray("clusterNetwork");
                        System.out.println("Cluster Network:");
                        for (JsonElement clusterNetworkElement : clusterNetworkArray) {
                            JsonObject clusterNetworkObject = clusterNetworkElement.getAsJsonObject();
                            String cidr = clusterNetworkObject.get("cidr").getAsString();
                            int hostPrefix = clusterNetworkObject.get("hostPrefix").getAsInt();
                            System.out.println("- CIDR: " + cidr);
                            System.out.println("- Host Prefix: " + hostPrefix);
                        
                        // Map<String, String> clusterNetwork = new HashMap<>();
                        ClusterNetwork clusterNetwork = new ClusterNetwork();
                        clusterNetwork.setNetworkType(jsonObject.get("networkType").getAsString());
                        clusterNetwork.setServiceNetwork(jsonObject.get("serviceNetwork").getAsString());
                        clusterNetwork.setHostPrefix(clusterNetworkObject.get("hostPrefix").getAsString());
                        clusterNetwork.setCidr(clusterNetworkObject.get("cidr").getAsString());
                        // System.out.println("Network Type " + jsonObject.get("networkType"));
                        // clusterNetworkMap.put("serviceNetwork" , jsonObject.get("serviceNetwork").getAsString());
                        // clusterNetworkMap.put("cidr" , clusterNetworkObject.get("cidr").getAsString());
                        // clusterNetworkMap.put("hostPrefix" , clusterNetworkObject.get("hostPrefix").getAsString());
                        clusterNetworkInfo.add(clusterNetwork);
                    }}
                // System.out.println(clusterInfo);
        
                return Response.ok(clusterNetworkInfo).build();
        
            }

            catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("You are unauthorized to do this action.")
                        .build();
            }
        }
    }
        @Override
        public Response viewClusterConfig(OpenShiftClient authenticatedClient) {
            if (authenticatedClient == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("You are not logged in.")
                        .build();
            }
            else{
                try{
            OpenShiftClient openShiftClient = authenticatedClient.adapt(OpenShiftClient.class);
            InfrastructureList clusterConfig = openShiftClient.config().infrastructures().list();
            List<Map<String,String>> clusterConfigInfo = new ArrayList<>();
                Gson gson = new Gson();
                JsonElement jsonElement = gson.toJsonTree(clusterConfig);
                JsonArray jsonArray = jsonElement.getAsJsonObject().get("items").getAsJsonArray();
                
                for (JsonElement jsonElement2 : jsonArray) {
                    JsonObject jsonObject = jsonElement2.getAsJsonObject().get("status").getAsJsonObject().get("platformStatus").getAsJsonObject();
                    String apiServerInternalIP = jsonObject.get("baremetal").getAsJsonObject().get("apiServerInternalIP").getAsString();
                    String ingressIP = jsonObject.get("baremetal").getAsJsonObject().get("ingressIP").getAsString();
                    Map<String,String> clustMap = new HashMap<>();
                    clustMap.put("apiServerInternalIP", apiServerInternalIP);
                    clustMap.put("ingressIP", ingressIP);
                    clusterConfigInfo.add(clustMap);
                }
        
            return Response.ok(clusterConfigInfo).build();
        
        }

        catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("You are unauthorized to do this action.")
                    .build();
        }
    }
}
        
}


