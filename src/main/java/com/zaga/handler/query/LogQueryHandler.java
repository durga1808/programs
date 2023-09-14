package com.zaga.handler.query;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.repo.query.LogQueryRepo;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LogQueryHandler {

    @Inject
    LogQueryRepo logQueryRepo;

    
    @Inject
    MongoClient mongoClient;

    
    private final MongoCollection<Document> collection;


    public List<OtelLog> getLogProduct(OtelLog logs) {
        return logQueryRepo.listAll();
    }


    public LogQueryHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        collection = mongoClient.getDatabase("OtelLog").getCollection("Logs");
    }

    public List<Document> getLogByServiceName(String serviceName) {
        List<Document> logs = new ArrayList<>();

        // Create a query to filter documents where "resourceLogs.resource.attributes.value.stringValue" matches serviceName
        Bson query = Filters.eq("resourceLogs.resource.attributes.value.stringValue", serviceName);

        // Perform the query and return the documents
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                logs.add(cursor.next());
            }
        }

        return logs;
    }

    public List<Document> getLogsBySeverityText(String severityText) {
        List<Document> logs = new ArrayList<>();

        // Create a query to filter documents where "resourceLogs.scopeLogs.logRecords.body.stringValue" matches severityText
        Bson query = Filters.eq("resourceLogs.scopeLogs.logRecords.severityText", severityText);

        // Perform the query and return the documents
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                logs.add(cursor.next());
            }
        }

        return logs;
    }

    public List<Document> getLogsByServiceNameAndSeverityText(String serviceName, String severityText) {
        List<Document> logs = new ArrayList<>();
    
        // Create a query to filter documents where both "resourceLogs.resource.attributes.value.stringValue" matches serviceName
        // and "resourceLogs.scopeLogs.logRecords.body.stringValue" matches severityText
        Bson query = Filters.and(
            Filters.eq("resourceLogs.resource.attributes.value.stringValue", serviceName),
            Filters.eq("resourceLogs.scopeLogs.logRecords.severityText", severityText)
        );
    
        // Perform the query and return the documents
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                logs.add(cursor.next());
            }
        }
    
        return logs;
    }

    public List<Document> aggregateDocuments(String serviceName) {
        MongoDatabase database = mongoClient.getDatabase("OtelLog");
        MongoCollection<Document> collection = database.getCollection("Logs");


        List<Document> aggregationPipeline = Arrays.asList(
            new Document("$match", 
                new Document("resourceLogs.resource.attributes.value.stringValue", serviceName)),
            new Document("$project", 
                new Document("_id", 1L)
                    .append("resourceLogs.scopeLogs.logRecords.body", 1L)
                    .append("resourceLogs.scopeLogs.logRecords.observedTimeUnixNano", 1L)
                    .append("resourceLogs.scopeLogs.logRecords.severityText", 1L)
                    .append("resourceLogs.scopeLogs.logRecords.timeUnixNano", 1L)
                    .append("resourceLogs.scopeLogs.logRecords.spanId", 1L)
                    .append("resourceLogs.scopeLogs.logRecords.traceId", 1L)
            )
        );

        List<Document> result = collection.aggregate(aggregationPipeline, Document.class).into(new ArrayList<>());
        return result;
       
    }    
  

    


}

