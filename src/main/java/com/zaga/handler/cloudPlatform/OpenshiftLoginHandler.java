package com.zaga.handler.cloudPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zaga.entity.queryentity.openshift.ServiceList;
import com.zaga.repo.ServiceListRepo;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.OpenShiftClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class OpenshiftLoginHandler  implements LoginHandler{

     @Inject
    ServiceListRepo serviceListRepo;

    public OpenShiftClient login(String username, String password, String oauthToken, boolean useOAuthToken, String clusterUrl) {
        try {
            KubernetesClient kubernetesClient;
    
            if (useOAuthToken) {
                kubernetesClient = new KubernetesClientBuilder()
                    .withConfig(new ConfigBuilder()
                        .withOauthToken(oauthToken)
                        .withMasterUrl(clusterUrl)  //"https://api.zagaopenshift.zagaopensource.com:6443"
                        .build())
                    .build();
            } else {
                kubernetesClient = new KubernetesClientBuilder()
                    .withConfig(new ConfigBuilder()
                        .withPassword(password)
                        .withUsername(username)
                        .withMasterUrl(clusterUrl)
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

                        if (injectJavaValue == null) {
                            injectJavaValue = "false";
                        }

                        ServiceList serviceList = new ServiceList();
                        serviceList.setNamespaceName(namespaceName);  // Correct usage of setNamespaceName
                        serviceList.setServiceName(serviceName);
                        serviceList.setInstrumented(injectJavaValue);
                        serviceList.setDeploymentName(deploymentName);

                        serviceListRepo.persist(serviceList);

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
      

 

}
