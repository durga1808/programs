package com.zaga.handler.query;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import com.zaga.entity.otellog.OtelLog;
import com.zaga.repo.query.LogQueryRepo;

import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LogQueryHandler {

    @Inject
    LogQueryRepo logQueryRepo;

    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;

    public LogQueryHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        collection = mongoClient.getDatabase("OtelLog").getCollection("Logs"); 
    }

    public List<OtelLog> getLogProduct(OtelLog logs) {
        return logQueryRepo.listAll();
    }


    public List<Document> findByServiceName(String serviceName) {
        List<Document> resultList = new ArrayList<>();

        Document query = new Document("resourceLogs.resource.attributes.value.stringValue", serviceName);
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                resultList.add(cursor.next());
            }
        }
        return resultList;
    }
}
