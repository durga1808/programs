package com.zaga.handler;

import java.util.ArrayList;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.zaga.entity.otelmetric.OtelMetric;
import com.zaga.repo.MetricQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricQueryHandler {

    @Inject
    MetricQueryRepo metricQueryRepo;

    @Inject
    MongoClient mongoClient;

    private final MongoCollection<Document> collection;

    public List<OtelMetric> getMetricProduct(OtelMetric metric) {
        return metricQueryRepo.listAll();
    }

    public MetricQueryHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        collection = mongoClient.getDatabase("OtelMetric").getCollection("Metrics");
    }
    
    public List<Document> getMetricsByServiceName(String serviceName) {
        List<Document> metrics = new ArrayList<>();

        Bson query = Filters.eq("resourceMetrics.resource.attributes.value.stringValue", serviceName);

        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                metrics.add(cursor.next());
            }
        }

        return metrics;
    }
}
