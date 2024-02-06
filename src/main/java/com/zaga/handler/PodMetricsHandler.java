package com.zaga.handler;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zaga.entity.queryentity.podMetrics.MetricDTO;
import com.zaga.entity.queryentity.podMetrics.PodMetricsResponseData;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.bson.Document;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class PodMetricsHandler {

    @Inject
    MongoClient mongoClient;

    public List<PodMetricsResponseData> getAllPodMetricsByDate() {
        LocalDateTime startTime = LocalDateTime.now();
        System.out.println("------------DB call startTimestamp------ " + startTime);

        MongoDatabase database = mongoClient.getDatabase("OtelPodMetrics");
        MongoCollection<Document> collection = database.getCollection("PodMetricDTO");

        List<PodMetricsResponseData> result = executeAggregationPipeline(database, collection);

        LocalDateTime endTime = LocalDateTime.now();
        System.out.println("------------DB call endTimestamp------ " + endTime);
        System.out.println("-----------DB call ended Timestamp------ " + Duration.between(startTime, endTime));

        return result;
    }

    public List<PodMetricsResponseData> executeAggregationPipeline(MongoDatabase database, MongoCollection<Document> collection) {
        List<Document> pipeline = Arrays.asList(
                new Document("$group",
                        new Document("_id", "$podName")
                                .append("podName", new Document("$first", "$podName"))
                                .append("metrics",
                                        new Document("$push",
                                                new Document("cpuUsage",
                                                        new Document("$arrayElemAt", Arrays.asList("$metrics.cpuUsage", 0L)))
                                                        .append("date",
                                                                new Document("$arrayElemAt", Arrays.asList("$metrics.date", 0L)))
                                                        .append("memoryUsage",
                                                                new Document("$arrayElemAt", Arrays.asList("$metrics.memoryUsage", 0L)))
                                        )
                                )
                ),
                new Document("$merge",
                        new Document("into", "mergedPodMetrics")
                                .append("whenMatched", "merge")
                                .append("whenNotMatched", "insert"))
        );

        // Execute the aggregation pipeline
        collection.aggregate(pipeline).toCollection();

        // Assuming you have a new collection named "mergedPodMetrics", query the documents from it
        MongoCollection<Document> mergedCollection = database.getCollection("mergedPodMetrics");
        return queryMergedPodMetrics(mergedCollection);
    }

    private List<PodMetricsResponseData> queryMergedPodMetrics(MongoCollection<Document> collection) {
        // Add your logic to query and convert documents from the merged collection
        // This depends on the structure of your PodMetricsResponseData class
        List<PodMetricsResponseData> result = new ArrayList<>();
        // Example: Convert documents to PodMetricsResponseData
        for (Document document : collection.find()) {
            result.add(fromDocument(document));
        }
        return result;
    }

    public static PodMetricsResponseData fromDocument(Document document) {
        PodMetricsResponseData responseData = new PodMetricsResponseData();
    
        // Extract values from the Document and set them in the responseData object
        responseData.setPodName(document.getString("podName"));
    
        // Extract metrics sub-document
        List<?> metricsList = document.get("metrics", List.class);
        if (metricsList != null) {
            for (Object metricObject : metricsList) {
                if (metricObject instanceof Document) {
                    Document metricDocument = (Document) metricObject;
                    MetricDTO metricDTO = new MetricDTO();
                    metricDTO.setCpuUsage(metricDocument.getDouble("cpuUsage"));
                    metricDTO.setDate(metricDocument.getDate("date"));
                    metricDTO.setMemoryUsage(metricDocument.getLong("memoryUsage"));
                    responseData.getMetrics().add(metricDTO);
                }
            }
        }
    
        // Set totalCount from the document
        // responseData.setTotalCount(document.getInteger("totalCount", 0));
    
        return responseData;
    }
}

