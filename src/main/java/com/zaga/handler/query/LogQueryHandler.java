package com.zaga.handler.query;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.repo.query.LogQueryRepo;

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
}
