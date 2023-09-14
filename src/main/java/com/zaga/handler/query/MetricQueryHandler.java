package com.zaga.handler.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zaga.entity.otelmetric.OtelMetric;
import com.zaga.repo.query.MetricQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricQueryHandler {

    @Inject
    MetricQueryRepo metricQueryRepo;

    @Inject
    MongoClient mongoClient;

    public List<OtelMetric> getMetricProduct(OtelMetric metric) {
        return metricQueryRepo.listAll();
    }

     public List<Map<String, Object>> getMetricsByServiceName(String serviceName) {
        // Specify your MongoDB database and collection name
        MongoDatabase database = mongoClient.getDatabase("OtelMetric");
        MongoCollection<Document> collection = database.getCollection("Metrics");

        // Create a query to filter documents where "resourceMetrics.resource.attributes.value.stringValue" matches serviceName
        Document query = new Document("resourceMetrics.resource.attributes.value.stringValue", serviceName);

        // Perform the query
        FindIterable<Document> results = collection.find(query);

        // Map the results to List<Map<String, Object>>
        List<Map<String, Object>> metrics = new ArrayList<>();
        for (Document result : results) {
            Map<String, Object> metric = mapDocumentToMap(result);
            metrics.add(metric);
        }

        return metrics;
    }

    private Map<String, Object> mapDocumentToMap(Document document) {
        Map<String, Object> metric = new HashMap<>();
    
        // Extract the relevant fields from the MongoDB document
        List<Document> resourceMetrics = document.getList("resourceMetrics", Document.class);
    
        if (resourceMetrics != null && !resourceMetrics.isEmpty()) {
            Document resource = resourceMetrics.get(0).get("resource", Document.class);
    
            if (resource != null) {
                List<Document> attributes = resource.getList("attributes", Document.class);
    
                if (attributes != null && !attributes.isEmpty()) {
                    for (Document attribute : attributes) {
                        String key = attribute.getString("key");
                        Document value = attribute.get("value", Document.class);
    
                        if (key != null && value != null) {
                            // Map the key and value to the metric
                            metric.put(key, value.getString("stringValue"));
                        }
                    }
                }
            }
        }
    
        return metric;
    }
    
    
}
